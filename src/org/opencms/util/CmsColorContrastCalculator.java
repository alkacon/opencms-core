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
 *   <li>Calculate contrast ratios between colors</li>
 *   <li>Check if color combinations meet WCAG AA accessibility standards</li>
 *   <li>Suggest compliant foreground colors for given background colors</li>
 *   <li>Adjust non-compliant colors to meet contrast requirements</li>
 * </ul>
 *
 * Also provides utility methods for CSS colors to:
 * <ul>
 *   <li>Check if a given String represents a valid CSS color, supporting "#fff", "#ffffff", "#ffffffaa" and CSS color names like "white" or "aliceblue" as input.</li>
 *   <li>Converting a valid CSS color String to hex. e.g.  "white" to "#ffffff" or "transparent" to "ffffff00".</li>
 *   <li>Converting a valid CSS color String to an int[] array consisting of the RGB(A) values.</li>
 * </ul>
 *
 * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
 */
public final class CmsColorContrastCalculator {

    /** Color returned for invalid inputs in foreground methods. */
    public static final String INVALID_FOREGROUND = "#ff0000";

    /** Map of CSS named colors to their RGB values. */
    private static final Map<String, int[]> NAMED_COLORS = Map.ofEntries(
        Map.entry("black", new int[] {0, 0, 0}),
        Map.entry("white", new int[] {255, 255, 255}),
        Map.entry("red", new int[] {255, 0, 0}),
        Map.entry("green", new int[] {0, 128, 0}),
        Map.entry("blue", new int[] {0, 0, 255}),
        Map.entry("yellow", new int[] {255, 255, 0}),
        Map.entry("cyan", new int[] {0, 255, 255}),
        Map.entry("magenta", new int[] {255, 0, 255}),
        Map.entry("gray", new int[] {128, 128, 128}),
        Map.entry("grey", new int[] {128, 128, 128}),
        Map.entry("transparent", new int[] {255, 255, 255, 0}),
        Map.entry("aliceblue", new int[] {240, 248, 255}),
        Map.entry("antiquewhite", new int[] {250, 235, 215}),
        Map.entry("aqua", new int[] {0, 255, 255}),
        Map.entry("aquamarine", new int[] {127, 255, 212}),
        Map.entry("azure", new int[] {240, 255, 255}),
        Map.entry("beige", new int[] {245, 245, 220}),
        Map.entry("bisque", new int[] {255, 228, 196}),
        Map.entry("blanchedalmond", new int[] {255, 235, 205}),
        Map.entry("blueviolet", new int[] {138, 43, 226}),
        Map.entry("brown", new int[] {165, 42, 42}),
        Map.entry("burlywood", new int[] {222, 184, 135}),
        Map.entry("cadetblue", new int[] {95, 158, 160}),
        Map.entry("chartreuse", new int[] {127, 255, 0}),
        Map.entry("chocolate", new int[] {210, 105, 30}),
        Map.entry("coral", new int[] {255, 127, 80}),
        Map.entry("cornflowerblue", new int[] {100, 149, 237}),
        Map.entry("cornsilk", new int[] {255, 248, 220}),
        Map.entry("crimson", new int[] {220, 20, 60}),
        Map.entry("darkblue", new int[] {0, 0, 139}),
        Map.entry("darkcyan", new int[] {0, 139, 139}),
        Map.entry("darkgoldenrod", new int[] {184, 134, 11}),
        Map.entry("darkgray", new int[] {169, 169, 169}),
        Map.entry("darkgreen", new int[] {0, 100, 0}),
        Map.entry("darkgrey", new int[] {169, 169, 169}),
        Map.entry("darkkhaki", new int[] {189, 183, 107}),
        Map.entry("darkmagenta", new int[] {139, 0, 139}),
        Map.entry("darkolivegreen", new int[] {85, 107, 47}),
        Map.entry("darkorange", new int[] {255, 140, 0}),
        Map.entry("darkorchid", new int[] {153, 50, 204}),
        Map.entry("darkred", new int[] {139, 0, 0}),
        Map.entry("darksalmon", new int[] {233, 150, 122}),
        Map.entry("darkseagreen", new int[] {143, 188, 143}),
        Map.entry("darkslateblue", new int[] {72, 61, 139}),
        Map.entry("darkslategray", new int[] {47, 79, 79}),
        Map.entry("darkslategrey", new int[] {47, 79, 79}),
        Map.entry("darkturquoise", new int[] {0, 206, 209}),
        Map.entry("darkviolet", new int[] {148, 0, 211}),
        Map.entry("deeppink", new int[] {255, 20, 147}),
        Map.entry("deepskyblue", new int[] {0, 191, 255}),
        Map.entry("dimgray", new int[] {105, 105, 105}),
        Map.entry("dimgrey", new int[] {105, 105, 105}),
        Map.entry("dodgerblue", new int[] {30, 144, 255}),
        Map.entry("firebrick", new int[] {178, 34, 34}),
        Map.entry("floralwhite", new int[] {255, 250, 240}),
        Map.entry("forestgreen", new int[] {34, 139, 34}),
        Map.entry("fuchsia", new int[] {255, 0, 255}),
        Map.entry("gainsboro", new int[] {220, 220, 220}),
        Map.entry("ghostwhite", new int[] {248, 248, 255}),
        Map.entry("gold", new int[] {255, 215, 0}),
        Map.entry("goldenrod", new int[] {218, 165, 32}),
        Map.entry("greenyellow", new int[] {173, 255, 47}),
        Map.entry("honeydew", new int[] {240, 255, 240}),
        Map.entry("hotpink", new int[] {255, 105, 180}),
        Map.entry("indianred", new int[] {205, 92, 92}),
        Map.entry("indigo", new int[] {75, 0, 130}),
        Map.entry("ivory", new int[] {255, 255, 240}),
        Map.entry("khaki", new int[] {240, 230, 140}),
        Map.entry("lavender", new int[] {230, 230, 250}),
        Map.entry("lavenderblush", new int[] {255, 240, 245}),
        Map.entry("lawngreen", new int[] {124, 252, 0}),
        Map.entry("lemonchiffon", new int[] {255, 250, 205}),
        Map.entry("lightblue", new int[] {173, 216, 230}),
        Map.entry("lightcoral", new int[] {240, 128, 128}),
        Map.entry("lightcyan", new int[] {224, 255, 255}),
        Map.entry("lightgoldenrodyellow", new int[] {250, 250, 210}),
        Map.entry("lightgray", new int[] {211, 211, 211}),
        Map.entry("lightgreen", new int[] {144, 238, 144}),
        Map.entry("lightgrey", new int[] {211, 211, 211}),
        Map.entry("lightpink", new int[] {255, 182, 193}),
        Map.entry("lightsalmon", new int[] {255, 160, 122}),
        Map.entry("lightseagreen", new int[] {32, 178, 170}),
        Map.entry("lightskyblue", new int[] {135, 206, 250}),
        Map.entry("lightslategray", new int[] {119, 136, 153}),
        Map.entry("lightslategrey", new int[] {119, 136, 153}),
        Map.entry("lightsteelblue", new int[] {176, 196, 222}),
        Map.entry("lightyellow", new int[] {255, 255, 224}),
        Map.entry("lime", new int[] {0, 255, 0}),
        Map.entry("limegreen", new int[] {50, 205, 50}),
        Map.entry("linen", new int[] {250, 240, 230}),
        Map.entry("maroon", new int[] {128, 0, 0}),
        Map.entry("mediumaquamarine", new int[] {102, 205, 170}),
        Map.entry("mediumblue", new int[] {0, 0, 205}),
        Map.entry("mediumorchid", new int[] {186, 85, 211}),
        Map.entry("mediumpurple", new int[] {147, 112, 219}),
        Map.entry("mediumseagreen", new int[] {60, 179, 113}),
        Map.entry("mediumslateblue", new int[] {123, 104, 238}),
        Map.entry("mediumspringgreen", new int[] {0, 250, 154}),
        Map.entry("mediumturquoise", new int[] {72, 209, 204}),
        Map.entry("mediumvioletred", new int[] {199, 21, 133}),
        Map.entry("midnightblue", new int[] {25, 25, 112}),
        Map.entry("mintcream", new int[] {245, 255, 250}),
        Map.entry("mistyrose", new int[] {255, 228, 225}),
        Map.entry("moccasin", new int[] {255, 228, 181}),
        Map.entry("navajowhite", new int[] {255, 222, 173}),
        Map.entry("navy", new int[] {0, 0, 128}),
        Map.entry("oldlace", new int[] {253, 245, 230}),
        Map.entry("olive", new int[] {128, 128, 0}),
        Map.entry("olivedrab", new int[] {107, 142, 35}),
        Map.entry("opencms", new int[] {179, 27, 52}), // couldn't resist :)
        Map.entry("orange", new int[] {255, 165, 0}),
        Map.entry("orangered", new int[] {255, 69, 0}),
        Map.entry("orchid", new int[] {218, 112, 214}),
        Map.entry("palegoldenrod", new int[] {238, 232, 170}),
        Map.entry("palegreen", new int[] {152, 251, 152}),
        Map.entry("paleturquoise", new int[] {175, 238, 238}),
        Map.entry("palevioletred", new int[] {219, 112, 147}),
        Map.entry("papayawhip", new int[] {255, 239, 213}),
        Map.entry("peachpuff", new int[] {255, 218, 185}),
        Map.entry("peru", new int[] {205, 133, 63}),
        Map.entry("pink", new int[] {255, 192, 203}),
        Map.entry("plum", new int[] {221, 160, 221}),
        Map.entry("powderblue", new int[] {176, 224, 230}),
        Map.entry("purple", new int[] {128, 0, 128}),
        Map.entry("rebeccapurple", new int[] {102, 51, 153}),
        Map.entry("rosybrown", new int[] {188, 143, 143}),
        Map.entry("royalblue", new int[] {65, 105, 225}),
        Map.entry("saddlebrown", new int[] {139, 69, 19}),
        Map.entry("salmon", new int[] {250, 128, 114}),
        Map.entry("sandybrown", new int[] {244, 164, 96}),
        Map.entry("seagreen", new int[] {46, 139, 87}),
        Map.entry("seashell", new int[] {255, 245, 238}),
        Map.entry("sienna", new int[] {160, 82, 45}),
        Map.entry("silver", new int[] {192, 192, 192}),
        Map.entry("skyblue", new int[] {135, 206, 235}),
        Map.entry("slateblue", new int[] {106, 90, 205}),
        Map.entry("slategray", new int[] {112, 128, 144}),
        Map.entry("slategrey", new int[] {112, 128, 144}),
        Map.entry("snow", new int[] {255, 250, 250}),
        Map.entry("springgreen", new int[] {0, 255, 127}),
        Map.entry("steelblue", new int[] {70, 130, 180}),
        Map.entry("tan", new int[] {210, 180, 140}),
        Map.entry("teal", new int[] {0, 128, 128}),
        Map.entry("thistle", new int[] {216, 191, 216}),
        Map.entry("tomato", new int[] {255, 99, 71}),
        Map.entry("turquoise", new int[] {64, 224, 208}),
        Map.entry("violet", new int[] {238, 130, 238}),
        Map.entry("wheat", new int[] {245, 222, 179}),
        Map.entry("whitesmoke", new int[] {245, 245, 245}),
        Map.entry("yellowgreen", new int[] {154, 205, 50}));

