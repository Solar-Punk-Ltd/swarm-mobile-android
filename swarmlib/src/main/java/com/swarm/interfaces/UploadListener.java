package com.swarm.interfaces;

public interface UploadListener {
    void onUploadSuccessful(String hash, String uploadRateMBps);

    void onUploadFailed(String error);
}
