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

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit new or existing system user in the administration view.<p>
 *
 * @since 6.0.0
 */
public class CmsEditUserDialog extends A_CmsEditUserDialog {

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsEditUserDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsEditUserDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#createUser(java.lang.String, java.lang.String, java.lang.String, java.util.Map)
     */
    @Override
    protected CmsUser createUser(String name, String pwd, String desc, Map<String, Object> info) throws CmsException {

        return getCms().createUser(name, pwd, desc, info);
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#getListClass()
     */
    @Override
    protected String getListClass() {

        return CmsUsersList.class.getName();
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#getListRootPath()
     */
    @Override
    protected String getListRootPath() {

        return "/accounts/orgunit/users";
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#isEditable(org.opencms.file.CmsUser)
     */
    @Override
    protected boolean isEditable(CmsUser user) {

        return true;
    }

    /**
     * @see org.opencms.workplace.tools.accounts.A_CmsEditUserDialog#writeUser(org.opencms.file.CmsUser)
     */
    @Override
    protected void writeUser(CmsUser user) throws CmsException {

        getCms().writeUser(user);
    }
}