package com.example.studentrecordfinder.controller;

import org.json.JSONObject;

import okhttp3.*;

import java.io.IOException;

public class RemoteLLMService implements LLMService {

    private static final String API_URL = "https://YOUR_BACKEND_URL/ai/chat";
    private final OkHttpClient client = new OkHttpClient();

    @Override
    public void ask(String prompt, LLMCallback callback) {

        try {
            JSONObject json = new JSONObject();
            json.put("prompt", prompt);

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError("Network error");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        callback.onError("Server error");
                        return;
                    }

                    try {
                        JSONObject res = new JSONObject(response.body().string());
                        callback.onResponse(res.getString("reply"));
                    } catch (Exception e) {
                        callback.onError("Invalid response");
                    }
                }
            });

        } catch (Exception e) {
            callback.onError("Request error");
        }
    }
}
