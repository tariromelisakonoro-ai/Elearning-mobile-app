package com.example.learn_express;

import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class ResourcesActivity extends BaseActivity {

    private java.util.List<Material> list;
    private MaterialAdapter          adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);
        setupOfflineBanner();
        setupNavigationBar();

        TextView heroTitle = findViewById(R.id.resourcesHeroTitle);
        if (heroTitle != null) {
            heroTitle.post(() -> {
                Shader shader = new LinearGradient(0, 0, heroTitle.getWidth(), 0,
                        new int[]{0xFF4B2C5E, 0xFFF97316}, null, Shader.TileMode.CLAMP);
                heroTitle.getPaint().setShader(shader);
                heroTitle.invalidate();
            });
        }

        androidx.recyclerview.widget.RecyclerView recycler = findViewById(R.id.resourceRecycler);
        recycler.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        list    = new java.util.ArrayList<>();
        adapter = new MaterialAdapter(list);
        recycler.setAdapter(adapter);

        loadResources();

        TextView backHome = findViewById(R.id.tvBackToHome);
        if (backHome != null) backHome.setOnClickListener(v -> finish());
    }

    private void loadResources() {
        if (NetworkUtils.isOnline(this)) {
            fetchResources();
        } else {
            loadFromCache();
        }
    }

    private void fetchResources() {
        String url = Constants.BASE_URL + "get_materials.php?type=resource";
        com.android.volley.toolbox.StringRequest request =
                new com.android.volley.toolbox.StringRequest(
                        com.android.volley.Request.Method.GET, url,
                        response -> {
                            new OfflineCacheManager(this).save(OfflineCacheManager.KEY_RESOURCES, response);
                            parseResources(response, false);
                        },
                        error -> {
                            Toast.makeText(this, "Network error – loading cached data", Toast.LENGTH_SHORT).show();
                            loadFromCache();
                        });
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(request);
    }

    private void loadFromCache() {
        OfflineCacheManager cache = new OfflineCacheManager(this);
        if (cache.has(OfflineCacheManager.KEY_RESOURCES)) {
            String ago = cache.getLastUpdated(OfflineCacheManager.KEY_RESOURCES);
            parseResources(cache.load(OfflineCacheManager.KEY_RESOURCES), true);
            Toast.makeText(this, "Showing cached resources (updated " + ago + ")", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No cached resources – connect to load", Toast.LENGTH_LONG).show();
        }
    }

    private void parseResources(String json, boolean fromCache) {
        try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            if (obj.getBoolean("success")) {
                list.clear();
                org.json.JSONArray items = obj.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    org.json.JSONObject r = items.getJSONObject(i);
                    String type = r.optString("type", "");
                    if (!type.equals("exam") && !type.equals("quiz") && !type.equals("assignment")) {
                        list.add(new Material(
                                r.optString("id", ""),
                                type,
                                r.optString("title", "Untitled"),
                                r.optString("description", ""),
                                r.optString("course_title", "General"),
                                r.optString("instructor_name", "Instructor"),
                                r.optString("due_date", null),
                                r.optString("grading_criteria", null),
                                r.optString("file_url", null)));
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
        Toast.makeText(this, "Back online – refreshing resources…", Toast.LENGTH_SHORT).show();
        fetchResources();
    }
}