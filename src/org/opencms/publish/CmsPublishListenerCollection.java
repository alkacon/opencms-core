/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishListenerCollection.java,v $
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

import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;

/**
 * Publish job information bean.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.5.5
 */
public final class CmsPublishListenerCollection extends Vector {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishListenerCollection.class);

    /** serializable version id. */
    private static final long serialVersionUID = -4945973010986412449L;

    /**
     * Fires an abort event to all listeners.<p>
     * 
     * @param userName the name of the user that aborted the job
     * @param publishJob the publish job that is going to be aborted.
     */
    protected void fireAbort(String userName, CmsPublishJobEnqueued publishJob) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_PUBLISH_JOB_ABORT_0));
        }
        for (Iterator it = iterator(); it.hasNext();) {
            I_CmsPublishEventListener listener = (I_CmsPublishEventListener)it.next();
            listener.onAbort(userName, publishJob);
        }
    }

    /**
     * Fires an enqueue event to all listeners.<p>
     * 
     * @param publishJob the publish job that is going to be enqueued.
     */
    protected void fireEnqueued(CmsPublishJobBase publishJob) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_PUBLISH_JOB_ENQUEUE_0));
        }
        for (Iterator it = iterator(); it.hasNext();) {
            I_CmsPublishEventListener listener = (I_CmsPublishEventListener)it.next();
            listener.onEnqueue(publishJob);
        }
    }

    /**
     * Fires a finish event to all listeners.<p>
     * 
     * @param publishJob the publish job that has been finished.
     */
    protected void fireFinish(CmsPublishJobRunning publishJob) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_PUBLISH_JOB_FINISH_0));
        }
        for (Iterator it = iterator(); it.hasNext();) {
            I_CmsPublishEventListener listener = (I_CmsPublishEventListener)it.next();
            listener.onFinish(publishJob);
        }
    }

    /**
     * Fires a remove event to all listeners.<p>
     * 
     * @param publishJob the publish job that is going to be removed.
     */
    protected void fireRemove(CmsPublishJobFinished publishJob) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_PUBLISH_JOB_REMOVE_0));
        }
        for (Iterator it = iterator(); it.hasNext();) {
            I_CmsPublishEventListener listener = (I_CmsPublishEventListener)it.next();
            listener.onRemove(publishJob);
        }
    }

    /**
     * Fires a start event to all listeners.<p>
     * 
     * @param publishJob the publish job that is going to start.
     */
    protected void fireStart(CmsPublishJobEnqueued publishJob) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_PUBLISH_JOB_START_0));
        }
        for (Iterator it = iterator(); it.hasNext();) {
            I_CmsPublishEventListener listener = (I_CmsPublishEventListener)it.next();
            listener.onStart(publishJob);
        }
    }
}
