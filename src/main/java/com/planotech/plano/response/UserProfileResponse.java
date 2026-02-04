package com.planotech.plano.response;

import com.planotech.plano.enums.EventRole;
import com.planotech.plano.enums.PlatformRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    Long userId;
    String name;
    String email;
    private PlatformRole platformRole;
    private EventRole highestEventRole;
}
