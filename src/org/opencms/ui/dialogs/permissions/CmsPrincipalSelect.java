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

package org.opencms.ui.dialogs.permissions;

import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;

/**
 * The principal select widget.<p>
 */
public class CmsPrincipalSelect extends CustomComponent implements Field<String> {

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

    /** The widget types. */
    public static enum WidgetType {
        /** Select groups only. */
        groupwidget,
        /** Select any principal. */
        principalwidget,
        /** Select users only. */
        userwidget
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

    /** The widget type. */
    private WidgetType m_widgetType;

    /** The principal select dialog window. */
    private Window m_window;

    /** The main layout. */
    private HorizontalLayout m_main;

    /** Controls whether only real users/groups or also pseudo-principals like ALL_OTHERS should be shown. */
    private boolean m_realOnly;

    /**
     * Constructor.<p>
     */
    public CmsPrincipalSelect() {

        m_main = new HorizontalLayout();
        m_main.setSpacing(true);
        m_main.setWidth("100%");
        setCompositionRoot(m_main);

        m_widgetType = WidgetType.principalwidget;

        m_principalTypeSelect = new ComboBox();
        m_principalTypeSelect.setWidth("150px");
        Map<String, String> principalTypes = new LinkedHashMap<String, String>();
        principalTypes.put(
            I_CmsPrincipal.PRINCIPAL_USER,
            CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_USER_0));
        principalTypes.put(
            I_CmsPrincipal.PRINCIPAL_GROUP,
            CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_GROUP_0));
        CmsVaadinUtils.prepareComboBox(m_principalTypeSelect, principalTypes);

        m_principalTypeSelect.setNewItemsAllowed(false);
        m_principalTypeSelect.setNullSelectionAllowed(false);
        m_principalTypeSelect.select(I_CmsPrincipal.PRINCIPAL_USER);
        m_main.addComponent(m_principalTypeSelect);

        m_principalName = new TextField();
        m_principalName.setWidth("100%");
        m_main.addComponent(m_principalName);
        m_main.setExpandRatio(m_principalName, 2);

