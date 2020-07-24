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

package org.opencms.webdav;

import java.io.InputStream;

import org.apache.jackrabbit.webdav.DavServletRequest;
import org.apache.jackrabbit.webdav.io.InputContextImpl;

/**
 * Input context that also allows querying the request method.
 */
public class CmsDavInputContext extends InputContextImpl {

    /** The current request. */
    private DavServletRequest m_request;

    /**
     * Creates a new instance.
     *
     * @param request the request
     * @param stream the stream
     */
    public CmsDavInputContext(DavServletRequest request, InputStream stream) {

        super(request, stream);
        m_request = request;

    }

    /**
     * Gets the request method for the current request.
     *
     * @return the request method
     */
    public String getMethod() {

        return m_request.getMethod();
    }
}