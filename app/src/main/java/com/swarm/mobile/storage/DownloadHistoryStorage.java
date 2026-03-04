package com.swarm.mobile.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.swarm.mobile.HistoryRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public final class DownloadHistoryStorage {

    private static final String PREFS_NAME = "SwarmDownloadHistory";
    private static final String KEY_DOWNLOAD_RECORDS = "download_records";
    private static final String TAG = "DownloadHistoryStorage";

    private final SharedPreferences preferences;

    public DownloadHistoryStorage(Context context) {
        this.preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveDownloadHistory(List<HistoryRecord> downloadHistory) {
        try {
            JSONArray jsonArray = getJsonArray(downloadHistory);

            preferences.edit()
                    .putString(KEY_DOWNLOAD_RECORDS, jsonArray.toString())
                    .apply();

            Log.d(TAG, "Saved " + downloadHistory.size() + " download records");
        } catch (JSONException e) {
            Log.e(TAG, "Error saving download history", e);
        }
    }

    @NonNull
    private static JSONArray getJsonArray(List<HistoryRecord> downloadHistory) throws JSONException {
        JSONArray jsonArray = new JSONArray();

        for (HistoryRecord record : downloadHistory) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("filename", record.filename());
            jsonObject.put("hash", record.hash());
            jsonObject.put("uploadDate", record.uploadDate());
            jsonObject.put("stampId", record.stampId());
            jsonObject.put("stampLabel", record.stampLabel());
            jsonObject.put("transferRateMBps", record.transferRateMBps() != null ? record.transferRateMBps() : "");
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public List<HistoryRecord> loadDownloadHistory() {
        List<HistoryRecord> downloadHistory = new ArrayList<>();

        try {
            String jsonString = preferences.getString(KEY_DOWNLOAD_RECORDS, null);

            if (jsonString != null) {
                JSONArray jsonArray = new JSONArray(jsonString);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    String filename = jsonObject.getString("filename");
                    String hash = jsonObject.getString("hash");
                    long uploadDate = jsonObject.getLong("uploadDate");
                    String stampId = jsonObject.optString("stampId", "");
                    String stampLabel = jsonObject.optString("stampLabel", "");
                    String transferRateMBps = jsonObject.optString("transferRateMBps", "");

                    HistoryRecord record = new HistoryRecord(filename, hash, uploadDate, stampId, stampLabel, transferRateMBps);
                    downloadHistory.add(record);
                }

                Log.d(TAG, "Loaded " + downloadHistory.size() + " download records");
            } else {
                Log.d(TAG, "No saved download history found");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error loading download history", e);
        }

        return downloadHistory;
    }

    public void clearDownloadHistory() {
        preferences.edit()
                .remove(KEY_DOWNLOAD_RECORDS)
                .apply();
        Log.d(TAG, "Cleared download history");
    }
}
