/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishQueue.java,v $
 * Date   : $Date: 2006/11/29 15:04:09 $
 * Version: $Revision: 1.1.2.1 $
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.TypedBuffer;
import org.apache.commons.collections.buffer.UnboundedFifoBuffer;

/**
 * This queue contains all not jet started publish jobs.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.5.5
 */
final class CmsPublishQueue {

    /** Internal queue implementation. */
    private final Buffer m_queue;

    /**
     * Default constructor, for an empty queue.<p>
     */
    protected CmsPublishQueue() {

        m_queue = BufferUtils.synchronizedBuffer(TypedBuffer.decorate(
            new UnboundedFifoBuffer(),
            CmsPublishJobInfoBean.class));
    }

    /**
     * Aborts the given publish job.<p>
     * 
     * @param publishJob the publish job to abort
     * 
     * @return if successful or not 
     */
    protected boolean abortPublishJob(CmsPublishJobInfoBean publishJob) {

        return m_queue.remove(publishJob);
    }

    /**
     * Pushes a new publish job with the given information in publish queue.<p>
     * 
     * If possible, the publish job starts inmediatly.<p>
     * 
     * @param publishJob the publish job to enqueue
     */
    protected void add(CmsPublishJobInfoBean publishJob) {

        m_queue.add(publishJob);
        publishJob.enqueue();
    }

    /**
     * Returns an unmodifiable list representation of this queue.<p>
     * 
     * @return a list of {@link CmsPublishJobEnqueued} objects
     */
    protected List asList() {

        List result = new ArrayList(m_queue.size());
        Iterator it = m_queue.iterator();
        while (it.hasNext()) {
            CmsPublishJobInfoBean publishJob = (CmsPublishJobInfoBean)it.next();
            result.add(new CmsPublishJobEnqueued(publishJob));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Checks if the queue is empty.<p>
     * 
     * @return <code>true</code> if the queue is empty
     */
    protected boolean isEmpty() {

        return m_queue.isEmpty();
    }

    /**
     * Returns the next publish job to be published, removing it 
     * from the queue, or <code>null</code> if the queue is empty.<p> 
     * 
     * @return the next publish job to be published
     */
    protected CmsPublishJobInfoBean next() {

        return (CmsPublishJobInfoBean)m_queue.remove();
    }
}
