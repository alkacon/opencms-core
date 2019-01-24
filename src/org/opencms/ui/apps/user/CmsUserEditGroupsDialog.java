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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Window;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Class for the group edit dialog for users.<p>
 */
public class CmsUserEditGroupsDialog extends A_CmsEditUserGroupRoleDialog {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 7548706839526481814L;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserEditGroupsDialog.class);

    /**ID.*/
    public static final String ID_OU = "ou";

    /**vaadin component.*/
    Button m_close;

    /**vaadin component.*/
    VerticalLayout m_leftTableHolder;

    /**vaadin component.*/
    VerticalLayout m_rightTableHolder;

    /**vaadin component.*/
    VerticalLayout m_vlayout;

    /**vaadin component.*/
    HorizontalLayout m_hlayout;

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param userId id of user
     * @param window window
     * @param app the app instance
     */
    public CmsUserEditGroupsDialog(CmsObject cms, CmsUUID userId, final Window window, CmsAccountsApp app) {

        super(cms, userId, window, app);
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#addItem(java.util.Set)
     */
    @Override
    public void addItem(Set<String> data) {

        if (m_app.checkAddGroup((CmsUser)m_principal, data)) {
            Iterator<String> it = data.iterator();
            while (it.hasNext()) {
                String groupName = it.next();
                try {
                    m_cms.addUserToGroup(m_principal.getName(), groupName);
                } catch (CmsException e) {
                    LOG.error("Unable to add user to group", e);
                }
            }
        }
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getAddActionCaption()
     */
    @Override
    public String getAddActionCaption() {

        return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_ADD_GROUP_0);
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getAddCaptionText()
     */
    @Override
    public String getAddCaptionText() {

        return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_CHOOSE_GROUP_0);
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getAvailableItemsIndexedContainer(java.lang.String, java.lang.String)
     */
    @Override
    public IndexedContainer getAvailableItemsIndexedContainer(String caption, String propIcon) {

        try {
            return m_app.getAvailableGroupsContainerWithout(
                m_cms,
                m_principal.getOuFqn(),
                caption,
                propIcon,
                ID_OU,
                m_cms.getGroupsOfUser(m_principal.getName(), true),
                m_app::getGroupIcon);
        } catch (CmsException e) {
            LOG.error("Can't read groups of user", e);
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

        return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_CURRENTLY_SET_GROUPS_0);
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getDescriptionForItemId(java.lang.Object)
     */
    @Override
    public String getDescriptionForItemId(Object itemId) {

        return null;

    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getEmptyMessage()
     */
    @Override
    public String getEmptyMessage() {

        return Messages.GUI_USERMANAGEMENT_EDIT_EMPTY_GROUPS_0;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getFurtherColumnId()
     */
    @Override
    public String getFurtherColumnId() {

        return ID_OU;
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

        CmsUser user = (CmsUser)m_principal;
        IndexedContainer container = m_app.getUserGroupsEditorContainer(user, propName, propIcon, propStatus);
        return container;
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
            res.add(((CmsGroup)o).getName());
        }
        return res;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#getWindowCaptionMessageKey()
     */
    @Override
    public String getWindowCaptionMessageKey() {

        return Messages.GUI_USERMANAGEMENT_EDIT_USERGROUP_1;
    }

    /**
     * @see org.opencms.ui.apps.user.A_CmsEditUserGroupRoleDialog#removeItem(java.util.Set)
     */
    @Override
    public void removeItem(Set<String> items) {

        if (m_app.checkRemoveGroups((CmsUser)m_principal, items)) {
            Iterator<String> iterator = items.iterator();
            while (iterator.hasNext()) {
                try {

                    m_cms.removeUserFromGroup(m_principal.getName(), iterator.next());

                } catch (CmsIllegalArgumentException | CmsException e) {
                    //happens if admin group was deleted( = user is deleted at the same time)
                }
            }
        }
    }

}
