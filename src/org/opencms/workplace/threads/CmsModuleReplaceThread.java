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
import org.opencms.report.A_CmsReportThread;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Replaces a module.<p>
 *
 * @since 6.0.0
 */
public class CmsModuleReplaceThread extends A_CmsReportThread {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleReplaceThread.class);

    /** The delete thread. */
    private A_CmsReportThread m_deleteThread;

    /** The import thread. */
    private A_CmsReportThread m_importThread;

    /** The module name. */
    private String m_moduleName;

    /** The replacement phase. */
    private int m_phase;

    /** The report content. */
    private String m_reportContent;

    /** The zip file name. */
    private String m_zipName;

    /**
     * Creates the module replace thread.<p>
     * @param cms the current cms context
     * @param moduleName the name of the module
     * @param zipName the name of the module ZIP file
     */
    public CmsModuleReplaceThread(CmsObject cms, String moduleName, String zipName) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_REPLACE_MODULE_THREAD_NAME_1, moduleName));
        m_moduleName = moduleName;
        m_zipName = zipName;

        List<String> modules = new ArrayList<String>();
        modules.add(m_moduleName);
        m_deleteThread = new CmsModuleDeleteThread(getCms(), modules, true);
        m_importThread = new CmsDatabaseImportThread(getCms(), m_zipName, true);
        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_REPLACE_THREAD_CONSTRUCTED_0));
        }
        m_phase = 0;
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        switch (m_phase) {
            case 1:
                return m_deleteThread.getReportUpdate();
            case 2:
                String content;
                if (m_reportContent != null) {
                    content = m_reportContent;
                    m_reportContent = null;
                } else {
                    content = "";
                }
                return content + m_importThread.getReportUpdate();
            default:
                // noop
        }
        return "";
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_REPLACE_THREAD_START_DELETE_0));
        }

        try {
            OpenCms.getSearchManager().pauseOfflineIndexing();
            // phase 1: delete the existing module
            m_phase = 1;
            m_deleteThread.start();
            try {
                m_deleteThread.join();
            } catch (InterruptedException e) {
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            // get remaining report contents
            m_reportContent = m_deleteThread.getReportUpdate();
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_REPLACE_THREAD_START_IMPORT_0));
            }
            // phase 2: import the new module
            m_phase = 2;
            m_importThread.start();
            try {
                m_importThread.join();
            } catch (InterruptedException e) {
                // should never happen
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug(Messages.get().getBundle().key(Messages.LOG_REPLACE_THREAD_FINISHED_0));
            }
        } finally {
            OpenCms.getSearchManager().resumeOfflineIndexing();
        }
    }
}