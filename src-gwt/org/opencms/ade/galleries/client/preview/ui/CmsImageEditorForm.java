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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.preview.CmsImagePreviewHandler.Attribute;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.util.CmsJSONMap;
import org.opencms.gwt.shared.property.CmsClientProperty;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Simple image tag properties form, use in editor mode only.<p>
 * 
 * @since 8.0.
 */
public class CmsImageEditorForm extends Composite {

    /** Ui binder interface. */
    protected interface I_CmsImageEditorFormatsTabUiBinder extends UiBinder<Widget, CmsImageEditorForm> {
        // GWT interface, nothing to do
    }

    /** Ui binder instance. */
    private static I_CmsImageEditorFormatsTabUiBinder m_uiBinder = GWT.create(I_CmsImageEditorFormatsTabUiBinder.class);

    /** The reset copyright button. */
    @UiField
    protected CmsPushButton m_buttonResetCopyright;

    /** The reset alt/title button. */
    @UiField
    protected CmsPushButton m_buttonResetTitle;

    /** The insert copyright check-box. */
    @UiField
    protected CmsCheckBox m_checkboxInsertCopyright;

    /** The insert link to original check-box. */
    @UiField
    protected CmsCheckBox m_checkboxInsertLinkOrig;

    /** The insert subtitle check-box. */
    @UiField
    protected CmsCheckBox m_checkboxInsertSubtitle;

    /** The insert spacing check-box. */
    @UiField
    protected CmsCheckBox m_checkboxSpacing;

    /** The alt/title input field. */
    @UiField
    protected CmsTextBox m_inputAltTitle;

    /** The copyright input field. */
    @UiField
    protected CmsTextBox m_inputCopyright;

    /** The hSpace input field. */
    @UiField
    protected CmsTextBox m_inputHSpace;

    /** The vSpace input field. */
    @UiField
    protected CmsTextBox m_inputVSpace;

    /** The alignment select-box label. */
    @UiField
    protected Label m_labelAlign;

    /** The alt/title field label. */
    @UiField
    protected Label m_labelAltTitle;

    /** The copyright field label. */
    @UiField
    protected Label m_labelCopyright;

    /** The hSpace field label. */
    @UiField
    protected Label m_labelHSpace;

    /** The image spacing check-box label. */
    @UiField
    protected Label m_labelImageSpacing;

    /** The insert copyright check-box label. */
    @UiField
    protected Label m_labelInsertCopyright;

    /** The insert link to original check-box label. */
    @UiField
    protected Label m_labelInsertLinkOrig;

    /** The insert subtitles check-box label. */
    @UiField
    protected Label m_labelInsertSubtitle;

    /** The vSpace field label. */
    @UiField
    protected Label m_labelVSpace;

    /** The alignment select-box. */
    @UiField
    CmsSelectBox m_selectAlign;

    /** The form fields. */
    private Map<Attribute, I_CmsFormWidget> m_fields;

    /** The initial image attribute values. */
    private CmsJSONMap m_initialImageAttributes;

    /**
     * Constructor.<p>
     */
    public CmsImageEditorForm() {

        initWidget(m_uiBinder.createAndBindUi(this));
        m_labelAltTitle.setText(Messages.get().key(Messages.GUI_IMAGE_TITLE_ALT_0));
        m_labelCopyright.setText(Messages.get().key(Messages.GUI_IMAGE_COPYRIGHT_0));
        m_labelImageSpacing.setText(Messages.get().key(Messages.GUI_IMAGE_SPACING_0));
        m_labelInsertSubtitle.setText(Messages.get().key(Messages.GUI_IMAGE_INSERT_SUBTITLE_0));
        m_labelInsertCopyright.setText(Messages.get().key(Messages.GUI_IMAGE_INSERT_COPYRIGHT_0));
        m_labelHSpace.setText(Messages.get().key(Messages.GUI_IMAGE_HSPACE_0));
        m_labelVSpace.setText(Messages.get().key(Messages.GUI_IMAGE_VSPACE_0));
        m_labelInsertLinkOrig.setText(Messages.get().key(Messages.GUI_IMAGE_INSERT_LINK_TO_ORG_0));
        m_buttonResetTitle.setSize(Size.small);
        m_buttonResetTitle.setText(Messages.get().key(Messages.GUI_IMAGE_RESET_TITLE_0));
        m_buttonResetCopyright.setSize(Size.small);
        m_buttonResetCopyright.setText(Messages.get().key(Messages.GUI_IMAGE_RESET_COPYRIGHT_0));

        m_labelAlign.setText(Messages.get().key(Messages.GUI_IMAGE_ALIGN_0));
        m_selectAlign.addOption("", Messages.get().key(Messages.GUI_IMAGE_ALIGN_NOT_SET_0));
        m_selectAlign.addOption("left", Messages.get().key(Messages.GUI_IMAGE_ALIGN_LEFT_0));
        m_selectAlign.addOption("right", Messages.get().key(Messages.GUI_IMAGE_ALIGN_RIGHT_0));
        m_fields = new HashMap<Attribute, I_CmsFormWidget>();
        m_fields.put(Attribute.alt, m_inputAltTitle);
        m_fields.put(Attribute.hspace, m_inputHSpace);
        m_fields.put(Attribute.vspace, m_inputVSpace);
        m_fields.put(Attribute.align, m_selectAlign);
        m_fields.put(Attribute.copyright, m_inputCopyright);
        m_fields.put(Attribute.insertCopyright, m_checkboxInsertCopyright);
        m_fields.put(Attribute.insertSubtitle, m_checkboxInsertSubtitle);
        m_fields.put(Attribute.insertSpacing, m_checkboxSpacing);
        m_fields.put(Attribute.insertLinkOrig, m_checkboxInsertLinkOrig);
    }

