/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsDatabaseImportThread.java,v $
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
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.workplace.CmsXmlLanguageFile;

/**
 * Imports an OpenCms export file into the VFS.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.1.10
 */
public class CmsDatabaseImportThread extends A_CmsReportThread {
    private CmsObject m_cms;

    private String m_importFile;

    /**
     * Imports an OpenCms export file into the VFS.<p>
     * 
     * @param cms the current OpenCms context object
     * @param importFile the file to import
     */
    public CmsDatabaseImportThread(CmsObject cms, String importFile) {
        super("OpenCms: Database import from " + importFile);
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        m_importFile = importFile;
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
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
            m_report.println(m_report.key("report.import_db_begin"), I_CmsReport.C_FORMAT_HEADLINE);
            m_cms.importResources(m_importFile, I_CmsConstants.C_ROOT, m_report);
            m_report.println(m_report.key("report.import_db_end"), I_CmsReport.C_FORMAT_HEADLINE);
        } catch (CmsException e) {
            m_report.println(e);
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.getMessage());
            }
        }
    }
}
