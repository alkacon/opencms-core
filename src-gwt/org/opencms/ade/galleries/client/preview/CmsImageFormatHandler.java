/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.ade.galleries.client.preview.ui.CmsCroppingDialog;
import org.opencms.ade.galleries.client.preview.ui.CmsImageFormatsForm;
import org.opencms.ade.galleries.client.ui.Messages;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.util.CmsStringUtil;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;

/**
 * Image format form handler.<p>
 * 
 * @since 8.0.0
 */
public class CmsImageFormatHandler implements HasValueChangeHandlers<CmsCroppingParamBean> {

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

    private CmsCroppingDialog m_croppingDialog;

    /** The current cropping parameter. */
    private CmsCroppingParamBean m_croppingParam;

    /** The current image format restriction. */
    private I_CmsFormatRestriction m_currentFormat;

    /** The event bus. */
    private SimpleEventBus m_eventBus;

    /** The format form. */
    private CmsImageFormatsForm m_formatForm;

    /** The format names and labels configuration. */
    private String[] m_formatNames;

    /** The map of available format restrictions. */
    private Map<String, I_CmsFormatRestriction> m_formats = Collections.emptyMap();

    /** The Format configuration. */
    private String[] m_formatValues;

    /** The image height. */
    private int m_originalHeight = -1;

    /** The image width. */
    private int m_originalWidth = -1;

    /** Flag indicating if the height / width ratio is locked. */
    private boolean m_ratioLocked;

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
        m_croppingParam.setOrgHeight(imageHeight);
        m_croppingParam.setOrgWidth(imageWidth);
        readFormatsConfig(galleryMode);
        if (m_useFormats) {
            generateFormats();
        }
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<CmsCroppingParamBean> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.event.shared.HasHandlers#fireEvent(com.google.gwt.event.shared.GwtEvent)
     */
    public void fireEvent(GwtEvent<?> event) {

        ensureHandlers().fireEventFromSource(event, this);
    }

