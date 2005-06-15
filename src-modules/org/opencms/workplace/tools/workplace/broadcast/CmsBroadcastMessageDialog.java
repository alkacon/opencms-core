/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/broadcast/CmsBroadcastMessageDialog.java,v $
 * Date   : $Date: 2005/06/15 16:01:31 $
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

package org.opencms.workplace.tools.workplace.broadcast;

import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsDisplayWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.CmsHtmlList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit a message to broadcast in the administration view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * 
 * @version $Revision: 1.2 $
 * @since 5.9.1
 */
public class CmsBroadcastMessageDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String C_KEY_PREFIX = "message";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Request parameter name for the project id. */
    public static final String PARAM_SESSIONIDS = "sessionids";

    /** Stores the value of the request parameter for the project id. */
    private String m_paramSessionids;

    /** Message info object. */
    private CmsMessageInfo m_msgInfo;
    
    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsBroadcastMessageDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsBroadcastMessageDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited project to the db.<p>
     */
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            if (isForAll()) {
                OpenCms.getSessionManager().sendBroadcast(getCms(), m_msgInfo.getMsg());
            } else {
                List ids = CmsStringUtil.splitAsList(getParamSessionids(), CmsHtmlList.C_ITEM_SEPARATOR);
                Iterator itIds = ids.iterator();
                while (itIds.hasNext()) {
                    String id = itIds.next().toString();
                    OpenCms.getSessionManager().sendBroadcast(getCms(), m_msgInfo.getMsg(), id);
                }
            }
        } catch (Throwable t) {
            errors.add(t);
        }
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
     * @see org.opencms.workplace.CmsWidgetDialog#createDialogHtml(java.lang.String)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        result.append(createWidgetTableStart());
        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            // create the widgets for the first dialog page
            result.append(dialogBlockStart(key(Messages.GUI_MESSAGE_EDITOR_LABEL_HEADER_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 1));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(dialogBlockStart(key(Messages.GUI_MESSAGE_EDITOR_LABEL_CONTENT_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(2, 2));
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

        // initialize the project object to use for the dialog
        initProjectObject();

        setKeyPrefix(C_KEY_PREFIX);

        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "from", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "to", PAGES[0], new CmsDisplayWidget()));
        addWidget(new CmsWidgetDialogParameter(m_msgInfo, "msg", PAGES[0], new CmsTextareaWidget()));
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
        addMessages(org.opencms.workplace.tools.workplace.Messages.get().getBundleName());
        // add default resource bundles
        super.initMessages();
    }

    /**
     * Initializes the message info object to work with depending on the dialog state and request parameters.<p>
     * 
     * Two initializations of the message info object on first dialog call are possible:
     * <ul>
     * <li>edit an existing message info object</li>
     * <li>create a new message info object</li>
     * </ul>
     */
    protected void initProjectObject() {

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
            m_msgInfo.setFrom(getCms().getRequestContext().currentUser().getFullName());
            m_msgInfo.setTo(getToNames());
        }
    }

    /**
     * Returns a semicolon separated list of user names.<p>
     * 
     * @return a semicolon separated list of user names
     */
    private String getToNames() {

        List users = new ArrayList();
        List ids;
        if (!isForAll()) {
            ids = CmsStringUtil.splitAsList(getParamSessionids(), CmsHtmlList.C_ITEM_SEPARATOR);
        } else {
            ids = new ArrayList();
            Iterator itSessions = OpenCms.getSessionManager().getSessionInfos().iterator();
            while (itSessions.hasNext()) {
                ids.add(((CmsSessionInfo)itSessions.next()).getSessionId());
            }
        }
        Iterator itIds = ids.iterator();
        while (itIds.hasNext()) {
            String id = itIds.next().toString();
            CmsSessionInfo session = OpenCms.getSessionManager().getSessionInfo(id);
            if (session != null) {
                String userName = session.getUser().getFullName();
                if (!users.contains(userName)) {
                    users.add(userName);
                }
            }
        }
        StringBuffer result = new StringBuffer(256);
        Iterator itUsers = users.iterator();
        while (itUsers.hasNext()) {
            result.append(itUsers.next().toString());
            if (itUsers.hasNext()) {
                result.append("; ");
            }
        }
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
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
    private boolean isForAll() {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamSessionids());
    }
}