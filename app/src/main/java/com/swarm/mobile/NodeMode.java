package com.swarm.mobile;

import androidx.annotation.NonNull;

public enum NodeMode {
    ULTRA_LIGHT("ultra light"),
    LIGHT("light");

    private final String displayName;

    NodeMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get NodeMode from display name string
     * @param displayName The display name (e.g., "ultra light", "light")
     * @return The corresponding NodeMode, or ULTRA_LIGHT as default
     */
    public static NodeMode fromDisplayName(String displayName) {
        if (displayName == null) {
            return ULTRA_LIGHT;
        }
        for (NodeMode mode : NodeMode.values()) {
            if (mode.displayName.equalsIgnoreCase(displayName.trim())) {
                return mode;
            }
        }
        return ULTRA_LIGHT; // default fallback
    }

    /**
     * Get all display names as a String array for UI
     */
    public static String[] getDisplayNames() {
        NodeMode[] modes = values();
        String[] names = new String[modes.length];
        for (int i = 0; i < modes.length; i++) {
            names[i] = modes[i].displayName;
        }
        return names;
    }

    @NonNull
    @Override
    public String toString() {
        return displayName;
    }
}
