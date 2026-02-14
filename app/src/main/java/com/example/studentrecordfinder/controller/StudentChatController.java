package com.example.studentrecordfinder.controller;

import com.example.studentrecordfinder.model.AiMessage;
import com.example.studentrecordfinder.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.*;

public class StudentChatController {

    // 🔥 SAME GEMINI KEY STYLE AS ADMIN/FACULTY
    private static final String GEMINI_API_KEY =
            BuildConfig.GEMINI_API_KEY;

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY;

    private final OkHttpClient client =
            new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build();

    // 🧠 MEMORY (last 3 turns)
    private static final int MAX_MEMORY = 6;
    private final List<AiMessage> memory = new ArrayList<>();

    // ==================================================
    // PUBLIC API
    // ==================================================
    public void sendMessage(
            String userMessage,
            String studentContext,
            AIChatCallback callback
    ) {

        try {
            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONArray parts = new JSONArray();

            // 🔐 STRICT SYSTEM PROMPT
            String prompt =
                    "You are an AI Academic Assistant for ONE student.\n" +
                            "Use ONLY the provided student data.\n" +
                            "Never mention other students, faculty, admin, or system internals.\n" +
                            "Be supportive, concise, and actionable.\n\n" +
                            "Student Data:\n" +
                            studentContext +
                            "\nConversation:\n" +
                            buildConversationMemory() +
                            "\nUser: " + userMessage;

            parts.put(new JSONObject().put("text", prompt));
            contents.put(new JSONObject().put("parts", parts));
            body.put("contents", contents);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(RequestBody.create(
                            body.toString(),
                            MediaType.parse("application/json")))
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onFailure("Network issue. Please try again.");
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {

                    if (!response.isSuccessful()) {
                        callback.onFailure("AI error: " + response.code());
                        return;
                    }

                    try {
                        JSONObject res =
                                new JSONObject(response.body().string());

                        String reply =
                                res.getJSONArray("candidates")
                                        .getJSONObject(0)
                                        .getJSONObject("content")
                                        .getJSONArray("parts")
                                        .getJSONObject(0)
                                        .getString("text");

                        // 💾 Save memory
                        memory.add(new AiMessage(userMessage, true));
                        memory.add(new AiMessage(reply, false));

                        while (memory.size() > MAX_MEMORY) {
                            memory.remove(0);
                        }

                        callback.onSuccess(reply.trim());

                    } catch (Exception e) {
                        callback.onFailure("Failed to parse AI response.");
                    }
                }
            });

        } catch (Exception e) {
            callback.onFailure("AI request failed.");
        }
    }

    // ==================================================
    // MEMORY BUILDER
    // ==================================================
    private String buildConversationMemory() {

        if (memory.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();

        for (AiMessage m : memory) {
            sb.append(m.isUser ? "User: " : "AI: ")
                    .append(m.message)
                    .append("\n");
        }

        return sb.toString();
    }

    // ==================================================
    // CALLBACK
    // ==================================================
    public interface AIChatCallback {
        void onSuccess(String reply);
        void onFailure(String error);
    }
}
