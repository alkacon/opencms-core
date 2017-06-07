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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.publish;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

/** Helper functions used to collect information for the publish dialog. */
public final class CmsPublishListHelper {

    /**
     * Hide default constructor.
     */
    private CmsPublishListHelper() {

        // Just to hide the public default contructor
    }

    /**
     * Initializes a CmsObject based on the given one, but with adjusted project information and configured, such that release and expiration date are ignored.<p>
     * @param cms the original CmsObject.
     *
     * @param online true if a CmsObject for the Online project should be returned
     * @return the initialized CmsObject
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsObject adjustCmsObject(CmsObject cms, boolean online) throws CmsException {

        CmsObject result = OpenCms.initCmsObject(cms);
        if (online) {
            CmsProject onlineProject = cms.readProject(CmsProject.ONLINE_PROJECT_ID);
            result.getRequestContext().setCurrentProject(onlineProject);
        }
        result.getRequestContext().setRequestTime(CmsResource.DATE_RELEASED_EXPIRED_IGNORE);
        return result;
    }
}
