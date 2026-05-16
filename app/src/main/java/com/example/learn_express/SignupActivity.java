package com.example.learn_express;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText etName = findViewById(R.id.etName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        EditText etConfirmPassword = findViewById(R.id.etConfirmPassword);
        AutoCompleteTextView spinnerRole = findViewById(R.id.spinnerRole);
        TextInputLayout roleInputLayout = findViewById(R.id.roleInputLayout);

        TextView tvGoToLogin = findViewById(R.id.tvGoToLogin);
        tvGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        String[] roles = getResources().getStringArray(R.array.roles_array);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, roles);
        spinnerRole.setAdapter(adapter);

        spinnerRole.setOnClickListener(v -> spinnerRole.showDropDown());
        roleInputLayout.setEndIconOnClickListener(v -> spinnerRole.showDropDown());

        Button btnSignup = findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim().toLowerCase();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            String role = spinnerRole.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty() || role.equals("Choose a role")) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            registerUser(name, email, password, role);
        });

        findViewById(R.id.btnGoogle).setOnClickListener(v -> handleSocialLogin("google"));
        findViewById(R.id.btnGithub).setOnClickListener(v -> handleSocialLogin("github"));
    }

    private void handleSocialLogin(String provider) {
        String url = Constants.BASE_URL + "social_auth.php";
        
        Toast.makeText(this, "Authenticating with " + provider + "...", Toast.LENGTH_SHORT).show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONObject userObj = jsonObject.getJSONObject("user");
                            String name = userObj.getString("fullname");
                            String email = userObj.getString("email");
                            String role = userObj.getString("role");
                            String userId = userObj.getString("id");

                            android.content.SharedPreferences sharedPref = getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
                            android.content.SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("current_user_email", email);
                            editor.putString(email + "_name", name);
                            editor.putString(email + "_role", role);
                            editor.putString(email + "_id", userId);
                            editor.apply();

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
                            finish();
                        } else {
                            Toast.makeText(this, "Social Auth Failed: " + jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()) {
            @Override
            public byte[] getBody() {
                try {
                    JSONObject body = new JSONObject();
                    body.put("provider", provider);
                    return body.toString().getBytes("utf-8");
                } catch (Exception e) {
                    return null;
                }
            }
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void registerUser(String name, String email, String password, String role) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.SIGNUP_URL,
                response -> {
                    Log.d("SignupResponse", "Response: " + response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = false;
                        if (jsonObject.has("success")) {
                            success = jsonObject.getBoolean("success");
                        } else if (jsonObject.has("status")) {
                            success = jsonObject.getString("status").equals("success");
                        }

                        if (success) {
                            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, LoginActivity.class));
                            finish();
                        } else {
                            String message = jsonObject.has("message") ? jsonObject.getString("message") : "Registration failed";
                            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("SignupError", "JSON Error: " + response);
                        Toast.makeText(this, "Server Response Error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    String errorMsg = error.getMessage();
                    if (errorMsg == null) errorMsg = "Check your XAMPP server/IP address";
                    Log.e("SignupError", "Volley Error: " + errorMsg);
                    Toast.makeText(this, "Network Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                // Changed "name" to "fullname" to match signup.php
                params.put("fullname", name);
                params.put("email", email);
                params.put("password", password);
                params.put("role", role);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        Volley.newRequestQueue(this).add(stringRequest);
    }
}
