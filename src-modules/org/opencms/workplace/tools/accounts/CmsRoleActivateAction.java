/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/Attic/CmsRoleActivateAction.java,v $
 * Date   : $Date: 2005/06/07 16:25:40 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.list.A_CmsListTwoStatesAction;
import org.opencms.workplace.list.I_CmsListDirectAction;

import java.util.List;

/**
 * Adds/Removes a user to/from a role.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public class CmsRoleActivateAction extends A_CmsListTwoStatesAction {

    /** The user name. */
    private String m_userName;
    
    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param id unique id
     * @param cms the cms context
     * @param userName the user name
     */
    protected CmsRoleActivateAction(String listId, String id, CmsObject cms, String userName) {

        super(listId, id, cms);
        m_userName = userName;
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#getName()
     */
    public CmsMessageContainer getName() {

        if (!isEnabled()) {
            return Messages.get().container(Messages.GUI_USERROLES_LIST_ACTION_STATE_DISABLED_NAME_0);
        }
        return super.getName();
    }
    
    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#selectAction()
     */
    public I_CmsListDirectAction selectAction() {

        if (getItem() != null) {
            String roleName = (String)getItem().get(A_CmsUserGroupsList.LIST_COLUMN_NAME);
            try {
                if (!getCms().userInGroup(m_userName, roleName)) {
                    return getFirstAction();
                }
            } catch (CmsException e) {
                // noop
            }
        }
        return getSecondAction();
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isEnabled()
     */
    public boolean isEnabled() {

        if (getItem() != null) {
            String roleName = (String)getItem().get(A_CmsUserGroupsList.LIST_COLUMN_NAME);
            try {
                List dRoles = getCms().getDirectGroupsOfUser(m_userName);
                CmsGroup role = getCms().readGroup(roleName);
                return dRoles.contains(role);
            } catch (Exception e) {
                throw new CmsRuntimeException(Messages.get().container(
                    Messages.ERR_USERROLES_DIRECT_ROLE_1, m_userName), e);
            }
        }
        return super.isEnabled();
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#getHelpText()
     */
    public CmsMessageContainer getHelpText() {

        if (isEnabled()) {
            return super.getHelpText();
        }
        return Messages.get().container(Messages.GUI_USERROLES_LIST_ACTION_STATE_DISABLED_HELP_0);
    }

    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#getIconPath()
     */
    public String getIconPath() {

        if (isEnabled()) {
            return super.getIconPath();
        }
        return CmsUsersList.PATH_BUTTONS + "indirect_disabled.gif";
    }

}