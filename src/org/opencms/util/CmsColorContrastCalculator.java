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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    /** Cache for precomputed luminance values. */
    private Map<String, Double> m_luminanceCache;

    public CmsColorContrastCalculator() {

        m_luminanceCache = new ConcurrentHashMap<>();
    }

    /**
     * Calculates the contrast ratio between two colors according to WCAG 2.2 guidelines.
     * The contrast ratio formula is (L1 + 0.05) / (L2 + 0.05), where L1 is the lighter
     * relative luminance and L2 is the darker.
     *
     * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
     * @param bgHex background color in hex format (e.g., "#ffffff" or "#fff")
     * @param fgHex foreground color in hex format (e.g., "#000000" or "#000")
     * @return contrast ratio between 1:1 and 21:1
     */
    public double getContrast(String bgHex, String fgHex) {

        int[] bgRgb = hexToRgb(normalizeHex(bgHex));
        int[] fgRgb = hexToRgb(normalizeHex(fgHex));
        return calculateContrastRatio(bgRgb, fgRgb);
    }

    /**
     * Calculates the contrast ratio between two RGB colors according to WCAG 2.2 guidelines.
     *
     * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
     * @param bgRgb background color as RGB array [r, g, b] with values 0-255
     * @param fgRgb foreground color as RGB array [r, g, b] with values 0-255
     * @return contrast ratio between 1:1 and 21:1
     * @throws IllegalArgumentException if RGB values are invalid
     */
    public double getContrastRgb(int[] bgRgb, int[] fgRgb) {

        validateRgb(bgRgb);
        validateRgb(fgRgb);
        return calculateContrastRatio(bgRgb, fgRgb);
    }

    /**
     * Checks the background color and returns either black or white as foreground color,
     * choosing whichever provides better contrast according to WCAG guidelines.
     *
     * @param bgHex background color in hex format
     * @return "#000000" or "#ffffff" depending on contrast
     */
    public String getForeground(String bgHex) {

        int[] bgRgb = hexToRgb(normalizeHex(bgHex));
        return getForegroundRgb(bgRgb);
    }

    /**
     * Checks if the provided foreground color has sufficient contrast with the background.
     * If not, returns either black or white (whichever provides better contrast).
     *
     * @param bgHex background color in hex format
     * @param fgHex foreground color to check in hex format
     * @return original foreground color if compliant, otherwise black or white
     */
    public String getForegroundCheck(String bgHex, String fgHex) {

        int[] bgRgb = hexToRgb(normalizeHex(bgHex));
        int[] fgRgb = hexToRgb(normalizeHex(fgHex));
        return getForegroundCheckRgb(bgRgb, fgRgb);
    }

    /**
     * RGB version of getForegroundCheck. Checks if the provided foreground color has sufficient
     * contrast with the background. If not, returns either black or white.
     *
     * @param bgRgb background color as RGB array
     * @param fgRgb foreground color to check as RGB array
     * @return original foreground color if compliant, otherwise black or white
     */
    public String getForegroundCheckRgb(int[] bgRgb, int[] fgRgb) {

        validateRgb(bgRgb);
        validateRgb(fgRgb);
        return getHasSufficientContrastRgb(bgRgb, fgRgb) ? rgbToHex(fgRgb) : getForegroundRgb(bgRgb);
    }

    /**
     * Checks the RGB background color and returns either black or white as foreground color,
     * choosing whichever provides better contrast according to WCAG guidelines.
     * Optimized to use luminance threshold instead of contrast ratio comparison.
     *
     * @param bgRgb background color as RGB array [r, g, b]
     * @return "#000000" or "#ffffff" depending on contrast
     * @throws IllegalArgumentException if RGB values are invalid
     */
    public String getForegroundRgb(int[] bgRgb) {

        validateRgb(bgRgb);
        // Use luminance threshold of 0.179 (true middle point between black and white luminance)
        // This is more efficient than calculating contrast ratios with both black and white
        return getCachedLuminance(bgRgb) > 0.179 ? "#000000" : "#ffffff";
    }

    /**
     * Suggests a WCAG-compliant foreground color based on the given background color.
     * If the provided foreground color doesn't meet the minimum contrast ratio of 4.5:1,
     * returns an adjusted color that does.
     *
     * @param bgHex background color in hex format
     * @param possibleFgHex proposed foreground color in hex format
     * @return either the original color if compliant, or a suggested compliant alternative
     */
    public String getForegroundSuggest(String bgHex, String possibleFgHex) {

        int[] bgRgb = hexToRgb(normalizeHex(bgHex));
        int[] possibleFgRgb = hexToRgb(normalizeHex(possibleFgHex));
        return getHasSufficientContrastRgb(bgRgb, possibleFgRgb)
        ? possibleFgHex
        : getClosestCompliantColor(bgRgb, possibleFgRgb);
    }

    /**
     * Suggests a WCAG-compliant foreground color based on the given RGB background color.
     * If the provided foreground color doesn't meet the minimum contrast ratio of 4.5:1,
     * returns an adjusted color that does.
     *
     * @param bgRgb background color as RGB array [r, g, b]
     * @param possibleFgRgb proposed foreground color as RGB array [r, g, b]
     * @return hex code of either the original color if compliant, or a suggested compliant alternative
     * @throws IllegalArgumentException if RGB values are invalid
     */
    public String getForegroundSuggestRgb(int[] bgRgb, int[] possibleFgRgb) {

        validateRgb(bgRgb);
        validateRgb(possibleFgRgb);
        return getHasSufficientContrastRgb(bgRgb, possibleFgRgb)
        ? rgbToHex(possibleFgRgb)
        : getClosestCompliantColor(bgRgb, possibleFgRgb);
    }

    /**
     * Checks if the contrast ratio between two colors meets the WCAG AA standard
     * minimum requirement of 4.5:1 for normal text.
     *
     * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
     * @param bgHex background color in hex format
     * @param fgHex foreground color in hex format
     * @return true if contrast ratio is at least 4.5:1
     */
    public boolean getHasSufficientContrast(String bgHex, String fgHex) {

        return getHasSufficientContrast(bgHex, fgHex, 4.5); // Default threshold
    }

    /**
     * Checks if the contrast ratio between two colors meets a specified threshold.
     * WCAG 2.2 recommends:
     * - 4.5:1 for normal text
     * - 3:1 for large text
     * - 3:1 for graphics and UI components
     *
     * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
     * @param bgHex background color in hex format
     * @param fgHex foreground color in hex format
     * @param threshold minimum required contrast ratio
     * @return true if contrast ratio meets or exceeds the threshold
     */
    public boolean getHasSufficientContrast(String bgHex, String fgHex, double threshold) {

        return getContrast(bgHex, fgHex) >= threshold;
    }

    /**
     * Checks if the contrast ratio between two RGB colors meets the WCAG AA standard
     * minimum requirement of 4.5:1 for normal text.
     *
     * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
     * @param bgRgb background color as RGB array [r, g, b]
     * @param fgRgb foreground color as RGB array [r, g, b]
     * @return true if contrast ratio is at least 4.5:1
     * @throws IllegalArgumentException if RGB values are invalid
     */
    public boolean getHasSufficientContrastRgb(int[] bgRgb, int[] fgRgb) {

        return calculateContrastRatio(bgRgb, fgRgb) >= 4.5;
    }

    private double calculateContrastRatio(int[] color1, int[] color2) {

        double l1 = getCachedLuminance(color1);
        double l2 = getCachedLuminance(color2);
        // Ensure lighter luminance is always first in formula
        double lighter = Math.max(l1, l2);
        double darker = Math.min(l1, l2);
        // WCAG contrast ratio formula: (L1 + 0.05) / (L2 + 0.05)
        return (lighter + 0.05) / (darker + 0.05);
    }

    private double calculateRelativeLuminance(int[] rgb) {

        // WCAG relative luminance formula for sRGB
        double r = normalizeChannel(rgb[0]);
        double g = normalizeChannel(rgb[1]);
        double b = normalizeChannel(rgb[2]);
        // Coefficients from WCAG 2.0 relative luminance formula
        return (0.2126 * r) + (0.7152 * g) + (0.0722 * b);
    }

    private double getCachedLuminance(int[] rgb) {

        String hex = rgbToHex(rgb);
        return m_luminanceCache.computeIfAbsent(hex, k -> calculateRelativeLuminance(rgb));
    }

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

    private int[] hexToRgb(String hex) {

        return new int[] {
            Integer.parseInt(hex.substring(1, 3), 16),
            Integer.parseInt(hex.substring(3, 5), 16),
            Integer.parseInt(hex.substring(5, 7), 16)};
    }

    private double normalizeChannel(int value) {

        // Convert to sRGB value
        double srgb = value / 255.0;
        // WCAG requires gamma correction
        if (srgb <= 0.03928) {
            return srgb / 12.92;
        }
        return Math.pow((srgb + 0.055) / 1.055, 2.4);
    }

    private String normalizeHex(String hex) {

        hex = hex.toLowerCase().replace("#", "");
        if (hex.length() == 3) {
            hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
        }
        if (hex.length() != 6) {
            throw new IllegalArgumentException("Invalid hex color value: " + hex);
        }
        return "#" + hex;
    }

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

    private char toHexChar(int value) {

        return (char)(value < 10 ? '0' + value : 'a' + (value - 10));
    }

    private void validateRgb(int[] rgb) {

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
