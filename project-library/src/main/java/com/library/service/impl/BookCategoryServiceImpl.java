package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.common.exception.BusinessException;
import com.library.dto.CategoryDTO;
import com.library.entity.BookCategory;
import com.library.mapper.BookCategoryMapper;
import com.library.service.BookCategoryService;
import com.library.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 图书分类服务实现类
 */
@Service
public class BookCategoryServiceImpl extends ServiceImpl<BookCategoryMapper, BookCategory> implements BookCategoryService {

    @Override
    public List<CategoryVO> getCategoryTree() {
        LambdaQueryWrapper<BookCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(BookCategory::getSortOrder);
        List<BookCategory> all = baseMapper.selectList(wrapper);
        return buildTree(all);
    }

    @Override
    public List<CategoryVO> getEnabledCategories() {
        LambdaQueryWrapper<BookCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookCategory::getStatus, 1);
        wrapper.orderByAsc(BookCategory::getSortOrder);
        List<BookCategory> list = baseMapper.selectList(wrapper);
        return list.stream().map(this::entityToVO).collect(Collectors.toList());
    }

    @Override
    public BookCategory getByCategoryCode(String categoryCode) {
        LambdaQueryWrapper<BookCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookCategory::getCategoryCode, categoryCode);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public void addCategory(CategoryDTO dto, Long creatorId) {
        BookCategory entity = dtoToEntity(dto);
        entity.setCreatorId(creatorId);

        // 自动计算层级
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            BookCategory parent = baseMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException(400, "上级分类不存在");
            }
            entity.setCategoryLevel(parent.getCategoryLevel() + 1);
        } else {
            entity.setCategoryLevel(1);
            entity.setParentCategoryId(null);
        }

        // 自动生成分类编码（CAT + 时间戳）
        entity.setCategoryCode("CAT" + System.currentTimeMillis());

        // 默认值
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }

        baseMapper.insert(entity);
    }

    @Override
    public void updateCategory(Long id, CategoryDTO dto) {
        BookCategory existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "分类不存在");
        }

        // 防止自引用
        if (dto.getParentId() != null && dto.getParentId().equals(id)) {
            throw new BusinessException(400, "上级分类不能是自身");
        }

        // 部分更新
        BookCategory entity = new BookCategory();
        entity.setCategoryId(id);
        entity.setCategoryName(dto.getName());

        if (dto.getParentId() != null && dto.getParentId() > 0) {
            BookCategory parent = baseMapper.selectById(dto.getParentId());
            if (parent == null) {
                throw new BusinessException(400, "上级分类不存在");
            }
            entity.setParentCategoryId(dto.getParentId());
            entity.setCategoryLevel(parent.getCategoryLevel() + 1);
        } else {
            entity.setParentCategoryId(null);
            entity.setCategoryLevel(1);
        }

        if (dto.getSort() != null) {
            entity.setSortOrder(dto.getSort());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }

        baseMapper.updateById(entity);
    }

    /**
     * O(n) 递归树构建：按 parentId 分组 → 挂载 children → 返回根节点
     */
    private List<CategoryVO> buildTree(List<BookCategory> all) {
        List<CategoryVO> voList = all.stream().map(this::entityToVO).collect(Collectors.toList());

        // 按 parentId 分组
        Map<Long, List<CategoryVO>> parentMap = voList.stream()
                .filter(vo -> vo.getParentId() != null && vo.getParentId() > 0)
                .collect(Collectors.groupingBy(CategoryVO::getParentId));

        // 挂载 children
        for (CategoryVO vo : voList) {
            List<CategoryVO> children = parentMap.get(vo.getId());
            if (children != null) {
                vo.setChildren(children);
            }
        }

        // 返回根节点（parentId 为 null 或 0）
        return voList.stream()
                .filter(vo -> vo.getParentId() == null || vo.getParentId() == 0)
                .collect(Collectors.toList());
    }

    /**
     * Entity → VO 转换
     */
    private CategoryVO entityToVO(BookCategory entity) {
        CategoryVO vo = new CategoryVO();
        vo.setId(entity.getCategoryId());
        vo.setName(entity.getCategoryName());
        vo.setParentId(entity.getParentCategoryId());
        vo.setSort(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    /**
     * DTO → Entity 转换
     */
    private BookCategory dtoToEntity(CategoryDTO dto) {
        BookCategory entity = new BookCategory();
        entity.setCategoryName(dto.getName());
        entity.setParentCategoryId(dto.getParentId());
        entity.setSortOrder(dto.getSort());
        entity.setStatus(dto.getStatus());
        return entity;
    }
}
