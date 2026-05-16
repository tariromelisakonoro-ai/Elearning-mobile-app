package com.example.learn_express;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class AssignmentsActivity extends BaseActivity {

    private java.util.List<Material> list;
    private MaterialAdapter          adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignments);
        setupOfflineBanner();
        setupNavigationBar();

        TextView heroTitle = findViewById(R.id.assignmentsHeroTitle);
        if (heroTitle != null) {
            heroTitle.post(() -> {
                Shader shader = new LinearGradient(0, 0, heroTitle.getWidth(), 0,
                        new int[]{0xFF4B2C5E, 0xFFF97316}, null, Shader.TileMode.CLAMP);
                heroTitle.getPaint().setShader(shader);
                heroTitle.invalidate();
            });
        }

        androidx.recyclerview.widget.RecyclerView recycler = findViewById(R.id.assignmentRecycler);
        recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        list    = new java.util.ArrayList<>();
        adapter = new MaterialAdapter(list);
        recycler.setAdapter(adapter);

        loadAssignments();

        TextView backHome = findViewById(R.id.tvBackToHome);
        if (backHome != null) backHome.setOnClickListener(v -> finish());
    }

    private void loadAssignments() {
        if (NetworkUtils.isOnline(this)) {
            fetchAssignments();
        } else {
            loadFromCache();
        }
    }

    private void fetchAssignments() {
        String url = Constants.BASE_URL + "get_assignments.php";
        com.android.volley.toolbox.StringRequest request =
                new com.android.volley.toolbox.StringRequest(
                        com.android.volley.Request.Method.GET, url,
                        response -> {
                            new OfflineCacheManager(this).save(OfflineCacheManager.KEY_ASSIGNMENTS, response);
                            parseAssignments(response, false);
                        },
                        error -> {
                            Toast.makeText(this, "Network error – loading cached data", Toast.LENGTH_SHORT).show();
                            loadFromCache();
                        });
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void loadFromCache() {
        OfflineCacheManager cache = new OfflineCacheManager(this);
        if (cache.has(OfflineCacheManager.KEY_ASSIGNMENTS)) {
            String ago = cache.getLastUpdated(OfflineCacheManager.KEY_ASSIGNMENTS);
            parseAssignments(cache.load(OfflineCacheManager.KEY_ASSIGNMENTS), true);
            Toast.makeText(this, "Showing cached assignments (updated " + ago + ")", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No cached assignments – connect to load", Toast.LENGTH_LONG).show();
        }
    }

    private void parseAssignments(String json, boolean fromCache) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            if (obj.getBoolean("success")) {
                list.clear();
                org.json.JSONArray items = obj.getJSONArray("assignments");
                for (int i = 0; i < items.length(); i++) {
                    org.json.JSONObject a = items.getJSONObject(i);
                    list.add(new Material(
                            a.optString("id", ""),
                            "assignment",
                            a.optString("title", "Untitled"),
                            a.optString("description", ""),
                            a.optString("course_title", "General"),
                            a.optString("instructor_name", "Instructor"),
                            a.optString("due_date", null),
                            a.optString("grading_criteria", null),
                            a.optString("file_url", null)));
                }
                adapter.notifyDataSetChanged();
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onConnectivityRestored() {
        Toast.makeText(this, "Back online – refreshing assignments…", Toast.LENGTH_SHORT).show();
        fetchAssignments();
    }
}