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
 * Capability interface for loaders which can handle on-demand static export without a local export file.<p>
 */
public interface I_CmsStaticExportDirectResponseLoader {

    /**
     * Tries to handle an on-demand static export directly.<p>
     *
     * @param cms the CMS context
     * @param resource the resource to export
     * @param req the current request
     * @param res the current response
     *
     * @return <code>true</code> if the export response has been handled
     *
     * @throws IOException in case writing fails
     * @throws CmsException in case export fails
     */
    boolean tryExportDirectResponse(
        CmsObject cms,
        CmsResource resource,
        HttpServletRequest req,
        HttpServletResponse res)
    throws IOException, CmsException;
}
