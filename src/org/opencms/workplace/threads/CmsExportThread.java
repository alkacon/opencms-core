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
import org.opencms.importexport.I_CmsImportExportHandler;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;

import org.apache.commons.logging.Log;

/**
 * Exports selected resources of the OpenCms into an OpenCms export file.<p>
 *
 * @since 6.0.0
 */
public class CmsExportThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsExportThread.class);

    /** The import export handler. */
    private I_CmsImportExportHandler m_handler;

    /**
     * Creates a new data export thread.<p>
     *
     * @param cms the current OpenCms context object
     * @param handler export handler containing the export data
     * @param old flag for old report mode
     */
    public CmsExportThread(CmsObject cms, I_CmsImportExportHandler handler, boolean old) {

        super(cms, "OpenCms: " + handler.getDescription());
        m_handler = handler;
        if (old) {
            initOldHtmlReport(cms.getRequestContext().getLocale());
        } else {
            initHtmlReport(cms.getRequestContext().getLocale());
        }
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        try {
            OpenCms.getImportExportManager().exportData(getCms(), m_handler, getReport());
        } catch (Throwable e) {
            getReport().println(e);
            LOG.error(Messages.get().getBundle().key(Messages.ERR_DB_EXPORT_0), e);
        }
    }
}