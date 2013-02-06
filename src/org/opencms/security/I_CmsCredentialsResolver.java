/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.security;

/**
 * This interface can be used to override or replace user names or passwords which occur in the OpenCms configuration,
 * for example to hide passwords from users who can read the configuration files. 
 */
public interface I_CmsCredentialsResolver {

    /** Credential type for database pool passwords. */
    String DB_PASSWORD = "db_password";

    /** Credential type for database pool user names. */
    String DB_USER = "db_user";

    /** Credential type for LDAP passwords. */
    String LDAP_PASSWORD = "ldap_password";

    /** Credential type for LDAP users. */
    String LDAP_USER = "ldap_user";

    /**
     * Translates user names or passwords for authentication.<p>
     * 
     * @param credentialType the type of credential to resolve (this is equal to one of the constants defined in this interface)
     * @param valueFromConfiguration the original value which was read from the configuration file (may be null) 
     *  
     * @return the credential value that should be used for authentication 
     */
    String resolveCredential(String credentialType, String valueFromConfiguration);
}
