/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsSynchronizeThread.java,v $
 * Date   : $Date: 2003/09/10 07:20:04 $
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

package org.opencms.threads;

import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;

import java.util.Vector;

/**
 * Synchronizes a VFS folder with a folder form the "real" file system.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.1.10
 */
public class CmsSynchronizeThread extends A_CmsReportThread {

    private Throwable m_error;
    private Vector m_resources;
    private boolean m_newProject;

    /**
     * Creates the synchronize Thread.<p>
     * 
     * @param cms the current OpenCms context object
     * @param resources the VFS resources to include in the synchronization
     * @param newProject if true, a new project will be created for the synchronization
     * @param session
     */
    public CmsSynchronizeThread(CmsObject cms, Vector resources, boolean newProject) {
        super(cms, "OpenCms: Synchronizing foldes in project " + cms.getRequestContext().currentProject().getName());
        m_resources = resources;
        m_newProject = newProject;
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
        try {
            // synchronize the resource
            for (int i = 0; i < m_resources.size(); i++) {
                // if a new project was created for synchronisation, copy the resource to the project
                if (m_newProject) {
                    getCms().copyResourceToProject((String)m_resources.elementAt(i));
                }
                getCms().syncFolder((String)m_resources.elementAt(i), getReport());
            }
        } catch (CmsException e) {
            m_error = e;
            if (OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, e.getMessage());
            }
        }
    }
}