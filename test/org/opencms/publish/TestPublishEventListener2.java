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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

/**
 * Test event listener implementation, restarting the engine while a publish job is running.<p>
 *
 * @since 6.5.5
 */
public class TestPublishEventListener2 implements I_CmsPublishEventListener {

    /** The cms object. */
    private CmsObject m_cms;

    /**
     * Constructor for passing a cms object.<p>
     *
     * @param cms the cms object
     */
    public TestPublishEventListener2(CmsObject cms) {

        m_cms = cms;
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onAbort(CmsUUID, org.opencms.publish.CmsPublishJobEnqueued)
     */
    public void onAbort(CmsUUID userId, CmsPublishJobEnqueued publishJob) {

        // noop
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onEnqueue(org.opencms.publish.CmsPublishJobBase)
     */
    public void onEnqueue(CmsPublishJobBase publishJob) {

        // noop
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onFinish(org.opencms.publish.CmsPublishJobRunning)
     */
    public void onFinish(CmsPublishJobRunning publishJob) {

        // noop
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onRemove(org.opencms.publish.CmsPublishJobFinished)
     */
    public void onRemove(CmsPublishJobFinished publishJob) {

        // noop
    }

    /**
     * @see org.opencms.publish.I_CmsPublishEventListener#onStart(org.opencms.publish.CmsPublishJobEnqueued)
     */
    public void onStart(CmsPublishJobEnqueued publishJob) {

        // leads to reloading the queue and history data from the database
        try {
            OpenCms.getPublishManager().initialize(m_cms);
        } catch (CmsException exc) {
            // noop
        }
    }
}
