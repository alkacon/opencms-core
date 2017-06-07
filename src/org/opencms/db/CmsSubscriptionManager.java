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

package org.opencms.db;

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.history.I_CmsHistoryResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsStringUtil;

import java.util.List;

/**
 * Manager that provides methods to subscribe resources to users, read subscribed or unvisited resources and more.<p>
 *
 * @since 8.0
 */
public class CmsSubscriptionManager {

    /** The default maximum number of visited resources to store per user. */
    private static final int DEFAULT_MAX_VISITEDCOUNT = 1000;

    /** The security manager to access the cms. */
    protected CmsSecurityManager m_securityManager;

    /** Indicates if the subscription functionality is enabled. */
    private boolean m_enabled;

    /** Indicates if the configuration can be modified. */
    private boolean m_frozen;

    /** The maximum number of visited resources to store per user. */
    private int m_maxVisitedCount;

    /** The name of the database pool to use. */
    private String m_poolName;

    /**
     * Initializes a new CmsSubscriptionManager, called from the configuration.<p>
     */
    public CmsSubscriptionManager() {

        m_frozen = false;
    }

    /**
     * Returns the date when the resource was last visited by the user.<p>
     *
     * @param cms the current users context
     * @param user the user to check the date
     * @param resource the resource to check the date
     *
     * @return the date when the resource was last visited by the user
     *
     * @throws CmsException if something goes wrong
     */
    public long getDateLastVisitedBy(CmsObject cms, CmsUser user, CmsResource resource) throws CmsException {

        return m_securityManager.getDateLastVisitedBy(cms.getRequestContext(), getPoolName(), user, resource);
    }

    /**
     * Returns the date when the resource was last visited by the user.<p>
     *
     * @param cms the current users context
     * @param user the user to check the date
     * @param resourcePath the name of the resource to check the date
     *
     * @return the date when the resource was last visited by the user
     *
     * @throws CmsException if something goes wrong
     */
    public long getDateLastVisitedBy(CmsObject cms, CmsUser user, String resourcePath) throws CmsException {

        CmsResource resource = cms.readResource(resourcePath, CmsResourceFilter.ALL);
        return m_securityManager.getDateLastVisitedBy(cms.getRequestContext(), getPoolName(), user, resource);
    }

    /**
     * Returns the maximum number of visited resources to store per user.<p>
     *
     * @return the maximum number of visited resources to store per user
     */
    public int getMaxVisitedCount() {

        if (m_maxVisitedCount < 1) {
            m_maxVisitedCount = DEFAULT_MAX_VISITEDCOUNT;
        }
        return m_maxVisitedCount;
    }

    /**
     * Returns the name of the database pool to use.<p>
     *
     * @return the name of the database pool to use
     */
    public String getPoolName() {

        if (CmsStringUtil.isEmpty(m_poolName)) {
            // use default pool as pool name
            m_poolName = OpenCms.getSqlManager().getDefaultDbPoolName();
        }
        return m_poolName;
    }

    /**
     * Initializes this subscription manager with the OpenCms system configuration.<p>
     *
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     *
     * @throws CmsRoleViolationException in case the given opencms object does not have <code>{@link CmsRole#ROOT_ADMIN}</code> permissions
     */
    public void initialize(CmsObject cms) throws CmsRoleViolationException {

        OpenCms.getRoleManager().checkRole(cms, CmsRole.ROOT_ADMIN);
        m_frozen = true;
    }

    /**
     * Returns if the subscription functionality is enabled.<p>
     *
     * @return <code>true</code> if the subscription functionality is enabled, otherwise <code>false</code>
     */
    public boolean isEnabled() {

        return m_enabled && (m_securityManager != null) && m_securityManager.isSubscriptionDriverAvailable();
    }

