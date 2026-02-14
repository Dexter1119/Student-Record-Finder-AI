package com.example.studentrecordfinder.ui.faculty.chat;

public class ChatMessage {
    public String text;
    public boolean isUser;
    public boolean isTyping;

    public ChatMessage(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
        this.isTyping = false;
    }

    public static ChatMessage typing() {
        ChatMessage m = new ChatMessage("Typing…", false);
        m.isTyping = true;
        return m;
    }
}
