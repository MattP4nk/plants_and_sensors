package com.rodrigo_luna.plants_and_sensors.dtos;

import org.springframework.stereotype.Component;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Component
@Data
public class RequestDTO {
    @NotBlank(message = "Area can't be blank")
    @NotNull(message = "Area can't be blank")
    private String area;
    @NotBlank(message = "Command can't be blank")
    @NotNull(message = "Command can't be null")
    private String command;
    private String target;
    private Long targetId;
    private RegistrationDTO regInfo;
    private PlantDTO plant;
    private SensorDTO sensor;
    private LoginDTO credentials;
    private AuthRecoveryDTO recoveryDTO;
    private ChangePasswordDTO newCredentials;
    private String key;
}
