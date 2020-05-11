package com.example.statemachine;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {

        config.withConfiguration()
                .autoStartup(true);
    }

    @Override
    public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {

        states.withStates()
                .initial(States.NOT_STARTED)
                .states(EnumSet.allOf(States.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {

        transitions.withExternal()
                .source(States.NOT_STARTED).target(States.MAIN_KEYBOARD)
                .action(showKeyboard())
                .action(showTodayTasks())
                .event(Events.SEND_START_COMMAND)
                .and()
                .withExternal()
                .source(States.MAIN_KEYBOARD).target(States.SETTINGS)
                .event(Events.PRESS_SETTINGS_BUTTON)
                .and()
                .withExternal()
                .source(States.SETTINGS).target(States.MAIN_KEYBOARD)
                .event(Events.PRESS_GO_TO_MAIN_KEYBOARD);
    }

    @Bean
    public ShowMainKeyboard showKeyboard() {
        return new ShowMainKeyboard();
    }

    @Bean
    public ShowTodayTasks showTodayTasks() {
        return new ShowTodayTasks();
    }
}
