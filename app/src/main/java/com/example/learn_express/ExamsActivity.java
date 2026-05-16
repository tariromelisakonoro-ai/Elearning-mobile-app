package com.example.learn_express;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class ExamsActivity extends BaseActivity {

    private java.util.List<Material> list;
    private MaterialAdapter          adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exams);
        setupOfflineBanner();
        setupNavigationBar();

        TextView heroTitle = findViewById(R.id.examsHeroTitle);
        if (heroTitle != null) {
            heroTitle.post(() -> {
                Shader shader = new LinearGradient(0, 0, heroTitle.getWidth(), 0,
                        new int[]{0xFF4B2C5E, 0xFFF97316}, null, Shader.TileMode.CLAMP);
                heroTitle.getPaint().setShader(shader);
                heroTitle.invalidate();
            });
        }

        androidx.recyclerview.widget.RecyclerView recycler = findViewById(R.id.examRecycler);
        recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        list    = new java.util.ArrayList<>();
        adapter = new MaterialAdapter(list);
        recycler.setAdapter(adapter);

        loadExams();

        TextView backHome = findViewById(R.id.tvBackToHome);
        if (backHome != null) backHome.setOnClickListener(v -> finish());
    }

    private void loadExams() {
        if (NetworkUtils.isOnline(this)) {
            fetchExams();
        } else {
            loadFromCache();
        }
    }

    private void fetchExams() {
        String url = Constants.BASE_URL + "get_materials.php?type=exam";
        com.android.volley.toolbox.StringRequest request =
                new com.android.volley.toolbox.StringRequest(
                        com.android.volley.Request.Method.GET, url,
                        response -> {
                            new OfflineCacheManager(this).save(OfflineCacheManager.KEY_EXAMS, response);
                            parseExams(response, false);
                        },
                        error -> {
                            Toast.makeText(this, "Network error – loading cached data", Toast.LENGTH_SHORT).show();
                            loadFromCache();
                        });
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void loadFromCache() {
        OfflineCacheManager cache = new OfflineCacheManager(this);
        if (cache.has(OfflineCacheManager.KEY_EXAMS)) {
            String ago = cache.getLastUpdated(OfflineCacheManager.KEY_EXAMS);
            parseExams(cache.load(OfflineCacheManager.KEY_EXAMS), true);
            Toast.makeText(this, "Showing cached exams (updated " + ago + ")", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No cached exams – connect to load", Toast.LENGTH_LONG).show();
        }
    }

    private void parseExams(String json, boolean fromCache) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            if (obj.getBoolean("success")) {
                list.clear();
                org.json.JSONArray items = obj.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    org.json.JSONObject exam = items.getJSONObject(i);
                    String type = exam.optString("type", "");
                    if (type.equals("exam") || type.equals("quiz")) {
                        list.add(new Material(
                                exam.optString("id", ""),
                                type,
                                exam.optString("title", "Untitled"),
                                exam.optString("description", ""),
                                exam.optString("course_title", "General"),
                                exam.optString("instructor_name", "Instructor"),
                                exam.optString("due_date", null),
                                exam.optString("grading_criteria", null),
                                exam.optString("file_url", null)));
                    }
                }
                adapter.notifyDataSetChanged();
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onConnectivityRestored() {
        Toast.makeText(this, "Back online – refreshing exams…", Toast.LENGTH_SHORT).show();
        fetchExams();
    }
}