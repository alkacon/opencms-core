/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsReport.java,v $
 * Date   : $Date: 2003/09/08 18:21:28 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.workplace;

import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;

import com.opencms.flex.jsp.CmsJspActionElement;
import com.opencms.flex.util.CmsUUID;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides an output window for a CmsReport.<p> 
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.1.10
 */
public class CmsReport extends CmsDialog {       
    
    /** Max. byte size of report output on client */
    public static final int REPORT_UPDATE_SIZE = 512000;
    
    /** Update time for report reloading */
    public static final int REPORT_UPDATE_TIME = 2000;
    
    /** The Thread to display in this report */
    private CmsUUID m_paramThread;
    
    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsReport(CmsJspActionElement jsp) {
        super(jsp);
    }
    
    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {
        this(new CmsJspActionElement(context, req, res));
    }            
    
    /**
     * Loads a new report sub-element that will generate the report master frameset.<p>
     * 
     * @param jsp an initialized JSP context
     * @param threadId the id of the Thread to report
     * @throws JspException in case something goes wrong
     */
    public static void loadReportBegin(CmsJspActionElement jsp, String threadId) throws JspException {
        Map paramMap = new HashMap(jsp.getRequest().getParameterMap());
        paramMap.put(PARAM_ACTION, REPORT_BEGIN);
        paramMap.put(PARAM_THREAD, threadId);
        jsp.include(C_FILE_REPORT_OUTPUT, null, paramMap);            
    }
    
    /**
     * Loads a report update.<p>
     * 
     * @param jsp an initialized JSP context
     * @throws JspException in case something goes wrong
     */    
    public static void loadReportUpdate(CmsJspActionElement jsp) throws JspException {
        Map paramMap = new HashMap(jsp.getRequest().getParameterMap());
        paramMap.put(PARAM_ACTION, REPORT_UPDATE);
        jsp.include(C_FILE_REPORT_OUTPUT, null, paramMap);            
    }
   
    /**
     * Returns the Thread id to display in this report.<p>
     * 
     * @return the Thread id to display in this report
     */
    public String getParamThread() {
        if ((m_paramThread != null) && (! m_paramThread.equals(CmsUUID.getNullUUID()))) { 
            return m_paramThread.toString();
        } else {
            return null;
        }
    }
    
    /**
     * Returns the part of the report that is ready for output.<p>
     * 
     * @return the part of the report that is ready for output
     */
    public String getReportUpdate() {
        A_CmsReportThread thread = OpenCms.getThreadStore().retrieveThread(m_paramThread);
        if (thread != null) {            
            return thread.getReportUpdate();
        } else {
            return "";
        }
    }
    
    /**
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * This overloads the default method of the parent class.<p>
     * 
     * @return the start html of the page
     */
    public String htmlStart() {
        return pageHtml(HTML_START, true);
    }    
    
    /**
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * This overloads the default method of the parent class.<p>
     * 
     * @param loadStyles if true, the defaul style sheet will be loaded
     * @return the start html of the page
     */
    public String htmlStart(boolean loadStyles) {
        return pageHtml(HTML_START, loadStyles);
    }
        
    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {
        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the action for the JSP switch 
        if (REPORT_UPDATE.equals(getParamAction())) {
            setAction(ACTION_REPORT_UPDATE);         
        } else {
            setAction(ACTION_REPORT_BEGIN);
        }                 
    }
    
    /**
     * Returns true if the report Thread is still alive (i.e. running), false otherwise.<p>
     *  
     * @return true if the report Thread is still alive
     */
    public boolean isAlive() {
        A_CmsReportThread thread = OpenCms.getThreadStore().retrieveThread(m_paramThread);
        if (thread != null) {            
            return thread.isAlive();
        } else {
            return false;
        }
    }
    
    /**
     * Builds the start html of the page, including setting of DOCTYPE and 
     * inserting a header with the content-type.<p>
     * 
     * This overloads the default method of the parent class.<p>
     * 
     * @param segment the HTML segment (START / END)
     * @param loadStyles if true, the defaul style sheet will be loaded
     * @return the start html of the page
     */
    public String pageHtml(int segment, boolean loadStyles) {        
        if (segment == HTML_START) {
            StringBuffer result = new StringBuffer(512);
            result.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n");
            result.append("<html>\n<head>\n");
            result.append("<meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=");
            result.append(getEncoding());
            result.append("\">\n");
            if (loadStyles) {
                result.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
                result.append(getSkinUri());
                result.append("files/css_workplace.css\">\n");
            }
            return result.toString();
        } else {
            return "</html>";
        }
    }

    /**
     * Sets the Thread id to display in this report.<p>
     * 
     * @param value the Thread id to display in this report
     */
    public void setParamThread(String value) {
        try {
            m_paramThread = new CmsUUID(value);
        } catch (Exception e) {
            m_paramThread = CmsUUID.getNullUUID();
        }
    }    
}
