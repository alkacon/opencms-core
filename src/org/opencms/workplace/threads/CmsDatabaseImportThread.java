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
import org.opencms.importexport.CmsImportParameters;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;

import org.apache.commons.logging.Log;

/**
 * Imports an OpenCms export file into the VFS.<p>
 *
 * @since 6.0.0
 */
public class CmsDatabaseImportThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDatabaseImportThread.class);

    /** The import file name. */
    private String m_importFile;

    /** The keep permissions flag. */
    private boolean m_keepPermissions;

    /**
     * Imports an OpenCms export file into the VFS.<p>
     *
     * @param cms the current OpenCms context object
     * @param importFile the file to import
     * @param keepPermissions if set, the permissions set on existing resources will not be modified
     */
    public CmsDatabaseImportThread(CmsObject cms, String importFile, boolean keepPermissions) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_DB_IMPORT_THREAD_NAME_1, importFile));
        m_importFile = importFile;
        m_keepPermissions = keepPermissions;
        initHtmlReport(cms.getRequestContext().getLocale());
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

        CmsImportParameters parameters = new CmsImportParameters(m_importFile, "/", m_keepPermissions);
        boolean indexingAlreadyPaused = OpenCms.getSearchManager().isOfflineIndexingPaused();
        try {
            if (!indexingAlreadyPaused) {
                OpenCms.getSearchManager().pauseOfflineIndexing();
            }
            OpenCms.getImportExportManager().importData(getCms(), getReport(), parameters);
        } catch (Throwable e) {
            getReport().println(e);
            if (LOG.isErrorEnabled()) {
                LOG.error(Messages.get().getBundle().key(Messages.ERR_DB_IMPORT_0), e);
            }
        } finally {
            if (!indexingAlreadyPaused) {
                OpenCms.getSearchManager().resumeOfflineIndexing();
            }
        }
    }
}