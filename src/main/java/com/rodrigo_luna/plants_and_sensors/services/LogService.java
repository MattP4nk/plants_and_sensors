package com.rodrigo_luna.plants_and_sensors.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rodrigo_luna.plants_and_sensors.dtos.LogDTO;
import com.rodrigo_luna.plants_and_sensors.dtos.ResponseDTO;
import com.rodrigo_luna.plants_and_sensors.models.LogModel;
import com.rodrigo_luna.plants_and_sensors.repositories.ILogRepository;

@Service
public class LogService {

    ResponseDTO response = new ResponseDTO();

    @Autowired
    ILogRepository logRepository;

    public void createLog(LogDTO logDTO) {
        LogModel log = LogModel.builder()
                .id(System.currentTimeMillis())
                .admin(logDTO.getAdmin())
                .area(logDTO.getArea())
                .command(logDTO.getCommand())
                .target(logDTO.getTarget())
                .date(LocalDateTime.now())
                .build();
        logRepository.save(log);
    }

    public ResponseDTO readLogs() {
        response.setStatus("OK");
        response.setPack(logRepository.findAll());
        return response;
    }

}
