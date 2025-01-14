package com.rodrigo_luna.plants_and_sensors.dtos;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Data
@NoArgsConstructor
public class PlantDTO {
    private String name;
    private String country;
}
