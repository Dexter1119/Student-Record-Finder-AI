package com.example.studentrecordfinder.ui.student.fragments.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.model.AiMessage;

import java.util.List;

public class AiChatAdapter
        extends RecyclerView.Adapter<AiChatAdapter.VH> {

    private static final int USER = 0;
    private static final int AI = 1;
    private static final int TYPING = 2;

    private final List<AiMessage> messages;

    public AiChatAdapter(List<AiMessage> messages) {
        this.messages = messages;
    }

    // --------------------------------------------------
    // VIEW TYPE
    // --------------------------------------------------
    @Override
    public int getItemViewType(int position) {

        AiMessage msg = messages.get(position);

        if (msg.Typing) return TYPING;
        return msg.isUser ? USER : AI;
    }

    // --------------------------------------------------
    // CREATE VIEW
    // --------------------------------------------------
    @NonNull
    @Override
    public VH onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        int layout;

        if (viewType == USER) {
            layout = R.layout.item_chat_user;
        } else if (viewType == TYPING) {
            layout = R.layout.item_chat_typing;
        } else {
            layout = R.layout.item_chat_ai;
        }

        View v = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new VH(v);
    }

    // --------------------------------------------------
    // BIND DATA
    // --------------------------------------------------
    @Override
    public void onBindViewHolder(@NonNull VH vh, int position) {

        AiMessage msg = messages.get(position);

        if (msg.Typing) {
            vh.txt.setText("AI is thinking…");
            vh.txt.setAlpha(0.6f);
            return;
        }

        vh.txt.setText(msg.message);
        vh.txt.setAlpha(1f);

        vh.txt.setBackgroundResource(
                msg.isUser
                        ? R.drawable.bg_chat_user
                        : R.drawable.bg_chat_ai
        );
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // --------------------------------------------------
    // VIEW HOLDER
    // --------------------------------------------------
    static class VH extends RecyclerView.ViewHolder {

        TextView txt;

        VH(View v) {
            super(v);
            txt = v.findViewById(R.id.txtMessage);
        }
    }
}
