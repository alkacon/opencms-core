/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupThread.java,v $
 * Date   : $Date: 2004/02/14 00:22:01 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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
package org.opencms.setup;

import org.opencms.main.CmsShell;

import java.io.File;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.io.PrintStream;

/**
 * Used for the workplace setup in the OpenCms setup wizard.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.3 $
 */
public class CmsSetupThread extends Thread {

    /** Gets the System.err stream so it can be restored */
    public static PrintStream m_tempErr;

    /** Path to opencms home folder */
    private String m_basePath;

    /** Logging Thread */
    private CmsSetupLoggingThread m_loggingThread;

    /** System.out and System.err are redirected to this stream */
    private PipedOutputStream m_pipedOut;

    /** Gets the System.out stream so it can be restored */
    private PrintStream m_tempOut;

    /** 
     * Constructor.<p>
     */
    public CmsSetupThread() {
        
        super("OpenCms: Setup");
        
        // init stream and logging thread
        m_pipedOut = new PipedOutputStream();
        m_loggingThread = new CmsSetupLoggingThread(m_pipedOut);
    }

    /** 
     * Returns the status of the logging thread.<p>
     * 
     * @return the status of the logging thread 
     */
    public boolean finished() {
        return m_loggingThread.getStopThread();
    }

    /** 
     * Cleans up the logging thread.<p> 
     */
    public void reset() {
        m_loggingThread.reset();
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
    
            // start importing the workplace
            CmsShell.startSetup(m_basePath + "WEB-INF/", m_basePath + CmsSetupDb.C_SETUP_DATA_FOLDER + "cmssetup.txt");
    
            // stop the logging thread
            try {
                sleep(1000);
                m_loggingThread.stopThread();
                m_pipedOut.close();
            } catch (InterruptedException e)  {
                m_loggingThread.stopThread();
                e.printStackTrace(m_tempErr);
            } catch (IOException e)  {
                m_loggingThread.stopThread();
                e.printStackTrace(m_tempErr);
            }
        } finally {
            // restore to the old streams
            System.setOut(m_tempOut);
            System.setErr(m_tempErr);
        }
    }


    /** 
     * Set the OpenCms application base path for the setup.<p>
     * 
     * @param basePath the path to set
     */
    public void setBasePath(String basePath)  {
        this.m_basePath = basePath;
        if (! basePath.endsWith(File.separator)) {
            this.m_basePath += File.separator;
        }
    }

    /** 
     * Stops the logging thread.<p>
     */
    public void stopLoggingThread() {
        m_loggingThread.stopThread();
    }
}