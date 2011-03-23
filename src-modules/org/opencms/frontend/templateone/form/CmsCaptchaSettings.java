/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/frontend/templateone/form/CmsCaptchaSettings.java,v $
 * Date   : $Date: 2011/03/23 14:50:48 $
 * Version: $Revision: 1.15 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsEncoder;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Stores the settings to render captcha images.<p>
 * 
 * @author Thomas Weckert
 * @author Achim Westermann
 * 
 * @version $Revision: 1.15 $
 */
public final class CmsCaptchaSettings implements Cloneable {

    /** Request parameter for the background color. */
    public static final String C_PARAM_BACKGROUND_COLOR = "bgcol";

    /** Request parameter for the min phrase length. */
    public static final String C_PARAM_CHARACTERS = "crs";

    /** Request parameter for the filter amplitude. */
    public static final String C_PARAM_FILTER_AMPLITUDE = "famplit";

    /** Request parameter for the filter amplitude. */
    public static final String C_PARAM_FILTER_WAVE_LENGTH = "fwavlen";

    /** Request parameter for the font color. */
    public static final String C_PARAM_FONT_COLOR = "fcol";

    /** Request parameter for the font color. */
    public static final String C_PARAM_HOLES_PER_GLYPH = "holes";

    /** Request parameter for the image height. */
    public static final String C_PARAM_IMAGE_HEIGHT = "h";

    /** Request parameter for the image width. */
    public static final String C_PARAM_IMAGE_WIDTH = "w";

    /** Request parameter for the max. font size. */
    public static final String C_PARAM_MAX_FONT_SIZE = "maxfs";

    /** Request parameter for the max phrase length. */
    public static final String C_PARAM_MAX_PHRASE_LENGTH = "maxpl";

    /** Request parameter for the min. font size. */
    public static final String C_PARAM_MIN_FONT_SIZE = "minfs";

    /** Request parameter for the min phrase length. */
    public static final String C_PARAM_MIN_PHRASE_LENGTH = "minpl";

    /** Request parameter for the min phrase length. */
    public static final String C_PARAM_PRESET = "prst";

    /** Request parameter for the min phrase length. */
    public static final String C_PARAM_USE_BACKGROUND_IMAGE = "bgimg";

    /** Configuration node name for the optional captcha background color. */
    public static final String NODE_CAPTCHAPRESET_BACKGROUNDCOLOR = "BackgroundColor";

    /** Configuration node name for the field value node. */
    public static final String NODE_CAPTCHAPRESET_FILTER_AMPLITUDE = "FilterAmplitude";

    /** Configuration node name for the optional captcha image holes per glyph. */
    public static final String NODE_CAPTCHAPRESET_FILTER_WAVELENGTH = "FilterWaveLength";

    /** Configuration node name for the optional captcha font color. */
    public static final String NODE_CAPTCHAPRESET_FONTCOLOR = "FontColor";

    /** Configuration node name for the optional captcha image holes per glyph. */
    public static final String NODE_CAPTCHAPRESET_HOLESPERGLYPH = "HolesPerGlyph";

    /** Configuration node name for the optional captcha image height. */
    public static final String NODE_CAPTCHAPRESET_IMAGEHEIGHT = "ImageHeight";

    /** Configuration node name for the optional captcha image width. */
    public static final String NODE_CAPTCHAPRESET_IMAGEWIDTH = "ImageWidth";

    /** Configuration node name for the optional captcha max. font size. */
    public static final String NODE_CAPTCHAPRESET_MAX_FONT_SIZE = "MaxFontSize";

    /** Configuration node name for the optional captcha max. phrase length. */
    public static final String NODE_CAPTCHAPRESET_MAX_PHRASE_LENGTH = "MaxPhraseLength";

    /** Configuration node name for the optional captcha min. font size. */
    public static final String NODE_CAPTCHAPRESET_MIN_FONT_SIZE = "MinFontSize";