    /** Cache for precomputed luminance values. */
    private Map<String, Double> m_luminanceCache;

    public CmsColorContrastCalculator() {

        m_luminanceCache = new ConcurrentHashMap<>();
    }

    /**
     * Checks if the provided foreground color has sufficient contrast with the background.<p>
     *
     * Returns the provided {@code fgColor} if compliant, otherwise black ("#000000") or white ("#ffffff") depending on contrast, or red ("#ff0000") if any parameter is invalid.<p>
     *
     * @param bgColor background color
     * @param fgColor potential foreground color
     *
     * @return the provided {@code fgColor} if compliant, otherwise black ("#000000") or white ("#ffffff") depending on contrast, or red ("#ff0000") if any parameter is invalid
     */
    public String checkForeground(String bgColor, String fgColor) {

        try {
            return checkForegroundRgb(toRgbArray(bgColor, true, false), toRgbArray(fgColor, true, false));
        } catch (IllegalArgumentException e) {
            return INVALID_FOREGROUND;
        }
    }

    /**
     * Checks if any of the foreground colors in the provided list has sufficient contrast with the background.
     * If so, returns the first compliant color from the list.
     * If not, returns either black ("#000000") or white ("#ffffff"), whichever provides better contrast.<p>
     *
     * @param bgColor background color
     * @param fgColorList list of potential foreground colors
     *
     * @return the first compliant color from the list, otherwise black ("#000000") or white ("#ffffff") depending on contrast, or red ("#ff0000") if any parameter is invalid
     */
    public String checkForegroundList(String bgColor, List<String> fgColorList) {

        return checkForegroundListRgb(toRgbArray(bgColor, true, false), colorListToRgbList(fgColorList));
    }

