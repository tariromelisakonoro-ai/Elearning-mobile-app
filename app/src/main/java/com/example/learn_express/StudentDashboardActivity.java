package com.example.learn_express;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;

public class StudentDashboardActivity extends BaseActivity {
    private String childEmail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);
        setupOfflineBanner();
        setupNavigationBar();

        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String currentUserEmail = sharedPref.getString("current_user_email", "");
        String studentName = sharedPref.getString(currentUserEmail + "_name", "User");
        String studentRole = sharedPref.getString(currentUserEmail + "_role", "Student");

        childEmail = getIntent().getStringExtra("student_email");
        if (childEmail != null && !childEmail.isEmpty()) {
            studentName = "Child (" + childEmail + ")";
            currentUserEmail = childEmail;
            
            View banner = findViewById(R.id.bannerParentViewing);
            if (banner != null) {
                banner.setVisibility(View.VISIBLE);
                TextView tvParent = findViewById(R.id.tvParentNameBanner);
                if (tvParent != null) {
                    tvParent.setText("👤 " + sharedPref.getString(sharedPref.getString("current_user_email", "") + "_name", "Parent"));
                }
                View btnBack = findViewById(R.id.btnBackToParent);
                if (btnBack != null) {
                    btnBack.setOnClickListener(v -> finish());
                }
            }
        }

        TextView tvName = findViewById(R.id.tvStudentName);
        if (tvName != null) tvName.setText(studentName);

        TextView tvEmail = findViewById(R.id.tvStudentEmail);
        if (tvEmail != null) tvEmail.setText(currentUserEmail);

        TextView tvRole = findViewById(R.id.tvStudentRole);
        if (tvRole != null) tvRole.setText(studentRole);

        ImageView ivProfile = findViewById(R.id.ivStudentProfile);
        if (ivProfile != null) {
            Glide.with(this).load(R.drawable.student_dashboard).circleCrop().into(ivProfile);
        }

        setupClickListeners();
        loadDashboardData();
    }

    private void setupClickListeners() {
        View cardProfile = findViewById(R.id.cardProfile);
        if (cardProfile != null) cardProfile.setOnClickListener(v -> Toast.makeText(this, "Opening Profile...", Toast.LENGTH_SHORT).show());

        View btnMyCourses = findViewById(R.id.btnMyCourses);
        if (btnMyCourses != null) btnMyCourses.setOnClickListener(v -> startActivity(new Intent(this, CoursesActivity.class)));

        View btnEnrollmentStatus = findViewById(R.id.btnEnrollmentStatus);
        if (btnEnrollmentStatus != null) btnEnrollmentStatus.setOnClickListener(v -> startActivity(new Intent(this, CoursesActivity.class)));

        View btnAvailableCourses = findViewById(R.id.btnAvailableCourses);
        if (btnAvailableCourses != null) btnAvailableCourses.setOnClickListener(v -> startActivity(new Intent(this, CoursesActivity.class)));

        View btnQuickAssignments = findViewById(R.id.btnQuickAssignments);
        if (btnQuickAssignments != null) btnQuickAssignments.setOnClickListener(v -> startActivity(new Intent(this, AssignmentsActivity.class)));

        View btnQuickExams = findViewById(R.id.btnQuickExams);
        if (btnQuickExams != null) btnQuickExams.setOnClickListener(v -> startActivity(new Intent(this, ExamsActivity.class)));

        View btnQuickResources = findViewById(R.id.btnQuickResources);
        if (btnQuickResources != null) btnQuickResources.setOnClickListener(v -> startActivity(new Intent(this, ResourcesActivity.class)));

        View cardPerformance = findViewById(R.id.cardPerformance);
        if (cardPerformance != null) cardPerformance.setOnClickListener(v -> startActivity(new Intent(this, ResultsActivity.class)));

        View btnViewResults = findViewById(R.id.btnViewResults);
        if (btnViewResults != null) btnViewResults.setOnClickListener(v -> startActivity(new Intent(this, ResultsActivity.class)));

        View btnViewInvoices = findViewById(R.id.btnViewInvoices);
        if (btnViewInvoices != null) btnViewInvoices.setOnClickListener(v -> startActivity(new Intent(this, PaymentsActivity.class)));
    }

    // -----------------------------------------------------------------------
    // Data loading (online / offline)
    // -----------------------------------------------------------------------

    private void loadDashboardData() {
        if (NetworkUtils.isOnline(this)) {
            fetchEnrollments();
        } else {
            loadEnrollmentsFromCache();
        }
    }

    // -- Enrollments --

    private void fetchEnrollments() {
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String currentUserEmail = sharedPref.getString("current_user_email", "");
        String currentUserId = sharedPref.getString(currentUserEmail + "_id", "");
        String currentUserRole = sharedPref.getString(currentUserEmail + "_role", "Student");
        
        String url = Constants.BASE_URL + "get_student_enrollments.php?user_id=" + currentUserId + "&role=" + currentUserRole.toLowerCase();
        if (childEmail != null && !childEmail.isEmpty()) {
            url += "&student_email=" + android.net.Uri.encode(childEmail);
        }
        com.android.volley.toolbox.StringRequest request =
                new com.android.volley.toolbox.StringRequest(
                        com.android.volley.Request.Method.GET, url,
                        response -> {
                            new OfflineCacheManager(this).save(OfflineCacheManager.KEY_ENROLLMENTS, response);
                            parseEnrollments(response);
                            // Also fetch other sections
                            fetchAvailableCourses();
                            fetchTasks();
                        },
                        error -> loadEnrollmentsFromCache());
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void loadEnrollmentsFromCache() {
        OfflineCacheManager cache = new OfflineCacheManager(this);
        if (cache.has(OfflineCacheManager.KEY_ENROLLMENTS)) {
            parseEnrollments(cache.load(OfflineCacheManager.KEY_ENROLLMENTS));
        }
        // Load other cached sections too
        if (cache.has(OfflineCacheManager.KEY_COURSES)) {
            parseAvailableCourses(cache.load(OfflineCacheManager.KEY_COURSES));
        }
        if (cache.has(OfflineCacheManager.KEY_ASSIGNMENTS)) {
            parseTasksFromCache(cache.load(OfflineCacheManager.KEY_ASSIGNMENTS));
        }
    }

    private void parseEnrollments(String json) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            if (obj.getBoolean("success")) {
                org.json.JSONArray enrollments = obj.getJSONArray("enrollments");
                int activeCount = 0;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < enrollments.length(); i++) {
                    org.json.JSONObject e = enrollments.getJSONObject(i);
                    String status = e.getString("status");
                    String title  = e.getString("title");
                    sb.append("• ").append(title).append(" (").append(status.toUpperCase()).append(")\n");
                    if ("approved".equals(status)) activeCount++;
                }
                int finalCount = activeCount;
                TextView tvMy = findViewById(R.id.tvMyCoursesDesc);
                if (tvMy != null && finalCount > 0) tvMy.setText("You have " + finalCount + " active courses tracking progress.");
                TextView tvEnroll = findViewById(R.id.tvEnrollmentDesc);
                if (tvEnroll != null && enrollments.length() > 0) tvEnroll.setText(sb.toString().trim());
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // -- Available Courses --

    private void fetchAvailableCourses() {
        String url = Constants.BASE_URL + "get_courses.php";
        com.android.volley.toolbox.StringRequest request =
                new com.android.volley.toolbox.StringRequest(
                        com.android.volley.Request.Method.GET, url,
                        response -> {
                            new OfflineCacheManager(this).save(OfflineCacheManager.KEY_COURSES, response);
                            parseAvailableCourses(response);
                        },
                        error -> {
                            OfflineCacheManager c = new OfflineCacheManager(this);
                            if (c.has(OfflineCacheManager.KEY_COURSES))
                                parseAvailableCourses(c.load(OfflineCacheManager.KEY_COURSES));
                        });
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void parseAvailableCourses(String json) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            if (obj.getBoolean("success")) {
                org.json.JSONArray courses = obj.getJSONArray("courses");
                
                android.widget.LinearLayout container = findViewById(R.id.containerAvailableList);
                if (container != null && courses.length() > 0) {
                    container.removeAllViews();
                    
                    for (int i = 0; i < courses.length(); i++) {
                        org.json.JSONObject course = courses.getJSONObject(i);
                        final int courseId = course.getInt("id");
                        
                        android.widget.LinearLayout card = new android.widget.LinearLayout(this);
                        card.setOrientation(android.widget.LinearLayout.VERTICAL);
                        card.setBackgroundResource(R.drawable.input_bg);
                        card.setPadding(32, 32, 32, 32);
                        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, 24);
                        card.setLayoutParams(lp);

                        TextView tvTitle = new TextView(this);
                        tvTitle.setText(course.optString("icon", "📚") + " " + course.getString("title"));
                        tvTitle.setTextColor(getResources().getColor(R.color.heading));
                        tvTitle.setTextSize(16);
                        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                        card.addView(tvTitle);

                        TextView tvCategory = new TextView(this);
                        tvCategory.setText(course.optString("category", "Uncategorized"));
                        tvCategory.setTextColor(getResources().getColor(R.color.primary));
                        tvCategory.setTextSize(12);
                        tvCategory.setPadding(0, 8, 0, 16);
                        card.addView(tvCategory);

                        TextView btnRegister = new TextView(this);
                        btnRegister.setText("Register Now");
                        btnRegister.setTextColor(0xFFFFFFFF);
                        btnRegister.setBackgroundResource(R.drawable.btn_orange);
                        btnRegister.setPadding(32, 16, 32, 16);
                        btnRegister.setTextSize(14);
                        btnRegister.setGravity(android.view.Gravity.CENTER);
                        btnRegister.setOnClickListener(v -> enrollCourse(courseId));
                        card.addView(btnRegister);

                        container.addView(card);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void enrollCourse(int courseId) {
        String url = Constants.BASE_URL + "enroll.php";
        com.android.volley.toolbox.StringRequest req = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.POST, url,
                response -> {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response);
                        Toast.makeText(this, obj.optString("message", "Enrollment requested!"), Toast.LENGTH_LONG).show();
                        if (obj.optBoolean("success", false)) {
                            loadDashboardData(); // Refresh data
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }, 
                error -> Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> params = new java.util.HashMap<>();
                SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String email = sharedPref.getString("current_user_email", "");
                String studentId = sharedPref.getString(email + "_id", "");
                
                params.put("course_id", String.valueOf(courseId));
                if (childEmail != null && !childEmail.isEmpty()) {
                    // Parent viewing child - don't allow enrolling for child right now, or pass child ID
                    // Assuming we pass current student id which would be child's ID if we had it
                    // Actually, if we are parent, we shouldn't allow enroll, but for now pass current user
                }
                params.put("user_id", studentId);
                return params;
            }
        };
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(req);
    }

    // -- Tasks / Assignments --

    private void fetchTasks() {
        String url = Constants.BASE_URL + "get_assignments.php";
        com.android.volley.toolbox.StringRequest request =
                new com.android.volley.toolbox.StringRequest(
                        com.android.volley.Request.Method.GET, url,
                        response -> {
                            new OfflineCacheManager(this).save(OfflineCacheManager.KEY_ASSIGNMENTS, response);
                            parseTasksFromCache(response);
                        },
                        error -> {
                            OfflineCacheManager c = new OfflineCacheManager(this);
                            if (c.has(OfflineCacheManager.KEY_ASSIGNMENTS))
                                parseTasksFromCache(c.load(OfflineCacheManager.KEY_ASSIGNMENTS));
                        });
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void parseTasksFromCache(String json) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            if (obj.getBoolean("success")) {
                org.json.JSONArray assignments = obj.getJSONArray("assignments");
                android.widget.LinearLayout container = findViewById(R.id.containerTasksHorizontal);
                if (container != null && assignments.length() > 0) {
                    container.removeAllViews();
                    for (int i = 0; i < Math.min(assignments.length(), 4); i++) {
                        org.json.JSONObject a = assignments.getJSONObject(i);

                        android.widget.LinearLayout taskBox = new android.widget.LinearLayout(this);
                        taskBox.setOrientation(android.widget.LinearLayout.VERTICAL);
                        taskBox.setBackgroundResource(R.drawable.input_bg);
                        taskBox.setPadding(32, 32, 32, 32);
                        android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(450, -2);
                        lp.setMargins(0, 0, 24, 0);
                        taskBox.setLayoutParams(lp);

                        TextView tDue = new TextView(this);
                        tDue.setText("DUE " + a.optString("due_date", "").toUpperCase());
                        tDue.setTextColor(getResources().getColor(R.color.errorRed));
                        tDue.setTextSize(10);
                        tDue.setTypeface(null, android.graphics.Typeface.BOLD);
                        taskBox.addView(tDue);

                        TextView tTitle = new TextView(this);
                        tTitle.setText(a.getString("title"));
                        tTitle.setTextColor(getResources().getColor(R.color.heading));
                        tTitle.setTextSize(14);
                        tTitle.setTypeface(null, android.graphics.Typeface.BOLD);
                        android.widget.LinearLayout.LayoutParams lpT = new android.widget.LinearLayout.LayoutParams(-2, -2);
                        lpT.setMargins(0, 8, 0, 16);
                        tTitle.setLayoutParams(lpT);
                        taskBox.addView(tTitle);

                        TextView btn = new TextView(this);
                        btn.setText("View Task");
                        btn.setTextColor(0xFFFFFFFF);
                        btn.setBackgroundResource(R.drawable.btn_orange);
                        btn.setPadding(32, 16, 32, 16);
                        btn.setTextSize(12);
                        btn.setOnClickListener(v -> startActivity(new Intent(this, AssignmentsActivity.class)));
                        taskBox.addView(btn);

                        container.addView(taskBox);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Auto-refresh when connection restores
    @Override
    protected void onConnectivityRestored() {
        Toast.makeText(this, "Back online – refreshing dashboard…", Toast.LENGTH_SHORT).show();
        loadDashboardData();
    }
}
