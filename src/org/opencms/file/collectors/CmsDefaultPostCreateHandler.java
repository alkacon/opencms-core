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

package org.opencms.file.collectors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;

import org.apache.commons.logging.Log;

/**
 * Default implementation for the post-create handler interface.<p>
 *
 * This default implementation does nothing.
 */
public class CmsDefaultPostCreateHandler implements I_CmsCollectorPostCreateHandler {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultPostCreateHandler.class);

    /**
     * @see org.opencms.file.collectors.I_CmsCollectorPostCreateHandler#onCreate(org.opencms.file.CmsObject, org.opencms.file.CmsResource, boolean)
     */
    public void onCreate(CmsObject cms, CmsResource createdResource, boolean copyMode) {

        // This method does nothing useful

        LOG.debug("onCreate " + createdResource.getRootPath() + ", copyMode = " + copyMode);
    }

}
