package com.planotech.plano.response;

import com.planotech.plano.enums.CheckpointType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckpointLogResponse {

    private String attendeeName;
    private String attendeeEmail;

    private String checkpointName;
    private CheckpointType checkpointType;

    private String scannedBy;
    private LocalDateTime scannedAt;
}
