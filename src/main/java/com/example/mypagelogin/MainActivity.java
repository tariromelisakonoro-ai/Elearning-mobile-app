package com.example.mypagelogin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set click listeners for course cards
        setCourseCardListeners();

        // Set click listener for logout button
        TextView logoutButton = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logout());
    }

    private void setCourseCardListeners() {
        View.OnClickListener courseClickListener = v -> {
            String courseName = "";
            int viewId = v.getId();
            if (viewId == R.id.htmlCard) {
                courseName = getString(R.string.course_html);
            } else if (viewId == R.id.cssCard) {
                courseName = getString(R.string.course_css);
            } else if (viewId == R.id.jsCard) {
                courseName = getString(R.string.course_js);
            } else if (viewId == R.id.pythonCard) {
                courseName = getString(R.string.course_python);
            } else if (viewId == R.id.javaCard) {
                courseName = getString(R.string.course_java);
            } else if (viewId == R.id.cppCard) {
                courseName = getString(R.string.course_cpp);
            } else if (viewId == R.id.sqlCard) {
                courseName = getString(R.string.course_sql);
            }

            if (!courseName.isEmpty()) {
                Toast.makeText(MainActivity.this, "Opening " + courseName + " course", Toast.LENGTH_SHORT).show();
            }
        };

        findViewById(R.id.htmlCard).setOnClickListener(courseClickListener);
        findViewById(R.id.cssCard).setOnClickListener(courseClickListener);
        findViewById(R.id.jsCard).setOnClickListener(courseClickListener);
        findViewById(R.id.pythonCard).setOnClickListener(courseClickListener);
        findViewById(R.id.javaCard).setOnClickListener(courseClickListener);
        findViewById(R.id.cppCard).setOnClickListener(courseClickListener);
        findViewById(R.id.sqlCard).setOnClickListener(courseClickListener);
    }

    private void logout() {
        // For now, just show a toast message. You can add your actual logout logic here.
        Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, activity_login.class);
        startActivity(intent);
        finish();
    }
}
