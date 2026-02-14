package com.example.studentrecordfinder.controller;

public interface LLMService {

    void ask(String prompt, LLMCallback callback);

    interface LLMCallback {
        void onResponse(String reply);
        void onError(String error);
    }
}
