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
import org.opencms.file.history.CmsHistoryPrincipal;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsPermissionSet;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.workplace.commons.Messages;

import java.util.Arrays;

import org.apache.commons.logging.Log;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.FontIcon;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * Displays the permission settings for a single principal.<p>
 */
public class CmsPermissionView extends CssLayout {

    /**
     * Permission change handler.<p>
     */
    public interface PermissionChangeHandler {

        /**
         * Called to delete a permission set.<p>
         *
         * @param principalType the principal type
         * @param principalName the principal name
         */
        void deletePermissionSet(String principalType, String principalName);

        /**
         * Called on view changes, allowing for resizing or centering.<p>
         */
        void onViewChange();

        /**
         * Sets a changed permission set.<p>
         *
         * @param principalType the principal type
         * @param principalName the principal name
         * @param allowed the allowed flag
         * @param denied the denied flag
         * @param flags the other flags
         */
        void setPermissions(String principalType, String principalName, int allowed, int denied, int flags);
    }

    /** The table field factory. */
    private static final TableFieldFactory FIELD_FACTORY = new DefaultFieldFactory() {

        private static final long serialVersionUID = 1L;

        /**
         * @see com.vaadin.ui.DefaultFieldFactory#createField(com.vaadin.data.Container, java.lang.Object, java.lang.Object, com.vaadin.ui.Component)
         */
        @Override
        public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {

            Field<?> result = null;
            if (PROPERTY_ALLOWED.equals(propertyId) || PROPERTY_DENIED.equals(propertyId)) {
                result = super.createField(container, itemId, propertyId, uiContext);
                result.setCaption("");
            }
            return result;
        }
    };

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPermissionView.class);

    /** The allowed table property id. */
    private static final String PROPERTY_ALLOWED = "allowed";

    /** The denied table property id. */
    private static final String PROPERTY_DENIED = "denied";

    /** The display allowed table property id. */
    private static final String PROPERTY_DISPLAY_ALLOWED = "display_allowed";

    /** The display denied table property id. */
    private static final String PROPERTY_DISPLAY_DENIED = "display_denied";

    /** The label table property id. */
    private static final String PROPERTY_LABEL = "label";

    /** The value table property id. */
    private static final String PROPERTY_VALUE = "value";

    /** The serial version id. */
    private static final long serialVersionUID = 3440901877277200393L;

    /** Constant for unknown type. */
    private static final String UNKNOWN_TYPE = "Unknown";

    /** The button bar. */
    private VerticalLayout m_buttonBar;

    /** The permission change handler. */
    private PermissionChangeHandler m_changeHandler;

    /** The delete button. */
    private Button m_deleteButton;

    /** The container for the details button.  */
    private CssLayout m_detailButtonContainer;

    /** The details button. */
    private Button m_details;

    /** The editable flag. */
    private boolean m_editable;

    /** The access control entry to display. */
    private CmsAccessControlEntry m_entry;

    /** The inherit check box. */
    private CheckBox m_inheritCheckbox;

    /** The inherited from label. */
    private Label m_inheritedFrom;

    /** The label. */
    private Label m_label;

    /** The overwrite check box. */
    private CheckBox m_overwriteCheckbox;

    /** The permissions table. */
    private Table m_permissions;

    /** The principal name. */
    private String m_principalName;

    /** The principal type. */
    private String m_principalType;

    /** The responsible check box. */
    private CheckBox m_responsibleCheckbox;

    /** The set button. */
    private Button m_setButton;

    /**
     * Constructor.<p>
     *
     * @param entry the access control entry
     * @param editable the editable flag
     * @param isFolder the is folder flag
     * @param inheritedFrom the inherited from path
     * @param changeHandler the change handler
     */
    public CmsPermissionView(
        CmsAccessControlEntry entry,
        boolean editable,
        boolean isFolder,
        String inheritedFrom,
        PermissionChangeHandler changeHandler) {
        m_changeHandler = changeHandler;
        m_editable = editable;
        m_entry = entry;
        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        CmsObject cms = A_CmsUI.getCmsObject();
        // get name and type of the current entry
        I_CmsPrincipal principal;
        try {
            principal = CmsPrincipal.readPrincipalIncludingHistory(cms, entry.getPrincipal());
        } catch (CmsException e) {
            principal = null;
            LOG.debug(e.getLocalizedMessage(), e);
        }

        m_principalName = (principal != null) ? principal.getName() : entry.getPrincipal().toString();
        String ou = null;
        String displayName;

        int flags = 0;
        if ((principal != null) && (principal instanceof CmsHistoryPrincipal)) {
            // there is a history principal entry, handle it
            if (principal.isGroup()) {
                String niceName = OpenCms.getWorkplaceManager().translateGroupName(principal.getName(), false);
                displayName = CmsVaadinUtils.getMessageText(
                    org.opencms.security.Messages.GUI_ORGUNIT_DISPLAY_NAME_2,
                    ((CmsHistoryPrincipal)principal).getDescription(),
                    niceName);
                ou = CmsOrganizationalUnit.getParentFqn(m_principalName);
                flags = CmsAccessControlEntry.ACCESS_FLAGS_GROUP;
            } else {
                displayName = ((CmsHistoryPrincipal)principal).getName();
                ou = CmsOrganizationalUnit.getParentFqn(m_principalName);
                flags = CmsAccessControlEntry.ACCESS_FLAGS_USER;
            }
        } else if ((principal != null) && principal.isGroup()) {
            String niceName = OpenCms.getWorkplaceManager().translateGroupName(principal.getName(), false);
            displayName = CmsVaadinUtils.getMessageText(
                org.opencms.security.Messages.GUI_ORGUNIT_DISPLAY_NAME_2,
                ((CmsGroup)principal).getDescription(A_CmsUI.get().getLocale()),
                niceName);
            ou = CmsOrganizationalUnit.getParentFqn(m_principalName);
            flags = CmsAccessControlEntry.ACCESS_FLAGS_GROUP;
        } else if ((principal != null) && principal.isUser()) {
            displayName = ((CmsUser)principal).getFullName();
            ou = CmsOrganizationalUnit.getParentFqn(m_principalName);
            flags = CmsAccessControlEntry.ACCESS_FLAGS_USER;
        } else if ((m_principalName != null)
            && m_principalName.equals(CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_ID.toString())) {
            m_principalName = CmsAccessControlEntry.PRINCIPAL_ALL_OTHERS_NAME;
            displayName = CmsVaadinUtils.getMessageText(Messages.GUI_LABEL_ALLOTHERS_0);
            m_responsibleCheckbox.setVisible(false);
            flags = CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS;
        } else if ((m_principalName != null)
            && m_principalName.equals(CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_ID.toString())) {
            m_principalName = CmsAccessControlEntry.PRINCIPAL_OVERWRITE_ALL_NAME;
            displayName = CmsVaadinUtils.getMessageText(Messages.GUI_LABEL_OVERWRITEALL_0);
            flags = CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL;
        } else {
            // check if it is the case of a role
            CmsRole role = CmsRole.valueOfId(entry.getPrincipal());
            if (role != null) {
                displayName = role.getName(A_CmsUI.get().getLocale());
                m_principalName = role.getRoleName();
                flags = CmsAccessControlEntry.ACCESS_FLAGS_ROLE;
            } else {
                displayName = entry.getPrincipal().toString();
            }
        }

        if ((flags > 0) && ((entry.getFlags() & flags) == 0)) {
            // the flag is set to the wrong principal type
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle(A_CmsUI.get().getLocale()).key(
                        Messages.ERR_INVALID_ACE_1,
                        entry.toString()));
            }
            entry = new CmsAccessControlEntry(
                entry.getResource(),
                entry.getPrincipal(),
                entry.getAllowedPermissions(),
                entry.getDeniedPermissions(),
                (entry.getFlags() | flags));
        } else if (entry.getFlags() < CmsAccessControlEntry.ACCESS_FLAGS_USER) {
            // the flag is set to NO principal type
            if (LOG.isErrorEnabled()) {
                LOG.error(
                    Messages.get().getBundle(A_CmsUI.get().getLocale()).key(
                        Messages.ERR_INVALID_ACE_1,
                        entry.toString()));
            }
            entry = new CmsAccessControlEntry(
                entry.getResource(),
                entry.getPrincipal(),
                entry.getAllowedPermissions(),
                entry.getDeniedPermissions(),
                (entry.getFlags() | CmsAccessControlEntry.ACCESS_FLAGS_GROUP));
        }

        m_principalType = getEntryType(entry.getFlags(), false);

        if (m_principalName == null) {
            m_principalName = "";
        }

        FontIcon icon = null;
        boolean isOverwriteAll = false;
        switch (flags) {
            case CmsAccessControlEntry.ACCESS_FLAGS_USER:
                icon = FontAwesome.USER;
                break;
            case CmsAccessControlEntry.ACCESS_FLAGS_GROUP:
                icon = FontAwesome.GROUP;
                break;
            case CmsAccessControlEntry.ACCESS_FLAGS_ALLOTHERS:
                icon = FontAwesome.GLOBE;
                break;
            case CmsAccessControlEntry.ACCESS_FLAGS_ROLE:
                icon = FontAwesome.GRADUATION_CAP;
                break;
            case CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE_ALL:
                icon = FontAwesome.EXCLAMATION_CIRCLE;
                isOverwriteAll = true;
                break;
            default:
                icon = FontAwesome.QUESTION_CIRCLE;
        }

        m_label.setContentMode(ContentMode.HTML);
        String ouName = null;
        if (ou != null) {
            try {
                ouName = OpenCms.getOrgUnitManager().readOrganizationalUnit(cms, ou).getDisplayName(
                    UI.getCurrent().getLocale());
            } catch (CmsException e) {
                LOG.debug("Error reading OU name.", e);
            }
        }
        m_label.setValue(
            icon.getHtml()
                + " <b>"
                + displayName
                + "</b> "
                + entry.getPermissions().getPermissionString()
                + (ouName != null ? ("<br />" + ouName) : ""));
        m_label.setWidthUndefined();
        m_details.setIcon(FontAwesome.PLUS_SQUARE_O);
        m_details.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                toggleDetails();
            }
        });

        m_setButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                setPermissions();
            }
        });

        m_deleteButton.addClickListener(new ClickListener() {

            private static final long serialVersionUID = 1L;

            public void buttonClick(ClickEvent event) {

                deletePermissionSet();
            }
        });

        if (inheritedFrom != null) {
            m_inheritedFrom.setValue(inheritedFrom);
        } else {
            m_inheritedFrom.setVisible(false);
        }

        if (isOverwriteAll) {
            setDetailButtonVisible(false);
            if (m_editable) {
                addComponent(m_deleteButton, 2);
                m_deleteButton.addStyleName("o-permissions_delete");
            }
        } else {
            // get all permissions of the current entry
            CmsPermissionSet permissions = entry.getPermissions();
            IndexedContainer container = getPermissionContainer(permissions);
            m_permissions.setContainerDataSource(container);
            m_permissions.setColumnReorderingAllowed(false);
            m_permissions.setColumnHeader(PROPERTY_LABEL, CmsVaadinUtils.getMessageText(Messages.GUI_PERMISSION_0));
            m_permissions.setColumnHeader(
                PROPERTY_ALLOWED,
                CmsVaadinUtils.getMessageText(Messages.GUI_PERMISSION_ALLOWED_0));
            m_permissions.setColumnHeader(
                PROPERTY_DISPLAY_ALLOWED,
                CmsVaadinUtils.getMessageText(Messages.GUI_PERMISSION_ALLOWED_0));
            m_permissions.setColumnHeader(
                PROPERTY_DENIED,
                CmsVaadinUtils.getMessageText(Messages.GUI_PERMISSION_DENIED_0));
            m_permissions.setColumnHeader(
                PROPERTY_DISPLAY_DENIED,
                CmsVaadinUtils.getMessageText(Messages.GUI_PERMISSION_DENIED_0));

            m_permissions.setPageLength(5);
            m_permissions.setSortEnabled(false);
            if (m_editable) {
                toggleDetails();
                setDetailButtonVisible(false);
                m_permissions.setVisibleColumns(PROPERTY_LABEL, PROPERTY_ALLOWED, PROPERTY_DENIED);
                m_permissions.setTableFieldFactory(FIELD_FACTORY);
                m_permissions.setEditable(m_editable);
                m_responsibleCheckbox.setValue(isResponsible(entry.getFlags()));
                m_overwriteCheckbox.setValue(isOverWritingInherited(entry.getFlags()));
                m_inheritCheckbox.setVisible(isFolder);
                m_inheritCheckbox.setValue(Boolean.valueOf(m_entry.isInheriting()));

                m_buttonBar.setVisible(true);
            } else {
                m_permissions.setVisibleColumns(PROPERTY_LABEL, PROPERTY_DISPLAY_ALLOWED, PROPERTY_DISPLAY_DENIED);
            }
        }
    }

    /**
     * Hides the denied permissions column.<p>
     */
    public void hideDeniedColumn() {

        if (m_editable) {
            m_permissions.setVisibleColumns(PROPERTY_LABEL, PROPERTY_ALLOWED);
        } else {
            m_permissions.setVisibleColumns(PROPERTY_LABEL, PROPERTY_DISPLAY_ALLOWED);
        }
    }

    /**
     * Determines the type of the current access control entry.<p>
     *
     * @param flags the value of the current flags
     * @param all to include all types, or just user and groups
     *
     * @return String representation of the ace type
     */
    protected String getEntryType(int flags, boolean all) {

        for (int i = 0; i < getTypes(all).length; i++) {
            if ((flags & getTypesInt()[i]) > 0) {
                return getTypes(all)[i];
            }
        }
        return UNKNOWN_TYPE;
    }

    /**
     * Returns a String array with the possible entry types.<p>
     *
     * @param all to include all types, or just user, groups and roles
     *
     * @return the possible types
     */
    protected String[] getTypes(boolean all) {

        if (!all) {
            String[] array = new String[3];
            return Arrays.asList(CmsPermissionDialog.PRINCIPAL_TYPES).subList(0, 3).toArray(array);
        }
        return CmsPermissionDialog.PRINCIPAL_TYPES;
    }

    /**
     * Returns an int array with possible entry types.<p>
     *
     * @return the possible types as int array
     */
    protected int[] getTypesInt() {

        return CmsPermissionDialog.PRINCIPAL_TYPES_INT;
    }

    /**
     * Checks if a certain permission of a permission set is allowed.<p>
     *
     * @param p the current CmsPermissionSet
     * @param value the int value of the permission to check
     * @return true if the permission is allowed, otherwise false
     */
    protected Boolean isAllowed(CmsPermissionSet p, int value) {

        if ((p.getAllowedPermissions() & value) > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * Checks if a certain permission of a permission set is denied.<p>
     *
     * @param p the current CmsPermissionSet
     * @param value the int value of the permission to check
     * @return true if the permission is denied, otherwise false
     */
    protected Boolean isDenied(CmsPermissionSet p, int value) {

        if ((p.getDeniedPermissions() & value) > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * Check if the current permissions are overwriting the inherited ones.<p>
     *
     * @param flags value of all flags of the current entry
     * @return true if permissions are overwriting the inherited ones, otherwise false
     */
    protected Boolean isOverWritingInherited(int flags) {

        if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE) > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * Check if the user is a responsible for the resource.<p>
     *
     * @param flags value of all flags of the current entry
     * @return true if user is responsible for the resource, otherwise false
     */
    protected Boolean isResponsible(int flags) {

        if ((flags & CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE) > 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * Deletes the current permission set.<p>
     */
    void deletePermissionSet() {

        m_changeHandler.deletePermissionSet(m_principalType, m_principalName);
    }

    /**
     * Generates the permissions data container.<p>
     *
     * @param permissions the permission set
     *
     * @return the container
     */
    IndexedContainer getPermissionContainer(CmsPermissionSet permissions) {

        IndexedContainer result = new IndexedContainer();
        result.addContainerProperty(PROPERTY_LABEL, String.class, "");
        result.addContainerProperty(PROPERTY_VALUE, Integer.class, null);
        result.addContainerProperty(PROPERTY_ALLOWED, Boolean.class, Boolean.FALSE);
        result.addContainerProperty(PROPERTY_DISPLAY_ALLOWED, Label.class, null);
        result.addContainerProperty(PROPERTY_DENIED, Boolean.class, Boolean.FALSE);
        result.addContainerProperty(PROPERTY_DISPLAY_DENIED, Label.class, null);
        for (String key : CmsPermissionSet.getPermissionKeys()) {
            int flag = CmsPermissionSet.getPermissionValue(key);
            Item entry = result.addItem(key);
            entry.getItemProperty(PROPERTY_LABEL).setValue(CmsVaadinUtils.getMessageText(key));
            entry.getItemProperty(PROPERTY_ALLOWED).setValue(isAllowed(permissions, flag));
            entry.getItemProperty(PROPERTY_DISPLAY_ALLOWED).setValue(getCheckBoxLabel(isAllowed(permissions, flag)));
            entry.getItemProperty(PROPERTY_DENIED).setValue(isDenied(permissions, flag));
            entry.getItemProperty(PROPERTY_DISPLAY_DENIED).setValue(getCheckBoxLabel(isDenied(permissions, flag)));
            entry.getItemProperty(PROPERTY_VALUE).setValue(Integer.valueOf(flag));
        }

        return result;
    }

    /**
     * Sets the current permissions.<p>
     */
    void setPermissions() {

        IndexedContainer container = (IndexedContainer)m_permissions.getContainerDataSource();
        int allowed = 0;
        int denied = 0;
        for (Object itemId : container.getItemIds()) {
            Item item = container.getItem(itemId);
            Integer value = (Integer)item.getItemProperty(PROPERTY_VALUE).getValue();
            if (((Boolean)item.getItemProperty(PROPERTY_ALLOWED).getValue()).booleanValue()) {
                allowed |= value.intValue();
            }
            if (((Boolean)item.getItemProperty(PROPERTY_DENIED).getValue()).booleanValue()) {
                denied |= value.intValue();
            }
        }
        int flags = m_entry.getFlags();

        // modify the ace flags to determine inheritance of the current ace
        if (m_inheritCheckbox.getValue().booleanValue()) {
            flags |= CmsAccessControlEntry.ACCESS_FLAGS_INHERIT;
        } else {
            flags &= ~CmsAccessControlEntry.ACCESS_FLAGS_INHERIT;
        }

        // modify the ace flags to determine overwriting of inherited ace
        if (m_overwriteCheckbox.getValue().booleanValue()) {
            flags |= CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE;
        } else {
            flags &= ~CmsAccessControlEntry.ACCESS_FLAGS_OVERWRITE;
        }

        if (m_responsibleCheckbox.getValue().booleanValue()) {
            flags |= CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE;
        } else {
            flags &= ~CmsAccessControlEntry.ACCESS_FLAGS_RESPONSIBLE;
        }

        m_changeHandler.setPermissions(m_principalType, m_principalName, allowed, denied, flags);
    }

    /**
     * Toggles the details display.<p>
     */
    void toggleDetails() {

        if (m_permissions.isVisible()) {
            m_permissions.setVisible(false);
            m_details.setIcon(FontAwesome.PLUS_SQUARE_O);
        } else {
            m_permissions.setVisible(true);
            m_details.setIcon(FontAwesome.MINUS_SQUARE_O);
        }
        m_changeHandler.onViewChange();
    }

    /**
     * Generates a check box label.<p>
     *
     * @param value the value to display
     *
     * @return the label
     */
    private Label getCheckBoxLabel(Boolean value) {

        String content;
        if (value.booleanValue()) {
            content = "<input type='checkbox' disabled='true' checked='true' />";
        } else {
            content = "<input type='checkbox' disabled='true' />";
        }

        return new Label(content, ContentMode.HTML);
    }

    /**
     * Shows / hides the details button.<p>
     *
     * @param visible true if the details button should be shown
     */
    private void setDetailButtonVisible(boolean visible) {

        m_detailButtonContainer.setVisible(visible);
        if (visible) {
            removeStyleName("o-permission-no-details");
        } else {
            addStyleName("o-permission-no-details");
        }

    }
}
