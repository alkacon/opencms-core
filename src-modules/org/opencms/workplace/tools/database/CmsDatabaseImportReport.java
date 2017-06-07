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
 * @since 6.0.0
 */
public class CmsDatabaseImportReport extends A_CmsListReport {

    /** The keep permissions flag. */
    private String m_keepPermissions;

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
     * Returns the keepPermissions parameter.<p>
     *
     * @return the keepPermissions parameter
     */
    public String getKeepPermissions() {

        return m_keepPermissions;
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
    @Override
    public I_CmsReportThread initializeThread() {

        String importFile = OpenCms.getSystemInfo().getPackagesRfsPath() + getParamFile();
        boolean keepPermissions = Boolean.valueOf(getKeepPermissions()).booleanValue();

        I_CmsReportThread importThread = new CmsDatabaseImportThread(getCms(), importFile, keepPermissions);

        return importThread;
    }

    /**
     * Sets the keepPermissions parameter.<p>
     *
     * @param keepPermissions the keepPermissions parameter
     */
    public void setKeepPermissions(String keepPermissions) {

        m_keepPermissions = keepPermissions;
    }

    /**
     * Sets the request parameter value for the file name to import.<p>
     *
     * @param file the request parameter value for the file name to import
     */
    public void setParamFile(String file) {

        m_paramFile = file;
    }

    /**
     * Sets the keepPermissions parameter.<p>
     *
     * @param keepPermissions the keepPermissions parameter
     */
    public void setParamKeepPermissions(String keepPermissions) {

        setKeepPermissions(keepPermissions);
    }

}