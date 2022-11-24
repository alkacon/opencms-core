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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleAsPrincipal;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.dialogs.CmsEmbeddedDialogContext;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.I_PrincipalSelectHandler;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.PrincipalType;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.WidgetType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.logging.Log;

import com.vaadin.server.FontIcon;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property.ValueChangeEvent;
import com.vaadin.v7.data.Property.ValueChangeListener;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.ui.ComboBox;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the dialog to show the principal table.<p>
 */
public class CmsPrincipalSelectDialog extends CmsBasicDialog {

    /** Parameter key OU. */
    public static final String PARAM_OU = "ou";

    /** Parameter key widget type. */
    public static final String PARAM_TYPE = "type";

    /** Parameter key start view type. */
    public static final String PARAM_START_TYPE = "starttype";

    /** Parameter key real groups only. */
    public static final String PARAM_REAL_ONLY = "realonly";

    /** The dialog id. */
    public static final String DIALOG_ID = "principalselect";

    /**vaadin serial id.*/
    private static final long serialVersionUID = 4650407086145654695L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPrincipalSelectDialog.class);

    /**Icon property. */
    private static String ID_ICON = "icon";

    /**Caption property. */
    private static String ID_CAPTION = "caption";

    /**Descrpition property. */
    private static String ID_DESC = "desc";

    /**Organizational unit property. */
    private static String ID_OU = "ou";

    /**CmsObject.*/
    CmsObject m_cms;

    /**Calling vaadin component.*/
    I_CmsPrincipalSelect m_selectField;

    /**Vaadin component.*/
    private Button m_closeButton;

    /**Vaadin component.*/
    ComboBox m_typeCombo;

    /**Vaadin component.*/
    private TextField m_tableFilter;

    /**Vaadin component.*/
    CmsPrincipalTable m_table;

    /**Ou name. */
    private String m_ou;

    /**Type to display. */
    WidgetType m_type;

    /**Only show real principals? */
    private boolean m_realOnly;

    /**Vaadin component.*/
    private ComboBox m_ouCombo;

    /** The principal select handler. */
    private I_PrincipalSelectHandler m_selectHandler;

    /**
     * public constructor.<p>
     *
     * @param cmsPrincipalSelect calling vaadin component
     * @param ou the current OU
     * @param window window to be closed after finishing
     * @param widgetType type of principal to be shown
     * @param realOnly true, only show real principals
     * @param defaultView default mode to open
     */
    public CmsPrincipalSelectDialog(
        I_CmsPrincipalSelect cmsPrincipalSelect,
        String ou,
        final Window window,
        WidgetType widgetType,
        boolean realOnly,
        CmsPrincipalSelect.PrincipalType defaultView) {

        this(cmsPrincipalSelect, ou, window, widgetType, realOnly, defaultView, true, false);

    }

    /**
     * public constructor.<p>
     *
     * @param cmsPrincipalSelect calling vaadin component
     * @param ou the current OU
     * @param window window to be closed after finishing
     * @param widgetType type of principal to be shown
     * @param realOnly true, only show real principals
     * @param defaultView default mode to open
     * @param includeWebOus if web OUs should be included
     * @param roleSelectionAllowed if true, selecting roles should be allowed (if the widget type allows for roles)
     */
    public CmsPrincipalSelectDialog(
        I_CmsPrincipalSelect cmsPrincipalSelect,
        String ou,
        final Window window,
        WidgetType widgetType,
        boolean realOnly,
        CmsPrincipalSelect.PrincipalType defaultView,
        boolean includeWebOus,
        boolean roleSelectionAllowed) {

        m_ou = ou;
        m_type = widgetType;
        m_realOnly = realOnly;
        try {
            m_cms = A_CmsUI.getCmsObject();

            m_selectField = cmsPrincipalSelect;

            m_ouCombo = CmsVaadinUtils.getOUComboBox(m_cms, m_cms.getRequestContext().getOuFqn(), null, includeWebOus);
            m_ouCombo.select(m_ou);

            IndexedContainer data;

            data = getContainerForType(defaultView, m_realOnly, (String)m_ouCombo.getValue());
            m_table = new CmsPrincipalTable(this, data, ID_ICON, ID_CAPTION, ID_DESC, ID_OU);
            m_table.setColumnHeader(
                ID_CAPTION,
                CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUP_NAME_0));
            m_table.setColumnHeader(
                ID_DESC,
                CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUP_DESCRIPTION_0));
            m_table.setColumnHeader(ID_OU, CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUP_OU_0));

            m_tableFilter = new TextField();
            m_tableFilter.setIcon(FontOpenCms.FILTER);
            m_tableFilter.setInputPrompt(
                Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
            m_tableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
            m_tableFilter.setWidth("200px");
            m_tableFilter.addTextChangeListener(new TextChangeListener() {

                private static final long serialVersionUID = 1L;

                public void textChange(TextChangeEvent event) {

                    m_table.filterTable(event.getText());
                }
            });
            VerticalLayout vl = new VerticalLayout();
            vl.setSpacing(true);
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);
            hl.setWidth("100%");
            m_typeCombo = new ComboBox();
            Label space = new Label();

            hl.addComponent(m_typeCombo);
            hl.addComponent(m_ouCombo);
            hl.addComponent(space);
            hl.addComponent(m_tableFilter);
            hl.setExpandRatio(space, 1);
            hl.setComponentAlignment(m_ouCombo, com.vaadin.ui.Alignment.MIDDLE_CENTER);
            vl.addComponent(hl);
            vl.addComponent(m_table);

            if (!OpenCms.getRoleManager().hasRole(m_cms, CmsRole.ACCOUNT_MANAGER)) {
                m_ouCombo.setValue(m_cms.getRequestContext().getOuFqn());
                m_ouCombo.setEnabled(false);
                m_typeCombo.setValue(WidgetType.groupwidget);
                m_typeCombo.setEnabled(false);
            }

            setContent(vl);
        } catch (CmsException e) {
            LOG.error("Can't read principals", e);
        }
        m_closeButton = new Button(CmsVaadinUtils.messageClose());
        addButton(m_closeButton);

        m_closeButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = -3702402223196220788L;

            public void buttonClick(ClickEvent event) {

                window.close();

            }
        });
        m_ouCombo.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -5035831853626955191L;

            public void valueChange(ValueChangeEvent event) {

                initTable((CmsPrincipalSelect.PrincipalType)m_typeCombo.getValue());

            }
        });
        m_typeCombo.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 645817336220889132L;

            public void valueChange(ValueChangeEvent event) {

                CmsPrincipalSelect.PrincipalType principalType = (CmsPrincipalSelect.PrincipalType)m_typeCombo.getValue();
                initTable(principalType);
                m_ouCombo.setVisible(principalType != PrincipalType.role);
            }

        });
        initTypeCombo(defaultView, roleSelectionAllowed);
    }

    /**
     * Opens the principal select dialog within an embedded dialog context.<p>
     *
     * @param dialogContext the dialog context
     * @param params the request parameters
     */
    public static void openEmbeddedDialog(final CmsEmbeddedDialogContext dialogContext, Map<String, String[]> params) {

        openEmbeddedDialog(dialogContext, params, true);
    }

    /**
     * Opens the principal select dialog within an embedded dialog context.<p>
     *
     * @param dialogContext the dialog context
     * @param params the request parameters
     * @param includeWebOus include WebOu?
     */
    public static void openEmbeddedDialog(
        final CmsEmbeddedDialogContext dialogContext,
        Map<String, String[]> params,
        boolean includeWebOus) {

        String[] param = params.get(PARAM_OU);
        String ou;
        if ((param != null) && (param.length >= 1)) {
            ou = param[0];
        } else {
            ou = dialogContext.getCms().getRequestContext().getCurrentUser().getOuFqn();
        }
        boolean realOnly;
        param = params.get(PARAM_REAL_ONLY);
        if ((param != null) && (param.length >= 1)) {
            realOnly = Boolean.parseBoolean(param[0]);
        } else {
            realOnly = true;
        }
        WidgetType type = WidgetType.groupwidget;
        param = params.get(PARAM_TYPE);
        if ((param != null) && (param.length >= 1)) {
            try {
                type = WidgetType.valueOf(param[0]);
            } catch (Exception e) {
                // ignore
            }
        }
        CmsPrincipalSelect.PrincipalType startType = null;
        param = params.get(PARAM_START_TYPE);
        if ((param != null) && (param.length >= 1)) {
            try {
                startType = CmsPrincipalSelect.PrincipalType.valueOf(param[0]);
            } catch (Exception e) {
                // ignore
            }
        }
        if (startType == null) {
            if ((type == WidgetType.principalwidget) || (type == WidgetType.groupwidget)) {
                startType = CmsPrincipalSelect.PrincipalType.group;
            } else if (type == WidgetType.userwidget) {
                startType = CmsPrincipalSelect.PrincipalType.user;
            }
        }
        Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
        dialogContext.setWindow(window);
        CmsPrincipalSelectDialog dialog = new CmsPrincipalSelectDialog(
            null,
            ou,
            window,
            type,
            realOnly,
            startType,
            includeWebOus,
            false);
        dialog.setSelectHandler(new I_PrincipalSelectHandler() {

            public void onPrincipalSelect(String principalType, String principalName) {

                dialogContext.selectString(principalName);
            }
        });
        window.setCaption(
            CmsVaadinUtils.getMessageText(
                org.opencms.workplace.commons.Messages.GUI_PRINCIPALSELECTION_LIST_ACTION_SELECT_NAME_0));
        window.setContent(dialog);
        A_CmsUI.get().addWindow(window);
    }

    /**
     * Opens the principal select dialog within an embedded dialog context.<p>
     *
     * @param dialogContext the dialog context
     * @param params the request parameters
     * @param includeWebOus include WebOu?
     */
    public static void openEmbeddedDialogV2(
        final CmsEmbeddedDialogContext dialogContext,
        Map<String, String> params,
        boolean includeWebOus) {

        String ou = params.get(PARAM_OU);
        if (ou == null) {
            ou = dialogContext.getCms().getRequestContext().getCurrentUser().getOuFqn();
        }
        boolean realOnly;
        String param = params.get(PARAM_REAL_ONLY);
        if (param != null) {
            realOnly = Boolean.parseBoolean(param);
        } else {
            realOnly = true;
        }
        WidgetType type = WidgetType.groupwidget;
        param = params.get(PARAM_TYPE);
        if (param != null) {
            try {
                type = WidgetType.valueOf(param);
            } catch (Exception e) {
                // ignore
            }
        }
        CmsPrincipalSelect.PrincipalType startType = null;
        param = params.get(PARAM_START_TYPE);
        if (param != null) {
            try {
                startType = CmsPrincipalSelect.PrincipalType.valueOf(param);
            } catch (Exception e) {
                // ignore
            }
        }
        if (startType == null) {
            if ((type == WidgetType.principalwidget) || (type == WidgetType.groupwidget)) {
                startType = CmsPrincipalSelect.PrincipalType.group;
            } else if (type == WidgetType.userwidget) {
                startType = CmsPrincipalSelect.PrincipalType.user;
            }
        }
        Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
        dialogContext.setWindow(window);
        CmsPrincipalSelectDialog dialog = new CmsPrincipalSelectDialog(
            null,
            ou,
            window,
            type,
            realOnly,
            startType,
            includeWebOus,
            false);
        dialog.setSelectHandler(new I_PrincipalSelectHandler() {

            public void onPrincipalSelect(String principalType, String principalName) {

                dialogContext.selectString(principalName);
            }
        });
        window.setCaption(
            CmsVaadinUtils.getMessageText(
                org.opencms.workplace.commons.Messages.GUI_PRINCIPALSELECTION_LIST_ACTION_SELECT_NAME_0));
        window.setContent(dialog);
        A_CmsUI.get().addWindow(window);
    }

    /**
     * Selects a principal and closes the dialog.<p>
     *
     * @param value the principal which was clicked
     */
    public void select(I_CmsPrincipal value) {

        if (m_selectField != null) {
            m_selectField.handlePrincipal(value);
        }
        if (m_selectHandler != null) {
            m_selectHandler.onPrincipalSelect(
                value.isGroup() ? I_CmsPrincipal.PRINCIPAL_GROUP : I_CmsPrincipal.PRINCIPAL_USER,
                value.getName());
        }
        m_closeButton.click();
    }

    /**
     * En/Disables the ou combo box.<p>
     *
     * @param enabled enabled boolean
     */
    public void setOuComboBoxEnabled(boolean enabled) {

        m_ouCombo.setEnabled(enabled);
    }

    /**
     * Sets the principal select handler.<p>
     *
     * @param selectHandler the principal select handler
     */
    public void setSelectHandler(I_PrincipalSelectHandler selectHandler) {

        m_selectHandler = selectHandler;
    }

    /**
     * Init table.<p>
     * @param type WidgetType to initialize
     */
    void initTable(CmsPrincipalSelect.PrincipalType type) {

        IndexedContainer data;
        try {
            data = getContainerForType(type, m_realOnly, (String)m_ouCombo.getValue());
            m_table.updateContainer(data);
            m_tableFilter.setValue("");
        } catch (CmsException e) {
            LOG.error("Can't read principals", e);
        }
    }

    /**
     * Returns the container for the currently selected Principal group.<p>
     *
     * @param type to be shown
     * @param realOnly true->get only real principals
     * @param ou ou
     * @return indexed container
     * @throws CmsException exception
     */
    private IndexedContainer getContainerForType(CmsPrincipalSelect.PrincipalType type, boolean realOnly, String ou)
    throws CmsException {

        IndexedContainer res = null;
        List<FontIcon> icons = new ArrayList<FontIcon>();

        if (type == PrincipalType.group) {
            List<CmsGroup> groups = OpenCms.getRoleManager().getManageableGroups(m_cms, ou, false);
            if (!realOnly) {
                groups.add(
                    0,
                    new CmsGroup(
                        CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID,
                        null,
                        CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_ALLOTHERS_0),
                        CmsVaadinUtils.getMessageText(
                            org.opencms.workplace.commons.Messages.GUI_DESCRIPTION_ALLOTHERS_0),
                        0));
                icons.add(new CmsCssIcon(OpenCmsTheme.ICON_PRINCIPAL_ALL));
                if (OpenCms.getRoleManager().hasRole(m_cms, CmsRole.VFS_MANAGER)) {
                    groups.add(
                        0,
                        new CmsGroup(
                            CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID,
                            null,
                            CmsVaadinUtils.getMessageText(
                                org.opencms.workplace.commons.Messages.GUI_LABEL_OVERWRITEALL_0),
                            CmsVaadinUtils.getMessageText(
                                org.opencms.workplace.commons.Messages.GUI_DESCRIPTION_OVERWRITEALL_0),
                            0));
                    icons.add(0, new CmsCssIcon(OpenCmsTheme.ICON_PRINCIPAL_OVERWRITE));
                }
            }

            res = CmsVaadinUtils.getPrincipalContainer(
                A_CmsUI.getCmsObject(),
                groups,
                ID_CAPTION,
                ID_DESC,
                ID_ICON,
                ID_OU,
                OpenCmsTheme.ICON_GROUP,
                icons);
        }
        if (type == PrincipalType.user) {
            List<CmsUser> users = OpenCms.getRoleManager().getManageableUsers(m_cms, ou, false, true);
            if (!realOnly) {
                CmsUser user = new CmsUser(
                    CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID,
                    CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_ALLOTHERS_0),
                    "",
                    "",
                    "",
                    "",
                    0,
                    0,
                    0,
                    null);
                user.setDescription(
                    CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_DESCRIPTION_ALLOTHERS_0));
                users.add(0, user);
                icons.add(new CmsCssIcon(OpenCmsTheme.ICON_PRINCIPAL_ALL));
                if (OpenCms.getRoleManager().hasRole(m_cms, CmsRole.VFS_MANAGER)) {
                    user = new CmsUser(
                        CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID,
                        CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_LABEL_OVERWRITEALL_0),
                        "",
                        "",
                        "",
                        "",
                        0,
                        0,
                        0,
                        null);
                    user.setDescription(
                        CmsVaadinUtils.getMessageText(
                            org.opencms.workplace.commons.Messages.GUI_DESCRIPTION_OVERWRITEALL_0));
                    users.add(0, user);
                    icons.add(0, new CmsCssIcon(OpenCmsTheme.ICON_PRINCIPAL_OVERWRITE));
                }
            }
            res = CmsVaadinUtils.getPrincipalContainer(
                A_CmsUI.getCmsObject(),
                users,
                ID_CAPTION,
                ID_DESC,
                ID_ICON,
                ID_OU,
                OpenCmsTheme.ICON_USER,
                icons);
        }

        if (type == PrincipalType.role) {

            List<CmsRole> roles = CmsRole.getSystemRoles();
            List<CmsRoleAsPrincipal> roleWrappers = roles.stream().map(role -> new CmsRoleAsPrincipal(role)).collect(
                Collectors.toList());
            res = CmsVaadinUtils.getPrincipalContainer(
                A_CmsUI.getCmsObject(),
                roleWrappers,
                ID_CAPTION,
                ID_DESC,
                ID_ICON,
                ID_OU,
                OpenCmsTheme.ICON_ROLE,
                icons);
        }

        return res;
    }

    /**
     * Init ComboBox for choosing type of principal.<p>
     * @param defaultType Default mode to open
     */
    private void initTypeCombo(CmsPrincipalSelect.PrincipalType defaultType, boolean roleSelectionAllowed) {

        IndexedContainer container = new IndexedContainer();

        container.addContainerProperty("caption", String.class, "");

        Item item = container.addItem(CmsPrincipalSelect.PrincipalType.group);
        item.getItemProperty("caption").setValue(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUP_0));

        item = container.addItem(CmsPrincipalSelect.PrincipalType.user);
        item.getItemProperty("caption").setValue(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_0));
        if (roleSelectionAllowed) {
            if ((m_type == null) || m_type.getPrincipalTypes().contains(CmsPrincipalSelect.PrincipalType.role)) {
                item = container.addItem(CmsPrincipalSelect.PrincipalType.role);
                String message = CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_ROLE_0);
                item.getItemProperty("caption").setValue(message);
            }
        }

        m_typeCombo.setContainerDataSource(container);
        m_typeCombo.select(defaultType);
        m_typeCombo.setEnabled((m_type == null) || (m_type.getPrincipalTypes().size() > 1));
        m_typeCombo.setItemCaptionPropertyId("caption");
        m_typeCombo.setNullSelectionAllowed(false);
        m_typeCombo.setNewItemsAllowed(false);

    }

}
