package com.swarm.interfaces;

import com.swarm.lib.Stamp;

import java.util.List;

public interface StampListener {

    void stampsReceived(List<Stamp> stamps);

    void stampCreated(String hash);
}