    /** Configuration node name for the optional captcha min. phrase length. */
    public static final String NODE_CAPTCHAPRESET_MIN_PHRASE_LENGTH = "MinPhraseLength";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsCaptchaSettings.class);

    /** The the background color. */
    private Color m_backgroundColor = Color.WHITE;

    /** The string containing the characters to use for word generation. */
    private String m_characterPool = "abcdefghiklmnoprstuvwxyz";

    /** The filter amplitude for the water filter that bends the text. */
    private int m_filterAmplitude = 2;

    /** The filter wave length for the water filter that bends the text. */
    private int m_filterWaveLength = 100;

    /** The font color. */
    private Color m_fontColor = Color.BLACK;

    /** The amount of holes per glyph. */
    private Integer m_holesPerGlyp = new Integer(0);

    /** The image height in pixels. */
    private int m_imageHeight = 50;

    /** The image width in pixels. */
    private int m_imageWidth = 150;

    /** The maximum font size in pixels. */
    private int m_maxFontSize = 40;

    /** The maximum phrase length. */
    private int m_maxPhraseLength = 5;

    /** minimum font size in pixels. */
    private int m_minFontSize = 30;

    /** The minimum phrase length. */
    private int m_minPhraseLength = 5;

    /** The map of request parameters. */
    private Map m_parameterMap;

    /**
     * The path to the preset configuration (captchapreset) that has been used to initialize these
     * settings. This is read only, as the path is internally read from a nested CmsForm/FormCaptcha
     * XML content.
     */
    private String m_presetPath = "factory defaults (classfile)";

    /** The flag that decides wethter a background image or a background color is used. */
    private boolean m_useBackgroundImage = true;

    /**
     * Private constructor for the clone method.<p>
     * 
     * May only be called from {@link #clone()} as that method guarantees to install the default
     * value from the master captcha settings.<p>
     */
    private CmsCaptchaSettings() {

        // nop
    }

    /**
     * Constructor that will use request parameters to init theses settings.<p>
     * 
     * @param jsp the jsp context object
     */
    private CmsCaptchaSettings(CmsJspActionElement jsp) {

        init(jsp);
    }

    /**
     * Returns a clone of the singleton instance of the
     * <em>"master"</em>  <code>CmsCaptchaSettings</code> and potential overridden values from
     * the request context.<p>
     * 
     * The <em>"master"</em>  <code>CmsCaptchaSettings</code> are read from an XML content that
     * contains the global defaults.<p>
     * 
     * @param jsp used to potentially access the XML content with the default captcha settings and
     *            to read overriden values from the request parameters.
     * 
     * @return a clone of the singleton instance of the
     *         <em>"master"</em> <code>CmsCaptchaSettings</code>.
     */
    public static CmsCaptchaSettings getInstance(CmsJspActionElement jsp) {

        CmsCaptchaSettings result = new CmsCaptchaSettings(jsp);
        return (CmsCaptchaSettings)result.clone();
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
     * Returns the filter amplitude for the water filter that bends the text.<p>
     * 
     * @return the filter amplitude for the water filter that bends the text.
     */
    public int getFilterAmplitude() {

        return m_filterAmplitude;
    }

    /**
     * Returns the filter wave length for the water filter that bends the text.<p>
     * 
     * @return the filter wave length for the water filter that bends the text.
     */
    public int getFilterWaveLength() {

        return m_filterWaveLength;
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
     * Returns the holes per glyph for a captcha image text (distortion).<p>
     * 
     * @return the holes per glyph for a captcha image text
     */
    public Integer getHolesPerGlyph() {

        return m_holesPerGlyp;
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
     * Returns the image width.<p>
     * 
     * @return the image width
     */
    public int getImageWidth() {

        return m_imageWidth;
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
     * Returns the max. phrase length.<p>
     * 
     * @return the max. phrase length
     */
    public int getMaxPhraseLength() {

        return m_maxPhraseLength;
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
     * Returns the min. phrase length.<p>
     * 
     * @return the min. phrase length
     */
    public int getMinPhraseLength() {

        return m_minPhraseLength;
    }

    /**
     * Configures the instance with values overridden from the the request parameters.<p>
     * 
     * @param jsp a Cms JSP page
     * 
     * @see #C_PARAM_BACKGROUND_COLOR
     * @see #C_PARAM_FILTER_AMPLITUDE
     */
    public void init(CmsJspActionElement jsp) {

        List mulipartFileItems = CmsRequestUtil.readMultipartFileItems(jsp.getRequest());
        m_parameterMap = new HashMap();
        if (mulipartFileItems != null) {
            m_parameterMap = CmsRequestUtil.readParameterMapFromMultiPart(
                jsp.getRequestContext().getEncoding(),
                mulipartFileItems);
        } else {
            m_parameterMap = jsp.getRequest().getParameterMap();
        }

        // image width
        String stringValue = getParameter(C_PARAM_IMAGE_WIDTH);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_imageWidth = Integer.parseInt(stringValue);
        }

        // image height
        stringValue = getParameter(C_PARAM_IMAGE_HEIGHT);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_imageHeight = Integer.parseInt(stringValue);
        }

        // min. phrase length
        stringValue = getParameter(C_PARAM_MIN_PHRASE_LENGTH);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_minPhraseLength = Integer.parseInt(stringValue);
        }

        // max. phrase length
        stringValue = getParameter(C_PARAM_MAX_PHRASE_LENGTH);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_maxPhraseLength = Integer.parseInt(stringValue);
        }

        // min. font size
        stringValue = getParameter(C_PARAM_MIN_FONT_SIZE);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_minFontSize = Integer.parseInt(stringValue);
        }

        // max. font size
        stringValue = getParameter(C_PARAM_MAX_FONT_SIZE);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_maxFontSize = Integer.parseInt(stringValue);
        }

        // font color
        stringValue = getParameter(C_PARAM_FONT_COLOR);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            stringValue = CmsEncoder.unescape(stringValue, jsp.getRequestContext().getEncoding());
            setFontColor(stringValue);
        }

        // background color
        stringValue = getParameter(C_PARAM_BACKGROUND_COLOR);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            stringValue = CmsEncoder.unescape(stringValue, jsp.getRequestContext().getEncoding());
        }
        setBackgroundColor(stringValue);

        // holes per glyph
        stringValue = getParameter(C_PARAM_HOLES_PER_GLYPH);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setHolesPerGlyph(Integer.parseInt(stringValue));
        }

        // filter amplitude
        stringValue = getParameter(C_PARAM_FILTER_AMPLITUDE);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setFilterAmplitude(Integer.parseInt(stringValue));
        }

        // filter wave length
        stringValue = getParameter(C_PARAM_FILTER_WAVE_LENGTH);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setFilterWaveLength(Integer.parseInt(stringValue));
        }
        // flag for generation of background image (vs. background color)
        stringValue = getParameter(C_PARAM_USE_BACKGROUND_IMAGE);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setUseBackgroundImage(Boolean.valueOf(stringValue).booleanValue());
        }

        // characters to use for word generation:
        stringValue = getParameter(C_PARAM_CHARACTERS);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setCharacterPool(stringValue);
        }
        // characters to use for word generation:
        stringValue = getParameter(C_PARAM_CHARACTERS);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            setCharacterPool(stringValue);
        }

        // just for logging comfort (find misconfigured presets):
        stringValue = getParameter(C_PARAM_PRESET);
        if (CmsStringUtil.isNotEmpty(stringValue)) {
            m_presetPath = stringValue;
        }
    }

    /**
     * Configures the instance with overridden values from the given XML content.<p>
     * 
     * <h3>Xmlcontent configuration notes</h3>
     * <ol>
     *   <li>
     *     <ul>
     *       <li> If the xmlcontent contains no node for BackgroundColor ({@link CmsCaptchaSettings#NODE_CAPTCHAPRESET_BACKGROUNDCOLOR}),
     *          a background image will be used. </li>
     *       <li> If the xmlcontent node contains an empty node (trimmable to the empty String), the
     *          default background colour {@link Color#WHITE}) will be used as background. </li>
     *       <li> Else the chosen background color will be used. </li>
     *     </ul>
     *   </li>
     * </ol>
     * <p>
     * 
     * @param cms the current user's Cms object
     * @param content the XML content of the form
     * @param locale the current locale
     */
    public void init(CmsObject cms, CmsXmlContent content, Locale locale) {

        try {
            String captchaSettingsPath = content.getStringValue(
                cms,
                new StringBuffer(CmsForm.NODE_CAPTCHA).append("/").append(CmsForm.NODE_CAPTCHA_PRESET).toString(),
                locale);
            if (CmsStringUtil.isNotEmpty(captchaSettingsPath)) {
                m_presetPath = captchaSettingsPath;
                CmsFile captchaSettingsFile = cms.readFile(captchaSettingsPath);
                CmsXmlContent preset = CmsXmlContentFactory.unmarshal(cms, captchaSettingsFile);

                Locale captchaSettingsLocale = Locale.ENGLISH;

                // image width
                String stringValue = preset.getStringValue(
                    cms,
                    CmsCaptchaSettings.NODE_CAPTCHAPRESET_IMAGEWIDTH,
                    captchaSettingsLocale);
                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    m_imageWidth = Integer.parseInt(stringValue);
                }

                // image height
                stringValue = preset.getStringValue(
                    cms,
                    CmsCaptchaSettings.NODE_CAPTCHAPRESET_IMAGEHEIGHT,
                    captchaSettingsLocale);
                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    m_imageHeight = Integer.parseInt(stringValue);
                }

                // min. phrase length
                stringValue = preset.getStringValue(
                    cms,
                    CmsCaptchaSettings.NODE_CAPTCHAPRESET_MIN_PHRASE_LENGTH,
                    captchaSettingsLocale);
                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    m_minPhraseLength = Integer.parseInt(stringValue);
                }

                // max. phrase length
                stringValue = preset.getStringValue(
                    cms,
                    CmsCaptchaSettings.NODE_CAPTCHAPRESET_MAX_PHRASE_LENGTH,
                    captchaSettingsLocale);
                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    m_maxPhraseLength = Integer.parseInt(stringValue);
                }

                // min. font size
                stringValue = preset.getStringValue(
                    cms,
                    CmsCaptchaSettings.NODE_CAPTCHAPRESET_MIN_FONT_SIZE,
                    captchaSettingsLocale);
                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    m_minFontSize = Integer.parseInt(stringValue);
                }

                // max. font size
                stringValue = preset.getStringValue(
                    cms,
                    CmsCaptchaSettings.NODE_CAPTCHAPRESET_MAX_FONT_SIZE,
                    captchaSettingsLocale);
                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    m_maxFontSize = Integer.parseInt(stringValue);
                }

                // font color
                stringValue = preset.getStringValue(
                    cms,
                    CmsCaptchaSettings.NODE_CAPTCHAPRESET_FONTCOLOR,
                    captchaSettingsLocale);
                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    setFontColor(stringValue);
                }

                // background color
                // if the field is defined but left blank, the default background color will be used
                // if the field is not defined a gimpy background image will be used
                stringValue = preset.getStringValue(
                    cms,
                    CmsCaptchaSettings.NODE_CAPTCHAPRESET_BACKGROUNDCOLOR,
                    captchaSettingsLocale);
                setBackgroundColor(stringValue);

                // holes per glyph
                stringValue = preset.getStringValue(
                    cms,
                    CmsCaptchaSettings.NODE_CAPTCHAPRESET_HOLESPERGLYPH,
                    captchaSettingsLocale);
                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    setHolesPerGlyph(Integer.parseInt(stringValue));
                }

                // filter amplitude
                stringValue = preset.getStringValue(
                    cms,
                    CmsCaptchaSettings.NODE_CAPTCHAPRESET_FILTER_AMPLITUDE,
                    captchaSettingsLocale);
                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    setFilterAmplitude(Integer.parseInt(stringValue));
                }

                // filter wave length
                stringValue = preset.getStringValue(
                    cms,
                    CmsCaptchaSettings.NODE_CAPTCHAPRESET_FILTER_WAVELENGTH,
                    captchaSettingsLocale);
                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    setFilterWaveLength(Integer.parseInt(stringValue));
                }

                stringValue = preset.getStringValue(cms, CmsForm.NODE_CAPTCHA_CHARACTERS, captchaSettingsLocale);
                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    setCharacterPool(stringValue);
                }

                if (CmsStringUtil.isNotEmpty(stringValue)) {
                    setCharacterPool(stringValue);
                }

            } else {
                // the optional preset selector is missing...
            }

        } catch (Exception ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error(ex.getLocalizedMessage());
            }

        }
    }

    /**
     * Returns the flag that decides wethter a background image or a background color is used.<p>
     * 
     * @return the flag that decides wethter a background image or a background color is used
     */
    public boolean isUseBackgroundImage() {

        return m_useBackgroundImage;
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

        if (CmsStringUtil.isNotEmpty(backgroundColor)) {
            if (backgroundColor.startsWith("#")) {
                backgroundColor = backgroundColor.substring(1);
            }
            m_backgroundColor = new Color(Integer.valueOf(backgroundColor, 16).intValue());
            m_useBackgroundImage = false;
        } else if (backgroundColor != null) {
            // not totally empty but consists of whitespaces only: use default value
            // this happens e.g. if the XML content to configure did contain the node but left the
            // value empty
            // in this case the default background color will be used
            m_useBackgroundImage = false;
            m_backgroundColor = Color.WHITE;
        } else {
            // really empty and null - not even defined in XML content:
            // don't use background color but a gimpy background image
            m_useBackgroundImage = true;
            // the color is not used but we have to avoid NPE in getBackgroundColorString()
            m_backgroundColor = Color.WHITE;
        }
    }

    /**
     * Sets the filter amplitude for the water filter that will bend the text.<p>
     * 
     * @param i the filter amplitude for the water filter that will bend the text to set.
     */
    public void setFilterAmplitude(int i) {

        m_filterAmplitude = i;

    }

    /**
     * Sets the filter wave length for the water filter that bends the text.<p>
     * 
     * @param filterWaveLength the filter wave length for the water filter that bends the text to  set
     */
    public void setFilterWaveLength(int filterWaveLength) {

        m_filterWaveLength = filterWaveLength;
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

        if (CmsStringUtil.isNotEmpty(fontColor)) {
            if (fontColor.startsWith("#")) {
                fontColor = fontColor.substring(1);
            }
            m_fontColor = new Color(Integer.valueOf(fontColor, 16).intValue());
        } else {
            m_fontColor = Color.BLACK;
        }
    }

    /**
     * Sets the holes per glyph for a captcha image text (distortion).<p>
     * 
     * @param holes the holes per glyph for a captcha image text to set.
     */
    public void setHolesPerGlyph(int holes) {

        m_holesPerGlyp = new Integer(holes);
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
     * Sets the image width.<p>
     * 
     * @param imageWidth the image width to set
     */
    public void setImageWidth(int imageWidth) {

        m_imageWidth = imageWidth;
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
     * Sets the max. phrase length.<p>
     * 
     * @param maxPhraseLength the max. phrase length to set
     */
    public void setMaxPhraseLength(int maxPhraseLength) {

        m_maxPhraseLength = maxPhraseLength;
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
     * Sets the min. phrase length.<p>
     * 
     * @param minPhraseLength the min. phrase length to set
     */
    public void setMinPhraseLength(int minPhraseLength) {

        m_minPhraseLength = minPhraseLength;
    }

    /**
     * Returns the flag that decides wethter a background image or a background color is used.<p>
     * 
     * @param useBackgroundImage the flag that decides wethter a background image or a background
     *            color is used.
     */
    public void setUseBackgroundImage(boolean useBackgroundImage) {

        m_useBackgroundImage = useBackgroundImage;
    }

    /**
     * Creates a request parameter string from including all captcha settings.<p>
     * 
     * @param cms needed for the context / encoding
     * @return a request parameter string from including all captcha settings
     */
    public String toRequestParams(CmsObject cms) {

        StringBuffer buf = new StringBuffer();

        buf.append(C_PARAM_IMAGE_WIDTH).append("=").append(m_imageWidth);
        buf.append("&").append(C_PARAM_IMAGE_HEIGHT).append("=").append(m_imageHeight);
        buf.append("&").append(C_PARAM_MIN_FONT_SIZE).append("=").append(m_minFontSize);
        buf.append("&").append(C_PARAM_MAX_FONT_SIZE).append("=").append(m_maxFontSize);
        buf.append("&").append(C_PARAM_MIN_PHRASE_LENGTH).append("=").append(m_minPhraseLength);
        buf.append("&").append(C_PARAM_MAX_PHRASE_LENGTH).append("=").append(m_maxPhraseLength);
        buf.append("&").append(C_PARAM_FONT_COLOR).append("=").append(
            CmsEncoder.escape(getFontColorString(), cms.getRequestContext().getEncoding()));
        buf.append("&").append(C_PARAM_BACKGROUND_COLOR).append("=").append(
            CmsEncoder.escape(getBackgroundColorString(), cms.getRequestContext().getEncoding()));
        buf.append("&").append(C_PARAM_HOLES_PER_GLYPH).append("=").append(m_holesPerGlyp);
        buf.append("&").append(C_PARAM_FILTER_AMPLITUDE).append("=").append(m_filterAmplitude);
        buf.append("&").append(C_PARAM_FILTER_WAVE_LENGTH).append("=").append(m_filterWaveLength);
        buf.append("&").append(C_PARAM_CHARACTERS).append("=").append(m_characterPool);
        buf.append("&").append(C_PARAM_PRESET).append("=").append(m_presetPath);
        buf.append("&").append(C_PARAM_USE_BACKGROUND_IMAGE).append("=").append(Boolean.toString(m_useBackgroundImage));
        return buf.toString();
    }

    /**
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() {

        CmsCaptchaSettings result = new CmsCaptchaSettings();
        // copy all members here:
        result.m_backgroundColor = m_backgroundColor;
        result.m_filterAmplitude = m_filterAmplitude;
        result.m_filterWaveLength = m_filterWaveLength;
        result.m_fontColor = m_fontColor;
        result.m_holesPerGlyp = m_holesPerGlyp;
        result.m_imageHeight = m_imageHeight;
        result.m_imageWidth = m_imageWidth;
        result.m_maxFontSize = m_maxFontSize;
        result.m_maxPhraseLength = m_maxPhraseLength;
        result.m_minFontSize = m_minFontSize;
        result.m_useBackgroundImage = m_useBackgroundImage;
        result.m_minPhraseLength = m_minPhraseLength;
        result.m_characterPool = m_characterPool;
        result.m_presetPath = m_presetPath;
        return result;
    }

    /**
     * Returns the character Pool.<p>
     * 
     * @return the character Pool
     */
    String getCharacterPool() {

        return m_characterPool;
    }

    /**
     * Returns the preset path that was used to configure these settings.<p>
     * 
     * This is read only, as the path is internally read from a nested CmsForm/FormCaptcha XML
     * content.<p>
     * 
     * @return the preset path that was used to configure these settings
     */
    String getPresetPath() {

        return m_presetPath;
    }

    /**
     * Sets the character Pool.<p>
     * 
     * @param characterPool the character Pool to set
     */
    void setCharacterPool(String characterPool) {

        m_characterPool = characterPool;
    }

    /**
     * Returns the request parameter with the specified name.<p>
     * 
     * @param parameter the parameter to return
     * 
     * @return the parameter value
     */
    private String getParameter(String parameter) {

        try {
            return ((String[])m_parameterMap.get(parameter))[0];
        } catch (NullPointerException e) {
            return "";
        }
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
