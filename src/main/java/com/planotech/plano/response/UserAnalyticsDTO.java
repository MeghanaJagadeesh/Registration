package com.planotech.plano.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserAnalyticsDTO {
    private long totalAssignedEvents;
    private long totalRegistrations;
    private long totalCheckIns;
    private long totalScans;
    private long todayScans;

    private double overallCheckInRate;

    private List<EventPerformanceDTO> eventPerformance;

    private String bestPerformingEvent;
    private String worstPerformingEvent;
    private String mostActiveEvent;
}
