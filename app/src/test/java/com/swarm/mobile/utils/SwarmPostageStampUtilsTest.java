package com.swarm.mobile.utils;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Reference values verified against bee-js:
 *   <a href="https://github.com/ethersphere/bee-js/blob/master/src/utils/stamps.ts">...</a>
 * Key formulas:
 *   theoreticalCapacity = 2^depth × 4096
 *   effectiveCapacity   = theoreticalCapacity × utilizationRate[depth]
 *   ttlSeconds          = (amount / pricePerBlock) × 5
 */
public class SwarmPostageStampUtilsTest {

    // ────────────────────────────────────────────────────────────────────────
    // calculateTheoreticalCapacity
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void theoreticalCapacity_depth17() {
        // 2^17 × 4096 = 131072 × 4096 = 536 870 912 bytes ≈ 536.87 MB
        assertEquals(536_870_912L, SwarmPostageStampUtils.calculateTheoreticalCapacity(17));
    }

    @Test
    public void theoreticalCapacity_depth20() {
        // 2^20 × 4096 = 1 048 576 × 4096 = 4 294 967 296 bytes ≈ 4.29 GB
        assertEquals(4_294_967_296L, SwarmPostageStampUtils.calculateTheoreticalCapacity(20));
    }

    @Test
    public void theoreticalCapacity_depth25() {
        // 2^25 × 4096 = 33 554 432 × 4096 = 137 438 953 472 bytes ≈ 137.44 GB
        assertEquals(137_438_953_472L, SwarmPostageStampUtils.calculateTheoreticalCapacity(25));
    }

