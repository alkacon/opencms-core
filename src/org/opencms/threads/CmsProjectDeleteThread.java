/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsProjectDeleteThread.java,v $
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

import com.opencms.core.CmsException;
import com.opencms.file.CmsObject;

/**
 * Deletes a project.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.1.10
 */
public class CmsProjectDeleteThread extends A_CmsReportThread {

    private Throwable m_error;
    private int m_projectId;

    /**
     * Creates the project delete thread.<p>
     * 
     * @param cms the current OpenCms context object
     * @param projectId the project id to delete
     */
    public CmsProjectDeleteThread(CmsObject cms, int projectId) {
        super(cms, "OpenCms: Project deletion of " + projectId);
        m_projectId = projectId;
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
        return "";
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        try {
            getCms().deleteProject(m_projectId);
        } catch (CmsException e) {
            m_error = e;
            if (OpenCms.getLog(this).isErrorEnabled()) {
                OpenCms.getLog(this).error("Error deleting project " + m_projectId, e);
            }
        }
    }
}