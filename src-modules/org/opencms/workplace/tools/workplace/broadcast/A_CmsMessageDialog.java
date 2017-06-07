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

package org.opencms.workplace.tools.workplace.broadcast;

import org.opencms.file.CmsUser;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.CmsHtmlList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * Base dialog to edit a message info object.<p>
 *
 * @since 6.0.0
 */
public abstract class A_CmsMessageDialog extends CmsWidgetDialog {

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Request parameter name for the list of session ids. */
    public static final String PARAM_SESSIONIDS = "sessionids";

    /** The static log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsMessageDialog.class);

    /** Message info object. */
    protected CmsMessageInfo m_msgInfo;

    /** Stores the value of the request parameter for the list of session ids. */
    private String m_paramSessionids;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public A_CmsMessageDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Returns the list of session ids parameter value.<p>
     *
     * @return the list of session ids parameter value
     */
    public String getParamSessionids() {

        return m_paramSessionids;
    }

    /**
     * Sets the list of session ids parameter value.<p>
     *
     * @param sessionIds the list of session ids parameter value
     */
    public void setParamSessionids(String sessionIds) {

        m_paramSessionids = sessionIds;
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    @Override
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Returns a semicolon separated list of user names.<p>
     *
     * @return a semicolon separated list of user names
     */
    protected String getToNames() {

        List<String> users = new ArrayList<String>();
        Iterator<String> itIds = idsList().iterator();
        while (itIds.hasNext()) {
            String id = itIds.next();
            CmsSessionInfo session = OpenCms.getSessionManager().getSessionInfo(id);
            if (session != null) {
                try {
                    String userName = getCms().readUser(session.getUserId()).getFullName();
                    if (!users.contains(userName)) {
                        users.add(userName);
                    }
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        StringBuffer result = new StringBuffer(256);
        Iterator<String> itUsers = users.iterator();
        while (itUsers.hasNext()) {
            result.append(itUsers.next());
            if (itUsers.hasNext()) {
                result.append("; ");
            }
        }
        return result.toString();
    }

    /**
     * Returns the list of session ids.<p>
     *
     * @return the list of session ids
     */
    protected List<String> idsList() {

        if (!isForAll()) {
            return CmsStringUtil.splitAsList(getParamSessionids(), CmsHtmlList.ITEM_SEPARATOR);
        }
        List<CmsUser> manageableUsers = new ArrayList<CmsUser>();
        try {
            manageableUsers = OpenCms.getRoleManager().getManageableUsers(getCms(), "", true);
        } catch (CmsException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        List<String> ids = new ArrayList<String>();
        Iterator<CmsSessionInfo> itSessions = OpenCms.getSessionManager().getSessionInfos().iterator();
        while (itSessions.hasNext()) {
            CmsSessionInfo sessionInfo = itSessions.next();
            CmsUser user;
            try {
                user = getCms().readUser(sessionInfo.getUserId());
            } catch (CmsException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn(e.getLocalizedMessage(), e);
                }
                continue;
            }
            if (!manageableUsers.contains(user)) {
                continue;
            }
            ids.add(sessionInfo.getSessionId().toString());
        }
        return ids;
    }

    /**
     * Initializes the message info object to work with depending on the dialog state and request parameters.<p>
     */
    protected void initMessageObject() {

        Object o = null;

        try {
            // this is not the initial call, get the message info object from session
            o = getDialogObject();
            m_msgInfo = (CmsMessageInfo)o;
            // test
            m_msgInfo.getTo();
        } catch (Exception e) {
            // create a new message info object
            m_msgInfo = new CmsMessageInfo();
        }
        m_msgInfo.setFrom(getCms().getRequestContext().getCurrentUser().getFullName());
        m_msgInfo.setTo(getToNames());
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    @Override
    protected void initMessages() {

        // add specific dialog resource bundle
        addMessages(Messages.get().getBundleName());
        addMessages(org.opencms.workplace.tools.workplace.Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the message (may be changed because of the widget values)
        setDialogObject(m_msgInfo);
    }

    /**
     * Checks if the edited message has to be sent to all sessions.<p>
     *
     * @return <code>true</code> if the edited message has to be sent to all sessions
     */
    protected boolean isForAll() {

        return CmsStringUtil.isEmptyOrWhitespaceOnly(getParamSessionids());
    }
}