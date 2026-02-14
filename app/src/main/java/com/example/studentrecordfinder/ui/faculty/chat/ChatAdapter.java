package com.example.studentrecordfinder.ui.faculty.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int USER = 0;
    private static final int AI = 1;

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUser ? USER : AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        int layout = viewType == USER
                ? R.layout.item_chat_user
                : R.layout.item_chat_ai;

        View v = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);

        return new VH(v);
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ChatMessage msg = messages.get(position);
        VH vh = (VH) holder;

        if (msg.isTyping) {
            vh.txt.setText("AI is typing…");
            vh.txt.setAlpha(0.6f);
        } else {
            vh.txt.setText(msg.text);
            vh.txt.setAlpha(1f);
        }

        // Optional: align based on sender
        if (msg.isUser) {
            vh.txt.setBackgroundResource(R.drawable.bg_chat_user);
        } else {
            vh.txt.setBackgroundResource(R.drawable.bg_chat_ai);
        }
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
