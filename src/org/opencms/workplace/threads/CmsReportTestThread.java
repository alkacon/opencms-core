/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/threads/Attic/CmsReportTestThread.java,v $
 * Date   : $Date: 2005/06/22 10:38:29 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.threads;

import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import org.opencms.file.CmsObject;

/**
 * Does a full static export of all system resources in the current site.<p>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.3 $
 * @since 5.1.10
 */
public class CmsReportTestThread extends A_CmsReportThread {

    private int m_count;

    /**
     * Creates a static export Thread.<p>
     * 
     * @param cms the current cms context
     * @param count the count for the test output
     */
    public CmsReportTestThread(CmsObject cms, int count) {

        super(cms, Messages.get().key(
            cms.getRequestContext().getLocale(),
            Messages.GUI_REPORT_TEST_THREAD_NAME_0,
            null));
        m_count = count;
        initHtmlReport(cms.getRequestContext().getLocale());
        start();
    }

    /**
     * @see org.opencms.report.A_CmsReportThread#getReportUpdate()
     */
    public String getReportUpdate() {

        return getReport().getReportUpdate();
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        I_CmsReport report = getReport();
        report.println(
            Messages.get().container(Messages.RPT_TEST_REPORT_BEGIN_0),
            I_CmsReport.C_FORMAT_HEADLINE);
        for (int i = 0; i < m_count; i++) {
            report.print(org.opencms.report.Messages.get().container(
                org.opencms.report.Messages.RPT_SUCCESSION_2,
                String.valueOf(i),
                String.valueOf(m_count)));
            report.println(Messages.get().container(Messages.RPT_TEST_REPORT_OUTPUT_0));
            try {
                sleep(250);
            } catch (InterruptedException e) {
                // just continue
            }
        }
        getReport().println(
            Messages.get().container(Messages.RPT_TEST_REPORT_END_0),
            I_CmsReport.C_FORMAT_HEADLINE);
    }
}