/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishHistory.java,v $
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

import org.opencms.main.CmsLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.apache.commons.collections.buffer.TypedBuffer;

/**
 * List of already finished publish jobs.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.5.5
 */
final class CmsPublishHistory {

    /** The internal FIFO queue. */
    private final Buffer m_queue;

    /**
     * Default constructor.<p>
     * 
     * @param publishEngine the publish engine instance
     * @param size the maximal number of entries to store
     */
    protected CmsPublishHistory(final CmsPublishEngine publishEngine, int size) {

        m_queue = BufferUtils.synchronizedBuffer(TypedBuffer.decorate(new CircularFifoBuffer(size) {

            /** The serialization version id constant. */
            private static final long serialVersionUID = -6257542123241183114L;

            /**
             * Called when the queue is full to remove the oldest element.<p>
             * 
             * @see org.apache.commons.collections.buffer.BoundedFifoBuffer#remove()
             */
            public Object remove() {

                CmsPublishJobFinished publishJob = (CmsPublishJobFinished)super.remove();
                publishEngine.removeJob(publishJob);
                return publishJob;
            }
        }, CmsPublishJobFinished.class));

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_PUBLISH_HISTORY_SIZE_SET_1, new Integer(size)));
        }
    }

    /**
     * Adds the given publish job to the list.<p>
     * 
     * @param publishJob the publish job object to add
     */
    protected void add(CmsPublishJobFinished publishJob) {

        m_queue.add(publishJob);
    }

    /**
     * Returns an unmodifiable list representation of this list.<p>
     * 
     * @return a list of {@link CmsPublishJobFinished} objects
     */
    protected List asList() {

        return Collections.unmodifiableList(new ArrayList(m_queue));
    }

    /**
     * Removes all publish jobs from the history.<p>
     */
    protected void clear() {

        while (!m_queue.isEmpty()) {
            m_queue.remove();
        }
    }

    /**
     * 
     */
    public void readFromRepository() {

        // TODO Auto-generated method stub
        
    }

    /**
     * 
     */
    public void writeToRepository() {

        // TODO Auto-generated method stub
        
    }
}
