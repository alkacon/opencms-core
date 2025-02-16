/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Calculator for color contrast ratios according to WCAG 2.2 guidelines.<p>
 *
 * This class provides functionality to:
 * <ul>
 *   <li>Calculate contrast ratios between colors (both hex and RGB formats)</li>
 *   <li>Check if color combinations meet WCAG AA accessibility standards</li>
 *   <li>Suggest compliant foreground colors for given background colors</li>
 *   <li>Adjust non-compliant colors to meet contrast requirements</li>
 * </ul>
 *
 * Key features:
 * <ul>
 *   <li>Supports both hex (#ffffff) and RGB ([255,255,255]) color formats</li>
 *   <li>Implements WCAG AA standard minimum contrast ratio of 4.5:1 for normal text</li>
 *   <li>Provides automatic adjustments of non-compliant colors</li>
 *   <li>Uses caching to optimize performance for repeated calculations</li>
 * </ul>
 *
 * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
 */
public final class CmsColorContrastCalculator {

    /** Color returned for invalid inputs in foreground methods. */
    public static final String INVALID_FOREGROUND = "#ff0000";

    /** Cache for precomputed luminance values. */
    private Map<String, Double> m_luminanceCache;

    public CmsColorContrastCalculator() {

        m_luminanceCache = new ConcurrentHashMap<>();
    }

    /**
     * Checks if the provided foreground color has sufficient contrast with the background.
     * If not, returns either black or white (whichever provides better contrast).
     * Returns red (#ff0000) if either parameter is invalid.
     *
     * @param bgHex background color in hex format
     * @param fgHex foreground color to check in hex format
     * @return original foreground color if compliant, otherwise black or white
     */
    public String checkForeground(String bgHex, String fgHex) {
        try {
            return checkForegroundRgb(hexToRgb(bgHex), hexToRgb(fgHex));
        } catch (IllegalArgumentException e) {
            return INVALID_FOREGROUND;
        }
    }

    /**
     * Checks if any of the foreground colors in the provided list has sufficient contrast with the background.
     * If so, returns the first compliant color from the list.
     * If not, returns either black or white (whichever provides better contrast).
     * Returns red (#ff0000) if either parameter is invalid.
     *
     * @param bgHex background color in hex format
     * @param fgHex foreground color to check in hex format
     * @return original foreground color if compliant, otherwise black or white
     */
    public String checkForegroundList(String bgHex, List<String> fgHexList) {

        return checkForegroundListRgb(
            hexToRgb(bgHex),
            hexListToRgbList(fgHexList)
        );
    }

    /**
     * Checks if any of the foreground colors in the provided list has sufficient contrast with the background.
     * Returns the first compliant color found, or black/white if none is compliant.
     * Returns red (#ff0000) if the background is invalid or if all foreground colors are invalid.
     *
     * @param bgRgb background color as RGB array
     * @param fgRgbList list of foreground colors to check as RGB arrays
     * @return first compliant color from the list, black/white if none found, or red if invalid input
     */
    public String checkForegroundListRgb(int[] bgRgb, List<int[]> fgRgbList) {

        if ((fgRgbList == null) || fgRgbList.isEmpty()) {
            return INVALID_FOREGROUND;
        }
        try {
            validateRgb(bgRgb);
        } catch (IllegalArgumentException e) {
            return INVALID_FOREGROUND;
        }

        boolean hasValidColor = false;
        for (int[] fgRgb : fgRgbList) {
            try {
                validateRgb(fgRgb);
                if (hasSufficientContrastRgb(bgRgb, fgRgb)) {
                    return rgbToHex(fgRgb);
                }
                hasValidColor = true;
            } catch (IllegalArgumentException e) {
                continue;
            }
        }

        return hasValidColor ? getForegroundRgb(bgRgb) : INVALID_FOREGROUND;
    }

    /**
     * Checks if the provided foreground color has sufficient contrast with the background.
     * If not, returns either black or white (whichever provides better contrast).
     * Returns red (#ff0000) if either parameter is invalid.
     *
     * @param bgRgb background color as RGB array
     * @param fgRgb foreground color to check as RGB array
     * @return original foreground color if compliant, otherwise black or white
     */
    public String checkForegroundRgb(int[] bgRgb, int[] fgRgb) {
        try {
            validateRgb(bgRgb);
            validateRgb(fgRgb);
        } catch (IllegalArgumentException e) {
            return INVALID_FOREGROUND;
        }
        return hasSufficientContrastRgb(bgRgb, fgRgb) ? rgbToHex(fgRgb) : getForegroundRgb(bgRgb);
    }

    /**
     * Calculates the contrast ratio between two colors according to WCAG 2.2 guidelines.
     * The contrast ratio formula is (L1 + 0.05) / (L2 + 0.05), where L1 is the lighter
     * relative luminance and L2 is the darker.
     * Returns 0 if either parameter is invalid.
     *
     * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
     * @param bgHex background color in hex format (e.g., "#ffffff" or "#fff")
     * @param fgHex foreground color in hex format (e.g., "#000000" or "#000")
     * @return contrast ratio between 1:1 and 21:1
     */
    public double getContrast(String bgHex, String fgHex) {

        return getContrastRgb(hexToRgb(bgHex), hexToRgb(fgHex));
    }

    /**
     * Calculates the contrast ratio between two RGB colors according to WCAG 2.2 guidelines.
     * Returns 0 if either parameter is invalid.
     *
     * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
     * @param bgRgb background color as RGB array [r, g, b] with values 0-255
     * @param fgRgb foreground color as RGB array [r, g, b] with values 0-255
     * @return contrast ratio between 1:1 and 21:1
     * @throws IllegalArgumentException if RGB values are invalid
     */
    public double getContrastRgb(int[] bgRgb, int[] fgRgb) {

        try {
            validateRgb(bgRgb);
            validateRgb(fgRgb);
        } catch (IllegalArgumentException e) {
            return 0.0; // Return zero for invalid input
        }
        return calculateContrastRatio(bgRgb, fgRgb);
    }

    /**
     * Checks the background color and returns either black or white as foreground color,
     * choosing whichever provides better contrast according to WCAG guidelines.
     * Returns red (#ff0000) if the parameter is invalid.
     *
     * @param bgHex background color in hex format
     * @return "#000000" or "#ffffff" depending on contrast
     */
    public String getForeground(String bgHex) {
        try {
            return getForegroundRgb(hexToRgb(bgHex));
        } catch (IllegalArgumentException e) {
            return INVALID_FOREGROUND;
        }
    }

    /**
     * Checks the RGB background color and returns either black or white as foreground color,
     * choosing whichever provides better contrast according to WCAG guidelines.
     * Returns red (#ff0000) if the parameter is invalid.
     *
     * @param bgRgb background color as RGB array [r, g, b]
     * @return "#000000" or "#ffffff" depending on contrast
     * @throws IllegalArgumentException if RGB values are invalid
     */
    public String getForegroundRgb(int[] bgRgb) {

        try {
            validateRgb(bgRgb);
        } catch (IllegalArgumentException e) {
            return INVALID_FOREGROUND;
        }
        // Use luminance threshold of 0.179 (true middle point between black and white luminance)
        // This is more efficient than calculating contrast ratios with both black and white
        return getCachedLuminance(bgRgb) > 0.179 ? "#000000" : "#ffffff";
    }

    /**
     * Checks if the contrast ratio between two colors meets the WCAG AA standard
     * minimum requirement of 4.5:1 for normal text.
     * Returns false if either parameter is invalid.
     *
     * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
     * @param bgHex background color in hex format
     * @param fgHex foreground color in hex format
     * @return true if contrast ratio is at least 4.5:1
     */
    public boolean hasSufficientContrast(String bgHex, String fgHex) {

        return hasSufficientContrastRgb(hexToRgb(bgHex), hexToRgb(fgHex));
    }

    /**
     * Checks if the contrast ratio between two RGB colors meets the WCAG AA standard
     * minimum requirement of 4.5:1 for normal text.
     * Returns false if either parameter is invalid.
     *
     * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
     * @param bgRgb background color as RGB array [r, g, b]
     * @param fgRgb foreground color as RGB array [r, g, b]
     * @return true if contrast ratio is at least 4.5:1
     * @throws IllegalArgumentException if RGB values are invalid
     */
    public boolean hasSufficientContrastRgb(int[] bgRgb, int[] fgRgb) {

        return getContrastRgb(bgRgb, fgRgb) >= 4.5;
    }

    /**
     * Converts a hex color code to its corresponding RGB values.<p>
     *
     * This method takes a hex color code in the format "rrggbb" or "rgb" and converts it to an array of integers
     * representing the red, green, and blue components of the color. The input hex color code can optionally include
     * a leading "#" character.<p>
     *
     * If the input hex color code is invalid, the method returns {@code null}.
     *
     * @param hex the color in hex format (e.g., "#ffffff" or "#fff")
     * @return an array of integers representing the RGB values, or {@code null} if the input is invalid
     */
    public int[] hexToRgb(String hex) {

        try {
            hex = normalizeHex(hex);
            return new int[] {
                Integer.parseInt(hex.substring(1, 3), 16),
                Integer.parseInt(hex.substring(3, 5), 16),
                Integer.parseInt(hex.substring(5, 7), 16)
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Validates a web hex color input.
     * Returns true if the input is a valid 3-digit or 6-digit hex color code with or without # prefix.
     *
     * @param hexColor the color to validate
     * @return true if the input is a valid 3-digit or 6-digit hex color code with or without # prefix
     */
    public boolean isValid(String hexColor) {

        try {
            validateRgb(hexToRgb(normalizeHex(hexColor)));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Suggests a WCAG-compliant foreground color based on the given background color.
     * If the provided foreground color doesn't meet the minimum contrast ratio of 4.5:1,
     * returns an adjusted color that does.
     * Returns red (#ff0000) if either parameter is invalid.
     *
     * @param bgHex background color in hex format
     * @param possibleFgHex proposed foreground color in hex format
     * @return either the original color if compliant, or a suggested compliant alternative
     */
    public String suggestForeground(String bgHex, String possibleFgHex) {
        try {
            return suggestForegroundRgb(hexToRgb(bgHex), hexToRgb(possibleFgHex));
        } catch (IllegalArgumentException e) {
            return INVALID_FOREGROUND;
        }
    }

    /**
     * Suggests a WCAG-compliant foreground color based on the given RGB background color.
     * If the provided foreground color doesn't meet the minimum contrast ratio of 4.5:1,
     * returns an adjusted color that does.
     * Returns red (#ff0000) if either parameter is invalid.
     *
     * @param bgRgb background color as RGB array [r, g, b]
     * @param possibleFgRgb proposed foreground color as RGB array [r, g, b]
     * @return hex code of either the original color if compliant, or a suggested compliant alternative
     * @throws IllegalArgumentException if RGB values are invalid
     */
    public String suggestForegroundRgb(int[] bgRgb, int[] possibleFgRgb) {
        try {
            validateRgb(bgRgb);
            validateRgb(possibleFgRgb);
        } catch (IllegalArgumentException e) {
            return INVALID_FOREGROUND;
        }
        return hasSufficientContrastRgb(bgRgb, possibleFgRgb)
        ? rgbToHex(possibleFgRgb)
        : getClosestCompliantColor(bgRgb, possibleFgRgb);
    }

    /**
     * Validates a web hex color input.
     * Supports both 3-digit and 6-digit hex color codes with or without # prefix.
     * In case the input is a valid hex color, returns the color.
     * All returned colors will use 6-digit web hex notation with # prefix.
     * If the input is invalid, returns red (#ff0000).
     *
     * @param hexColor the color to validate
     * @return the color in 6-digit web hex notation with # prefix or red (#ff0000) if the input is invalid
     */
    public String validate(String hexColor) {

        return isValid(hexColor) ? normalizeHex(hexColor) : INVALID_FOREGROUND;
    }

    /**
     * Calculates the contrast ratio between two colors using WCAG formula.
     *
     * @param color1 first color as RGB array
     * @param color2 second color as RGB array
     * @return contrast ratio between 1:1 and 21:1
     */
    private double calculateContrastRatio(int[] color1, int[] color2) {

        double l1 = getCachedLuminance(color1);
        double l2 = getCachedLuminance(color2);
        // Ensure lighter luminance is always first in formula
        double lighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);
        // WCAG contrast ratio formula: (L1 + 0.05) / (L2 + 0.05)
        return (lighter + 0.05) / (darker + 0.05);
    }

    /**
     * Calculates the relative luminance of a color according to WCAG 2.2.
     *
     * @param rgb color as RGB array
     * @return relative luminance value between 0 and 1
     */
    private double calculateRelativeLuminance(int[] rgb) {

        // WCAG relative luminance formula for sRGB
        double r = normalizeChannel(rgb[0]);
        double g = normalizeChannel(rgb[1]);
        double b = normalizeChannel(rgb[2]);
        // Coefficients from WCAG 2.0 relative luminance formula
        return (0.2126 * r) + (0.7152 * g) + (0.0722 * b);
    }

    /**
     * Gets the cached luminance value for a color, calculating it if not present.
     *
     * @param rgb color as RGB array
     * @return luminance value between 0 and 1
     */
    private double getCachedLuminance(int[] rgb) {

        String hex = rgbToHex(rgb);
        return m_luminanceCache.computeIfAbsent(hex, k -> calculateRelativeLuminance(rgb));
    }

    /**
     * Finds the closest color that meets WCAG contrast requirements by adjusting brightness.
     *
     * @param bgRgb background color as RGB array
     * @param fgRgb foreground color as RGB array to adjust
     * @return hex color code of the compliant color
     */
    private String getClosestCompliantColor(int[] bgRgb, int[] fgRgb) {

        int step = 5; // Smaller steps for more precise adjustments
        int[] originalFgRgb = fgRgb.clone();

        // Try both lighter and darker variants
        for (int i = 0; i <= 255; i += step) {
            // Try lighter version
            int[] lighterRgb = new int[] {
                Math.min(originalFgRgb[0] + i, 255),
                Math.min(originalFgRgb[1] + i, 255),
                Math.min(originalFgRgb[2] + i, 255)};
            if (calculateContrastRatio(bgRgb, lighterRgb) >= 4.5) {
                return rgbToHex(lighterRgb);
            }

            // Try darker version
            int[] darkerRgb = new int[] {
                Math.max(originalFgRgb[0] - i, 0),
                Math.max(originalFgRgb[1] - i, 0),
                Math.max(originalFgRgb[2] - i, 0)};
            if (calculateContrastRatio(bgRgb, darkerRgb) >= 4.5) {
                return rgbToHex(darkerRgb);
            }
        }

        // If no compliant color found, return black or white based on background luminance
        return getForegroundRgb(bgRgb);
    }

    /**
     * Converts a list of hex color strings to a list of RGB arrays.
     *
     * @param hexList list of hex color strings
     * @return list of RGB int arrays
     */
    private List<int[]> hexListToRgbList(List<String> hexList) {

        if (hexList == null) {
            return null;
        }
        return hexList.stream()
            .map(this::hexToRgb)
            .collect(Collectors.toList());
    }

    /**
     * Applies gamma correction to normalize RGB channel values.
     *
     * @param value RGB channel value (0-255)
     * @return normalized value according to WCAG formula
     */
    private double normalizeChannel(int value) {

        // Convert to sRGB value
        double srgb = value / 255.0;
        // WCAG requires gamma correction
        if (srgb <= 0.03928) {
            return srgb / 12.92;
        }
        return Math.pow((srgb + 0.055) / 1.055, 2.4);
    }

    /**
     * Normalizes hex color codes to standard 6-digit format with # prefix.
     *
     * @param hex color code to normalize
     * @return normalized hex color (e.g. "#ffffff")
     * @throws IllegalArgumentException if hex format is invalid
     */
    private String normalizeHex(String hex) {

        if ((hex == null) || (hex.length() == 0)) {
            throw new IllegalArgumentException("Invalid empty hex color value");
        }
        hex = hex.toLowerCase().trim().replace("#", "");
        if ((hex.length() != 3) && (hex.length() != 6)) {
            throw new IllegalArgumentException("Invalid hex color value: " + hex);
        }
        if (hex.length() == 3) {
            hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
        }
        return "#" + hex;
    }

    /**
     * Converts RGB values to hex color code.
     *
     * @param rgb color as RGB array
     * @return hex color code (e.g. "#ffffff")
     */
    private String rgbToHex(int[] rgb) {

        char[] hexChars = new char[7];
        hexChars[0] = '#';
        for (int i = 0; i < 3; i++) {
            int value = rgb[i];
            hexChars[1 + (i * 2)] = toHexChar((value >> 4) & 0xF);
            hexChars[2 + (i * 2)] = toHexChar(value & 0xF);
        }
        return new String(hexChars);
    }

    /**
     * Converts a number (0-15) to its hexadecimal character representation.
     *
     * @param value number to convert (0-15)
     * @return hexadecimal character ('0'-'9' or 'a'-'f')
     */
    private char toHexChar(int value) {

        return (char)(value < 10 ? '0' + value : 'a' + (value - 10));
    }

    /**
     * Validates that an RGB array contains three values between 0 and 255.
     *
     * @param rgb array to validate
     * @throws IllegalArgumentException if array is null or contains invalid values
     */
    private void validateRgb(int[] rgb) {

        if (rgb == null) {
            throw new IllegalArgumentException("Invalid RGB value: " + rgb);
        }
        if ((rgb.length != 3)
            || (rgb[0] < 0)
            || (rgb[0] > 255)
            || (rgb[1] < 0)
            || (rgb[1] > 255)
            || (rgb[2] < 0)
            || (rgb[2] > 255)) {
            throw new IllegalArgumentException(
                "Invalid RGB values: ["
                    + rgb[0]
                    + ", "
                    + rgb[1]
                    + ", "
                    + rgb[2]
                    + "]. "
                    + "Each value must be an integer between 0 and 255.");
        }
    }
}
