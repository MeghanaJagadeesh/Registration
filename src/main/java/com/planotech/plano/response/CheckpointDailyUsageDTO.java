package com.planotech.plano.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckpointDailyUsageDTO {

    private LocalDate date;
    private String checkpointName;
    private long totalScans;
}
