/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishHistory.java,v $
 * Date   : $Date: 2011/03/23 14:53:16 $
 * Version: $Revision: 1.10 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.collections.buffer.TypedBuffer;
import org.apache.commons.logging.Log;

/**
 * List of already finished publish jobs.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.10 $
 * 
 * @since 6.5.5
 */
public class CmsPublishHistory {

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsPublishHistory.class);

    /** The publish engine. */
    protected final CmsPublishEngine m_publishEngine;

    /**
     * Default constructor.<p>
     * 
     * @param publishEngine the publish engine instance
     */
    protected CmsPublishHistory(final CmsPublishEngine publishEngine) {

        m_publishEngine = publishEngine;
    }

    /**
     * Returns (and initializes) the queue.<p>
     * 
     * @param size the history size
     * 
     * @return the queue buffer
     */
    public static Buffer getQueue(int size) {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_PUBLISH_HISTORY_SIZE_SET_1, new Integer(size)));
        }

        return BufferUtils.synchronizedBuffer(TypedBuffer.decorate(new CircularFifoBuffer(size) {

            /** The serialization version id constant. */
            private static final long serialVersionUID = -6257542123241183114L;

            /**
             * Called when the queue is full to remove the oldest element.<p>
             * 
             * @see org.apache.commons.collections.buffer.BoundedFifoBuffer#remove()
             */
            @Override
            public Object remove() {

                CmsPublishJobInfoBean publishJob = (CmsPublishJobInfoBean)super.remove();
                try {
                    OpenCms.getPublishManager().getEngine().getPublishHistory().remove(publishJob);
                } catch (CmsException exc) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error(exc.getLocalizedMessage(), exc);
                    }
                }
                return publishJob;
            }
        }, CmsPublishJobInfoBean.class));
    }

    /**
     * Adds the given publish job to the list.<p>
     * 
     * @param publishJob the publish job object to add
     * 
     * @throws CmsException if something goes wrong
     */
    protected void add(CmsPublishJobInfoBean publishJob) throws CmsException {

        OpenCms.getMemoryMonitor().cachePublishJobInHistory(publishJob);
        // write job to db if necessary
        if (OpenCms.getMemoryMonitor().requiresPersistency()) {
            CmsDbContext dbc = m_publishEngine.getDbContextFactory().getDbContext();
            try {
                m_publishEngine.getDriverManager().writePublishJob(dbc, publishJob);
                // additionally, write the publish report
                m_publishEngine.getDriverManager().writePublishReport(dbc, publishJob);
                // delete publish list of started job
                m_publishEngine.getDriverManager().deletePublishList(dbc, publishJob.getPublishHistoryId());
            } finally {
                dbc.clear();
            }
        }
    }

    /**
     * Returns an unmodifiable list representation of this list.<p>
     * 
     * @return a list of {@link CmsPublishJobFinished} objects
     */
    protected List asList() {

        List cachedPublishJobs = OpenCms.getMemoryMonitor().getAllCachedPublishJobsInHistory();
        List result = new ArrayList(cachedPublishJobs.size());
        Iterator it = cachedPublishJobs.iterator();
        while (it.hasNext()) {
            CmsPublishJobInfoBean publishJob = (CmsPublishJobInfoBean)it.next();
            result.add(new CmsPublishJobFinished(publishJob));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Initializes the internal FIFO queue with publish jobs from the database.<p> 
     */
    protected void initialize() {

        CmsDriverManager driverManager = m_publishEngine.getDriverManager();
        CmsDbContext dbc = m_publishEngine.getDbContextFactory().getDbContext();

        try {
            OpenCms.getMemoryMonitor().flushCache(CmsMemoryMonitor.CacheType.PUBLISH_HISTORY);
            // read all finished published jobs from the database
            List publishJobs = driverManager.readPublishJobs(dbc, 1L, Long.MAX_VALUE);
            for (Iterator i = publishJobs.iterator(); i.hasNext();) {
                CmsPublishJobInfoBean job = (CmsPublishJobInfoBean)i.next();
                OpenCms.getMemoryMonitor().cachePublishJobInHistory(job);
            }
        } catch (CmsException exc) {
            if (LOG.isErrorEnabled()) {
                LOG.error(exc.getLocalizedMessage(), exc);
            }
        } finally {
            dbc.clear();
        }
    }

    /**
     * Removes the given job from the list.<p>
     * 
     * @param publishJob the publish job to remove
     * 
     * @throws CmsException if something goes wrong
     */
    protected void remove(CmsPublishJobInfoBean publishJob) throws CmsException {

        OpenCms.getMemoryMonitor().uncachePublishJobInHistory(publishJob);
        // delete job from db if necessary
        if (OpenCms.getMemoryMonitor().requiresPersistency()) {
            CmsDbContext dbc = m_publishEngine.getDbContextFactory().getDbContext();
            try {
                OpenCms.getPublishManager().getEngine().getDriverManager().deletePublishJob(
                    dbc,
                    publishJob.getPublishHistoryId());
            } finally {
                dbc.clear();
            }
        }

        m_publishEngine.publishJobRemoved(publishJob);
    }
}
