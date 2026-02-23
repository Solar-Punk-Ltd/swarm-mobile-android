package com.swarm.mobile.utils;

public class StringUtils {

    /**
     * Truncates a string with ellipsis in the middle if it exceeds the specified length.
     *
     * @param text The text to truncate
     * @param maxLength The maximum length before truncation
     * @return The truncated string with ellipsis in the middle, or the original if shorter than maxLength
     */
    public static String ellipsizeMiddle(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }

        // Reserve 3 characters for "..."
        int charsToShow = maxLength - 3;
        int frontChars = (charsToShow + 1) / 2; // Round up for front
        int backChars = charsToShow / 2;       // Round down for back

        return text.substring(0, frontChars) + "..." + text.substring(text.length() - backChars);
    }
}
