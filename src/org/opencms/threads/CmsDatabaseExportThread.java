/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsDatabaseExportThread.java,v $
 * Date   : $Date: 2003/09/19 14:42:52 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.threads;

import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;

/**
 * Exports selected resources of the OpenCms VFS or COS into an OpenCms export file.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.1.10
 */
public class CmsDatabaseExportThread extends A_CmsReportThread {

    private long m_contentAge;
    private boolean m_excludeSystem;
    private boolean m_excludeUnchanged;
    private String[] m_exportModules;
    private String[] m_exportPaths;
    private boolean m_exportUserdata;
    private String m_fileName;
    private boolean m_moduledataExport;

    /**
     * Export the VFS (Virtual File System) resources.<p>
     *
     * @param cms the current OpenCms context object
     * @param fileName the file name to export to 
     * @param exportPaths the path to export
     * @param excludeSystem if true, do not export resources in the /system/ folder
     * @param excludeUnchanged if true, do not export unchanged resources
     * @param exportUserdata if true, also export user and group data
     * @param contentAge only files that are last modified after this date will be exported
     */
    public CmsDatabaseExportThread(
        CmsObject cms, 
        String fileName, 
        String[] exportPaths, 
        boolean excludeSystem, 
        boolean excludeUnchanged, 
        boolean exportUserdata, 
        long contentAge
    ) {
        super(cms, "OpenCms: Database VFS export to " + fileName);
        m_exportPaths = exportPaths;
        m_fileName = fileName;
        m_excludeSystem = excludeSystem;
        m_excludeUnchanged = excludeUnchanged;
        m_exportUserdata = exportUserdata;
        m_contentAge = contentAge;
        m_moduledataExport = false;
        initHtmlReport();
    }

    /**
     * Export the COS (Content Object Store) resources and the module data.<p>
     * 
     * @param cms the current OpenCms context object
     * @param fileName the file name to export to
     * @param exportChannels the channels to export
     * @param exportModules the modules to export 
     */
    public CmsDatabaseExportThread(
        CmsObject cms, 
        String fileName, 
        String[] exportChannels, 
        String[] exportModules
    ) {
        super(cms, "OpenCms: Database COS export to " + fileName);
        m_exportPaths = exportChannels;
        m_exportModules = exportModules;
        m_fileName = fileName;
        initHtmlReport();
        m_moduledataExport = true;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    public String getReportUpdate() {
        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            // do the export
            getReport().println(getReport().key("report.export_db_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            if (m_moduledataExport) {
                getCms().exportModuledata(m_fileName, m_exportPaths, m_exportModules, getReport());
            } else {
                getCms().exportResources(m_fileName, m_exportPaths, m_excludeSystem, m_excludeUnchanged, m_exportUserdata, m_contentAge, getReport());
            }
            getReport().println(getReport().key("report.export_db_end"), I_CmsReport.C_FORMAT_HEADLINE);
        } catch (CmsException e) {
            getReport().println(e);
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error exporting the database", e);
            }
        }
    }
}