    /**
     * Displays the provided image information.<p>
     * 
     * @param imageInfo the image information
     * @param imageAttributes the image attributes
     * @param initialFill flag to indicate that a new image has been selected 
     */
    public void fillContent(CmsImageInfoBean imageInfo, CmsJSONMap imageAttributes, boolean initialFill) {

        m_initialImageAttributes = imageAttributes;
        boolean hasSpacing = false;
        for (Entry<Attribute, I_CmsFormWidget> entry : m_fields.entrySet()) {
            String val = imageAttributes.getString(entry.getKey().name());
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(val)) {
                entry.getValue().setFormValueAsString(val);
                if ((entry.getKey() == Attribute.hspace) || (entry.getKey() == Attribute.vspace)) {
                    hasSpacing = true;
                }
            } else {
                if (entry.getKey() == Attribute.alt) {
                    entry.getValue().setFormValueAsString(
                        imageInfo.getProperties().get(CmsClientProperty.PROPERTY_TITLE));
                }
                if (entry.getKey() == Attribute.copyright) {
                    entry.getValue().setFormValueAsString(imageInfo.getCopyright());
                }
                if (initialFill && (entry.getKey() == Attribute.align)) {
                    entry.getValue().setFormValueAsString("left");
                }
            }
        }
        m_checkboxSpacing.setFormValueAsString("" + hasSpacing);
    }

    /**
     * Adds necessary attributes to the map.<p>
     * 
     * @param attributes the attribute map
     * @return the attribute map
     */
    public Map<String, String> getImageAttributes(Map<String, String> attributes) {

        for (Entry<Attribute, I_CmsFormWidget> entry : m_fields.entrySet()) {
            String val = entry.getValue().getFormValueAsString();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(val)) {
                continue;
            }
            attributes.put(entry.getKey().name(), val);
            // put the same value in 'alt' and 'title' attribute
            if (entry.getKey() == Attribute.alt) {
                attributes.put(Attribute.title.name(), val);
            }
        }
        return attributes;
    }

    /**
     * Hides the enhanced image options in this form.<p>
     * 
     * @param hide if <code>true</code> the enhanced options will get hidden
     */
    public void hideEnhancedOptions(boolean hide) {

        if (hide) {
            addStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().hiding());
        } else {
            removeStyleName(I_CmsLayoutBundle.INSTANCE.previewDialogCss().hiding());
        }
    }

    /**
     * Handles the click on 'reset copyright' button.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_buttonResetCopyright")
    protected void onResetCopyrightClick(ClickEvent event) {

        resetValue(Attribute.copyright);
    }

    /**
     * Handles the click on 'reset title' button.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_buttonResetTitle")
    protected void onResetTitleClick(ClickEvent event) {

        resetValue(Attribute.alt);
    }

    /**
     * Handles value changes on the insert spacing check box.<p>
     * 
     * @param event the event to handle
     */
    @UiHandler("m_checkboxSpacing")
    protected void onChangeSpacing(ValueChangeEvent<Boolean> event) {

        // if spacing is activated and no previous values present, set '5' as default 
        if (event.getValue().booleanValue()) {
            m_inputHSpace.setEnabled(true);
            m_inputVSpace.setEnabled(true);
            m_inputHSpace.setFormValueAsString("5");
            m_inputVSpace.setFormValueAsString("5");
        } else {
            m_inputHSpace.setFormValueAsString("");
            m_inputVSpace.setFormValueAsString("");
            m_inputHSpace.setEnabled(false);
            m_inputVSpace.setEnabled(false);
        }
    }

    /**
     * Resets the value for the given attribute to it's initial value.<p>
     * 
     * @param attribute the attribute to reset
     */
    protected void resetValue(Attribute attribute) {

        String initValue = "";
        if (m_initialImageAttributes.containsKey(attribute.name())) {
            initValue = m_initialImageAttributes.getString(attribute.name());
        }
        if (m_fields.containsKey(attribute)) {
            m_fields.get(attribute).setFormValueAsString(initValue);
        }
    }

}
