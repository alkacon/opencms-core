/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/threads/CmsProjectDeleteThread.java,v $
 * Date   : $Date: 2005/06/23 11:11:55 $
 * Version: $Revision: 1.6 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.threads;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.report.A_CmsReportThread;

import org.apache.commons.logging.Log;

/**
 * Deletes a project.<p>
 *
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.6 $ 
 * 
 * @since 6.0.0 
 */
public class CmsProjectDeleteThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsProjectDeleteThread.class);

    private Throwable m_error;
    private int m_projectId;

    /**
     * Creates the project delete thread.<p>
     * 
     * @param cms the current OpenCms context object
     * @param projectId the project id to delete
     */
    public CmsProjectDeleteThread(CmsObject cms, int projectId) {

        super(cms, Messages.get().key(
            cms.getRequestContext().getLocale(),
            Messages.GUI_DELETE_PROJECT_THREAD_NAME_1,
            new Object[] {new Integer(projectId)}));
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
            LOG.warn(Messages.get().key(Messages.LOG_PROJECT_DELETE_FAILED_1, new Integer(m_projectId)), e);
        }
    }
}