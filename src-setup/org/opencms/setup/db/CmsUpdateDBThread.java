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

package org.opencms.setup.db;

import org.opencms.main.CmsLog;
import org.opencms.main.CmsSystemInfo;
import org.opencms.setup.CmsSetupLoggingThread;
import org.opencms.setup.CmsUpdateBean;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.io.PrintStream;

/**
 * Used for the workplace setup in the OpenCms setup wizard.<p>
 *
 * @since 6.0.0
 */
public class CmsUpdateDBThread extends Thread {

    /** Saves the System.err stream so it can be restored. */
    public PrintStream m_tempErr;

    /** Logging thread. */
    private CmsSetupLoggingThread m_loggingThread;

    /** System.out and System.err are redirected to this stream. */
    private PipedOutputStream m_pipedOut;

    /** The additional shell commands, i.e. the update bean. */
    private CmsUpdateBean m_updateBean;

    /** Saves the System.out stream so it can be restored. */
    private PrintStream m_tempOut;

    /**
     * Constructor.<p>
     *
     * @param updateBean the initialized update bean
     */
    public CmsUpdateDBThread(CmsUpdateBean updateBean) {

        super("OpenCms: Database Update");

        // store setup bean
        m_updateBean = updateBean;
        // init stream and logging thread
        m_pipedOut = new PipedOutputStream();
        m_loggingThread = new CmsSetupLoggingThread(
            m_pipedOut,
            m_updateBean.getWebAppRfsPath() + CmsSystemInfo.FOLDER_WEBINF + CmsLog.FOLDER_LOGS + "db-update.log");
    }

    /**
     * Returns the logging thread.<p>
     *
     * @return the logging thread
     */
    public CmsSetupLoggingThread getLoggingThread() {

        return m_loggingThread;
    }

    /**
     * Returns the status of the logging thread.<p>
     *
     * @return the status of the logging thread
     */
    public boolean isFinished() {

        return m_loggingThread.isFinished();
    }

    /**
     * Kills this Thread as well as the included logging Thread.<p>
     */
    public void kill() {

        if (m_loggingThread != null) {
            m_loggingThread.stopThread();
        }
        m_updateBean = null;
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
            System.setOut(new PrintStream(m_pipedOut));
            System.setErr(new PrintStream(m_pipedOut));

            // start the logging thread
            m_loggingThread.start();

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
            kill();
            if (m_pipedOut != null) {
                try {
                    m_pipedOut.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            // restore to the old streams
            System.setOut(m_tempOut);
            System.setErr(m_tempErr);
        }
    }
}