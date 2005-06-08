/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/Attic/CmsGroupDisabledStateAction.java,v $
 * Date   : $Date: 2005/06/08 16:44:19 $
 * Version: $Revision: 1.2 $
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
import org.opencms.main.CmsRuntimeException;
import org.opencms.workplace.list.A_CmsListTwoStatesAction;
import org.opencms.workplace.list.I_CmsListDirectAction;

import java.util.List;

/**
 * Adds/Removes a user to/from a role.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.2 $
 * @since 5.7.3
 */
public class CmsGroupDisabledStateAction extends A_CmsListTwoStatesAction {

    /** The user name. */
    private String m_userName;
    
    /**
     * Default Constructor.<p>
     * 
     * @param listId the id of the associated list
     * @param id unique id
     * @param cms the cms context
     */
    protected CmsGroupDisabledStateAction(String listId, String id, CmsObject cms) {

        super(listId, id, cms);
    }
    
    
    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#getId()
     */
    public String getId() {

        // needed to avoid calling the <code>{@link selectAction()}</code> method while setting the right username
        return getFirstAction().getId();
    }
    
    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#isEnabled()
     */
    public boolean isEnabled() {

        return false;
    }
    
    /**
     * @see org.opencms.workplace.list.A_CmsListToggleAction#selectAction()
     */
    public I_CmsListDirectAction selectAction() {

        try {
            String groupName = (String)getItem().get(A_CmsUserGroupsList.LIST_COLUMN_NAME);
            List dGroups = getCms().getDirectGroupsOfUser(m_userName);
            CmsGroup group = getCms().readGroup(groupName);
            if (dGroups.contains(group)) {
                return getFirstAction();
            }
        } catch (Exception e) {
            throw new CmsRuntimeException(Messages.get().container(
                Messages.ERR_USERGROUPS_DIRECT_GROUP_1, m_userName), e);
        }
        return getSecondAction();
    }
    
    /**
     * Sets the userName.<p>
     *
     * @param userName the userName to set
     */
    public void setUserName(String userName) {

        m_userName = userName;
    }
}