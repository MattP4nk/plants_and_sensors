package com.rodrigo_luna.plants_and_sensors.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import com.rodrigo_luna.plants_and_sensors.models.UserModel;

@Repository
public interface IUserRepository extends MongoRepository<UserModel, Long> {
    UserModel findByUsername(String username) throws UsernameNotFoundException;
}
