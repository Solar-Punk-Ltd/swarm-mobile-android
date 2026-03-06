package com.swarm.mobile.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.swarm.mobile.UploadHistoryRecord;

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


    public void saveUploadHistory(List<UploadHistoryRecord> uploadHistory) {
        try {
            JSONArray jsonArray = new JSONArray();

            for (UploadHistoryRecord record : uploadHistory) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("filename", record.filename());
                jsonObject.put("hash", record.hash());
                jsonObject.put("downloadDate", record.downloadDate());
                jsonObject.put("stampId", record.stampId());
                jsonObject.put("stampLabel", record.stampLabel());
                jsonObject.put("transferRateMBps", record.transferRateMBps() != null ? record.transferRateMBps() : "");
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


    public List<UploadHistoryRecord> loadUploadHistory() {
        List<UploadHistoryRecord> uploadHistory = new ArrayList<>();

        try {
            String jsonString = preferences.getString(KEY_UPLOAD_RECORDS, null);

            if (jsonString != null) {
                JSONArray jsonArray = new JSONArray(jsonString);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String filename = jsonObject.getString("filename");
                    String hash = jsonObject.getString("hash");
                    long downloadDate = jsonObject.getLong("downloadDate");
                    String stampId = jsonObject.optString("stampId", "");
                    String stampLabel = jsonObject.optString("stampLabel", "");
                    String transferRateMBps = jsonObject.optString("transferRateMBps", "");

                    uploadHistory.add(new UploadHistoryRecord(filename, hash, downloadDate, stampId, stampLabel, transferRateMBps));
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

}
