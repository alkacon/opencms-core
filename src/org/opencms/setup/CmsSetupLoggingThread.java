/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/setup/Attic/CmsSetupLoggingThread.java,v $
 * Date   : $Date: 2004/02/22 14:14:28 $
 * Version: $Revision: 1.4 $
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

import java.util.*;
import java.io.*;

/**
 * Logging Thread which collects the output from CmsSetupThread and
 * stores it in a Vector that the OpenCms setup wizard can read via
 * the getMessages() method.<p>
 *
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.4 $
 */
public class CmsSetupLoggingThread extends Thread {
    private static Vector messages;
    private PipedInputStream m_pipedIn;
    private LineNumberReader m_lineReader;
    private boolean m_stopThread;
    private PipedOutputStream m_pipedOut;

    /** 
     * Constructor.<p>
     * 
     * @param pipedOut the output stream to write to 
     */
    public CmsSetupLoggingThread(PipedOutputStream pipedOut) {

        super("OpenCms: Setup logging");

        m_pipedOut = pipedOut;
        messages = new Vector();
        m_stopThread = false;

        try {
            m_pipedIn = new PipedInputStream();
            m_pipedIn.connect(m_pipedOut);
            m_lineReader = new LineNumberReader(new BufferedReader(new InputStreamReader(m_pipedIn)));
        } catch (Exception e) {
            messages.addElement(e.toString());
        }    
    }
    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        int lineNr = 0;
        int lastLineNr = -1;
        String line = null;
        while (!m_stopThread) {
            lineNr = m_lineReader.getLineNumber();
            try {
                line = m_lineReader.readLine();
            } catch (IOException e) {
                // "Write end dead" IO exceptions can be ignored                
            }
            if (line != null) {
                if (lineNr > lastLineNr) {
                    // supress multiple output of the same line after "Write end dead" IO exception      
                    messages.addElement(lineNr + ":\t" + line);
                    lastLineNr = lineNr;
                }
            }          
        }
        try {
            m_pipedIn.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** 
     * Returns a Vector with the last collected log messages.<p>
     * 
     * @return a Vector with the last collected log messages
     */
    public static Vector getMessages() {
        return messages;
    }

    /** 
     * Used to break the loop in the run() method.<p> 
     */
    public void stopThread() {
        m_stopThread = true;
    }

    /** 
     * Indicates if the Thread has been stopped.<p>
     * 
     * @return true if the Thread is stopped 
     */
    public boolean getStopThread() {
        return m_stopThread;
    }

    /**
     * Cleans up.<p> 
     */
    public void reset() {
        messages.clear();
        m_stopThread = false;
    }

}