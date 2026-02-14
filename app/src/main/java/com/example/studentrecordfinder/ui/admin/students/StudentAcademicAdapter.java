package com.example.studentrecordfinder.ui.admin.students;

import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;

import java.util.List;

public class StudentAcademicAdapter
        extends RecyclerView.Adapter<StudentAcademicAdapter.VH> {

    private final List<StudentAcademicRow> list;

    public StudentAcademicAdapter(List<StudentAcademicRow> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_academic, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {

        StudentAcademicRow r = list.get(pos);

        h.txtSubject.setText(r.subject);
        h.txtAttendance.setText("Attendance: " + r.attendancePercent + "%");
        h.txtInternal.setText("Internal: " + r.internal);
        h.txtExternal.setText("External: " + r.external);
        h.txtTotal.setText("Total: " + r.total);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtSubject, txtAttendance, txtInternal, txtExternal, txtTotal;

        VH(View v) {
            super(v);
            txtSubject = v.findViewById(R.id.txtSubject);
            txtAttendance = v.findViewById(R.id.txtAttendance);
            txtInternal = v.findViewById(R.id.txtInternal);
            txtExternal = v.findViewById(R.id.txtExternal);
            txtTotal = v.findViewById(R.id.txtTotal);
        }
    }
}
