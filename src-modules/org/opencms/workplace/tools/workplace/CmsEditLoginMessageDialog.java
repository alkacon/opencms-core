/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/CmsEditLoginMessageDialog.java,v $
 * Date   : $Date: 2005/06/22 10:38:25 $
 * Version: $Revision: 1.5 $
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

package org.opencms.workplace.tools.workplace;

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.db.CmsLoginMessage;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.widgets.CmsCalendarWidget;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsTextareaWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Dialog to edit the login message of the OpenCms Workplace.<p>
 *
 * @author  Alexander Kandzior 
 * @version $Revision: 1.5 $
 * 
 * @since 6.0
 */
public class CmsEditLoginMessageDialog extends CmsWidgetDialog {

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1"};

    /** The login message that is edited with this dialog. */
    private CmsLoginMessage m_loginMessage;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsEditLoginMessageDialog(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsEditLoginMessageDialog(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Commits the edited login message to the login manager.<p>
     */
    public void actionCommit() {

        List errors = new ArrayList();

        try {
            // set the edited message
            OpenCms.getLoginManager().setLoginMessage(getCms(), m_loginMessage);
            // update the system configuration
            OpenCms.writeConfiguration(CmsSystemConfiguration.class);
            // clear the message object
            setDialogObject(null);
        } catch (Throwable t) {
            errors.add(t);
        }

        // set the list of errors to display when saving failed
        setCommitErrors(errors);
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

        // create the widgets for the first dialog page
        result.append(dialogBlockStart(key(Messages.GUI_EDITOR_LABEL_ACTIVATE_BLOCK_0)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(0, 0));
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());
        result.append(dialogBlockStart(key(Messages.GUI_EDITOR_LABEL_CONFIGURATION_BLOCK_0)));
        result.append(createWidgetTableStart());
        result.append(createDialogRowsHtml(1, 4));
        result.append(createWidgetTableEnd());
        result.append(dialogBlockEnd());

        // close widget table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        // initialize the object to use for the dialog
        initLoginMessageObject();

        // required to read the default values for the optional context parameters for the widgets
        CmsLoginMessage def = new CmsLoginMessage();

        addWidget(new CmsWidgetDialogParameter(m_loginMessage, "enabled", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_loginMessage, "message", PAGES[0], new CmsTextareaWidget()));
        addWidget(new CmsWidgetDialogParameter(m_loginMessage, "loginForbidden", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(
            m_loginMessage,
            "timeStart",
            String.valueOf(def.getTimeStart()),
            PAGES[0],
            new CmsCalendarWidget(),
            0,
            1));
        addWidget(new CmsWidgetDialogParameter(
            m_loginMessage,
            "timeEnd",
            String.valueOf(def.getTimeEnd()),
            PAGES[0],
            new CmsCalendarWidget(),
            0,
            1));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * Initializes the login message object for this dialog.<p>
     */
    protected void initLoginMessageObject() {

        Object o = getDialogObject();

        if ((o == null) || !(o instanceof CmsLoginMessage)) {
            o = OpenCms.getLoginManager().getLoginMessage();
        }

        if (o != null) {
            m_loginMessage = (CmsLoginMessage)((CmsLoginMessage)o).clone();
        } else {
            m_loginMessage = new CmsLoginMessage();
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
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // initialize parameters and dialog actions in super implementation
        super.initWorkplaceRequestValues(settings, request);

        // save the current login message (may be changed because of the widget values)
        setDialogObject(m_loginMessage);
    }

}