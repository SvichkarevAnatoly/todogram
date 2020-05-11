package com.example.statemachine;

import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {

        states.withStates()
                .initial(States.MAIN_KEYBOARD)
                .end(States.SETTINGS);
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {

        transitions.withExternal()
                .source(States.MAIN_KEYBOARD)
                .target(States.SETTINGS)
                .event(Events.PRESS_SETTINGS_BUTTON)
                .and()
                .withExternal()
                .source(States.SETTINGS)
                .target(States.MAIN_KEYBOARD)
                .event(Events.PRESS_GO_TO_MAIN_KEYBOARD);
    }
}
