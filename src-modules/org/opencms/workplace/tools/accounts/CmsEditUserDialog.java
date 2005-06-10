/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/accounts/CmsEditUserDialog.java,v $
 * Date   : $Date: 2005/06/10 15:58:06 $
 * Version: $Revision: 1.4 $
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

package org.opencms.workplace.tools.accounts;

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.security.CmsPasswordInfo;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsPasswordWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit new and existing user in the administration view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * 
 * @version $Revision: 1.4 $
 * @since 5.9.1
 */
public class CmsEditUserDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String C_KEY_PREFIX = "user";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Request parameter name for the user id. */
    public static final String PARAM_USERID = "userid";

    /** Request parameter name for the user name. */
    public static final String PARAM_USERNAME = "username";

    /** Session parameter name for the pwd info object. */
    private static final Object C_PWD_OBJECT = "PWD_INFO";

    /** Session parameter name for the user object. */
    private static final Object C_USER_OBJECT = "USER";

    /** The user object that is edited on this dialog. */
    protected CmsUser m_user;

    /** Stores the value of the request parameter for the user id. */
    private String m_paramUserid;

    /** Stores the value of the request parameter for the user name. */
    private String m_paramUsername;

    /** The password information object. */
    private CmsPasswordInfo m_pwdInfo;

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
            // in the case the confirmation widget has not be enabled 
            if (CmsStringUtil.isNotEmpty(m_pwdInfo.getNewPwd())) {
                m_pwdInfo.setConfirmation(m_pwdInfo.getConfirmation());
            }
            // if new create it first
            if (m_user.getId() == null) {
                CmsUser newUser = getCms().createUser(
                    m_user.getName(),
                    m_pwdInfo.getNewPwd(),
                    m_user.getDescription(),
                    m_user.getAdditionalInfo());
                newUser.setFirstname(m_user.getFirstname());
                newUser.setLastname(m_user.getLastname());
                newUser.setEmail(m_user.getEmail());
                newUser.setAddress(m_user.getAddress());
                m_user = newUser;
            } else if (CmsStringUtil.isNotEmpty(m_pwdInfo.getNewPwd())) {
                if (!m_pwdInfo.getNewPwd().equals(m_pwdInfo.getConfirmation())) {
                    m_pwdInfo.setConfirmation(null);
                }
                getCms().setPassword(m_user.getName(), m_pwdInfo.getNewPwd());
            }
            // write the edited user
            getCms().writeUser(m_user);
            // refresh the list
            Map objects = (Map)getSettings().getListObject();
            if (objects != null) {
                objects.remove(CmsUsersList.class.getName());
            }
        } catch (Throwable t) {
            errors.add(t);
        }

        if (errors.isEmpty() && isNewUser()) {
            if (getParamCloseLink() != null && getParamCloseLink().indexOf("path=/accounts/users") > -1) {
                // set closelink
                Map argMap = new HashMap();
                argMap.put("userid", m_user.getId());
                argMap.put("username", m_user.getName());
                setParamCloseLink(getToolManager().linkForPath(getJsp(), "/accounts/users/edit", argMap));
            }
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
     * @param userId the user id parameter value
     */
    public void setParamUserid(String userId) {

        m_paramUserid = userId;
    }

    /**
     * Sets the user name parameter value.<p>
     * 
     * @param userName the user name parameter value
     */
    public void setParamUsername(String userName) {

        m_paramUsername = userName;
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

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_USER_EDITOR_LABEL_IDENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 4));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_USER_EDITOR_LABEL_ADDRESS_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(5, 8));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_USER_EDITOR_LABEL_AUTHENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(9, 11));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        result.append(createWidgetTableEnd());
        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        // initialize the user object to use for the dialog
        initUserObject();

        setKeyPrefix(C_KEY_PREFIX);

        // widgets to display
        if (m_user.getId() == null) {
            addWidget(new CmsWidgetDialogParameter(m_user, "name", PAGES[0], new CmsInputWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(m_user, "name", PAGES[0], new CmsDisplayWidget()));
        }
        addWidget(new CmsWidgetDialogParameter(m_user, "description", "", PAGES[0], new CmsTextareaWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_user, "lastname", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_user, "firstname", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_user, "email", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_user, "address", "", PAGES[0], new CmsInputWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_user, "zipcode", "", PAGES[0], new CmsInputWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_user, "city", "", PAGES[0], new CmsInputWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_user, "country", "", PAGES[0], new CmsInputWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_user, "enabled", PAGES[0], new CmsCheckboxWidget()));
        if (m_user.getId() == null) {
            addWidget(new CmsWidgetDialogParameter(m_pwdInfo, "newPwd", PAGES[0], new CmsPasswordWidget()));
            addWidget(new CmsWidgetDialogParameter(m_pwdInfo, "confirmation", PAGES[0], new CmsPasswordWidget()));
        } else {
            addWidget(new CmsWidgetDialogParameter(m_pwdInfo, "newPwd", "", PAGES[0], new CmsPasswordWidget(), 0, 1));
            addWidget(new CmsWidgetDialogParameter(
                m_pwdInfo,
                "confirmation",
                "",
                PAGES[0],
                new CmsPasswordWidget(),
                0,
                1));
        }
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
     * Two initializations of the user object on first dialog call are possible:
     * <ul>
     * <li>edit an existing user</li>
     * <li>create a new user</li>
     * </ul>
     */
    protected void initUserObject() {

        Object o = null;

        try {
            if (CmsStringUtil.isEmpty(getParamAction()) || CmsDialog.DIALOG_INITIAL.equals(getParamAction())) {
                // edit an existing user, get the user object from db
                m_user = getCms().readUser(new CmsUUID(getParamUserid()));
                m_pwdInfo = new CmsPasswordInfo();
                return;
            } else {
                // this is not the initial call, get the user object from session            
                o = getDialogObject();
                Map dialogObject = (Map)o;
                m_user = (CmsUser)dialogObject.get(C_USER_OBJECT);
                m_pwdInfo = (CmsPasswordInfo)dialogObject.get(C_PWD_OBJECT);
                // test
                m_user.getId();
                return;
            }
        } catch (Exception e) {
            // noop
        }
        // create a new user object
        m_user = new CmsUser();
        m_pwdInfo = new CmsPasswordInfo();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        if (!isNewUser()) {
            // test the needed parameters
            try {
                getCms().readUser(getParamUsername());
                getCms().readUser(new CmsUUID(getParamUserid()));
            } catch (Exception e) {
                // redirect to parent if parameters not available
                setAction(ACTION_CANCEL);
                try {
                    actionCloseDialog();
                } catch (JspException e1) {
                    // noop
                }
                return;
            }
        }

        // save the current state of the user and pwd (may be changed because of the widget values)
        Map dialogObject = new HashMap();
        dialogObject.put(C_USER_OBJECT, m_user);
        dialogObject.put(C_PWD_OBJECT, m_pwdInfo);
        setDialogObject(dialogObject);
    }

    /**
     * Checks if the User overview has to be displayed.<p>
     * 
     * @return <code>true</code> if the user overview has to be displayed
     */
    private boolean isNewUser() {

        return getCurrentToolPath().equals("/accounts/users/new");
    }
}