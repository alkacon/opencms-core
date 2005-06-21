/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/Attic/CmsGroupDisabledStateAction.java,v $
 * Date   : $Date: 2005/06/21 15:54:15 $
 * Version: $Revision: 1.4 $
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
import org.opencms.workplace.list.A_CmsListTwoStatesAction;
import org.opencms.workplace.list.I_CmsListDirectAction;

import java.util.List;

/**
 * Show diferent states depending on user direct/indirect group assignment.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.4 $
 * @since 5.7.3
 */
public class CmsGroupDisabledStateAction extends A_CmsListTwoStatesAction {

    /** The user name. */
    private String m_userName;
    
    /**
     * Default Constructor.<p>
     *
     * @param id the unique id
     * @param cms the cms context
     * @param userName the name of a valid opencms user
     */
    protected CmsGroupDisabledStateAction(String id, CmsObject cms, String userName) {

        super(id, cms);
        setUserName(userName);
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
            // ignore
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