package com.example.statemachine;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = StateMachineConfig.class)
public class StateMachineConfigTest {

    private StateMachine<States, Events> stateMachine;

    @Autowired
    private StateMachineFactory<States, Events> stateMachineFactory;

    @Before
    public void setUp() {
        stateMachine = stateMachineFactory.getStateMachine();
    }

    @Test
    public void initTest() {
        stateMachine.start();
        Assertions.assertThat(stateMachine.getState().getId())
                .isEqualTo(States.MAIN_KEYBOARD);
        Assertions.assertThat(stateMachine).isNotNull();
    }

    @Test
    public void greenFlow() {
        stateMachine.start();
        stateMachine.sendEvent(Events.PRESS_SETTINGS_BUTTON);
        Assertions.assertThat(stateMachine.getState().getId())
                .isEqualTo(States.SETTINGS);
    }
}