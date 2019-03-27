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

package org.opencms.setup.db;

import org.opencms.setup.CmsUpdateBean;
import org.opencms.ui.report.CmsStreamReportWidget;

import java.io.PrintStream;

public class CmsVaadinUpdateDBThread extends Thread {

    /** Saves the System.err stream so it can be restored. */
    public PrintStream m_tempErr;

    /** System.out and System.err are redirected to this stream. */
    private PrintStream m_out;

    /** The additional shell commands, i.e. the update bean. */
    private CmsUpdateBean m_updateBean;

    /** Saves the System.out stream so it can be restored. */
    private PrintStream m_tempOut;

    private CmsStreamReportWidget m_reportWidget;

    /**
     * Constructor.<p>
     *
     * @param updateBean the initialized update bean
     */
    public CmsVaadinUpdateDBThread(CmsUpdateBean updateBean, CmsStreamReportWidget reportWidget) {

        super("OpenCms: Database Update");

        // store setup bean
        m_updateBean = updateBean;
        // init stream and logging thread
        m_out = reportWidget.getStream();
        m_reportWidget = reportWidget;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        // save the original out and err stream
        m_tempOut = System.out;
        m_tempErr = System.err;
        try {
            // redirect the streams
            System.setOut(m_out);
            System.setErr(m_out);

            System.out.println("Starting DB Update... ");

            CmsUpdateDBManager dbMan = new CmsUpdateDBManager();
            try {
                dbMan.initialize(m_updateBean);
                dbMan.run();
            } catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println("... DB Update finished.");
        } finally {
            // restore to the old streams
            System.setOut(m_tempOut);
            System.setErr(m_tempErr);
            m_reportWidget.finish();
        }
    }

}
