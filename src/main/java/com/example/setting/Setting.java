package com.example.setting;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.data.annotation.Id;

/**
 * Модель для настроек юзера
 */
public class Setting {

    @Id
    private String id;
    private String userName;

    private String defaultProject;

    public Setting() {
    }

    public Setting(String userName, String defaultProject) {
        this.userName = userName;
        this.defaultProject = defaultProject;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDefaultProject() {
        return defaultProject;
    }

    public void setDefaultProject(String defaultProject) {
        this.defaultProject = defaultProject;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("id", id)
                .append("userName", userName)
                .append("defaultProject", defaultProject)
                .toString();
    }
}
