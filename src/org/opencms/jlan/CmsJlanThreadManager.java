/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.jlan;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.File;

import org.apache.commons.logging.Log;

import org.alfresco.jlan.app.JLANServer;

/**
 * A simple class used to start and stop JLAN.<p>
 *
 * Since the JLAN server requires its own thread and cannot be run in the same thread as the startup,
 * this creates a new thread solely for starting JLAN.<p>
 */
public class CmsJlanThreadManager {

    /**
     * The thread for starting the JLAN server.<p>
     */
    protected class JlanThread extends Thread {

        /** Path of the jlan config file. */
        private String m_configPath;

        /**
         * Constructor.<p>
         *
         * @param configPath the path of the JLAN config file
         */
        public JlanThread(String configPath) {

            m_configPath = configPath;
        }

        /**
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {

            CmsJlanServer server = new CmsJlanServer();

            // we don't want to interactively shut down the server!
            JLANServer.setAllowConsoleShutdown(false);
            server.start(new String[] {m_configPath});

        }
    }

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJlanThreadManager.class);

    /** The maximum amount of time OpenCms should wait during shutdown after trying to stop the JLAN server. */
    private static final int MAX_SHUTDOWN_WAIT_MILLIS = 30000;

    /** The JLAN thread instance. */
    private Thread m_thread;

    /**
     * Starts the JLAN server in a new thread.<p>
     */
    public synchronized void start() {

        String path = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("config/jlanConfig.xml");
        File configFile = new File(path);
        if (configFile.exists()) {

            if (m_thread == null) {
                m_thread = new JlanThread(path);
                m_thread.start();
            }
        } else {
            String message = "Not starting JLAN server because no config file was found at " + path;
            System.out.println(message);
            LOG.warn(message);
        }
    }

    /**
     * Tries to stop the JLAN server and return after it is stopped, but will also return if the thread hasn't stopped after MAX_SHUTDOWN_WAIT_MILLIS.
     */
    public synchronized void stop() {

        if (m_thread != null) {
            long timeBeforeShutdownWasCalled = System.currentTimeMillis();
            JLANServer.shutdownServer(new String[] {});
            while (m_thread.isAlive()
                && ((System.currentTimeMillis() - timeBeforeShutdownWasCalled) < MAX_SHUTDOWN_WAIT_MILLIS)) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }

    }

}
