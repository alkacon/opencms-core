/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/CmsImageFormatHandler.java,v $
 * Date   : $Date: 2010/07/19 07:45:28 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.client.preview.ui.CmsImageFormatsForm;
import org.opencms.ade.galleries.client.ui.Messages;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.util.CmsStringUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Image format form handler.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsImageFormatHandler {

    /** Default image formats. */
    private enum DefaultRestriction {
        big, free, original, small, user
    }

    /** Default format configuration. */
    private static final String[] DEFAULT_FORMAT_NAMES = {
        DefaultRestriction.original.name() + ":" + Messages.get().key(Messages.GUI_IMAGE_ORIGINAL_FORMAT_LABEL_0),
        DefaultRestriction.user.name() + ":" + Messages.get().key(Messages.GUI_IMAGE_USER_FORMAT_LABEL_0),
        DefaultRestriction.free.name() + ":" + Messages.get().key(Messages.GUI_IMAGE_FREE_FORMAT_LABEL_0),
        DefaultRestriction.small.name() + ":" + Messages.get().key(Messages.GUI_IMAGE_SMALL_FORMAT_LABEL_0),
        DefaultRestriction.big.name() + ":" + Messages.get().key(Messages.GUI_IMAGE_BIG_FORMAT_LABEL_0)};

    /** Default format configuration. */
    private static final String[] DEFAULT_FORMAT_VALUES = {
        DefaultRestriction.original.name(),
        DefaultRestriction.user.name(),
        DefaultRestriction.free.name(),
        DefaultRestriction.small.name(),
        DefaultRestriction.big.name()};

    /** The current cropping parameter. */
    private CmsCroppingParamBean m_croppingParam;

    private I_CmsFormatRestriction m_currentFormat;

    /** The format form. */
    private CmsImageFormatsForm m_formatForm;

    /** The format names and labels configuration. */
    private String[] m_formatNames;

    /** The map of available format restrictions. */
    private Map<String, I_CmsFormatRestriction> m_formats = Collections.emptyMap();

    /** The Format configuration. */
    private String[] m_formatValues;

    /** The gallery mode. */
    private GalleryMode m_galleryMode;

    /** The image height. */
    private int m_originalHeight = -1;

    /** The image width. */
    private int m_originalWidth = -1;

    /** Flag to indicate if image format may be changed. */
    private boolean m_useFormats;

    /**
     * Constructor.<p>
     * 
     * @param galleryMode the gallery mode
     * @param selectedPath the selected gallery path
     * @param imageHeight the image height
     * @param imageWidth the image width
     */
    public CmsImageFormatHandler(GalleryMode galleryMode, String selectedPath, int imageHeight, int imageWidth) {

        m_originalHeight = imageHeight;
        m_originalWidth = imageWidth;
        m_croppingParam = CmsCroppingParamBean.parseImagePath(selectedPath);
        m_galleryMode = galleryMode;
        readFormatsConfig(galleryMode);
        if (m_useFormats) {
            generateFormats();
        }
    }

    /**
     * Returns the current format.<p>
     *
     * @return the current format
     */
    public I_CmsFormatRestriction getCurrentFormat() {

        return m_currentFormat;
    }

    /**
     * Returns the formats.<p>
     *
     * @return the formats
     */
    public Map<String, I_CmsFormatRestriction> getFormats() {

        return m_formats;
    }

    /**
     * Returns the original height.<p>
     *
     * @return the original height
     */
    public int getOriginalHeight() {

        return m_originalHeight;
    }

    /**
     * Returns the original width.<p>
     *
     * @return the original width
     */
    public int getOriginalWidth() {

        return m_originalWidth;
    }

    /**
     * Returns the scaling parameter.<p>
     * 
     * @return the scaling parameter
     */
    public String getScaleParam() {

        String result = m_croppingParam.toString();
        if (result.length() > 0) {
            result = "?" + result;
        }
        return result;
    }

    /**
     * Initializes the format form handler.<p>
     * 
     * @param formatForm the format form
     */
    public void init(CmsImageFormatsForm formatForm) {

        m_formatForm = formatForm;
        if (m_useFormats) {
            for (Entry<String, I_CmsFormatRestriction> entry : m_formats.entrySet()) {
                m_formatForm.addFormatSelectOption(entry.getKey(), entry.getValue().getLabel());
            }
            Entry<String, I_CmsFormatRestriction> match = getMatchingFormat(m_croppingParam);
            if (match != null) {
                m_formatForm.setFormatSelectValue(match.getKey());
            }
            m_formatForm.setHeightInput(m_croppingParam.getTargetHeight() != -1
            ? m_croppingParam.getTargetHeight()
            : m_originalHeight);
            m_formatForm.setWidthInput(m_croppingParam.getTargetWidth() != -1
            ? m_croppingParam.getTargetWidth()
            : m_originalWidth);
        } else {
            m_formatForm.addFormatSelectOption("--", "--");
            m_formatForm.setFormEnabled(m_useFormats);
        }

    }

    /**
     * Execute on format change.<p>
     * 
     * @param formatKey the new format value
     */
    public void onFormatChange(String formatKey) {

        I_CmsFormatRestriction restriction = m_formats.get(formatKey);
        m_croppingParam.setTargetHeight(restriction.getHeight(m_originalHeight, m_originalWidth));
        m_formatForm.setHeightInput(m_croppingParam.getTargetHeight());
        m_croppingParam.setTargetWidth(restriction.getWidth(m_originalHeight, m_originalWidth));
        m_formatForm.setWidthInput(m_croppingParam.getTargetWidth());
        if (restriction.isFixedRatio()) {

            //TODO enable/disable ratio lock
        }
    }

    /**
     * Execute on height change.<p>
     * 
     * @param height the new height
     */
    public void onHeightChange(String height) {

        int value = CmsClientStringUtil.parseInt(height);
        m_croppingParam.setTargetHeight(value);
    }

    /**
     * Execute on width change.<p>
     * 
     * @param width the new width
     */
    public void onWidthChange(String width) {

        int value = CmsClientStringUtil.parseInt(width);
        m_croppingParam.setTargetWidth(value);
    }

    /**
     * Sets the original width.<p>
     *
     * @param originalWidth the original width to set
     */
    public void setOriginalWidth(int originalWidth) {

        m_originalWidth = originalWidth;
    }

    /**
     * Generates the format restriction objects.<p>
     */
    private void generateFormats() {

        m_formats = new HashMap<String, I_CmsFormatRestriction>();
        for (int i = 0; i < m_formatValues.length; i++) {
            String value = m_formatValues[i].trim();

            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(value)) {
                String label = value;
                String key = value;
                if ((m_formatNames != null)
                    && (m_formatNames.length > i)
                    && CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_formatNames[i])) {
                    int pos = m_formatNames[i].indexOf(":");
                    if (pos > 0) {
                        label = m_formatNames[i].substring(pos + 1, m_formatNames[i].length());
                        key = m_formatNames[i].substring(pos);
                    } else {
                        label = m_formatNames[i];
                        key = m_formatNames[i];
                    }
                }

                DefaultRestriction restrictionType = null;
                try {
                    restrictionType = DefaultRestriction.valueOf(value);
                } catch (Exception e) {
                    // happens with user defined restriction settings
                }
                if (restrictionType != null) {
                    switch (restrictionType) {
                        case original:
                            m_formats.put(key, new CmsOriginalFormatRestriction(label));
                            break;
                        case user:
                            m_formats.put(key, new CmsUserFormatRestriction(label));
                            break;
                        case free:
                            m_formats.put(key, new CmsFreeFormatRestriction(label));
                            break;
                        case small:
                            m_formats.put(key, new CmsImageFormatRestriction(label, "200x?"));
                            break;
                        case big:
                            m_formats.put(key, new CmsImageFormatRestriction(label, "500x?"));
                    }
                } else {
                    if (CmsImageFormatRestriction.isValidConfig(value)) {
                        m_formats.put(key, new CmsImageFormatRestriction(label, value));
                    }
                }
            }
        }
    }

    /**
     * Checks the format restrictions if the match the giving cropping parameter.<p>
     * 
     * @param croppingParam the cropping parameter
     * 
     * @return the matching format restriction
     */
    private Entry<String, I_CmsFormatRestriction> getMatchingFormat(CmsCroppingParamBean croppingParam) {

        Entry<String, I_CmsFormatRestriction> result = null;
        for (Entry<String, I_CmsFormatRestriction> entry : m_formats.entrySet()) {
            if (entry.getValue().matchesCroppingParam(croppingParam)) {
                result = entry;
            }
        }
        return result;
    }

    /**
     * Reads the format configuration for the given gallery mode.<p>
     * 
     * @param mode the gallery mode
     */
    private void readFormatsConfig(GalleryMode mode) {

        switch (mode) {
            case editor:
                m_useFormats = true;
                m_formatNames = DEFAULT_FORMAT_NAMES;
                m_formatValues = DEFAULT_FORMAT_VALUES;
                break;
            case widget:
                m_useFormats = CmsPreviewUtil.isShowFormats();
                if (m_useFormats) {
                    m_formatValues = CmsPreviewUtil.getFormats();
                    if (m_formatValues == null) {
                        m_formatNames = DEFAULT_FORMAT_NAMES;
                        m_formatValues = DEFAULT_FORMAT_VALUES;
                    } else {
                        String temp = CmsPreviewUtil.getFormatNames();
                        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(temp)) {
                            m_formatNames = temp.split("|");
                        }
                    }
                }
                break;
            case ade:
            case sitemap:
            case view:
                m_useFormats = false;
        }

    }
}
