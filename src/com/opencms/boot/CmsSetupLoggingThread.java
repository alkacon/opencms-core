
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
 * Logging Thread which collects the output from CmsSetupThread,
 * stores it in a vector which the OpenCms setup wizard can read via
 * the getMessages() method.
 *
 * @author Magnus Meurer
 */
public class CmsSetupLoggingThread extends Thread  {
    private static Vector messages;
    private PipedInputStream m_pipedIn;
    private LineNumberReader m_LineReader;
    private boolean m_stopThread;

    /** Constructor */
    public CmsSetupLoggingThread(PipedOutputStream pipedOut)  {
        messages = new Vector();
        m_stopThread = false;
        try {
            m_pipedIn = new PipedInputStream();
            m_pipedIn.connect(pipedOut);
            m_LineReader = new LineNumberReader(new BufferedReader(new InputStreamReader(m_pipedIn)));
        }
        catch (Exception e) {
            messages.addElement(e.toString());
        }
    }

    /**
     * Thread's run() method.
     * Gets a line and its number from the LineNumber Reader
     * and stores it in the messages vector.
     */
    public void run() {
        int lineNr = 0;
        String line = null;
        while(!m_stopThread)  {
            try {
                lineNr = m_LineReader.getLineNumber();
                line = m_LineReader.readLine();
            }
            catch (IOException e) {
                messages.addElement(e.toString());
                m_stopThread = true;
            }
            if(line!=null)  {
                messages.addElement(lineNr + ":\t" + line);
            }
        }
        try {
            m_pipedIn.close();
        }
        catch(IOException e)  {
            e.printStackTrace();
        };
    }

    /** Returns a vector containing messages */
    public static Vector getMessages() {
      return messages;
    }

    /** This method breaks the loop in the run() method */
    public void stopThread() {
        m_stopThread = true;
    }

    /** Indicates if the Thread has been stopped */
    public boolean getStopThread()  {
        return m_stopThread;
    }

    /** Cleans uo */
    public void reset() {
        messages.clear();
        m_stopThread = false;
    }

}