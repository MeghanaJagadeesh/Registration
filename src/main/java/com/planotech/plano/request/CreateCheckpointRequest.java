package com.planotech.plano.request;

import com.planotech.plano.enums.CheckpointType;
import lombok.Data;

import java.util.Map;

@Data
public class CreateCheckpointRequest {
    private CheckpointType type;     // HALL / FOOD / CUSTOM
    private String name;             // Hall A / Snacks / VIP Entry
    private Map<String, Object> metadata; // hallName, foodType, session, etc
}
