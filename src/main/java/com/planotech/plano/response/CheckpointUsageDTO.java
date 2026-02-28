package com.planotech.plano.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CheckpointUsageDTO {
    private String checkpointName;
    private long totalScans;
    private long uniqueAttendees;
    private long todayScans;

}
