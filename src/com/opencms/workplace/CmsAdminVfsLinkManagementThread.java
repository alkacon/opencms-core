/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminVfsLinkManagementThread.java,v $
 * Date   : $Date: 2003/07/21 12:45:17 $
 * Version: $Revision: 1.3 $
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
 
package com.opencms.workplace;

import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.report.CmsHtmlReport;
import com.opencms.report.I_CmsReport;

/**
 * A thread to join all VFS links with their target resources.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsAdminVfsLinkManagementThread extends Thread implements I_CmsConstants {

    private CmsObject m_cms;
    private I_CmsReport m_report;

    public CmsAdminVfsLinkManagementThread(CmsObject cms) {
        m_cms = cms;
        m_cms.getRequestContext().setUpdateSessionEnabled(false);
        String locale = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        m_report = new CmsHtmlReport(locale);        
    }
    
    public void run() {
        m_report.println( "Disabled" );
    }    

    /**
     * returns the part of the report that is ready.
     */
    public String getReportUpdate(){
        return m_report.getReportUpdate();
    }
}