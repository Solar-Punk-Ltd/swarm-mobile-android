package com.swarm.mobile;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DownloadHistoryRecord {
    private final String filename;
    private final String hash;
    private final long uploadDate;
    private final String transferRateMBps;

    public DownloadHistoryRecord(String filename, String hash, long uploadDate, String transferRateMBps) {
        this.filename = filename;
        this.hash = hash;
        this.uploadDate = uploadDate;
        this.transferRateMBps = transferRateMBps;
    }

    public String filename() { return filename; }
    public String hash() { return hash; }
    public long uploadDate() { return uploadDate; }
    public String transferRateMBps() { return transferRateMBps; }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);
        return sdf.format(new Date(uploadDate));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DownloadHistoryRecord other)) return false;
        return uploadDate == other.uploadDate &&
               Objects.equals(filename, other.filename) &&
               Objects.equals(hash, other.hash) &&
               Objects.equals(transferRateMBps, other.transferRateMBps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, hash, uploadDate, transferRateMBps);
    }

    @NonNull
    @Override
    public String toString() {
        return "DownloadHistoryRecord[filename=" + filename + ", hash=" + hash +
               ", uploadDate=" + uploadDate + ", transferRateMBps=" + transferRateMBps + "]";
    }
}
