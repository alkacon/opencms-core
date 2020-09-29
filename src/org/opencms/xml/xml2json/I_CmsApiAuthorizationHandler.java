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

package org.opencms.xml.xml2json;

import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import javax.servlet.http.HttpServletRequest;

/**
 * Special authorization handler for APIs using stateless authorization.
 *
 * <p>This does *not* handle authorization for normal OpenCms users (editors and website users), but is meant for use in APIs implemented
 * as request handlers or resource init handlers. Authorization is supposed to be stateless, i.e. authorization information is passed with every
 * HTTP request.
 */
public interface I_CmsApiAuthorizationHandler {

    /**
     * Authenticates a user from a request and returns a CmsObject initialized with that user.
     *
     *  <p>If no user can be authenticated from the request, this method returns null.
     *
     * @param adminCms a CmsObject with root admin privileges
     * @param request the request
     * @return the CmsObject for the request
     *
     * @throws CmsException if something goes wrong
     */
    CmsObject initCmsObject(CmsObject adminCms, HttpServletRequest request) throws CmsException;

    /**
     * Sets the admin CmsObject used internally by this handler.
     *
     * @param cms the admin CmsObject to use
     */
    void initialize(CmsObject cms);

    /**
     * Sets the configuration parameters for this handler.
     *
     * @param params the configuration parameters
     */
    void setParameters(CmsParameterConfiguration params);

}
