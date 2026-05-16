package com.example.learn_express;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ParentDashboardActivity extends BaseActivity {

    private List<String> studentEmails = new ArrayList<>();
    private List<String> studentDisplayNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);
        setupNavigationBar();

        // Get Views
        TextView tvParentName = findViewById(R.id.tvParentName);
        TextView tvParentRole = findViewById(R.id.tvParentRole);
        TextView tvParentEmail = findViewById(R.id.tvParentEmail);
        TextView tvProfileInitial = findViewById(R.id.tvProfileInitial);
        TextView tvSignOut = findViewById(R.id.tvSignOut);

        // Load User Data from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = sharedPref.getString("current_user_email", "");
        String name = sharedPref.getString(email + "_name", "User");
        String role = sharedPref.getString(email + "_role", "Parent / Guardian");

        // Set Dynamic Data
        if (tvParentName != null) tvParentName.setText(name);
        if (tvParentRole != null) tvParentRole.setText(role);
        if (tvParentEmail != null) tvParentEmail.setText(email);
        
        if (tvProfileInitial != null && !name.isEmpty()) {
            tvProfileInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        // Click listeners
        if (tvSignOut != null) {
            tvSignOut.setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Load student list for the picker
        loadStudentList();

        View btnViewStudent = findViewById(R.id.btnViewStudent);
        if (btnViewStudent != null) {
            btnViewStudent.setOnClickListener(v -> {
                AutoCompleteTextView spinnerStudent = findViewById(R.id.spinnerStudent);
                if (spinnerStudent != null) {
                    String selectedText = spinnerStudent.getText().toString().trim();
                    // Find the matching email from selection
                    String studentEmail = "";
                    for (int i = 0; i < studentDisplayNames.size(); i++) {
                        if (studentDisplayNames.get(i).equals(selectedText)) {
                            studentEmail = studentEmails.get(i);
                            break;
                        }
                    }
                    if (studentEmail.isEmpty()) {
                        // Try direct email entry
                        studentEmail = selectedText;
                    }
                    if (!studentEmail.isEmpty() && !studentEmail.startsWith("--")) {
                        Intent intent = new Intent(ParentDashboardActivity.this, StudentDashboardActivity.class);
                        intent.putExtra("student_email", studentEmail);
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Please select a student", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void loadStudentList() {
        String url = Constants.BASE_URL + "parent_overview.php";
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success") && obj.has("students")) {
                            JSONArray students = obj.getJSONArray("students");
                            studentEmails.clear();
                            studentDisplayNames.clear();
                            for (int i = 0; i < students.length(); i++) {
                                JSONObject s = students.getJSONObject(i);
                                studentEmails.add(s.getString("email"));
                                studentDisplayNames.add(s.getString("fullname") + " (" + s.getString("email") + ")");
                            }
                            AutoCompleteTextView spinnerStudent = findViewById(R.id.spinnerStudent);
                            if (spinnerStudent != null) {
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                                        android.R.layout.simple_dropdown_item_1line, studentDisplayNames);
                                spinnerStudent.setAdapter(adapter);
                                spinnerStudent.setThreshold(1);
                            }
                        }
                    } catch (Exception e) {
                        Log.e("ParentDashboard", "Error loading students", e);
                    }
                }, error -> {});
        Volley.newRequestQueue(this).add(req);
    }


    private void fetchStudentData(String studentEmail) {
        String url = Constants.BASE_URL + "parent_overview.php?student_email=" + studentEmail;
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            View container = findViewById(R.id.containerStudentData);
                            if (container != null) container.setVisibility(View.VISIBLE);
                            
                            JSONObject student = obj.getJSONObject("student");
                            JSONArray enrollments = obj.getJSONArray("enrollments");
                            JSONArray submissions = obj.getJSONArray("submissions");
                            JSONArray assignments = obj.getJSONArray("assignments");
                            
                            int activeCourses = 0;
                            for (int i = 0; i < enrollments.length(); i++) {
                                if ("approved".equals(enrollments.getJSONObject(i).getString("status"))) {
                                    activeCourses++;
                                }
                            }
                            
                            int gradedWork = 0;
                            int pendingGrades = 0;
                            for (int i = 0; i < submissions.length(); i++) {
                                if ("graded".equals(submissions.getJSONObject(i).getString("status"))) {
                                    gradedWork++;
                                } else {
                                    pendingGrades++;
                                }
                            }
                            
                            TextView tvActive = findViewById(R.id.tvKpiActiveCourses);
                            if (tvActive != null) tvActive.setText(String.valueOf(activeCourses));
                            
                            TextView tvGraded = findViewById(R.id.tvKpiGradedWork);
                            if (tvGraded != null) tvGraded.setText(String.valueOf(gradedWork));
                            
                            TextView tvPending = findViewById(R.id.tvKpiPendingGrades);
                            if (tvPending != null) tvPending.setText(String.valueOf(pendingGrades));
                            
                            TextView tvUpcoming = findViewById(R.id.tvKpiUpcomingTasks);
                            if (tvUpcoming != null) tvUpcoming.setText(String.valueOf(assignments.length()));
                            
                            // Academic Progress
                            LinearLayout containerAcademicProgress = findViewById(R.id.containerAcademicProgress);
                            if (containerAcademicProgress != null) {
                                containerAcademicProgress.removeAllViews();
                                if (enrollments.length() > 0) {
                                    for (int i = 0; i < enrollments.length(); i++) {
                                        JSONObject e = enrollments.getJSONObject(i);
                                        if ("approved".equals(e.getString("status"))) {
                                            LinearLayout row = new LinearLayout(this);
                                            row.setOrientation(LinearLayout.VERTICAL);
                                            row.setPadding(0, 0, 0, 16);
                                            
                                            LinearLayout titleRow = new LinearLayout(this);
                                            titleRow.setOrientation(LinearLayout.HORIZONTAL);
                                            TextView title = new TextView(this);
                                            title.setText(String.format("%s %s", e.getString("icon"), e.getString("title")));
                                            title.setTextColor(Color.parseColor("#1a2a4a"));
                                            title.setTypeface(null, android.graphics.Typeface.BOLD);
                                            title.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
                                            
                                            TextView progressTxt = new TextView(this);
                                            progressTxt.setText("75%"); // Mock
                                            progressTxt.setTextColor(Color.parseColor("#F97316"));
                                            progressTxt.setTextSize(12);
                                            titleRow.addView(title);
                                            titleRow.addView(progressTxt);
                                            row.addView(titleRow);
                                            
                                            View barBg = new View(this);
                                            barBg.setLayoutParams(new LinearLayout.LayoutParams(-1, 8));
                                            barBg.setBackgroundColor(Color.parseColor("#F1F5F9"));
                                            row.addView(barBg);
                                            
                                            containerAcademicProgress.addView(row);
                                        }
                                    }
                                } else {
                                    TextView t = new TextView(this);
                                    t.setText("No active courses.");
                                    containerAcademicProgress.addView(t);
                                }
                            }
                            
                            // Upcoming Tasks
                            LinearLayout containerUpcomingTasks = findViewById(R.id.containerUpcomingTasks);
                            if (containerUpcomingTasks != null) {
                                containerUpcomingTasks.removeAllViews();
                                if (assignments.length() > 0) {
                                    for (int i = 0; i < assignments.length(); i++) {
                                        JSONObject a = assignments.getJSONObject(i);
                                        TextView t = new TextView(this);
                                        t.setText(String.format("%s\nDue: %s", a.getString("title"), a.getString("due_date")));
                                        t.setPadding(0, 0, 0, 16);
                                        t.setTextColor(Color.parseColor("#1a2a4a"));
                                        containerUpcomingTasks.addView(t);
                                    }
                                } else {
                                    TextView t = new TextView(this);
                                    t.setText("No upcoming tasks.");
                                    containerUpcomingTasks.addView(t);
                                }
                            }
                            
                            // Grades
                            LinearLayout containerGrades = findViewById(R.id.containerGrades);
                            if (containerGrades != null) {
                                containerGrades.removeAllViews();
                                if (submissions.length() > 0) {
                                    for (int i = 0; i < submissions.length(); i++) {
                                        JSONObject s = submissions.getJSONObject(i);
                                        LinearLayout row = new LinearLayout(this);
                                        row.setOrientation(LinearLayout.HORIZONTAL);
                                        row.setPadding(0, 16, 0, 16);
                                        
                                        LinearLayout col1 = new LinearLayout(this);
                                        col1.setOrientation(LinearLayout.VERTICAL);
                                        col1.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 2f));
                                        TextView tTitle = new TextView(this);
                                        tTitle.setText(s.getString("assignment_title"));
                                        tTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                                        tTitle.setTextColor(Color.parseColor("#1a2a4a"));
                                        TextView tCourse = new TextView(this);
                                        tCourse.setText(s.getString("course_title"));
                                        tCourse.setTextSize(12);
                                        tCourse.setTextColor(Color.parseColor("#6B7280"));
                                        col1.addView(tTitle);
                                        col1.addView(tCourse);
                                        
                                        TextView tStatus = new TextView(this);
                                        tStatus.setText(s.getString("status"));
                                        tStatus.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
                                        
                                        TextView tGrade = new TextView(this);
                                        String gradeStr = s.isNull("grade") ? "-" : s.getString("grade") + "%";
                                        tGrade.setText(gradeStr);
                                        tGrade.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
                                        tGrade.setGravity(Gravity.END);
                                        tGrade.setTypeface(null, android.graphics.Typeface.BOLD);
                                        tGrade.setTextColor(Color.parseColor("#1a2a4a"));
                                        
                                        row.addView(col1);
                                        row.addView(tStatus);
                                        row.addView(tGrade);
                                        
                                        containerGrades.addView(row);
                                        
                                        View div = new View(this);
                                        div.setLayoutParams(new LinearLayout.LayoutParams(-1, 2));
                                        div.setBackgroundColor(Color.parseColor("#F1F5F9"));
                                        containerGrades.addView(div);
                                    }
                                } else {
                                    TextView t = new TextView(this);
                                    t.setText("No submissions.");
                                    containerGrades.addView(t);
                                }
                            }
                            
                            // Summary
                            TextView tvReportSummary = findViewById(R.id.tvReportSummary);
                            if (tvReportSummary != null) {
                                String summary = String.format("%s is enrolled in %d active courses and has %d assignments pending grading. They have %d upcoming tasks.",
                                        student.getString("fullname"), activeCourses, pendingGrades, assignments.length());
                                tvReportSummary.setText(summary);
                            }
                            
                            Toast.makeText(this, "Student data loaded.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Student not found.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("ParentDashboard", "Error parsing student data", e);
                    }
                }, error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show());
        Volley.newRequestQueue(this).add(req);
    }
}
