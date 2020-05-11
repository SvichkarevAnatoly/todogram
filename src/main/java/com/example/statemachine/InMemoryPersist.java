package com.example.statemachine;

import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;

import java.util.HashMap;
import java.util.Map;

public class InMemoryPersist implements StateMachinePersist<States, Events, String> {

    private final Map<String, StateMachineContext<States, Events>> storage = new HashMap<>();

    @Override
    public void write(StateMachineContext<States, Events> context, String contextObj) {
        storage.put(contextObj, context);
    }

    @Override
    public StateMachineContext<States, Events> read(String contextObj) {
        return storage.get(contextObj);
    }
}