    /**
     * Calculates the contrast ratio between two colors according to WCAG 2.2 guidelines.<p>
     *
     * The contrast ratio formula is (L1 + 0.05) / (L2 + 0.05), where L1 is the lighter
     * relative luminance and L2 is the darker.<p>
     *
     * Returns the contrast ratio between 1:1 and 21:1, or 0 if either parameter is invalid.<p>
     *
     * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
     *
     * @param bgColor background color
     * @param fgColor foreground color
     *
     * @return the contrast ratio between 1:1 and 21:1, or 0 if either parameter is invalid
     */
    public double getContrast(String bgColor, String fgColor) {

        return getContrastRgb(toRgbArray(bgColor, true, false), toRgbArray(fgColor, true, false));
    }

    /**
     * Treats the given color as background and returns either black or white as foreground color,
     * choosing whichever provides better contrast according to WCAG guidelines.<p>
     *
     * Returns black ("#000000") or white ("#ffffff") depending on contrast, or red ("#ff0000") if the parameter is invalid.<p>
     *
     * @param bgColor background color
     *
     * @return black ("#000000") or white ("#ffffff") depending on contrast, or red ("#ff0000") if the parameter is invalid
     */
    public String getForeground(String bgColor) {

        try {
            return getForegroundRgb(toRgbArray(bgColor, true, false));
        } catch (IllegalArgumentException e) {
            return INVALID_FOREGROUND;
        }
    }

