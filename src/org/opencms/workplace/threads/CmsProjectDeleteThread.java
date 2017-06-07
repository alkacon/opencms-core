/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.main.CmsLog;
import org.opencms.report.A_CmsReportThread;
import org.opencms.util.CmsUUID;

import org.apache.commons.logging.Log;

/**
 * Deletes a project.<p>
 *
 * @since 6.0.0
 */
public class CmsProjectDeleteThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsProjectDeleteThread.class);

    /** The throwable. */
    private Throwable m_error;

    /** The project id. */
    private CmsUUID m_projectId;

    /**
     * Creates the project delete thread.<p>
     *
     * @param cms the current OpenCms context object
     * @param projectId the project id to delete
     */
    public CmsProjectDeleteThread(CmsObject cms, CmsUUID projectId) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_DELETE_PROJECT_THREAD_NAME_1, projectId));
        m_projectId = projectId;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getError()
     */
    @Override
    public Throwable getError() {

        return m_error;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return "";
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        try {
            getCms().deleteProject(m_projectId);
        } catch (Throwable e) {
            m_error = e;
            LOG.warn(Messages.get().getBundle().key(Messages.LOG_PROJECT_DELETE_FAILED_1, m_projectId), e);
        }
    }
}