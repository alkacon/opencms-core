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

import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.OpenCmsTheme;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.Validator.InvalidValueException;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.TextField;

/**
 * The principal select widget.<p>
 */
public class CmsPrincipalSelect extends CustomComponent implements Field<String>, I_CmsPrincipalSelect {

    /**
     * Handles the principal selection.<p>
     */
    public interface I_PrincipalSelectHandler {

        /**
         * Called to select a principal.<p>
         *
         * @param principalType the principal type
         * @param principalName the principal name
         */
        void onPrincipalSelect(String principalType, String principalName);
    }

    /** Type of principal. */
    public static enum PrincipalType {
        /** Groups. */
        group,

        /** Users. */
        user,

        /** Roles. */
        role;
    }

    /** The widget types. */
    public static enum WidgetType {

        /** Select groups only. */
        groupwidget(PrincipalType.group),
        /** Select any principal. */
        principalwidget(PrincipalType.group, PrincipalType.user, PrincipalType.role),
        /** Select users only. */
        userwidget(PrincipalType.user);

        private Set<PrincipalType> m_principalTypes;

        private WidgetType(PrincipalType... principalTypes) {

            m_principalTypes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(principalTypes)));
        }

        public Set<PrincipalType> getPrincipalTypes() {

            return m_principalTypes;
        }

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
    private I_PrincipalSelectHandler m_selectHandler;

    /** The open principal select dialog button. */
    private Button m_selectPrincipalButton;

    /** The widget type. */
    private WidgetType m_widgetType;

    /** The principal select dialog window. */
    private Window m_window;

    /** The main layout. */
    private HorizontalLayout m_main;

    /**Indicates if web ous should be included. */
    private boolean m_includeWebOus = true;

    /** Controls whether only real users/groups or also pseudo-principals like ALL_OTHERS should be shown. */
    private boolean m_realOnly;

    /**Ou. */
    private String m_ou;

    /** Is ou change enabled?*/
    private boolean m_ouChangeEnabled = true;

    /** True if role selection should be allowed. */
    private boolean m_roleSelectionAllowed;

    /**
     * Constructor.<p>
     */
    public CmsPrincipalSelect() {

        m_main = new HorizontalLayout();
        m_main.setSpacing(true);
        m_main.setWidth("100%");
        setCompositionRoot(m_main);

        m_widgetType = WidgetType.principalwidget;

        ComboBox principalTypeSelect = new ComboBox();
        principalTypeSelect.setWidth("150px");
        Map<String, String> principalTypes = new LinkedHashMap<String, String>();
        principalTypes.put(
            I_CmsPrincipal.PRINCIPAL_USER,
            CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_USER_0));
        principalTypes.put(
            I_CmsPrincipal.PRINCIPAL_GROUP,
            CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_GROUP_0));
        CmsVaadinUtils.prepareComboBox(principalTypeSelect, principalTypes);

        principalTypeSelect.setNewItemsAllowed(false);
        principalTypeSelect.setNullSelectionAllowed(false);
        principalTypeSelect.select(I_CmsPrincipal.PRINCIPAL_USER);
        m_main.addComponent(principalTypeSelect);
        m_principalTypeSelect = principalTypeSelect;

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
     * @see com.vaadin.v7.data.Property.ValueChangeNotifier#addListener(com.vaadin.v7.data.Property.ValueChangeListener)
     */
    @SuppressWarnings({"deprecation", "javadoc"})
    public void addListener(com.vaadin.v7.data.Property.ValueChangeListener listener) {

        m_principalName.addListener(listener);
    }

    /**
     * @see com.vaadin.data.Validatable#addValidator(com.vaadin.data.Validator)
     */
    public void addValidator(Validator validator) {

        m_principalName.addValidator(validator);
    }

    /**
     * @see com.vaadin.v7.data.Property.ValueChangeNotifier#addValueChangeListener(com.vaadin.v7.data.Property.ValueChangeListener)
     */
    public void addValueChangeListener(com.vaadin.v7.data.Property.ValueChangeListener listener) {

        m_principalName.addValueChangeListener(listener);
    }

    /**
     * @see com.vaadin.v7.ui.Field#clear()
     */
    public void clear() {

        m_principalName.clear();
    }

    /**
     * @see com.vaadin.v7.data.Buffered#commit()
     */
    public void commit() throws SourceException, InvalidValueException {

        m_principalName.commit();
    }

    /**
     * @see com.vaadin.v7.data.Buffered#discard()
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
     * @see com.vaadin.v7.data.Property.Viewer#getPropertyDataSource()
     */
    public Property getPropertyDataSource() {

        return m_principalName.getPropertyDataSource();
    }

    /**
     * @see com.vaadin.v7.ui.Field#getRequiredError()
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
     * @see com.vaadin.v7.data.Property#getType()
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
     * @see com.vaadin.v7.data.Property#getValue()
     */
    public String getValue() {

        return m_principalName.getValue();
    }

    /**
     * @see org.opencms.ui.dialogs.permissions.I_CmsPrincipalSelect#handlePrincipal(org.opencms.security.I_CmsPrincipal)
     */
    public void handlePrincipal(I_CmsPrincipal principal) {

        if ((principal != null) && !Objects.equals(CmsPrincipal.getType(principal), m_principalTypeSelect.getValue())) {
            m_principalTypeSelect.setValue(CmsPrincipal.getType(principal));
        }
        setValue(principal.getName());

    }

    /**
     * @see com.vaadin.v7.data.Buffered#isBuffered()
     */
    public boolean isBuffered() {

        return m_principalName.isBuffered();
    }

    /**
     * @see com.vaadin.v7.ui.Field#isEmpty()
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
     * @see com.vaadin.v7.data.BufferedValidatable#isInvalidCommitted()
     */
    public boolean isInvalidCommitted() {

        return m_principalName.isInvalidCommitted();
    }

    /**
     * @see com.vaadin.v7.data.Buffered#isModified()
     */
    public boolean isModified() {

        return m_principalName.isModified();
    }

    public boolean isReadOnly() {

        return super.isReadOnly();
    }

    /**
     * @see com.vaadin.v7.ui.Field#isRequired()
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
     * @see com.vaadin.v7.data.Property.ValueChangeNotifier#removeListener(com.vaadin.v7.data.Property.ValueChangeListener)
     */
    @SuppressWarnings({"deprecation", "javadoc"})
    public void removeListener(com.vaadin.v7.data.Property.ValueChangeListener listener) {

        m_principalName.removeListener(listener);
    }

    /**
     * @see com.vaadin.data.Validatable#removeValidator(com.vaadin.data.Validator)
     */
    public void removeValidator(Validator validator) {

        m_principalName.removeValidator(validator);
    }

    /**
     * @see com.vaadin.v7.data.Property.ValueChangeNotifier#removeValueChangeListener(com.vaadin.v7.data.Property.ValueChangeListener)
     */
    public void removeValueChangeListener(com.vaadin.v7.data.Property.ValueChangeListener listener) {

        m_principalName.removeValueChangeListener(listener);
    }

    /**
     * @see com.vaadin.v7.data.Buffered#setBuffered(boolean)
     */
    public void setBuffered(boolean buffered) {

        m_principalName.setBuffered(buffered);
    }

    /**
     * Set if web Ous should be included. Default behavior is true.<p>
     *
     * @param include boolean
     */
    public void setIncludeWebOus(boolean include) {

        m_includeWebOus = include;
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
     * Set the ou.
     *
     * @param ou to choose principals for
     */
    public void setOU(String ou) {

        m_ou = ou;
    }

    public void setOuChangeEnabled(boolean enabled) {

        m_ouChangeEnabled = enabled;
    }

    /**
     * Sets the principal type and clears the name.<p>
     *
     * @param type the principal type
     */
    public void setPrincipalType(String type) {

        m_principalTypeSelect.setValue(type);
    }

    /**
     * @see com.vaadin.v7.data.Property.Viewer#setPropertyDataSource(com.vaadin.v7.data.Property)
     */
    public void setPropertyDataSource(Property newDataSource) {

        m_principalName.setPropertyDataSource(newDataSource);
    }

    public void setReadOnly(boolean readOnly) {

        super.setReadOnly(readOnly);
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
     * @see com.vaadin.v7.ui.Field#setRequired(boolean)
     */
    public void setRequired(boolean required) {

        m_principalName.setRequired(required);
    }

    /**
     * @see com.vaadin.v7.ui.Field#setRequiredError(java.lang.String)
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
        m_roleSelectionAllowed = editRoles;
        m_principalTypeSelect.setNewItemsAllowed(!editRoles);

    }

    /**
     * Sets the principal select handler.<p>
     *
     * @param selectHandler the principal select handler
     */
    public void setSelectHandler(I_PrincipalSelectHandler selectHandler) {

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
     * @see com.vaadin.v7.data.Property#setValue(java.lang.Object)
     */
    public void setValue(String newValue) throws com.vaadin.v7.data.Property.ReadOnlyException {

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
        m_principalTypeSelect.setValue(
            m_widgetType.equals(WidgetType.groupwidget)
            ? I_CmsPrincipal.PRINCIPAL_GROUP
            : I_CmsPrincipal.PRINCIPAL_USER);
    }

    /**
     * @see com.vaadin.data.Validatable#validate()
     */
    public void validate() throws InvalidValueException {

        m_principalName.validate();
    }

    /**
     * @see com.vaadin.v7.data.Property.ValueChangeListener#valueChange(com.vaadin.v7.data.Property.ValueChangeEvent)
     */
    public void valueChange(com.vaadin.v7.data.Property.ValueChangeEvent event) {

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
    protected void setPrincipal(int type, String principalName) {

        m_principalName.setValue(principalName);

        String typeName = null;
        switch (type) {
            case 0:
                typeName = I_CmsPrincipal.PRINCIPAL_GROUP;
                break;
            case 1:
            default:
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

        CmsPrincipalSelectDialog dialog;

        m_window = CmsBasicDialog.prepareWindow(DialogWidth.max);
        CmsPrincipalSelect.PrincipalType defaultType = CmsPrincipalSelect.PrincipalType.group;
        if (m_principalTypeSelect.getValue().equals(I_CmsPrincipal.PRINCIPAL_USER)) {
            defaultType = CmsPrincipalSelect.PrincipalType.user;
        } else if (m_principalTypeSelect.getValue().equals(CmsRole.PRINCIPAL_ROLE)) {
            defaultType = CmsPrincipalSelect.PrincipalType.role;
        }

        dialog = new CmsPrincipalSelectDialog(
            this,
            m_ou == null ? A_CmsUI.getCmsObject().getRequestContext().getOuFqn() : m_ou,
            m_window,
            m_widgetType,
            m_realOnly,
            defaultType,
            m_includeWebOus,
            m_roleSelectionAllowed);

        dialog.setOuComboBoxEnabled(m_ouChangeEnabled);

        m_window.setCaption(
            CmsVaadinUtils.getMessageText(
                org.opencms.workplace.commons.Messages.GUI_PRINCIPALSELECTION_LIST_ACTION_SELECT_NAME_0));
        m_window.setContent(dialog);
        A_CmsUI.get().addWindow(m_window);
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
