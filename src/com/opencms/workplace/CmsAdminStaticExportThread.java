/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminStaticExportThread.java,v $
* Date   : $Date: 2003/08/11 18:30:52 $
* Version: $Revision: 1.25 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.workplace;

import com.opencms.file.CmsObject;
import com.opencms.report.A_CmsReportThread;
import com.opencms.report.CmsHtmlReport;

/**
 * A thread to export resources into the physical file system.
 * 
 * @author Hanjo Riege
 * @version 1.0
 */

public class CmsAdminStaticExportThread extends A_CmsReportThread {

    private CmsObject m_cms;


    // the object to send the information to the workplace.
    private CmsHtmlReport m_report;

    /**
     * Starts a new static export Thread.<p>
     * 
     * @param cms the current cms context
     */
    public CmsAdminStaticExportThread(CmsObject cms) {
        super("OpenCms: Static export of project " + cms.getRequestContext().currentProject().getName());
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);
    }

    /**
     * Executes the static export Thread.<p>
     */
    public void run() {
        // TODO: Implement "crawler" thread here
    }

    /**
     * Returns the part of the report that is ready.<p>
     * 
     * @return the part of the report that is ready
     */
    public String getReportUpdate() {
        return m_report.getReportUpdate();
    }
}