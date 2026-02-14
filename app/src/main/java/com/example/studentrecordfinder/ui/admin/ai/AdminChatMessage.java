package com.example.studentrecordfinder.ui.admin.ai;

public class AdminChatMessage {

    public String text;
    public boolean isUser;
    public boolean isTyping;

    public AdminChatMessage(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
        this.isTyping = false;
    }

    public static AdminChatMessage typing() {
        AdminChatMessage m = new AdminChatMessage("Analyzing…", false);
        m.isTyping = true;
        return m;
    }
}
