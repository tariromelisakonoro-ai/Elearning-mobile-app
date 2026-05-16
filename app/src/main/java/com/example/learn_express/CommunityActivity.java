package com.example.learn_express;

import android.content.Intent;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

public class CommunityActivity extends BaseActivity {
    private String selectedSubject = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community);
        setupNavigationBar();

        TextView heroTitle = findViewById(R.id.communityHeroTitle);
        if (heroTitle != null) {
            heroTitle.post(() -> {
                Shader textShader = new LinearGradient(0, 0, heroTitle.getWidth(), 0,
                        new int[]{0xFF4B2C5E, 0xFFF97316},
                        null, Shader.TileMode.CLAMP);
                heroTitle.getPaint().setShader(textShader);
                heroTitle.invalidate();
            });
        }

        setupNavButtons();
        setupPostForm();
        setupFilterCards();
        setupCollabFeatures();
        setupExploreCards();
        refreshLists();

        TextView backHome = findViewById(R.id.tvBackToHome);
        if (backHome != null) {
            backHome.setOnClickListener(v -> finish());
        }
    }

    private void setupNavButtons() {
        ScrollView scrollView = findViewById(R.id.communityScrollView);
        
        findViewById(R.id.btnNavAnnouncements).setOnClickListener(v -> {
            View target = findViewById(R.id.sectionAnnouncements);
            if (target != null) scrollView.smoothScrollTo(0, target.getTop());
        });

        findViewById(R.id.btnNavAsk).setOnClickListener(v -> {
            View target = findViewById(R.id.sectionAsk);
            if (target != null) scrollView.smoothScrollTo(0, target.getTop());
        });
    }

    private void setupPostForm() {
        TextView tvPicker = findViewById(R.id.tvSpinnerPlaceholder);
        String[] subjects = getResources().getStringArray(R.array.subjects_array);

        View btnPicker = findViewById(R.id.btnSubjectPicker);
        if (btnPicker != null) {
            btnPicker.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle(R.string.select_subject_area)
                    .setItems(subjects, (dialog, which) -> {
                        selectedSubject = subjects[which];
                        tvPicker.setText(selectedSubject);
                    })
                    .show());
        }

        findViewById(R.id.btnPostQuestion).setOnClickListener(v -> {
            EditText etTitle = findViewById(R.id.etQuestionTitle);
            EditText etDetail = findViewById(R.id.etQuestionDetail);

            String title = etTitle.getText().toString().trim();
            String detail = etDetail.getText().toString().trim();

            if (title.isEmpty() || detail.isEmpty() || selectedSubject.isEmpty()) {
                Toast.makeText(this, R.string.fill_all_fields, Toast.LENGTH_SHORT).show();
                return;
            }

            // Add to DataManager
            DataManager.getInstance().addQuestion(new DataManager.Question(title, detail, selectedSubject));
            
            // Reset form
            etTitle.setText("");
            etDetail.setText("");
            tvPicker.setText(R.string.select_subject_area_placeholder);
            selectedSubject = "";

            Toast.makeText(this, R.string.question_posted_success, Toast.LENGTH_SHORT).show();
            refreshLists();
            
            // Scroll to feed
            findViewById(R.id.sectionQA).post(() -> 
                ((ScrollView)findViewById(R.id.communityScrollView)).smoothScrollTo(0, findViewById(R.id.sectionQA).getTop()));
        });
    }

    private void setupFilterCards() {
        View.OnClickListener filterClick = v -> {
            Toast.makeText(this, R.string.filtering_feed, Toast.LENGTH_SHORT).show();
            View target = findViewById(R.id.sectionQA);
            ((ScrollView)findViewById(R.id.communityScrollView)).smoothScrollTo(0, target.getTop());
        };

        findViewById(R.id.cardAllDiscussions).setOnClickListener(filterClick);
        findViewById(R.id.cardMath).setOnClickListener(filterClick);
        findViewById(R.id.cardSciences).setOnClickListener(filterClick);
        findViewById(R.id.cardEnglish).setOnClickListener(filterClick);
        
        findViewById(R.id.filterAll).setOnClickListener(filterClick);
        findViewById(R.id.filterMath).setOnClickListener(filterClick);
        findViewById(R.id.filterSciences).setOnClickListener(filterClick);
        findViewById(R.id.filterEnglish).setOnClickListener(filterClick);
        findViewById(R.id.filterHistory).setOnClickListener(filterClick);
        findViewById(R.id.filterBusiness).setOnClickListener(filterClick);
        findViewById(R.id.filterCS).setOnClickListener(filterClick);
        findViewById(R.id.filterOther).setOnClickListener(filterClick);
    }

    private void refreshLists() {
        androidx.recyclerview.widget.RecyclerView recyclerAnnouncements = findViewById(R.id.recyclerAnnouncements);
        androidx.recyclerview.widget.RecyclerView recyclerQuestions = findViewById(R.id.recyclerQuestions);
        TextView tvNoAnnouncements = findViewById(R.id.tvNoAnnouncements);
        TextView tvNoQuestions = findViewById(R.id.tvNoQuestions);

        recyclerAnnouncements.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        recyclerQuestions.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));

        // Fetch Announcements
        String urlAnn = Constants.BASE_URL + "admin_announcements.php";
        com.android.volley.toolbox.StringRequest reqAnn = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.GET, urlAnn,
                response -> {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response);
                        if (obj.getBoolean("success")) {
                            org.json.JSONArray items = obj.getJSONArray("announcements");
                            if (items.length() > 0) {
                                tvNoAnnouncements.setVisibility(View.GONE);
                                recyclerAnnouncements.setAdapter(new AnnouncementAdapter(items));
                            } else {
                                tvNoAnnouncements.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Exception e) {}
                }, error -> tvNoAnnouncements.setVisibility(View.VISIBLE));

        // Fetch Q&A Questions
        String urlQa = Constants.BASE_URL + "community_questions.php";
        com.android.volley.toolbox.StringRequest reqQa = new com.android.volley.toolbox.StringRequest(
                com.android.volley.Request.Method.GET, urlQa,
                response -> {
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(response);
                        if (obj.getBoolean("success")) {
                            org.json.JSONArray items = obj.getJSONArray("questions");
                            if (items.length() > 0) {
                                tvNoQuestions.setVisibility(View.GONE);
                                recyclerQuestions.setAdapter(new QAAdapter(items));
                            } else {
                                tvNoQuestions.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (Exception e) {}
                }, error -> tvNoQuestions.setVisibility(View.VISIBLE));

        com.android.volley.toolbox.Volley.newRequestQueue(this).add(reqAnn);
        com.android.volley.toolbox.Volley.newRequestQueue(this).add(reqQa);
    }

    private class AnnouncementAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<AnnouncementAdapter.VH> {
        org.json.JSONArray items;
        AnnouncementAdapter(org.json.JSONArray items) { this.items = items; }
        @androidx.annotation.NonNull @Override public VH onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_announcement, parent, false);
            return new VH(view);
        }
        @Override public void onBindViewHolder(@androidx.annotation.NonNull VH holder, int position) {
            try {
                org.json.JSONObject item = items.getJSONObject(position);
                holder.title.setText(item.optString("title"));
                holder.message.setText(item.optString("message"));
                String author = item.optString("author_name", "LearnExpress Admin");
                holder.meta.setText("Posted by " + author);
            } catch (Exception e) {}
        }
        @Override public int getItemCount() { return items.length(); }
        class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView title, message, meta;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tvAnnTitle);
                message = v.findViewById(R.id.tvAnnMessage);
                meta = v.findViewById(R.id.tvAnnMeta);
            }
        }
    }

    private class QAAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<QAAdapter.VH> {
        org.json.JSONArray items;
        QAAdapter(org.json.JSONArray items) { this.items = items; }
        @androidx.annotation.NonNull @Override public VH onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_qa, parent, false);
            return new VH(view);
        }
        @Override public void onBindViewHolder(@androidx.annotation.NonNull VH holder, int position) {
            try {
                org.json.JSONObject item = items.getJSONObject(position);
                holder.title.setText(item.optString("title"));
                holder.subject.setText(item.optString("subject", "General"));
                holder.body.setText(item.optString("body"));
                holder.votes.setText(String.valueOf(item.optInt("upvotes", 0)));
                holder.author.setText("👤 " + item.optString("author", "Anonymous"));
                holder.time.setText("🕒 " + item.optString("created_at", "Recently"));
            } catch (Exception e) {}
        }
        @Override public int getItemCount() { return items.length(); }
        class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            TextView title, subject, body, votes, author, time;
            VH(View v) {
                super(v);
                title = v.findViewById(R.id.tvQaTitle);
                subject = v.findViewById(R.id.tvQaSubject);
                body = v.findViewById(R.id.tvQaBody);
                votes = v.findViewById(R.id.tvQaVotes);
                author = v.findViewById(R.id.tvQaAuthor);
                time = v.findViewById(R.id.tvQaTime);
            }
        }
    }

    private void setupCollabFeatures() {
        // Weekly Challenges
        View v1 = findViewById(R.id.collabWeekly);
        ((TextView) v1.findViewById(R.id.tvCollabIcon)).setText("🏆");
        ((TextView) v1.findViewById(R.id.tvCollabTitle)).setText(R.string.collab_weekly_title);
        ((TextView) v1.findViewById(R.id.tvCollabDesc)).setText(R.string.collab_weekly_desc);

        // Teacher Interaction
        View v2 = findViewById(R.id.collabTeacher);
        ((TextView) v2.findViewById(R.id.tvCollabIcon)).setText("👩‍🏫");
        ((TextView) v2.findViewById(R.id.tvCollabTitle)).setText(R.string.collab_teacher_title);
        ((TextView) v2.findViewById(R.id.tvCollabDesc)).setText(R.string.collab_teacher_desc);

        // Live Sessions
        View v3 = findViewById(R.id.collabLive);
        ((TextView) v3.findViewById(R.id.tvCollabIcon)).setText("🗓️");
        ((TextView) v3.findViewById(R.id.tvCollabTitle)).setText(R.string.collab_live_title);
        ((TextView) v3.findViewById(R.id.tvCollabDesc)).setText(R.string.collab_live_desc);

        // Study Buddies
        View v4 = findViewById(R.id.collabBuddy);
        ((TextView) v4.findViewById(R.id.tvCollabIcon)).setText("🤝");
        ((TextView) v4.findViewById(R.id.tvCollabTitle)).setText(R.string.collab_buddy_title);
        ((TextView) v4.findViewById(R.id.tvCollabDesc)).setText(R.string.collab_buddy_desc);
    }

    private void setupExploreCards() {
        // Browse Courses
        View e1 = findViewById(R.id.exploreCourses);
        ((TextView) e1.findViewById(R.id.tvExploreIcon)).setText("📚");
        ((TextView) e1.findViewById(R.id.tvExploreTitle)).setText(R.string.explore_courses_title);
        ((TextView) e1.findViewById(R.id.tvExploreDesc)).setText(R.string.explore_courses_desc);
        View btn1 = e1.findViewById(R.id.btnExploreAction);
        ((TextView) btn1).setText(R.string.go_to_courses);
        btn1.setOnClickListener(v -> startActivity(new Intent(this, CoursesActivity.class)));

        // Assignments
        View e2 = findViewById(R.id.exploreAssignments);
        ((TextView) e2.findViewById(R.id.tvExploreIcon)).setText("📄");
        ((TextView) e2.findViewById(R.id.tvExploreTitle)).setText(R.string.explore_assignments_title);
        ((TextView) e2.findViewById(R.id.tvExploreDesc)).setText(R.string.explore_assignments_desc);
        View btn2 = e2.findViewById(R.id.btnExploreAction);
        ((TextView) btn2).setText(R.string.view_assignments);
        btn2.setOnClickListener(v -> startActivity(new Intent(this, AssignmentsActivity.class)));

        // Exams & Quizzes
        View e3 = findViewById(R.id.exploreExams);
        ((TextView) e3.findViewById(R.id.tvExploreIcon)).setText("🧪");
        ((TextView) e3.findViewById(R.id.tvExploreTitle)).setText(R.string.explore_exams_title);
        ((TextView) e3.findViewById(R.id.tvExploreDesc)).setText(R.string.explore_exams_desc);
        View btn3 = e3.findViewById(R.id.btnExploreAction);
        ((TextView) btn3).setText(R.string.open_exams);
        btn3.setOnClickListener(v -> startActivity(new Intent(this, ExamsActivity.class)));

        // Study Resources
        View e4 = findViewById(R.id.exploreResources);
        ((TextView) e4.findViewById(R.id.tvExploreIcon)).setText("📂");
        ((TextView) e4.findViewById(R.id.tvExploreTitle)).setText(R.string.explore_resources_title);
        ((TextView) e4.findViewById(R.id.tvExploreDesc)).setText(R.string.explore_resources_desc);
        View btn4 = e4.findViewById(R.id.btnExploreAction);
        ((TextView) btn4).setText(R.string.view_resources);
        btn4.setOnClickListener(v -> startActivity(new Intent(this, ResourcesActivity.class)));
    }
}