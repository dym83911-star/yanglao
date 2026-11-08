package com.elderlycare.dto;

import lombok.Data;

@Data
public class AuthSmsLoginDTO {
    private String phone;
    private String code;
}
