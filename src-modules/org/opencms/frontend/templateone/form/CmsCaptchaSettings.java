/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsCaptchaSettings.java,v $
 * Date   : $Date: 2005/10/10 16:11:12 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.frontend.templateone.form;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.awt.Color;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * Stores the settings to render captcha images.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsCaptchaSettings {

    /** The image width in pixels. */
    private int m_imageWidth;
    
    /** The image height in pixels. */
    private int m_imageHeight;
    
    /** minimum font size in pixels. */
    private int m_minFontSize;
    
    /** The maximum font size in pixels. */
    private int m_maxFontSize;
    
    /** The minimum phrase length. */
    private int m_minPhraseLength;
    
    /** The maximum phrase length. */
    private int m_maxPhraseLength;
    
    /** The font color. */
    private Color m_fontColor;
    
    /** The the background color. */
    private Color m_backgroundColor;
    
    /** An ID to store the settings in a map. */
    private String m_key;
    
    /** Request parameter for the image width. */
    public static final String C_PARAM_IMAGE_WIDTH = "w";
    
    /** Request parameter for the image height. */
    public static final String C_PARAM_IMAGE_HEIGHT = "h";
    
    /** Request parameter for the min. font size. */
    public static final String C_PARAM_MIN_FONT_SIZE = "minfs";
    
    /** Request parameter for the max. font size. */
    public static final String C_PARAM_MAX_FONT_SIZE = "maxfs";
    
    /** Request parameter for the min phrase length. */
    public static final String C_PARAM_MIN_PHRASE_LENGTH = "minpl";
    
    /** Request parameter for the max phrase length. */
    public static final String C_PARAM_MAX_PHRASE_LENGTH = "maxpl";
    
    /** Request parameter for the font color. */
    public static final String C_PARAM_FONT_COLOR = "fcol";
    
    /** Request parameter for the background color. */
    public static final String C_PARAM_BACKGROUND_COLOR = "bgcol";
    
    /**
     * Creates captcha settings with default settings as used in the JCaptcha lib.<p>
     */
    public CmsCaptchaSettings() {
        
        m_imageWidth = 150;
        m_imageHeight = 50;

        m_minFontSize = 40;
        m_maxFontSize = 40;

        m_minPhraseLength = 6;
        m_maxPhraseLength = 7;
        
        m_fontColor = Color.BLACK;
        m_backgroundColor = Color.WHITE;
        
        m_key = createKey();
    }
    
    /**
     * Creates new captcha settings from an XML content.<p>
     * 
     * @param cms the current user's Cms object
     * @param content the XML content of the form
     * @param locale the current locale
     */
    public CmsCaptchaSettings(CmsObject cms, CmsXmlContent content, Locale locale) {
        
        I_CmsXmlContentValue xmlFormCaptcha = content.getValue(CmsForm.NODE_CAPTCHA, locale);
        String xPathFormCaptcha = xmlFormCaptcha.getPath() + "/";
        
        // image width
        String stringValue = content.getStringValue(cms, xPathFormCaptcha + CmsForm.NODE_CAPTCHA_IMAGEWIDTH, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_imageWidth = Integer.parseInt(stringValue);
        } else {
            m_imageWidth = 150;
        }
        
        // image height
        stringValue = content.getStringValue(cms, xPathFormCaptcha + CmsForm.NODE_CAPTCHA_IMAGEHEIGHT, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_imageHeight = Integer.parseInt(stringValue);
        } else {
            m_imageHeight = 50;
        }
        
        // min. phrase length
        stringValue = content.getStringValue(cms, xPathFormCaptcha + CmsForm.NODE_CAPTCHA_MIN_PHRASE_LENGTH, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_minPhraseLength = Integer.parseInt(stringValue);
        } else {
            m_minPhraseLength = 6;
        }
        
        // max. phrase length
        stringValue = content.getStringValue(cms, xPathFormCaptcha + CmsForm.NODE_CAPTCHA_MAX_PHRASE_LENGTH, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_maxPhraseLength = Integer.parseInt(stringValue);
        } else {
            m_maxPhraseLength = 7;
        }
        
        // min. font size
        stringValue = content.getStringValue(cms, xPathFormCaptcha + CmsForm.NODE_CAPTCHA_MIN_FONT_SIZE, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_minFontSize = Integer.parseInt(stringValue);
        } else {
            m_minFontSize = 40;
        }
        
        // max. font size
        stringValue = content.getStringValue(cms, xPathFormCaptcha + CmsForm.NODE_CAPTCHA_MAX_FONT_SIZE, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_maxFontSize = Integer.parseInt(stringValue);
        } else {
            m_maxFontSize = 40;
        }
        
        // font color
        stringValue = content.getStringValue(cms, xPathFormCaptcha + CmsForm.NODE_CAPTCHA_FONTCOLOR, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setFontColor(stringValue);
        } else {
            m_fontColor = Color.BLACK;
        }
        
        // background color
        stringValue = content.getStringValue(cms, xPathFormCaptcha + CmsForm.NODE_CAPTCHA_BACKGROUNDCOLOR, locale);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setBackgroundColor(stringValue);
        } else {
            m_backgroundColor = Color.WHITE;
        }
        
        m_key = createKey();
    }
    
    /**
     * Creates new captcha settings from request parameters.<p>
     * 
     * @param jsp a Cms JSP page
     */
    public CmsCaptchaSettings(CmsJspActionElement jsp) {
        
        HttpServletRequest request = jsp.getRequest();
        
        // image width
        String stringValue = request.getParameter(C_PARAM_IMAGE_WIDTH);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_imageWidth = Integer.parseInt(stringValue);
        } else {
            m_imageWidth = 150;
        }
        
        // image height
        stringValue = request.getParameter(C_PARAM_IMAGE_HEIGHT);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_imageHeight = Integer.parseInt(stringValue);
        } else {
            m_imageHeight = 50;
        }
        
        // min. phrase length
        stringValue = request.getParameter(C_PARAM_MIN_PHRASE_LENGTH);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_minPhraseLength = Integer.parseInt(stringValue);
        } else {
            m_minPhraseLength = 6;
        }
        
        // max. phrase length
        stringValue = request.getParameter(C_PARAM_MAX_PHRASE_LENGTH);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_maxPhraseLength = Integer.parseInt(stringValue);
        } else {
            m_maxPhraseLength = 7;
        }
        
        // min. font size
        stringValue = request.getParameter(C_PARAM_MIN_FONT_SIZE);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_minFontSize = Integer.parseInt(stringValue);
        } else {
            m_minFontSize = 40;
        }
        
        // max. font size
        stringValue = request.getParameter(C_PARAM_MAX_FONT_SIZE);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_maxFontSize = Integer.parseInt(stringValue);
        } else {
            m_maxFontSize = 40;
        }
        
        // font color
        stringValue = request.getParameter(C_PARAM_FONT_COLOR);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            stringValue = CmsEncoder.unescape(stringValue, jsp.getRequestContext().getEncoding());
            setFontColor(stringValue);
        } else {
            m_fontColor = Color.BLACK;
        }
        
        // background color
        stringValue = request.getParameter(C_PARAM_BACKGROUND_COLOR);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            stringValue = CmsEncoder.unescape(stringValue, jsp.getRequestContext().getEncoding());
            setBackgroundColor(stringValue);
        } else {
            m_backgroundColor = Color.WHITE;
        }
        
        m_key = createKey();
    }
    
    /**
     * Returns the background color.<p>
     *
     * @return the background color
     */
    public Color getBackgroundColor() {

        return m_backgroundColor;
    }
    
    /**
     * Returns the background color as a hex string.<p>
     *
     * @return the background color as a hex string
     */
    public String getBackgroundColorString() {
        
        StringBuffer buf = new StringBuffer();
        
        buf.append("#");
        buf.append(toHexString(m_backgroundColor.getRed()));
        buf.append(toHexString(m_backgroundColor.getGreen()));
        buf.append(toHexString(m_backgroundColor.getBlue()));
        
        return buf.toString();
    }
    
    /**
     * Sets the background color.<p>
     *
     * @param backgroundColor the background color to set
     */
    public void setBackgroundColor(Color backgroundColor) {

        m_backgroundColor = backgroundColor;
    }
    
    /**
     * Sets the background color as a hex string.<p>
     *
     * @param backgroundColor the background color to set as a hex string
     */
    public void setBackgroundColor(String backgroundColor) {
        
        if (backgroundColor.startsWith("#")) {
            backgroundColor = backgroundColor.substring(1);
        }
        
        m_backgroundColor = new Color(Integer.valueOf(backgroundColor, 16).intValue());
    }
    
    /**
     * Returns the font color.<p>
     *
     * @return the font color
     */
    public Color getFontColor() {

        return m_fontColor;
    }
    
    /**
     * Returns the font color as a hex string.<p>
     *
     * @return the font color as a hex string
     */
    public String getFontColorString() {
        
        StringBuffer buf = new StringBuffer();
        
        buf.append("#");
        buf.append(toHexString(m_fontColor.getRed()));
        buf.append(toHexString(m_fontColor.getGreen()));
        buf.append(toHexString(m_fontColor.getBlue()));
        
        return buf.toString();
    }
    
    /**
     * Sets the font color.<p>
     *
     * @param fontColor the font color to set
     */
    public void setFontColor(Color fontColor) {

        m_fontColor = fontColor;
    }
    
    /**
     * Sets the font color as a hex string.<p>
     *
     * @param fontColor the font color to set as a hex string
     */
    public void setFontColor(String fontColor) {
        
        if (fontColor.startsWith("#")) {
            fontColor = fontColor.substring(1);
        }
        
        m_fontColor = new Color(Integer.valueOf(fontColor, 16).intValue());
    }
    
    /**
     * Returns the image height.<p>
     *
     * @return the image height
     */
    public int getImageHeight() {

        return m_imageHeight;
    }
    
    /**
     * Sets the image height.<p>
     *
     * @param imageHeight the image height to set
     */
    public void setImageHeight(int imageHeight) {

        m_imageHeight = imageHeight;
    }
    
    /**
     * Returns the image width.<p>
     *
     * @return the image width
     */
    public int getImageWidth() {

        return m_imageWidth;
    }
    
    /**
     * Sets the image width.<p>
     *
     * @param imageWidth the image width to set
     */
    public void setImageWidth(int imageWidth) {

        m_imageWidth = imageWidth;
    }
    
    /**
     * Returns the max. font size.<p>
     *
     * @return the max. font size
     */
    public int getMaxFontSize() {

        return m_maxFontSize;
    }
    
    /**
     * Sets the max. font size.<p>
     *
     * @param maxFontSize the max. font size to set
     */
    public void setMaxFontSize(int maxFontSize) {

        m_maxFontSize = maxFontSize;
    }
    
    /**
     * Returns the max. phrase length.<p>
     *
     * @return the max. phrase length
     */
    public int getMaxPhraseLength() {

        return m_maxPhraseLength;
    }
    
    /**
     * Sets the max. phrase length.<p>
     *
     * @param maxPhraseLength the max. phrase length to set
     */
    public void setMaxPhraseLength(int maxPhraseLength) {

        m_maxPhraseLength = maxPhraseLength;
    }
    
    /**
     * Returns the min. font size.<p>
     *
     * @return the min. font size
     */
    public int getMinFontSize() {

        return m_minFontSize;
    }
    
    /**
     * Sets the min. font size.<p>
     *
     * @param minFontSize the min. font size to set
     */
    public void setMinFontSize(int minFontSize) {

        m_minFontSize = minFontSize;
    }
    
    /**
     * Returns the min. phrase length.<p>
     *
     * @return the min. phrase length
     */
    public int getMinPhraseLength() {

        return m_minPhraseLength;
    }
    
    /**
     * Sets the min. phrase length.<p>
     *
     * @param minPhraseLength the min. phrase length to set
     */
    public void setMinPhraseLength(int minPhraseLength) {

        m_minPhraseLength = minPhraseLength;
    }
    
    /**
     * Creates a key to store the settings in a map.<p>
     * 
     * @return a key to store the settings in a map
     */
    private String createKey() {
        
        StringBuffer buf = new StringBuffer();
        
        buf.append(m_imageWidth);
        buf.append("-");
        buf.append(m_imageHeight);
        buf.append("-");
        buf.append(m_minFontSize);
        buf.append("-");
        buf.append(m_maxFontSize);
        buf.append("-");
        buf.append(m_minPhraseLength);
        buf.append("-");
        buf.append(m_maxPhraseLength);
        buf.append("-");
        buf.append(m_fontColor.toString());
        buf.append("-");
        buf.append(m_backgroundColor.toString());
        
        return buf.toString();
    }
    
    /**
     * Returns the key to store the settings in a map.<p>
     * 
     * @return the key to store the settings in a map
     */
    public String getKey() {
        
        return m_key;
    }
    
    /**
     * Creates a request parameter string from including all captcha settings.<p>
     * 
     * @param jsp the Cms JSP page
     * @return a request parameter string from including all captcha settings
     */
    public String toRequestParams(CmsJspActionElement jsp) {
        
        StringBuffer buf = new StringBuffer();
        
        buf.append(C_PARAM_IMAGE_WIDTH).append("=").append(m_imageWidth);
        buf.append("&").append(C_PARAM_IMAGE_HEIGHT).append("=").append(m_imageHeight);
        buf.append("&").append(C_PARAM_MIN_FONT_SIZE).append("=").append(m_minFontSize);
        buf.append("&").append(C_PARAM_MAX_FONT_SIZE).append("=").append(m_maxFontSize);
        buf.append("&").append(C_PARAM_MIN_PHRASE_LENGTH).append("=").append(m_minPhraseLength);
        buf.append("&").append(C_PARAM_MAX_PHRASE_LENGTH).append("=").append(m_maxPhraseLength);
        buf.append("&").append(C_PARAM_FONT_COLOR).append("=").append(CmsEncoder.escape(getFontColorString(), jsp.getRequestContext().getEncoding()));
        buf.append("&").append(C_PARAM_BACKGROUND_COLOR).append("=").append(CmsEncoder.escape(getBackgroundColorString(), jsp.getRequestContext().getEncoding()));

        return buf.toString();
    }
    
    /**
     * Converts a color range of a color into a hex string.<p>
     * 
     * @param colorRange the color range of a color
     * @return the hex string of the color range
     */
    private String toHexString(int colorRange) {
        
        if (colorRange < 10) {
            return "0" + Integer.toHexString(colorRange);
        } else {
            return Integer.toHexString(colorRange);
        }
    }
    
}
