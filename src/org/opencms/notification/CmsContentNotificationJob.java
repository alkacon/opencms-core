/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/notification/CmsContentNotificationJob.java,v $
 * Date   : $Date: 2005/09/16 08:51:27 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.notification;

import org.opencms.file.CmsObject;
import org.opencms.scheduler.I_CmsScheduledJob;

import java.util.Map;

/**
 * 
 * Scheduled job that checks the system for resources that will shortly expire, be released, or will be outdated. 
 * A notification e-mail will be send to its responsibles.<p>
 * 
 * @author Jan Baudisch
 * 
 */
public class CmsContentNotificationJob implements I_CmsScheduledJob {

    /**
     * @see org.opencms.scheduler.I_CmsScheduledJob#launch(org.opencms.file.CmsObject, java.util.Map)
     */
    public String launch(CmsObject cms, Map parameters) throws Exception {

        CmsNotificationCandidates candidates = new CmsNotificationCandidates(cms);
        return candidates.notifyResponsibles();
    }
}
