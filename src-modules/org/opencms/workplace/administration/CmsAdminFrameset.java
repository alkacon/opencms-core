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

package org.opencms.workplace.administration;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.tools.CmsToolDialog;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper class to create the administration frameset.<p>
 *
 * It allows to specify if you want or not an left side menu.<p>
 *
 * The following files use this class:<br>
 * <ul>
 * <li>/views/admin/external-fs.jsp</li>
 * <li>/views/admin/admin-fs.jsp</li>
 * </ul>
 * <p>
 *
 * @since 6.0.0
 */
public class CmsAdminFrameset extends CmsToolDialog {

    /** Request parameter name for the "with menu" flag. */
    public static final String PARAM_MENU = "menu";

    /** Request parameter value. */
    private String m_paramMenu;

    /**
     * Public constructor.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsAdminFrameset(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Returns the menu parameter value.<p>
     *
     * @return the menu parameter value
     */
    public String getParamMenu() {

        return m_paramMenu;
    }

    /**
     * Sets the menu parameter value.<p>
     *
     * @param paramMenu the menu parameter value to set
     */
    public void setParamMenu(String paramMenu) {

        m_paramMenu = paramMenu;
    }

    /**
     * Tests if the current dialog should be displayed with or without menu.<p>
     *
     * The default is with menu, use <code>menu=no</code> for avoiding it.<p>
     *
     * @return <code>true</code> if the dialog should be displayed with menu
     */
    public boolean withMenu() {

        return (getParamMenu() == null) || !getParamMenu().equals("no");
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
    }

}