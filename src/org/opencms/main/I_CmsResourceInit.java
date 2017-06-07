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

package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.security.CmsSecurityException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This interface checks the requested resource from the OpenCms request context
 * and returns it to the calling method, which will usually be
 * {@link OpenCms#initResource(CmsObject, String, HttpServletRequest, HttpServletResponse)}.<p>
 *
 * @since 6.0.0
 */
public interface I_CmsResourceInit {

    /**
     * Possibility to modify or change the CmsFile with the CmsObject.<p>
     *
     * Caution: reading parameters from the request, destroys special characters in all
     * parameters, because the encoding for the request was not set yet.<p>
     *
     * @param resource the requested file
     * @param cms the current CmsObject
     * @param req the current request
     * @param res the current response
     * @return a resource in the OpenCms VFS
     *
     * @throws CmsResourceInitException if other implementations of the interface should not be executed
     * @throws CmsSecurityException if other implementations of the interface should not be executed,
     *      and the security exception should be escalated
     */
    CmsResource initResource(CmsResource resource, CmsObject cms, HttpServletRequest req, HttpServletResponse res)
    throws CmsResourceInitException, CmsSecurityException;

}
