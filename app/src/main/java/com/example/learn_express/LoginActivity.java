package com.example.learn_express;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin;
    private View tvOfflineHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        EditText etEmail    = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        btnLogin            = findViewById(R.id.btnLogin);
        tvOfflineHint       = findViewById(R.id.tvOfflineHint);

        // Show offline hint if there's no connection
        updateOfflineHint();

        TextView tvGoToSignup = findViewById(R.id.tvGoToSignup);
        tvGoToSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v ->
                    Toast.makeText(this, "Forgot password functionality coming soon!", Toast.LENGTH_SHORT).show());
        }

        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim().toLowerCase();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show();
                return;
            }

            if (NetworkUtils.isOnline(this)) {
                loginUser(email, password);
            } else {
                // --- Offline login: validate against cached credentials ---
                tryOfflineLogin(email, password);
            }
        });

        findViewById(R.id.btnGoogle).setOnClickListener(v -> handleSocialLogin("google"));
        findViewById(R.id.btnGithub).setOnClickListener(v -> handleSocialLogin("github"));
    }

    /** Shows or hides the "You are offline — use cached credentials" hint */
    private void updateOfflineHint() {
        if (tvOfflineHint == null) return;
        boolean online = NetworkUtils.isOnline(this);
        tvOfflineHint.setVisibility(online ? View.GONE : View.VISIBLE);
    }

    // -----------------------------------------------------------------------
    // Offline login
    // -----------------------------------------------------------------------

    private void tryOfflineLogin(String email, String password) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String cachedHash = prefs.getString(email + "_pass_hash", null);

        if (cachedHash == null) {
            Toast.makeText(this,
                    "No cached session for this account. Connect to the internet to log in for the first time.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Compare against stored hash
        if (cachedHash.equals(hashPassword(password))) {
            String name   = prefs.getString(email + "_name", "User");
            String role   = prefs.getString(email + "_role", "Student");
            String userId = prefs.getString(email + "_id",   "");

            // Restore session
            prefs.edit().putString("current_user_email", email).apply();

            Toast.makeText(this, "Logged in offline as " + name, Toast.LENGTH_SHORT).show();
            navigateToDashboard(role);
        } else {
            Toast.makeText(this, "Incorrect password for offline login.", Toast.LENGTH_SHORT).show();
        }
    }

    /** Simple deterministic hash for local credential storage. */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return password; // fallback (should never happen)
        }
    }

    // -----------------------------------------------------------------------
    // Social login
    // -----------------------------------------------------------------------

    private void handleSocialLogin(String provider) {
        if (!NetworkUtils.isOnline(this)) {
            Toast.makeText(this, "Social login requires an internet connection.", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = Constants.BASE_URL + "social_auth.php";
        Toast.makeText(this, "Authenticating with " + provider + "...", Toast.LENGTH_SHORT).show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getBoolean("success")) {
                            JSONObject userObj = jsonObject.getJSONObject("user");
                            String name   = userObj.getString("fullname");
                            String email  = userObj.getString("email");
                            String role   = userObj.getString("role");
                            String userId = userObj.getString("id");

                            saveSession(email, name, role, userId, null);
                            navigateToDashboard(role);
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
                } catch (Exception e) { return null; }
            }
            @Override
            public String getBodyContentType() { return "application/json; charset=utf-8"; }
        };
        Volley.newRequestQueue(this).add(stringRequest);
    }

    // -----------------------------------------------------------------------
    // Online login
    // -----------------------------------------------------------------------

    private void loginUser(String email, String password) {
        btnLogin.setEnabled(false);
        btnLogin.setText("Connecting...");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Constants.LOGIN_URL,
                response -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Sign In");
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = false;
                        if (jsonObject.has("success"))      success = jsonObject.getBoolean("success");
                        else if (jsonObject.has("status"))  success = jsonObject.getString("status").equals("success");

                        if (success) {
                            JSONObject userObj = jsonObject.has("user") ? jsonObject.getJSONObject("user") : jsonObject;
                            String name   = userObj.getString("fullname");
                            String role   = userObj.getString("role");
                            String userId = userObj.has("id") ? userObj.getString("id") : "";

                            // Save session + hashed password for future offline login
                            saveSession(email, name, role, userId, password);
                            navigateToDashboard(role);
                        } else {
                            String message = jsonObject.has("message") ? jsonObject.getString("message") : "Login failed";
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Log.e("LoginError", "JSON Parse error: " + response);
                        Toast.makeText(LoginActivity.this, "Server error. Check PHP response.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Sign In");
                    String msg = error.getMessage();
                    if (msg == null) msg = "Timeout. Check IP and Firewall.";
                    Toast.makeText(LoginActivity.this, "Network Error: " + msg, Toast.LENGTH_LONG).show();
                    Log.e("LoginError", "Volley Error: ", error);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email",    email);
                params.put("password", password);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                10000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Persists user session and, optionally, a hashed password for offline login. */
    private void saveSession(String email, String name, String role, String userId, String rawPassword) {
        SharedPreferences.Editor editor = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE).edit();
        editor.putString("current_user_email", email);
        editor.putString(email + "_name",  name);
        editor.putString(email + "_role",  role);
        editor.putString(email + "_id",    userId);
        if (rawPassword != null) {
            editor.putString(email + "_pass_hash", hashPassword(rawPassword));
        }
        editor.apply();
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        if      (role.equalsIgnoreCase("Administrator")) intent = new Intent(this, AdminDashboardActivity.class);
        else if (role.equalsIgnoreCase("Instructor"))    intent = new Intent(this, InstructorDashboardActivity.class);
        else if (role.equalsIgnoreCase("Parent"))        intent = new Intent(this, ParentDashboardActivity.class);
        else                                             intent = new Intent(this, StudentDashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
