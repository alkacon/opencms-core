/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/CmsImageEditorForm.java,v $
 * Date   : $Date: 2010/08/26 13:34:11 $
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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.preview.CmsImagePreviewHandler.Attribute;
import org.opencms.ade.galleries.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.I_CmsFormWidget;
import org.opencms.gwt.client.util.CmsJSONMap;
import org.opencms.util.CmsStringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Simple image tag properties form, use in editor mode only.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
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

    /**
     * Constructor.<p>
     */
    public CmsImageEditorForm() {

        //TODO: localization
        initWidget(m_uiBinder.createAndBindUi(this));
        m_labelAltTitle.setText("Title / Alt-Text:");
        m_labelCopyright.setText("Copyright:");
        m_labelImageSpacing.setText("Image spacing:");
        m_labelInsertSubtitle.setText("Insert subtitle:");
        m_labelInsertCopyright.setText("Insert copyright:");
        m_labelHSpace.setText("HSpace:");
        m_labelVSpace.setText("VSpace:");
        m_labelInsertLinkOrig.setText("Insert link to original image");
        m_buttonResetTitle.setSize(Size.small);
        m_buttonResetTitle.setText("Reset title");
        m_buttonResetCopyright.setSize(Size.small);
        m_buttonResetCopyright.setText("Reset copyright");

        m_labelAlign.setText("Align:");
        m_selectAlign.addOption("", "not set");
        m_selectAlign.addOption("left", "left");
        m_selectAlign.addOption("right", "right");

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
     * @param imageAttributes the image attributes
     */
    public void fillContent(CmsJSONMap imageAttributes) {

        for (Entry<Attribute, I_CmsFormWidget> entry : m_fields.entrySet()) {
            String val = imageAttributes.getString(entry.getKey().name());
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(val)) {
                entry.getValue().setFormValueAsString(val);
            }
        }
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
                val = null;
            }
            attributes.put(entry.getKey().name(), val);
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

}
