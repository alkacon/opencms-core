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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * The role overview widget dialog.<p>
 *
 * @since 6.5.6
 */
public class CmsRoleOverviewDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "role";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The role object that is viewed on this dialog. */
    protected CmsRole m_role;

    /** Stores the value of the request parameter for the organizational unit. */
    private String m_paramOufqn;

    /** Stores the value of the request parameter for the role name. */
    private String m_paramRole;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsRoleOverviewDialog(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRoleOverviewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    @Override
    public void actionCommit() {

        // noop
    }

    /**
     * Returns a String inculding all parent roles of the role object.<p>
     *
     * @return a String inculding all parent roles of the role object
     */
    public String getDependency() {

        String dependency = "";
        CmsRole role = m_role;
        while (role.getParentRole() != null) {
            dependency = dependency + role.getParentRole().getName(getCms().getRequestContext().getLocale());
            role = role.getParentRole();
            if (role.getParentRole() != null) {
                dependency = dependency + ", ";
            }
        }
        return dependency;
    }

    /**
     * Returns the localized description of the role object.<p>
     *
     * @return the localized description of the role object
     */
    public String getDescription() {

        return m_role.getDescription(getCms().getRequestContext().getLocale());
    }

    /**
     * Returns the localized name of the role object.<p>
     *
     * @return the localized name of the role object
     */
    public String getName() {

        return m_role.getName(getCms().getRequestContext().getLocale());
    }

    /**
     * Returns the organizational unit parameter value.<p>
     *
     * @return the organizational unit parameter value
     */
    public String getParamOufqn() {

        return m_paramOufqn;
    }

    /**
     * Returns the role name parameter value.<p>
     *
     * @return the role name parameter value
     */
    public String getParamRole() {

        return m_paramRole;
    }

    /**
     * This method is needed only for displaying reasons.<p>
     *
     * @param dependency nothing to do with this parameter
     */
    public void setDependency(String dependency) {

        // nothing will be done here, just to avoid warnings
        dependency.length();
    }

    /**
     * This method is needed only for displaying reasons.<p>
     *
     * @param description nothing to do with this parameter
     */
    public void setDescription(String description) {

        // nothing will be done here, just to avoid warnings
        description.length();
    }

    /**
     * This method is needed only for displaying reasons.<p>
     *
     * @param name nothing to do with this parameter
     */
    public void setName(String name) {

        // nothing will be done here, just to avoid warnings
        name.length();
    }

    /**
     * Sets the user organizational unit value.<p>
     *
     * @param ouFqn the organizational unit parameter value
     */
    public void setParamOufqn(String ouFqn) {

        if (ouFqn == null) {
            ouFqn = "";
        }
        m_paramOufqn = ouFqn;
    }

    /**
     * Sets the role name value.<p>
     *
     * @param role the role name parameter value
     */
    public void setParamRole(String role) {

        m_paramRole = role;
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>
     *
     * This overwrites the method from the super class to create a layout variation for the widgets.<p>
     *
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    @Override
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create widget table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_ROLE_OVERVIEW_LABEL_IDENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 2));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        // close widget table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defaultActionHtmlEnd()
     */
    @Override
    protected String defaultActionHtmlEnd() {

        return "";
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#defineWidgets()
     */
    @Override
    protected void defineWidgets() {

        // initialize the user object to use for the dialog
        initRoleObject();

        setKeyPrefix(KEY_PREFIX);

        // widgets to display
        addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "dependency", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(this, "description", PAGES[0], new CmsDisplayWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the group object.<p>
     */
    protected void initRoleObject() {

        try {
            m_role = CmsRole.valueOf(getCms().readGroup(m_paramRole));
        } catch (CmsException e) {
            // noop
        }
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        OpenCms.getRoleManager().checkRole(getCms(), CmsRole.ACCOUNT_MANAGER.forOrgUnit(getParamOufqn()));
        CmsRole.valueOf(getCms().readGroup(getParamRole())).getGroupName();
    }
}
