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

import org.opencms.util.CmsUUID;

/**
 * This interface listens to events for a specific publish job.<p>
 *
 * The life cycle of publish is following:
 * <ul>
 *   <li>created, getting immediatly
 *   <li>enqueued, getting the state waiting
 *   <li>during waiting it could be aborted, getting removed (only the abort event is triggered)
 *   <li>started, getting the state running
 *   <li>finished, getting the state finished
 *   <li>removed
 * </ul>
 *
 * During the waiting state a publish job can be aborted for shutdown.<p>
 *
 * @since 6.5.5
 */
public interface I_CmsPublishEventListener {

    /**
     * Called when the job is going to be aborted, this may happen during the shutdown
     * And can only happen if the job is waiting.<p>
     *
     * @param userId the id of the user that aborted the job
     * @param publishJob the publish job that is going to be aborted
     */
    void onAbort(CmsUUID userId, CmsPublishJobEnqueued publishJob);

    /**
     * Called once the job is going to be enqueued.<p>
     *
     * @param publishJob the publish job that is going to be enqueued
     */
    void onEnqueue(CmsPublishJobBase publishJob);

    /**
     * Called once the job has finished.<p>
     *
     * @param publishJob the publish job that has finished
     */
    void onFinish(CmsPublishJobRunning publishJob);

    /**
     * Called once the job is going to be removed from the history.<p>
     *
     * @param publishJob the publish job that is going to be removed from the history
     */
    void onRemove(CmsPublishJobFinished publishJob);

    /**
     * Called once the job is going to start.<p>
     *
     * @param publishJob the publish job that is going to start
     */
    void onStart(CmsPublishJobEnqueued publishJob);
}
