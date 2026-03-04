package com.swarm.mobile;

import androidx.annotation.NonNull;

import java.util.Objects;

public final class UploadDownloadHistoryRecord extends DownloadHistoryRecord {
    private final String stampId;
    private final String stampLabel;

    public UploadDownloadHistoryRecord(String filename, String hash, long uploadDate, String stampId, String stampLabel, String transferRateMBps) {
        super(filename, hash, uploadDate, transferRateMBps);
        this.stampId = stampId;
        this.stampLabel = stampLabel;
    }

    public String stampId() { return stampId; }
    public String stampLabel() { return stampLabel; }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        if (!(o instanceof UploadDownloadHistoryRecord other)) return false;
        return Objects.equals(stampId, other.stampId) &&
               Objects.equals(stampLabel, other.stampLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), stampId, stampLabel);
    }

    @NonNull
    @Override
    public String toString() {
        return "UploadDownloadHistoryRecord[filename=" + filename() + ", hash=" + hash() +
               ", uploadDate=" + uploadDate() + ", stampId=" + stampId +
               ", stampLabel=" + stampLabel + ", transferRateMBps=" + transferRateMBps() + "]";
    }
}

