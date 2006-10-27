/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/I_CmsAuthorizationHandler.java,v $
 * Date   : $Date: 2006/10/27 17:23:52 $
 * Version: $Revision: 1.1.2.2 $
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

package org.opencms.security;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;

import javax.servlet.http.HttpServletRequest;

/**
 * Defines general authorization methods.<p>
 * 
 * One of the aplication scenarios for this interface is a personalized SSO implementation.<p>
 * 
 * @author Michael Moossen
 *
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.5.4 
 */
public interface I_CmsAuthorizationHandler {

    /**
     * Creates a new cms object from the given request object.<p>
     * 
     * This method is called by OpenCms everytime a resource is requested
     * and the session can not automatically be authenticated.<p>
     * 
     * @param request the http request to authenticate
     * 
     * @return the cms context object associated to the current session
     */
    CmsObject initCmsObject(HttpServletRequest request);

    /**
     * Autheticates the current request with additional user information.<p>
     * 
     * You have to call this method by your own.<p>
     * 
     * @param request the http request to authenticate
     * @param userName the user name to authenticate
     * @param pwd the user password to authenticate with
     * 
     * @return the cms context object associated to the given user
     * 
     * @throws CmsException if something goes wrong 
     */
    CmsObject initCmsObject(HttpServletRequest request, String userName, String pwd) throws CmsException;
}