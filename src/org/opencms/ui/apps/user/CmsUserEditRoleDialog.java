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

package org.opencms.ui.apps.user;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the dialog to edit and view roles of user.<p>
 */
public class CmsUserEditRoleDialog extends A_CmsEditUserGroupRoleDialog {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserEditRoleDialog.class);

    /**vaadin serial id.*/
    private static final long serialVersionUID = -5734296145021453705L;

    /**vaadin component.*/
    Button m_close;

    /**vaadin component. */
    HorizontalLayout m_hlayout;

    /**vaadin component. */
    VerticalLayout m_leftTableHolder;

    /**vaadin component. */
    VerticalLayout m_rightTableHolder;

    /**vaadin component. */
    VerticalLayout m_vlayout;

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param userId id of user
     * @param window window
     * @param app
     */
    public CmsUserEditRoleDialog(CmsObject cms, CmsUUID userId, final Window window, CmsAccountsApp app) {

        super(cms, userId, window, app);
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#addItem(java.util.Set)
     */
    @Override
    public void addItem(Set<String> data) {

        Iterator<String> it = data.iterator();
        while (it.hasNext()) {
            String roleName = it.next();
            try {
                OpenCms.getRoleManager().addUserToRole(m_cms, CmsRole.valueOfRoleName(roleName), m_principal.getName());
            } catch (CmsException e) {
                LOG.error("Unable to add user to role", e);
            }
        }

    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getAddActionCaption()
     */
    @Override
    public String getAddActionCaption() {

        return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_ADD_ROLE_0);
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getAddCaptionText()
     */
    @Override
    public String getAddCaptionText() {

        return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_CHOOSE_ROLE_0);
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getAvailableItemsIndexedContainer(java.lang.String, java.lang.String)
     */
    @Override
    public IndexedContainer getAvailableItemsIndexedContainer(String caption, String icon) {

        try {
            List<CmsRole> roles = OpenCms.getRoleManager().getRoles(m_cms, m_principal.getOuFqn(), false);
            List<CmsRole> invisibleRoles = new ArrayList<CmsRole>();
            for (CmsRole role : roles) {
                if (!OpenCms.getRoleManager().hasRole(
                    m_cms,
                    m_cms.getRequestContext().getCurrentUser().getName(),
                    role)) {
                    invisibleRoles.add(role);
                }
            }
            roles.removeAll(invisibleRoles);
            CmsRole.applySystemRoleOrder(roles);
            List<CmsRole> userRoles = OpenCms.getRoleManager().getRolesOfUser(
                m_cms,
                m_principal.getName(),
                m_principal.getOuFqn(),
                false,
                false,
                false);
            IndexedContainer container = new IndexedContainer();
            container.addContainerProperty(caption, String.class, "");
            container.addContainerProperty(icon, CmsCssIcon.class, new CmsCssIcon(OpenCmsTheme.ICON_ROLE));
            for (CmsRole role : roles) {

                if (!userRoles.contains(role)) {
                    Item item = container.addItem(role);
                    item.getItemProperty(caption).setValue(role.getDisplayName(m_cms, A_CmsUI.get().getLocale()));
                }
            }
            return container;
        } catch (CmsException e) {
            LOG.error("unable to get roles", e);
            return null;
        }
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getCloseButton()
     */
    @Override
    public Button getCloseButton() {

        return m_close;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getCurrentTableCaption()
     */
    @Override
    public String getCurrentTableCaption() {

        return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_CURRENTLY_SET_ROLES_0);
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getDescriptionForItemId(java.lang.Object)
     */
    @Override
    public String getDescriptionForItemId(Object itemId) {

        CmsRole role = (CmsRole)itemId;
        return role.getDescription(m_cms.getRequestContext().getLocale());
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getEmptyMessage()
     */
    @Override
    public String getEmptyMessage() {

        return Messages.GUI_USERMANAGEMENT_EDIT_EMPTY_ROLES_0;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getFurtherColumnId()
     */
    @Override
    public String getFurtherColumnId() {

        return null;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getHLayout()
     */
    @Override
    public HorizontalLayout getHLayout() {

        return m_hlayout;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getItemName()
     */
    @Override
    public String getItemName() {

        return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_NAME_0);
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getItemsOfUserIndexedContainer(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public IndexedContainer getItemsOfUserIndexedContainer(String propName, String propIcon, String propStatus) {

        try {
            List<CmsRole> userRoles = OpenCms.getRoleManager().getRolesOfUser(
                m_cms,
                m_principal.getName(),
                m_principal.getOuFqn(),
                false,
                false,
                false);
            CmsRole.applySystemRoleOrder(userRoles);
            List<CmsRole> directRoles = OpenCms.getRoleManager().getRolesOfUser(
                m_cms,
                m_principal.getName(),
                "",
                true,
                true,
                true);
            //            CmsRole.applySystemRoleOrder(directRoles);
            for (CmsRole directRole : directRoles) {
                if (!userRoles.contains(directRole)) {
                    //Role is from other OU directly set to user.
                    userRoles.add(directRole);
                }
            }

            IndexedContainer container = new IndexedContainer();
            container.addContainerProperty(propName, String.class, "");
            container.addContainerProperty(propIcon, CmsCssIcon.class, new CmsCssIcon(OpenCmsTheme.ICON_ROLE));
            container.addContainerProperty(propStatus, Boolean.class, Boolean.valueOf(true));
            for (CmsRole role : userRoles) {
                Item item = container.addItem(role);
                item.getItemProperty(propName).setValue(role.getDisplayName(m_cms, A_CmsUI.get().getLocale()));
                item.getItemProperty(propStatus).setValue(Boolean.valueOf(directRoles.contains(role)));
            }
            return container;
        } catch (CmsException e) {
            LOG.error("unable to get roles", e);
            return null;
        }
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getLeftTableLayout()
     */
    @Override
    public VerticalLayout getLeftTableLayout() {

        return m_leftTableHolder;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getParentLayout()
     */
    @Override
    public VerticalLayout getParentLayout() {

        return m_vlayout;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getRightTableLayout()
     */
    @Override
    public VerticalLayout getRightTableLayout() {

        return m_rightTableHolder;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getStringSetValue(java.util.Set)
     */
    @Override
    public Set<String> getStringSetValue(Set<Object> value) {

        Set<String> res = new HashSet<String>();
        for (Object o : value) {
            CmsRole role = (CmsRole)o;
            res.add(role.getFqn());
        }
        return res;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getWindowCaptionMessageKey()
     */
    @Override
    public String getWindowCaptionMessageKey() {

        return Messages.GUI_USERMANAGEMENT_EDIT_USERROLES_1;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#removeItem(java.util.Set)
     */
    @Override
    public void removeItem(Set<String> items) {

        try {
            Iterator<String> iterator = items.iterator();
            while (iterator.hasNext()) {
                CmsRole role = CmsRole.valueOfRoleName(iterator.next());
                OpenCms.getRoleManager().removeUserFromRole(m_cms, role, m_principal.getName());
            }
        } catch (CmsException e) {
            LOG.error("Unable to remove user from role", e);
        }
    }
}
