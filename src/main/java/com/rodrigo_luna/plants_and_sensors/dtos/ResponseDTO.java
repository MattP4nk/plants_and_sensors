package com.rodrigo_luna.plants_and_sensors.dtos;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@NoArgsConstructor
public class ResponseDTO {
    private String status;
    private String token;
    private String toLog;
    private Object pack;
}
