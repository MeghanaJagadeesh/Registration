package com.planotech.plano.response;

import com.planotech.plano.enums.CheckpointType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckpointResponse {

    private Long checkpointId;
    private String name;
    private CheckpointType type;
    private Boolean systemDefined;
    private Boolean active;

    // Optional
    private Map<String, Object> metadata;
}