    /**
     * Mark the given resource as visited by the user.<p>
     *
     * @param cms the current users context
     * @param resource the resource to mark as visited
     * @param user the user that visited the resource
     *
     * @throws CmsException if something goes wrong
     */
    public void markResourceAsVisitedBy(CmsObject cms, CmsResource resource, CmsUser user) throws CmsException {

        if (!isEnabled()) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_SUBSCRIPTION_MANAGER_DISABLED_0));
        }
        m_securityManager.markResourceAsVisitedBy(cms.getRequestContext(), getPoolName(), resource, user);
    }

    /**
     * Mark the given resource as visited by the user.<p>
     *
     * @param cms the current users context
     * @param resourcePath the name of the resource to mark as visited
     * @param user the user that visited the resource
     *
     * @throws CmsException if something goes wrong
     */
    public void markResourceAsVisitedBy(CmsObject cms, String resourcePath, CmsUser user) throws CmsException {

        CmsResource resource = cms.readResource(resourcePath, CmsResourceFilter.ALL);
        markResourceAsVisitedBy(cms, resource, user);
    }

    /**
     * Returns all resources subscribed by the given user or group.<p>
     *
     * @param cms the current users context
     * @param principal the principal to read the subscribed resources
     *
     * @return all resources subscribed by the given user or group
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readAllSubscribedResources(CmsObject cms, CmsPrincipal principal) throws CmsException {

        return m_securityManager.readAllSubscribedResources(cms.getRequestContext(), getPoolName(), principal);
    }

    /**
     * Returns the resources that were visited by a user set in the filter.<p>
     *
     * @param cms the current users context
     * @param filter the filter that is used to get the visited resources
     *
     * @return the resources that were visited by a user set in the filter
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readResourcesVisitedBy(CmsObject cms, CmsVisitedByFilter filter) throws CmsException {

        return m_securityManager.readResourcesVisitedBy(cms.getRequestContext(), getPoolName(), filter);
    }

    /**
     * Returns the subscribed history resources that were deleted.<p>
     *
     * @param cms the current users context
     * @param user the user that subscribed to the resource
     * @param includeGroups indicates if the users groups should also be checked for subscribed deleted resources
     * @param folderPath the folder path of the deleted resources, if <code>null</code> all deleted resources will be returned
     * @param includeSubFolders indicates if the sub folders of the specified folder path should be considered, too
     * @param deletedFrom the time stamp from which the resources should have been deleted
     *
     * @return the subscribed history resources that were deleted
     *
     * @throws CmsException if something goes wrong
     */
    public List<I_CmsHistoryResource> readSubscribedDeletedResources(
        CmsObject cms,
        CmsUser user,
        boolean includeGroups,
        String folderPath,
        boolean includeSubFolders,
        long deletedFrom) throws CmsException {

        List<CmsGroup> groups = null;
        if (includeGroups) {
            try {
                groups = cms.getGroupsOfUser(user.getName(), false);
            } catch (CmsException e) {
                // failed to set user groups
            }
        }
        CmsResource resource = null;
        if (CmsStringUtil.isNotEmpty(folderPath)) {
            resource = cms.readResource(folderPath, CmsResourceFilter.ALL);
        }
        return m_securityManager.readSubscribedDeletedResources(
            cms.getRequestContext(),
            getPoolName(),
            user,
            groups,
            resource,
            includeSubFolders,
            deletedFrom);
    }

    /**
     * Returns the resources that were subscribed by a user or group set in the filter.<p>
     *
     * @param cms the current users context
     * @param filter the filter that is used to get the subscribed resources
     *
     * @return the resources that were subscribed by a user or group set in the filter
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> readSubscribedResources(CmsObject cms, CmsSubscriptionFilter filter) throws CmsException {

        return m_securityManager.readSubscribedResources(cms.getRequestContext(), getPoolName(), filter);
    }

    /**
     * Sets if the subscription functionality is enabled.<p>
     *
     * @param enabled the flag indicating if the subscription functionality is enabled
     */
    public void setEnabled(boolean enabled) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_SUBSCRIPTIONMANAGER_FROZEN_0));
        }
        m_enabled = enabled;
    }

    /**
     * Sets if the subscription functionality is enabled.<p>
     *
     * @param enabled the flag indicating if the subscription functionality is enabled
     */
    public void setEnabled(String enabled) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_SUBSCRIPTIONMANAGER_FROZEN_0));
        }
        m_enabled = Boolean.valueOf(enabled).booleanValue();
    }

    /**
     * Sets the maximum number of visited resources to store per user.<p>
     *
     * @param maxVisitedCount the maximum number of visited resources to store per user
     */
    public void setMaxVisitedCount(String maxVisitedCount) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_SUBSCRIPTIONMANAGER_FROZEN_0));
        }
        try {
            int intValue = Integer.parseInt(maxVisitedCount);
            m_maxVisitedCount = (intValue > 0) ? intValue : DEFAULT_MAX_VISITEDCOUNT;
        } catch (NumberFormatException e) {
            // use default value
            m_maxVisitedCount = DEFAULT_MAX_VISITEDCOUNT;
        }
    }

    /**
     * Sets the name of the database pool to use.<p>
     *
     * @param poolName the name of the database pool to use
     */
    public void setPoolName(String poolName) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_SUBSCRIPTIONMANAGER_FROZEN_0));
        }
        m_poolName = poolName;
    }

    /**
     * Sets the security manager during initialization.<p>
     *
     * @param securityManager the security manager
     */
    public void setSecurityManager(CmsSecurityManager securityManager) {

        if (m_frozen) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_CONFIG_SUBSCRIPTIONMANAGER_FROZEN_0));
        }
        m_securityManager = securityManager;
    }

    /**
     * Marks a subscribed resource as deleted.<p>
     *
     * @param cms the current users context
     * @param resource the subscribed resource to mark as deleted
     *
     * @throws CmsException if something goes wrong
     */
    public void setSubscribedResourceAsDeleted(CmsObject cms, CmsResource resource) throws CmsException {

        if (!isEnabled()) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_SUBSCRIPTION_MANAGER_DISABLED_0));
        }
        m_securityManager.setSubscribedResourceAsDeleted(cms.getRequestContext(), getPoolName(), resource);
    }

    /**
     * Subscribes the user or group to the resource.<p>
     *
     * @param cms the current users context
     * @param principal the principal that subscribes to the resource
     * @param resource the resource to subscribe to
     *
     * @throws CmsException if something goes wrong
     */
    public void subscribeResourceFor(CmsObject cms, CmsPrincipal principal, CmsResource resource) throws CmsException {

        if (!isEnabled()) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_SUBSCRIPTION_MANAGER_DISABLED_0));
        }
        m_securityManager.subscribeResourceFor(cms.getRequestContext(), getPoolName(), principal, resource);
    }

    /**
     * Subscribes the user or group to the resource.<p>
     *
     * @param cms the current users context
     * @param principal the principal that subscribes to the resource
     * @param resourcePath the name of the resource to subscribe to
     *
     * @throws CmsException if something goes wrong
     */
    public void subscribeResourceFor(CmsObject cms, CmsPrincipal principal, String resourcePath) throws CmsException {

        CmsResource resource = cms.readResource(resourcePath, CmsResourceFilter.ALL);
        subscribeResourceFor(cms, principal, resource);
    }

    /**
     * Unsubscribes all deleted resources that were deleted before the specified time stamp.<p>
     *
     * @param cms the current users context
     * @param deletedTo the time stamp to which the resources have been deleted
     *
     * @throws CmsException if something goes wrong
     */
    public void unsubscribeAllDeletedResources(CmsObject cms, long deletedTo) throws CmsException {

        if (!isEnabled()) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_SUBSCRIPTION_MANAGER_DISABLED_0));
        }
        m_securityManager.unsubscribeAllDeletedResources(cms.getRequestContext(), getPoolName(), deletedTo);
    }

    /**
    * Unsubscribes the user or group from all resources.<p>
    *
    * @param cms the current users context
    * @param principal the principal that unsubscribes from all resources
    *
    * @throws CmsException if something goes wrong
    */
    public void unsubscribeAllResourcesFor(CmsObject cms, CmsPrincipal principal) throws CmsException {

        if (!isEnabled()) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_SUBSCRIPTION_MANAGER_DISABLED_0));
        }
        m_securityManager.unsubscribeAllResourcesFor(cms.getRequestContext(), getPoolName(), principal);
    }

    /**
     * Unsubscribes the principal from the resource.<p>
     *
     * @param cms the current users context
     * @param principal the principal that unsubscribes from the resource
     * @param resource the resource to unsubscribe from
     *
     * @throws CmsException if something goes wrong
     */
    public void unsubscribeResourceFor(CmsObject cms, CmsPrincipal principal, CmsResource resource)
    throws CmsException {

        if (!isEnabled()) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_SUBSCRIPTION_MANAGER_DISABLED_0));
        }
        m_securityManager.unsubscribeResourceFor(cms.getRequestContext(), getPoolName(), principal, resource);
    }

    /**
     * Unsubscribes the principal from the resource.<p>
     *
     * @param cms the current users context
     * @param principal the principal that unsubscribes from the resource
     * @param resourcePath the name of the resource to unsubscribe from
     *
     * @throws CmsException if something goes wrong
     */
    public void unsubscribeResourceFor(CmsObject cms, CmsPrincipal principal, String resourcePath) throws CmsException {

        CmsResource resource = cms.readResource(resourcePath, CmsResourceFilter.ALL);
        unsubscribeResourceFor(cms, principal, resource);
    }

    /**
     * Unsubscribes all groups and users from the resource.<p>
     *
     * @param cms the current users context
     * @param resource the resource to unsubscribe all groups and users from
     *
     * @throws CmsException if something goes wrong
     */
    public void unsubscribeResourceForAll(CmsObject cms, CmsResource resource) throws CmsException {

        if (!isEnabled()) {
            throw new CmsRuntimeException(Messages.get().container(Messages.ERR_SUBSCRIPTION_MANAGER_DISABLED_0));
        }
        m_securityManager.unsubscribeResourceForAll(cms.getRequestContext(), getPoolName(), resource);
    }

    /**
     * Unsubscribes all groups and users from the resource.<p>
     *
     * @param cms the current users context
     * @param resourcePath the name of the resource to unsubscribe all groups and users from
     *
     * @throws CmsException if something goes wrong
     */
    public void unsubscribeResourceForAll(CmsObject cms, String resourcePath) throws CmsException {

        CmsResource resource = cms.readResource(resourcePath, CmsResourceFilter.ALL);
        unsubscribeResourceForAll(cms, resource);
    }

}