        m_selectPrincipalButton = new Button(FontAwesome.USER);
        m_selectPrincipalButton.addStyleName(OpenCmsTheme.BUTTON_ICON);
        m_selectPrincipalButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                openPrincipalSelect();
            }
        });
        m_main.addComponent(m_selectPrincipalButton);
    }

    /**
     * @see com.vaadin.data.Property.ValueChangeNotifier#addListener(com.vaadin.data.Property.ValueChangeListener)
     */
    @SuppressWarnings({"deprecation", "javadoc"})
    public void addListener(com.vaadin.data.Property.ValueChangeListener listener) {

        m_principalName.addListener(listener);
    }

    /**
     * @see com.vaadin.data.Validatable#addValidator(com.vaadin.data.Validator)
     */
    public void addValidator(Validator validator) {

        m_principalName.addValidator(validator);
    }

    /**
     * @see com.vaadin.data.Property.ValueChangeNotifier#addValueChangeListener(com.vaadin.data.Property.ValueChangeListener)
     */
    public void addValueChangeListener(com.vaadin.data.Property.ValueChangeListener listener) {

        m_principalName.addValueChangeListener(listener);
    }

    /**
     * @see com.vaadin.ui.Field#clear()
     */
    public void clear() {

        m_principalName.clear();
    }

    /**
     * @see com.vaadin.data.Buffered#commit()
     */
    public void commit() throws SourceException, InvalidValueException {

        m_principalName.commit();
    }

    /**
     * @see com.vaadin.data.Buffered#discard()
     */
    public void discard() throws SourceException {

        m_principalName.discard();
    }

    /**
     * @see com.vaadin.ui.AbstractComponent#focus()
     */
    @Override
    public void focus() {

        m_principalName.focus();
    }

    /**
     * @see com.vaadin.data.Property.Viewer#getPropertyDataSource()
     */
    public Property getPropertyDataSource() {

        return m_principalName.getPropertyDataSource();
    }

    /**
     * @see com.vaadin.ui.Field#getRequiredError()
     */
    public String getRequiredError() {

        return m_principalName.getRequiredError();
    }

    /**
     * @see com.vaadin.ui.Component.Focusable#getTabIndex()
     */
    public int getTabIndex() {

        return m_principalName.getTabIndex();
    }

    /**
     * @see com.vaadin.data.Property#getType()
     */
    public Class<? extends String> getType() {

        return m_principalName.getType();
    }

    /**
     * @see com.vaadin.data.Validatable#getValidators()
     */
    public Collection<Validator> getValidators() {

        return m_principalName.getValidators();
    }

    /**
     * @see com.vaadin.data.Property#getValue()
     */
    public String getValue() {

        return m_principalName.getValue();
    }

    /**
     * @see com.vaadin.data.Buffered#isBuffered()
     */
    public boolean isBuffered() {

        return m_principalName.isBuffered();
    }

    /**
     * @see com.vaadin.ui.Field#isEmpty()
     */
    public boolean isEmpty() {

        return m_principalName.isEmpty();
    }

    /**
     * @see com.vaadin.data.Validatable#isInvalidAllowed()
     */
    public boolean isInvalidAllowed() {

        return m_principalName.isInvalidAllowed();
    }

    /**
     * @see com.vaadin.data.BufferedValidatable#isInvalidCommitted()
     */
    public boolean isInvalidCommitted() {

        return m_principalName.isInvalidCommitted();
    }

    /**
     * @see com.vaadin.data.Buffered#isModified()
     */
    public boolean isModified() {

        return m_principalName.isModified();
    }

    /**
     * @see com.vaadin.ui.Field#isRequired()
     */
    public boolean isRequired() {

        return m_principalName.isRequired();
    }

    /**
     * @see com.vaadin.data.Validatable#isValid()
     */
    public boolean isValid() {

        return m_principalName.isValid();
    }

    /**
     * @see com.vaadin.data.Validatable#removeAllValidators()
     */
    public void removeAllValidators() {

        m_principalName.removeAllValidators();
    }

    /**
     * @see com.vaadin.data.Property.ValueChangeNotifier#removeListener(com.vaadin.data.Property.ValueChangeListener)
     */
    @SuppressWarnings({"deprecation", "javadoc"})
    public void removeListener(com.vaadin.data.Property.ValueChangeListener listener) {

        m_principalName.removeListener(listener);
    }

    /**
     * @see com.vaadin.data.Validatable#removeValidator(com.vaadin.data.Validator)
     */
    public void removeValidator(Validator validator) {

        m_principalName.removeValidator(validator);
    }

    /**
     * @see com.vaadin.data.Property.ValueChangeNotifier#removeValueChangeListener(com.vaadin.data.Property.ValueChangeListener)
     */
    public void removeValueChangeListener(com.vaadin.data.Property.ValueChangeListener listener) {

        m_principalName.removeValueChangeListener(listener);
    }

    /**
     * @see com.vaadin.data.Buffered#setBuffered(boolean)
     */
    public void setBuffered(boolean buffered) {

        m_principalName.setBuffered(buffered);
    }

    /**
     * @see com.vaadin.data.Validatable#setInvalidAllowed(boolean)
     */
    public void setInvalidAllowed(boolean invalidValueAllowed) throws UnsupportedOperationException {

        m_principalName.setInvalidAllowed(invalidValueAllowed);
    }

    /**
     * @see com.vaadin.data.BufferedValidatable#setInvalidCommitted(boolean)
     */
    public void setInvalidCommitted(boolean isCommitted) {

        m_principalName.setInvalidCommitted(isCommitted);
    }

    /**
     * Enable layout margins. Affects all four sides of the layout.
     * This will tell the client-side implementation to leave extra space around the layout.
     * The client-side implementation decides the actual amount, and it can vary between themes.<p>
     *
     * @param enabled <code>true</code> if margins should be enabled on all sides, false to disable all margins
     */
    public void setMargin(boolean enabled) {

        ((HorizontalLayout)getCompositionRoot()).setMargin(enabled);
    }

    /**
     * Sets the principal type and clears the name.<p>
     *
     * @param type the principal type
     */
    public void setPrincipalType(String type) {

        m_principalName.setValue("");
        m_principalTypeSelect.setValue(type);
    }

    /**
     * @see com.vaadin.data.Property.Viewer#setPropertyDataSource(com.vaadin.data.Property)
     */
    public void setPropertyDataSource(Property newDataSource) {

        m_principalName.setPropertyDataSource(newDataSource);
    }

    /**
     * Controls whether only real users/groups or also pseudo-principals like ALL_OTHERS should be shown.
     *
     *  @param realOnly if true, only real users / groups will be shown
     */
    public void setRealPrincipalsOnly(boolean realOnly) {

        m_realOnly = realOnly;
    }

    /**
     * @see com.vaadin.ui.Field#setRequired(boolean)
     */
    public void setRequired(boolean required) {

        m_principalName.setRequired(required);
    }

    /**
     * @see com.vaadin.ui.Field#setRequiredError(java.lang.String)
     */
    public void setRequiredError(String requiredMessage) {

        m_principalName.setRequiredError(requiredMessage);
    }

    /**
     * Enables/disables selection of the 'Roles' prinipal type.<p>
     *
     * @param editRoles true if the user should be allowed to select roles
     */
    public void setRoleSelectionAllowed(boolean editRoles) {

        m_principalTypeSelect.removeItem(CmsRole.PRINCIPAL_ROLE);
        if (editRoles) {
            Item item = m_principalTypeSelect.addItem(CmsRole.PRINCIPAL_ROLE);
            String roleText = CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_ROLE_0);
            item.getItemProperty(CmsVaadinUtils.PROPERTY_LABEL).setValue(roleText);
        }
        m_principalTypeSelect.setNewItemsAllowed(!editRoles);

    }

    /**
     * Sets the principal select handler.<p>
     *
     * @param selectHandler the principal select handler
     */
    public void setSelectHandler(PrincipalSelectHandler selectHandler) {

        m_selectHandler = selectHandler;
        enableSetButton(m_selectHandler != null);
    }

    /**
     * @see com.vaadin.ui.Component.Focusable#setTabIndex(int)
     */
    public void setTabIndex(int tabIndex) {

        m_principalName.setTabIndex(tabIndex);
    }

    /**
     * @see com.vaadin.data.Property#setValue(java.lang.Object)
     */
    public void setValue(String newValue) throws com.vaadin.data.Property.ReadOnlyException {

        m_principalName.setValue(newValue);
    }

    /**
     * Sets the widget type.<p>
     *
     * @param type the widget type
     */
    public void setWidgetType(WidgetType type) {

        m_widgetType = type;
        m_principalTypeSelect.setVisible(m_widgetType.equals(WidgetType.principalwidget));
    }

    /**
     * @see com.vaadin.data.Validatable#validate()
     */
    public void validate() throws InvalidValueException {

        m_principalName.validate();
    }

    /**
     * @see com.vaadin.data.Property.ValueChangeListener#valueChange(com.vaadin.data.Property.ValueChangeEvent)
     */
    public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {

        m_principalName.valueChange(event);
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
            String principalType = (String)m_principalTypeSelect.getValue();
            if (CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_ROLE_0).equals(
                principalType)) {
                principalType = CmsRole.PRINCIPAL_ROLE;
            }
            m_selectHandler.onPrincipalSelect(principalType, m_principalName.getValue());
        }
    }

    /**
     * Opens the principal select dialog window.<p>
     */
    void openPrincipalSelect() {

        String parameters = "?type="
            + m_widgetType.name()
            + "&realonly="
            + m_realOnly
            + "&flags=null&action=listindependentaction&useparent=true&listaction=";
        if ((m_widgetType.equals(WidgetType.principalwidget)
            && I_CmsPrincipal.PRINCIPAL_GROUP.equals(m_principalTypeSelect.getValue()))
            || m_widgetType.equals(WidgetType.groupwidget)) {
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
        m_window.setCaption(
            CmsVaadinUtils.getMessageText(
                org.opencms.workplace.commons.Messages.GUI_PRINCIPALSELECTION_LIST_ACTION_SELECT_NAME_0));
        m_window.setContent(dialog);
        A_CmsUI.get().addWindow(m_window);
        CmsPrincipalSelectExtension.getInstance().setCurrentSelect(this);
    }

    /**
     * Sets the add permission button enabled.<p>
     *
     * @param enabled <code>true</code> to enable the button
     */
    private void enableSetButton(boolean enabled) {

        if (enabled) {
            if (m_addPermissionSetButton == null) {
                m_addPermissionSetButton = new Button(FontAwesome.PLUS);
                m_addPermissionSetButton.addStyleName(OpenCmsTheme.BUTTON_ICON);
                m_addPermissionSetButton.addClickListener(new ClickListener() {

                    private static final long serialVersionUID = 1L;

                    public void buttonClick(ClickEvent event) {

                        onSelect();
                    }
                });
                m_main.addComponent(m_addPermissionSetButton);
            }
        } else if (m_addPermissionSetButton != null) {
            m_main.removeComponent(m_addPermissionSetButton);
            m_addPermissionSetButton = null;
        }
    }
}