    /**
     * Checks if the contrast ratio between two colors meets the WCAG AA standard minimum requirement of 4.5:1 for normal text.<p>
     *
     * Returns false if either parameter is invalid.<p>
     *
     * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html">WCAG 2.2 Contrast Guidelines</a>
     *
     * @param bgColor background color
     * @param fgColor foreground color
     *
     * @return true if contrast ratio is at least 4.5:1
     */
    public boolean hasSufficientContrast(String bgColor, String fgColor) {

        return hasSufficientContrastRgb(toRgbArray(bgColor, true, false), toRgbArray(fgColor, true, false));
    }

    /**
     * Checks if the provided String represents a CSS color.<p>
     *
     * Accepts hex colors in the format "#rrggbb", "#rgb" or "#rrggbbaa".
     * Additionally, this method supports named CSS colors, e.g. "white", "blue", "transparent" etc.
     * The input will be trimmed, so it can contain leading or trailing white spaces.<p>
     *
     * @param color the color to validate
     *
     * @return true if the input is a valid CSS color
     *
     * @see #normalize(String)
     * @see #toHex(String)
     */
    public boolean isValid(String color) {

        return null != toRgbArray(color);
    }

    /**
     * Normalizes a CSS color by converting it to a 6 (or 8 for RGBA) digit hex representation.<p>
     *
     * Accepts a hex colors in the format "#rrggbb", "#rgb" or "#rrggbbaa".
     * Additionally, this method supports named CSS colors, e.g. "white", "blue", "transparent" etc.
     * If the input includes an alpha channel, it will also be included in the returned String.
     * The input will be trimmed, so it can contain leading or trailing white spaces.<p>
     *
     * If the input is not a valid CSS color, the method returns red ("#ff0000").
     * So it is assured that the output of this methods is always a valid CSS color.<p>
     *
     * @param color the color name or hex color code (e.g. "white", "#ffffff" or "#fff")
     *
     * @return to colors hex representation, or red ("#ff0000") if the input is invalid
     *
     * @see #isValid(String)
     * @see #toHex(String)
     */
    public String normalize(String color) {

        String result = toHex(color);
        if (result == null) {
            result = INVALID_FOREGROUND;
        }
        return result;
    }

