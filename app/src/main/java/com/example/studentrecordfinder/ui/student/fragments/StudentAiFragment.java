package com.example.studentrecordfinder.ui.student.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.controller.StudentChatController;
import com.example.studentrecordfinder.model.AiMessage;
import com.example.studentrecordfinder.ui.student.fragments.adapters.AiChatAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class StudentAiFragment extends Fragment {

    private RecyclerView recycler;
    private EditText edtMessage;
    private ImageButton btnSend;

    private AiChatAdapter adapter;
    private final List<AiMessage> messages = new ArrayList<>();

    private StudentChatController chatController;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_student_ai_chat,
                container,
                false
        );

        recycler = view.findViewById(R.id.recyclerChat);
        edtMessage = view.findViewById(R.id.edtMessage);
        btnSend = view.findViewById(R.id.btnSend);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AiChatAdapter(messages);
        recycler.setAdapter(adapter);

        chatController = new StudentChatController();

        btnSend.setOnClickListener(v -> sendMessage());

        return view;
    }

    // ==================================================
    // TYPING INDICATOR
    // ==================================================
    private void showTypingIndicator() {
        messages.add(AiMessage.typing());
        adapter.notifyItemInserted(messages.size() - 1);
        recycler.scrollToPosition(messages.size() - 1);
    }

    private void removeTypingIndicator() {
        if (!messages.isEmpty()
                && messages.get(messages.size() - 1).Typing) {

            int pos = messages.size() - 1;
            messages.remove(pos);
            adapter.notifyItemRemoved(pos);
        }
    }

    // ==================================================
    // SEND MESSAGE
    // ==================================================
    private void sendMessage() {

        String msg = edtMessage.getText().toString().trim();
        if (msg.isEmpty()) return;

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(getContext(),
                    "User not authenticated",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        edtMessage.setText("");

        // USER MESSAGE
        messages.add(new AiMessage(msg, true));
        adapter.notifyItemInserted(messages.size() - 1);
        recycler.scrollToPosition(messages.size() - 1);

        showTypingIndicator();
        btnSend.setEnabled(false);

        FirebaseDatabase.getInstance()
                .getReference("students")
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        removeTypingIndicator();
                        btnSend.setEnabled(true);
                        Toast.makeText(getContext(),
                                "Student data not found",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // ✅ SUMMARIZED CONTEXT (VERY IMPORTANT)
                    StringBuilder ctx = new StringBuilder();
                    ctx.append("Student Profile:\n");
                    ctx.append("Name: ").append(snapshot.child("name").getValue(String.class)).append("\n");
                    ctx.append("Roll No: ").append(snapshot.child("roll_no").getValue(String.class)).append("\n");
                    ctx.append("Department: ").append(snapshot.child("department").getValue(String.class)).append("\n\n");

                    ctx.append("Attendance Summary:\n");
                    for (com.google.firebase.database.DataSnapshot sub : snapshot.child("attendance").getChildren()) {
                        Integer pct = sub.child("percentage").getValue(Integer.class);
                        if (pct != null && pct < 75) {
                            ctx.append("- ").append(sub.getKey()).append(": ")
                                    .append(pct).append("% (At Risk)\n");
                        }
                    }

                    chatController.sendMessage(
                            msg,
                            ctx.toString(),
                            new StudentChatController.AIChatCallback() {

                                @Override
                                public void onSuccess(String reply) {
                                    if (getActivity() == null) return;
                                    getActivity().runOnUiThread(() -> {
                                        removeTypingIndicator();
                                        messages.add(new AiMessage(reply, false));
                                        adapter.notifyItemInserted(messages.size() - 1);
                                        recycler.scrollToPosition(messages.size() - 1);
                                        btnSend.setEnabled(true);
                                    });
                                }

                                @Override
                                public void onFailure(String error) {
                                    if (getActivity() == null) return;
                                    getActivity().runOnUiThread(() -> {
                                        removeTypingIndicator();
                                        btnSend.setEnabled(true);
                                        Toast.makeText(getContext(),
                                                error,
                                                Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                    );
                })
                .addOnFailureListener(e -> {
                    removeTypingIndicator();
                    btnSend.setEnabled(true);
                    Toast.makeText(getContext(),
                            "Failed to load student data",
                            Toast.LENGTH_SHORT).show();
                });
    }
}
