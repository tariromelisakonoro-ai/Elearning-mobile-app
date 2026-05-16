package com.example.learn_express;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public abstract class BaseActivity extends AppCompatActivity
        implements NetworkStateReceiver.NetworkStateListener {

    // Brand colors
    private static final int COLOR_PRIMARY = Color.parseColor("#FF7A1A");
    private static final int COLOR_MUTED   = Color.parseColor("#5F6C8A");
    private static final int COLOR_HEADING = Color.parseColor("#142A5B");

    // Offline banner
    private TextView      tvOfflineBanner;
    private NetworkStateReceiver networkReceiver;

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Register connectivity receiver
        networkReceiver = new NetworkStateReceiver(this);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (networkReceiver != null) {
            unregisterReceiver(networkReceiver);
        }
    }

    // -----------------------------------------------------------------------
    // Offline banner injection — called AFTER setContentView in subclasses
    // -----------------------------------------------------------------------

    /**
     * Injects the offline banner into the root view of the activity and
     * sets its initial visibility based on current connectivity.
     * Subclasses should call this after setContentView().
     */
    protected void setupOfflineBanner() {
        ViewGroup root = (ViewGroup) getWindow().getDecorView()
                .findViewById(android.R.id.content);

        // Build banner programmatically so no layout changes are needed
        tvOfflineBanner = new TextView(this);
        tvOfflineBanner.setText("📡  You are offline — showing cached data");
        tvOfflineBanner.setTextColor(Color.WHITE);
        tvOfflineBanner.setTextSize(12f);
        tvOfflineBanner.setGravity(Gravity.CENTER);
        tvOfflineBanner.setBackgroundColor(Color.parseColor("#CC2D2D")); // deep red
        tvOfflineBanner.setPadding(24, 20, 24, 20);
        tvOfflineBanner.setElevation(8f);

        // Position banner at the very top of the decor view
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.TOP;
        tvOfflineBanner.setLayoutParams(params);

        root.addView(tvOfflineBanner);

        // Set initial state
        updateBannerState(NetworkUtils.isOnline(this));
    }

    private void updateBannerState(boolean online) {
        if (tvOfflineBanner == null) return;
        runOnUiThread(() -> {
            if (online) {
                tvOfflineBanner.setVisibility(View.GONE);
            } else {
                tvOfflineBanner.setVisibility(View.VISIBLE);
            }
        });
    }

    // -----------------------------------------------------------------------
    // NetworkStateListener callbacks
    // -----------------------------------------------------------------------

    @Override
    public void onNetworkAvailable() {
        updateBannerState(true);
        onConnectivityRestored(); // hook for subclasses to refresh data
    }

    @Override
    public void onNetworkLost() {
        updateBannerState(false);
    }

    /**
     * Override this in subclasses to trigger data refresh when connectivity
     * is restored while the activity is visible.
     */
    protected void onConnectivityRestored() {
        // Default: do nothing. Subclasses override.
    }

    // -----------------------------------------------------------------------
    // Navigation helpers
    // -----------------------------------------------------------------------

    protected void setupNavigationBar() {
        TextView navHome        = findViewById(R.id.navHome);
        TextView navCourses     = findViewById(R.id.navCourses);
        TextView navAssignments = findViewById(R.id.navAssignments);
        TextView navExams       = findViewById(R.id.navExams);
        TextView navResources   = findViewById(R.id.navResources);
        TextView navCommunity   = findViewById(R.id.navCommunity);
        TextView navSignIn      = findViewById(R.id.navSignIn);
        TextView navSignUp      = findViewById(R.id.navSignUp);
        TextView tvSignOut      = findViewById(R.id.tvSignOut);

        setupNavItem(navHome,        MainActivity.class);
        setupNavItem(navCourses,     CoursesActivity.class);
        setupNavItem(navAssignments, AssignmentsActivity.class);
        setupNavItem(navExams,       ExamsActivity.class);
        setupNavItem(navResources,   ResourcesActivity.class);
        setupNavItem(navCommunity,   CommunityActivity.class);
        setupNavItem(navSignIn,      LoginActivity.class);
        setupNavItem(navSignUp,      SignupActivity.class);

        // Check login state
        android.content.SharedPreferences sharedPref =
                getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
        String currentUserEmail = sharedPref.getString("current_user_email", "");
        boolean isLoggedIn = !currentUserEmail.isEmpty();

        if (isLoggedIn) {
            if (navSignIn != null) navSignIn.setVisibility(View.GONE);
            if (navSignUp != null) navSignUp.setVisibility(View.GONE);
            if (tvSignOut != null) tvSignOut.setVisibility(View.VISIBLE);
        } else {
            if (navSignIn != null) navSignIn.setVisibility(View.VISIBLE);
            if (navSignUp != null) navSignUp.setVisibility(View.VISIBLE);
            if (tvSignOut != null) tvSignOut.setVisibility(View.GONE);
        }

        if (tvSignOut != null) {
            tvSignOut.setOnClickListener(v -> {
                // Clear cache on sign-out
                new OfflineCacheManager(this).clearAll();
                sharedPref.edit().clear().apply();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }

    private void setupNavItem(TextView view, Class<?> targetActivity) {
        if (view == null) return;
        boolean isActive = this.getClass().equals(targetActivity);
        view.setTextColor(isActive ? COLOR_PRIMARY : COLOR_MUTED);
        if (isActive) view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setOnClickListener(v -> navigateTo(targetActivity));
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setTextColor(COLOR_PRIMARY);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (!isActive) view.setTextColor(COLOR_MUTED);
                    break;
            }
            return false;
        });
    }

    private void navigateTo(Class<?> targetActivity) {
        if (!this.getClass().equals(targetActivity)) {
            startActivity(new Intent(this, targetActivity));
        }
    }
}