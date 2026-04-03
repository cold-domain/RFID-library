package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.utils.ContextHolder;
import com.library.common.vo.Result;
import com.library.entity.ExceptionRecord;
import com.library.service.ExceptionRecordService;
import com.library.vo.ExceptionRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/admin/exceptions")
public class ExceptionController {

    @Autowired
    private ExceptionRecordService exceptionRecordService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GetMapping
    @PreAuthorize("hasAuthority('exception:view')")
    public Result<?> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String exceptionType,
            @RequestParam(required = false) String exceptionLevel,
            @RequestParam(required = false) Integer resolvedStatus) {
        IPage<ExceptionRecord> page = exceptionRecordService.getExceptionPage(pageNum, pageSize, exceptionType, exceptionLevel, resolvedStatus);
        IPage<ExceptionRecordVO> voPage = page.convert(this::toVO);
        return Result.success(voPage);
    }

    @GetMapping("/overview")
    @PreAuthorize("hasAuthority('exception:view')")
    public Result<?> overview() {
        return Result.success(exceptionRecordService.getOverview());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('exception:view')")
    public Result<?> getById(@PathVariable Long id) {
        ExceptionRecord record = exceptionRecordService.getById(id);
        if (record == null) {
            return Result.error("\u5f02\u5e38\u8bb0\u5f55\u4e0d\u5b58\u5728");
        }
        return Result.success(toVO(record));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('exception:resolve')")
    public Result<?> resolve(@PathVariable Long id, @RequestParam String resolveNote) {
        Long resolverId = ContextHolder.getCurrentUserId();
        exceptionRecordService.resolveException(id, resolverId, resolveNote);
        return Result.success("\u5f02\u5e38\u5df2\u5904\u7406");
    }

    @PostMapping("/{id}/ignore")
    @PreAuthorize("hasAuthority('exception:resolve')")
    public Result<?> ignore(@PathVariable Long id, @RequestParam(required = false) String resolveNote) {
        Long resolverId = ContextHolder.getCurrentUserId();
        exceptionRecordService.ignoreException(id, resolverId, resolveNote);
        return Result.success("\u5f02\u5e38\u5df2\u5ffd\u7565");
    }

    private ExceptionRecordVO toVO(ExceptionRecord record) {
        ExceptionRecordVO vo = new ExceptionRecordVO();
        vo.setId(record.getExceptionId());
        vo.setExceptionType(record.getExceptionType());
        vo.setExceptionLevel(record.getExceptionLevel());
        vo.setExceptionContent(record.getExceptionMessage());
        vo.setExceptionSource(record.getExceptionSource());
        vo.setExceptionDetails(record.getExceptionDetails());
        vo.setUsername(record.getUsername());
        vo.setIpAddress(record.getIpAddress());
        vo.setRequestUrl(record.getRequestUrl());
        vo.setRequestMethod(record.getRequestMethod());
        vo.setRequestSummary(record.getRequestSummary());
        vo.setStackTraceSummary(record.getStackTraceSummary());
        vo.setResolvedStatus(record.getResolvedStatus());
        vo.setResolveNote(record.getResolveNote());
        if (record.getResolveTime() != null) {
            vo.setResolveTime(record.getResolveTime().format(FMT));
        }
        if (record.getCreateTime() != null) {
            vo.setCreateTime(record.getCreateTime().format(FMT));
        }
        return vo;
    }
}
