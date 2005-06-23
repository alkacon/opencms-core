/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/modules/CmsModulesListReplaceReport.java,v $
 * Date   : $Date: 2005/06/23 09:05:01 $
 * Version: $Revision: 1.3 $
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

package org.opencms.workplace.tools.modules;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReportThread;
import org.opencms.workplace.list.A_CmsListReport;
import org.opencms.workplace.threads.CmsModuleReplaceThread;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a report for imporintg modules.<p> 
 *
 * @author  Michael Emmerich 
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 6.0.0 
 */
public class CmsModulesListReplaceReport extends A_CmsListReport {

    /** Module. */
    private String m_paramModule;

    /** Modulename. */
    private String m_paramModulename;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsModulesListReplaceReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsModulesListReplaceReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Gets the module parameter.<p>
     * 
     * @return the module parameter
     */
    public String getParamModule() {

        return m_paramModule;
    }

    /**
     * Gets the modulename parameter.<p>
     * 
     * @return the modulename parameter
     */
    public String getParamModulename() {

        return m_paramModulename;
    }

    /** 
     * 
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    public I_CmsReportThread initializeThread() {

        String module = getParamModule();
        String modulename = getParamModulename();
        String importpath = OpenCms.getSystemInfo().getPackagesRfsPath();
        importpath = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf(importpath + "modules/" + module);

        I_CmsReportThread replaceThread = new CmsModuleReplaceThread(getCms(), modulename, importpath, false);

        return replaceThread;
    }

    /** 
     * Sets the module parameter.<p>
     * @param paramModule the module parameter
     */
    public void setParamModule(String paramModule) {

        m_paramModule = paramModule;
    }

    /** 
     * Sets the modulename parameter.<p>
     * @param paramModulename the module nameparameter
     */
    public void setParamModulename(String paramModulename) {

        m_paramModulename = paramModulename;
    }

}
