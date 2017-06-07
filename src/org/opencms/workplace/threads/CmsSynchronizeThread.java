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

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.synchronize.CmsSynchronize;
import org.opencms.synchronize.CmsSynchronizeSettings;

/**
 * Synchronizes a VFS folder with a folder form the "real" file system.<p>
 *
 * @since 6.0.0
 */
public class CmsSynchronizeThread extends A_CmsReportThread {

    /** An error that occurred during the report. */
    private Throwable m_error;

    /** The current users synchonize settings. */
    private CmsSynchronizeSettings m_settings;

    /**
     * Creates the synchronize Thread.<p>
     *
     * @param cms the current OpenCms context object
     */
    public CmsSynchronizeThread(CmsObject cms) {

        super(
            cms,
            Messages.get().getBundle().key(
                Messages.GUI_SYNCHRONIZE_THREAD_NAME_1,
                cms.getRequestContext().getCurrentProject().getName()));
        initHtmlReport(cms.getRequestContext().getLocale());
        m_settings = new CmsUserSettings(cms).getSynchronizeSettings();
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

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        I_CmsReport report = getReport();

        report.println(Messages.get().container(Messages.RPT_SYNCHRONIZE_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);
        try {
            new CmsSynchronize(getCms(), m_settings, getReport());
        } catch (Throwable e) {
            m_error = e;
            report.println(e);
        }
        report.println(Messages.get().container(Messages.RPT_SYNCHRONIZE_END_0), I_CmsReport.FORMAT_HEADLINE);
    }
}