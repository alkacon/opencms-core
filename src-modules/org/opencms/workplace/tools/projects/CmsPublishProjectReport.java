/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/projects/CmsPublishProjectReport.java,v $
 * Date   : $Date: 2005/06/10 15:58:06 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.projects;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsProject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.report.I_CmsReportThread;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.list.A_CmsListReport;
import org.opencms.workplace.list.CmsHtmlList;
import org.opencms.workplace.threads.CmsPublishThread;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a report for publishing projects.<p> 
 *
 * @author  Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public class CmsPublishProjectReport extends A_CmsListReport {

    /** Request parameter name for the project ids. */
    public static final String PARAM_PROJECTIDS = "projectids";

    /** list of project ids. */
    private String m_paramProjectids;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsPublishProjectReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsPublishProjectReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Gets the module parameter.<p>
     * 
     * @return the module parameter
     */
    public String getParamProjectids() {

        return m_paramProjectids;
    }

    /** 
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    public I_CmsReportThread initializeThread() {

        String[] projectIds = CmsStringUtil.splitAsArray(getParamProjectids(), CmsHtmlList.C_ITEM_SEPARATOR);
        CmsPublishList list;
        try {
            CmsProject currentProject = getCms().getRequestContext().currentProject();
            getCms().getRequestContext().setCurrentProject(getCms().readProject(new Integer(projectIds[0]).intValue()));
            list = getCms().getPublishList();
            getCms().getRequestContext().setCurrentProject(currentProject);
        } catch (CmsException e) {
            throw new CmsRuntimeException(e.getMessageContainer(), e);
        }
        CmsPublishThread publishProjectThread = new CmsPublishThread(getCms(), list, getSettings());
        return publishProjectThread;
    }

    /** 
     * Sets the module parameter.<p>
     * @param paramModule the module parameter
     */
    public void setParamProjectids(String paramModule) {

        m_paramProjectids = paramModule;
    }
}
