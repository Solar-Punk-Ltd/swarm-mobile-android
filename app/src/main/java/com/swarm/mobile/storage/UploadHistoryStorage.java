package com.swarm.mobile.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.swarm.mobile.UploadRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


@SuppressWarnings("ClassCanBeRecord")
public final class UploadHistoryStorage {

    private static final String PREFS_NAME = "SwarmUploadHistory";
    private static final String KEY_UPLOAD_RECORDS = "upload_records";
    private static final String TAG = "UploadHistoryStorage";

    private final SharedPreferences preferences;

    public UploadHistoryStorage(Context context) {
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }


    public void saveUploadHistory(List<UploadRecord> uploadHistory) {
        try {
            JSONArray jsonArray = new JSONArray();

            for (UploadRecord record : uploadHistory) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("filename", record.filename());
                jsonObject.put("hash", record.hash());
                jsonObject.put("uploadDate", record.uploadDate());
                jsonObject.put("stampId", record.stampId());
                jsonObject.put("stampLabel", record.stampLabel());
                jsonArray.put(jsonObject);
            }

            preferences.edit()
                    .putString(KEY_UPLOAD_RECORDS, jsonArray.toString())
                    .apply();

            Log.d(TAG, "Saved " + uploadHistory.size() + " upload records");
        } catch (JSONException e) {
            Log.e(TAG, "Error saving upload history", e);
        }
    }


    public List<UploadRecord> loadUploadHistory() {
        List<UploadRecord> uploadHistory = new ArrayList<>();

        try {
            String jsonString = preferences.getString(KEY_UPLOAD_RECORDS, null);

            if (jsonString != null) {
                JSONArray jsonArray = new JSONArray(jsonString);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String filename = jsonObject.getString("filename");
                    String hash = jsonObject.getString("hash");
                    long uploadDate = jsonObject.getLong("uploadDate");
                    String stampId = jsonObject.getString("stampId");
                    String stampLabel = jsonObject.getString("stampLabel");

                    UploadRecord record = new UploadRecord(filename, hash, uploadDate, stampId, stampLabel);
                    uploadHistory.add(record);
                }

                Log.d(TAG, "Loaded " + uploadHistory.size() + " upload records");
            } else {
                Log.d(TAG, "No saved upload history found");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error loading upload history", e);
        }

        return uploadHistory;
    }


    public void clearUploadHistory() {
        preferences.edit()
                .remove(KEY_UPLOAD_RECORDS)
                .apply();
        Log.d(TAG, "Cleared upload history");
    }

    public void addUploadRecord(UploadRecord record) {
        List<UploadRecord> history = loadUploadHistory();
        history.add(0, record); // Add to beginning
        saveUploadHistory(history);
    }
}
