/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/update6to7/Attic/CmsUpdateDBThread.java,v $
 * Date   : $Date: 2007/05/24 19:15:39 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.setup.update6to7;

import org.opencms.main.CmsLog;
import org.opencms.main.CmsSystemInfo;
import org.opencms.setup.CmsSetupBean;
import org.opencms.setup.CmsSetupLoggingThread;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.io.PrintStream;

/**
 * Used for the workplace setup in the OpenCms setup wizard.<p>
 *
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.1.2.1 $ 
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

    /** The additional shell commands, i.e. the setup bean. */
    private CmsSetupBean m_setupBean;

    /** Saves the System.out stream so it can be restored. */
    private PrintStream m_tempOut;

    /** 
     * Constructor.<p>
     * 
     * @param setupBean the initialized setup bean
     */
    public CmsUpdateDBThread(CmsSetupBean setupBean) {

        super("OpenCms: Database Update");

        // store setup bean
        m_setupBean = setupBean;
        // init stream and logging thread
        m_pipedOut = new PipedOutputStream();
        m_loggingThread = new CmsSetupLoggingThread(m_pipedOut, m_setupBean.getWebAppRfsPath()
            + CmsSystemInfo.FOLDER_WEBINF
            + CmsLog.FOLDER_LOGS
            + "db-update.log");
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
        m_setupBean = null;
    }

    /**
     * @see java.lang.Runnable#run()
     */
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
                dbMan.initialize(m_setupBean);
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