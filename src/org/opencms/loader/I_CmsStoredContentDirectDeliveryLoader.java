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

package org.opencms.loader;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Capability interface for loaders which allow direct delivery of externally stored original content.<p>
 */
public interface I_CmsStoredContentDirectDeliveryLoader {

    /**
     * Tries to deliver externally stored content directly during static export.<p>
     *
     * @param cms the CMS context
     * @param resource the resource
     * @param req the request
     * @param res the response
     *
     * @return <code>true</code> if the response was handled
     *
     * @throws IOException in case writing to the response fails
     * @throws CmsException in case storage delivery fails
     */
    boolean exportStoredContentTo(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws IOException, CmsException;

    /**
     * Returns if direct delivery of externally stored original content is enabled for the given resource.<p>
     *
     * @param cms the CMS context
     * @param resource the resource
     * @param req the request
     * @param res the response
     *
     * @return <code>true</code> if direct stored content delivery may be used
     *
     * @throws CmsException in case the decision can not be made
     */
    boolean isStoredContentDirectDeliveryEnabled(
        CmsObject cms,
        CmsResource resource,
        HttpServletRequest req,
        HttpServletResponse res)
    throws CmsException;
}
