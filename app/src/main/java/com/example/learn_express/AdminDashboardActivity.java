package com.example.learn_express;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class AdminDashboardActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        setupNavigationBar();

        // Load User Data from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email = sharedPref.getString("current_user_email", "");
        String name = sharedPref.getString(email + "_name", "Admin");

        // Find Views
        TextView tvAdminName = findViewById(R.id.tvAdminName);
        TextView tvAdminEmail = findViewById(R.id.tvAdminEmail);
        TextView tvAdminInitial = findViewById(R.id.tvAdminInitial);
        TextView tvSignOut = findViewById(R.id.tvSignOut);

        if (tvAdminName != null) tvAdminName.setText(name);
        if (tvAdminEmail != null) tvAdminEmail.setText(email);
        if (tvAdminInitial != null && !name.isEmpty()) {
            tvAdminInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }

        // Setup Sign Out
        if (tvSignOut != null) {
            tvSignOut.setOnClickListener(v -> {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        Spinner spinnerRole = findViewById(R.id.spinnerTargetRole);
        if (spinnerRole != null) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"all", "student", "instructor"});
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRole.setAdapter(adapter);
        }

        findViewById(R.id.btnBroadcast).setOnClickListener(v -> {
            EditText etTitle = findViewById(R.id.etAnnounceTitle);
            EditText etMessage = findViewById(R.id.etAnnounceMessage);
            String title = etTitle.getText().toString().trim();
            String message = etMessage.getText().toString().trim();
            String role = spinnerRole.getSelectedItem().toString();

            if (title.isEmpty() || message.isEmpty()) {
                Toast.makeText(this, "Title and message required.", Toast.LENGTH_SHORT).show();
                return;
            }

            StringRequest req = new StringRequest(Request.Method.POST, Constants.BASE_URL + "admin_announcements.php",
                    response -> {
                        try {
                            JSONObject res = new JSONObject(response);
                            if (res.getBoolean("success")) {
                                Toast.makeText(this, "Broadcast sent!", Toast.LENGTH_SHORT).show();
                                etTitle.setText("");
                                etMessage.setText("");
                            } else {
                                Toast.makeText(this, res.optString("message"), Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {}
                    }, error -> Toast.makeText(this, "Error sending broadcast", Toast.LENGTH_SHORT).show()) {
                @Override
                protected java.util.Map<String, String> getParams() {
                    java.util.Map<String, String> params = new java.util.HashMap<>();
                    params.put("title", title);
                    params.put("message", message);
                    params.put("target_role", role);
                    return params;
                }
            };
            Volley.newRequestQueue(this).add(req);
        });

        fetchStats();
        fetchUsers();
        fetchPendingEnrollments();
    }

    private void fetchStats() {
        String url = Constants.BASE_URL + "admin_stats.php";
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            JSONObject stats = obj.getJSONObject("stats");
                            ((TextView) findViewById(R.id.tvStatsStudents)).setText(stats.getString("total_students"));
                            ((TextView) findViewById(R.id.tvStatsInstructors)).setText(stats.getString("total_instructors"));
                            ((TextView) findViewById(R.id.tvStatsCourses)).setText(stats.getString("total_courses"));
                            ((TextView) findViewById(R.id.tvStatsEnrollments)).setText(stats.getString("total_enrollments"));
                        }
                    } catch (Exception e) {}
                }, error -> {});
        Volley.newRequestQueue(this).add(req);
    }

    private void fetchUsers() {
        String url = Constants.BASE_URL + "admin_users.php";
        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            JSONArray users = obj.getJSONArray("users");
                            LinearLayout container = findViewById(R.id.containerUserManagement);
                            container.removeAllViews();
                            
                            for (int i = 0; i < users.length(); i++) {
                                JSONObject u = users.getJSONObject(i);
                                
                                LinearLayout row = new LinearLayout(this);
                                row.setOrientation(LinearLayout.HORIZONTAL);
                                row.setPadding(8, 16, 8, 16);
                                row.setGravity(Gravity.CENTER_VERTICAL);
                                
                                LinearLayout col1 = new LinearLayout(this);
                                col1.setOrientation(LinearLayout.VERTICAL);
                                col1.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1.5f));
                                TextView name = new TextView(this);
                                name.setText(u.getString("fullname"));
                                name.setTextSize(14);
                                name.setTypeface(null, android.graphics.Typeface.BOLD);
                                name.setTextColor(Color.parseColor("#1a2a4a"));
                                TextView email = new TextView(this);
                                email.setText(u.getString("email"));
                                email.setTextSize(11);
                                email.setTextColor(Color.parseColor("#6B7280"));
                                col1.addView(name);
                                col1.addView(email);
                                
                                TextView role = new TextView(this);
                                role.setText(u.getString("role"));
                                role.setTextSize(13);
                                role.setTextColor(Color.parseColor("#6B7280"));
                                role.setLayoutParams(new LinearLayout.LayoutParams(0, -2, 1f));
                                
                                TextView status = new TextView(this);
                                status.setTextSize(11);
                                status.setTypeface(null, android.graphics.Typeface.BOLD);
                                status.setPadding(20, 8, 20, 8);
                                
                                int isApproved = u.getInt("approved");
                                String uStatus = u.getString("status");
                                if (isApproved == 0) {
                                    status.setText("Pending");
                                    status.setTextColor(Color.parseColor("#9A3412"));
                                    status.setBackgroundResource(R.drawable.badge_bg);
                                    status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFEDD5")));
                                } else if ("blocked".equals(uStatus)) {
                                    status.setText("Blocked");
                                    status.setTextColor(Color.parseColor("#991B1B"));
                                    status.setBackgroundResource(R.drawable.badge_bg);
                                    status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FEE2E2")));
                                } else if ("suspended".equals(uStatus)) {
                                    status.setText("Suspended");
                                    status.setTextColor(Color.parseColor("#9A3412"));
                                    status.setBackgroundResource(R.drawable.badge_bg);
                                    status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#FFEDD5")));
                                } else {
                                    status.setText("Active");
                                    status.setTextColor(Color.parseColor("#166534"));
                                    status.setBackgroundResource(R.drawable.badge_bg);
                                    status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#DCFCE7")));
                                }
                                
                                row.addView(col1);
                                row.addView(role);
                                row.addView(status);
                                
                                container.addView(row);
                                
                                View div = new View(this);
                                div.setLayoutParams(new LinearLayout.LayoutParams(-1, 2));
                                div.setBackgroundColor(Color.parseColor("#F1F5F9"));
                                container.addView(div);
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
                        LinearLayout container = findViewById(R.id.containerEnrollmentControl);
                        container.removeAllViews();
                        if (obj.getBoolean("success")) {
                            JSONArray pending = obj.getJSONArray("pending");
                            if (pending.length() > 0) {
                                for (int i = 0; i < pending.length(); i++) {
                                    JSONObject e = pending.getJSONObject(i);
                                    TextView t = new TextView(this);
                                    t.setText(e.getString("student_name") + " requested " + e.getString("course_title"));
                                    t.setPadding(0, 8, 0, 8);
                                    t.setTextColor(Color.parseColor("#1a2a4a"));
                                    container.addView(t);
                                }
                            } else {
                                TextView t = new TextView(this);
                                t.setText("No pending course enrollments.");
                                t.setTextColor(Color.parseColor("#6B7280"));
                                container.addView(t);
                            }
                        }
                    } catch (Exception e) {}
                }, error -> {});
        Volley.newRequestQueue(this).add(req);
    }
}
