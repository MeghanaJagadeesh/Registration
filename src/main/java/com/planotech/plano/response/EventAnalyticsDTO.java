package com.planotech.plano.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EventAnalyticsDTO {
    private long totalRegistrations;
    private long todayRegistrations;
    private long totalCheckIns;
    private long todayCheckIns;
    private double checkInRate;

    private long totalScans;
    private long todayScans;

    private RegistrationGrowthDTO registrationGrowth;

    // Trends
    private List<RegistrationTrendDTO> registrationTrend;
    private List<CheckpointDailyUsageDTO> checkpointDailyUsage;

    // Breakdown
    private List<CheckpointUsageDTO> checkpointTotalUsage;

    private String mostActiveCheckpoint;

}