    @Test
    public void theoreticalCapacity_isAlwaysPositive() {
        for (int d = 17; d <= 33; d++) {
            assertTrue("depth " + d + " should be positive",
                    SwarmPostageStampUtils.calculateTheoreticalCapacity(d) > 0);
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // calculateEffectiveCapacity
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void effectiveCapacity_depth20() {
        // theoretical = 4 294 967 296 × 0.1565 ≈ 672 162 781 bytes ≈ 628.91 MB
        long effective = SwarmPostageStampUtils.calculateEffectiveCapacity(20);
        // Allow small rounding difference
        long expected = (long) (4_294_967_296L * 0.1565);
        assertEquals(expected, effective);
    }

    @Test
    public void effectiveCapacity_depth24() {
        // theoretical = 2^24 × 4096 = 68 719 476 736; rate = 0.6848
        long effective = SwarmPostageStampUtils.calculateEffectiveCapacity(24);
        long expected = (long) (68_719_476_736L * 0.6848);
        assertEquals(expected, effective);
    }

    @Test
    public void effectiveCapacity_isLessThanOrEqualToTheoretical() {
        for (int d = 17; d <= 33; d++) {
            assertTrue("depth " + d + " effective should be ≤ theoretical",
                    SwarmPostageStampUtils.calculateEffectiveCapacity(d)
                            <= SwarmPostageStampUtils.calculateTheoreticalCapacity(d));
        }
    }

    @Test
    public void effectiveCapacity_growsWithDepth() {
        for (int d = 17; d < 33; d++) {
            assertTrue("effective capacity should grow as depth increases",
                    SwarmPostageStampUtils.calculateEffectiveCapacity(d + 1)
                            > SwarmPostageStampUtils.calculateEffectiveCapacity(d));
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // calculateTTLSeconds
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void ttlSeconds_mainnetExample() {
        // amount=5 666 666 666, price=62 681 → blocks=90 405 → ttl=452 025 s ≈ 5.23 days
        long ttl = SwarmPostageStampUtils.calculateTTLSeconds(5_666_666_666L, 62_681L);
        long expectedBlocks = 5_666_666_666L / 62_681L; // 90405
        assertEquals(expectedBlocks * 5, ttl);
    }

    @Test
    public void ttlSeconds_zeroPriceReturnsZero() {
        assertEquals(0L, SwarmPostageStampUtils.calculateTTLSeconds(1_000_000L, 0));
    }

    @Test
    public void ttlSeconds_negativePriceReturnsZero() {
        assertEquals(0L, SwarmPostageStampUtils.calculateTTLSeconds(1_000_000L, -1));
    }

    @Test
    public void ttlSeconds_zeroAmountIsZero() {
        assertEquals(0L, SwarmPostageStampUtils.calculateTTLSeconds(0, 62_681L));
    }

    @Test
    public void ttlSeconds_blockTimeIsApplied() {
        // 100 000 / 10 = 10 000 blocks × 5 s = 50 000 s
        long ttl = SwarmPostageStampUtils.calculateTTLSeconds(100_000L, 10L);
        assertEquals(50_000L, ttl);
    }

    // ────────────────────────────────────────────────────────────────────────
    // formatSize
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void formatSize_bytes() {
        assertEquals("500 B", SwarmPostageStampUtils.formatSize(500));
    }

    @Test
    public void formatSize_kilobytes() {
        assertEquals("1.50 KB", SwarmPostageStampUtils.formatSize(1_500));
    }

    @Test
    public void formatSize_megabytes() {
        assertEquals("1.50 MB", SwarmPostageStampUtils.formatSize(1_500_000));
    }

    @Test
    public void formatSize_gigabytes() {
        assertEquals("4.29 GB", SwarmPostageStampUtils.formatSize(4_294_967_296L));
    }

    // ────────────────────────────────────────────────────────────────────────
    // formatCapacitySummary
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void formatCapacitySummary_depth20ContainsGbAndMb() {
        String summary = SwarmPostageStampUtils.formatCapacitySummary(20);
        assertTrue("should contain 'Theoretical'", summary.contains("Theoretical"));
        assertTrue("should contain 'Effective'",   summary.contains("Effective"));
        assertTrue("should contain 'GB'",          summary.contains("GB"));
        assertTrue("should contain 'MB'",          summary.contains("MB"));
    }

    @Test
    public void formatCapacitySummary_depth20KnownValues() {
        // effective comes first on line 1, theoretical on line 2
        // effective ≈ 672.16 MB, theoretical ≈ 4.29 GB
        String summary = SwarmPostageStampUtils.formatCapacitySummary(20);
        assertTrue("expected effective MB value, got: " + summary, summary.contains("MB"));
        assertTrue("expected ~4.29 GB theoretical, got: " + summary, summary.contains("4.29 GB"));
        // Effective should appear before Theoretical in the string
        assertTrue("Effective should come before Theoretical",
                summary.indexOf("Effective") < summary.indexOf("Theoretical"));
        // Should be two lines separated by newline
        assertTrue("should contain newline separator", summary.contains("\n"));
        String[] lines = summary.split("\n");
        assertEquals("should have exactly 2 lines", 2, lines.length);
        assertTrue("first line should be Effective", lines[0].startsWith("Effective:"));
        assertTrue("second line should be Theoretical", lines[1].startsWith("Theoretical:"));
    }

    // ────────────────────────────────────────────────────────────────────────
    // calculateAmountFromTTL (reverse of calculateTTLSeconds)
    // ────────────────────────────────────────────────────────────────────────

    @Test
    public void calculateAmountFromTTL_roundTrip() {
        // amount → TTL → amount should be close (integer division may lose up to pricePerBlock-1)
        long originalAmount = 5_666_666_666L;
        long price = 62_681L;
        long ttl = SwarmPostageStampUtils.calculateTTLSeconds(originalAmount, price);
        long recoveredAmount = SwarmPostageStampUtils.calculateAmountFromTTL(ttl, price);
        // Recovered amount should be ≤ original and within one block's worth
        assertTrue(recoveredAmount <= originalAmount);
        assertTrue(originalAmount - recoveredAmount < price);
    }

    @Test
    public void calculateAmountFromTTL_twoDays() {
        // 2 days = 172 800 s → blocks = 34 560 → amount = 34 560 × 62 681 = 2 166 351 360
        long ttlSeconds = 2 * 86_400L;
        long amount = SwarmPostageStampUtils.calculateAmountFromTTL(ttlSeconds, 62_681L);
        assertEquals(34_560L * 62_681L, amount);
    }

    @Test
    public void calculateAmountFromTTL_oneWeek() {
        long ttlSeconds = 7 * 86_400L;
        long amount = SwarmPostageStampUtils.calculateAmountFromTTL(ttlSeconds, 62_681L);
        assertEquals((7 * 86_400L / 5) * 62_681L, amount);
    }

    @Test
    public void calculateAmountFromTTL_zeroPriceReturnsZero() {
        assertEquals(0L, SwarmPostageStampUtils.calculateAmountFromTTL(86_400L, 0));
    }

    @Test
    public void calculateAmountFromTTL_zeroTtlReturnsZero() {
        assertEquals(0L, SwarmPostageStampUtils.calculateAmountFromTTL(0, 62_681L));
    }

}

