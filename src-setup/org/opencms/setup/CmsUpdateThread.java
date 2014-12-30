/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.setup;

import org.opencms.main.CmsLog;
import org.opencms.main.CmsShell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PipedOutputStream;
import java.io.PrintStream;

/**
 * Used for the OpenCms workplace update wizard.<p>
 * 
 * @since 6.0.0 
 */
public class CmsUpdateThread extends Thread {

    /** Saves the System.err stream so it can be restored. */
    public PrintStream m_tempErr;

    /** Logging thread. */
    private CmsSetupLoggingThread m_loggingThread;

    /** System.out and System.err are redirected to this stream. */
    private PipedOutputStream m_pipedOut;

    /** The cms shell to import the workplace with. */
    private CmsShell m_shell;

    /** Saves the System.out stream so it can be restored. */
    private PrintStream m_tempOut;

    /** The additional shell commands, i.e. the setup bean. */
    private CmsUpdateBean m_updateBean;

    /** 
     * Constructor.<p>
     * 
     * @param updateBean the initialized update bean
     */
    public CmsUpdateThread(CmsUpdateBean updateBean) {

        super("OpenCms: Workplace update");

        // store setup bean
        m_updateBean = updateBean;
        // init stream and logging thread
        m_pipedOut = new PipedOutputStream();
        m_loggingThread = new CmsSetupLoggingThread(m_pipedOut, m_updateBean.getLogName());
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

        if (m_shell != null) {
            m_shell.exit();
        }
        if (m_loggingThread != null) {
            m_loggingThread.stopThread();
        }
        m_shell = null;
        m_updateBean = null;
    }

    /**
     * Write somthing to System.out during setup.<p>
     * 
     * @param str the string to write
     */
    public void printToStdOut(String str) {

        m_tempOut.println(str);
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

            // create a shell that will start importing the workplace
            m_shell = new CmsShell(
                m_updateBean.getWebAppRfsPath() + "WEB-INF" + File.separator,
                m_updateBean.getServletMapping(),
                m_updateBean.getDefaultWebApplication(),
                "${user}@${project}>",
                m_updateBean);

            try {
                try {
                    if (CmsLog.INIT.isInfoEnabled()) {
                        // log welcome message, the full package name is required because
                        // two different Message classes are used
                        CmsLog.INIT.info(org.opencms.main.Messages.get().getBundle().key(
                            org.opencms.main.Messages.INIT_DOT_0));
                        CmsLog.INIT.info(org.opencms.main.Messages.get().getBundle().key(
                            org.opencms.main.Messages.INIT_DOT_0));
                        CmsLog.INIT.info(org.opencms.main.Messages.get().getBundle().key(
                            org.opencms.main.Messages.INIT_DOT_0));
                        CmsLog.INIT.info(org.opencms.setup.Messages.get().getBundle().key(
                            org.opencms.setup.Messages.INIT_WELCOME_UPDATE_0));
                        CmsLog.INIT.info(org.opencms.setup.Messages.get().getBundle().key(
                            org.opencms.setup.Messages.INIT_UPDATE_WORKPLACE_START_0));
                        CmsLog.INIT.info(org.opencms.main.Messages.get().getBundle().key(
                            org.opencms.main.Messages.INIT_DOT_0));
                        CmsLog.INIT.info(org.opencms.main.Messages.get().getBundle().key(
                            org.opencms.main.Messages.INIT_DOT_0));
                        for (int i = 0; i < org.opencms.main.Messages.COPYRIGHT_BY_ALKACON.length; i++) {
                            CmsLog.INIT.info(". " + org.opencms.main.Messages.COPYRIGHT_BY_ALKACON[i]);
                        }
                        CmsLog.INIT.info(org.opencms.main.Messages.get().getBundle().key(
                            org.opencms.main.Messages.INIT_DOT_0));
                        CmsLog.INIT.info(org.opencms.main.Messages.get().getBundle().key(
                            org.opencms.main.Messages.INIT_DOT_0));
                        CmsLog.INIT.info(org.opencms.main.Messages.get().getBundle().key(
                            org.opencms.main.Messages.INIT_LINE_0));

                    }
                    m_shell.execute(new FileInputStream(new File(m_updateBean.getWebAppRfsPath()
                        + CmsUpdateBean.FOLDER_UPDATE
                        + "cmsupdate.txt")));
                    if (CmsLog.INIT.isInfoEnabled()) {
                        CmsLog.INIT.info(org.opencms.setup.Messages.get().getBundle().key(
                            org.opencms.setup.Messages.INIT_UPDATE_WORKPLACE_FINISHED_0));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                // stop the logging thread
                kill();
                m_pipedOut.close();
            } catch (Throwable t) {
                // ignore
            }
        } finally {
            // restore to the old streams
            System.setOut(m_tempOut);
            System.setErr(m_tempErr);
        }
    }
}