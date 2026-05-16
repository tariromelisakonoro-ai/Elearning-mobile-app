package com.example.learn_express;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class InstructorDashboardActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_dashboad);
        setupNavigationBar();

        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String currentUserEmail = sharedPref.getString("current_user_email", "");
        String instructorName = sharedPref.getString(currentUserEmail + "_name", "Instructor");
        
        TextView tvName = findViewById(R.id.tvInstructorName);
        if (tvName != null) tvName.setText(instructorName);

        TextView tvInitial = findViewById(R.id.tvInstructorInitial);
        if (tvInitial != null && !instructorName.isEmpty()) tvInitial.setText(String.valueOf(instructorName.charAt(0)));

        TextView tvEmail = findViewById(R.id.tvInstructorEmail);
        if (tvEmail != null) tvEmail.setText(currentUserEmail);

        setupCreatorStudio();
        fetchInstructorData();
    }

    private void setupCreatorStudio() {
        View tabAddCourse = findViewById(R.id.tabAddCourse);
        if (tabAddCourse != null) tabAddCourse.setOnClickListener(v -> navigateToUpload("course"));

        View tabAddExam = findViewById(R.id.tabAddExam);
        if (tabAddExam != null) tabAddExam.setOnClickListener(v -> navigateToUpload("exam"));

        View tabAddResource = findViewById(R.id.tabAddResource);
        if (tabAddResource != null) tabAddResource.setOnClickListener(v -> navigateToUpload("resource"));

        View tabAddAssignment = findViewById(R.id.tabAddAssignment);
        if (tabAddAssignment != null) tabAddAssignment.setOnClickListener(v -> navigateToUpload("assignment"));
    }

    private void navigateToUpload(String type) {
        Intent i = new Intent(this, UploadActivity.class);
        i.putExtra("UPLOAD_TYPE", type);
        startActivity(i);
    }

    private void fetchInstructorData() {
        fetchYourCourses();
        fetchPendingEnrollments();
        fetchPendingStudents();
    }

    private void fetchYourCourses() {
        String url = Constants.BASE_URL + "get_courses.php";
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            JSONArray courses = obj.getJSONArray("courses");
                            SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                            String email = sharedPref.getString("current_user_email", "");
                            String currentUserId = sharedPref.getString(email + "_id", "");
                            
                            LinearLayout container = findViewById(R.id.containerYourCourses);
                            container.removeAllViews();
                            
                            TextView titleView = new TextView(this);
                            titleView.setText("Your Courses");
                            titleView.setTextColor(Color.parseColor("#1a2a4a"));
                            titleView.setTextSize(16);
                            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
                            container.addView(titleView);

                            int count = 0;
                            for (int i = 0; i < courses.length(); i++) {
                                JSONObject c = courses.getJSONObject(i);
                                if (c.getString("instructor_id").equals(currentUserId)) {
                                    count++;
                                    LinearLayout row = new LinearLayout(this);
                                    row.setOrientation(LinearLayout.VERTICAL);
                                    row.setPadding(0, 16, 0, 16);
                                    
                                    TextView t = new TextView(this);
                                    t.setText(c.getString("icon") + " " + c.getString("title"));
                                    t.setTextColor(Color.parseColor("#1a2a4a"));
                                    t.setTypeface(null, android.graphics.Typeface.BOLD);
                                    
                                    TextView cat = new TextView(this);
                                    cat.setText(c.getString("category"));
                                    cat.setTextColor(Color.parseColor("#6B7280"));
                                    cat.setTextSize(12);
                                    
                                    row.addView(t);
                                    row.addView(cat);
                                    container.addView(row);
                                }
                            }
                            
                            if (count == 0) {
                                TextView t = new TextView(this);
                                t.setText("No courses yet. Add your first one above!");
                                t.setTextColor(Color.parseColor("#6B7280"));
                                t.setTextSize(12);
                                t.setTypeface(null, android.graphics.Typeface.ITALIC);
                                t.setPadding(0, 12, 0, 0);
                                container.addView(t);
                            }
                        }
                    } catch (Exception e) {}
                }, error -> {});
        Volley.newRequestQueue(this).add(req);
    }

    private void fetchPendingEnrollments() {
        String url = Constants.BASE_URL + "get_pending_enrollments.php";
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            JSONArray pending = obj.getJSONArray("pending");
                            LinearLayout container = findViewById(R.id.containerCourseRequests);
                            container.removeAllViews();
                            
                            TextView titleView = new TextView(this);
                            titleView.setText("Course Requests");
                            titleView.setTextColor(Color.parseColor("#1a2a4a"));
                            titleView.setTextSize(16);
                            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
                            container.addView(titleView);

                            TextView subtitleView = new TextView(this);
                            subtitleView.setText("Student enrollment applications.");
                            subtitleView.setTextColor(Color.parseColor("#6B7280"));
                            subtitleView.setTextSize(11);
                            subtitleView.setPadding(0, 4, 0, 12);
                            container.addView(subtitleView);

                            if (pending.length() > 0) {
                                for (int i = 0; i < pending.length(); i++) {
                                    JSONObject e = pending.getJSONObject(i);
                                    TextView t = new TextView(this);
                                    t.setText(e.getString("student_name") + " -> " + e.getString("course_title"));
                                    t.setTextColor(Color.parseColor("#1a2a4a"));
                                    t.setPadding(0, 8, 0, 8);
                                    container.addView(t);
                                }
                            } else {
                                TextView t = new TextView(this);
                                t.setText("No pending requests.");
                                t.setTextColor(Color.parseColor("#1a2a4a"));
                                t.setTextSize(11);
                                t.setGravity(Gravity.CENTER);
                                t.setBackgroundResource(R.drawable.input_bg);
                                t.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#1A142A5B")));
                                t.setPadding(8, 8, 8, 8);
                                container.addView(t);
                            }
                        }
                    } catch (Exception e) {}
                }, error -> {});
        Volley.newRequestQueue(this).add(req);
    }

    private void fetchPendingStudents() {
        String url = Constants.BASE_URL + "admin_users.php";
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            JSONArray users = obj.getJSONArray("users");
                            LinearLayout container = findViewById(R.id.containerStudentAccess);
                            container.removeAllViews();
                            
                            TextView titleView = new TextView(this);
                            titleView.setText("Student Access");
                            titleView.setTextColor(Color.parseColor("#1a2a4a"));
                            titleView.setTextSize(16);
                            titleView.setTypeface(null, android.graphics.Typeface.BOLD);
                            container.addView(titleView);

                            TextView subtitleView = new TextView(this);
                            subtitleView.setText("Approve new student accounts.");
                            subtitleView.setTextColor(Color.parseColor("#6B7280"));
                            subtitleView.setTextSize(12);
                            subtitleView.setPadding(0, 4, 0, 16);
                            container.addView(subtitleView);

                            int pendingCount = 0;
                            for (int i = 0; i < users.length(); i++) {
                                JSONObject u = users.getJSONObject(i);
                                if (u.getInt("approved") == 0) {
                                    pendingCount++;
                                    TextView t = new TextView(this);
                                    t.setText(u.getString("fullname") + " (" + u.getString("email") + ")");
                                    t.setTextColor(Color.parseColor("#1a2a4a"));
                                    t.setPadding(0, 8, 0, 8);
                                    container.addView(t);
                                }
                            }
                            
                            if (pendingCount == 0) {
                                TextView t = new TextView(this);
                                t.setText("No pending student registrations.");
                                t.setTextColor(Color.parseColor("#6B7280"));
                                t.setTextSize(13);
                                t.setTypeface(null, android.graphics.Typeface.ITALIC);
                                container.addView(t);
                            }
                        }
                    } catch (Exception e) {}
                }, error -> {});
        Volley.newRequestQueue(this).add(req);
    }
}
