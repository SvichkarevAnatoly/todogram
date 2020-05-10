package com.example.project;

import java.util.List;
import java.util.stream.Collectors;

public class ProjectService {

    private ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    public List<String> getProjects() {
        // TODO: Можено вроде явно из репозитория запросить
        return repository.findAll().stream()
                .map(Project::getName)
                .collect(Collectors.toList());
    }
}
