/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsAdminHistoryClearThread.java,v $
 * Date   : $Date: 2003/10/10 13:18:22 $
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

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;

import java.util.Map;

import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

/**
 * Clears the file history of the OpenCms database.<p>
 * 
 * @author  Andreas Zahner (a.zahner@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.1.12
 */
public class CmsAdminHistoryClearThread extends A_CmsReportThread {

    private Throwable m_error;
    private Map m_params;

    /**
     * Creates the history clear Thread.<p>
     * 
     * @param cms the current OpenCms context object
     * @param params the necessary parameters to delete the backup versions
     */
    public CmsAdminHistoryClearThread(CmsObject cms, Map params) {
        super(cms, "OpenCms: Synchronizing to project " + cms.getRequestContext().currentProject().getName());
        m_params = params;
        initHtmlReport();
        start();
    }
    
    /**
     * @see org.opencms.report.A_CmsReportThread#getError()
     */
    public Throwable getError() {
        return m_error;
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
        getReport().println(getReport().key("report.history.begin"), I_CmsReport.C_FORMAT_HEADLINE);
        
        // get the necessary parameters from the map
        int versions = Integer.parseInt((String)m_params.get("versions"));
        long timeStamp = Long.parseLong((String)m_params.get("timeStamp"));
    
        // delete the backup files
        try {
            getCms().deleteBackups(timeStamp, versions, getReport());
        } catch (CmsException e) {
            getReport().println(e);
        }         
        getReport().println(getReport().key("report.history.end"), I_CmsReport.C_FORMAT_HEADLINE);       
    }
}