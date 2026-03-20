package com.swarm.interfaces;

import com.swarm.mobile.Stamp;

import java.util.List;

public interface StampListener {

    void stampsReceived(List<Stamp> stamps);

    void stampCreated(String hash);
}
