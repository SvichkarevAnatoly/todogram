package com.example.statemachine;

import org.springframework.statemachine.data.mongodb.MongoDbStateMachineRepository;

/**
 * Для сохранения состояния state machine
 */
public interface StateMachineRepository extends MongoDbStateMachineRepository {
}
