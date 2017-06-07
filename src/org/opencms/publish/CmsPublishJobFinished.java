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

/**
 * Defines a read-only publish job that has been already published.<p>
 *
 * @since 6.5.5
 */
public class CmsPublishJobFinished extends CmsPublishJobBase {

    /**
     * Default constructor.<p>
     *
     * @param publishJob the delegate publish job
     */
    protected CmsPublishJobFinished(CmsPublishJobInfoBean publishJob) {

        super(publishJob);
    }

    /**
     * Returns the time this object has been created.<p>
     *
     * @return the time this object has been created
     */
    public long getEnqueueTime() {

        return m_publishJob.getEnqueueTime();
    }

    /**
     * Returns the time the publish job ends.<p>
     *
     * @return the time the publish job ends
     */
    public long getFinishTime() {

        return m_publishJob.getFinishTime();
    }

    /**
     * Returns the time the publish job did actually start.<p>
     *
     * @return the time the publish job did actually start
     */
    public long getStartTime() {

        return m_publishJob.getStartTime();
    }
}
