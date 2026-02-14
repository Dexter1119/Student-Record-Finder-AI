package com.example.studentrecordfinder.ui.faculty.alerts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.core.content.ContextCompat;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.model.AIAlert;
import androidx.recyclerview.widget.RecyclerView;



import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.AlertViewHolder> {

    private final List<AIAlert> alerts;

    public AlertAdapter(List<AIAlert> alerts) {
        this.alerts = alerts;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ai_alert, parent, false);
        return new AlertViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {

        AIAlert alert = alerts.get(position);

        holder.txtTitle.setText(
                alert.title != null ? alert.title : "Alert"
        );

        holder.txtDesc.setText(
                alert.description != null ? alert.description : "No details available"
        );

        // Severity color
        if ("HIGH".equalsIgnoreCase(alert.severity)) {
            holder.txtTitle.setTextColor(
                    ContextCompat.getColor(
                            holder.itemView.getContext(),
                            R.color.red
                    )
            );
        } else {
            holder.txtTitle.setTextColor(
                    ContextCompat.getColor(
                            holder.itemView.getContext(),
                            R.color.text_main
                    )
            );
        }

    }


    @Override
    public int getItemCount() {
        return alerts.size();
    }

    // ==================================================
    // VIEW HOLDER
    // ==================================================
    static class AlertViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtDesc;

        AlertViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDesc = itemView.findViewById(R.id.txtDesc);
        }
    }
}
