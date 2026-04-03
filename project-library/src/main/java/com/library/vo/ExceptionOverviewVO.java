package com.library.vo;

import lombok.Data;

@Data
public class ExceptionOverviewVO {

    private long totalCount;
    private long todayCount;
    private long pendingCount;
    private long resolvedCount;
    private long ignoredCount;
    private long highRiskCount;
}
