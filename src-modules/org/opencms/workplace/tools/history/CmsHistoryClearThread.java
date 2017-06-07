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

package org.opencms.workplace.tools.history;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

/**
 * Clears the file history of the OpenCms database.<p>
 *
 * @since 6.0.0
 */
public class CmsHistoryClearThread extends A_CmsReportThread {

    private CmsHistoryClear m_historyClear;

    /**
     * Creates the history clear Thread.<p>
     *
     * @param cms the current OpenCms context object
     * @param historyClear the settings to clear the history
     */
    public CmsHistoryClearThread(CmsObject cms, CmsHistoryClear historyClear) {

        super(
            cms,
            Messages.get().getBundle().key(
                Messages.GUI_HISTORY_CLEAR_THREAD_NAME_1,
                cms.getRequestContext().getCurrentProject().getName()));
        m_historyClear = historyClear;
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

        getReport().println(Messages.get().container(Messages.RPT_DELETE_HISTORY_BEGIN_0), I_CmsReport.FORMAT_HEADLINE);

        // get the necessary parameters from the map
        int versions = m_historyClear.getKeepVersions();
        int versionsDeleted;
        long timeDeleted = m_historyClear.getClearOlderThan();

        if (timeDeleted == 0) {
            timeDeleted = -1;
        }

        if (m_historyClear.getClearDeletedMode().equals(CmsHistoryClearDialog.MODE_CLEANDELETED_DELETE_NONE)) {
            versionsDeleted = -1;
        } else if (m_historyClear.getClearDeletedMode().equals(CmsHistoryClearDialog.MODE_CLEANDELETED_DELETE_ALL)) {
            versionsDeleted = 0;
        } else {
            versionsDeleted = 1;
        }

        // delete the historical files
        try {
            getCms().deleteHistoricalVersions(versions, versionsDeleted, timeDeleted, getReport());
        } catch (CmsException e) {
            getReport().println(e);
        }
        getReport().println(Messages.get().container(Messages.RPT_DELETE_HISTORY_END_0), I_CmsReport.FORMAT_HEADLINE);
    }
}