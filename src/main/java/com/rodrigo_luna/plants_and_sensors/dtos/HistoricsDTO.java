package com.rodrigo_luna.plants_and_sensors.dtos;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class HistoricsDTO {
    public String type;
    public Integer safe;
    public Integer warning;
    public Integer redAlert;
    public Integer disabled;
}
