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

package org.opencms.workplace.tools.searchindex;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsRuntimeException;
import org.opencms.report.I_CmsReportThread;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplaceSettings;
import org.opencms.workplace.list.A_CmsListReport;
import org.opencms.workplace.tools.CmsToolManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * A report for displaying the rebuild process of the corresponding
 * <code>{@link org.opencms.workplace.tools.searchindex.CmsIndexingReportThread}</code>.<p>
 *
 * @since 6.0.0
 */
public class CmsRebuildReport extends A_CmsListReport {

    /** Indexes parameter: Value is a list of comma separated search index name. */
    public static final String PARAM_INDEXES = "indexes";

    /** The request parameter value for search indexes: comma-separated names. **/
    private String m_paramIndexes;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsRebuildReport.class);

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsRebuildReport(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRebuildReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);

    }

    /**
     * Returns the comma-separated String of index names of the indexes that have to be rebuilt.<p>
     *
     * @return the comma-separated String of index names of the indexes that have to be rebuilt
     */
    public String getParamIndexes() {

        return m_paramIndexes;
    }

    /**
     * Returns the <b>unstarted</b> <code>Thread</code> that will do the work of rebuilding the indexes
     * provided by the request parameter "indexes" value (comma-separated List).<p>
     *
     * @throws CmsRuntimeException if the request parameter "indexes" is missing.
     *
     * @return the <b>unstarted</b> <code>Thread</code> that will do the work of rebuilding the indexes
     *         provided by the request parameter "indexes" value (comma-separated List)
     *
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    @Override
    public I_CmsReportThread initializeThread() throws CmsRuntimeException {

        if (getParamIndexes() == null) {
            CmsIllegalArgumentException ex = new CmsIllegalArgumentException(
                Messages.get().container(Messages.ERR_SEARCHINDEX_EDIT_MISSING_PARAM_1, PARAM_INDEXES));
            LOG.warn(ex);

            try {
                getToolManager().jspForwardTool(this, "/searchindex", null);
            } catch (Exception e) {
                LOG.error(e);
            }

            throw ex;
        }
        List<String> indexes = extractIndexNames();
        CmsIndexingReportThread thread = new CmsIndexingReportThread(getCms(), indexes);
        return thread;
    }

    /**
     * Sets the comma-separated String of index names of the indexes that that have to be rebuilt.<p>
     *
     * @param paramIndexes the comma-separated String of index names of the indexes that have to be rebuilt
     */
    public void setParamIndexes(String paramIndexes) {

        m_paramIndexes = paramIndexes;
    }

    /**
     *
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        super.initWorkplaceRequestValues(settings, request);
        // closelink is a bit complicated: If a forward from a single searchindex overview page
        // was made, go back to that searchindex-overview. If more indexes are in the given
        // parameter "indexes" go back to the search management entry page...
        List<String> indexes = extractIndexNames();
        if (indexes.size() == 1) {
            // back to index overview
            Map<String, String[]> params = new HashMap<String, String[]>();
            params.put(A_CmsEditSearchIndexDialog.PARAM_INDEXNAME, new String[] {indexes.get(0)});
            setParamCloseLink(CmsToolManager.linkForToolPath(getJsp(), "/searchindex/singleindex", params));
        } else {
            // back to search entry page
            setParamCloseLink(CmsToolManager.linkForToolPath(getJsp(), "/searchindex"));
        }
    }

    /**
     * Extracts all modules to delete form the module parameter.<p>
     * @return list of module names
     */
    private List<String> extractIndexNames() {

        List<String> modules = new ArrayList<String>();

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamIndexes())) {
            StringTokenizer tok = new StringTokenizer(getParamIndexes(), ",");
            while (tok.hasMoreTokens()) {
                String module = tok.nextToken();
                modules.add(module);
            }
        }
        return modules;
    }
}
