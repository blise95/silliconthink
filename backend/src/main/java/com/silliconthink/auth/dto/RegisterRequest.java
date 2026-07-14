package com.silliconthink.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 4, max = 32, message = "username length must be 4-32")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "username may only contain letters, digits and underscore")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 8, max = 64, message = "password length must be 8-64")
    private String password;

    @Size(max = 64, message = "displayName too long")
    private String displayName;
}
