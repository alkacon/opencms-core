/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/publish/CmsPublishJobFinished.java,v $
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

import org.opencms.util.CmsUUID;

/**
 * Defines a read-only publish job that has been already published.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $
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
     * Returns the publish history id.<p>
     * 
     * @return the publish history id
     */
    public CmsUUID getPublishHistoryId() {
        
        return m_publishJob.getPublishHistoryId();
    }

    /**
     * Returns the path in the RFS to temporary store the report for the publish job.<p>
     *
     * @return the path in the RFS to temporary store the report for the publish job
     */
    public String getReportFilePath() {

        return m_publishJob.getReportFilePath();
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
