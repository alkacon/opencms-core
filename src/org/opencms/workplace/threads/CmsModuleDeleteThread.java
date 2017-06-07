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
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModuleManager;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Deletes a module.<p>
 *
 * @since 6.0.0
 */
public class CmsModuleDeleteThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleDeleteThread.class);

    /** A list of module name to delete. */
    private List<String> m_moduleNames;

    /** mode indicating if pre-replacement or final deletion. */
    private boolean m_replaceMode;

    /**
     * Creates the module delete thread.<p>
     *
     * @param cms the current cms context
     * @param moduleNames the name of the module
     * @param replaceMode the replace mode
     */
    public CmsModuleDeleteThread(CmsObject cms, List<String> moduleNames, boolean replaceMode) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_DELETE_MODULE_THREAD_NAME_1, moduleNames));
        m_moduleNames = moduleNames;
        m_replaceMode = replaceMode;
        initHtmlReport(cms.getRequestContext().getLocale());
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_DELETE_THREAD_CONSTRUCTED_0));
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

        I_CmsReport report = getReport();
        boolean indexingAlreadyPaused = OpenCms.getSearchManager().isOfflineIndexingPaused();
        try {
            if (!indexingAlreadyPaused) {
                OpenCms.getSearchManager().pauseOfflineIndexing();
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DELETE_THREAD_STARTED_0));
            }
            if (!m_replaceMode) {
                OpenCms.getModuleManager().checkModuleSelectionList(m_moduleNames, null, true);
            }
            m_moduleNames = CmsModuleManager.topologicalSort(m_moduleNames, null);
            Collections.reverse(m_moduleNames);

            Iterator<String> j = m_moduleNames.iterator();
            while (j.hasNext()) {
                String moduleName = j.next();

                moduleName = moduleName.replace('\\', '/');

                // now delete the module
                OpenCms.getModuleManager().deleteModule(getCms(), moduleName, m_replaceMode, report);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_DELETE_THREAD_FINISHED_0));
            }
        } catch (Throwable e) {
            report.println(e);
            LOG.error(Messages.get().getBundle().key(Messages.LOG_MODULE_DELETE_FAILED_1, m_moduleNames), e);
        } finally {
            if (!indexingAlreadyPaused) {
                OpenCms.getSearchManager().resumeOfflineIndexing();
            }
        }
    }
}