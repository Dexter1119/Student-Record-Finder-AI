package com.example.studentrecordfinder.ui.admin.students;

import android.content.Intent;
import android.view.*;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.model.Student;

import java.util.List;
import java.util.Map;

public class AdminStudentAdapter
        extends RecyclerView.Adapter<AdminStudentAdapter.VH> {

    private final List<Student> students;
    private final Map<String, String> facultyNames;

    public AdminStudentAdapter(List<Student> students,
                               Map<String, String> facultyNames) {
        this.students = students;
        this.facultyNames = facultyNames;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_student, parent, false);
        return new VH(v);


    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {

        Student s = students.get(pos);

        h.txtName.setText(s.name);
        h.txtRoll.setText("Roll: " + s.roll_no);
        h.txtDept.setText("Dept: " + s.department);

        String facultyName =
                facultyNames.getOrDefault(s.faculty_id, "Loading...");
        h.txtFaculty.setText("Faculty: " + facultyName);
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(v.getContext(), AdminStudentDetailActivity.class);
            i.putExtra("student_uid", s.student_id);
            v.getContext().startActivity(i);
        });

    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtName, txtRoll, txtDept, txtFaculty;

        VH(View v) {
            super(v);
            txtName = v.findViewById(R.id.txtStudentName);
            txtRoll = v.findViewById(R.id.txtStudentRoll);
            txtDept = v.findViewById(R.id.txtStudentDept);
            txtFaculty = v.findViewById(R.id.txtFacultyName);
        }
    }
}
