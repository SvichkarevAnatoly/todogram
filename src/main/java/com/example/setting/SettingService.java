package com.example.setting;

public class SettingService {

    private SettingRepository repository;

    public SettingService(SettingRepository repository) {
        this.repository = repository;
    }

    // TODO: Вынести какой-то контекст с update, чтобы не передавать части update
    public String getDefaultProject(String userName) {
        return repository.findByUserName(userName)
                .map(Setting::getDefaultProject)
                .orElseGet(() -> repository.save(new Setting(userName, null))
                        .getDefaultProject());
    }

    public void setDefaultProject(String defaultProject, String userName) {
        final Setting setting = repository.findByUserName(userName)
                .orElseGet(() -> repository.save(new Setting(userName, null)));

        setting.setDefaultProject(defaultProject);
        repository.save(setting);
    }
}
