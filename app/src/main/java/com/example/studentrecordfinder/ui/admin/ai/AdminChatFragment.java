package com.example.studentrecordfinder.ui.admin.ai;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.controller.AIChatController;
import com.example.studentrecordfinder.controller.AdminController;

import java.util.ArrayList;
import java.util.List;

public class AdminChatFragment extends Fragment {

    private final List<AdminChatMessage> messages = new ArrayList<>();
    private AdminChatAdapter adapter;

    private AIChatController aiController;
    private AdminController adminChatController;

    private boolean contextReady = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_admin_chat, container, false);

        RecyclerView recycler = v.findViewById(R.id.recyclerChat);
        EditText edt = v.findViewById(R.id.edtMessage);
        ImageButton send = v.findViewById(R.id.btnSend);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AdminChatAdapter(messages);
        recycler.setAdapter(adapter);

        aiController = new AIChatController();
        adminChatController = new AdminController();

        send.setEnabled(false);
        buildAdminContext(send);

        send.setOnClickListener(btn -> {

            if (!contextReady) {
                Toast.makeText(getContext(),
                        "Preparing AI context…",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String q = edt.getText().toString().trim();
            if (q.isEmpty()) return;

            messages.add(new AdminChatMessage(q, true));
            adapter.notifyItemInserted(messages.size() - 1);
            recycler.scrollToPosition(messages.size() - 1);
            edt.setText("");

            send.setEnabled(false);

            AdminChatMessage typing = AdminChatMessage.typing();
            messages.add(typing);
            int typingPos = messages.size() - 1;
            adapter.notifyItemInserted(typingPos);

            aiController.processQuery(q,
                    new AIChatController.AICallback() {
                        @Override
                        public void onResponse(String reply) {

                            if (getActivity() == null) return;

                            getActivity().runOnUiThread(() -> {
                                messages.remove(typingPos);
                                messages.add(new AdminChatMessage(reply, false));
                                adapter.notifyDataSetChanged();
                                recycler.scrollToPosition(messages.size() - 1);
                                send.setEnabled(true);
                            });
                        };


                        public void onFailure(String error) {
                            if (getActivity() == null) return;
                            getActivity().runOnUiThread(() -> {
                                messages.remove(typingPos);
                                messages.add(new AdminChatMessage(
                                        "AI Error: " + error, false));
                                adapter.notifyDataSetChanged();
                                send.setEnabled(true);
                            });
                        }
                    });
        });

        return v;
    }

    // ==================================================
    // ADMIN CONTEXT (GLOBAL)
    // ==================================================
    private void buildAdminContext(ImageButton sendBtn) {

        adminChatController.buildGlobalContext(ctx -> {

            // 🔓 Enable ADMIN mode with GLOBAL context
            aiController.enableAdminMode(() -> ctx);

            contextReady = true;
            sendBtn.setEnabled(true);
        });
    }

}
