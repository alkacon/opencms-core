/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/broadcast/CmsSendEmailDialog.java,v $
 * Date   : $Date: 2005/06/15 13:50:49 $
 * Version: $Revision: 1.1 $
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

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit a message to broadcast in the administration view.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.9.1
 */
public class CmsSendEmailDialog extends CmsWidgetDialog {

    /** localized messages Keys prefix. */
    public static final String C_KEY_PREFIX = "message";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** Request parameter name for the project id. */
    public static final String PARAM_SESSIONIDS = "sessionids";

    /** Stores the value of the request parameter for the project id. */
    private String m_paramSessionids;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSendEmailDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSendEmailDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited project to the db.<p>
     */
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            // TODO
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
            //result.append(dialogBlockStart(key(Messages.GUI_PROJECT_EDITOR_LABEL_IDENTIFICATION_BLOCK_0)));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(0, 4));
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

        // TODO
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
     * Initializes the project object to work with depending on the dialog state and request parameters.<p>
     * 
     * Two initializations of the project object on first dialog call are possible:
     * <ul>
     * <li>edit an existing project</li>
     * <li>create a new project</li>
     * </ul>
     */
    protected void initProjectObject() {

        //Object o = null;

        try {
            // TODO
        } catch (Exception e) {
            // TODO
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the project (may be changed because of the widget values)
        //setDialogObject(m_project);
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