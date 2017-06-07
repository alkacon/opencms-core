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

package org.opencms.gwt.client.ui.input.colorpicker;

/**
 * Definition of an color. <p>
 * */
public class CmsColor {

    /** Blue value [0-1]. */
    private float m_blue;
    /** Brightness value [0-1]. */
    private float m_bri;
    /** Green value [0-1]. */
    private float m_green;
    /** Hex value of RGB. */
    private String m_hex;
    /** Hue value [0-360]. */
    private float m_hue;
    /** Red value [0-1]. */
    private float m_red;
    /** Saturation value [0-1]. */
    private float m_sat;

    /**
     * Returns the integer of the blue component of the RGB.<p>
     * @return blue component
     */
    public int getBlue() {

        return (int)(m_blue * 255);
    }

    /**
     * Returns the integer of the green component of the RGB.<p>
     * @return green component
     */
    public int getGreen() {

        return (int)(m_green * 255);
    }

    /**
     * Returns the hexadecimal representation of the RGB.<p>
     * @return hexadecimal representation
     */
    public String getHex() {

        return m_hex.toUpperCase();
    }

    /**
     * Returns the integer of the hue component of the HSV.<p>
     * @return hue component
     */
    public int getHue() {

        return (int)m_hue;
    }

    /**
     * Returns the integer of the red component of the RGB.<p>
     * @return red component
     */
    public int getRed() {

        return (int)(m_red * 255);
    }

    /**
     * Returns the integer of the saturation component of the HSV.<p>
     * @return saturation component
     */
    public int getSaturation() {

        return (int)(m_sat * 100);
    }

    /**
     * Returns the integer of the value (brightness) component of the HSV.<p>
     * @return value component
     */
    public int getValue() {

        return (int)(m_bri * 100);
    }

    /**
     * Sets the hexadecimal representation of Red, Green and Blue.<p>
     * @param hex The hexadecimal string notation. It must be 6 or 3 letters long and consist of the characters 0-9 and A-F
     * @throws java.lang.Exception if something goes wrong
     */
    public void setHex(String hex) throws Exception {

        if (hex.length() == 6) {
            setRGB(
                Integer.parseInt(hex.substring(0, 2), 16),
                Integer.parseInt(hex.substring(2, 4), 16),
                Integer.parseInt(hex.substring(4, 6), 16));
        } else if (hex.length() == 3) {
            setRGB(
                Integer.parseInt(hex.substring(0, 1), 16),
                Integer.parseInt(hex.substring(1, 2), 16),
                Integer.parseInt(hex.substring(2, 3), 16));
        } else {
            setRGB(255, 255, 255);
        }
    }

    /**
     * Set the Hue, Saturation and Value (Brightness) variables.<p>
     *
     * @param hue hue - valid range is 0-359
     * @param sat saturation - valid range is 0-100
     * @param val brightness - valid range is 0-100
     * @throws java.lang.Exception if something goes wrong
     */
    public void setHSV(int hue, int sat, int val) throws Exception {

        if ((hue < 0) || (hue > 360)) {
            throw new Exception();
        }
        if ((sat < 0) || (sat > 100)) {
            throw new Exception();
        }
        if ((val < 0) || (val > 100)) {
            throw new Exception();
        }

        m_hue = hue;
        m_sat = (float)sat / 100;
        m_bri = (float)val / 100;

        HSVtoRGB(m_hue, m_sat, m_bri);

        setHex();
    }

    /**
     * Sets the Red, Green, and Blue color variables.<p>
     *
     * @param red valid range is 0-255
     * @param green valid range is 0-255
     * @param blue valid range is 0-255
     * @throws java.lang.Exception if something goes wrong
     */
    public void setRGB(int red, int green, int blue) throws Exception {

        if ((red < 0) || (red > 255)) {
            throw new Exception();
        }
        if ((green < 0) || (green > 255)) {
            throw new Exception();
        }
        if ((blue < 0) || (blue > 255)) {
            throw new Exception();
        }

        m_red = (float)red / 255;
        m_green = (float)green / 255;
        m_blue = (float)blue / 255;

        RGBtoHSV(m_red, m_green, m_blue);

        setHex();
    }

