/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsDatabaseExportThread.java,v $
 * Date   : $Date: 2003/09/05 12:22:25 $
 * Version: $Revision: 1.1 $
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
import org.opencms.report.CmsHtmlReport;
import org.opencms.report.I_CmsReport;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;
import com.opencms.workplace.CmsXmlLanguageFile;

/**
 * Exports selected resources of the OpenCms VFS or COS into an OpenCms export file.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.1.10
 */
public class CmsDatabaseExportThread extends A_CmsReportThread {

    private CmsObject m_cms;
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
        super("OpenCms: Database VFS export to " + fileName);
        m_cms = cms;
        m_exportPaths = exportPaths;
        m_fileName = fileName;
        m_excludeSystem = excludeSystem;
        m_excludeUnchanged = excludeUnchanged;
        m_exportUserdata = exportUserdata;
        m_contentAge = contentAge;
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
        m_moduledataExport = false;
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
        super("OpenCms: Database COS export to " + fileName);
        m_cms = cms;
        m_exportPaths = exportChannels;
        m_exportModules = exportModules;
        m_fileName = fileName;
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
        m_moduledataExport = true;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    public String getReportUpdate() {
        return m_report.getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            // do the export
            m_report.println(m_report.key("report.export_db_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            if (m_moduledataExport) {
                m_cms.exportModuledata(m_fileName, m_exportPaths, m_exportModules, m_report);
            } else {
                m_cms.exportResources(m_fileName, m_exportPaths, m_excludeSystem, m_excludeUnchanged, m_exportUserdata, m_contentAge, m_report);
            }
            m_report.println(m_report.key("report.export_db_end"), I_CmsReport.C_FORMAT_HEADLINE);
        } catch (CmsException e) {
            m_report.println(e);
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.getMessage());
            }
        }
    }
}
