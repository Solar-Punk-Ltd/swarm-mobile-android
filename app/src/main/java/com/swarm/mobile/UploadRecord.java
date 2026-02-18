package com.swarm.mobile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public record UploadRecord(String filename, String hash, long uploadDate, String stampId,
                           String stampLabel) {

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);
        return sdf.format(new Date(uploadDate));
    }
}
