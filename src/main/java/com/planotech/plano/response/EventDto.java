package com.planotech.plano.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventDto {
    private Long eventId;
    private String name;
    private String description;
    private String eventKey;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String logoUrl;

    // Flattened fields (NOT entities)
    private Long createdById;
    private String createdByName;

    private Long companyId;
    private String companyName;
}
