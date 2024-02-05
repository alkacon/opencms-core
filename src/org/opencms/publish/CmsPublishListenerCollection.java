/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.main.OpenCms;
import org.opencms.staticexport.CmsAfterPublishStaticExportHandler;
import org.opencms.util.CmsUUID;

import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;

/**
 * Publish job information bean.<p>
 *
 * @since 6.5.5
 */
public final class CmsPublishListenerCollection extends Vector<I_CmsPublishEventListener> {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishListenerCollection.class);

    /** serializable version id. */
    private static final long serialVersionUID = -4945973010986412449L;

    /** Publish engine. */
    private transient CmsPublishEngine m_publishEngine;

    /**
     * Default constructor.<p>
     *
     * @param publishEngine the publish engine
     */
    protected CmsPublishListenerCollection(CmsPublishEngine publishEngine) {

        m_publishEngine = publishEngine;
    }

    /**
     * Fires an abort event to all listeners.<p>
     *
     * @param userId the id of the user that aborted the job
     * @param publishJob the publish job that is going to be aborted.
     */
    protected void fireAbort(CmsUUID userId, CmsPublishJobEnqueued publishJob) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().getBundle().key(Messages.LOG_PUBLISH_JOB_ABORT_0));
        }
        for (Iterator<I_CmsPublishEventListener> it = iterator(); it.hasNext();) {
            I_CmsPublishEventListener listener = it.next();
            try {
                listener.onAbort(userId, publishJob);
            } catch (Throwable t) {
                // catch every thing including runtime exceptions
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.ERR_PUBLISH_JOB_ABORT_ERROR_1,
                            listener.getClass().getName()),
                        t);
                }
                if (publishJob.m_publishJob.getPublishReport() != null) {
                    publishJob.m_publishJob.getPublishReport().println(t);
                }
            }
        }
        if ((userId != null) && userId.equals(publishJob.getUserId())) {
            // prevent showing messages if the owner aborted the job by himself
            return;
        }
        // popup the abort message
        String msgText = Messages.get().getBundle(publishJob.getLocale()).key(
            Messages.GUI_PUBLISH_JOB_ABORTED_2,
            Long.valueOf(publishJob.getEnqueueTime()),
            m_publishEngine.getUser(userId).getName());
        m_publishEngine.sendMessage(publishJob.getUserId(), msgText, true);
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
        for (Iterator<I_CmsPublishEventListener> it = iterator(); it.hasNext();) {
            I_CmsPublishEventListener listener = it.next();
            try {
                listener.onEnqueue(publishJob);
            } catch (Throwable t) {
                // catch every thing including runtime exceptions
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.ERR_PUBLISH_JOB_ENQUEUE_ERROR_1,
                            listener.getClass().getName()),
                        t);
                }
                if (publishJob.m_publishJob.getPublishReport() != null) {
                    publishJob.m_publishJob.getPublishReport().println(t);
                }
            }
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
        for (Iterator<I_CmsPublishEventListener> it = iterator(); it.hasNext();) {
            I_CmsPublishEventListener listener = it.next();
            try {
                listener.onFinish(publishJob);
            } catch (Throwable t) {
                // catch every thing including runtime exceptions
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.ERR_PUBLISH_JOB_FINISH_ERROR_1,
                            listener.getClass().getName()),
                        t);
                }
                if (publishJob.m_publishJob.getPublishReport() != null) {
                    publishJob.m_publishJob.getPublishReport().println(t);
                }
            }
        }
        // popup the finish message
        String msgText;
        boolean hasError = false;
        if (!publishJob.getReport().hasError() && !publishJob.getReport().hasWarning()) {
            msgText = Messages.get().getBundle(publishJob.getLocale()).key(
                Messages.GUI_PUBLISH_JOB_FINISHED_1,
                Long.valueOf(publishJob.getEnqueueTime()));
        } else {
            hasError = true;
            Object[] params = new Object[] {
                Long.valueOf(publishJob.getEnqueueTime()),
                Integer.valueOf(publishJob.getReport().getErrors().size()),
                Integer.valueOf(publishJob.getReport().getWarnings().size())};
            msgText = Messages.get().getBundle(publishJob.getLocale()).key(
                Messages.GUI_PUBLISH_JOB_FINISHED_WITH_WARNS_3,
                params);
        }
        m_publishEngine.sendMessage(publishJob.getUserId(), msgText, hasError);
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
        for (Iterator<I_CmsPublishEventListener> it = iterator(); it.hasNext();) {
            I_CmsPublishEventListener listener = it.next();
            try {
                listener.onRemove(publishJob);
            } catch (Throwable t) {
                // catch every thing including runtime exceptions
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.ERR_PUBLISH_JOB_REMOVE_ERROR_1,
                            listener.getClass().getName()),
                        t);
                }
                if (publishJob.m_publishJob.getPublishReport() != null) {
                    publishJob.m_publishJob.getPublishReport().println(t);
                }
            }
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
        for (Iterator<I_CmsPublishEventListener> it = iterator(); it.hasNext();) {
            I_CmsPublishEventListener listener = it.next();
            try {
                listener.onStart(publishJob);
            } catch (Throwable t) {
                // catch every thing including runtime exceptions
                if (LOG.isErrorEnabled()) {
                    LOG.error(
                        Messages.get().getBundle().key(
                            Messages.ERR_PUBLISH_JOB_START_ERROR_1,
                            listener.getClass().getName()),
                        t);
                }
                if (publishJob.m_publishJob.getPublishReport() != null) {
                    publishJob.m_publishJob.getPublishReport().println(t);
                }
            }
        }
        // popup the start message
        boolean busyStart = ((System.currentTimeMillis() - publishJob.getEnqueueTime()) > 2000);
        boolean bigJob = ((publishJob.getPublishList().size() > 25)
            || (OpenCms.getStaticExportManager().getHandler() instanceof CmsAfterPublishStaticExportHandler));
        if (busyStart || bigJob) {
            String msgText = Messages.get().getBundle(publishJob.getLocale()).key(
                Messages.GUI_PUBLISH_JOB_STARTED_1,
                Long.valueOf(publishJob.getEnqueueTime()));
            m_publishEngine.sendMessage(publishJob.getUserId(), msgText, false);
        }
    }
}
