/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.tools.searchindex.sourcesearch;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.report.I_CmsReportThread;
import org.opencms.workplace.CmsDialog;
import org.opencms.workplace.list.A_CmsListExplorerDialog;
import org.opencms.workplace.list.A_CmsListReport;
import org.opencms.workplace.tools.CmsToolDialog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Provides a report for searching in contents.
 * <p>
 *
 * @since 7.5.3
 */
public class CmsSourceSearchReport extends A_CmsListReport {

    /**
     * Public constructor with JSP action element.
     * <p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsSourceSearchReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.
     * <p>
     *
     * @param context the JSP page context.
     *
     * @param req the JSP request.
     *
     * @param res the JSP response.
     */
    public CmsSourceSearchReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Used to close the current JSP dialog.<p>
     *
     * This method tries to include the URI stored in the workplace settings.
     * This URI is determined by the frame name, which has to be set
     * in the frame name parameter.<p>
     *
     * @throws JspException if including an element fails
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void actionCloseDialog() throws JspException {

        // create a map with empty "resource" parameter to avoid changing the folder when returning to explorer file list
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put(PARAM_RESOURCE, new String[] {""});
        // set action parameter to initial dialog call
        params.put(CmsDialog.PARAM_ACTION, new String[] {CmsDialog.DIALOG_INITIAL});
        params.put(A_CmsListExplorerDialog.PARAM_SHOW_EXPLORER, new String[] {Boolean.TRUE.toString()});
        params.put(PARAM_STYLE, new String[] {CmsToolDialog.STYLE_NEW});

        // close link parameter present
        try {
            HttpSession session = getJsp().getJspContext().getSession();
            Collection resultList = (Collection)session.getAttribute(
                CmsSearchReplaceSettings.ATTRIBUTE_NAME_SOURCESEARCH_RESULT_LIST);
            if ((resultList != null) && !resultList.isEmpty()) {
                getToolManager().jspForwardTool(this, "/searchindex/sourcesearch/fileslist", params);
            } else {
                getToolManager().jspForwardPage(this, "index.jsp", params);
            }
        } catch (Exception e) {
            // forward failed
            throw new JspException(e.getMessage(), e);
        }
    }

    /**
     *
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    @SuppressWarnings("rawtypes")
    @Override
    public I_CmsReportThread initializeThread() {

        CmsSearchReplaceSettings settings = (CmsSearchReplaceSettings)((Map)getSettings().getDialogObject()).get(
            CmsSourceSearchDialog.class.getName());
        if (settings == null) {
            settings = (CmsSearchReplaceSettings)((Map)getSettings().getDialogObject()).get(
                CmsSourceSearchDialog.class.getName());
        }

        // clear the matched file list in the session
        HttpSession session = getJsp().getJspContext().getSession();
        session.removeAttribute(CmsSearchReplaceSettings.ATTRIBUTE_NAME_SOURCESEARCH_RESULT_LIST);

        // no redirect to the matched file list is necessary here, because the
        // org.opencms.workplace.list.A_CmsListReport.actionCloseDialog() method is overwritten here in this class

        // start the thread
        I_CmsReportThread searchThread = new CmsSearchReplaceThread(session, getCms(), settings);

        return searchThread;
    }
}