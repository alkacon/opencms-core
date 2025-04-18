/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.publish;

import org.opencms.util.CmsUUID;

/**
 * Default implementation for the {@link I_CmsPublishEventListener}.<p>
 *
 * @since 6.5.5
 */
public class CmsPublishEventAdapter implements I_CmsPublishEventListener {

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onAbort(CmsUUID, org.opencms.publish.CmsPublishJobEnqueued)
     */
    public void onAbort(CmsUUID userId, CmsPublishJobEnqueued publishJob) {

        // do nothing
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onEnqueue(org.opencms.publish.CmsPublishJobBase)
     */
    public void onEnqueue(CmsPublishJobBase publishJob) {

        // do nothing
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onFinish(org.opencms.publish.CmsPublishJobRunning)
     */
    public void onFinish(CmsPublishJobRunning publishJob) {

        // do nothing
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onRemove(org.opencms.publish.CmsPublishJobFinished)
     */
    public void onRemove(CmsPublishJobFinished publishJob) {

        // do nothing
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onStart(org.opencms.publish.CmsPublishJobEnqueued)
     */
    public void onStart(CmsPublishJobEnqueued publishJob) {

        // do nothing
    }
}
