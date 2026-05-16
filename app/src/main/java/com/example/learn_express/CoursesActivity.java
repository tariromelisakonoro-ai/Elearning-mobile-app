package com.example.learn_express;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CoursesActivity extends BaseActivity {

    private List<Course>   list;
    private CourseAdapter  adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_courses);
        setupOfflineBanner();
        setupNavigationBar();

        // Gradient hero title
        TextView heroTitle = findViewById(R.id.animatedHeroTitle);
        if (heroTitle != null) {
            heroTitle.post(() -> {
                Shader textShader = new LinearGradient(0, 0, heroTitle.getWidth(), 0,
                        new int[]{0xFF4B2C5E, 0xFFF97316}, null, Shader.TileMode.CLAMP);
                heroTitle.getPaint().setShader(textShader);
                heroTitle.invalidate();
            });
        }

        RecyclerView recycler = findViewById(R.id.courseRecycler);
        recycler.setLayoutManager(new GridLayoutManager(this, 2));
        list    = new ArrayList<>();
        adapter = new CourseAdapter(list);
        recycler.setAdapter(adapter);

        loadCourses();

        TextView backHome = findViewById(R.id.tvBackToHome);
        if (backHome != null) backHome.setOnClickListener(v -> finish());
    }

    private void loadCourses() {
        if (NetworkUtils.isOnline(this)) {
            fetchCourses();
        } else {
            loadFromCache();
        }
    }

    private void fetchCourses() {
        com.android.volley.toolbox.StringRequest request =
                new com.android.volley.toolbox.StringRequest(
                        com.android.volley.Request.Method.GET, Constants.COURSES_URL,
                        response -> {
                            // Cache the response
                            new OfflineCacheManager(this).save(OfflineCacheManager.KEY_COURSES, response);
                            parseCourses(response, false);
                        },
                        error -> {
                            Toast.makeText(this, "Network error – loading cached data", Toast.LENGTH_SHORT).show();
                            loadFromCache();
                        });
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void loadFromCache() {
        OfflineCacheManager cache = new OfflineCacheManager(this);
        if (cache.has(OfflineCacheManager.KEY_COURSES)) {
            String cached = cache.load(OfflineCacheManager.KEY_COURSES);
            String ago    = cache.getLastUpdated(OfflineCacheManager.KEY_COURSES);
            parseCourses(cached, true);
            Toast.makeText(this, "Showing cached courses (updated " + ago + ")", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No cached courses – connect to the internet to load", Toast.LENGTH_LONG).show();
        }
    }

    private void parseCourses(String json, boolean fromCache) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            if (obj.getBoolean("success")) {
                list.clear();
                org.json.JSONArray coursesArray = obj.getJSONArray("courses");
                for (int i = 0; i < coursesArray.length(); i++) {
                    org.json.JSONObject c = coursesArray.getJSONObject(i);
                    list.add(new Course(
                            c.getString("title"),
                            c.optString("description", ""),
                            c.optString("instructor_name", "Unknown")));
                }
                adapter.notifyDataSetChanged();
            } else if (!fromCache) {
                Toast.makeText(this, "Failed to load courses.", Toast.LENGTH_SHORT).show();
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    // Auto-refresh when internet comes back
    @Override
    protected void onConnectivityRestored() {
        Toast.makeText(this, "Back online – refreshing courses…", Toast.LENGTH_SHORT).show();
        fetchCourses();
    }
}