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

package org.opencms.ui.apps.filehistory;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;

import org.apache.commons.logging.Log;

/**
 * Clears the file history of the OpenCms database.<p>
 *
 * @since 6.0.0
 */
public class CmsHistoryClearThread extends A_CmsReportThread {

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsHistoryClearThread.class.getName());

    /**Clear all deleted versions older than this date.*/
    private long m_dateClearDeletedOlder;

    /**amount of versions to keep for deleted resources. */
    private int m_keepDeletedVersions;

    /**amount of versions to keep.*/
    private int m_keepVersions;

    /**
     * Creates the history clear Thread.<p>
     *
     * @param cms the current OpenCms context object
     * @param keepV count of Versions to keep
     * @param keepD count of Versions to keep for deleted resources
     * @param date Clear all deleted versions older than this date
     */
    public CmsHistoryClearThread(CmsObject cms, int keepV, int keepD, long date) {

        super(
            cms,
            CmsVaadinUtils.getMessageText(
                Messages.GUI_FILEHISTORY_DELETE_THREAD_NAME_1,
                cms.getRequestContext().getCurrentProject().getName()));

        m_keepVersions = keepV;
        m_keepDeletedVersions = keepD;
        m_dateClearDeletedOlder = date;
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

        LOG.info("Start delete history thread from user " + getCms().getRequestContext().getCurrentUser().getName());
        LOG.info(
            "Parameter: m_keepVersions="
                + m_keepVersions
                + ", m_keepDeletedVersions="
                + m_keepDeletedVersions
                + ", m_dateClearDeletedOlder="
                + m_dateClearDeletedOlder);

        getReport().println(
            Messages.get().container(Messages.RPT_DELETE_FILEHISTORY_BEGIN_0),
            I_CmsReport.FORMAT_HEADLINE);

        if (m_dateClearDeletedOlder == 0) {
            m_dateClearDeletedOlder = -1;
        }

        // delete the historical files
        try {
            getCms().deleteHistoricalVersions(m_keepVersions, m_keepDeletedVersions, m_dateClearDeletedOlder, getReport());
            LOG.info("Delete history thread successfully finished.");
        } catch (CmsException e) {
            getReport().println(e);
            LOG.error("Delete history thread stoped because of exceptions", e);
        }
        LOG.info("Delete history thread closed.");
        getReport().println(
            Messages.get().container(Messages.RPT_DELETE_FILEHISTORY_END_0),
            I_CmsReport.FORMAT_HEADLINE);
    }
}