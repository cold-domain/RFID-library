package com.library.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.library.common.utils.ContextHolder;
import com.library.common.vo.Result;
import com.library.entity.ExceptionRecord;
import com.library.service.ExceptionRecordService;
import com.library.vo.ExceptionRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;

/**
 * 异常记录控制器（管理员权限）
 */
@RestController
@RequestMapping("/admin/exceptions")
public class ExceptionController {

    @Autowired
    private ExceptionRecordService exceptionRecordService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 分页查询异常记录
     */
    @GetMapping
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

    /**
     * 查询异常详情
     */
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        ExceptionRecord record = exceptionRecordService.getById(id);
        if (record == null) {
            return Result.error("异常记录不存在");
        }
        return Result.success(toVO(record));
    }

    /**
     * 处理异常（前端用params发送，后端用@RequestParam接收）
     */
    @PostMapping("/{id}/resolve")
    public Result<?> resolve(@PathVariable Long id, @RequestParam String resolveNote) {
        Long resolverId = ContextHolder.getCurrentUserId();
        exceptionRecordService.resolveException(id, resolverId, resolveNote);
        return Result.success("异常已处理");
    }

    private ExceptionRecordVO toVO(ExceptionRecord record) {
        ExceptionRecordVO vo = new ExceptionRecordVO();
        vo.setId(record.getExceptionId());
        vo.setExceptionType(record.getExceptionType());
        vo.setExceptionLevel(record.getExceptionLevel());
        vo.setExceptionContent(record.getExceptionMessage());
        vo.setExceptionSource(record.getExceptionSource());
        vo.setUsername(record.getUsername());
        vo.setIpAddress(record.getIpAddress());
        vo.setRequestUrl(record.getRequestUrl());
        vo.setRequestMethod(record.getRequestMethod());
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
