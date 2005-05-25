/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/list/A_CmsListReport.java,v $
 * Date   : $Date: 2005/05/25 09:01:57 $
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

package org.opencms.workplace.list;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.report.I_CmsReportThread;
import org.opencms.workplace.CmsReport;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides a report in the list widget.<p> 
 *
 * @author  Michael Emmerich (m.emmerich@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public abstract class A_CmsListReport extends CmsReport {

     /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public A_CmsListReport(CmsJspActionElement jsp) {

        super(jsp);
   
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public A_CmsListReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
 
    }
      
    /**
     * @see org.opencms.workplace.CmsDialog#actionCloseDialog()
     */
    public void actionCloseDialog() throws JspException {

        getSettings().setHtmlList(null);
        super.actionCloseDialog();
    }

    /**
     * Performs the dialog actions depending on the initialized action.<p>
     * 
     * @throws JspException if dialog actions fail
     */
    public void displayReport() throws  JspException {
        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        switch (getAction()) {
            case ACTION_REPORT_END:
                actionCloseDialog();
                break;
            case ACTION_CANCEL:
                actionCloseDialog();
                break;
            case ACTION_REPORT_UPDATE:
                setParamAction(REPORT_UPDATE);
                getJsp().include(C_FILE_REPORT_OUTPUT);
                break;
            case ACTION_REPORT_BEGIN:

            case ACTION_DEFAULT:
            default:
                I_CmsReportThread m_thread = initializeThread();
                m_thread.start();
                setParamAction(REPORT_BEGIN);
                setParamThread(m_thread.getUUID().toString());
                getJsp().include(C_FILE_REPORT_OUTPUT);
        }
    }

    /**
     * Initalizes the report thread to use for this report.<p>
     * 
     * @return the reportd thread to use for this report.
     */
    public abstract I_CmsReportThread initializeThread();


    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        if (REPORT_UPDATE.equals(getParamAction())) {
            setAction(ACTION_REPORT_UPDATE);
        } else if (REPORT_BEGIN.equals(getParamAction())) {
            setAction(ACTION_REPORT_BEGIN);
        } else if (REPORT_END.equals(getParamAction())) {
            setAction(ACTION_REPORT_END);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            // set the default action               
            setAction(ACTION_DEFAULT);
        }
    }

}