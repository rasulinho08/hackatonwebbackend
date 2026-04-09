package com.project.soc.dto.auth;

import com.project.soc.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private Role role;
}
