package com.example.setting;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SettingRepository extends MongoRepository<Setting, String> {

    Optional<Setting> findByUserName(String userName);
}
