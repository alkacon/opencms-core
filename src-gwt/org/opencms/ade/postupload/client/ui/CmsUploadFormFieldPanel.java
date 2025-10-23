/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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

import org.opencms.gwt.client.property.CmsPropertyPanel;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.ui.input.form.CmsFormRow;
import org.opencms.gwt.client.ui.input.form.CmsInfoBoxFormFieldPanel;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsPropertyModification;

/**
 * Custom form field panel for the post-upload dialog which adds buttons for copying a property value to all other uploads.
 */
public class CmsUploadFormFieldPanel extends CmsInfoBoxFormFieldPanel {

    /** The handler that triggers the transfer of the property value to all other uploads. */
    private I_CmsUpdateAllUploadsHandler m_updateAllHandler;

    /**
     * Creates a new instance.
     *
     * @param info the info bean
     * @param updateAllHandler the handler to call for transferring a property value to all uploads
     */
    public CmsUploadFormFieldPanel(CmsListInfoBean info, I_CmsUpdateAllUploadsHandler updateAllHandler) {

        super(info);
        m_updateAllHandler = updateAllHandler;

    }

    /**
     * @see org.opencms.gwt.client.ui.input.form.A_CmsFormFieldPanel#createRow(org.opencms.gwt.client.ui.input.I_CmsFormField)
     */
    @Override
    protected CmsFormRow createRow(I_CmsFormField field) {

        CmsFormRow result = super.createRow(field);
        String propName = field.getLayoutData().get(CmsPropertyPanel.LD_PROPERTY);
        if (m_updateAllHandler != null) {
            if (!CmsPropertyModification.FILE_NAME_PROPERTY.equals(propName)) {
                CmsPushButton updateAllButton = new CmsPushButton();

                updateAllButton.setImageClass(I_CmsButton.CIRCLE_PLUS_INV);
                updateAllButton.setTitle(CmsConfirmTransferWidget.MessageStrings.caption());
                updateAllButton.setButtonStyle(I_CmsButton.ButtonStyle.FONT_ICON, null);
                updateAllButton.addClickHandler(event -> {
                    String currentValue = field.getWidget().getFormValueAsString();
                    m_updateAllHandler.updateAll(propName, currentValue);
                    updateAllButton.clearHoverState();

                });
                result.getIcon().add(updateAllButton);
            }
        }
        return result;

    }

}