    /**
     * Returns the current cropping parameter.<p>
     * 
     * @return the current cropping parameter
     */
    public CmsCroppingParamBean getCroppingParam() {

        return m_croppingParam;
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
     * Adds necessary attributes to the map.<p>
     * 
     * @param attributes the attribute map
     * @return the attribute map
     */
    public Map<String, String> getImageAttributes(Map<String, String> attributes) {

        if (m_croppingParam.getTargetHeight() > 0) {
            attributes.put("height", String.valueOf(m_croppingParam.getTargetHeight()));
        }
        if (m_croppingParam.getTargetWidth() > 0) {
            attributes.put("width", String.valueOf(m_croppingParam.getTargetWidth()));
        }
        return attributes;
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
     * Initializes the format form handler.<p>
     * 
     * @param formatForm the format form
     * @param croppingDialog the cropping dialog
     */
    public void init(CmsImageFormatsForm formatForm, CmsCroppingDialog croppingDialog) {

        m_croppingDialog = croppingDialog;
        m_croppingDialog.addValueChangeHandler(new ValueChangeHandler<CmsCroppingParamBean>() {

            /**
             * Executed on value change. Sets the returned cropping parameters.<p>
             * 
             * @param event the value change event
             */
            public void onValueChange(ValueChangeEvent<CmsCroppingParamBean> event) {

                setCropping(event.getValue());
            }
        });
        m_formatForm = formatForm;
        if (m_useFormats) {
            for (Entry<String, I_CmsFormatRestriction> entry : m_formats.entrySet()) {
                m_formatForm.addFormatSelectOption(entry.getKey(), entry.getValue().getLabel());
            }
            Entry<String, I_CmsFormatRestriction> match = getMatchingFormat(m_croppingParam);

            if (match != null) {
                m_currentFormat = match.getValue();
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
        if (m_croppingParam.isCropped()) {
            setCropping(m_croppingParam);
        }

    }

    /**
     * Execute on format change.<p>
     * 
     * @param formatKey the new format value
     */
    public void onFormatChange(String formatKey) {

        if (m_currentFormat == m_formats.get(formatKey)) {
            return;
        }

        // setting the selected format restriction
        m_currentFormat = m_formats.get(formatKey);
        m_currentFormat.adjustCroppingParam(m_croppingParam);
        // in case of a locked or fixed image ratio height and width need to be reset
        int height = m_croppingParam.getOrgHeight();
        int width = m_croppingParam.getOrgWidth();
        if (m_croppingParam.isScaled()) {
            if (m_croppingParam.getTargetHeight() == -1) {
                height = (int)Math.floor(((1.00 * m_croppingParam.getOrgHeight()) / m_croppingParam.getOrgWidth())
                    * m_croppingParam.getTargetWidth());
            } else {
                height = m_croppingParam.getTargetHeight();
            }
            if (m_croppingParam.getTargetWidth() == -1) {
                width = (int)Math.floor(((1.00 * m_croppingParam.getOrgWidth()) / m_croppingParam.getOrgHeight())
                    * m_croppingParam.getTargetHeight());
            } else {
                width = m_croppingParam.getTargetWidth();
            }

        }
        m_formatForm.setHeightInput(height);
        m_formatForm.setWidthInput(width);
        // enabling/disabling ratio lock button
        if (m_currentFormat.isFixedRatio()) {
            m_formatForm.setRatioButton(false, false, Messages.get().key(Messages.GUI_PRIVIEW_BUTTON_RATIO_FIXED_0));
            m_ratioLocked = true;
        } else {
            if (!m_currentFormat.isHeightEditable() && !m_currentFormat.isWidthEditable()) {
                // neither height nor width are editable, disable ratio lock button
                m_formatForm.setRatioButton(
                    false,
                    false,
                    Messages.get().key(Messages.GUI_PRIVIEW_BUTTON_NOT_EDITABLE_0));
            } else {
                m_formatForm.setRatioButton(false, true, null);
            }
            m_ratioLocked = false;
        }
        // enabling/disabling height and width input
        m_formatForm.setHeightInputEnabled(m_currentFormat.isHeightEditable());
        m_formatForm.setWidthInputEnabled(m_currentFormat.isWidthEditable());

        fireValueChangedEvent();
    }

    /**
     * Execute on height change.<p>
     * 
     * @param height the new height
     */
    public void onHeightChange(String height) {

        int value = CmsClientStringUtil.parseInt(height);
        if ((m_croppingParam.getTargetHeight() == value) || (value == 0)) {
            // the value has not changed, ignore'0'
            return;
        }
        m_croppingParam.setTargetHeight(value);
        if (m_ratioLocked) {
            m_croppingParam.setTargetWidth((value * m_originalWidth) / m_originalHeight);
            m_formatForm.setWidthInput(m_croppingParam.getTargetWidth());
        }
        fireValueChangedEvent();
    }

    /**
     * Execute when the lock image ratio is clicked.<p>
     * 
     * @param locked <code>true</code> if ratio is locked
     */
    public void onLockRatio(boolean locked) {

        m_ratioLocked = locked;
    }

    /**
     * Execute when cropping is removed.<p>
     */
    public void onRemoveCropping() {

        m_formatForm.setCropped(false);
        onResetSize();
    }

    /**
     * Execute to reset image format and size input.<p>
     */
    public void onResetSize() {

        String restrictionKey;
        if (m_formats.containsKey(DefaultRestriction.original.name())) {
            restrictionKey = DefaultRestriction.original.name();
        } else {
            restrictionKey = m_formats.keySet().iterator().next();
        }
        m_formatForm.setFormatSelectValue(restrictionKey);
        m_croppingParam.reset();
        onFormatChange(DefaultRestriction.original.name());
    }

    /**
     * Execute on width change.<p>
     * 
     * @param width the new width
     */
    public void onWidthChange(String width) {

        int value = CmsClientStringUtil.parseInt(width);
        if ((m_croppingParam.getTargetWidth() == value) || (value == 0)) {
            // the value has not changed, ignore'0'
            return;
        }
        m_croppingParam.setTargetWidth(value);
        if (m_ratioLocked) {
            m_croppingParam.setTargetHeight((value * m_originalHeight) / m_originalWidth);
            m_formatForm.setHeightInput(m_croppingParam.getTargetHeight());
        }
        fireValueChangedEvent();
    }

    /**
     * Shows the image cropping dialog.<p>
     */
    public void openCropping() {

        CmsCroppingParamBean param = new CmsCroppingParamBean(m_croppingParam);
        m_currentFormat.adjustCroppingParam(param);
        m_croppingDialog.show(param);
    }

    /**
     * Sets the given cropping parameter.<p>
     * 
     * @param croppingParam the cropping parameter
     */
    public void setCropping(CmsCroppingParamBean croppingParam) {

        m_croppingParam = croppingParam;
        m_formatForm.setHeightInput(m_croppingParam.getTargetHeight());
        m_formatForm.setWidthInput(m_croppingParam.getTargetWidth());

        // only in case of the original-format-restriction, the cropping dialog may be opened to override the selected format
        if (m_currentFormat instanceof CmsOriginalFormatRestriction) {
            Entry<String, I_CmsFormatRestriction> match = getMatchingFormat(m_croppingParam);
            if (match != null) {
                m_currentFormat = match.getValue();
                m_formatForm.setFormatSelectValue(match.getKey());
            }
        }
        m_formatForm.setCropped(true);
        fireValueChangedEvent();
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
     * Adds this handler to the widget.
     * 
     * @param <H> the type of handler to add
     * @param type the event type
     * @param handler the handler
     * @return {@link HandlerRegistration} used to remove the handler
     */
    protected final <H extends EventHandler> HandlerRegistration addHandler(final H handler, GwtEvent.Type<H> type) {

        return ensureHandlers().addHandlerToSource(type, this, handler);
    }

    /** 
     * Helper method for firing a 'value changed' event.<p>
     */
    protected void fireValueChangedEvent() {

        ValueChangeEvent.fire(this, m_croppingParam);
    }

    /**
     * Lazy initializing the handler manager.<p>
     * 
     * @return the handler manager
     */
    private SimpleEventBus ensureHandlers() {

        if (m_eventBus == null) {
            m_eventBus = new SimpleEventBus();
        }
        return m_eventBus;
    }

    /**
     * Generates the format restriction objects.<p>
     */
    private void generateFormats() {

        m_formats = new LinkedHashMap<String, I_CmsFormatRestriction>();
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
                            m_formats.put(DefaultRestriction.original.name(), new CmsOriginalFormatRestriction(label));
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
                            break;
                        default:
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
            case view:
                m_useFormats = false;
                break;
            default:
        }
    }
}
