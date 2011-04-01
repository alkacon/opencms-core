/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/CmsImageAdvancedForm.java,v $
 * Date   : $Date: 2011/04/01 10:34:07 $
 * Version: $Revision: 1.2 $
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
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.CmsVfsLinkWidget;
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
 * Advanced image tag properties form, use in editor mode only.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.
 */
public class CmsImageAdvancedForm extends Composite {

    /** The ui binder interface. */
    interface I_CmsImageAdvancedFormUiBinder extends UiBinder<Widget, CmsImageAdvancedForm> {
        // GWT interface, nothing to do
    }

    /** Ui binder instance. */
    private static I_CmsImageAdvancedFormUiBinder m_uiBinder = GWT.create(I_CmsImageAdvancedFormUiBinder.class);

    //    /** Browse button. */
    //    @UiField
    //    protected CmsPushButton m_buttonBrowse;

    /** Advisory title input field. */
    @UiField
    protected CmsTextBox m_inputAdvisoryTitle;

    /** Description input field. */
    @UiField
    protected CmsTextBox m_inputDescription;

    /** Id input field. */
    @UiField
    protected CmsTextBox m_inputId;

    /** Language code input field. */
    @UiField
    protected CmsTextBox m_inputLanguageCode;

    /** Style input field. */
    @UiField
    protected CmsTextBox m_inputStyle;

    /** Style classes input field. */
    @UiField
    protected CmsTextBox m_inputStyleClasses;

    /** URL input field. */
    @UiField
    protected CmsVfsLinkWidget m_inputUrl;

    /** Advisory title field label. */
    @UiField
    protected Label m_labelAdvisoryTitle;

    /** Description field label. */
    @UiField
    protected Label m_labelDescription;

    /** Id field label. */
    @UiField
    protected Label m_labelId;

    /** Language code field label. */
    @UiField
    protected Label m_labelLanguageCode;

    /** Set link checkbox label. */
    @UiField
    protected Label m_labelSetLink;

    /** Style field label. */
    @UiField
    protected Label m_labelStyle;

    /** Style classes field label. */
    @UiField
    protected Label m_labelStyleClasses;

    /** Additional attributes label. */
    @UiField
    protected Label m_labelTagAttributes;

    /** Target field label. */
    @UiField
    protected Label m_labelTarget;

    /** Language direction field label. */
    @UiField
    protected Label m_labelTextDirection;

    /** URL field label. */
    @UiField
    protected Label m_labelUrl;

    /** Target select box. */
    @UiField
    protected CmsSelectBox m_selectTarget;

    /** Language direction select box. */
    @UiField
    protected CmsSelectBox m_selectTextDirection;

    /** The form fields. */
    private Map<Attribute, I_CmsFormWidget> m_fields;

    /**
     * The constructor.<p>
     */
    public CmsImageAdvancedForm() {

        initWidget(m_uiBinder.createAndBindUi(this));

        m_labelUrl.setText("Url:");
        m_labelTarget.setText("Target:");
        m_labelId.setText("Id:");
        m_labelStyleClasses.setText("Style classes:");
        m_labelStyle.setText("Style rules:");
        m_labelSetLink.setText("Set image as link:");
        m_labelTagAttributes.setText("Additional image tag attributes:");
        m_labelDescription.setText("Description:");
        m_labelAdvisoryTitle.setText("Advisory Title:");
        m_labelLanguageCode.setText("Language code:");
        m_labelTextDirection.setText("Text direction:");

        m_selectTarget.addOption("", "not set");
        m_selectTarget.addOption("_blank", "New window");
        m_selectTarget.addOption("_top", "Topmost window");
        m_selectTarget.addOption("_self", "Same window");
        m_selectTarget.addOption("_parent", "Parent window");

        m_selectTextDirection.addOption("ltr", "left to right");
        m_selectTextDirection.addOption("rtl", "Right to left");
        // buttons        

        //        m_buttonBrowse.setText("Browse server");
        m_inputUrl.setButtonSize(Size.small);
        m_inputUrl.addInputStyleName(I_CmsLayoutBundle.INSTANCE.imageAdvancedFormCss().input());
        m_fields = new HashMap<Attribute, I_CmsFormWidget>();
        m_fields.put(Attribute.linkTarget, m_selectTarget);
        m_fields.put(Attribute.dir, m_selectTextDirection);
        m_fields.put(Attribute.linkPath, m_inputUrl);
        m_fields.put(Attribute.longDesc, m_inputDescription);
        m_fields.put(Attribute.style, m_inputStyle);
        m_fields.put(Attribute.clazz, m_inputStyleClasses);
        m_fields.put(Attribute.title, m_inputAdvisoryTitle);
        m_fields.put(Attribute.id, m_inputId);
        m_fields.put(Attribute.lang, m_inputLanguageCode);
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

}
