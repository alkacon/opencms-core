/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsGroupOverviewDialog.java,v $
 * Date   : $Date: 2005/10/10 16:11:03 $
 * Version: $Revision: 1.10 $
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
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
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
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.10 $ 
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
    public void actionCommit() {

        // no saving needed
        setCommitErrors(new ArrayList());
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

        if (parentGroup == null || parentGroup.equals("") || parentGroup.equals("null") || parentGroup.equals("none")) {
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
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create widget table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        int n = (!isOverview() ? 1 : 3);
        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_GROUP_EDITOR_LABEL_IDENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, n));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            if (!isOverview()) {
                result.append(createWidgetTableEnd());
                return result.toString();
            }
            result.append(dialogBlockStart(key(Messages.GUI_GROUP_EDITOR_LABEL_FLAGS_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(4, 6));
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
    protected String defaultActionHtmlEnd() {

        return "";
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        // initialize the user object to use for the dialog
        initGroupObject();

        setKeyPrefix(KEY_PREFIX);

        // widgets to display
        if (!isOverview()) {
            addWidget(new CmsWidgetDialogParameter(m_group, "name", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_group, "description", PAGES[0], new CmsDisplayWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(m_group, "name", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_group, "description", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(this, "parentGroup", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_group, "enabled", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_group, "role", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_group, "projectManager", PAGES[0], new CmsDisplayWidget()));
            addWidget(new CmsWidgetDialogParameter(m_group, "projectCoWorker", PAGES[0], new CmsDisplayWidget()));
        }
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the group object.<p>
     */
    protected void initGroupObject() {

        try {
            // edit an existing user, get the user object from db
            m_group = getCms().readGroup(new CmsUUID(getParamGroupid()));
        } catch (CmsException e) {
            // should never happen
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
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
    protected void initWorkplaceMembers(CmsJspActionElement jsp) {

        super.initWorkplaceMembers(jsp);
        setOnlineHelpUriCustom("/accounts/groups/overview/");
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#validateParamaters()
     */
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
