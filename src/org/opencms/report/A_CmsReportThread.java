/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/report/A_CmsReportThread.java,v $
 * Date   : $Date: 2003/09/05 16:05:23 $
 * Version: $Revision: 1.2 $
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
 
package org.opencms.report;

import org.opencms.main.OpenCms;

import com.opencms.flex.util.CmsUUID;

/** 
 * Provides a common Thread class for the reports.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com) 
 * 
 * @version $Revision: 1.2 $
 * @since 5.0
 */
public abstract class A_CmsReportThread extends Thread {
    
    /** Indicates if the Thread was already checked by the grim reaper */
    public boolean m_doomed;
    
    /** The id of this report */
    private CmsUUID m_id;
    
    /** The report that belongs to the thread */
    public I_CmsReport m_report;

    /**
     * Constructs a new thread with the given name.<p>
     * 
     * @param name the name of the Thread
     */
    public A_CmsReportThread(String name) {
        super(OpenCms.getThreadStore().getThreadGroup(), name);
        setDaemon(false);
        m_id = new CmsUUID();
        m_doomed = false;
        setName(name + " [" + m_id + "]");
        OpenCms.getThreadStore().addThread(this);
    }
    
    /**
     * Flag to indicate if broken links where found during the Thread opertation.<p>
     * 
     * Not all report Thread implementations need to check for broken links, 
     * the default implementation is to return <code>false</code>,
     * indicating that no broken links where found.<p> 
     * 
     * @return boolean true if broken links where found, false (default) otherwise 
     */
    public boolean brokenLinksFound() {
        return false;
    }
    
    /**
     * Returns the error exception in case there was an error during the execution of
     * this Thread, null otherwise.<p>
     * 
     * @return the error exception in case there was an error, null otherwise
     */
    public Throwable getError() {
        return null;
    }           
    
    /**
     * Returns the id of this report thread.<p>
     * 
     * @return the id of this report thread
     */
    public CmsUUID getId() {
        return m_id;
    }
    
    /**
     * Returns the report where the output of this Thread is written to.<o>
     * 
     * @return the report where the output of this Thread is written to
     */
    public I_CmsReport getReport() {
        return m_report;
    }
    
    /**
     * Returns the part of the report that is ready for output.
     * 
     * @return the part of the report that is ready for output
     */
    public abstract String getReportUpdate();
    
    /**
     * Returns true if this thread is already "doomed" to be deleted.<p>
     * 
     * A OpenCms deamon Thread (the "Grim Reaper") will collect all 
     * doomed Threads, i.e. threads that are not longer active for some
     * time.<p>
     * 
     * @return true if this thread is already "doomed" to be deleted
     */
    public boolean isDoomed() {
        if (isAlive()) {
            // as long as the Thread is still active it is never doomed
            return false;
        }
        if (m_doomed) {
            // not longer active, and already doomed, so rest in peace...
            return true;
        }
        // condemn the Thread to be collected by the grim reaper next time  
        m_doomed = true;
        return false;
    }

    /**
     * Sets the report where the output of this Thread is written to.<p>
     * 
     * @param report the report where the output of this Thread is written to
     */
    public void setReport(I_CmsReport report) {
        m_report = report;
    }
}
