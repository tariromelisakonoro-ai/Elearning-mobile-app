package com.example.mypagelogin;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class activity_register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        final TextInputEditText emailInput = findViewById(R.id.emailInput);
        final TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        final TextInputEditText confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        Button registerButton = findViewById(R.id.registerButton);
        TextView loginLink = findViewById(R.id.loginLink);
        TextView backButton = findViewById(R.id.backButton);

        // Set click listeners
        registerButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            // Validation
            if (email.isEmpty()) {
                emailInput.setError("Email is required");
                emailInput.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.setError("Enter a valid email");
                emailInput.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                passwordInput.setError("Password is required");
                passwordInput.requestFocus();
                return;
            }

            if (password.length() < 6) {
                passwordInput.setError("Password must be at least 6 characters");
                passwordInput.requestFocus();
                return;
            }

            if (confirmPassword.isEmpty()) {
                confirmPasswordInput.setError("Please confirm your password");
                confirmPasswordInput.requestFocus();
                return;
            }

            if (!password.equals(confirmPassword)) {
                confirmPasswordInput.setError("Passwords do not match");
                confirmPasswordInput.requestFocus();
                return;
            }

            // For demo purposes - you should replace this with actual registration logic
            Toast.makeText(activity_register.this, "Registration Successful!", Toast.LENGTH_SHORT).show();

            // Navigate to Login screen after successful registration
            Intent intent = new Intent(activity_register.this, activity_login.class);
            startActivity(intent);
            finish();
        });

        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(activity_register.this, activity_login.class);
            startActivity(intent);
            finish();
        });

        backButton.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
    }
}
