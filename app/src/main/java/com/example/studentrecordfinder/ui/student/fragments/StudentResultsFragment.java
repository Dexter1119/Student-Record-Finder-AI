package com.example.studentrecordfinder.ui.student.fragments;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studentrecordfinder.R;
import com.example.studentrecordfinder.controller.StudentController;
import com.example.studentrecordfinder.model.AttendanceRecord;
import com.example.studentrecordfinder.model.ResultRecord;
import com.example.studentrecordfinder.model.Student;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StudentResultsFragment extends Fragment {

    private TextView tvOverallGpa, tvCircularPercent;
    private LinearLayout layoutAttendanceContainer, layoutMarksContainer;
    private CircularProgressIndicator circularAttendance;
    private LineChart lineChart;
    private MaterialButton btnExportPdf;

    private StudentController controller;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        View view = inflater.inflate(
                R.layout.fragment_student_report,
                container,
                false
        );

        bindViews(view);
        controller = new StudentController();
        loadReport();

        btnExportPdf.setOnClickListener(v -> generatePdf());

        return view;
    }

    private void bindViews(View v) {
        tvOverallGpa = v.findViewById(R.id.tvOverallGpa);
        tvCircularPercent = v.findViewById(R.id.tvCircularPercent);
        layoutAttendanceContainer = v.findViewById(R.id.layoutAttendanceContainer);
        layoutMarksContainer = v.findViewById(R.id.layoutMarksContainer);
        circularAttendance = v.findViewById(R.id.circularAttendance);
        lineChart = v.findViewById(R.id.lineChartPerformance);
        btnExportPdf = v.findViewById(R.id.btnExportPdf);
    }

    private void loadReport() {

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        controller.fetchMyProfile(uid, new StudentController.StudentCallback() {
            @Override
            public void onSuccess(Student student) {

                if (!isAdded()) return;
                if (student == null) return;

                layoutAttendanceContainer.removeAllViews();
                layoutMarksContainer.removeAllViews();

                int attendanceSum = 0;
                int subjectCount = 0;
                int marksSum = 0;

                // ================= ATTENDANCE =================
                if (student.attendance != null) {

                    for (Map.Entry<String, AttendanceRecord> entry :
                            student.attendance.entrySet()) {

                        String subject = entry.getKey();
                        AttendanceRecord record = entry.getValue();
                        if (record == null) continue;

                        View item = LayoutInflater.from(getContext()).inflate(
                                R.layout.item_subject_attendance,
                                layoutAttendanceContainer,
                                false
                        );

                        TextView tvSubject = item.findViewById(R.id.tvSubjectName);
                        TextView tvPercent = item.findViewById(R.id.tvSubjectPercent);
                        ProgressBar progress = item.findViewById(R.id.progressSubject);

                        tvSubject.setText(subject);
                        tvPercent.setText(record.percentage + "%");
                        progress.setProgress(record.percentage);

                        layoutAttendanceContainer.addView(item);

                        attendanceSum += record.percentage;
                        subjectCount++;
                    }
                }

                // ================= MARKS =================
                List<Entry> entries = new ArrayList<>();
                int index = 0;

                if (student.marks != null) {

                    for (Map.Entry<String, ResultRecord> entry :
                            student.marks.entrySet()) {

                        String subject = entry.getKey();
                        ResultRecord record = entry.getValue();
                        if (record == null) continue;

                        View item = LayoutInflater.from(getContext()).inflate(
                                R.layout.item_subject_marks,
                                layoutMarksContainer,
                                false
                        );

                        TextView tvSubject = item.findViewById(R.id.tvMarksSubjectName);
                        TextView tvMarks = item.findViewById(R.id.tvSubjectMarks);

                        int percent = record.total; // Already out of 100
                        tvSubject.setText(subject);
                        tvMarks.setText(percent + "%");

                        layoutMarksContainer.addView(item);

                        marksSum += percent;

                        entries.add(new Entry(index, percent));
                        index++;
                    }
                }

                // ================= OVERALL =================
                int overallAttendance = subjectCount == 0
                        ? 0
                        : attendanceSum / subjectCount;

                double avgMarks = subjectCount == 0
                        ? 0
                        : (double) marksSum / subjectCount;

                double gpa = (avgMarks / 100.0) * 4.0;

                circularAttendance.setProgress(overallAttendance);
                tvCircularPercent.setText(overallAttendance + "%");
                tvOverallGpa.setText("GPA: " + String.format("%.2f", gpa));

                setupLineChart(entries);
            }

            @Override
            public void onFailure(String error) {}
        });
    }

    private void setupLineChart(List<Entry> entries) {

        if (entries.isEmpty()) return;

        LineDataSet dataSet = new LineDataSet(entries, "Performance Trend");
        dataSet.setColor(getResources().getColor(R.color.primary));
        dataSet.setCircleRadius(5f);
        dataSet.setValueTextSize(10f);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        Description description = new Description();
        description.setText("");
        lineChart.setDescription(description);

        lineChart.animateY(1000);
        lineChart.invalidate();
    }

    private void generatePdf() {

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(1080, 1920, 1).create();

        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        paint.setTextSize(40);
        canvas.drawText("Student Academic Report", 250, 100, paint);

        paint.setTextSize(30);
        canvas.drawText(tvOverallGpa.getText().toString(),
                100, 200, paint);

        document.finishPage(page);

        try {
            File file = new File(
                    requireContext().getExternalFilesDir(null),
                    "StudentReport.pdf"
            );
            document.writeTo(new FileOutputStream(file));
            Toast.makeText(getContext(),
                    "PDF Saved Successfully",
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

        document.close();
    }
}
