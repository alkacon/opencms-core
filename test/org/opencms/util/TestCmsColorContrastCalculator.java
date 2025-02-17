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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import static org.junit.Assert.assertArrayEquals;

import org.opencms.test.OpenCmsTestCase;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * Test cases for {@link org.opencms.util.CmsColorContrastCalculator} focusing on WCAG 2.2 compliance.
 * Test values validated against WebAIM Contrast Checker (https://webaim.org/resources/contrastchecker/).
 */
public class TestCmsColorContrastCalculator extends OpenCmsTestCase {

    private CmsColorContrastCalculator m_calculator;

    /**
     * Default JUnit constructor.<p>
     *
     * @param arg0 JUnit parameters
     */
    public TestCmsColorContrastCalculator(String arg0) {

        super(arg0);
        m_calculator = new CmsColorContrastCalculator();
    }

    @Test
    public void testCheckForeground() {

        // Test that compliant colors are preserved
        assertEquals("#000000", m_calculator.checkForeground("#ffffff", "#000000")); // Keep black on white
        assertEquals("#ffffff", m_calculator.checkForeground("#000000", "#ffffff")); // Keep white on black
        assertEquals("#000000", m_calculator.checkForeground("#ffff00", "#000")); // Keep black on yellow

        // Test that non-compliant colors are replaced with black or white
        assertEquals("#000000", m_calculator.checkForeground("#ffffff", "#777777")); // Replace gray with black on white
        assertEquals("#ffffff", m_calculator.checkForeground("#000000", "#444")); // Replace gray with white on black
        assertEquals("#ffffff", m_calculator.checkForeground("#0000ff", "#aaaaaa")); // Replace gray with white on blue

        // Test edge cases
        assertEquals("#000000", m_calculator.checkForeground("#ffffff", "#ffffff")); // White on white -> black
        assertEquals("#ffffff", m_calculator.checkForeground("#000000", "#000000")); // Black on black -> white

        // Test with various background colors
        String fgColor = "#777777"; // A non-compliant gray
        assertEquals("#000000", m_calculator.checkForeground("#ffff00", fgColor)); // Yellow bg -> black
        assertEquals("#ffffff", m_calculator.checkForeground("#0000ff", fgColor)); // Blue bg -> white
        assertEquals("#000000", m_calculator.checkForeground("#0fc", fgColor)); // Cyan bg -> black

        // Alpha channels and named colors
        assertEquals("#ffffff", m_calculator.checkForeground("opencms", "white"));
        assertEquals("#ffffff", m_calculator.checkForeground("#b31b34", "white"));
        assertEquals("#ffffff", m_calculator.checkForeground("opencms", "#ffffff"));
        assertEquals("#ffffff", m_calculator.checkForeground("opencms", "#ffffffff"));
        assertEquals(
            CmsColorContrastCalculator.INVALID_FOREGROUND,
            m_calculator.checkForeground("opencms", "#ffffffaa"));
        assertEquals(
            CmsColorContrastCalculator.INVALID_FOREGROUND,
            m_calculator.checkForeground("#b31b34aa", "#ffffff"));
        assertEquals(
            CmsColorContrastCalculator.INVALID_FOREGROUND,
            m_calculator.checkForeground("transparent", "#000"));
        assertEquals(
            CmsColorContrastCalculator.INVALID_FOREGROUND,
            m_calculator.checkForeground("ffffff", "transparent"));

        // Invalid input values
        assertEquals(
            CmsColorContrastCalculator.INVALID_FOREGROUND,
            m_calculator.checkForeground("not-a-color", "#ffffff"));
        assertEquals(
            CmsColorContrastCalculator.INVALID_FOREGROUND,
            m_calculator.checkForeground("#ffffff", "not-a-color"));
        assertEquals(CmsColorContrastCalculator.INVALID_FOREGROUND, m_calculator.checkForeground(null, null));
        assertEquals(CmsColorContrastCalculator.INVALID_FOREGROUND, m_calculator.checkForeground("", null));
        assertEquals(CmsColorContrastCalculator.INVALID_FOREGROUND, m_calculator.checkForeground("#12", "#ffffff"));
    }

    @Test
    public void testCheckForegroundList() {

        // Test list with compliant colors
        List<String> colors = Arrays.asList("#eee", "#000000", "#666666");
        assertEquals("#000000", m_calculator.checkForegroundList("#ffffff", colors)); // Should pick first compliant color

        // Test list with no compliant colors
        colors = Arrays.asList("#999999", "#aaaaaa", "#cccccc");
        assertEquals("#000000", m_calculator.checkForegroundList("#ffffff", colors)); // Should return black for white background

        // Test empty list
        assertEquals(
            CmsColorContrastCalculator.INVALID_FOREGROUND,
            m_calculator.checkForegroundList("#ffffff", Collections.emptyList()));

        // Test null list
        assertEquals(CmsColorContrastCalculator.INVALID_FOREGROUND, m_calculator.checkForegroundList("#ffffff", null));

        // Test list with invalid colors
        colors = Arrays.asList("invalid", "not-a-color", "#zzzzzz");
        assertEquals(
            CmsColorContrastCalculator.INVALID_FOREGROUND,
            m_calculator.checkForegroundList("#ffffff", colors));

        // Test list with mix of valid and invalid colors
        colors = Arrays.asList("invalid", "#444444ee", "#333", "not-a-color");
        assertEquals("#333333", m_calculator.checkForegroundList("#ffffff", colors)); // Should find the valid compliant color

        // Test list with mix of valid and invalid colors
        colors = Arrays.asList("invalid", "#444444ff", "#333", "not-a-color");
        assertEquals("#444444", m_calculator.checkForegroundList("#ffffff", colors)); // Should find the valid compliant color

        // Test list with valid but non-compliant colors
        colors = Arrays.asList("#777777", "#888888", "#999999");
        assertEquals("#000000", m_calculator.checkForegroundList("#ffffff", colors)); // Should return black for white background

        // Test with invalid background color
        colors = Arrays.asList("#000000", "#ffffff");
        assertEquals(
            CmsColorContrastCalculator.INVALID_FOREGROUND,
            m_calculator.checkForegroundList("invalid", colors));
    }

    @Test
    public void testGetContrast() {

        // Test cases from WebAIM with known contrast ratios
        assertContrast("#000000", "#ffffff", 21.0); // Black on White
        assertContrast("#ffffff", "#000000", 21.0); // White on Black
        assertContrast("#777777", "#ffffff", 4.48); // Grey on White
        assertContrast("#ff0000", "#ffffff", 4.0); // Red on White
        assertContrast("#0000ff", "#ffffff", 8.59); // Blue on White
        assertContrast("#ffff00", "#000000", 19.56); // Yellow on Black
        assertContrast("#008000", "#ffffff", 5.13); // Green on white

        // Test identical colors (must have 1.0 contrast)
        assertContrast("#ffffff", "#ffffff", 1.0);
        assertContrast("#000000", "#000000", 1.0);
        assertContrast("#ff0000", "#ff0000", 1.0);

        // Test some OpenCms colors
        assertContrast("#b31b34", "#ffffff", 6.72); // OpenCms red on white

        // Test invalid hex values
        assertContrast("not-a-color", "#ffffff", 0.0);
        assertContrast("#ffffff", "invalid", 0.0);
        assertContrast("#fff", "12345", 0.0);
        assertContrast("#12", "#ffffff", 0.0);
        assertContrast("#12345", "#ffffff", 0.0);
        assertContrast("", "", 0.0);
        assertContrast(null, "#ffffff", 0.0);
        assertContrast("#ffffff", null, 0.0);

        // Test with alpha channel, only 'ff' is allowed
        assertContrast("#b31b34ff", "#ffffff", 6.72);
        assertContrast("#b31b34", "#ffffffff", 6.72);
        assertContrast("#b31b34ff", "#ffffffff", 6.72);
        assertContrast("#b31b34aa", "#ffffffff", 0.0);
        assertContrast("#b31b34", "#ffffffaa", 0.0);

        // Test with named color
        assertContrast("#b31b34", "white", 6.72);
        assertContrast("opencms", "#ffffff", 6.72);
        assertContrast("opencms", "transparent", 0.0);
        assertContrast("transparent", "opencms", 0.0);
        assertContrast("transparent", "white", 0.0);
        assertContrast("transparent", "#ffffff", 0.0);
        assertContrast("transparent", "black", 0.0);
        assertContrast("transparent", "#000000", 0.0);
    }

    @Test
    public void testGetForeground() {

        // Test color suggestions for non-compliant colors
        assertEquals("#000000", m_calculator.getForeground("#ffffff")); // White bg -> Black text
        assertEquals("#ffffff", m_calculator.getForeground("#000000")); // Black bg -> White text
        assertEquals("#ffffff", m_calculator.getForeground("#0000ff")); // Blue bg -> White text
        assertEquals("#ffffff", m_calculator.getForeground("opencms")); // Blue bg -> White text

        // Test invalid inputs return red
        assertEquals(CmsColorContrastCalculator.INVALID_FOREGROUND, m_calculator.getForeground("invalid-color"));
        assertEquals(CmsColorContrastCalculator.INVALID_FOREGROUND, m_calculator.getForeground(null));
        assertEquals(CmsColorContrastCalculator.INVALID_FOREGROUND, m_calculator.getForeground(""));
        assertEquals(CmsColorContrastCalculator.INVALID_FOREGROUND, m_calculator.getForeground("#12")); // Too short
        assertEquals(CmsColorContrastCalculator.INVALID_FOREGROUND, m_calculator.getForeground("#12345")); // Wrong length
        assertEquals(CmsColorContrastCalculator.INVALID_FOREGROUND, m_calculator.getForeground("transparent"));
    }

    public void testHasSufficientContrast() {

        // Test WCAG AA compliance (4.5:1 minimum for normal text)
        assertTrue(m_calculator.hasSufficientContrast("#000000", "#ffffff")); // Black on White
        assertTrue(m_calculator.hasSufficientContrast("#0000ff", "#ffffff")); // Blue on White
        assertTrue(m_calculator.hasSufficientContrast("#ffffff", "#000000"));
        assertTrue(m_calculator.hasSufficientContrast("#fff", "#000"));
        assertFalse(m_calculator.hasSufficientContrast("#777777", "#ffffff")); // Grey on White (4.48:1)
        assertFalse(m_calculator.hasSufficientContrast("#ff0000", "#ffffff")); // Red on White (4.0:1)

        // Test common UI color combinations
        assertTrue(m_calculator.hasSufficientContrast("#ffffff", "#b31b34")); // OpenCms Red
        assertTrue(m_calculator.hasSufficientContrast("#ffffff", "#1a73e8")); // Google Blue
        assertTrue(m_calculator.hasSufficientContrast("#ffffff", "#dc3545")); // Bootstrap Danger
        assertFalse(m_calculator.hasSufficientContrast("#ffffff", "#007bff")); // Bootstrap Primary
        assertFalse(m_calculator.hasSufficientContrast("#ffffff", "#ffc107")); // Bootstrap Warning

        // Test very similar colors
        assertFalse(m_calculator.hasSufficientContrast("#fefefe", "#ffffff"));
        assertFalse(m_calculator.hasSufficientContrast("#010101", "#000000"));

        // Test with invalid hex values
        assertFalse(m_calculator.hasSufficientContrast("invalid", "#ffffff"));
        assertFalse(m_calculator.hasSufficientContrast("#ffffff", "invalid"));
        assertFalse(m_calculator.hasSufficientContrast("", "#000000"));
        assertFalse(m_calculator.hasSufficientContrast(null, "#000"));
        assertFalse(m_calculator.hasSufficientContrast("#fffzzz", "#000"));

        // Test with alpha channel, only 'ff' is allowed
        assertTrue(m_calculator.hasSufficientContrast("#ffffff", "#b31b34ff"));
        assertTrue(m_calculator.hasSufficientContrast("#ffffffff", "#b31b34"));
        assertTrue(m_calculator.hasSufficientContrast("#ffffffff", "#b31b34ff"));

        assertFalse(m_calculator.hasSufficientContrast("#ffffff", "#b31b34aa"));
        assertFalse(m_calculator.hasSufficientContrast("#ffffffaa", "#b31b34"));
        assertFalse(m_calculator.hasSufficientContrast("#ffffffaa", "#b31b34aa"));

        assertFalse(m_calculator.hasSufficientContrast("transparent", "#000000"));
        assertFalse(m_calculator.hasSufficientContrast("transparent", "#ffffff"));
    }

    @Test
    public void testIsValid() {

        assertTrue(m_calculator.isValid("#ffffff"));
        assertTrue(m_calculator.isValid("#fff"));
        assertTrue(m_calculator.isValid("#ffffffaa"));
        assertTrue(m_calculator.isValid("#ffffff  "));
        assertTrue(m_calculator.isValid("  #ffffff"));
        assertTrue(m_calculator.isValid("white"));
        assertTrue(m_calculator.isValid("white  "));

        assertTrue(m_calculator.isValid("#123"));
        assertTrue(m_calculator.isValid("#123456"));
        assertTrue(m_calculator.isValid("#12345678"));

        assertFalse(m_calculator.isValid("#1"));
        assertFalse(m_calculator.isValid("#12"));
        assertFalse(m_calculator.isValid("#1234"));
        assertFalse(m_calculator.isValid("#12345"));
        assertFalse(m_calculator.isValid("#1234567"));

        assertFalse(m_calculator.isValid("ffffff"));
        assertFalse(m_calculator.isValid("fff"));
        assertFalse(m_calculator.isValid("ffffffaa"));
        assertFalse(m_calculator.isValid("i-am-invalid"));
    }

    @Test
    public void testSuggestForeground() {

        // Test suggestions maintain WCAG compliance
        String suggestion = m_calculator.suggestForeground("#ffffff", "#777777");
        assertTrue(m_calculator.hasSufficientContrast("#ffffff", suggestion));

        suggestion = m_calculator.suggestForeground("#000000", "#444444");
        assertTrue(m_calculator.hasSufficientContrast("#000000", suggestion));

        String bgHex = "#dd00dd";
        String fgHex = "#808080";

        suggestion = m_calculator.suggestForeground(bgHex, fgHex);
        System.out.println("Fg " + fgHex + " on Bg " + bgHex + " > " + suggestion);
        assertTrue(m_calculator.hasSufficientContrast(bgHex, suggestion));

        bgHex = "#b31b34";
        suggestion = m_calculator.suggestForeground(bgHex, fgHex);
        System.out.println("Fg " + fgHex + " on Bg " + bgHex + " > " + suggestion);
        assertTrue(m_calculator.hasSufficientContrast(bgHex, suggestion));

        bgHex = "#00ffcc";
        suggestion = m_calculator.suggestForeground(bgHex, fgHex);
        System.out.println("Fg " + fgHex + " on Bg " + bgHex + " > " + suggestion);
        assertTrue(m_calculator.hasSufficientContrast(bgHex, suggestion));

        // Valid hex values
        assertEquals("#727272", m_calculator.suggestForeground("#ffffff", "#777777"));
        assertEquals("#767676", m_calculator.suggestForeground("#000000", "#444444"));

        // Invalid values
        assertEquals(
            CmsColorContrastCalculator.INVALID_FOREGROUND,
            m_calculator.suggestForeground("invalid", "#ffffff"));
        assertEquals(
            CmsColorContrastCalculator.INVALID_FOREGROUND,
            m_calculator.suggestForeground("#ffffff", "invalid"));
        assertEquals(CmsColorContrastCalculator.INVALID_FOREGROUND, m_calculator.suggestForeground(null, null));
        assertEquals(CmsColorContrastCalculator.INVALID_FOREGROUND, m_calculator.suggestForeground("#12", "#ffffff"));
        assertEquals(
            CmsColorContrastCalculator.INVALID_FOREGROUND,
            m_calculator.suggestForeground("#12345", "#ffffff"));
    }

    @Test
    public void testToHex() {

        assertEquals("#ffffff", m_calculator.toHex("#ffffff"));
        assertEquals("#ffffff", m_calculator.toHex("#fff"));
        assertEquals("#b31b34", m_calculator.toHex("opencms"));
        assertEquals("#ffffff", m_calculator.toHex("white"));
    }

    @Test
    public void testToRgb() {

        // Valid hex values
        assertArrayEquals(new int[] {255, 255, 255}, m_calculator.toRgb("#ffffff"));
        assertArrayEquals(new int[] {0, 0, 0}, m_calculator.toRgb("#000000"));
        assertArrayEquals(new int[] {255, 0, 0}, m_calculator.toRgb("#ff0000"));
        assertArrayEquals(new int[] {0, 255, 0}, m_calculator.toRgb("#00ff00"));
        assertArrayEquals(new int[] {0, 0, 255}, m_calculator.toRgb("#0000ff"));
        assertArrayEquals(new int[] {255, 255, 0}, m_calculator.toRgb("#ffff00  "));
        assertArrayEquals(new int[] {0, 255, 255}, m_calculator.toRgb("  #00ffff"));
        assertArrayEquals(new int[] {255, 0, 255}, m_calculator.toRgb("  #ff00ff  "));

        // Valid shorthand hex values
        assertArrayEquals(new int[] {255, 255, 255}, m_calculator.toRgb("#fff"));
        assertArrayEquals(new int[] {0, 0, 0}, m_calculator.toRgb("#000"));
        assertArrayEquals(new int[] {255, 0, 0}, m_calculator.toRgb("#f00"));
        assertArrayEquals(new int[] {0, 255, 0}, m_calculator.toRgb("#0f0"));
        assertArrayEquals(new int[] {0, 0, 255}, m_calculator.toRgb("#00f"));
        assertArrayEquals(new int[] {255, 255, 0}, m_calculator.toRgb("#ff0"));
        assertArrayEquals(new int[] {0, 255, 255}, m_calculator.toRgb("#0ff"));
        assertArrayEquals(new int[] {255, 0, 255}, m_calculator.toRgb("#f0f"));

        // Valid rgba hex values
        assertArrayEquals(new int[] {255, 255, 255, 0}, m_calculator.toRgb("#ffffff00"));
        assertArrayEquals(new int[] {255, 255, 255, 170}, m_calculator.toRgb("#ffffffaa"));
        assertArrayEquals(new int[] {0, 0, 0, 255}, m_calculator.toRgb("#000000ff"));
        assertArrayEquals(new int[] {255, 0, 0, 128}, m_calculator.toRgb("#ff000080"));

        // Named colors
        assertArrayEquals(new int[] {0, 0, 0}, m_calculator.toRgb("black"));
        assertArrayEquals(new int[] {255, 255, 255}, m_calculator.toRgb("white   "));
        assertArrayEquals(new int[] {255, 0, 0}, m_calculator.toRgb("red"));
        assertArrayEquals(new int[] {0, 128, 0}, m_calculator.toRgb("green"));
        assertArrayEquals(new int[] {0, 0, 255}, m_calculator.toRgb("blue"));
        assertArrayEquals(new int[] {255, 255, 0}, m_calculator.toRgb("   yellow"));
        assertArrayEquals(new int[] {0, 255, 255}, m_calculator.toRgb("cyan"));
        assertArrayEquals(new int[] {255, 0, 255}, m_calculator.toRgb("magenta"));
        assertArrayEquals(new int[] {128, 128, 128}, m_calculator.toRgb("  gray"));
        assertArrayEquals(new int[] {128, 128, 128}, m_calculator.toRgb("grey"));
        assertArrayEquals(new int[] {255, 255, 255, 0}, m_calculator.toRgb("transparent"));
        assertArrayEquals(new int[] {255, 255, 255, 0}, m_calculator.toRgb("transparent", true, false));
        // Invalid values
        assertNull(m_calculator.toRgb("not-a-color"));
        assertNull(m_calculator.toRgb("#12"));
        assertNull(m_calculator.toRgb("#12345"));
        assertNull(m_calculator.toRgb("123456"));
        assertNull(m_calculator.toRgb("#1234567"));
        assertNull(m_calculator.toRgb("#123456xx"));
        assertNull(m_calculator.toRgb("#xyz"));
        assertNull(m_calculator.toRgb("#zzzzzz"));
    }

    private void assertContrast(String color1, String color2, double expectedRatio) {

        double actualRatio = m_calculator.getContrast(color1, color2);
        assertEquals("Contrast ratio between " + color1 + " and " + color2, expectedRatio, actualRatio, 0.01);
    }
}
