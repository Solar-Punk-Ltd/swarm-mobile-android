package com.swarm.mobile.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberUtils {

    /**
     * 1 xBZZ = 10^16 PLUR
     */
    private static final BigDecimal PLUR_PER_XBZZ = new BigDecimal("10000000000000000");

    public static String formatXBzz(String plurStr) {
        if (plurStr == null || plurStr.isBlank()) {
            return "";
        }

        try {
            BigDecimal plur = new BigDecimal(plurStr.trim());
            BigDecimal xBzz = plur.divide(PLUR_PER_XBZZ, 4, RoundingMode.DOWN);
            return xBzz.stripTrailingZeros().toPlainString() + " xBZZ";
        } catch (NumberFormatException e) {
            return plurStr + " PLUR";
        }

    }
}
