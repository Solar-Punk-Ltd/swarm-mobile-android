package com.swarm.mobile;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class DownloadHistoryRecord {
    private final String filename;
    private final String hash;
    private final long downloadDate;
    private final String transferRateMBps;

    public DownloadHistoryRecord(String filename, String hash, long downloadDate, String transferRateMBps) {
        this.filename = filename;
        this.hash = hash;
        this.downloadDate = downloadDate;
        this.transferRateMBps = transferRateMBps;
    }

    public String filename() {
        return filename;
    }

    public String hash() {
        return hash;
    }

    public long downloadDate() {
        return downloadDate;
    }

    public String transferRateMBps() {
        return transferRateMBps;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US);
        return sdf.format(new Date(downloadDate));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DownloadHistoryRecord that = (DownloadHistoryRecord) o;
        return downloadDate == that.downloadDate &&
                Objects.equals(filename, that.filename) &&
                Objects.equals(hash, that.hash) &&
                Objects.equals(transferRateMBps, that.transferRateMBps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filename, hash, downloadDate, transferRateMBps);
    }

    @NonNull
    @Override
    public String toString() {
        return "DownloadHistoryRecord[filename=" + filename + ", hash=" + hash +
                ", downloadDate=" + downloadDate + ", transferRateMBps=" + transferRateMBps + "]";
    }
}
