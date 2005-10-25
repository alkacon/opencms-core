/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/CmsContentCheckReport.java,v $
 * Date   : $Date: 2005/10/25 15:14:32 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.content.check;

import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.report.I_CmsReportThread;
import org.opencms.workplace.list.A_CmsListReport;

import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides a report for checking the content of resources in the OpenCms VFS.<p> 
 *
 * @author  Michael Emmerich
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.1.2 
 */
public class CmsContentCheckReport extends A_CmsListReport {

    /** The object edited with this widget dialog. */
    protected Object m_dialogObject;

    /** The Content Check object. */
    private CmsContentCheck m_contentCheck;

    /** Request parameter for the class name to get the dialog object from. */
    private String m_paramClassname;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsContentCheckReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsContentCheckReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the dialog actions depending on the initialized action.<p>
     * 
     * @throws JspException if dialog actions fail
     */
    public void displayReport() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        switch (getAction()) {
            case ACTION_REPORT_END:
                // set the results of the content check
                m_contentCheck = (CmsContentCheck)((Map)getSettings().getDialogObject()).get(getParamClassname());
                I_CmsResourceCollector collector = new CmsContentCheckCollector(m_contentCheck.getResults());
                getSettings().setCollector(collector);
                actionCloseDialog();
                break;
            case ACTION_CANCEL:
                actionCloseDialog();
                break;
            case ACTION_REPORT_UPDATE:
                setParamAction(REPORT_UPDATE);
                getJsp().include(FILE_REPORT_OUTPUT);
                break;
            case ACTION_REPORT_BEGIN:
            case ACTION_CONFIRMED:
            case ACTION_DEFAULT:
            default:
                I_CmsReportThread m_thread = initializeThread();
                m_thread.start();
                setParamAction(REPORT_BEGIN);
                setParamThread(m_thread.getUUID().toString());
                getJsp().include(FILE_REPORT_OUTPUT);
        }
    }

    /**
     * Returns the request parameter value for the class name to get the dialog object from.<p>
     * 
     * @return the request parameter value for the class name to get the dialog object from
     */
    public String getParamClassname() {

        return m_paramClassname;
    }

    /** 
     * 
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    public I_CmsReportThread initializeThread() {

        m_contentCheck = (CmsContentCheck)((Map)getSettings().getDialogObject()).get(getParamClassname());

        I_CmsReportThread contentCheckThread = new CmsContentCheckThread(getCms(), m_contentCheck);

        return contentCheckThread;
    }

    /**
     * Stores the given object as "dialog object" for this widget dialog in the current users session.<p> 
     * 
     * @param dialogObject the object to store
     */
    public void setDialogObject(Object dialogObject) {

        m_dialogObject = dialogObject;
        if (dialogObject == null) {
            // null object: remove the entry from the map
            getDialogObjectMap().remove(getClass().getName());
        } else {
            getDialogObjectMap().put(getClass().getName(), dialogObject);
        }
    }

    /** 
     * Sets the request parameter value for the class name to get the dialog object from.<p>
     * 
     * @param className the request parameter value for the class name to get the dialog object from
     */
    public void setParamClassname(String className) {

        m_paramClassname = className;
    }

    /**
     * Returns the (internal use only) map of dialog objects.<p>
     * 
     * @return the (internal use only) map of dialog objects 
     */
    private Map getDialogObjectMap() {

        Map objects = (Map)getSettings().getDialogObject();
        if (objects == null) {
            // using hashtable as most efficient version of a synchronized map
            objects = new Hashtable();
            getSettings().setDialogObject(objects);
        }
        return objects;
    }

}