package com.example.studentrecordfinder.controller;

import com.example.studentrecordfinder.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AIChatController {

    // ⚠️ DEMO ONLY — ROTATE KEY AFTER TESTING
    private static final String GEMINI_API_KEY =
            BuildConfig.GEMINI_API_KEY;

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/"
                    + "gemini-2.5-flash:generateContent?key=" + GEMINI_API_KEY;

    private final OkHttpClient client =
            new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build();

    // ==================================================
    // ROLE MODE
    // ==================================================
    public enum RoleMode {
        FACULTY,
        ADMIN
    }

    private RoleMode roleMode = RoleMode.FACULTY;

    // ==================================================
    // RATE LIMIT
    // ==================================================
    private long lastRequestTime = 0;
    private static final long MIN_DELAY_MS = 3000;

    // ==================================================
    // MEMORY (LAST 3 TURNS)
    // ==================================================
    private static final int MAX_MEMORY = 6;
    private final List<String> conversationMemory = new ArrayList<>();

    // ==================================================
    // CALLBACK
    // ==================================================
    public interface AICallback {
        void onResponse(String reply);
    }

    // ==================================================
    // CONTEXT PROVIDER
    // ==================================================
    public interface ContextProvider {
        String getContext();
    }

    private ContextProvider contextProvider;

    // 🔒 FACULTY (BACKWARD COMPATIBLE)
    public void setStudentContextProvider(ContextProvider provider) {
        this.roleMode = RoleMode.FACULTY;
        this.contextProvider = provider;
    }

    // 🔓 ADMIN MODE
    public void enableAdminMode(ContextProvider provider) {
        this.roleMode = RoleMode.ADMIN;
        this.contextProvider = provider;
    }

    // ==================================================
    // MAIN ENTRY
    // ==================================================
    public void processQuery(String query, AICallback callback) {

        long now = System.currentTimeMillis();
        if (now - lastRequestTime < MIN_DELAY_MS) {
            callback.onResponse("Please wait a moment before asking again.");
            return;
        }
        lastRequestTime = now;

        addToMemory("User", query);

        // FAST RULE
        if (query.toLowerCase().contains("at risk")) {
            String reply =
                    "Students with attendance below 75% are considered at risk.";
            addToMemory("AI", reply);
            callback.onResponse(reply);
            return;
        }

        callGemini(query, reply -> {
            addToMemory("AI", reply);
            callback.onResponse(reply);
        });
    }

    // ==================================================
    // CONTEXT BUILDERS
    // ==================================================
    private String buildSystemContext() {

        if (roleMode == RoleMode.ADMIN) {
            return "System Context:\n" +
                    "- Role: ADMIN\n" +
                    "- Scope: All faculty and students\n" +
                    "- Task: Analyze workload, risks, trends\n\n";
        }

        // DEFAULT = FACULTY
        return "System Context:\n" +
                "- Role: FACULTY\n" +
                "- Scope: Own students only\n" +
                "- Attendance below 75% is at risk\n" +
                "- Marks include internal + external\n\n";
    }

    private void addToMemory(String role, String text) {
        conversationMemory.add(role + ": " + text);
        if (conversationMemory.size() > MAX_MEMORY) {
            conversationMemory.remove(0);
        }
    }

    private String buildConversationContext() {
        if (conversationMemory.isEmpty()) return "";
        StringBuilder sb = new StringBuilder("Conversation so far:\n");
        for (String m : conversationMemory) {
            sb.append(m).append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }

    private String buildDynamicContext() {
        if (contextProvider == null) return "";
        String ctx = contextProvider.getContext();
        if (ctx == null || ctx.isEmpty()) return "";
        return "Data Context:\n" + ctx + "\n\n";
    }

    // ==================================================
    // GEMINI API CALL
    // ==================================================
    private void callGemini(String query, AICallback callback) {

        try {
            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONArray parts = new JSONArray();

            String prompt =
                    buildSystemContext()
                            + buildConversationContext()
                            + buildDynamicContext()
                            + "User: " + query;

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
                    callback.onResponse("Network issue. Please try again.");
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {

                    if (response.code() == 429) {
                        callback.onResponse("AI busy. Try again shortly.");
                        return;
                    }

                    if (!response.isSuccessful()) {
                        callback.onResponse("AI error: " + response.code());
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

                        callback.onResponse(reply.trim());

                    } catch (Exception e) {
                        callback.onResponse("Failed to parse AI response.");
                    }
                }
            });

        } catch (Exception e) {
            callback.onResponse("AI request failed.");
        }
    }
}
