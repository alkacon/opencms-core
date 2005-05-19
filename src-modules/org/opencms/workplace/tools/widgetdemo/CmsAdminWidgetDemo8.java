/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/widgetdemo/Attic/CmsAdminWidgetDemo8.java,v $
 * Date   : $Date: 2005/05/19 12:55:53 $
 * Version: $Revision: 1.6 $
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
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.CmsCheckboxWidget;
import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.CmsVfsFileWidget;
import org.opencms.workplace.CmsWidgetDialog;
import org.opencms.workplace.CmsWidgetDialogParameter;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * A basic example and proof-of-concept on how to use OpenCms widgets within a custom build form
 * without XML contents.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.6 $
 * @since 5.9.1
 */
public class CmsAdminWidgetDemo8 extends CmsWidgetDialog {

    /** Value for the action: display dialog page 1. */
    public static final int ACTION_DISPLAY_PAGE_1 = 301;

    /** Value for the action: display dialog page 2. */
    public static final int ACTION_DISPLAY_PAGE_2 = 302;

    /** The dialog type. */
    public static final String DIALOG_TYPE = "widgetdemo8";

    /** Defines which pages are valid for this dialog. */
    public static final String[] PAGES = {"page1", "page2"};
    
    /** The allowed pages for this dialog in a List. */
    public static final List PAGE_LIST = Arrays.asList(PAGES);

    /** The job info object that is edited on this dialog. */
    protected CmsScheduledJobInfo m_jobInfo;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAdminWidgetDemo8(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAdminWidgetDemo8(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
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
     * Creats the HTML for the buttons on the dialog.<p>
     * 
     * @return the HTML for the buttons on the dialog.<p>
     */
    public String dialogButtonsCustom() {

        if (PAGES[1].equals(getParamPage())) {
            // this is the second dialog page
            return dialogButtons(new int[] {BUTTON_OK, BUTTON_BACK, BUTTON_CANCEL}, new String[3]);
        } else {
            return dialogButtons(new int[] {BUTTON_CONTINUE, BUTTON_CANCEL}, new String[2]);
        }
    }

    /**
     * @see org.opencms.workplace.CmsDialog#getCancelAction()
     */
    public String getCancelAction() {

        // set the default action
        setParamPage(PAGES[0]);

        return DIALOG_SET;
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

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(getParamPage()) || !PAGE_LIST.contains(getParamPage())) {
            // ensure a valid page is set
            setParamPage(PAGES[0]);
        }

        // fill the widget map
        defineWidgets();
        fillWidgetValues(request);

        // set the action for the JSP switch 
        if (DIALOG_SAVE.equals(getParamAction())) {
            // ok button pressed
            setAction(ACTION_SAVE);
            List errors = commitWidgetValues();
            if (errors.size() > 0) {
                Iterator i = errors.iterator();
                while (i.hasNext()) {
                    Exception e = (Exception)i.next();
                    System.err.println(e.getMessage());
                    if (e.getCause() != null) {
                        System.err.println("Cause: " + e.getCause().getMessage());
                    }
                }
                setAction(ACTION_DEFAULT);
            }
        } else if (DIALOG_OK.equals(getParamAction())) {
            // ok button pressed
            setAction(ACTION_CANCEL);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            // cancel button pressed
            setAction(ACTION_CANCEL);
        } else if (EDITOR_ACTION_ELEMENT_ADD.equals(getParamAction())) {
            setAction(ACTION_ELEMENT_ADD);
            actionToggleElement();
            setAction(ACTION_DEFAULT);
        } else if (EDITOR_ACTION_ELEMENT_REMOVE.equals(getParamAction())) {
            setAction(ACTION_ELEMENT_REMOVE);
            actionToggleElement();
            setAction(ACTION_DEFAULT);
        } else if (DIALOG_BACK.equals(getParamAction())) {

            setAction(ACTION_DEFAULT);
            List errors = commitWidgetValues(PAGES[1]);
            if (errors.size() > 0) {
                return;
            }

            setParamPage(PAGES[0]);

        } else if (DIALOG_CONTINUE.equals(getParamAction())) {

            setAction(ACTION_DEFAULT);
            List errors = commitWidgetValues(PAGES[0]);
            if (errors.size() > 0) {
                return;
            }

            setParamPage(PAGES[1]);

        } else {
            // set the default action               
            setAction(ACTION_DEFAULT);
        }

        // save the current state of the job (may be changed because of the widget values)
        setDialogObject(m_jobInfo);
    }

}
