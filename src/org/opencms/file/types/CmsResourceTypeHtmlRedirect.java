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
 * For further information about Alkacon Software, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.types;

import org.opencms.file.CmsResource;
import org.opencms.loader.CmsRedirectLoader;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import org.apache.commons.logging.Log;

/**
 * The HTML redirect resource type.<p>
 */
public class CmsResourceTypeHtmlRedirect extends CmsResourceTypeXmlAdeConfiguration {

    private static final Log LOG = CmsLog.getLog(CmsResourceTypeHtmlRedirect.class);

    /** The serial version id. */
    private static final long serialVersionUID = 2757710991033290640L;

    /** Type name constant. */
    public static final String TYPE_NAME = "htmlredirect";

    /**
     * Checks if the given resource is a htmlredirect.
     *
     * @param resource the resource to check
     * @return true if it is an htmlredirect
     */
    public static boolean isRedirect(CmsResource resource) {

        return OpenCms.getResourceManager().matchResourceType(TYPE_NAME, resource.getTypeId());
    }

    /**
     * @see org.opencms.file.types.CmsResourceTypeXmlContent#getLoaderId()
     */
    @Override
    public int getLoaderId() {

        return CmsRedirectLoader.LOADER_ID;
    }
}
