/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/repository/I_CmsRepository.java,v $
 * Date   : $Date: 2011/03/23 14:50:50 $
 * Version: $Revision: 1.9 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2011 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.main.CmsException;

/**
 * Represents the entry point into the repository.<p>
 *
 * Get a {@link I_CmsRepositorySession} through login in with the
 * username and password ({@link #login(String, String)}).<p>
 * 
 * @author Michael Moossen
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.9 $
 * 
 * @since 6.2.4
 */
public interface I_CmsRepository {

    /**
     * Returns the name of the repository.<p>
     * 
     * @return the name of the repository
     */
    String getName();

    /**
     * Login a user given the username and the password.<p> 
     * 
     * @param userName the user name
     * @param password the user's password
     * 
     * @return the authenticated session
     * 
     * @throws CmsException if the login was not succesful
     */
    I_CmsRepositorySession login(String userName, String password) throws CmsException;

    /**
     * Sets the name for this repository.<p>
     * 
     * @param name the name to use for the repository
     */
    void setName(String name);
}
