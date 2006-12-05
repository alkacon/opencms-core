/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/Attic/CmsPublishDefaultEventListener.java,v $
 * Date   : $Date: 2006/12/05 16:31:07 $
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


/**
 * Default implementation for the {@link I_CmsPublishEventListener}.<p>
 * 
 * This implementation notifies the user once a publish job starts and finishes.<p> 
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.5.5
 */
class CmsPublishDefaultEventListener extends CmsPublishEventAdapter {

    /** Publish engine. */
    private CmsPublishEngine m_publishEngine;

    /**
     * Default constructor.<p>
     * 
     * @param publishEngine the publish engine
     */
    protected CmsPublishDefaultEventListener(CmsPublishEngine publishEngine) {

        m_publishEngine = publishEngine;
    }

    /**
     * @see org.opencms.publish.CmsPublishEventAdapter#onAbort(String, org.opencms.publish.CmsPublishJobEnqueued)
     */
    public void onAbort(String userName, CmsPublishJobEnqueued publishJob) {

        if (userName.equals(publishJob.getUserName())) {
            // prevent showing messages if the owner aborted the job by himself 
            return;
        }
        String msgText = Messages.get().getBundle(publishJob.getLocale()).key(
            Messages.GUI_PUBLISH_JOB_ABORTED_2,
            new Long(publishJob.getEnqueueTime()),
            userName);
        sendMessage(publishJob.getUserName(), msgText);
    }

    /**
     * @see org.opencms.publish.CmsPublishEventAdapter#onFinish(org.opencms.publish.CmsPublishJobRunning)
     */
    public void onFinish(CmsPublishJobRunning publishJob) {

        String msgText;
        if (!publishJob.getReport().hasError() && !publishJob.getReport().hasWarning()) {
            msgText = Messages.get().getBundle(publishJob.getLocale()).key(
                Messages.GUI_PUBLISH_JOB_FINISHED_1,
                new Long(publishJob.getEnqueueTime()));
        } else {
            Object[] params = new Object[] {
                new Long(publishJob.getEnqueueTime()),
                new Integer(publishJob.getReport().getErrors().size()),
                new Integer(publishJob.getReport().getWarnings().size())};
            msgText = Messages.get().getBundle(publishJob.getLocale()).key(
                Messages.GUI_PUBLISH_JOB_FINISHED_WITH_WARNS_3,
                params);
        }
        sendMessage(publishJob.getUserName(), msgText);
    }

    /**
     * @see org.opencms.publish.CmsPublishEventAdapter#onStart(org.opencms.publish.CmsPublishJobEnqueued)
     */
    public void onStart(CmsPublishJobEnqueued publishJob) {

        boolean busyStart = ((System.currentTimeMillis() - publishJob.getEnqueueTime()) > 2000);
        boolean bigJob = (publishJob.getPublishList().size() > 25);
        if (busyStart && bigJob) {
            String msgText = Messages.get().getBundle(publishJob.getLocale()).key(
                Messages.GUI_PUBLISH_JOB_STARTED_1,
                new Long(publishJob.getEnqueueTime()));
            sendMessage(publishJob.getUserName(), msgText);
        }
    }

    /**
     * Sends a message to the given user.<p>
     * 
     * @param toUserName the user to send the message to
     * @param message the message to send
     */
    protected void sendMessage(String toUserName, String message) {

        m_publishEngine.sendMessage(toUserName, message);
    }
}
