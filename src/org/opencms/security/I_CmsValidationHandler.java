/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/I_CmsValidationHandler.java,v $
 * Date   : $Date: 2006/05/12 15:52:36 $
 * Version: $Revision: 1.1.2.1 $
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

import org.opencms.main.CmsIllegalArgumentException;

/**
 * Defines general validation methods.<p>
 * 
 * @author Michael Moossen
 *
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 6.3.0 
 */
public interface I_CmsValidationHandler {

    /**
     * Checks if the provided email is a valid email address.<p>
     * 
     * @param email the email address to validate
     * 
     * @throws CmsIllegalArgumentException if the given email address is not valid
     */
    void checkEmail(String email) throws CmsIllegalArgumentException;

    /**
     * Checks if the provided group name is a valid user name.<p>
     *
     * @param groupName the group name to check
     * 
     * @throws CmsIllegalArgumentException if the given group name is not valid
     */
    void checkGroupName(String groupName) throws CmsIllegalArgumentException;

    /**
     * Checks if the provided user name is a valid user name.<p>
     *
     * @param userName the user name to check
     * 
     * @throws CmsIllegalArgumentException if the given user name is not valid
     */
    void checkUserName(String userName) throws CmsIllegalArgumentException;

    /**
     * Checks if the provided string is a valid zipcode.<p>
     * 
     * @param zipcode the zipcode to validate
     * 
     * @throws CmsIllegalArgumentException if the given zipcode is not valid
     */
    void checkZipCode(String zipcode) throws CmsIllegalArgumentException;
}