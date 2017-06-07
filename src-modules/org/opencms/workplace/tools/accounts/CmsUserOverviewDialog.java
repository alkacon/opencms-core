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
import org.opencms.main.CmsSessionManager;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.list.CmsListDateMacroFormatter;
import org.opencms.workplace.list.I_CmsListFormatter;

import java.util.ArrayList;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit new or existing user in the administration view.<p>
 *
 * @since 6.0.0
 */
public class CmsUserOverviewDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "user.ov";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Request parameter name for the user id. */
    public static final String PARAM_USERID = "userid";

    /** Formatter for the last login property. */
    private static final I_CmsListFormatter LAST_LOGIN_FORMATTER = CmsListDateMacroFormatter.getDefaultDateFormatter();

    /** The user object that is edited on this dialog. */
    protected CmsUser m_user;

    /** Stores the value of the request parameter for the user id. */
    private String m_paramUserid;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsUserOverviewDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsUserOverviewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited user to the db.<p>
     */
    @Override
    public void actionCommit() {

        // no saving is done
        setCommitErrors(new ArrayList<Throwable>());
    }

    /**
     * Calls the switch user method of the SessionManager.<p>
     *
     * @return the direct edit patch
     *
     * @throws CmsException if something goes wrong
     */
    public String actionSwitchUser() throws CmsException {

        try {
            CmsSessionManager sessionManager = OpenCms.getSessionManager();
            CmsUser user = getCms().readUser(new CmsUUID(getJsp().getRequest().getParameter("userid")));
            return sessionManager.switchUser(getCms(), getJsp().getRequest(), user);
        } catch (CmsException e) {
            String toolPath = getCurrentToolPath().substring(0, getCurrentToolPath().lastIndexOf("/"));
            getToolManager().setCurrentToolPath(this, toolPath);
            throw e;
        }
    }

    /**
     * Returns the description of the parent ou.<p>
     *
     * @return the description of the parent ou
     */
    public String getAssignedOu() {

        try {
            return OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), m_user.getOuFqn()).getDisplayName(
                getLocale());
        } catch (CmsException e) {
            return null;
        }
    }

    /**
     * Returns the creation date.<p>
     *
     * Auxiliary Property for better representation.<p>
     *
     * @return the creation date
     */
    public String getCreated() {

        return LAST_LOGIN_FORMATTER.format(new Date(m_user.getDateCreated()), getLocale());
    }

    /**
     * Returns the localized description of the user.<p>
     *
     * @return the localized description of the user
     */
    public String getDescription() {

        return m_user.getDescription(getLocale());
    }

    /**
     * Returns the last login.<p>
     *
     * Auxiliary Property for better representation.<p>
     *
     * @return the last login
     */
    public String getLastlogin() {

        return LAST_LOGIN_FORMATTER.format(new Date(m_user.getLastlogin()), getLocale());
    }

    /**
     * Returns the simple name of the user object.<p>
     *
     * @return the simple name of the user object
     */
    public String getName() {

        return m_user.getSimpleName();
    }

    /**
     * Returns the user id parameter value.<p>
     *
     * @return the user id parameter value
     */
    public String getParamUserid() {

        return m_paramUserid;
    }

    /**
     * Returns the selfManagement.<p>
     *
     * @return the selfManagement
     */
    public boolean isSelfManagement() {

        return !m_user.isManaged();
    }

    /**
     * Setter for widget definition.<p>
     *
     * @param assignedOu the ou description
     */
    public void setAssignedOu(String assignedOu) {

        assignedOu.length(); // prevent warning
    }

    /**
     * Sets the creation date.<p>
     *
     * Auxiliary Property for better representation.<p>
     *
     * @param created the creation date to set
     */
    public void setCreated(String created) {

        if (created == null) {
            // just to avoid warnings
        }
    }

    /**
     * Sets the description of the user.<p>
     *
     * @param description the user description
     */
    public void setDescription(String description) {

        m_user.setDescription(description);
    }

    /**
     * Sets the last login.<p>
     *
     * Auxiliary Property for better representation.<p>
     *
     * @param lastlogin the last login to set
     */
    public void setLastlogin(String lastlogin) {

        if (lastlogin == null) {
            // just to avoid warnings
        }
    }

    /**
     * Sets the name of the user object.<p>
     *
     * @param name the name of the user object
     */
    public void setName(String name) {

        name.length();
    }

    /**
     * Sets the user id parameter value.<p>
     *
     * @param userId the user id parameter value
     */
    public void setParamUserid(String userId) {

        m_paramUserid = userId;
    }

    /**
     * Sets the selfManagement.<p>
     *
     * @param selfManagement the selfManagement to set
     */
    public void setSelfManagement(boolean selfManagement) {

        m_user.setManaged(!selfManagement);
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

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        int n = (!isOverview() ? 3 : 5);
        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_USER_EDITOR_LABEL_IDENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, n));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            if (!isOverview()) {
                result.append(createWidgetTableEnd());
                return result.toString();
            }
            result.append(dialogBlockStart(key(Messages.GUI_USER_EDITOR_LABEL_ADDRESS_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(6, 10));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_USER_EDITOR_LABEL_AUTHENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(11, 14));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

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
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        // initialize the user object to use for the dialog
        initUserObject();

        setKeyPrefix(KEY_PREFIX);

        // widgets to display
        if (isOverview()) {
            addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "description", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "lastname", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "firstname", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "email", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "assignedOu", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "institution", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "address", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "zipcode", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "city", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "country", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "enabled", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "selfManagement", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "lastlogin", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "created", PAGES[0], new CmsDisplayWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "lastname", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_user, "firstname", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "assignedOu", PAGES[0], new CmsDisplayWidget()));
        }
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Initializes the user object.<p>
     */
    protected void initUserObject() {

        try {
            // edit an existing user, get the user object from db
            m_user = getCms().readUser(new CmsUUID(getParamUserid()));
        } catch (CmsException e) {
            // should never happen
        }
    }

    /**
     * Overridden to set a custom online help path. <p>
     *
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceMembers(org.opencms.jsp.CmsJspActionElement)
     */
    @Override
    protected void initWorkplaceMembers(CmsJspActionElement jsp) {

        super.initWorkplaceMembers(jsp);
        setOnlineHelpUriCustom("/accounts/users/overview/");
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        getCms().readUser(new CmsUUID(getParamUserid())).getName();
    }

    /**
     * Checks if the User overview has to be displayed.<p>
     *
     * @return <code>true</code> if the user overview has to be displayed
     */
    private boolean isOverview() {

        return getCurrentToolPath().endsWith("/users/edit");
    }
}