package com.example.studentrecordfinder.controller;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;

public class FacultyChatController {
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_API_KEY = buildConfig.GROQ_API_KEY;
    private final OkHttpClient client = new OkHttpClient();

    public void sendMessage(String userMessage, AIChatCallback callback) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("model", "llama3-8b-8192");

            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system")
                    .put("content", "You are an AI for Faculty. If a user asks for attendance or marks of a roll number, " +
                            "mention the roll number and the words 'FETCH_ATTENDANCE' or 'FETCH_MARKS' in your reply."));

            messages.put(new JSONObject().put("role", "user").put("content", userMessage));
            payload.put("messages", messages);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(RequestBody.create(payload.toString(), MediaType.parse("application/json")))
                    .addHeader("Authorization", "Bearer " + GROQ_API_KEY)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) { callback.onFailure("AI Offline"); }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            String reply = new JSONObject(response.body().string())
                                    .getJSONArray("choices").getJSONObject(0)
                                    .getJSONObject("message").getString("content");
                            callback.onSuccess(reply);
                        } catch (Exception e) { callback.onFailure("Parse Error"); }
                    }
                }
            });
        } catch (Exception e) { callback.onFailure("Request Error"); }
    }

    public interface AIChatCallback {
        void onSuccess(String reply);
        void onFailure(String error);
    }
}