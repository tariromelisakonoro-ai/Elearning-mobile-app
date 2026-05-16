package com.example.learn_express;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Using activity_main as a placeholder

        // Placeholder logic to show these activities are being used
        // This will prevent "unused class" warnings if that was part of the issue
        Intent instructorIntent = new Intent(this, InstructorDashboardActivity.class);
        Intent adminIntent = new Intent(this, AdminDashboardActivity.class);
        Intent parentIntent = new Intent(this, ParentDashboardActivity.class);
        Intent studentIntent = new Intent(this, StudentDashboardActivity.class);
    }
}