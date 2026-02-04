package com.planotech.plano.request;

import com.planotech.plano.enums.EventRole;
import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String email;
    private String phone;
    private String password;
    private EventRole role;

}
