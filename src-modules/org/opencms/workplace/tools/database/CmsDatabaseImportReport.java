/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/database/CmsDatabaseImportReport.java,v $
 * Date   : $Date: 2005/06/09 07:59:25 $
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

package org.opencms.workplace.tools.database;

import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReportThread;
import org.opencms.workplace.list.A_CmsListReport;
import org.opencms.workplace.threads.CmsDatabaseImportThread;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

/**
 * Provides a report for importing zipped files or folder to the OpenCms VFS.<p> 
 *
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 6.0
 */
public class CmsDatabaseImportReport extends A_CmsListReport {
    

    /** Request parameter for the file name to import. */
    private String m_paramFile;

    /**
     * Public constructor with JSP action element.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsDatabaseImportReport(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsDatabaseImportReport(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Returns the request parameter value for the file name to import.<p>
     * 
     * @return the request parameter value for the file name to import
     */
    public String getParamFile() {

        return m_paramFile;
    }

    /** 
     * 
     * @see org.opencms.workplace.list.A_CmsListReport#initializeThread()
     */
    public I_CmsReportThread initializeThread() {

        String importFile = OpenCms.getSystemInfo().getPackagesRfsPath() + getParamFile();

        I_CmsReportThread importThread = new CmsDatabaseImportThread(getCms(), importFile, false);

        return importThread;
    }

    /** 
     * Sets the request parameter value for the file name to import.<p>
     * 
     * @param file the request parameter value for the file name to import
     */
    public void setParamFile(String file) {

        m_paramFile = file;
    }

}