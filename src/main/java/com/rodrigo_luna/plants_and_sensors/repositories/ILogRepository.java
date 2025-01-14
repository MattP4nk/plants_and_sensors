package com.rodrigo_luna.plants_and_sensors.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.rodrigo_luna.plants_and_sensors.models.LogModel;

@Repository
public interface ILogRepository extends MongoRepository<LogModel, Integer> {

}
