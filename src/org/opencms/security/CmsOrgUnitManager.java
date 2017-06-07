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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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
import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsUserSearchParameters;
import org.opencms.main.CmsException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * This manager provide access to the organizational unit related operations.<p>
 *
 * @since 6.5.6
 */
public class CmsOrgUnitManager {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsOrgUnitManager.class);

    /** The security manager. */
    private final CmsSecurityManager m_securityManager;

    /**
     * Default constructor.<p>
     *
     * @param securityManager the security manager
     */
    public CmsOrgUnitManager(CmsSecurityManager securityManager) {

        m_securityManager = securityManager;
    }

    /**
     * Adds a resource to the given organizational unit.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the full qualified name of the organizational unit to add the resource to
     * @param resourceName the name of the resource that is to be added to the organizational unit
     *
     * @throws CmsException if something goes wrong
     */
    public void addResourceToOrgUnit(CmsObject cms, String ouFqn, String resourceName) throws CmsException {

        CmsOrganizationalUnit orgUnit = readOrganizationalUnit(cms, ouFqn);
        CmsResource resource = cms.readResource(resourceName);
        m_securityManager.addResourceToOrgUnit(cms.getRequestContext(), orgUnit, resource);
    }

    /**
     * Counts the users which fit the given search criteria.<p>
     *
     * @param cms the current CMS context
     * @param params the user search parameters
     *
     * @return the total number of users which fit the given search parameters
     *
     * @throws CmsException if something goes wrong
     */
    public long countUsers(CmsObject cms, CmsUserSearchParameters params) throws CmsException {

        return m_securityManager.countUsers(cms.getRequestContext(), params);
    }

    /**
     * Creates a new organizational unit.<p>
     *
     * The parent structure must exist.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the fully qualified name of the new organizational unit
     * @param description the description of the new organizational unit
     * @param flags the flags for the new organizational unit
     * @param resourceName the first associated resource
     *
     * @return a <code>{@link CmsOrganizationalUnit}</code> object representing
     *          the newly created organizational unit
     *
     * @throws CmsException if operation was not successful
     */
    public CmsOrganizationalUnit createOrganizationalUnit(
        CmsObject cms,
        String ouFqn,
        String description,
        int flags,
        String resourceName) throws CmsException {

        CmsResource resource = null;
        if (((flags & CmsOrganizationalUnit.FLAG_WEBUSERS) == 0) || (resourceName != null)) {
            // only normal OUs have to have at least one resource
            resource = cms.readResource(resourceName);
        }
        return m_securityManager.createOrganizationalUnit(cms.getRequestContext(), ouFqn, description, flags, resource);
    }

    /**
     * Deletes an organizational unit.<p>
     *
     * Only organizational units that contain no sub units can be deleted.<p>
     *
     * The organizational unit can not be delete if it is used in the request context,
     * or if the current user belongs to it.<p>
     *
     * All users and groups in the given organizational unit will be deleted.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the fully qualified name of the organizational unit to delete
     *
     * @throws CmsException if operation was not successful
     */
    public void deleteOrganizationalUnit(CmsObject cms, String ouFqn) throws CmsException {

        CmsOrganizationalUnit orgUnit = readOrganizationalUnit(cms, ouFqn);
        m_securityManager.deleteOrganizationalUnit(cms.getRequestContext(), orgUnit);
    }

    /**
     * Returns all accessible projects of the given organizational unit.
     *
     * That is all projects which are owned by the current user or which are
     * accessible for the group of the user.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the fully qualified name of the organizational unit to get projects for
     * @param includeSubOus if all projects of sub-organizational units should be retrieved too
     *
     * @return all <code>{@link org.opencms.file.CmsProject}</code> objects in the organizational unit
     *
     * @throws CmsException if operation was not successful
     */
    public List<CmsProject> getAllAccessibleProjects(CmsObject cms, String ouFqn, boolean includeSubOus)
    throws CmsException {

        CmsOrganizationalUnit orgUnit = readOrganizationalUnit(cms, ouFqn);
        return (m_securityManager.getAllAccessibleProjects(cms.getRequestContext(), orgUnit, includeSubOus));
    }

    /**
     * Returns all manageable projects of the given organizational unit.<p>
     *
     * That is all projects which are owned by the current user or which are manageable
     * for the group of the user.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the fully qualified name of the organizational unit to get projects for
     * @param includeSubOus if all projects of sub-organizational units should be retrieved too
     *
     * @return all <code>{@link org.opencms.file.CmsProject}</code> objects in the organizational unit
     *
     * @throws CmsException if operation was not successful
     */
    public List<CmsProject> getAllManageableProjects(CmsObject cms, String ouFqn, boolean includeSubOus)
    throws CmsException {

        CmsOrganizationalUnit orgUnit = readOrganizationalUnit(cms, ouFqn);
        return (m_securityManager.getAllManageableProjects(cms.getRequestContext(), orgUnit, includeSubOus));
    }

    /**
     * Returns all groups of the given organizational unit.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the fully qualified name of the organizational unit to get all principals for
     * @param includeSubOus if all groups of sub-organizational units should be retrieved too
     *
     * @return all <code>{@link org.opencms.file.CmsGroup}</code> objects in the organizational unit
     *
     * @throws CmsException if operation was not successful
     */
    public List<CmsGroup> getGroups(CmsObject cms, String ouFqn, boolean includeSubOus) throws CmsException {

        CmsOrganizationalUnit orgUnit = readOrganizationalUnit(cms, ouFqn);
        return (m_securityManager.getGroups(cms.getRequestContext(), orgUnit, includeSubOus, false));
    }

    /**
     * Returns all child organizational units of the given parent organizational unit including
     * hierarchical deeper organization units if needed.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the fully qualified name of the parent organizational unit
     * @param includeChildren if hierarchical deeper organization units should also be returned
     *
     * @return a list of <code>{@link CmsOrganizationalUnit}</code> objects
     *
     * @throws CmsException if operation was not successful
     */
    public List<CmsOrganizationalUnit> getOrganizationalUnits(CmsObject cms, String ouFqn, boolean includeChildren)
    throws CmsException {

        CmsOrganizationalUnit parent = readOrganizationalUnit(cms, ouFqn);
        return m_securityManager.getOrganizationalUnits(cms.getRequestContext(), parent, includeChildren);
    }

    /**
     * Returns all resources of the given organizational unit.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the fully qualified name of the organizational unit to get all resources for
     *
     * @return all <code>{@link CmsResource}</code> objects in the organizational unit
     *
     * @throws CmsException if operation was not successful
     */
    public List<CmsResource> getResourcesForOrganizationalUnit(CmsObject cms, String ouFqn) throws CmsException {

        CmsOrganizationalUnit orgUnit = readOrganizationalUnit(cms, ouFqn);
        return m_securityManager.getResourcesForOrganizationalUnit(cms.getRequestContext(), orgUnit);
    }

    /**
     * Returns all users of the given organizational unit.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the fully qualified name of the organizational unit to get all principals for
     * @param recursive if all users of sub-organizational units should be retrieved too
     *
     * @return all <code>{@link org.opencms.file.CmsUser}</code> objects in the organizational unit
     *
     * @throws CmsException if operation was not successful
     */
    public List<CmsUser> getUsers(CmsObject cms, String ouFqn, boolean recursive) throws CmsException {

        CmsOrganizationalUnit orgUnit = readOrganizationalUnit(cms, ouFqn);
        return m_securityManager.getUsers(cms.getRequestContext(), orgUnit, recursive);
    }

    /**
     * Returns all users of the given organizational unit, without their additional info.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the fully qualified name of the organizational unit to get all principals for
     * @param recursive if all users of sub-organizational units should be retrieved too
     *
     * @return all <code>{@link org.opencms.file.CmsUser}</code> objects in the organizational unit
     *
     * @throws CmsException if operation was not successful
     */
    public List<CmsUser> getUsersWithoutAdditionalInfo(CmsObject cms, String ouFqn, boolean recursive)
    throws CmsException {

        CmsOrganizationalUnit orgUnit = readOrganizationalUnit(cms, ouFqn);
        return m_securityManager.getUsersWithoutAdditionalInfo(cms.getRequestContext(), orgUnit, recursive);
    }

    /**
     * Initializes the organizational units.<p>
     *
     * @param cms the admin CMS context
     *
     * @throws CmsException if something goes wrong
     */
    public void initialize(CmsObject cms) throws CmsException {

        List<CmsOrganizationalUnit> ous = getOrganizationalUnits(cms, "", true);
        for (CmsOrganizationalUnit ou : ous) {
            try {
                m_securityManager.initializeOrgUnit(cms.getRequestContext(), ou);
            } catch (CmsInitException e) {
                LOG.error("Error while initializing OU " + ou.getName() + ": " + e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Reads an organizational Unit based on its fully qualified name.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the fully qualified name of the organizational Unit to be read
     *
     * @return the organizational Unit with the provided fully qualified name
     *
     * @throws CmsException if something goes wrong
     */
    public CmsOrganizationalUnit readOrganizationalUnit(CmsObject cms, String ouFqn) throws CmsException {

        return m_securityManager.readOrganizationalUnit(cms.getRequestContext(), ouFqn);
    }

    /**
     * Removes a resource from the given organizational unit.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the fully qualified name of the organizational unit to remove the resource from
     * @param resourceName the name of the resource that is to be removed from the organizational unit
     *
     * @throws CmsException if something goes wrong
     */
    public void removeResourceFromOrgUnit(CmsObject cms, String ouFqn, String resourceName) throws CmsException {

        CmsOrganizationalUnit orgUnit = readOrganizationalUnit(cms, ouFqn);
        CmsResource resource = cms.readResource(resourceName, CmsResourceFilter.ALL);

        m_securityManager.removeResourceFromOrgUnit(cms.getRequestContext(), orgUnit, resource);
    }

    /**
     * Searches users which fit the given search parameters.<p>
     *
     * @param cms the current CMS context
     * @param params the user search parameters
     *
     * @return the users which fit the given search criteria
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsUser> searchUsers(CmsObject cms, CmsUserSearchParameters params) throws CmsException {

        return m_securityManager.searchUsers(cms.getRequestContext(), params);
    }

    /**
     * Moves an user to the given organizational unit.<p>
     *
     * @param cms the opencms context
     * @param ouFqn the full qualified name of the organizational unit to add the user to
     * @param userName the name of the user that is to be added to the organizational unit
     *
     * @throws CmsException if something goes wrong
     */
    public void setUsersOrganizationalUnit(CmsObject cms, String ouFqn, String userName) throws CmsException {

        CmsOrganizationalUnit orgUnit = readOrganizationalUnit(cms, ouFqn);
        CmsUser user = cms.readUser(userName);
        m_securityManager.setUsersOrganizationalUnit(cms.getRequestContext(), orgUnit, user);
    }

    /**
     * Writes an already existing organizational unit.<p>
     *
     * The organizational unit has to be a valid OpenCms organizational unit.<br>
     *
     * The organizational unit will be completely overridden by the given data.<p>
     *
     * @param cms the opencms context
     * @param organizationalUnit the organizational unit that should be written
     *
     * @throws CmsException if operation was not successful
     */
    public void writeOrganizationalUnit(CmsObject cms, CmsOrganizationalUnit organizationalUnit) throws CmsException {

        m_securityManager.writeOrganizationalUnit(cms.getRequestContext(), organizationalUnit);
    }
}
