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
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * The group overview and group info widget dialog.<p>
 *
 * @since 6.0.0
 */
public class CmsGroupOverviewDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String KEY_PREFIX = "group.ov";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Request parameter name for the user id. */
    public static final String PARAM_GROUPID = "groupid";

    /** The user object that is edited on this dialog. */
    protected CmsGroup m_group;

    /** Stores the value of the request parameter for the group id. */
    private String m_paramGroupid;

    /** Auxiliary Property for better representation of the bean parentId property. */
    private String m_parentGroup;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsGroupOverviewDialog(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsGroupOverviewDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited group to the db.<p>
     */
    @Override
    public void actionCommit() {

        // no saving needed
        setCommitErrors(new ArrayList<Throwable>());
    }

    /**
     * Returns the description of the parent ou.<p>
     *
     * @return the description of the parent ou
     */
    public String getAssignedOu() {

        try {
            return OpenCms.getOrgUnitManager().readOrganizationalUnit(getCms(), m_group.getOuFqn()).getDisplayName(
                getLocale());
        } catch (CmsException e) {
            return null;
        }
    }

    /**
     * Returns the localized description of the group if the description is a message key.<p>
     *
     * @return the localized description of the group if the description is a message key
     */
    public String getDescription() {

        return m_group.getDescription(getLocale());
    }

    /**
     * Returns the simple name of the user object.<p>
     *
     * @return the simple name of the user object
     */
    public String getName() {

        return m_group.getSimpleName();
    }

    /**
     * Returns the groups nice name.<p>
     *
     * @return the groups nice name
     */
    public String getNiceName() {

        return OpenCms.getWorkplaceManager().translateGroupName(m_group.getName(), false);
    }

    /**
     * Returns the user id parameter value.<p>
     *
     * @return the user id parameter value
     */
    public String getParamGroupid() {

        return m_paramGroupid;
    }

    /**
     * Returns the parent Group name.<p>
     *
     * @return the parent Group name
     */
    public String getParentGroup() {

        return m_parentGroup;
    }

    /**
     * Setter for widget definition.<p>
     *
     * @param assignedOu the ou description
     */
    public void setAssignedOu(String assignedOu) {

        assignedOu.length();
    }

    /**
     * Sets the description of the group.<p>
     *
     * @param description the description of the group
     */
    public void setDescription(String description) {

        m_group.setDescription(description);
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
     * Dummy setter for the nice name property.<p>
     *
     * @param name a name string
     */
    public void setNiceName(String name) {

        // if this method doesn't exist, the constructor of CmsWidgetDialogParameter throws an exception; not sure why
    }

    /**
     * Sets the user id parameter value.<p>
     *
     * @param userId the user id parameter value
     */
    public void setParamGroupid(String userId) {

        m_paramGroupid = userId;
    }

    /**
     * Sets the parent Group name.<p>
     *
     * @param parentGroup the parent Group name to set
     */
    public void setParentGroup(String parentGroup) {

        if (CmsStringUtil.isEmpty(parentGroup) || parentGroup.equals("null") || parentGroup.equals("none")) {
            parentGroup = null;
        }
        if (parentGroup != null) {
            try {
                getCms().readGroup(parentGroup);
            } catch (CmsException e) {
                throw new CmsIllegalArgumentException(e.getMessageContainer());
            }
        }
        m_parentGroup = parentGroup;
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

        int n = (!isOverview() ? 2 : 4);
        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_GROUP_EDITOR_LABEL_IDENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, n));
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
     * Creates the list of widgets for this dialog.<p>
     */
    @Override
    protected void defineWidgets() {

        // initialize the user object to use for the dialog
        initGroupObject();

        setKeyPrefix(KEY_PREFIX);

        // widgets to display
        if (!isOverview()) {
            addWidget(new CmsWidgetDialogParameter(this, "name", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "description", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "assignedOu", PAGES[0], new CmsDisplayWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(this, "niceName", "name", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "description", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "assignedOu", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "parentGroup", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_group, "enabled", PAGES[0], new CmsDisplayWidget()));
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
     * Initializes the group object.<p>
     */
    protected void initGroupObject() {

        try {
            // edit an existing group, get the group object from db
            m_group = getCms().readGroup(new CmsUUID(getParamGroupid()));
            setParentGroup(getCms().readGroup(m_group.getParentId()).getName());
        } catch (CmsException e) {
            // should never happen
        }
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
     * Overridden to set the online help path for this dialog.<p>
     *
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceMembers(org.opencms.jsp.CmsJspActionElement)
     */
    @Override
    protected void initWorkplaceMembers(CmsJspActionElement jsp) {

        super.initWorkplaceMembers(jsp);
        setOnlineHelpUriCustom("/accounts/groups/overview/");
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
    @Override
    protected void validateParamaters() throws Exception {

        // test the needed parameters
        getCms().readGroup(new CmsUUID(getParamGroupid())).getName();
    }

    /**
     * Checks if the group overview has to be displayed.<p>
     *
     * @return <code>true</code> if the group overview has to be displayed
     */
    private boolean isOverview() {

        return getCurrentToolPath().endsWith("/groups/edit");
    }
}
