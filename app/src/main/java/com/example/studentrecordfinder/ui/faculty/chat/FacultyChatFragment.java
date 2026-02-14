package com.example.studentrecordfinder.ui.faculty.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.controller.AIChatController;
import com.example.studentrecordfinder.controller.FacultyController;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class FacultyChatFragment extends Fragment {

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

    // Controllers (STRICT MVC)
    private AIChatController aiController;
    private FacultyController facultyController;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_faculty_chat, container, false);

        RecyclerView recycler = v.findViewById(R.id.recyclerChat);
        EditText edt = v.findViewById(R.id.edtMessage);
        ImageButton send = v.findViewById(R.id.btnSend);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatAdapter(messages);
        recycler.setAdapter(adapter);

        // Init controllers
        aiController = new AIChatController();
        facultyController = new FacultyController();

        // -----------------------------
        // INJECT STUDENT CONTEXT (ONCE)
        // -----------------------------
        injectStudentContext();

        // -----------------------------
        // SEND MESSAGE
        // -----------------------------
        send.setOnClickListener(btn -> {

            String q = edt.getText().toString().trim();
            if (q.isEmpty()) return;

            // User message
            messages.add(new ChatMessage(q, true));
            adapter.notifyItemInserted(messages.size() - 1);
            recycler.scrollToPosition(messages.size() - 1);
            edt.setText("");

            send.setEnabled(false);

            // Typing indicator
            ChatMessage typing = ChatMessage.typing();
            messages.add(typing);
            int typingPos = messages.size() - 1;
            adapter.notifyItemInserted(typingPos);
            recycler.scrollToPosition(typingPos);

            // AI response
            aiController.processQuery(q, reply -> {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {

                    // Remove typing indicator
                    if (typingPos < messages.size()) {
                        messages.remove(typingPos);
                        adapter.notifyItemRemoved(typingPos);
                    }

                    // Add AI reply
                    messages.add(new ChatMessage(reply, false));
                    adapter.notifyItemInserted(messages.size() - 1);
                    recycler.scrollToPosition(messages.size() - 1);

                    send.setEnabled(true);
                });
            });
        });

        return v;
    }

    // ==================================================
    // STUDENT CONTEXT INJECTION (MVC-CORRECT)
    // ==================================================
    private void injectStudentContext() {

        facultyController.fetchAllStudents(new FacultyController.FacultyCallback() {
            @Override
            public void onSuccess(DataSnapshot snapshot) {

                StringBuilder sb = new StringBuilder();

                for (DataSnapshot s : snapshot.getChildren()) {

                    String name = s.child("name").getValue(String.class);
                    String roll = s.child("roll_no").getValue(String.class);

                    for (DataSnapshot sub : s.child("attendance").getChildren()) {
                        Integer p = sub.child("percentage").getValue(Integer.class);

                        if (p != null && p < 75) {
                            sb.append("- ")
                                    .append(name)
                                    .append(" (Roll ")
                                    .append(roll)
                                    .append(") has ")
                                    .append(p)
                                    .append("% attendance in ")
                                    .append(sub.getKey())
                                    .append("\n");
                        }
                    }
                }

                aiController.setStudentContextProvider(() -> sb.toString());
            }

            @Override
            public void onFailure(String error) {
                // Optional: log error
            }
        });
    }
}
