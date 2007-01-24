/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/repository/Attic/I_CmsRepository.java,v $
 * Date   : $Date: 2007/01/24 10:04:26 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.repository;

import javax.servlet.http.HttpServletRequest;

/**
 * The entry point into the content repository.<p>
 * 
 * Represents the entry point into the content repository.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.5.6
 */
public interface I_CmsRepository {

    /**
     * Login a user given the session data.<p> 
     * 
     * @param request the user request
     * @param siteName the site
     * @param projectName the project
     * 
     * @return the authenticated session
     * 
     * @throws CmsRepositoryAuthorizationException if something goes wrong 
     */
    I_CmsRepositorySession login(HttpServletRequest request, String siteName, String projectName)
    throws CmsRepositoryAuthorizationException;

    /**
     * Login a user given the session data.<p> 
     * 
     * @param userName the user name
     * @param password the user's password
     * @param siteName the site
     * @param projectName the project
     * 
     * @return the authenticated session
     * 
     * @throws CmsRepositoryAuthorizationException if something goes wrong 
     */
    I_CmsRepositorySession login(String userName, String password, String siteName, String projectName)
    throws CmsRepositoryAuthorizationException;

}
