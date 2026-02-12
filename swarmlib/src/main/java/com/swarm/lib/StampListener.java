package com.swarm.lib;

import java.util.List;

public interface StampListener {

    void stampsReceived(List<Stamp> stamps);

    void stampCreated(String hash);
}
