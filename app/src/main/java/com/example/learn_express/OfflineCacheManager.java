package com.example.learn_express;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Manages local caching of API responses using SharedPreferences.
 * This enables the app to display previously-fetched data when offline.
 */
public class OfflineCacheManager {

    private static final String PREF_NAME = "OfflineCache";

    // Cache keys
    public static final String KEY_COURSES       = "cache_courses";
    public static final String KEY_ENROLLMENTS   = "cache_enrollments";
    public static final String KEY_ASSIGNMENTS   = "cache_assignments";
    public static final String KEY_EXAMS         = "cache_exams";
    public static final String KEY_RESOURCES     = "cache_resources";
    public static final String KEY_MATERIALS     = "cache_materials";
    public static final String KEY_STUDENT_DASH  = "cache_student_dashboard";
    public static final String KEY_INSTRUCTOR_DASH = "cache_instructor_dashboard";
    public static final String KEY_ADMIN_DASH    = "cache_admin_dashboard";
    public static final String KEY_PARENT_DASH   = "cache_parent_dashboard";

    // Timestamp keys (for cache freshness display)
    private static final String SUFFIX_TIMESTAMP = "_timestamp";

    private final SharedPreferences prefs;

    public OfflineCacheManager(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Save a JSON string to the cache under the given key. */
    public void save(String key, String jsonData) {
        prefs.edit()
             .putString(key, jsonData)
             .putLong(key + SUFFIX_TIMESTAMP, System.currentTimeMillis())
             .apply();
    }

    /** Load a cached JSON string. Returns null if nothing is cached. */
    public String load(String key) {
        return prefs.getString(key, null);
    }

    /** Returns true if there is cached data for this key. */
    public boolean has(String key) {
        return prefs.contains(key) && prefs.getString(key, null) != null;
    }

    /** Returns a human-readable "last updated" string, or null if never cached. */
    public String getLastUpdated(String key) {
        long ts = prefs.getLong(key + SUFFIX_TIMESTAMP, -1);
        if (ts < 0) return null;
        long diff = System.currentTimeMillis() - ts;
        if (diff < 60_000)          return "just now";
        if (diff < 3_600_000)       return (diff / 60_000) + " min ago";
        if (diff < 86_400_000)      return (diff / 3_600_000) + " hr ago";
        return (diff / 86_400_000) + " day(s) ago";
    }

    /** Clear all cached data (e.g. on logout). */
    public void clearAll() {
        prefs.edit().clear().apply();
    }

    /** Clear a single cache entry. */
    public void clear(String key) {
        prefs.edit()
             .remove(key)
             .remove(key + SUFFIX_TIMESTAMP)
             .apply();
    }
}
