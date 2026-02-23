package com.swarm.lib;

public record NodeInfo(String walletAddress,
                       String chequebookAddress,
                       String chequebookBalance,
                       NodeStatus status) {
}
