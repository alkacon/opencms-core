/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.commons;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsReport;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.threads.CmsSynchronizeThread;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides an output window for a CmsReport.<p> 
 * 
 * @since 6.0.0 
 */
public class CmsSynchronizeReport extends CmsReport {

    /** The dialog type. */
    public static final String DIALOG_TYPE = "sync";

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsSynchronizeReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsSynchronizeReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Performs the move report, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void actionReport() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);
        switch (getAction()) {
            case ACTION_REPORT_UPDATE:
                setParamAction(REPORT_UPDATE);
                getJsp().include(FILE_REPORT_OUTPUT);
                break;
            case ACTION_REPORT_BEGIN:
            case ACTION_CONFIRMED:
                setParamRefreshWorkplace(CmsStringUtil.TRUE);
                //$FALL-THROUGH$
            default:
                CmsSynchronizeThread thread = new CmsSynchronizeThread(getCms());
                thread.start();
                setParamAction(REPORT_BEGIN);
                setParamThread(thread.getUUID().toString());
                getJsp().include(FILE_REPORT_OUTPUT);
                break;
        }
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
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
            setParamTitle(key(Messages.GUI_SYNC_FOLDERS_AND_FILES_0));
        }
    }
}
