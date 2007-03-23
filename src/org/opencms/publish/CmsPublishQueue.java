/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishQueue.java,v $
 * Date   : $Date: 2007/03/23 16:52:33 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2006 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.publish;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.TypedBuffer;
import org.apache.commons.collections.buffer.UnboundedFifoBuffer;
import org.apache.commons.logging.Log;

/**
 * This queue contains all not jet started publish jobs.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.5.5
 */
public class CmsPublishQueue {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishHistory.class);
    
    /** The publish engine. */
    protected final CmsPublishEngine m_publishEngine;

    /**
     * Default constructor, for an empty queue.<p>
     * 
     * @param publishEngine the publish engine instance
     */
    protected CmsPublishQueue(final CmsPublishEngine publishEngine) {

        m_publishEngine = publishEngine;
    }

    /**
     * Creates the buffer used as publish queue.<p>
     * 
     * @return the queue buffer
     */
    public static Buffer getQueue() {
        
        return BufferUtils.synchronizedBuffer(TypedBuffer.decorate(new UnboundedFifoBuffer() {
                
                /** The serialization version id constant. */
                private static final long serialVersionUID = 606444342980861724L;
                
                /**
                 * Called when the queue is full to remove the oldest element.<p>
                 * 
                 * @see org.apache.commons.collections.buffer.BoundedFifoBuffer#remove()
                 */
                public Object remove() {

                    CmsPublishJobInfoBean publishJob = (CmsPublishJobInfoBean)super.remove();
                    return publishJob;
                }
            }, CmsPublishJobInfoBean.class));
    }
    
    /**
     * Aborts the given publish job.<p>
     * 
     * @param publishJob the publish job to abort
     * 
     * @return <code>true</code> if the publish job was found
     */
    protected boolean abortPublishJob(CmsPublishJobInfoBean publishJob) {

        if (OpenCms.getMemoryMonitor().getCachedPublishJob(publishJob.getPublishHistoryId().toString()) != null) {
            // remove publish job from cache
            OpenCms.getMemoryMonitor().uncachePublishJob(publishJob);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Pushes a new publish job with the given information in publish queue.<p>
     * 
     * If possible, the publish job starts inmediatly.<p>
     * 
     * @param publishJob the publish job to enqueue
     * 
     * @throws CmsException if something goes wrong
     */
    protected void add(CmsPublishJobInfoBean publishJob) 
    throws CmsException {

        publishJob.enqueue();
        
        // add publish job to cache
        OpenCms.getMemoryMonitor().cachePublishJob(publishJob);
        
        // add job to database if neccessary
        if (OpenCms.getMemoryMonitor().requiresPersistency()) {
            CmsDbContext dbc = m_publishEngine.getDbContextFactory().getDbContext();
            m_publishEngine.getDriverManager().createPublishJob(dbc, publishJob);
        }     
    }
    
    /**
     * Removes the given job from the list.<p>
     * 
     * @param publishJob the publish job to remove
     * 
     * @throws CmsException if something goes wrong
     */
    protected void remove(CmsPublishJobInfoBean publishJob) 
    throws CmsException {

        // signalize that job will be removed
        m_publishEngine.publishJobRemoved(publishJob);
        
        // remove publish job from cache
        OpenCms.getMemoryMonitor().uncachePublishJob(publishJob);
        
        // remove job from database if neccessary
        if (OpenCms.getMemoryMonitor().requiresPersistency()) {
            CmsDbContext dbc = m_publishEngine.getDbContextFactory().getDbContext();
            m_publishEngine.getDriverManager().deletePublishJob(dbc, publishJob.getPublishHistoryId());
        }
        // delete report
        if (!new File(publishJob.getReportFilePath()).delete()) {
            // warn if deletion failed
            if (LOG.isWarnEnabled()) {
                LOG.warn(Messages.get().getBundle().key(
                    Messages.LOG_PUBLISH_REPORT_DELETE_FAILED_1,
                    publishJob.getReportFilePath()));
            }
        }
    }
    
    /**
     * Updates the given job in the list.<p>
     * 
     * @param publishJob the publish job to 
     * 
     * @throws CmsException if something goes wrong
     */
    protected void update(CmsPublishJobInfoBean publishJob)
    throws CmsException {
        
        if (OpenCms.getMemoryMonitor().requiresPersistency()) {
            CmsDbContext dbc = m_publishEngine.getDbContextFactory().getDbContext();
            m_publishEngine.getDriverManager().writePublishJob(dbc, publishJob);
            // delete publish list of started job
            m_publishEngine.getDriverManager().deletePublishList(dbc, publishJob.getPublishHistoryId());
        }
    }    
        

    /**
     * Returns an unmodifiable list representation of this queue.<p>
     * 
     * @return a list of {@link CmsPublishJobEnqueued} objects
     */
    protected List asList() {

        List cachedPublishJobs = OpenCms.getMemoryMonitor().getAllCachedPublishJobs();
        List result = new ArrayList(cachedPublishJobs.size());
        Iterator it = cachedPublishJobs.iterator();
        while (it.hasNext()) {
            CmsPublishJobInfoBean publishJob = (CmsPublishJobInfoBean)it.next();
            result.add(new CmsPublishJobEnqueued(publishJob));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Initializes the internal FIFO queue with publish jobs from the database.<p>
     * 
     * @param adminCms an admin cms object
     */
    protected void initialize(CmsObject adminCms) {

        CmsDriverManager driverManager = m_publishEngine.getDriverManager();
        CmsDbContext dbc = m_publishEngine.getDbContextFactory().getDbContext();
        
        try {
            OpenCms.getMemoryMonitor().flushPublishJobs();
            // read all pending publish jobs from the database
            List publishJobs = driverManager.readPublishJobs(dbc, 0L, 0L);
            for (Iterator i = publishJobs.iterator(); i.hasNext();) {
                CmsPublishJobInfoBean job = (CmsPublishJobInfoBean)i.next();
                if (!job.isStarted()) {
                    // add jobs not already started to queue again
                    try {
                        job.revive(adminCms, driverManager.readPublishList(dbc, job.getPublishHistoryId()));
                        m_publishEngine.lockPublishList(job);
                        OpenCms.getMemoryMonitor().cachePublishJob(job);
                    } catch (CmsException exc) {
                        // skip job
                        LOG.error(exc);
                    }
                } else {
                    try {
                        // remove already started job
                        // m_publishEngine.abortPublishJob(null, new CmsPublishJobEnqueued(job), false);
                        // set finish info
                        new CmsPublishJobEnqueued(job).m_publishJob.finish();
                        m_publishEngine.getPublishHistory().add(job);
                    } catch (CmsException exc) {
                        LOG.error(exc);
                    }
                }
            }
        } catch (CmsException exc) {
            if (LOG.isErrorEnabled()) {
                LOG.error(exc);
            }
        }
    }
    
    /**
     * Checks if the queue is empty.<p>
     * 
     * @return <code>true</code> if the queue is empty
     */
    protected boolean isEmpty() {

        return (OpenCms.getMemoryMonitor().getFirstCachedPublishJob() == null);
    }

    /**
     * Returns the next publish job to be published, removing it 
     * from the queue, or <code>null</code> if the queue is empty.<p> 
     * 
     * @return the next publish job to be published
     */
    protected CmsPublishJobInfoBean next() {

        CmsPublishJobInfoBean publishJob = OpenCms.getMemoryMonitor().getFirstCachedPublishJob();
        if (publishJob != null) {
            OpenCms.getMemoryMonitor().uncachePublishJob(publishJob);
        }
        return publishJob;
    }
}