    /**
     * Suggests a WCAG compliant foreground color based on the given background color.<p>
     *
     * If the provided foreground color doesn't meet the minimum contrast ratio of 4.5:1,
     * it will be adjusted to be either darker or lighter until it does.<p>
     *
     * Returns red("#ff0000") if either parameter is invalid.<p>
     *
     * @param bgColor background color
     * @param fgColor potential foreground color
     *
     * @return either the original color if compliant, or a suggested compliant alternative
     */
    public String suggestForeground(String bgColor, String fgColor) {

        try {
            return suggestForegroundRgb(toRgbArray(bgColor, true, false), toRgbArray(fgColor, true, false));
        } catch (IllegalArgumentException e) {
            return INVALID_FOREGROUND;
        }
    }

    /**
     * Converts a CSS color to a 'normalized' hex representation.<p>
     *
     * Accepts a hex colors in the format "#rrggbb", "#rgb" or "#rrggbbaa".
     * Additionally, this method supports named CSS colors, e.g. "white", "blue", "transparent" etc.
     * If the input includes an alpha channel, it will also be included in the returned String.
     * The input will be trimmed, so it can contain leading or trailing white spaces.<p>
     *
     * If the input is not a valid CSS color, the method returns {@code null}.<p>
     *
     * @param color the color name or hex color code (e.g. "white", "#ffffff" or "#fff")
     *
     * @return to colors hex representation, or {@code null} if the input is invalid
     *
     * @see #isValid(String)
     * @see #toHex(String)
     */
    public String toHex(String color) {

        String result = null;
        if ((color != null) && (color.length() != 0)) {
            if (isValid(color)) {
                return rgbToHex(toRgbArray(color));
            }
        }
        return result;
    }

    /**
     * Converts a CSS color to its corresponding RGB values.<p>
     *
     * Accepts a hex colors in the format "#rrggbb", "#rgb" or "#rrggbbaa".
     * Additionally, this method supports named CSS colors, e.g. "white", "blue", "transparent" etc.
     * If the input includes an alpha channel, it will also be included in the returned array.
     * The input will be trimmed, so it can contain leading or trailing white spaces.<p>
     *
     * If the input is not a valid CSS color, the method returns {@code null}.<p>
     *
     * @param color the color name or hex color code (e.g. "white", "#ffffff" or "#fff")
     *
     * @return an array of integers representing the RGB(A) values, or {@code null} if the input is not a valid CSS color
     *
     * @see #toRgbArray(String, boolean, boolean)
     * @see #toRgb(String)
     */
    public int[] toRgbArray(String color) {

        return toRgbArray(color, true, true);
    }

