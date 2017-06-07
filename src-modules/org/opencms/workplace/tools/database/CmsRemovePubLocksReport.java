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

package org.opencms.workplace.tools.database;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.report.I_CmsReportThread;
import org.opencms.util.CmsFileUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.A_CmsListReport;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * A report for displaying the remove publish lock process.<p>
 *
 * @since 7.0.2
 */
public class CmsRemovePubLocksReport extends A_CmsListReport {

    /** Resources parameter: Value is a list of comma separated resources. */
    public static final String PARAM_RESOURCES = "resources";

    /** The request parameter value for resources: comma-separated names. **/
    private String m_paramResources;

    /**
     * Public constructor with JSP action element.<p>
     *
     * @param jsp an initialized JSP action element
     */
    public CmsRemovePubLocksReport(CmsJspActionElement jsp) {

        super(jsp);

    }

    /**
     * Public constructor with JSP variables.<p>
     *
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsRemovePubLocksReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);

    }

    /**
     * Returns the comma-separated list of resources.<p>
     *
     * @return the comma-separated list of resources
     */
    public String getParamResources() {

        return m_paramResources;
    }

    /**
     * Returns the <b>unstarted</b> <code>Thread</code> that will do the work.<p>
     *
     * @return the <b>unstarted</b> <code>Thread</code> that will do the work
     *
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    @Override
    public I_CmsReportThread initializeThread() {

        List resources = new ArrayList();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(getParamResources())) {
            resources = CmsStringUtil.splitAsList(getParamResources(), ",");
        }
        resources = CmsFileUtil.removeRedundancies(resources);

        CmsRemovePubLocksThread thread = new CmsRemovePubLocksThread(getCms(), resources);
        return thread;
    }

    /**
     * Sets the comma-separated list of resources.<p>
     *
     * @param paramIndexes the comma-separated list of resources
     */
    public void setParamResources(String paramIndexes) {

        m_paramResources = paramIndexes;
    }
}
