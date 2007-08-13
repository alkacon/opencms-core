/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/A_CmsUserDataImexportDialog.java,v $
 * Date   : $Date: 2007/08/13 16:29:45 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2007 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.widgets.CmsSelectWidgetOption;
import org.opencms.workplace.CmsWidgetDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Abstract dialog class to import and export user data.<p>
 * 
 * @author Raphael Schnuck 
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 6.7.1
 */
public abstract class A_CmsUserDataImexportDialog extends CmsWidgetDialog {

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** List of groups. */
    private List m_groups;

    /** Stores the value of the request parameter for the organizational unit fqn. */
    private String m_paramOufqn;

    /** List of roles. */
    private List m_roles;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public A_CmsUserDataImexportDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public A_CmsUserDataImexportDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    public abstract void actionCommit() throws IOException, ServletException;

    /**
     * Returns the list of groups.<p>
     * 
     * @return the list of groups
     */
    public List getGroups() {

        return m_groups;
    }

    /**
     * Returns the organizational unit fqn parameter value.<p>
     * 
     * @return the organizational unit fqn parameter value
     */
    public String getParamOufqn() {

        return m_paramOufqn;
    }

    /**
     * Returns the list of roles to export.<p>
     *  
     * @return the list of roles to export
     */
    public List getRoles() {

        return m_roles;
    }

    /**
     * Sets the groups list.<p>
     * 
     * @param groups the groups list
     */
    public void setGroups(List groups) {

        m_groups = groups;
    }

    /**
     * Sets the organizational unit fqn parameter value.<p>
     * 
     * @param ouFqn the organizational unit fqn parameter value
     */
    public void setParamOufqn(String ouFqn) {

        if (ouFqn == null) {
            ouFqn = "";
        }
        m_paramOufqn = ouFqn;
    }

    /**
     * Sets the roles list.<p>
     * 
     * @param roles the roles list
     */
    public void setRoles(List roles) {

        m_roles = roles;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    protected abstract void defineWidgets();

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Returns the role names to show in the select box.<p>
     * 
     * @return the role names to show in the select box
     */
    protected List getSelectRoles() {

        List retVal = new ArrayList();

        try {

            List roles = OpenCms.getRoleManager().getRoles(getCms(), getParamOufqn(), false);
            Iterator itRoles = roles.iterator();
            while (itRoles.hasNext()) {
                CmsRole role = (CmsRole)itRoles.next();
                retVal.add(new CmsSelectWidgetOption(role.getGroupName(), false, role.getName(getLocale())));
            }
        } catch (CmsException e) {
            // noop
        }
        return retVal;
    }
}
