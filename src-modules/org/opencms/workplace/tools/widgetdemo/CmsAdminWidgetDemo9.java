/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/widgetdemo/Attic/CmsAdminWidgetDemo9.java,v $
 * Date   : $Date: 2005/06/22 10:38:29 $
 * Version: $Revision: 1.8 $
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

package org.opencms.workplace.tools.widgetdemo;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsContextInfo;
import org.opencms.scheduler.CmsScheduledJobInfo;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * A basic example and proof-of-concept on how to use OpenCms widgets within a custom build form
 * without XML contents.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.8 $
 * @since 5.9.1
 */
public class CmsAdminWidgetDemo9 extends CmsWidgetDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "widgetdemo8";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1", "page2"};

    /** The OpenCms context info object used for the job info. */
    CmsContextInfo m_contextInfo;

    /** The job info object that is edited on this dialog. */
    CmsScheduledJobInfo m_jobInfo;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAdminWidgetDemo9(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAdminWidgetDemo9(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }
    
    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    public void actionCommit() {

        // not implemented for this demo

    }

    /**
     * Builds the HTML for the dialog form.<p>
     * 
     * @return the HTML for the dialog form
     */
    public String buildDialogForm() {

        StringBuffer result = new StringBuffer(1024);

        try {

            // create the dialog HTML
            result.append(createDialogHtml(getParamPage()));

        } catch (Throwable t) {
            // TODO: Error handling
        }
        return result.toString();
    }

    /**
     * @see org.opencms.workplace.CmsDialog#getCancelAction()
     */
    public String getCancelAction() {

        // set the default action
        setParamPage((String)getPages().get(0));

        return DIALOG_SET;
    }

    /**
     * Creates the dialog HTML for all defined widgets of the named dialog (page).<p>  
     * 
     * @param dialog the dialog (page) to get the HTML for
     * @return the dialog HTML for all defined widgets of the named dialog (page)
     */
    protected String createDialogHtml(String dialog) {

        StringBuffer result = new StringBuffer(1024);

        // create table
        result.append(createWidgetTableStart());

        // show error header once if there were validation errors
        result.append(createWidgetErrorHeader());

        if (dialog.equals(PAGES[0])) {
            result.append(createDialogRowsHtml(0, 2));
            result.append(dialogBlockStart("User context"));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(3, 9));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
            result.append(createDialogRowsHtml(10, 11));
        } else if (dialog.equals(PAGES[1])) {
            result.append(dialogBlockStart("Parameters"));
            result.append(createWidgetTableStart());
            result.append(createDialogRowsHtml(12, 12));
            result.append(createWidgetTableEnd());
            result.append(dialogBlockEnd());
        }

        // close table
        result.append(createWidgetTableEnd());

        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        Object o = getDialogObject();

        // required to read the default values for the optional context parameters
        CmsContextInfo dC = new CmsContextInfo();

        if (!(o instanceof CmsScheduledJobInfo)) {
            // create a new job info
            m_jobInfo = new CmsScheduledJobInfo();
            m_jobInfo.setContextInfo(dC);

            // add some parameters to check issues with pre-filled maps
            m_jobInfo.getParameters().put("key1", "value1");
            m_jobInfo.getParameters().put("key2", "value2");

        } else {
            // reuse job info object stored in session
            m_jobInfo = (CmsScheduledJobInfo)o;
        }

        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "jobName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "className", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "cronExpression", PAGES[0], new CmsInputWidget()));

        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.userName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.projectName", PAGES[0], new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.siteRoot", dC.getSiteRoot(), PAGES[0], new CmsVfsFileWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.requestedUri", dC.getRequestedUri(), PAGES[0], new CmsVfsFileWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.localeName", dC.getLocaleName(), PAGES[0], new CmsInputWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.encoding", dC.getEncoding(), PAGES[0], new CmsInputWidget(), 0, 1));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "contextInfo.remoteAddr", dC.getRemoteAddr(), PAGES[0], new CmsInputWidget(), 0, 1));

        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "reuseInstance", PAGES[0], new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "active", PAGES[0], new CmsCheckboxWidget()));

        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "parameters", PAGES[1], new CmsInputWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return PAGES;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        super.initWorkplaceRequestValues(settings, request);

        // save the current state of the job (may be changed because of the widget values)
        setDialogObject(m_jobInfo);
    }
}
