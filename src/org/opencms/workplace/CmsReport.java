/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/CmsReport.java,v $
 * Date   : $Date: 2005/06/23 11:11:33 $
 * Version: $Revision: 1.24 $
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

package org.opencms.workplace;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.util.CmsUUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Provides an output window for a CmsReport.<p> 
 *
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.24 $ 
 * 
 * @since 6.0.0 
 */
public class CmsReport extends CmsDialog {

    /** Request parameter key for the type of the report. */
    public static final String PARAM_REPORT_CONTINUEKEY = "reportcontinuekey";

    /** Request parameter key for the type of the report. */
    public static final String PARAM_REPORT_TYPE = "reporttype";

    /** Max. byte size of report output on client. */
    public static final int REPORT_UPDATE_SIZE = 512000;

    /** Update time for report reloading. */
    public static final int REPORT_UPDATE_TIME = 2000;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsReport.class);

    /** Flag for refreching workplace .*/
    private String m_paramRefreshWorkplace;

    /** The key name which contains the localized message for the continue checkbox. */
    private String m_paramReportContinueKey;

    /** The type of this report. */
    private String m_paramReportType;

    /** The thread to display in this report. */
    private CmsUUID m_paramThread;

    /** The next thread to display after this report. */
    private String m_paramThreadHasNext;

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
     * Returns an initialized CmsReport instance that is read from the request attributes.<p>
     * 
     * This method is used by dialog elements. 
     * The dialog elements do not initialize their own workplace class, 
     * but use the initialized instance of the "master" class.
     * This is required to ensure that parameters of the "master" class
     * can properly be kept on the dialog elements.<p>
     * 
     * To prevent null pointer exceptions, an empty dialog is returned if 
     * nothing is found in the request attributes.<p>
     *  
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     * @return an initialized CmsDialog instance that is read from the request attributes
     */
    public static CmsReport initCmsReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        CmsReport wp = (CmsReport)req.getAttribute(CmsWorkplace.C_SESSION_WORKPLACE_CLASS);
        if (wp == null) {
            // ensure that we don't get null pointers if the page is directly called
            wp = new CmsReport(new CmsJspActionElement(context, req, res));
        }
        return wp;
    }

    /**
     * Builds a button row with an "Ok", a "Cancel" and a "Details" button.<p>
     * 
     * This row is displayed when the first report is running.<p>
     * 
     * @param okAttrs optional attributes for the ok button
     * @param cancelAttrs optional attributes for the cancel button
     * @param detailsAttrs optional attributes for the details button
     * @return the button row
     */
    public String dialogButtonsContinue(String okAttrs, String cancelAttrs, String detailsAttrs) {

        if (detailsAttrs == null || "".equals(detailsAttrs.trim())) {
            detailsAttrs = "";
        } else {
            detailsAttrs += " ";
        }
        return dialogButtons(new int[] {BUTTON_OK, BUTTON_CANCEL, BUTTON_DETAILS}, new String[] {
            okAttrs,
            cancelAttrs,
            detailsAttrs + "onclick=\"switchOutputFormat();\""});
    }

    /**
     * Builds a button row with an "Ok", a "Cancel" and a "Details" button.<p>
     * 
     * This row is used when a single report is running or after the first report has finished.<p>
     * 
     * @param okAttrs optional attributes for the ok button
     * @param cancelAttrs optional attributes for the cancel button
     * @param detailsAttrs optional attributes for the details button
     * @return the button row
     */
    public String dialogButtonsOkCancelDetails(String okAttrs, String cancelAttrs, String detailsAttrs) {

        if (detailsAttrs == null || "".equals(detailsAttrs.trim())) {
            detailsAttrs = "";
        } else {
            detailsAttrs += " ";
        }

        if ("true".equals(getParamThreadHasNext()) && !"".equals(getParamReportContinueKey())) {
            return dialogButtons(new int[] {BUTTON_OK, BUTTON_CANCEL, BUTTON_DETAILS}, new String[] {
                okAttrs,
                cancelAttrs,
                detailsAttrs + "onclick=\"switchOutputFormat();\""});
        }
        return dialogButtons(new int[] {BUTTON_OK, BUTTON_DETAILS}, new String[] {
            okAttrs,
            detailsAttrs + "onclick=\"switchOutputFormat();\""});
    }

    /**
     * Returns if the workplace must be refreshed.<p>
     * 
     * @return "true" if the workplace must be refreshed.
     */
    public String getParamRefreshWorkplace() {

        return m_paramRefreshWorkplace;
    }

    /**
     * Returns the key name which contains the localized message for the continue checkbox.<p>
     * 
     * @return the key name which contains the localized message for the continue checkbox
     */
    public String getParamReportContinueKey() {

        if (m_paramReportContinueKey == null) {
            m_paramReportContinueKey = "";
        }
        return m_paramReportContinueKey;
    }

    /**
     * Returns the type of this report.<p>
     * 
     * @return the type of this report
     */
    public String getParamReportType() {

        if (m_paramReportType == null) {
            // the default report type is the simple report
            setParamReportType(getSettings().getUserSettings().getWorkplaceReportType());
        }

        return m_paramReportType;
    }

    /**
     * Returns the Thread id to display in this report.<p>
     * 
     * @return the Thread id to display in this report
     */
    public String getParamThread() {

        if ((m_paramThread != null) && (!m_paramThread.equals(CmsUUID.getNullUUID()))) {
            return m_paramThread.toString();
        } else {
            return null;
        }
    }

    /**
     * Returns if another report is following this report.<p>
     * 
     * @return "true" if another report is following this report
     */
    public String getParamThreadHasNext() {

        if (m_paramThreadHasNext == null) {
            m_paramThreadHasNext = "";
        }
        return m_paramThreadHasNext;
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
     * Returns if the report generated an error output.<p>
     * 
     * @return true if the report generated an error, otherwise false
     */
    public boolean hasError() {

        A_CmsReportThread thread = OpenCms.getThreadStore().retrieveThread(m_paramThread);
        if (thread != null) {
            return thread.hasError();
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
     * Checks whether this is a simple report.<p>
     * 
     * @return true, if the type of this report is a "simple"
     */
    public boolean isSimpleReport() {

        return getParamReportType().equalsIgnoreCase(I_CmsReport.REPORT_TYPE_SIMPLE);
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

        if (useNewStyle()) {
            return super.pageHtml(segment, null, getParamTitle());
        }
        if (segment == HTML_START) {
            StringBuffer result = new StringBuffer(512);
            result.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\">\n");
            result.append("<html>\n<head>\n");
            result.append("<meta HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=");
            result.append(getEncoding());
            result.append("\">\n");
            if (loadStyles) {
                result.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
                result.append(getStyleUri(getJsp(), "workplace.css"));
                result.append("\">\n");
                result.append("<script type=\"text/javascript\">\n");
                result.append(dialogScriptSubmit());
                result.append("</script>\n");
            }
            return result.toString();
        } else {
            return "</html>";
        }

    }

    /**
     * Sets  if the workplace must be refreshed.<p>
     * 
     * @param value "true" (String) if the workplace must be refreshed.
     */
    public void setParamRefreshWorkplace(String value) {

        m_paramRefreshWorkplace = value;
    }

    /**
     * Sets the key name which contains the localized message for the continue checkbox.<p>
     * 
     * @param key the key name which contains the localized message for the continue checkbox
     */
    public void setParamReportContinueKey(String key) {

        m_paramReportContinueKey = key;
    }

    /**
     * Sets the type of this report.<p>
     * 
     * @param value the type of this report
     */
    public void setParamReportType(String value) {

        m_paramReportType = value;
    }

    /**
     * Sets the Thread id to display in this report.<p>
     * 
     * @param value the Thread id to display in this report
     */
    public void setParamThread(String value) {

        m_paramThread = CmsUUID.getNullUUID();
        if (value != null) {
            try {
                m_paramThread = new CmsUUID(value);
            } catch (Exception e) {
                // can usually be ignored
                if (LOG.isInfoEnabled()) {
                    LOG.info(Messages.get().key(Messages.LOG_THREAD_CREATION_FAILED_1, new Integer(value)), e);
                }
            }
        }
    }

    /**
     * Sets if another report is following this report.<p>
     * 
     * @param value "true" if another report is following this report
     */
    public void setParamThreadHasNext(String value) {

        m_paramThreadHasNext = value;
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

}