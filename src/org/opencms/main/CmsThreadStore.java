/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsThreadStore.java,v $
 * Date   : $Date: 2003/09/05 16:05:23 $
 * Version: $Revision: 1.1 $
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

package org.opencms.main;

import org.opencms.report.A_CmsReportThread;

import com.opencms.flex.util.CmsUUID;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The OpenCms Thread store where all system Threads are maintained.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $ 
 * @since 5.1.10
 */
public class CmsThreadStore extends Thread {
    
    /** Debug flag */
    private static final boolean DEBUG = false;
    
    /** A map to store all system Thread in */
    private Map m_threads;
    
    /**
     * Hides the public constructor.<p>
     */
    protected CmsThreadStore() {
        super(new ThreadGroup("OpenCms Thread Store"), "OpenCms: Grim Reaper");
        setDaemon(true);
        m_threads = Collections.synchronizedMap(new HashMap());
        start();
    }

    /**
     * Adds a Thread to this Thread store.<p>
     * 
     * @param thread the Thread to add
     */
    public void addThread(A_CmsReportThread thread) {
        m_threads.put(thread.getId(), thread);
        if (DEBUG) {
            dumpThreads();
        }
    }
    
    /**
     * Method to dump all currently known Threads.<p> 
     */
    private void dumpThreads() {
        System.err.println("\n[CmsThreadStore] size: " + m_threads.size());
        Iterator i = m_threads.keySet().iterator();
        while (i.hasNext()) {
            CmsUUID key = (CmsUUID)i.next();
            A_CmsReportThread thread = (A_CmsReportThread)m_threads.get(key);            
            System.err.println(thread.getName());
        }
    }
    
    /**
     * Retrieves a Thread from this Thread store.<p>
     * 
     * @param key the key of the Thread to retrieve
     * @return the Thread form this Thread store that matches the given key
     */
    public A_CmsReportThread retrieveThread(CmsUUID key) {
        if (DEBUG) {
            dumpThreads();
        }     
        return (A_CmsReportThread)m_threads.get(key);   
    }
    
    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        while (true) {
            // the Grim Reaper is eternal, of course
            try {
                // one minute sleep time
                sleep(60000);
            } catch (InterruptedException e) {
                // let's go on reaping...
            }
            try {
                Iterator i;
                i = m_threads.keySet().iterator();
                Set doomed = new HashSet(); 
                // fisrt collect all doomed Threads
                while (i.hasNext()) {
                    CmsUUID key = (CmsUUID)i.next();
                    A_CmsReportThread thread = (A_CmsReportThread)m_threads.get(key);            
                    if (thread.isDoomed()) {
                        doomed.add(key);
                        if (DEBUG) {
                            System.err.println("[CmsThreadStore] Grim Reaper dooming: " + thread.getName());
                        }
                    }
                } 
                i = doomed.iterator();
                // no remove all doomed Threads from the Thread store
                while (i.hasNext()) {
                    m_threads.remove(i.next());
                }              
                if (DEBUG) {           
                    dumpThreads();
                }
            } catch (Throwable t) {
                // the Grim Reaper must not be stopped by any error 
                if (DEBUG) {
                    System.err.println("[CmsThreadStore] Grim Reaper exception " + t);
                }
            }
        }
    }
}
