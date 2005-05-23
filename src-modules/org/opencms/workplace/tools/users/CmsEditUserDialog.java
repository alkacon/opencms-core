/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/users/Attic/CmsEditUserDialog.java,v $
 * Date   : $Date: 2005/05/23 16:06:05 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.users;

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsContextInfo;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit new and existing user in the administration view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.9.1
 */
public class CmsEditUserDialog extends CmsWidgetDialog {

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1", "page2"};

    /** Request parameter name for the user id. */
    public static final String PARAM_USERID = "userid";

    /** Request parameter name for the user name. */
    public static final String PARAM_USERNAME = "username";

    /** The user object that is edited on this dialog. */
    private CmsUser m_user;
    
    /** Stores the value of the request parameter for the user id. */
    private String m_paramUserid;

    /** Stores the value of the request parameter for the user name. */
    private String m_paramUsername;

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
     * Commits the edited user to the db.<p>
     */
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            // write the edited user
            getCms().writeUser(m_user);
            // clear the HTML list to be up to date after editing
            getSettings().setHtmlList(null);
        } catch (Throwable t) {
            errors.add(t);
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
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
     * Returns the user name parameter value.<p>
     * 
     * @return the user name parameter value
     */
    public String getParamUsername() {

        return m_paramUsername;
    }

    /**
     * Sets the user id parameter value.<p>
     * 
     * @param userid the user id parameter value
     */
    public void setParamUserid(String userid) {

        m_paramUserid = userid;
    }

    /**
     * Sets the user name parameter value.<p>
     * 
     * @param username the user name parameter value
     */
    public void setParamUsername(String username) {

        m_paramUsername = username;
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

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(createDialogRowsHtml(0, 4));
            //result.append(dialogBlockStart(key(Messages.GUI_EDITOR_LABEL_CONTEXTINFO_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(5, 11));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        } else if (dialog.equals(PAGES[1])) {
            // create the widget for the second dialog page
            //result.append(dialogBlockStart(key(Messages.GUI_EDITOR_LABEL_PARAMETERS_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(12, 12));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        // close widget table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        // initialize the user object to use for the dialog
        initUserObject();

        // required to read the default values for the optional context parameters for the widgets
        CmsContextInfo dC = new CmsContextInfo();

        // widgets to display on the first dialog page
        addWidget(new CmsWidgetDialogParameter(m_user, "jobName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_user, "className", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_user, "cronExpression", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_user, "reuseInstance", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_user, "active", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_user, "contextInfo.userName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_user, "contextInfo.projectName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(
            m_user,
            "contextInfo.siteRoot",
            dC.getSiteRoot(),
            PAGES[0],
            new CmsVfsFileWidget(),
            0,
            1));
        addWidget(new CmsWidgetDialogParameter(
            m_user,
            "contextInfo.requestedUri",
            dC.getRequestedUri(),
            PAGES[0],
            new CmsVfsFileWidget(),
            0,
            1));
        addWidget(new CmsWidgetDialogParameter(
            m_user,
            "contextInfo.localeName",
            dC.getLocaleName(),
            PAGES[0],
            new CmsInputWidget(),
            0,
            1));
        addWidget(new CmsWidgetDialogParameter(
            m_user,
            "contextInfo.encoding",
            dC.getEncoding(),
            PAGES[0],
            new CmsInputWidget(),
            0,
            1));
        addWidget(new CmsWidgetDialogParameter(
            m_user,
            "contextInfo.remoteAddr",
            dC.getRemoteAddr(),
            PAGES[0],
            new CmsInputWidget(),
            0,
            1));

        // widget to display on the second dialog page
        addWidget(new CmsWidgetDialogParameter(m_user, "parameters", PAGES[1], new CmsInputWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
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
     * Initializes the user object to work with depending on the dialog state and request parameters.<p>
     * 
     * Three initializations of the user object on first dialog call are possible:
     * <ul>
     * <li>edit an existing user</li>
     * <li>create a new user</li>
     * </ul>
     */
    protected void initUserObject() {

        Object o = null;

        if (CmsStringUtil.isEmpty(getParamAction())
            || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
            // this is the initial dialog call
            if (CmsStringUtil.isNotEmpty(getParamUserid())) {
                try {
                    // edit an existing user, get the user object from db
                    o = getCms().readUser(getParamUserid());
                } catch (CmsException e) {
                    // noop
                }
            }
        } else {
            // this is not the initial call, get the user object from session
            o = getDialogObject();
        }

        if (!(o instanceof CmsUser)) {
            // create a new user object
            //m_user = new CmsUser();
        } else {
            // reuse user object stored in session
            m_user = (CmsUser)o;
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the user (may be changed because of the widget values)
        setDialogObject(m_user);
    }
}