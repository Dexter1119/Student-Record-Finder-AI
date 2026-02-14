package com.example.studentrecordfinder.ui.admin.ai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studentrecordfinder.R;

public class AdminAIFragment extends Fragment {

    private TextView txtInsight;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_admin_ai,
                container,
                false
        );

        txtInsight = view.findViewById(R.id.txtAdminAIInsight);

        loadAdminInsights();

        return view;
    }

    private void loadAdminInsights() {
        // TEMP placeholder (safe)
        txtInsight.setText(
                "AI is analyzing institution-wide trends...\n\n" +
                        "• Student performance patterns\n" +
                        "• Faculty workload distribution\n" +
                        "• Risk indicators"
        );
    }
}
