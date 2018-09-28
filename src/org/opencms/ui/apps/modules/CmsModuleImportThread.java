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

package org.opencms.ui.apps.modules;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleManager;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import org.apache.commons.logging.Log;

/**
 * Report thread for importing a module.<p>
 */
public class CmsModuleImportThread extends A_CmsReportThread {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleImportThread.class);

    /** The cms object used for the import. */
    private CmsObject m_cms;

    /** The module metadata. */
    private CmsModule m_module;

    /** The module file path. */
    private String m_path;

    /**
     * Creates a new instance.<p>
     *
     * @param cms the cms object used for the import
     * @param module the module
     * @param path the module file path
     */
    public CmsModuleImportThread(CmsObject cms, CmsModule module, String path) {

        super(cms, "Import of " + path);
        m_module = module;
        m_path = path;
        m_cms = cms;
        initHtmlReport(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms));
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    @Override
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        LOG.info("Starting import thread for " + m_module.getName() + ", import =  " + m_path);
        I_CmsReport report = getReport();

        try {
            CmsObject cms = m_cms;
            CmsModuleManager manager = OpenCms.getModuleManager();
            manager.replaceModule(cms, m_path, report);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            report.addError(e);
        }

    }

}
