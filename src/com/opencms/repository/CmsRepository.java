/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/repository/Attic/CmsRepository.java,v $
 * Date   : $Date: 2003/05/28 16:46:54 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

package com.opencms.repository;

import com.opencms.db.CmsDriverManager;

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.Repository;
import javax.jcr.Ticket;

/**
 * Level 1 implementation of a JCR repository.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1 $ $Date: 2003/05/28 16:46:54 $
 * @since 5.1.2
 */
public class CmsRepository extends Object implements Repository {

    /**
     * The driver manager to access the OpenCms drivers.
     */
    private CmsDriverManager m_driverManager;
    
    /**
     * The credentials used to authenticate/authorize the user. 
     */
    private CmsCredentials m_credentials;        

    /**
     * Creates a new repository using the specified driverManager to access the OpenCms drivers.
     * 
     * @param driverManager rhe driver manager to access the OpenCms drivers
     */
    public CmsRepository(CmsDriverManager driverManager) {
        m_driverManager = driverManager;
    }

    /**
     * Connect creates a new ticket for the given credentials.
     * 
     * @param credentials the credentials used to authenticate/authorize the user 
     * @return a valid ticket to access the repository 
     * @see javax.jcr.Repository#connect(javax.jcr.Credentials)
     * @throws LoginException if the authentication/authorization fails
     */
    public Ticket connect(Credentials credentials) throws LoginException {
        // TODO: implement connect(Credentials) with authentication/authorization
        m_credentials = (CmsCredentials) credentials;
        return new CmsTicket(this);
    }

    /**
     * Returns the driver manager to access the OpenCms drivers.
     * This method is only within the OpenCms repository package visible.
     * 
     * @return the driver manager to access the OpenCms drivers
     */
    CmsDriverManager getDriverManager() {
        return m_driverManager;
    }

    /**
     * Returns the credentials used to authenticate/authorize the user.
     * This method is only within the OpenCms repository package visible.
     * 
     * @return the credentials used to authenticate/authorize the user
     */
    CmsCredentials getCredentials() {
        return m_credentials;
    }    

}
