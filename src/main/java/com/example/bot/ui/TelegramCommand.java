package com.example.bot.ui;

public enum TelegramCommand {
    START("start");

    private final String command;

    TelegramCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return command;
    }
}
