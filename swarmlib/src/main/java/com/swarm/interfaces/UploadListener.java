package com.swarm.interfaces;

public interface UploadListener {
    void onUploadSuccessful(String hash);

    void onUploadFailed(String error);
}
