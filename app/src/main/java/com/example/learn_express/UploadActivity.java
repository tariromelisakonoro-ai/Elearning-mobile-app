package com.example.learn_express;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class UploadActivity extends BaseActivity {

    private static final int PICK_FILE_REQUEST = 101;
    private static final long MAX_FILE_SIZE_BYTES = 20 * 1024 * 1024; // 20 MB

    private String uploadType;
    private Spinner spinnerCourses;
    private Spinner spinnerCategory;
    private Spinner spinnerDocType;
    private LinearLayout layoutCourseSelection;
    private LinearLayout layoutCategorySelection;
    private LinearLayout layoutDocTypeSelection;
    private List<String> courseTitles = new ArrayList<>();
    private List<Integer> courseIds  = new ArrayList<>();
    private ArrayAdapter<String> courseAdapter;

    private Uri   selectedFileUri  = null;
    private String selectedFileName = null;
    private long  selectedFileSize  = 0;

    // Views
    private LinearLayout layoutFileDropZone;
    private LinearLayout layoutFileChip;
    private LinearLayout layoutUploadProgress;
    private TextView tvSelectedFileName;
    private TextView tvFileChipName;
    private TextView tvFileChipSize;
    private TextView tvFileChipIcon;
    private TextView btnClearFile;
    private TextView btnSubmit;
    private ProgressBar progressBarUpload;
    private TextView tvUploadStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        setupNavigationBar();

        uploadType = getIntent().getStringExtra("UPLOAD_TYPE");
        if (uploadType == null) uploadType = "course";

        // Title
        TextView tvTitle = findViewById(R.id.tvUploadTitle);
        String label = uploadType.substring(0, 1).toUpperCase() + uploadType.substring(1);
        tvTitle.setText("Publish " + label);

        // Update Extra label based on type
        TextView tvLabelExtra = findViewById(R.id.tvLabelExtra);
        EditText etExtra = findViewById(R.id.etExtra);
        
        layoutCourseSelection   = findViewById(R.id.layoutCourseSelection);
        layoutCategorySelection = findViewById(R.id.layoutCategorySelection);
        layoutDocTypeSelection  = findViewById(R.id.layoutDocTypeSelection);
        
        spinnerCategory = findViewById(R.id.spinnerCategory);
        spinnerDocType  = findViewById(R.id.spinnerDocType);

        // Course spinner setup
        spinnerCourses = findViewById(R.id.spinnerCourses);
        courseAdapter  = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, courseTitles);
        spinnerCourses.setAdapter(courseAdapter);

        // Category spinner setup
        String[] categories = {"Sciences", "Languages", "Mathematics", "Arts", "Technology", "Business", "Other"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(catAdapter);

        // Document Type spinner setup
        String[] docTypes = {"file (Downloadable PDF/PPT)", "text (Written Article)", "video (Lecture Link)", "live_class (Live Session)"};
        ArrayAdapter<String> docTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, docTypes);
        spinnerDocType.setAdapter(docTypeAdapter);

        if (uploadType.equals("course")) {
            layoutCourseSelection.setVisibility(View.GONE);
            layoutCategorySelection.setVisibility(View.VISIBLE);
            tvLabelExtra.setVisibility(View.GONE);
            etExtra.setVisibility(View.GONE);
        } else if (uploadType.equals("assignment")) {
            layoutCourseSelection.setVisibility(View.VISIBLE);
            tvLabelExtra.setText("Submission Deadline");
            etExtra.setHint("e.g. 2026-12-01 12:00:00");
            fetchMyCourses();
        } else if (uploadType.equals("exam")) {
            layoutCourseSelection.setVisibility(View.VISIBLE);
            tvLabelExtra.setText("Assessment Deadline (Optional)");
            etExtra.setHint("e.g. 2026-12-01 14:00:00");
            fetchMyCourses();
        } else {
            // resource
            layoutCourseSelection.setVisibility(View.VISIBLE);
            layoutDocTypeSelection.setVisibility(View.VISIBLE);
            tvLabelExtra.setText("External Link (Optional)");
            etExtra.setHint("e.g. https://youtube.com/...");
            fetchMyCourses();
        }

        // File UI
        layoutFileDropZone    = findViewById(R.id.layoutFileDropZone);
        layoutFileChip        = findViewById(R.id.layoutFileChip);
        layoutUploadProgress  = findViewById(R.id.layoutUploadProgress);
        tvSelectedFileName    = findViewById(R.id.tvSelectedFileName);
        tvFileChipName        = findViewById(R.id.tvFileChipName);
        tvFileChipSize        = findViewById(R.id.tvFileChipSize);
        tvFileChipIcon        = findViewById(R.id.tvFileChipIcon);
        btnClearFile          = findViewById(R.id.btnClearFile);
        progressBarUpload     = findViewById(R.id.progressBarUpload);
        tvUploadStatus        = findViewById(R.id.tvUploadStatus);
        btnSubmit             = findViewById(R.id.btnSubmitUpload);

        layoutFileDropZone.setOnClickListener(v -> openFilePicker());
        btnClearFile.setOnClickListener(v -> clearSelectedFile());
        btnSubmit.setOnClickListener(v -> submitContent());
    }

    // ────────────────────────────────────────────────────────
    //  File Picker
    // ────────────────────────────────────────────────────────

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        // Restrict to allowed MIME groups
        String[] mimeTypes = {
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "text/plain",
            "video/mp4",
            "video/webm",
            "application/zip",
            "image/jpeg",
            "image/png",
            "image/gif"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select a file"), PICK_FILE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.getData();
            resolveFileInfo(selectedFileUri);
        }
    }

    private void resolveFileInfo(Uri uri) {
        selectedFileName = "unknown_file";
        selectedFileSize = 0;

        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            int sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE);
            if (nameIdx >= 0) selectedFileName = cursor.getString(nameIdx);
            if (sizeIdx >= 0) selectedFileSize = cursor.getLong(sizeIdx);
            cursor.close();
        }

        // Enforce size limit
        if (selectedFileSize > MAX_FILE_SIZE_BYTES) {
            Toast.makeText(this, "File too large. Maximum allowed size is 20 MB.", Toast.LENGTH_LONG).show();
            clearSelectedFile();
            return;
        }

        // Update drop-zone label
        tvSelectedFileName.setText(selectedFileName);

        // Show chip
        tvFileChipName.setText(selectedFileName);
        tvFileChipSize.setText(formatFileSize(selectedFileSize));
        tvFileChipIcon.setText(getFileEmoji(selectedFileName));
        layoutFileChip.setVisibility(View.VISIBLE);
    }

    private void clearSelectedFile() {
        selectedFileUri  = null;
        selectedFileName = null;
        selectedFileSize = 0;
        tvSelectedFileName.setText("No file selected");
        layoutFileChip.setVisibility(View.GONE);
    }

    // ────────────────────────────────────────────────────────
    //  Submit
    // ────────────────────────────────────────────────────────

    private void submitContent() {
        EditText etTitle = findViewById(R.id.etTitle);
        EditText etDesc  = findViewById(R.id.etDescription);
        EditText etExtra = findViewById(R.id.etExtra);

        String title = etTitle.getText().toString().trim();
        String desc  = etDesc.getText().toString().trim();
        String extra = etExtra.getText().toString().trim();

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(this, "Title and description are required", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedCourseId = 0;
        if (!uploadType.equals("course")) {
            int pos = spinnerCourses.getSelectedItemPosition();
            if (pos >= 0 && pos < courseIds.size()) {
                selectedCourseId = courseIds.get(pos);
            } else {
                Toast.makeText(this, "You must select a course first", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        SharedPreferences sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String email         = sharedPref.getString("current_user_email", "");
        String currentUserId = sharedPref.getString(email + "_id", "");
        String role          = sharedPref.getString(email + "_role", "");

        // Build form fields
        Map<String, String> fields = new HashMap<>();
        fields.put("user_id", currentUserId);
        fields.put("role", role);
        fields.put("title", title);
        fields.put("description", desc);

        String url;
        if (uploadType.equals("course")) {
            url = Constants.BASE_URL + "add_course.php";
            fields.put("category", spinnerCategory.getSelectedItem().toString());
        } else if (uploadType.equals("assignment")) {
            url = Constants.BASE_URL + "add_assignment.php";
            fields.put("course_id", String.valueOf(selectedCourseId));
            fields.put("due_date", extra);
        } else {
            url = Constants.BASE_URL + "add_material.php";
            fields.put("course_id", String.valueOf(selectedCourseId));
            
            String resolvedType = uploadType;
            if (uploadType.equals("resource")) {
                resolvedType = spinnerDocType.getSelectedItem().toString().split(" ")[0]; // Extract "file", "text", etc.
            }
            fields.put("type", resolvedType);
            
            if (uploadType.equals("exam")) {
                fields.put("due_date", extra);
            } else {
                fields.put("url", extra);
            }
        }

        // Show progress
        setSubmitEnabled(false);
        layoutUploadProgress.setVisibility(View.VISIBLE);
        tvUploadStatus.setText(selectedFileUri != null ? "Uploading file, please wait…" : "Publishing…");

        if (selectedFileUri != null) {
            // Multipart upload via AsyncTask (supports all types now)
            new MultipartUploadTask(url, fields, selectedFileUri, selectedFileName).execute();
        } else {
            // Regular Volley POST (no file)
            submitWithVolley(url, fields);
        }
    }

    private void submitWithVolley(String url, Map<String, String> fields) {
        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    layoutUploadProgress.setVisibility(View.GONE);
                    setSubmitEnabled(true);
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            Toast.makeText(this, "Published successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(this, obj.optString("message", "Failed"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Server response error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    layoutUploadProgress.setVisibility(View.GONE);
                    setSubmitEnabled(true);
                    Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            protected Map<String, String> getParams() {
                return fields;
            }
        };
        Volley.newRequestQueue(this).add(request);
    }

    private void setSubmitEnabled(boolean enabled) {
        btnSubmit.setEnabled(enabled);
        btnSubmit.setAlpha(enabled ? 1f : 0.6f);
        btnSubmit.setText(enabled ? "Publish" : "Please wait…");
    }

    // ────────────────────────────────────────────────────────
    //  Fetch instructor's courses
    // ────────────────────────────────────────────────────────

    private void fetchMyCourses() {
        String url = Constants.BASE_URL + "get_courses.php";
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.getBoolean("success")) {
                            JSONArray coursesArray = obj.getJSONArray("courses");
                            SharedPreferences sp = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                            String em    = sp.getString("current_user_email", "");
                            String myId  = sp.getString(em + "_id", "");

                            for (int i = 0; i < coursesArray.length(); i++) {
                                JSONObject c = coursesArray.getJSONObject(i);
                                if (c.getString("instructor_id").equals(myId)) {
                                    courseIds.add(c.getInt("id"));
                                    courseTitles.add(c.getString("title"));
                                }
                            }
                            courseAdapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {});
        Volley.newRequestQueue(this).add(request);
    }

    // ────────────────────────────────────────────────────────
    //  Multipart Upload AsyncTask
    // ────────────────────────────────────────────────────────

    private class MultipartUploadTask extends AsyncTask<Void, Void, String> {
        private final String            uploadUrl;
        private final Map<String,String> formFields;
        private final Uri               fileUri;
        private final String            fileName;
        private final String            boundary = "----LearnExpressBoundary" + System.currentTimeMillis();

        MultipartUploadTask(String url, Map<String, String> fields, Uri uri, String name) {
            this.uploadUrl  = url;
            this.formFields = fields;
            this.fileUri    = uri;
            this.fileName   = name;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(uploadUrl).openConnection();
                conn.setUseCaches(false);
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                conn.setConnectTimeout(30000);
                conn.setReadTimeout(60000);

                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                // Write text fields
                for (Map.Entry<String, String> entry : formFields.entrySet()) {
                    dos.writeBytes("--" + boundary + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
                    dos.writeBytes(entry.getValue() + "\r\n");
                }

                // Write file field
                String mimeType = getContentResolver().getType(fileUri);
                if (mimeType == null) mimeType = "application/octet-stream";

                dos.writeBytes("--" + boundary + "\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"" + fileName + "\"\r\n");
                dos.writeBytes("Content-Type: " + mimeType + "\r\n\r\n");

                InputStream is = getContentResolver().openInputStream(fileUri);
                if (is != null) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        dos.write(buffer, 0, bytesRead);
                    }
                    is.close();
                }
                dos.writeBytes("\r\n--" + boundary + "--\r\n");
                dos.flush();
                dos.close();

                // Read response
                int responseCode = conn.getResponseCode();
                InputStream responseStream = (responseCode == 200) ? conn.getInputStream() : conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();
                conn.disconnect();
                return sb.toString();

            } catch (Exception e) {
                return "{\"success\":false,\"message\":\"Upload failed: " + e.getMessage() + "\"}";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            layoutUploadProgress.setVisibility(View.GONE);
            setSubmitEnabled(true);
            try {
                JSONObject obj = new JSONObject(result);
                if (obj.getBoolean("success")) {
                    Toast.makeText(UploadActivity.this, "Published with file successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(UploadActivity.this, obj.optString("message", "Upload failed"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(UploadActivity.this, "Server response error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────

    private String formatFileSize(long bytes) {
        if (bytes <= 0) return "";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private String getFileEmoji(String name) {
        if (name == null) return "📄";
        String ext = name.contains(".") ? name.substring(name.lastIndexOf('.') + 1).toLowerCase() : "";
        switch (ext) {
            case "pdf":  return "📕";
            case "doc":
            case "docx": return "📝";
            case "ppt":
            case "pptx": return "📊";
            case "txt":  return "📃";
            case "mp4":
            case "webm": return "🎬";
            case "zip":  return "🗜️";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":  return "🖼️";
            default:     return "📄";
        }
    }
}
