/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsSynchronizeThread.java,v $
 * Date   : $Date: 2005/02/17 12:44:32 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.synchronize.CmsSynchronize;

/**
 * Synchronizes a VFS folder with a folder form the "real" file system.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.9 $
 * @since 5.1.10
 */
public class CmsSynchronizeThread extends A_CmsReportThread {

    private Throwable m_error;

    /**
     * Creates the synchronize Thread.<p>
     * 
     * @param cms the current OpenCms context object
     */
    public CmsSynchronizeThread(CmsObject cms) {
        super(cms, "OpenCms: Synchronizing to project " + cms.getRequestContext().currentProject().getName());
        initHtmlReport(cms.getRequestContext().getLocale());
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
        getReport().println(getReport().key("report.sync_begin"), I_CmsReport.C_FORMAT_HEADLINE);
        getReport().println(getReport().key("report.sync_rfs_folder") + OpenCms.getSystemInfo().getSynchronizeSettings().getDestinationPathInRfs().replace('\\', '/'), I_CmsReport.C_FORMAT_HEADLINE);                         
        getReport().println(getReport().key("report.sync_vfs_resource") + OpenCms.getSystemInfo().getSynchronizeSettings().getSourcePathInVfs(), I_CmsReport.C_FORMAT_HEADLINE);                
        try {
            new CmsSynchronize(getCms(), getReport());
        } catch (CmsException e) {
            getReport().println(e);
        }
        getReport().println(getReport().key("report.sync_end"), I_CmsReport.C_FORMAT_HEADLINE);       
    }
}