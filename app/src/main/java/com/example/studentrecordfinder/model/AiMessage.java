package com.example.studentrecordfinder.model;

public class AiMessage {

    public String message;
    public boolean isUser;
    public boolean Typing;

    public AiMessage() {}

    public AiMessage(String message, boolean isUser) {
        this.message = message;
        this.isUser = isUser;
    }

    public static AiMessage typing() {
        AiMessage m = new AiMessage("Typing…", false);
        m.Typing = true;
        return m;
    }
}
