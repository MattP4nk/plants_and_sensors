package com.rodrigo_luna.plants_and_sensors.dtos;

import lombok.Data;

@Data
public class ChangePasswordDTO {
    public String username;
    public String oldPassword;
    public String newPassword;
}
