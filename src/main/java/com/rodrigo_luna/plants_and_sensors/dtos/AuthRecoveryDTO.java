package com.rodrigo_luna.plants_and_sensors.dtos;

import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class AuthRecoveryDTO {
    public String username;
    public String email;
}
