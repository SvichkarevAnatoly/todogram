package com.example.task.warrior;

import com.example.task.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;

class TaskWarriorServiceTest {

    @Test
    void name() throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final ObjectWriter objectWriter = objectMapper.writer()
                .withoutAttribute("entry");

        final Task task = new Task();
        task.description = "first task";
        task.entry = "lol";

        // assertEquals("{\"description\":\"first task\"}",
        //         objectWriter.writeValueAsString(task));
    }
}