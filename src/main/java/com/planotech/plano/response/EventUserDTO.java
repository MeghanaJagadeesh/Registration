package com.planotech.plano.response;

import com.planotech.plano.enums.EventRole;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventUserDTO {
    private Long userId;
    private String name;
    private String email;
    private EventRole role;
}
