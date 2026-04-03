package com.library.security.filter;

import com.library.common.utils.ContextHolder;
import com.library.common.utils.JwtUtils;
import com.library.entity.User;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWT授权过滤器
 */
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {
        try {
            String header = request.getHeader("Authorization");
            String token = null;
            String username = null;

            if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                token = header.substring(7);
                try {
                    username = jwtUtils.getUsernameFromToken(token);
                } catch (Exception e) {
                    logger.error("JWT token解析失败: " + e.getMessage());
                }
            }

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtils.validateToken(token)) {
                    // 获取用户信息
                    User currentUser = userService.getByUsername(username);
                    if (currentUser != null) {
                        // 获取用户角色列表，构建权限
                        List<String> roleCodes = userService.getUserRoleCodes(currentUser.getUserId());
                        List<String> permissionCodes = userService.getUserPermissionCodes(currentUser.getUserId());
                        Set<String> authorityCodes = new LinkedHashSet<>();
                        authorityCodes.addAll(roleCodes.stream()
                                .map(role -> "ROLE_" + role)
                                .collect(Collectors.toList()));
                        authorityCodes.addAll(permissionCodes);
                        List<SimpleGrantedAuthority> authorities = authorityCodes.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        // 设置当前用户到上下文
                        ContextHolder.setCurrentUser(currentUser);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("JWT认证处理异常: " + e.getMessage());
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // 请求结束清理ThreadLocal
            ContextHolder.clear();
        }
    }
}
