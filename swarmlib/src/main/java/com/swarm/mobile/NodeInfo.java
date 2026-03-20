package com.swarm.mobile;

public record NodeInfo(String walletAddress,
                       String chequebookAddress,
                       String chequebookBalance,
                       NodeStatus status) {
}
