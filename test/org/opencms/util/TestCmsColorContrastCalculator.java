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

import org.opencms.test.OpenCmsTestCase;

import org.junit.Test;

/**
 * Test cases for ColorContrastCalculator focusing on WCAG 2.2 compliance.
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
    public void testCommonUIColors() {

        // Test common UI color combinations
        assertTrue(m_calculator.getHasSufficientContrast("#ffffff", "#1a73e8")); // Google Blue
        assertFalse(m_calculator.getHasSufficientContrast("#ffffff", "#007bff")); // Bootstrap Primary
        assertTrue(m_calculator.getHasSufficientContrast("#ffffff", "#dc3545")); // Bootstrap Danger
        assertFalse(m_calculator.getHasSufficientContrast("#ffffff", "#ffc107")); // Bootstrap Warning
    }

    @Test
    public void testEdgeCases() {

        // Test identical colors (should have 1:1 contrast)
        assertEquals(1.0, m_calculator.getContrast("#ffffff", "#ffffff"), 0.01);
        assertEquals(1.0, m_calculator.getContrast("#000000", "#000000"), 0.01);
        assertEquals(1.0, m_calculator.getContrast("#ff0000", "#ff0000"), 0.01);

        // Test very similar colors
        assertFalse(m_calculator.getHasSufficientContrast("#fefefe", "#ffffff"));
        assertFalse(m_calculator.getHasSufficientContrast("#010101", "#000000"));
    }

    @Test
    public void testForegroundCheck() {

        // Test that compliant colors are preserved
        assertEquals("#000000", m_calculator.getForegroundCheck("#ffffff", "#000000")); // Keep black on white
        assertEquals("#ffffff", m_calculator.getForegroundCheck("#000000", "#ffffff")); // Keep white on black
        assertEquals("#000000", m_calculator.getForegroundCheck("#ffff00", "#000000")); // Keep black on yellow

        // Test that non-compliant colors are replaced with black or white
        assertEquals("#000000", m_calculator.getForegroundCheck("#ffffff", "#777777")); // Replace gray with black on white
        assertEquals("#ffffff", m_calculator.getForegroundCheck("#000000", "#444444")); // Replace gray with white on black
        assertEquals("#ffffff", m_calculator.getForegroundCheck("#0000ff", "#aaaaaa")); // Replace gray with white on blue

        // Test edge cases
        assertEquals("#000000", m_calculator.getForegroundCheck("#ffffff", "#ffffff")); // White on white -> black
        assertEquals("#ffffff", m_calculator.getForegroundCheck("#000000", "#000000")); // Black on black -> white

        // Test with various background colors
        String fgColor = "#777777"; // A non-compliant gray
        assertEquals("#000000", m_calculator.getForegroundCheck("#ffff00", fgColor)); // Yellow bg -> black
        assertEquals("#ffffff", m_calculator.getForegroundCheck("#0000ff", fgColor)); // Blue bg -> white
        assertEquals("#000000", m_calculator.getForegroundCheck("#00ffcc", fgColor)); // Cyan bg -> black
    }

    @Test
    public void testForegroundSuggestions() {

        // Test color suggestions for non-compliant colors
        assertEquals("#000000", m_calculator.getForeground("#ffffff")); // White bg -> Black text
        assertEquals("#ffffff", m_calculator.getForeground("#000000")); // Black bg -> White text
        assertEquals("#ffffff", m_calculator.getForeground("#0000ff")); // Blue bg -> White text

        // Test suggestions maintain WCAG compliance
        String suggestion = m_calculator.getForegroundSuggest("#ffffff", "#777777");
        assertTrue(m_calculator.getHasSufficientContrast("#ffffff", suggestion));

        suggestion = m_calculator.getForegroundSuggest("#000000", "#444444");
        assertTrue(m_calculator.getHasSufficientContrast("#000000", suggestion));

        String bgHex = "#dd00dd";
        String fgHex = "#808080";

        suggestion = m_calculator.getForegroundSuggest(bgHex, fgHex);
        System.out.println("Fg " + fgHex + " on Bg " + bgHex + " > " + suggestion);
        assertTrue(m_calculator.getHasSufficientContrast(bgHex, suggestion));

        bgHex = "#b31b34";
        suggestion = m_calculator.getForegroundSuggest(bgHex, fgHex);
        System.out.println("Fg " + fgHex + " on Bg " + bgHex + " > " + suggestion);
        assertTrue(m_calculator.getHasSufficientContrast(bgHex, suggestion));

        bgHex = "#00ffcc";
        suggestion = m_calculator.getForegroundSuggest(bgHex, fgHex);
        System.out.println("Fg " + fgHex + " on Bg " + bgHex + " > " + suggestion);
        assertTrue(m_calculator.getHasSufficientContrast(bgHex, suggestion));
    }

    @Test
    public void testInvalidHexValues() {
        // Test invalid hex values
        assertEquals(0.0, m_calculator.getContrast("not-a-color", "#ffffff"));
        assertEquals(0.0, m_calculator.getContrast("#ffffff", "invalid"));
        assertEquals(0.0, m_calculator.getContrast("#fff", "12345"));
        assertEquals(0.0, m_calculator.getContrast("#12", "#ffffff"));
        assertEquals(0.0, m_calculator.getContrast("#12345", "#ffffff"));
        assertEquals(0.0, m_calculator.getContrast("", ""));
        assertEquals(0.0, m_calculator.getContrast(null, "#ffffff"));
        assertEquals(0.0, m_calculator.getContrast("#ffffff", null));

        assertFalse(m_calculator.getHasSufficientContrast("", "#000000"));
        assertFalse(m_calculator.getHasSufficientContrast(null, "#000"));
        assertFalse(m_calculator.getHasSufficientContrast("#fffzzz", "#000"));

        // Ensure valid hex values still work
        assertTrue(m_calculator.getHasSufficientContrast("#ffffff", "#000000"));
        assertTrue(m_calculator.getHasSufficientContrast("#fff", "#000"));
    }

    @Test
    public void testInvalidInputHandling() {
        // Test invalid inputs return red (#ff0000)
        assertEquals("#ff0000", m_calculator.getForeground("invalid-color"));
        assertEquals("#ff0000", m_calculator.getForeground(null));
        assertEquals("#ff0000", m_calculator.getForeground(""));
        assertEquals("#ff0000", m_calculator.getForeground("#12")); // Too short
        assertEquals("#ff0000", m_calculator.getForeground("#12345")); // Wrong length

        assertEquals("#ff0000", m_calculator.getForegroundCheck("not-a-color", "#ffffff"));
        assertEquals("#ff0000", m_calculator.getForegroundCheck("#ffffff", "not-a-color"));
        assertEquals("#ff0000", m_calculator.getForegroundCheck(null, null));
        assertEquals("#ff0000", m_calculator.getForegroundCheck("", null));
        assertEquals("#ff0000", m_calculator.getForegroundCheck("#12", "#ffffff"));

        assertEquals("#ff0000", m_calculator.getForegroundSuggest("invalid", "#ffffff"));
        assertEquals("#ff0000", m_calculator.getForegroundSuggest("#ffffff", "invalid"));
        assertEquals("#ff0000", m_calculator.getForegroundSuggest(null, null));
        assertEquals("#ff0000", m_calculator.getForegroundSuggest("#12", "#ffffff"));
        assertEquals("#ff0000", m_calculator.getForegroundSuggest("#12345", "#ffffff"));
    }

    @Test
    public void testKnownContrastRatios() {

        // Test cases from WebAIM with known contrast ratios
        assertContrast("#000000", "#ffffff", 21.0); // Black on White
        assertContrast("#ffffff", "#000000", 21.0); // White on Black
        assertContrast("#777777", "#ffffff", 4.48); // Grey on White
        assertContrast("#ff0000", "#ffffff", 4.0); // Red on White
        assertContrast("#0000ff", "#ffffff", 8.59); // Blue on White
        assertContrast("#ffff00", "#000000", 19.56); // Yellow on Black
        assertContrast("#008000", "#ffffff", 5.13); // Green on White
        // Test some OpenCms colors
        assertContrast("#b31b34", "#ffffff", 6.72); // OpenCms red on white
    }

    @Test
    public void testWCAGCompliance() {

        // Test WCAG AA compliance (4.5:1 minimum for normal text)
        assertTrue(m_calculator.getHasSufficientContrast("#000000", "#ffffff")); // Black on White
        assertTrue(m_calculator.getHasSufficientContrast("#0000ff", "#ffffff")); // Blue on White
        assertFalse(m_calculator.getHasSufficientContrast("#777777", "#ffffff")); // Grey on White (4.48:1)
        assertFalse(m_calculator.getHasSufficientContrast("#ff0000", "#ffffff")); // Red on White (4.0:1)
    }

    private void assertContrast(String color1, String color2, double expectedRatio) {

        double actualRatio = m_calculator.getContrast(color1, color2);
        assertEquals("Contrast ratio between " + color1 + " and " + color2, expectedRatio, actualRatio, 0.01);
    }
}
