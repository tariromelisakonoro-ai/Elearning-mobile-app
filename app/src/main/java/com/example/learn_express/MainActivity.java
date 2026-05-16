package com.example.learn_express;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupNavigationBar();

        // Hero Background: Website uses same Unsplash image
        ImageView ivHeroBackground = findViewById(R.id.ivHeroBackground);
        String imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=1600&q=80";

        Glide.with(this)
                .load(imageUrl)
                .centerCrop()
                .into(ivHeroBackground);

        setupHeroButtons();
        setupFeatureCards();
        setupInfoCards();
        setupCourseCards();
        setupHowItWorks();
        setupTestimonials();

        // View All Courses Button
        findViewById(R.id.btnViewAllCourses)
                .setOnClickListener(v -> startActivity(new Intent(this, CoursesActivity.class)));

        // Back to top functionality
        TextView tvBackToTop = findViewById(R.id.tvBackToTop);
        ScrollView scrollView = findViewById(R.id.mainScrollView);
        tvBackToTop.setOnClickListener(v -> scrollView.smoothScrollTo(0, 0));
    }

    private void setupHeroButtons() {
        android.content.SharedPreferences sharedPref = getSharedPreferences("UserPrefs",
                android.content.Context.MODE_PRIVATE);
        String currentUserEmail = sharedPref.getString("current_user_email", "");
        boolean isLoggedIn = !currentUserEmail.isEmpty();
        String role = sharedPref.getString(currentUserEmail + "_role", "Student");

        androidx.appcompat.widget.AppCompatButton btnSignUpFree = findViewById(R.id.btnSignUpFree);
        androidx.appcompat.widget.AppCompatButton btnSignIn = findViewById(R.id.btnSignIn);
        androidx.appcompat.widget.AppCompatButton btnBrowseCourses = findViewById(R.id.btnBrowseCourses);

        if (isLoggedIn) {
            btnSignUpFree.setText("Go to Dashboard");
            btnSignUpFree.setOnClickListener(v -> {
                Intent intent;
                if (role.equalsIgnoreCase("Administrator")) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                } else if (role.equalsIgnoreCase("Instructor")) {
                    intent = new Intent(this, InstructorDashboardActivity.class);
                } else if (role.equalsIgnoreCase("Parent")) {
                    intent = new Intent(this, ParentDashboardActivity.class);
                } else {
                    intent = new Intent(this, StudentDashboardActivity.class);
                }
                startActivity(intent);
            });
            btnSignIn.setVisibility(View.GONE);
        } else {
            btnSignUpFree.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
            btnSignIn.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }

        btnBrowseCourses.setOnClickListener(v -> startActivity(new Intent(this, CoursesActivity.class)));
    }

    private void setupFeatureCards() {
        // Learn Anytime
        View f1 = findViewById(R.id.featureLearnAnytime);
        ((ImageView) f1.findViewById(R.id.ivFeatureImage)).setImageResource(R.drawable.learn_anytime);
        ((TextView) f1.findViewById(R.id.tvFeatureTitle)).setText(R.string.feature_learn_anytime_title);
        ((TextView) f1.findViewById(R.id.tvFeatureDesc)).setText(R.string.feature_learn_anytime_desc);
        TextView link1 = f1.findViewById(R.id.tvFeatureLink);
        link1.setText(R.string.feature_explore_courses);
        f1.setOnClickListener(v -> startActivity(new Intent(this, CoursesActivity.class)));

        // Stay Focused
        View f2 = findViewById(R.id.featureStayFocused);
        ((ImageView) f2.findViewById(R.id.ivFeatureImage)).setImageResource(R.drawable.stay_focussed);
        ((TextView) f2.findViewById(R.id.tvFeatureTitle)).setText(R.string.feature_stay_focused_title);
        ((TextView) f2.findViewById(R.id.tvFeatureDesc)).setText(R.string.feature_stay_focused_desc);
        TextView link2 = f2.findViewById(R.id.tvFeatureLink);
        link2.setText(R.string.feature_view_resources);
        f2.setOnClickListener(v -> startActivity(new Intent(this, ResourcesActivity.class)));

        // Collaborate Easily
        View f3 = findViewById(R.id.featureCollaborate);
        ((ImageView) f3.findViewById(R.id.ivFeatureImage)).setImageResource(R.drawable.collaborative);
        ((TextView) f3.findViewById(R.id.tvFeatureTitle)).setText(R.string.feature_collaborate_title);
        ((TextView) f3.findViewById(R.id.tvFeatureDesc)).setText(R.string.feature_collaborate_desc);
        TextView link3 = f3.findViewById(R.id.tvFeatureLink);
        link3.setText(R.string.feature_join_community);
        f3.setOnClickListener(v -> startActivity(new Intent(this, CommunityActivity.class)));
    }

    private void setupInfoCards() {
        // Website colors: card-icon-circle backgrounds matching website's emoji bg
        // tints
        // What is it? — 📖 book icon
        View i1 = findViewById(R.id.infoWhatIsIt);
        ((TextView) i1.findViewById(R.id.tvTitle)).setText(R.string.info_what_is_it_title);
        ((TextView) i1.findViewById(R.id.tvDesc)).setText(R.string.info_what_is_it_desc);
        ((TextView) i1.findViewById(R.id.tvAction)).setText(R.string.info_see_courses);
        ImageView iv1 = i1.findViewById(R.id.ivIcon);
        iv1.setImageResource(R.drawable.ic_book);
        iv1.setColorFilter(Color.parseColor("#FF7A1A")); // Website primary
        ((CardView) i1.findViewById(R.id.cvIconContainer)).setCardBackgroundColor(Color.parseColor("#FFF3E6")); // orange
                                                                                                                // tint
        i1.setOnClickListener(v -> startActivity(new Intent(this, CoursesActivity.class)));

        // Who is it for? — 🎓 school icon
        View i2 = findViewById(R.id.infoWhoIsItFor);
        ((TextView) i2.findViewById(R.id.tvTitle)).setText(R.string.info_who_is_it_for_title);
        ((TextView) i2.findViewById(R.id.tvDesc)).setText(R.string.info_who_is_it_for_desc);
        ((TextView) i2.findViewById(R.id.tvAction)).setText(R.string.info_join_now);
        ImageView iv2 = i2.findViewById(R.id.ivIcon);
        iv2.setImageResource(R.drawable.ic_school);
        iv2.setColorFilter(Color.parseColor("#FF7A1A"));
        ((CardView) i2.findViewById(R.id.cvIconContainer)).setCardBackgroundColor(Color.parseColor("#FFF3E6"));
        i2.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));

        // Why care? — ⚡ flash icon
        View i3 = findViewById(R.id.infoWhyCare);
        ((TextView) i3.findViewById(R.id.tvTitle)).setText(R.string.info_why_care_title);
        ((TextView) i3.findViewById(R.id.tvDesc)).setText(R.string.info_why_care_desc);
        ((TextView) i3.findViewById(R.id.tvAction)).setText(R.string.info_get_started);
        ImageView iv3 = i3.findViewById(R.id.ivIcon);
        iv3.setImageResource(R.drawable.ic_flash);
        iv3.setColorFilter(Color.parseColor("#FF7A1A"));
        ((CardView) i3.findViewById(R.id.cvIconContainer)).setCardBackgroundColor(Color.parseColor("#FFF3E6"));
        i3.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    private void setupCourseCards() {
        // Website: course category cards with emoji icons and orange tint backgrounds
        // Mathematics — 🧮
        View c1 = findViewById(R.id.courseMath);
        ((TextView) c1.findViewById(R.id.tvTitle)).setText(R.string.course_math_title);
        ((TextView) c1.findViewById(R.id.tvDesc)).setText(R.string.course_math_desc);
        ((TextView) c1.findViewById(R.id.tvAction)).setText(R.string.course_browse_math);
        ImageView iv1 = c1.findViewById(R.id.ivIcon);
        iv1.setImageResource(R.drawable.ic_math);
        iv1.setColorFilter(Color.parseColor("#FF7A1A"));
        ((CardView) c1.findViewById(R.id.cvIconContainer)).setCardBackgroundColor(Color.parseColor("#FFF3E6"));
        c1.setOnClickListener(v -> startActivity(new Intent(this, CoursesActivity.class)));

        // Sciences — 🧬
        View c2 = findViewById(R.id.courseScience);
        ((TextView) c2.findViewById(R.id.tvTitle)).setText(R.string.course_science_title);
        ((TextView) c2.findViewById(R.id.tvDesc)).setText(R.string.course_science_desc);
        ((TextView) c2.findViewById(R.id.tvAction)).setText(R.string.course_browse_science);
        ImageView iv2 = c2.findViewById(R.id.ivIcon);
        iv2.setImageResource(R.drawable.ic_science);
        iv2.setColorFilter(Color.parseColor("#FF7A1A"));
        ((CardView) c2.findViewById(R.id.cvIconContainer)).setCardBackgroundColor(Color.parseColor("#FFF3E6"));
        c2.setOnClickListener(v -> startActivity(new Intent(this, CoursesActivity.class)));

        // Humanities — 📚
        View c3 = findViewById(R.id.courseHumanities);
        ((TextView) c3.findViewById(R.id.tvTitle)).setText(R.string.course_humanities_title);
        ((TextView) c3.findViewById(R.id.tvDesc)).setText(R.string.course_humanities_desc);
        ((TextView) c3.findViewById(R.id.tvAction)).setText(R.string.course_browse_humanities);
        ImageView iv3 = c3.findViewById(R.id.ivIcon);
        iv3.setImageResource(R.drawable.ic_humanities);
        iv3.setColorFilter(Color.parseColor("#FF7A1A"));
        ((CardView) c3.findViewById(R.id.cvIconContainer)).setCardBackgroundColor(Color.parseColor("#FFF3E6"));
        c3.setOnClickListener(v -> startActivity(new Intent(this, CoursesActivity.class)));
    }

    private void setupHowItWorks() {
        // Step 1
        View s1 = findViewById(R.id.stepSignUp);
        ((TextView) s1.findViewById(R.id.tvStepNumber)).setText("1");
        ((TextView) s1.findViewById(R.id.tvStepTitle)).setText(R.string.step_signup_title);
        ((TextView) s1.findViewById(R.id.tvStepDesc)).setText(R.string.step_signup_desc);
        ((TextView) s1.findViewById(R.id.tvStepAction)).setText(R.string.step_create_account);
        s1.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));

        // Step 2
        View s2 = findViewById(R.id.stepLearn);
        ((TextView) s2.findViewById(R.id.tvStepNumber)).setText("2");
        ((TextView) s2.findViewById(R.id.tvStepTitle)).setText(R.string.step_learn_title);
        ((TextView) s2.findViewById(R.id.tvStepDesc)).setText(R.string.step_learn_desc);
        ((TextView) s2.findViewById(R.id.tvStepAction)).setText(R.string.step_explore_lessons);
        s2.setOnClickListener(v -> startActivity(new Intent(this, ResourcesActivity.class)));

        // Step 3
        View s3 = findViewById(R.id.stepTrack);
        ((TextView) s3.findViewById(R.id.tvStepNumber)).setText("3");
        ((TextView) s3.findViewById(R.id.tvStepTitle)).setText(R.string.step_track_title);
        ((TextView) s3.findViewById(R.id.tvStepDesc)).setText(R.string.step_track_desc);
        ((TextView) s3.findViewById(R.id.tvStepAction)).setText(R.string.step_see_dashboard);
        s3.setOnClickListener(v -> {
            android.content.SharedPreferences sharedPref = getSharedPreferences("UserPrefs",
                    android.content.Context.MODE_PRIVATE);
            String currentUserEmail = sharedPref.getString("current_user_email", "");
            if (!currentUserEmail.isEmpty()) {
                String role = sharedPref.getString(currentUserEmail + "_role", "Student");
                Intent intent;
                if (role.equalsIgnoreCase("Administrator")) {
                    intent = new Intent(this, AdminDashboardActivity.class);
                } else if (role.equalsIgnoreCase("Instructor")) {
                    intent = new Intent(this, InstructorDashboardActivity.class);
                } else if (role.equalsIgnoreCase("Parent")) {
                    intent = new Intent(this, ParentDashboardActivity.class);
                } else {
                    intent = new Intent(this, StudentDashboardActivity.class);
                }
                startActivity(intent);
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
    }

    private void setupTestimonials() {
        // Testimonial 1: Sarah M.
        View t1 = findViewById(R.id.testimonial1);
        ((TextView) t1.findViewById(R.id.tvQuote)).setText(getString(R.string.testimonial1_quote));
        ((TextView) t1.findViewById(R.id.tvAvatar)).setText(getString(R.string.testimonial1_initials));
        ((TextView) t1.findViewById(R.id.tvName)).setText(getString(R.string.testimonial1_name));
        ((TextView) t1.findViewById(R.id.tvDetails)).setText(getString(R.string.testimonial1_details));

        // Testimonial 2: David K.
        View t2 = findViewById(R.id.testimonial2);
        ((TextView) t2.findViewById(R.id.tvQuote)).setText(getString(R.string.testimonial2_quote));
        ((TextView) t2.findViewById(R.id.tvAvatar)).setText(getString(R.string.testimonial2_initials));
        ((TextView) t2.findViewById(R.id.tvName)).setText(getString(R.string.testimonial2_name));
        ((TextView) t2.findViewById(R.id.tvDetails)).setText(getString(R.string.testimonial2_details));

        // Testimonial 3: Chloe T.
        View t3 = findViewById(R.id.testimonial3);
        ((TextView) t3.findViewById(R.id.tvQuote)).setText(getString(R.string.testimonial3_quote));
        ((TextView) t3.findViewById(R.id.tvAvatar)).setText(getString(R.string.testimonial3_initials));
        ((TextView) t3.findViewById(R.id.tvName)).setText(getString(R.string.testimonial3_name));
        ((TextView) t3.findViewById(R.id.tvDetails)).setText(getString(R.string.testimonial3_details));
    }
}
