package com.swarm.mobile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public record HistoryRecord(String filename, String hash, long uploadDate, String stampId,
                            String stampLabel, String transferRateMBps) {

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);
        return sdf.format(new Date(uploadDate));
    }
}
