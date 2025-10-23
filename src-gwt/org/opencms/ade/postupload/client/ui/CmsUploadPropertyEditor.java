/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.postupload.client.ui;

import org.opencms.gwt.client.property.CmsSimplePropertyEditor;
import org.opencms.gwt.client.property.I_CmsPropertyEditorHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.Map;

import com.google.gwt.user.client.ui.Label;

/**
 * A property editor for the upload property dialog.<p>
 */
public class CmsUploadPropertyEditor extends CmsSimplePropertyEditor {

    private CmsUUID m_structureId;
    /** The handler for transferring property values to other uploads. */
    private I_CmsUpdateAllUploadsHandler m_updateAllHandler;

    /** The warning message. */
    private String m_warning;

    /**
     * Creates a new instance.<p>
     *
     * @param propConfig the property configuration
     * @param handler the property editor handler to use
     * @param updateAllHandler the handler to call for transferring a property value to all uploads
     */
    public CmsUploadPropertyEditor(
        CmsUUID structureId,
        Map<String, CmsXmlContentProperty> propConfig,
        I_CmsPropertyEditorHandler handler,
        I_CmsUpdateAllUploadsHandler updateAllHandler) {

        super(propConfig, handler);
        m_structureId = structureId;
        m_updateAllHandler = updateAllHandler;
        CmsUploadPropertyEditorHandler uploadPropertyHandler = (CmsUploadPropertyEditorHandler)handler;
        m_warning = uploadPropertyHandler.getWarning();
    }

    /**
     * @see org.opencms.gwt.client.property.CmsSimplePropertyEditor#isAlwaysAllowEmpty(java.lang.String)
     */
    @Override
    protected boolean isAlwaysAllowEmpty(String name) {

        return false;
    }

    /**
     * @see org.opencms.gwt.client.property.CmsSimplePropertyEditor#maybeAddCustomValidator(org.opencms.xml.content.CmsXmlContentProperty, org.opencms.gwt.client.ui.input.I_CmsFormField)
     */
    @Override
    protected void maybeAddCustomValidator(CmsXmlContentProperty propDef, I_CmsFormField field) {

        if (field.getId().contains(CmsPropertyModification.FILE_NAME_PROPERTY)) {
            field.setValidator(new CmsFilenameValidator(m_structureId, propDef));
        }
    }

    /**
    * @see org.opencms.gwt.client.property.CmsSimplePropertyEditor#setupFieldContainer()
    */
    @Override
    protected void setupFieldContainer() {

        CmsUploadFormFieldPanel panel = new CmsUploadFormFieldPanel(m_handler.getPageInfo(), m_updateAllHandler);
        panel.addStyleName(I_CmsLayoutBundle.INSTANCE.propertiesCss().noPaddingTop());
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(m_warning)) {
            Label warningLabel = new Label(m_warning);
            warningLabel.addStyleName(
                org.opencms.ade.postupload.client.ui.css.I_CmsLayoutBundle.INSTANCE.dialogCss().warningBox());
            panel.addWidgetAfterListInfo(warningLabel);
        }
        m_form.setWidget(panel);
    }

}