    /**
     * Converts the HSV into the RGB.<p>
     * @param hue value
     * @param sat the saturation value
     * @param bri the brightness value
     */
    private void HSVtoRGB(float hue, float sat, float bri) {

        int i;
        float f;
        float p;
        float q;
        float t;
        if (sat == 0) {
            m_red = bri;
            m_green = bri;
            m_blue = bri;
            return;
        }
        hue /= 60;
        i = (int)Math.floor(hue);
        f = hue - i;
        p = bri * (1 - sat);
        q = bri * (1 - (sat * f));
        t = bri * (1 - (sat * (1 - f)));
        switch (i) {
            case 0:
                m_red = bri;
                m_green = t;
                m_blue = p;
                break;
            case 1:
                m_red = q;
                m_green = bri;
                m_blue = p;
                break;
            case 2:
                m_red = p;
                m_green = bri;
                m_blue = t;
                break;
            case 3:
                m_red = p;
                m_green = q;
                m_blue = bri;
                break;
            case 4:
                m_red = t;
                m_green = p;
                m_blue = bri;
                break;
            default: // case 5:
                m_red = bri;
                m_green = p;
                m_blue = q;
                break;
        }
    }

    /**
     * Calculates the largest value between the three inputs.<p>
     * @param first value
     * @param second value
     * @param third value
     * @return the largest value between the three inputs
     */
    private float MAX(float first, float second, float third) {

        float max = Integer.MIN_VALUE;
        if (first > max) {
            max = first;
        }
        if (second > max) {
            max = second;
        }
        if (third > max) {
            max = third;
        }
        return max;
    }

    /**
     * Calculates the smallest value between the three inputs.
      * @param first value
     * @param second value
     * @param third value
     * @return the smallest value between the three inputs
     */
    private float MIN(float first, float second, float third) {

        float min = Integer.MAX_VALUE;
        if (first < min) {
            min = first;
        }
        if (second < min) {
            min = second;
        }
        if (third < min) {
            min = third;
        }
        return min;
    }

    /**
     * Converts the RGB into the HSV.<p>
     * @param red value
     * @param green value
     * @param blue value
     */
    private void RGBtoHSV(float red, float green, float blue) {

        float min = 0;
        float max = 0;
        float delta = 0;

        min = MIN(red, green, blue);
        max = MAX(red, green, blue);

        m_bri = max; // v

        delta = max - min;

        if (max != 0) {
            m_sat = delta / max; // s
        } else {
            m_sat = 0;
            m_hue = 0;
            return;
        }

        if (delta == 0) {
            m_hue = 0;
            return;
        }

        if (red == max) {
            m_hue = (green - blue) / delta;
        } else if (green == max) {
            m_hue = 2 + ((blue - red) / delta);
        } else {
            m_hue = 4 + ((red - green) / delta);
        }

        m_hue *= 60;

        if (m_hue < 0) {
            m_hue += 360;
        }
    }

    /**
     * Converts from RGB to Hexadecimal notation.
     */
    private void setHex() {

        String hRed = Integer.toHexString(getRed());
        String hGreen = Integer.toHexString(getGreen());
        String hBlue = Integer.toHexString(getBlue());

        if (hRed.length() == 0) {
            hRed = "00";
        }
        if (hRed.length() == 1) {
            hRed = "0" + hRed;
        }
        if (hGreen.length() == 0) {
            hGreen = "00";
        }
        if (hGreen.length() == 1) {
            hGreen = "0" + hGreen;
        }
        if (hBlue.length() == 0) {
            hBlue = "00";
        }
        if (hBlue.length() == 1) {
            hBlue = "0" + hBlue;
        }

        m_hex = hRed + hGreen + hBlue;
    }
}