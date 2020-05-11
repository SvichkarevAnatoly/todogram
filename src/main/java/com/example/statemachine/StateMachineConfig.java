package com.example.statemachine;

import com.example.statemachine.action.ShowMainKeyboard;
import com.example.statemachine.action.ShowTodayTasks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.data.mongodb.MongoDbPersistingStateMachineInterceptor;
import org.springframework.statemachine.data.mongodb.MongoDbStateMachineRepository;
import org.springframework.statemachine.persist.StateMachineRuntimePersister;
import org.springframework.statemachine.service.DefaultStateMachineService;
import org.springframework.statemachine.service.StateMachineService;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

    @Override
    public void configure(StateMachineConfigurationConfigurer<States, Events> config) throws Exception {

        config.withPersistence()
                .runtimePersister(mongoDbPersister);
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

    @Autowired
    public StateMachineRuntimePersister<States, Events, String> mongoDbPersister;

    @Autowired
    private StateMachineFactory<States, Events> stateMachineFactory;

    @Bean
    public StateMachineService<States, Events> stateMachineService() {
        return new DefaultStateMachineService<>(stateMachineFactory, mongoDbPersister);
    }

    @Bean
    public StateMachineRuntimePersister<States, Events, String> stateMachineRuntimePersister(
            MongoDbStateMachineRepository jpaStateMachineRepository) {
        return new MongoDbPersistingStateMachineInterceptor<>(jpaStateMachineRepository);
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
