/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsDialog;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Simple "dialog" class to unlock a user.<p>
 */
public class CmsUnlockUser extends CmsDialog {

    /** The user id of the user to unlock. */
    protected String m_paramUserId;

    /**
     * Creates a new dialog instance.<p>
     * 
     * @param context the page context 
     * @param req the current request 
     * @param res the current response 
     */
    public CmsUnlockUser(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);

    }

    /**
     * Unlocks the user.<p>
     * 
     * @throws Exception if something goes wrong 
     */
    public void actionUnlockUser() throws Exception {

        CmsUUID userId = new CmsUUID(m_paramUserId);
        CmsObject cms = getCms();
        CmsUser user = cms.readUser(userId);
        OpenCms.getLoginManager().unlockUser(getCms(), user);
        actionCloseDialog();
    }

    /**
     * Sets the user id parameter value.<p>
     * 
     * @param userId the user id parameter value
     */
    public void setParamUserid(String userId) {

        m_paramUserId = userId;
    }

}
