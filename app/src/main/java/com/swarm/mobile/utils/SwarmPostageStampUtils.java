package com.swarm.mobile.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;

public class SwarmPostageStampUtils {

    private static final long CHUNK_SIZE_BYTES = 4096; // 4 KB
    /** Fixed bucket depth for Swarm batches — matches bee node internals */
    @SuppressWarnings("unused")
    public static final int BUCKET_DEPTH = 16;
    /** Gnosis Chain average block time in seconds */
    public static final int BLOCK_TIME_SECONDS = 5;

    /**
     * Empirical utilization rates for different batch depths.
     * A batch fills when the first of the 2^BUCKET_DEPTH (65 536) buckets overflows.
     * Derived from the bee-js reference implementation:
     * <a href="https://github.com/ethersphere/bee-js/blob/master/src/utils/stamps.ts">...</a>
     */
    private static final Map<Integer, Double> UTILIZATION_RATES_WITHOUT_REDUNDANCY = Map.ofEntries(
            Map.entry(17, 0.0001), Map.entry(18, 0.0061), Map.entry(19, 0.0509),
            Map.entry(20, 0.1565), Map.entry(21, 0.3027), Map.entry(22, 0.4499),
            Map.entry(23, 0.5803), Map.entry(24, 0.6848), Map.entry(25, 0.7677),
            Map.entry(26, 0.8294), Map.entry(27, 0.8671), Map.entry(28, 0.8837),
            Map.entry(29, 0.9288), Map.entry(30, 0.9481), Map.entry(31, 0.9606),
            Map.entry(32, 0.9701), Map.entry(33, 0.9765)
    );

    /**
     * Calculates the maximum theoretical capacity of a batch.
     * Formula: 2^depth × CHUNK_SIZE (4 096 bytes)
     * Matches bee-js: capacity = 2^depth * 4096
     */
    public static long calculateTheoreticalCapacity(int depth) {
        return (1L << depth) * CHUNK_SIZE_BYTES;
    }

    /**
     * Calculates the effective (usable) capacity before the first bucket overflows.
     * Uses empirically measured utilization rates per depth.
     */
    public static long calculateEffectiveCapacity(int depth) {
        if (!UTILIZATION_RATES_WITHOUT_REDUNDANCY.containsKey(depth)) {
            throw new RuntimeException("Key depth " + depth + " not found in UTILIZATION_RATES map. Please update the map with the appropriate utilization rate for this depth.");
        }

        Double rateBoxed = UTILIZATION_RATES_WITHOUT_REDUNDANCY.get(depth);
        double rate = (rateBoxed != null) ? rateBoxed: 0.0;

        return (long) (calculateTheoreticalCapacity(depth) * rate);
    }

    /**
     * Calculates TTL (Time to Live) in seconds.
     * Formula: (amount / pricePerBlock) × BLOCK_TIME_SECONDS
     * Matches bee-js: ttl = (amount / pricePerBlock) * blockTime
     *
     * @param amount        The batch amount in PLUR (e.g. 2 000 000 000)
     * @param pricePerBlock Current network storage price per block in PLUR (e.g. 62 681)
     */
    public static long calculateTTLSeconds(long amount, long pricePerBlock) {
        if (pricePerBlock <= 0) {
            return 0;
        }
        return (amount / pricePerBlock) * BLOCK_TIME_SECONDS;
    }

    /**
     * Formats a byte count into a human-readable size string.
     */
    public static String formatSize(long bytes) {
        if (bytes >= 1_000_000_000L) return String.format(Locale.US, "%.2f GB", bytes / 1e9);
        if (bytes >= 1_000_000L)     return String.format(Locale.US, "%.2f MB", bytes / 1e6);
        if (bytes >= 1_000L)         return String.format(Locale.US, "%.2f KB", bytes / 1e3);
        return bytes + " B";
    }

    /**
     * Returns a single-line capacity summary for display in the stamp creation dialog.
     * E.g. "Effective: ~628.91 MB\nTheoretical: ~4.29 GB"
     */
    public static String formatCapacitySummary(int depth) {
        long theoretical = calculateTheoreticalCapacity(depth);
        long effective   = calculateEffectiveCapacity(depth);
        return String.format(Locale.US,
                "Effective: ~%s\nTheoretical: ~%s",
                formatSize(effective), formatSize(theoretical));
    }

    /**
     * Reverse of calculateTTLSeconds — computes the amount needed for a given TTL.
     * Formula: (ttlSeconds / BLOCK_TIME_SECONDS) * pricePerBlock
     *
     * @param ttlSeconds    Desired TTL in seconds
     * @param pricePerBlock Current network price per block in PLUR
     */
    public static long calculateAmountFromTTL(long ttlSeconds, long pricePerBlock) {
        if (ttlSeconds <= 0 || pricePerBlock <= 0) {
            return 0;
        }
        long blocks = ttlSeconds / BLOCK_TIME_SECONDS;
        return blocks * pricePerBlock;
    }

    /**
     * Calculates the indicative price in xBZZ.
     * Formula: amount × 2^depth / 10^16
     * (1 xBZZ = 10^16 PLUR)
     *
     * @param amount The batch amount in PLUR
     * @param depth  The batch depth
     */
    public static BigDecimal calculateIndicativePriceXBZZ(long amount, int depth) {
        BigDecimal amountBD  = BigDecimal.valueOf(amount);
        BigDecimal chunks    = BigDecimal.valueOf(1L << depth);
        return amountBD.multiply(chunks).divide(NumberUtils.PLUR_PER_XBZZ, 4, RoundingMode.HALF_UP);
    }

    /**
     * Returns a formatted indicative price string, e.g. "0.5941 xBZZ"
     */
    public static String formatIndicativePrice(long amount, int depth) {
        if (amount <= 0 || depth <= 0) {
            return "— xBZZ";
        }
        BigDecimal price = calculateIndicativePriceXBZZ(amount, depth);
        return String.format(Locale.US, "%s xBZZ", price.toPlainString());
    }
}

