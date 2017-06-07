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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.workplace.list.A_CmsListDialog;
import org.opencms.workplace.list.CmsListDefaultAction;

import java.util.List;

/**
 * Show diferent states depending on user direct/indirect group assignment.<p>
 *
 * @since 6.0.0
 */
public class CmsGroupStateAction extends CmsListDefaultAction {

    /** The cms context. */
    private CmsObject m_cms;

    /** Direct group flag. */
    private final boolean m_direct;

    /** Current user name. */
    private String m_userName;

    /**
     * Default constructor.<p>
     *
     * @param id the id of the action
     * @param direct the direct group flag
     */
    public CmsGroupStateAction(String id, boolean direct) {

        super(id);
        m_direct = direct;
    }

    /**
     * Default constructor.<p>
     *
     * @param id the id of the action
     * @param cms the cms context
     * @param direct the direct group flag
     *
     * @Deprecated cms object no longer needed
     */
    public CmsGroupStateAction(String id, CmsObject cms, boolean direct) {

        super(id);
        m_cms = cms;
        m_direct = direct;
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
     * Returns the user Name.<p>
     *
     * @return the user Name
     */
    public String getUserName() {

        if (m_userName == null) {
            m_userName = ((A_CmsUserGroupsList)getWp()).getParamUsername();
        }
        return m_userName;
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
     * @see org.opencms.workplace.tools.A_CmsHtmlIconButton#isVisible()
     */
    @Override
    public boolean isVisible() {

        try {
            String groupName = (String)getItem().get(A_CmsUserGroupsList.LIST_COLUMN_NAME);
            List<CmsGroup> dGroups = getCms().getGroupsOfUser(getUserName(), true);
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
     * Sets the user Name.<p>
     *
     * @param userName the user Name to set
     */
    public void setUserName(String userName) {

        m_userName = userName;
    }

    /**
     * @see org.opencms.workplace.list.I_CmsListAction#setWp(org.opencms.workplace.list.A_CmsListDialog)
     */
    @Override
    public void setWp(A_CmsListDialog wp) {

        super.setWp(wp);
        m_cms = wp.getCms();
        m_userName = null;
    }
}