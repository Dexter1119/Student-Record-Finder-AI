package com.example.studentrecordfinder.ui.admin.ai;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studentrecordfinder.R;
import java.util.List;

public class AdminChatAdapter extends RecyclerView.Adapter<AdminChatAdapter.VH> {

    private final List<AdminChatMessage> messages;

    public AdminChatAdapter(List<AdminChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int pos) {
        return messages.get(pos).isUser ? 0 : 1;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        int layout = type == 0
                ? R.layout.item_chat_user
                : R.layout.item_chat_ai;
        return new VH(LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        AdminChatMessage m = messages.get(pos);
        h.txt.setText(m.isTyping ? "AI is thinking…" : m.text);
        h.txt.setAlpha(m.isTyping ? 0.6f : 1f);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txt;
        VH(View v) {
            super(v);
            txt = v.findViewById(R.id.txtMessage);
        }
    }
}
