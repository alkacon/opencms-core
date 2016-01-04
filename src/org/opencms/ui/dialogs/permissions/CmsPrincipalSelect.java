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

package org.opencms.ui.dialogs.permissions;

import org.opencms.main.OpenCms;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;

import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/**
 * The principal select widget.<p>
 */
public class CmsPrincipalSelect extends CustomComponent {

    /**
     * Handles the principal selection.<p>
     */
    public interface PrincipalSelectHandler {

        /**
         * Called to select a principal.<p>
         *
         * @param principalType the principal type
         * @param principalName the principal name
         */
        void onPrincipalSelect(String principalType, String principalName);
    }

    /** The serial version id. */
    private static final long serialVersionUID = 6944968889428174262L;

    /** The add button. */
    private Button m_addPermissionSetButton;

    /** The principal name text field. */
    private TextField m_principalName;

    /** The type select box. */
    private ComboBox m_principalTypeSelect;

    /** The principal select handler. */
    private PrincipalSelectHandler m_selectHandler;

    /** The open principal select dialog button. */
    private Button m_selectPrincipalButton;

    /** The principal select dialog window. */
    private Window m_window;

    /**
     * Constructor.<p>
     */
    @SuppressWarnings("unused")
    public CmsPrincipalSelect() {

        HorizontalLayout main = new HorizontalLayout();
        main.setSpacing(true);
        main.setMargin(true);
        main.setWidth("100%");
        this.setCompositionRoot(main);
        m_principalTypeSelect = new ComboBox();
        m_principalTypeSelect.setWidth("150px");
        Map<String, String> principalTypes = new LinkedHashMap<String, String>();
        principalTypes.put(I_CmsPrincipal.PRINCIPAL_USER, "User");
        principalTypes.put(I_CmsPrincipal.PRINCIPAL_GROUP, "Group");
        CmsVaadinUtils.prepareComboBox(m_principalTypeSelect, principalTypes);
        m_principalTypeSelect.setNewItemsAllowed(false);
        m_principalTypeSelect.setNullSelectionAllowed(false);
        m_principalTypeSelect.select(I_CmsPrincipal.PRINCIPAL_USER);
        main.addComponent(m_principalTypeSelect);
        m_principalName = new TextField();
        main.addComponent(m_principalName);
        main.setWidth("100%");
        main.setExpandRatio(m_principalName, 2);

        m_selectPrincipalButton = new Button(FontAwesome.USER);
        m_selectPrincipalButton.addStyleName("borderless");
        m_selectPrincipalButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openPrincipalSelect();
            }
        });
        main.addComponent(m_selectPrincipalButton);

        m_addPermissionSetButton = new Button(FontAwesome.PLUS);
        m_addPermissionSetButton.addStyleName("borderless");
        m_addPermissionSetButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                onSelect();
            }
        });
        main.addComponent(m_addPermissionSetButton);

        new CmsPrincipalSelectExtension(this);
    }

    /**
     * Sets the principal select handler.<p>
     *
     * @param selectHandler the principal select handler
     */
    public void setSelectHandler(PrincipalSelectHandler selectHandler) {

        m_selectHandler = selectHandler;
    }

    /**
     * Closes the principal select dialog window if present.<p>
     */
    protected void closeWindow() {

        if (m_window != null) {
            m_window.close();
            m_window = null;
        }
    }

    /**
     * Sets the principal type and name.<p>
     *
     * @param type the principal type
     * @param principalName the principal name
     */
    @SuppressWarnings("incomplete-switch")
    protected void setPrincipal(int type, String principalName) {

        m_principalName.setValue(principalName);

        String typeName = null;
        switch (type) {
            case 0:
                typeName = I_CmsPrincipal.PRINCIPAL_GROUP;
                break;
            case 1:
                typeName = I_CmsPrincipal.PRINCIPAL_USER;
                break;
        }
        if (typeName != null) {
            m_principalTypeSelect.setValue(typeName);
        }
    }

    /**
     * Calls the principal select handler.<p>
     */
    void onSelect() {

        if (m_selectHandler != null) {
            m_selectHandler.onPrincipalSelect((String)m_principalTypeSelect.getValue(), m_principalName.getValue());
        }
    }

    /**
     * Opens the principal select dialog window.<p>
     */
    void openPrincipalSelect() {

        String parameters = "?type=principalwidget&flags=null&action=listindependentaction&useparent=true&listaction=";
        if (I_CmsPrincipal.PRINCIPAL_GROUP.equals(m_principalTypeSelect.getValue())) {
            parameters += "iag";
        } else {
            parameters += "iau";
        }
        BrowserFrame selectFrame = new BrowserFrame(
            "Select principal",
            new ExternalResource(
                OpenCms.getLinkManager().substituteLinkForUnknownTarget(
                    A_CmsUI.getCmsObject(),
                    "/system/workplace/commons/principal_selection.jsp") + parameters));
        selectFrame.setWidth("100%");
        selectFrame.setHeight("500px");
        CmsBasicDialog dialog = new CmsBasicDialog();
        dialog.setContent(selectFrame);
        m_window = CmsBasicDialog.prepareWindow();
        m_window.setCaption("Select principal");
        m_window.setContent(dialog);
        A_CmsUI.get().addWindow(m_window);
    }
}
