package com.library.vo;

import lombok.Data;

@Data
public class AuditLogOverviewVO {

    private long totalCount;
    private long todayCount;
    private long errorCount;
    private long securityCount;
    private long successCount;
}
