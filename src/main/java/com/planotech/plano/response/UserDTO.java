package com.planotech.plano.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String name;
    private String email;
    private List<AssignedEventDTO> assignedEvents;
}