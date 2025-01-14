package com.rodrigo_luna.plants_and_sensors.dtos;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PasswordRecoveryDTO {
    String username;
    @NotEmpty(message = "The password can't be empty")
    @Length(min = 8, message = "Password minimum length is 8 characters")
    String newPass;

    String confirmation;
}
