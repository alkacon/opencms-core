/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/threads/Attic/CmsReportTestThread.java,v $
 * Date   : $Date: 2004/02/06 20:52:43 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.threads;

import org.opencms.report.A_CmsReportThread;
import org.opencms.report.I_CmsReport;

import com.opencms.file.CmsObject;

/**
 * Does a full static export of all system resources in the current site.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.2 $
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
        super(cms, "OpenCms: Report test");
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
        getReport().println("Test output starting...", I_CmsReport.C_FORMAT_HEADLINE);
        for (int i=0; i<m_count; i++) {
            getReport().println("( " + i + " / " + m_count + " ) Test output", I_CmsReport.C_FORMAT_DEFAULT);
            try {
                sleep(250);
            } catch (InterruptedException e) {
                // just continue
            }
        }
        getReport().println("... finished writing Test output.", I_CmsReport.C_FORMAT_HEADLINE);
    }
}