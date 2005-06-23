/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/widgetdemo/Attic/CmsAdminWidgetDemo5.java,v $
 * Date   : $Date: 2005/06/23 09:05:01 $
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
 * 
 * @since 6.0.0 
 */
public class CmsAdminWidgetDemo5 extends CmsWidgetDialog {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "widgetdemo5";

    /** The OpenCms context info object used for the job info. */
    CmsContextInfo m_contextInfo;

    /** The job info object that is edited on this dialog. */
    CmsScheduledJobInfo m_jobInfo;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsAdminWidgetDemo5(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsAdminWidgetDemo5(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#actionCommit()
     */
    public void actionCommit() {

        // not implemented for this demo

    }

    /**
     * Builds the HTML for the demo5 form.<p>
     * 
     * @return the HTML for the demo5 form
     */
    public String buildDemo5Form() {

        StringBuffer result = new StringBuffer(1024);

        try {
            // create the dialog HTML
            result.append(createDialogHtml());
        } catch (Throwable t) {
            // since this is just a simple example...
            t.printStackTrace();
        }
        return result.toString();
    }

    /**
     * Creates the list of widgets for this dialog.<p>
     */
    protected void defineWidgets() {

        m_jobInfo = new CmsScheduledJobInfo();
        m_contextInfo = new CmsContextInfo();

        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "jobName", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "className", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "cronExpression", new CmsInputWidget()));

        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "userName", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "projectName", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "siteRoot", new CmsVfsFileWidget()));
        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "requestedUri", new CmsVfsFileWidget()));
        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "localeName", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "encoding", new CmsInputWidget()));
        addWidget(new CmsWidgetDialogParameter(m_contextInfo, "remoteAddr", new CmsInputWidget()));

        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "reuseInstance", new CmsCheckboxWidget()));
        addWidget(new CmsWidgetDialogParameter(m_jobInfo, "active", new CmsCheckboxWidget()));

        List testList = new ArrayList();
        testList.add("value1");
        testList.add("another value");
        addWidget(new CmsWidgetDialogParameter(testList, "theList", new CmsInputWidget()));

        Map testMap = new TreeMap();
        testMap.put("key1", "value1");
        testMap.put("key2", "another value");
        addWidget(new CmsWidgetDialogParameter(testMap, "theMap", new CmsInputWidget()));
    }

    /**
     * @see org.opencms.workplace.CmsWidgetDialog#getPageArray()
     */
    protected String[] getPageArray() {

        return new String[] {"page1"};
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);

        // fill the parameter values in the get/set methods
        fillParamValues(request);

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
        } else {
            // set the default action               
            setAction(ACTION_DEFAULT);
        }
    }
}