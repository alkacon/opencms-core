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
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.dialogs.permissions.CmsPrincipalSelect.WidgetType;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.FontIcon;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Class for the dialog to show the principal table.<p>
 */
public class CmsPrincipalSelectDialog extends CmsBasicDialog {

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
    CmsPrincipalSelect m_selectField;

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
    private CheckBox m_otherOUs;

    /**
     * public constructor.<p>
     *
     * @param cmsPrincipalSelect calling vaadin component
     * @param ou ou
     * @param window window to be closed after finishing
     * @param widgetType type of principal to be shown
     * @param realOnly true, only show real principals
     */
    public CmsPrincipalSelectDialog(
        CmsPrincipalSelect cmsPrincipalSelect,
        String ou,
        final Window window,
        WidgetType widgetType,
        boolean realOnly) {
        m_ou = ou;
        m_type = widgetType;
        m_realOnly = realOnly;
        try {
            m_cms = A_CmsUI.getCmsObject();
            m_selectField = cmsPrincipalSelect;
            IndexedContainer data;
            data = getContainerForType(m_type, m_realOnly, false);
            m_table = new CmsPrincipalTable(this, data, ID_ICON, ID_CAPTION, ID_DESC, ID_OU);
            m_table.setColumnHeader(ID_CAPTION, "Group");
            m_table.setColumnHeader(ID_DESC, "Beschreibung");

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
            m_otherOUs = new CheckBox();
            m_otherOUs.setValue(new Boolean(true));
            hl.addComponent(m_typeCombo);
            hl.addComponent(m_otherOUs);
            hl.addComponent(space);
            hl.addComponent(m_tableFilter);
            hl.setExpandRatio(space, 1);
            hl.setComponentAlignment(m_otherOUs, com.vaadin.ui.Alignment.MIDDLE_CENTER);
            vl.addComponent(hl);
            vl.addComponent(m_table);

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
        m_otherOUs.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = -5035831853626955191L;

            public void valueChange(ValueChangeEvent event) {

                initTable((WidgetType)m_typeCombo.getValue());

            }
        });
        m_typeCombo.addValueChangeListener(new ValueChangeListener() {

            private static final long serialVersionUID = 645817336220889132L;

            public void valueChange(ValueChangeEvent event) {

                initTable((WidgetType)m_typeCombo.getValue());
            }

        });
        initTypeCombo();
    }

    /**
     * Selects a principal and closes the dialog.<p>
     *
     * @param value the principal which was clicked
     */
    public void select(I_CmsPrincipal value) {

        m_selectField.setValue(value.getName());
        m_closeButton.click();
    }

    /**
     * Init table.<p>
     * @param type WidgetType to initialize
     */
    void initTable(WidgetType type) {

        m_otherOUs.setCaption(getCheckBoxCaption(type));
        IndexedContainer data;
        try {
            data = getContainerForType(type, m_realOnly, m_otherOUs.getValue().booleanValue());
            m_table.updateContainer(data);
            m_tableFilter.setValue("");
        } catch (CmsException e) {
            LOG.error("Can't read principals", e);
        }
    }

    /**
     * Sets the check box caption for viewing all OU's principal or not.<p>
     *
     * @param type WidgetType to initialize
     * @return Caption for check box
     */
    private String getCheckBoxCaption(WidgetType type) {

        if (type.equals(WidgetType.groupwidget)) {
            return CmsVaadinUtils.getMessageText(
                org.opencms.workplace.commons.Messages.GUI_GROUPS_DETAIL_HIDE_OTHEROU_NAME_0);
        }
        return CmsVaadinUtils.getMessageText(
            org.opencms.workplace.commons.Messages.GUI_USERS_DETAIL_SHOW_OTHEROU_NAME_0);
    }

    /**
     * Returns the container for the currently selected Principal group.<p>
     *
     * @param type to be shown
     * @param realOnly true->get only real principals
     * @param fromOtherOUs from outher ous?
     * @return indexed container
     * @throws CmsException exception
     */
    private IndexedContainer getContainerForType(WidgetType type, boolean realOnly, boolean fromOtherOUs)
    throws CmsException {

        IndexedContainer res = null;
        List<FontIcon> icon = new ArrayList<FontIcon>();
        if (type.equals(WidgetType.groupwidget) | type.equals(WidgetType.principalwidget)) {
            List<CmsGroup> groups = OpenCms.getRoleManager().getManageableGroups(m_cms, m_ou, fromOtherOUs);
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
                icon.add(new CmsCssIcon(OpenCmsTheme.ICON_PRINCIPAL_ALL));
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
                    icon.add(0, new CmsCssIcon(OpenCmsTheme.ICON_PRINCIPAL_OVERWRITE));
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
                icon);
        }
        if (type.equals(WidgetType.userwidget)) {
            List<CmsUser> users = OpenCms.getRoleManager().getManageableUsers(m_cms, m_ou, fromOtherOUs);
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
                icon.add(FontAwesome.EXCLAMATION_CIRCLE);
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
                    icon.add(0, FontAwesome.GLOBE);
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
                icon);
        }

        return res;
    }

    /**
     * Init ComboBox for choosing type of principal.<p>
     */
    private void initTypeCombo() {

        IndexedContainer container = new IndexedContainer();

        container.addContainerProperty("caption", String.class, "");

        Item item = container.addItem(WidgetType.groupwidget);
        item.getItemProperty("caption").setValue("Gruppe");

        item = container.addItem(WidgetType.userwidget);
        item.getItemProperty("caption").setValue("User");

        m_typeCombo.setContainerDataSource(container);
        m_typeCombo.select(m_type.equals(WidgetType.principalwidget) ? WidgetType.groupwidget : m_type);
        m_typeCombo.setEnabled(m_type.equals(WidgetType.principalwidget) | (m_type == null));
        m_typeCombo.setItemCaptionPropertyId("caption");
        m_typeCombo.setNullSelectionAllowed(false);
        m_typeCombo.setNewItemsAllowed(false);

    }
}
