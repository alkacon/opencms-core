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

package org.opencms.workplace.tools.database;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsEvent;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import java.util.HashMap;
import java.util.Map;

/**
 * Does a full static export of all system resources in the current site.<p>
 *
 * @since 6.0.0
 */
public class CmsStaticExportThread extends A_CmsReportThread {

    /**
     * Creates a static export Thread.<p>
     *
     * @param cms the current cms context
     */
    public CmsStaticExportThread(CmsObject cms) {

        super(cms, Messages.get().getBundle().key(Messages.GUI_STATEXP_THREAD_NAME_0));
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

        getReport().println(
            org.opencms.staticexport.Messages.get().container(
                org.opencms.staticexport.Messages.RPT_STATICEXPORT_BEGIN_0),
            I_CmsReport.FORMAT_HEADLINE);
        try {
            OpenCms.getStaticExportManager().exportFullStaticRender(true, getReport());
            Map eventData = new HashMap();
            eventData.put("purge", Boolean.TRUE);
            eventData.put(I_CmsEventListener.KEY_REPORT, getReport());
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FULLSTATIC_EXPORT, eventData));
        } catch (Exception e) {
            getReport().println(e);
        }

        // append runtime statistics to report
        getReport().print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_STAT_0));
        getReport().println(
            org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_STAT_DURATION_1,
                getReport().formatRuntime()));
        getReport().println(
            org.opencms.staticexport.Messages.get().container(org.opencms.staticexport.Messages.RPT_STATICEXPORT_END_0),
            I_CmsReport.FORMAT_HEADLINE);
    }
}