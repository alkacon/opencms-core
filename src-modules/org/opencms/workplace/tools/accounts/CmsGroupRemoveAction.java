/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsGroupRemoveAction.java,v $
 * Date   : $Date: 2005/10/10 16:11:03 $
 * Version: $Revision: 1.2 $
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
 * Shows direct/indirect assigned groups and enabled/disabled a remove action.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 6.0.0 
 */
public class CmsGroupRemoveAction extends CmsListDefaultAction {

    /** The cms context. */
    private final CmsObject m_cms;

    /** The direct group flag. */
    private final boolean m_direct;

    /** The user name. */
    private String m_userName;

    /**
     * Default Constructor.<p>
     * 
     * @param id the unique id
     * @param cms the cms context
     * @param direct the direct group flag
     */
    public CmsGroupRemoveAction(String id, CmsObject cms, boolean direct) {

        super(id);
        m_direct = direct;
        m_cms = cms;
    }

    /**
     * Returns the direct group flag.<p>
     *
     * @return the direct group flag
     */
    public boolean isDirect() {

        return m_direct;
    }

    /**
     * @see org.opencms.workplace.tools.I_CmsHtmlIconButton#isVisible()
     */
    public boolean isVisible() {

        if (getItem() != null) {
            String groupName = (String)getItem().get(A_CmsUserGroupsList.LIST_COLUMN_NAME);
            try {
                List dGroups = m_cms.getDirectGroupsOfUser(m_userName);
                CmsGroup group = m_cms.readGroup(groupName);
                if (isDirect()) {
                    return dGroups.contains(group);
                } else {
                    return !dGroups.contains(group);
                }
            } catch (Exception e) {
                return false;
            }
        }
        return super.isVisible();
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