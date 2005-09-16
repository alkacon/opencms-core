/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsGroupStateAction.java,v $
 * Date   : $Date: 2005/09/16 13:11:11 $
 * Version: $Revision: 1.9.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
import org.opencms.workplace.list.CmsListDefaultAction;

import java.util.List;

/**
 * Show diferent states depending on user direct/indirect group assignment.<p>
 * 
 * @author Michael Moossen 
 *  
 * @version $Revision: 1.9.2.1 $ 
 * 
 * @since 6.0.0 
 */
public class CmsGroupStateAction extends CmsListDefaultAction {

    /** Cms context. */
    private final CmsObject m_cms;

    /** Direct group flag. */
    private final boolean m_direct;

    /** Current user name. */
    private String m_userName;

    /**
     * Default constructor.<p>
     * 
     * @param id the id of the action
     * @param cms the cms context
     * @param direct the direct group flag
     */
    public CmsGroupStateAction(String id, CmsObject cms, boolean direct) {

        super(id);
        m_cms = cms;
        m_direct = direct;
    }

    /**
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
     */
    public boolean isVisible() {

        try {
            String groupName = (String)getItem().get(A_CmsUserGroupsList.LIST_COLUMN_NAME);
            List dGroups = getCms().getDirectGroupsOfUser(getUserName());
            CmsGroup group = getCms().readGroup(groupName);
            if (isDirect()) {
                return dGroups.contains(group);
            } else {
                return !dGroups.contains(group);
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the user Name.<p>
     *
     * @return the user Name
     */
    public String getUserName() {

        return m_userName;
    }

    /**
     * Sets the user Name.<p>
     *
     * @param userName the user Name to set
     */
    public void setUserName(String userName) {

        m_userName = userName;
    }

    /**
     * Returns the cms context.<p>
     *
     * @return the cms context
     */
    public CmsObject getCms() {

        return m_cms;
    }

    /**
     * Returns the direct group flag.<p>
     *
     * @return the direct group flag
     */
    public boolean isDirect() {

        return m_direct;
    }
}