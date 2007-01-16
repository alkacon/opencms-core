/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/security/CmsRoleManager.java,v $
 * Date   : $Date: 2007/01/16 09:50:47 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2006 Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

/**
 * This manager provide access to the role related operations.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.5.6
 */
public class CmsRoleManager {

    /** The security manager. */
    private final CmsSecurityManager m_securityManager;

    /**
     * Default constructor.<p>
     * 
     * @param securityManager the security manager
     */
    public CmsRoleManager(CmsSecurityManager securityManager) {

        m_securityManager = securityManager;
    }

    /**
     * Checks if the user of this OpenCms context is a member of the given role.<p>
     *  
     * This method can only be used for roles that are not organizational unit dependent.<p>
     *  
     * @param cms the opencms context
     * @param role the role to check
     * 
     * @throws CmsRoleViolationException if the user does not have the required role permissions
     * 
     * @see CmsRole#isOrganizationalUnitIndependent()
     */
    public void checkRole(CmsObject cms, CmsRole role) throws CmsRoleViolationException {

        m_securityManager.checkRole(cms.getRequestContext(), role);
    }

    /**
     * Checks if the user of this OpenCms context is a member of the given role
     * for the given organizational unit.<p>
     * 
     * The user must have the given role in at least one parent organizational unit.<p>
     * 
     * @param cms the opencms context
     * @param role the role to check
     * @param ouFqn the fully qualified name of the organizational unit to check the role for
     * 
     * @throws CmsRoleViolationException if the user does not have the required role permissions
     */
    public void checkRoleForOrgUnit(CmsObject cms, CmsRole role, String ouFqn) throws CmsRoleViolationException {

        m_securityManager.checkRole(cms.getRequestContext(), role, ouFqn);
    }

    /**
     * Checks if the user of this OpenCms context is a member of the given role
     * for the given resource.<p>
     * 
     * The user must have the given role in at least one organizational unit to which this resource belongs.<p>
     * 
     * @param cms the opencms context
     * @param role the role to check
     * @param resourceName the name of the resource to check the role for
     * 
     * @throws CmsRoleViolationException if the user does not have the required role permissions
     * @throws CmsException if something goes wrong, while reading the resource
     */
    public void checkRoleForResource(CmsObject cms, CmsRole role, String resourceName)
    throws CmsException, CmsRoleViolationException {

        CmsResource resource = cms.readResource(resourceName);
        m_securityManager.checkRole(cms.getRequestContext(), role, resource);
    }

    /**
     * Checks if the user of the current OpenCms context 
     * is a member of at last one of the roles in the given role set.<p>
     *  
     * @param cms the opencms context
     * @param role the role to check
     * 
     * @return <code>true</code> if the user of the current OpenCms context is at a member of at last 
     *      one of the roles in the given role set
     */
    public boolean hasRole(CmsObject cms, CmsRole role) {

        return m_securityManager.hasRole(cms.getRequestContext(), role, (String)null);
    }

    /**
     * Checks if the user of the current OpenCms context 
     * is a member of at last one of the roles in the given role set.<p>
     *  
     * @param cms the opencms context
     * @param role the role to check
     * @param ouFqn the fully qualified name of the organizational unit to check
     * 
     * @return <code>true</code> if the user of the current OpenCms context is at a member of at last 
     *      one of the roles in the given role set
     */
    public boolean hasRoleForOrgUnit(CmsObject cms, CmsRole role, String ouFqn) {

        return m_securityManager.hasRole(cms.getRequestContext(), role, ouFqn);
    }

    /**
     * Checks if the user of the current OpenCms context 
     * is a member of at last one of the roles in the given role set.<p>
     *  
     * @param cms the opencms context
     * @param role the role to check
     * @param resourceName the name of the resource to check
     * 
     * @return <code>true</code> if the user of the current OpenCms context is at a member of at last 
     *      one of the roles in the given role set
     * @throws CmsException if something goes wrong wile reading the resource
     */
    public boolean hasRoleForResource(CmsObject cms, CmsRole role, String resourceName) throws CmsException {

        CmsResource resource = cms.readResource(resourceName);
        return m_securityManager.hasRole(cms.getRequestContext(), role, resource);
    }

}
