/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.property.definition;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.util.CmsStringUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for defining new properties.<p>
 */
public class CmsPropertyDefinitionView extends Composite {

    /** The UiBinder interface for this class. */
    interface I_CmsPropertyDefinitionViewUiBinder extends UiBinder<Widget, CmsPropertyDefinitionView> {
        // empty
    }

    /** The max allowed property name length. */
    private static final int MAX_PROPERTY_NAME_LENGTH = 128;

    /** The UiBinder instance for this widget. */
    private static I_CmsPropertyDefinitionViewUiBinder uiBinder = GWT.create(I_CmsPropertyDefinitionViewUiBinder.class);

    /** The Cancel button. */
    @UiField
    CmsPushButton m_cancelButton;

    /** The panel displaying the existing properties.<p> */
    @UiField
    Panel m_existingProperties;

    /** The set of existing properties. */
    Set<String> m_existingPropertyNames = new HashSet<String>();

    /** The OK button. */
    @UiField
    CmsPushButton m_okButton;

    /** The popup containing this widget. */
    CmsPopup m_popup;

    /** The input field for the property name. */
    @UiField
    CmsTextBox m_propertyNameField;

    /**
     * Creates a new instance.<p>
     *
     * @param existingProperties the names of already existing properties
     */
    public CmsPropertyDefinitionView(Collection<String> existingProperties) {

        initWidget(uiBinder.createAndBindUi(this));
        addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().highTextBoxes());
        for (String existingProperty : existingProperties) {
            m_existingProperties.add(new Label(existingProperty));
            m_existingPropertyNames.add(existingProperty);
        }
        m_propertyNameField.addKeyPressHandler(new KeyPressHandler() {

            public void onKeyPress(KeyPressEvent event) {

                int code = event.getNativeEvent().getKeyCode();
                if (code == 13) {
                    onClickOk(null);
                }
            }
        });
        m_propertyNameField.getTextBox().setMaxLength(MAX_PROPERTY_NAME_LENGTH);
    }

    /**
     * Closes the popup if possible.<p>
     */
    public void closePopup() {

        if (m_popup != null) {
            m_popup.hide();
            m_popup = null;
        }
    }

    /**
     * Gets the cancel button.<p>
     *
     * @return the cancel button
     */
    public CmsPushButton getCancelButton() {

        return m_cancelButton;

    }

    /**
     * Gets the OK button.<p>
     *
     * @return the OK button
     */
    public CmsPushButton getOkButton() {

        return m_okButton;
    }

    /**
     * Sets the popup which contains this widget.<p>
     *
     * @param popup the popup
     */
    public void setPopup(CmsPopup popup) {

        m_popup = popup;
    }

    /**
     * Button handler for the cancel button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_cancelButton")
    void onClickCancel(ClickEvent e) {

        closePopup();
    }

    /**
     * Button handler for the OK button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_okButton")
    void onClickOk(ClickEvent e) {

        String propName = m_propertyNameField.getFormValueAsString();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(propName)) {
            m_propertyNameField.setErrorMessage(CmsPropertyDefinitionMessages.messageEmpty());
            return;
        }
        String normalizedName = propName.trim();
        if (!isValidName(normalizedName)) {
            m_propertyNameField.setErrorMessage(CmsPropertyDefinitionMessages.messageInvalidName());
            return;
        }
        if (m_existingPropertyNames.contains(normalizedName)) {
            m_propertyNameField.setErrorMessage(CmsPropertyDefinitionMessages.alreadyExists());
            return;
        }
        savePropertyDefinitionAndQuit(normalizedName);
    }

    /**
     * Checks whether the property name is valid.<p>
     *
     * @param normalizedName the property name to check
     *
     * @return true if the name is valid
     */
    private boolean isValidName(String normalizedName) {

        return normalizedName.matches("^[a-zA-Z0-9_\\-\\.~\\$]+$");
    }

    /**
     * Saves the property definition and closes the dialog.<p>
     *
     * @param normalizedName the property name
     */
    private void savePropertyDefinitionAndQuit(final String normalizedName) {

        CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

            @Override
            public void execute() {

                start(200, true);
                CmsCoreProvider.getVfsService().createPropertyDefinition(normalizedName, this);
            }

            @Override
            protected void onResponse(Void result) {

                stop(false);
                closePopup();
            }

        };
        action.execute();
    }
}