    /**
     * Converts a CSS color to its corresponding RGB values.<p>
     *
     * If the input is not a valid CSS color, the method returns {@code null}.<p>
     *
     * @param color the color name or hex color code (e.g. "white", "#ffffff", or "#fff")
     * @param supportNames if {@code true}, support named CSS colors in the input
     * @param supportAlpha if {@code true}, support alpha channel in RGB(A) hex codes in the input
     *
     * @return an array of integers representing the RGB(A) values, or {@code null} if the input is invalid
     *
     * @see #toRgbArray(String)
     * @see #toRgb(String)
     */
    public int[] toRgbArray(String color, boolean supportNames, boolean supportAlpha) {

        int[] result = null;
        if (color != null) {
            color = color.toLowerCase().trim();
            if (color.startsWith("#")) {
                try {
                    color = normalizeHex(color, supportAlpha);
                    int length = color.length();
                    if (supportAlpha || (length == 7)) {
                        result = new int[length == 7 ? 3 : 4];
                        for (int i = 0; i < result.length; i++) {
                            result[i] = Integer.parseInt(color.substring(1 + (i * 2), 3 + (i * 2)), 16);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    result = null;
                }
            } else if (supportNames) {
                result = NAMED_COLORS.get(color);
            }
        }
        return result;
    }

    /**
     * Converts a CSS color to its corresponding RGB representation as a comma separated String.<p>
     * For example, the color "red" will be converted to "255, 0, 0".<p>
     *
     * @param color the color name or hex color code (e.g. "white", "#ffffff" or "#fff")
     *
     * @return the RGB representation as a comma separated String, or {@code null} if the input is invalid
     *
     * @see #toRgbArray(String)
     */
    public String toRgb(String color) {

        String result = null;
        int[] rgb = toRgbArray(color, true, true);
        if (rgb != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rgb.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(rgb[i]);
            }
            result = sb.toString();
        }
        return result;
    }

    /**
     * Calculates the contrast ratio between two colors using WCAG formula.<p>
     *
     * @param color1 first color as RGB array
     * @param color2 second color as RGB array
     *
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
     * Calculates the relative luminance of a color according to WCAG 2.2.<p>
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
     * Checks if any of the foreground colors in the provided list has sufficient contrast with the background.
     * If so, returns the first compliant color from the list.
     * If not, returns either black ("#000000") or white ("#ffffff"), whichever provides better contrast.<p>
     *
     * @param bgRgb background color as RGB array
     * @param fgRgbList list of potential foreground colors as RGB arrays
     *
     * @return the first compliant color from the list, otherwise black ("#000000") or white ("#ffffff") depending on contrast, or red ("#ff0000") if any parameter is invalid
     */
    private String checkForegroundListRgb(int[] bgRgb, List<int[]> fgRgbList) {

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
     * Checks if the provided foreground color has sufficient contrast with the background.<p>
     *
     * Returns the provided {@code fgRgb} if compliant, otherwise black ("#000000") or white ("#ffffff") depending on contrast, or red ("#ff0000") if any parameter is invalid.<p>
     *
     * @param bgRgb background color as RGB array
     * @param fgRgb potential foreground color as RGB array
     * @return the provided {@code fgRgb} if compliant, otherwise black ("#000000") or white ("#ffffff") depending on contrast, or red ("#ff0000") if any parameter is invalid
     */
    private String checkForegroundRgb(int[] bgRgb, int[] fgRgb) {

        try {
            validateRgb(bgRgb);
            validateRgb(fgRgb);
        } catch (IllegalArgumentException e) {
            return INVALID_FOREGROUND;
        }
        return hasSufficientContrastRgb(bgRgb, fgRgb) ? rgbToHex(fgRgb) : getForegroundRgb(bgRgb);
    }

    /**
     * Converts a list of color strings to a list of RGB arrays.<p>
     *
     * @param colorList list of color strings
     * @return list of RGB int arrays
     */
    private List<int[]> colorListToRgbList(List<String> colorList) {

        if (colorList == null) {
            return null;
        }
        return colorList.stream().map(color -> toRgbArray(color, true, false)).collect(Collectors.toList());
    }

    /**
     * Gets the cached luminance value for a color, calculating it if not present.<p>
     *
     * @param rgb color as RGB array
     * @return luminance value between 0 and 1
     */
    private double getCachedLuminance(int[] rgb) {

        String hex = rgbToHex(rgb);
        return m_luminanceCache.computeIfAbsent(hex, k -> calculateRelativeLuminance(rgb));
    }

    /**
     * Finds the closest color that meets WCAG contrast requirements by adjusting brightness.<p>
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
     * Calculates the contrast ratio between two colors using their RGB values.<p>
     *
     * @param bgRgb background color as RGB array
     * @param fgRgb foreground color as RGB array
     * @return contrast ratio between 1:1 and 21:1
     */
    private double getContrastRgb(int[] bgRgb, int[] fgRgb) {

        try {
            validateRgb(bgRgb);
            validateRgb(fgRgb);
        } catch (IllegalArgumentException e) {
            return 0.0; // Return zero for invalid input
        }
        return calculateContrastRatio(bgRgb, fgRgb);
    }

    /**
     * Determines the best foreground color (black or white) for the given background color based on luminance.<p>
     *
     * @param bgRgb background color as RGB array
     *
     * @return black ("#000000") or white ("#ffffff") depending on contrast, or red ("#ff0000") if the parameter is invalid
     */
    private String getForegroundRgb(int[] bgRgb) {

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
     * Checks if the contrast ratio between two colors meets the WCAG AA standard minimum requirement of 4.5:1 for normal text.<p>
     *
     * @param bgRgb background color as RGB array
     * @param fgRgb foreground color as RGB array
     *
     * @return true if contrast ratio is at least 4.5:1
     */
    private boolean hasSufficientContrastRgb(int[] bgRgb, int[] fgRgb) {

        return getContrastRgb(bgRgb, fgRgb) >= 4.5;
    }

    /**
     * Applies gamma correction to normalize RGB channel values.<p>
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
     * Normalizes hex color codes to standard 6-digit or 8-digit format with # prefix.<p>
     *
     * @param hex color code to normalize
     * @param supportAlpha if true, supports alpha channel in RGB(A) hex codes
     *
     * @return normalized hex color (e.g. "#ffffff" or "#ffffff00")
     *
     * @throws IllegalArgumentException if hex format is invalid
     */
    private String normalizeHex(String hex, boolean supportAlpha) {

        if ((hex == null) || (hex.length() == 0)) {
            throw new IllegalArgumentException("Invalid empty hex color value");
        }
        hex = hex.toLowerCase().trim();
        if (!hex.startsWith("#")) {
            throw new IllegalArgumentException("Invalid hex color, value must start with '#'");
        }
        hex = hex.substring(1);
        if ((hex.length() != 3) && (hex.length() != 6) && (hex.length() != 8)) {
            throw new IllegalArgumentException("Invalid hex color value: " + hex);
        }
        if (hex.length() == 3) {
            hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
        } else if (!supportAlpha && (hex.length() == 8) && hex.endsWith("ff")) {
            hex = hex.substring(0, 6);
        }
        return "#" + hex;
    }

    /**
     * Converts RGB values to hex color code.<p>
     *
     * @param rgb color as RGB array
     *
     * @return hex color code (e.g. "#ffffff" or "#ffffff00" if alpha is present)
     */
    private String rgbToHex(int[] rgb) {

        char[] hexChars = new char[rgb.length == 4 ? 9 : 7];
        hexChars[0] = '#';
        for (int i = 0; i < rgb.length; i++) {
            int value = rgb[i];
            hexChars[1 + (i * 2)] = toHexChar((value >> 4) & 0xF);
            hexChars[2 + (i * 2)] = toHexChar(value & 0xF);
        }
        return new String(hexChars);
    }

    /**
     * Suggests a WCAG-compliant foreground color based on the given background color.
     * If the provided foreground color doesn't meet the minimum contrast ratio of 4.5:1, returns an adjusted color that does.<p>
     *
     * @param bgRgb background color as RGB array
     * @param possibleFgRgb proposed foreground color as RGB array
     *
     * @return hex code of either the original color if compliant, or a suggested compliant alternative
     */
    private String suggestForegroundRgb(int[] bgRgb, int[] possibleFgRgb) {

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
     * Converts a number (0-15) to its hexadecimal character representation.<p>
     *
     * @param value number to convert (0-15)
     *
     * @return hexadecimal character ('0'-'9' or 'a'-'f')
     */
    private char toHexChar(int value) {

        return (char)(value < 10 ? '0' + value : 'a' + (value - 10));
    }

    /**
     * Validates that an RGB array contains three values between 0 and 255.<p>
     *
     * @param rgb array to validate
     *
     * @throws IllegalArgumentException if array is null or contains invalid values
     */
    private void validateRgb(int[] rgb) {

        validateRgb(rgb, false);
    }

    /**
     * Validates that an RGB array contains three or four values between 0 and 255.<p>
     *
     * @param rgb array to validate
     * @param supportsAlpha if true, the array can optionally have the length 4 instead of 3
     *
     * @throws IllegalArgumentException if array is null or contains invalid values
     */
    private void validateRgb(int[] rgb, boolean supportsAlpha) {

        if (rgb == null) {
            throw new IllegalArgumentException("Invalid RGB value: " + rgb);
        }
        if ((rgb.length == 4) && (rgb[3] == 255)) {
            // Treat as length 3 if alpha channel is 255
            rgb = new int[] {rgb[0], rgb[1], rgb[2]};
        }
        if (((rgb.length != 3) && (!supportsAlpha || (rgb.length != 4)))
            || (rgb[0] < 0)
            || (rgb[0] > 255)
            || (rgb[1] < 0)
            || (rgb[1] > 255)
            || (rgb[2] < 0)
            || (rgb[2] > 255)
            || (supportsAlpha && (rgb.length == 4) && ((rgb[3] < 0) || (rgb[3] > 255)))) {
            throw new IllegalArgumentException(
                "Invalid RGB value: ["
                    + rgb[0]
                    + ", "
                    + rgb[1]
                    + ", "
                    + rgb[2]
                    + (supportsAlpha && (rgb.length == 4) ? ", " + rgb[3] : "")
                    + "]. "
                    + "Each value must be an integer between 0 and 255.");
        }
    }
}
