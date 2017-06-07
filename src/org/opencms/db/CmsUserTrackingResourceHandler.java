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

package org.opencms.db;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsResourceInit;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * This resource handler checks if a resource has to be marked as visited by the current user.
 * It checks the value of the <code>usertracking.mark</code> property.<p>
 *
 * Possible values are:
 * <ul>
 * <li><code>online</code>: The resource is marked only in the online project
 * <li><code>true</code>: The resource is marked in all projects
 * <li><code>false</code>: The resource is not marked at all
 * </ul>
 *
 * @since 8.0
 */
public class CmsUserTrackingResourceHandler implements I_CmsResourceInit {

    /** Property that indicates if resources should be tracked,
     *  value has to be <code>true</code>, <code>false</code> or <code>online</code>.
     */
    public static final String PROPERTY_USERTRACKING_MARK = "usertracking.mark";

    /** Constant for the property value <code>online</code>. */
    public static final String VALUE_ONLINE = "online";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserTrackingResourceHandler.class);

    /**
     * @see org.opencms.main.I_CmsResourceInit#initResource(org.opencms.file.CmsResource, org.opencms.file.CmsObject, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public CmsResource initResource(
        CmsResource resource,
        CmsObject cms,
        HttpServletRequest req,
        HttpServletResponse res) {

        if ((resource != null) && resource.isFile()) {
            // only do something if the resource was found and is a file (that can be marked)
            String mark = "";
            try {
                // read the property value
                mark = cms.readPropertyObject(resource, PROPERTY_USERTRACKING_MARK, true).getValue(CmsStringUtil.FALSE);
            } catch (CmsException e) {
                // ignore, resource will not be marked at all
            }
            if (Boolean.valueOf(mark).booleanValue()
                || (VALUE_ONLINE.equalsIgnoreCase(mark)
                    && cms.getRequestContext().getCurrentProject().isOnlineProject())) {
                // mark the resource as visited by the current user
                try {
                    OpenCms.getSubscriptionManager().markResourceAsVisitedBy(
                        cms,
                        resource,
                        cms.getRequestContext().getCurrentUser());
                } catch (CmsException e) {
                    // error marking resource
                    LOG.error(e);
                }
            }
        }

        return resource;
    }

}
