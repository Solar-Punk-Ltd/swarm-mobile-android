package com.swarm.lib;

public record Stamp(String label,
                    byte[] batchID,
                    String amount,
                    byte depth,
                    byte bucketDepth,
                    boolean immutable) {

}
