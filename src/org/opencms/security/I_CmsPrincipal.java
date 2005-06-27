/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/I_CmsPrincipal.java,v $
 * Date   : $Date: 2005/06/27 23:22:25 $
 * Version: $Revision: 1.13 $
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

import org.opencms.util.CmsUUID;

/**
 * Representation of an identity in the cms (currently user or group), 
 * used to define permissions on a resource.<p>
 * 
 * @author Carsten Weinholz 
 * 
 * @version $Revision: 1.13 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsPrincipal {

    /** This flag is set for disabled principals in the database. */
    int FLAG_DISABLED = 1;

    /** This flag is set for enabled principals in the database. */
    int FLAG_ENABLED = 0;

    /** Flag to indicate a goup is a potential project manager group. */
    int FLAG_GROUP_PROJECT_MANAGER = 2;

    /** Flag to indicate a goup is a potential project user group. */
    int FLAG_GROUP_PROJECT_USER = 4;

    /** Flag to indicate a goup is used as a role in the workflow. */
    int FLAG_GROUP_WORKFLOW_ROLE = 8;

    /** Identifier for group principals. */
    String PRINCIPAL_GROUP = "GROUP";

    /** Identifier for user principals. */
    String PRINCIPAL_USER = "USER";

    /**
     * Compares the given object with this principal.<p>
     * 
     * @param obj object to comapre
     * @return true if the object is equal
     */
    boolean equals(Object obj);

    /**
     * Gets the id of this principal.<p>
     * 
     * @return the unique id of the principal.
     */
    CmsUUID getId();

    /**
     * Gets the name of this principal.<p>
     * 
     * @return the name of the principal
     */
    String getName();

    /**
     * Returns the hash code of this object.<p>
     * 
     * @return the hash code
     */
    int hashCode();
}