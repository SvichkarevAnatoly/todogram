package com.example.bot;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Task {
    public String description;
    public String entry;
    public String modified;
    public String status;
    public String uuid;

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("description", description)
                .append("entry", entry)
                .append("modified", modified)
                .append("status", status)
                .append("uuid", uuid)
                .toString();
    }
}
