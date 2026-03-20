package com.swarm.mobile;

public record Stamp(String label,
                    String batchID,
                    String amount,
                    byte depth,
                    byte bucketDepth,
                    boolean immutable) {

}
