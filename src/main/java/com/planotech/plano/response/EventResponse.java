package com.planotech.plano.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private Long eventId;
    private String name;
    private String logoUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String eventKey;
    private String description;
    private UserDTO createdBy;
    private LocalDateTime createdAt;
    private List<EventUserDTO> assignedUsers;
}
