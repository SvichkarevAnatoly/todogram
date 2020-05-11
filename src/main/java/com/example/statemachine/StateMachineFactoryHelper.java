package com.example.statemachine;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.persist.StateMachinePersister;

public class StateMachineFactoryHelper {

    @Autowired
    private StateMachineFactory<States, Events> stateMachineFactory;

    @Autowired
    private StateMachinePersister<States, Events, String> persister;

    public StateMachine<States, Events> getStateMachine(String key) {
        final StateMachine<States, Events> stateMachine = stateMachineFactory.getStateMachine(key);
        try {
            persister.restore(stateMachine, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stateMachine;
    }

    public void saveState(StateMachine<States, Events> stateMachine, String key) {
        try {
            persister.persist(stateMachine, key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
