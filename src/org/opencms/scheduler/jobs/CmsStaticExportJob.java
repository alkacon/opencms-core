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

package org.opencms.scheduler.jobs;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.CmsLogReport;
import org.opencms.report.I_CmsReport;
import org.opencms.scheduler.I_CmsScheduledJob;
import org.opencms.staticexport.Messages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;

/**
 * A schedulable OpenCms job to write a complete static export (e.g. nightly exports).<p>
 *
 * This job does not have any parameters.<p>
 *
 * @since 6.0.0
 */
public class CmsStaticExportJob implements I_CmsScheduledJob {

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(CmsObject, Map)
     */
    public String launch(CmsObject cms, Map<String, String> parameters) throws Exception {

        I_CmsReport report = null;

        try {
            report = new CmsLogReport(cms.getRequestContext().getLocale(), CmsStaticExportJob.class);
            OpenCms.getStaticExportManager().exportFullStaticRender(true, report);
            Map<String, Object> eventData = new HashMap<String, Object>();
            eventData.put("purge", Boolean.TRUE);
            eventData.put(I_CmsEventListener.KEY_REPORT, report);
            OpenCms.fireCmsEvent(new CmsEvent(I_CmsEventListener.EVENT_FULLSTATIC_EXPORT, eventData));
        } catch (CmsException e) {
            report.println(e);
        } catch (IOException e) {
            report.println(e);
        } catch (ServletException e) {
            report.println(e);
        } finally {
            // append runtime statistics to the report
            if (report != null) {
                report.print(org.opencms.report.Messages.get().container(org.opencms.report.Messages.RPT_STAT_0));
                report.println(
                    org.opencms.report.Messages.get().container(
                        org.opencms.report.Messages.RPT_STAT_DURATION_1,
                        report.formatRuntime()));
                report.println(Messages.get().container(Messages.RPT_STATICEXPORT_END_0), I_CmsReport.FORMAT_HEADLINE);
            }
        }

        return null;
    }
}