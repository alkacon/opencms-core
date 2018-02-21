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

package org.opencms.ui.apps.dbmanager;

import org.opencms.file.CmsPropertyDefinition;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.util.CmsStringUtil;

import com.vaadin.v7.data.Validator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.v7.ui.TextField;
import com.vaadin.ui.Window;

/**
 * Class for dialog to create property definition.<p>
 */
public class CmsAddPropertyDefinitionDialog extends CmsBasicDialog {

    /**Validator for new Property name. */
    class PropertyExistValidator implements Validator {

        /**vaadin serial id. */
        private static final long serialVersionUID = -2500052661735259675L;

        /**
         * @see com.vaadin.data.Validator#validate(java.lang.Object)
         */
        public void validate(Object value) throws InvalidValueException {

            if (value == null) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_PROPERTY_VALIDATION_EMPTY_0));
            }
            String propName = (String)value;
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(propName)) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_PROPERTY_VALIDATION_EMPTY_0));
            }
            try {
                if (A_CmsUI.getCmsObject().readPropertyDefinition(propName) != null) {
                    throw new InvalidValueException(
                        CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_PROPERTY_VALIDATION_ALREADY_EXIST_0));
                }
            } catch (CmsException e) {
                //should not happen (non existing property leads to null, not throwing exception.)
            }

            try {
                CmsPropertyDefinition.checkPropertyName(propName);
            } catch (CmsIllegalArgumentException e) {
                throw new InvalidValueException(
                    CmsVaadinUtils.getMessageText(Messages.GUI_DATABASEAPP_PROPERTY_VALIDATION_NOTVALID_0));
            }
        }

    }

    /**vaadin serial id.*/
    private static final long serialVersionUID = -5454565964997277536L;

    /**New property name field. */
    protected TextField m_newProperty;

    /**ok button. */
    private Button m_ok;

    /**cancel button. */
    private Button m_cancel;

    /**
     * public constructor.<p>
     * @param window to be closed
     * @param table to be updated
     */
    public CmsAddPropertyDefinitionDialog(final Window window, final CmsPropertyTable table) {
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);

        m_ok.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -7356827828386377748L;

            public void buttonClick(ClickEvent event) {

                m_newProperty.removeAllValidators();
                m_newProperty.addValidator(new PropertyExistValidator());
                if (m_newProperty.isValid()) {

                    saveProperty();
                    table.init();
                    window.close();
                }
            }
        });

        m_cancel.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -3198675226086758775L;

            public void buttonClick(ClickEvent event) {

                window.close();

            }
        });

    }

    /**
     * saves the new property.<p>
     */
    protected void saveProperty() {

        try {
            A_CmsUI.getCmsObject().createPropertyDefinition(m_newProperty.getValue());
        } catch (CmsException e) {
            //
        }
    }

}
