/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/search/Attic/CmsIndexingReport.java,v $
 * Date   : $Date: 2005/02/17 12:44:32 $
 * Version: $Revision: 1.8 $
 *
 * This program is part of the Alkacon OpenCms Software library.
 *
 * This license applies to all programs, pages, Java classes, parts and
 * modules of the Alkacon OpenCms Software library published by
 * Alkacon Software, unless otherwise noted.
 *
 * Copyright (C) 2003 - 2005 Alkacon Software (http://www.alkacon.com)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * For further information about Alkacon Software, please see the
 * companys website: http://www.alkacon.com.
 * 
 * For further information about OpenCms, please see the OpenCms project
 * website: http://www.opencms.org.
 * 
 * The names "Alkacon", "Alkacon Software" and "OpenCms" must not be used 
 * to endorse or promote products derived from this software without prior 
 * written permission. For written permission, please contact info@alkacon.com.
 * 
 * Products derived from this software may not be called "Alkacon", 
 * "Alkacon Software" or "OpenCms", nor may "Alkacon", "Alkacon Software" 
 * or "OpenCms" appear in their name, without prior written permission of 
 * Alkacon Software. 
 *
 * This program is also available under a commercial non-GPL license. For
 * pricing and ordering information, please inquire at sales@alkacon.com.
 */

package org.opencms.search;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.workplace.CmsReport;
import org.opencms.workplace.CmsWorkplaceSettings;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Implements methods for <code>CmsReport</code> to display the indexing progress.<p>
 * 
 * @author Carsten Weinholz (c.weinholz@alkacon.com)
 * @version $Revision: 1.8 $
 * @since 5.3.1
 */
public class CmsIndexingReport extends CmsReport {

    /** Postfix for error.message dialog keys in the resource bundle.<p> */
    public static final String DIALOG_TYPE = "indexing";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsIndexingReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsIndexingReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the report, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionReport() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(C_SESSION_WORKPLACE_CLASS, this);
        switch (getAction()) {
            case ACTION_REPORT_UPDATE:
                setParamAction(REPORT_UPDATE);
                getJsp().include(C_FILE_REPORT_OUTPUT);
                break;
            case ACTION_REPORT_BEGIN:
            case ACTION_CONFIRMED:
            default:

                CmsIndexingReportThread thread = new CmsIndexingReportThread(getCms());
                setParamAction(REPORT_BEGIN);
                setParamThread(thread.getUUID().toString());
                getJsp().include(C_FILE_REPORT_OUTPUT);
                break;
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);
        // set the dialog type
        setParamDialogtype(DIALOG_TYPE);
        // set the action for the JSP switch 
        if (DIALOG_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_CONFIRMED);
        } else if (REPORT_UPDATE.equals(getParamAction())) {
            setAction(ACTION_REPORT_UPDATE);
        } else if (REPORT_BEGIN.equals(getParamAction())) {
            setAction(ACTION_REPORT_BEGIN);
        } else if (REPORT_END.equals(getParamAction())) {
            setAction(ACTION_REPORT_END);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else {
            setAction(ACTION_DEFAULT);
            // add the title for the dialog 
            setParamTitle(key("title." + getParamDialogtype()));
        }
    }

}