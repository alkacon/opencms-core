
/*
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.boot;

import java.util.*;
import java.io.*;

/**
 * Thread which first redirects System.out and System.err
 * to a new outputstream. The CmsSetupLoggingThread gets the outputstream
 * and gets started. The import of the opencms workplace is started so the
 * Logging Thread can collect all the output. After the work is done,
 * System.out and System.err are restored.
 *
 * @author Magnus Meurer
 */
public class CmsSetupThread extends Thread {

    /** Logging Thread */
    private CmsSetupLoggingThread m_lt;

    /** Path to opencms home folder */
    private String basePath;

    /** System.out and System.err are redirected to this stream */
    private PipedOutputStream m_pipedOut;

    /** Gets the System.out stream so it can be restored */
    private PrintStream m_tempOut;

    /** Gets the System.err stream so it can be restored */
    public static PrintStream m_tempErr;

    /** Constructor */
    public CmsSetupThread() {
        /** init stream and logging thread */
        m_pipedOut = new PipedOutputStream();
        m_lt = new CmsSetupLoggingThread(m_pipedOut);
    }

    public void run() {

        /* save the original out and err stream */
        m_tempOut = System.out;
        m_tempErr = System.err;

        /* redirect the streams */
        System.setOut(new PrintStream(m_pipedOut));
        System.setErr(new PrintStream(m_pipedOut));

        /* start the logging thread */
        m_lt.start();

        /* start importing the workplace */
        CmsMain.startSetup(basePath + "WEB-INF/ocsetup/cmssetup.txt", basePath + "WEB-INF/");

        /* stop the logging thread */
        try {
            sleep(1000);
            m_lt.stopThread();
            m_pipedOut.close();
        }
        catch (InterruptedException e)  {
            m_lt.stopThread();
            e.printStackTrace(m_tempErr);
        }
        catch (IOException e)  {
            m_lt.stopThread();
            e.printStackTrace(m_tempErr);
        }

        /* restore to the old streams */
        System.setOut(m_tempOut);
        System.setErr(m_tempErr);

    }


    /** Set the base path to the given value */
    public void setBasePath(String basePath)  {
        this.basePath = basePath;
    }

    /** Returns the status of the logging thread */
    public boolean finished() {
        return m_lt.getStopThread();
    }

    /** stop logging thread */
    public void stopLoggingThread() {
        m_lt.stopThread();
    }

    /** Cleans up */
    public void reset() {
        m_lt.reset();
    }
}