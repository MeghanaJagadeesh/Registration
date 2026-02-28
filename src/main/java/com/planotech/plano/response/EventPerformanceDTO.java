package com.planotech.plano.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventPerformanceDTO {
    private Long eventId;
    private String eventName;
    private long registrations;
    private long checkIns;
    private double checkInRate;
    private long totalScans;
}
