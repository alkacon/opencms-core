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

package org.opencms.lock;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.I_CmsResource;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The CmsLockManager is used by the Cms application to detect
 * the lock state of a resource.<p>
 *
 * The lock state depends on the path of the resource, and probably
 * locked parent folders. The result of a query to the lock manager
 * are instances of CmsLock objects.<p>
 *
 * @since 6.0.0
 *
 * @see org.opencms.file.CmsObject#getLock(CmsResource)
 * @see org.opencms.lock.CmsLock
 */
public final class CmsLockManager {

    /** The driver manager instance. */
    private CmsDriverManager m_driverManager;

    /** The flag to indicate if the locks should be written to the db. */
    private boolean m_isDirty;

    /** The flag to indicate if the lock manager has been started in run level 4. */
    private boolean m_runningInServlet;

    /**
     * Default constructor, creates a new lock manager.<p>
     *
     * @param driverManager the driver manager instance
     */
    public CmsLockManager(CmsDriverManager driverManager) {

        m_driverManager = driverManager;
    }

    /**
     * Adds a resource to the lock manager.<p>
     *
     * @param dbc the current database context
     * @param resource the resource
     * @param user the user who locked the resource
     * @param project the project where the resource is locked
     * @param type the lock type
     *
     * @throws CmsLockException if the resource is locked
     * @throws CmsException if something goes wrong
     */
    public void addResource(CmsDbContext dbc, CmsResource resource, CmsUser user, CmsProject project, CmsLockType type)
    throws CmsLockException, CmsException {

        // check the type
        if (!type.isSystem() && !type.isExclusive()) {
            // invalid type
            throw new CmsLockException(Messages.get().container(Messages.ERR_INVALID_LOCK_TYPE_1, type.toString()));
        }

        // get the current lock
        CmsLock currentLock = getLock(dbc, resource);

        // check lockability
        checkLockable(dbc, resource, user, project, type, currentLock);

        boolean needNewLock = true;
        // prevent shared locks get compromised
        if ((type.isExclusive()) && !(type.isTemporary() && currentLock.isInherited())) {
            if (!currentLock.getEditionLock().isUnlocked()) {
                needNewLock = false;
            }
        }

        CmsLock newLock = CmsLock.getNullLock();
        if (needNewLock) {
            // lock the resource
            newLock = new CmsLock(resource.getRootPath(), user.getId(), project, type);
            lockResource(newLock);
        }

        // handle collisions with exclusive locked sub-resources in case of a folder
        if (resource.isFolder() && newLock.getSystemLock().isUnlocked()) {
            String resourceName = resource.getRootPath();
            Iterator<CmsLock> itLocks = OpenCms.getMemoryMonitor().getAllCachedLocks().iterator();
            while (itLocks.hasNext()) {
                CmsLock lock = itLocks.next();
                String lockedPath = lock.getResourceName();
                if (lockedPath.startsWith(resourceName) && !lockedPath.equals(resourceName)) {
                    unlockResource(lockedPath, false);
                }
            }
        }
    }

    /**
     * Counts the exclusive locked resources in a project.<p>
     *
     * @param project the project
     *
     * @return the number of exclusive locked resources in the specified project
     */
    public int countExclusiveLocksInProject(CmsProject project) {

        int count = 0;
        Iterator<CmsLock> itLocks = OpenCms.getMemoryMonitor().getAllCachedLocks().iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = itLocks.next();
            if (lock.getEditionLock().isInProject(project)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the lock state of the given resource.<p>
     *
     * In case no lock is set, the <code>null lock</code> which can be obtained
     * by {@link CmsLock#getNullLock()} is returned.<p>
     *
     * @param dbc the current database context
     * @param resource the resource
     *
     * @return the lock state of the given resource

     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsDbContext dbc, CmsResource resource) throws CmsException {

        return getLock(dbc, resource, true);
    }

    /**
     * Returns the lock state of the given resource.<p>
     *
     * In case no lock is set, the <code>null lock</code> which can be obtained
     * by {@link CmsLock#getNullLock()} is returned.<p>
     *
     * @param dbc the current database context
     * @param resource the resource
     * @param includeSiblings if siblings (shared locks) should be included in the search
     *
     * @return the lock state of the given resource

     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsDbContext dbc, CmsResource resource, boolean includeSiblings) throws CmsException {

        // resources are never locked in the online project
        // and non-existent resources are never locked
        if ((resource == null) || (dbc.currentProject().isOnlineProject())) {
            return CmsLock.getNullLock();
        }

        // check exclusive direct locks first
        CmsLock lock = getDirectLock(resource.getRootPath());
        if ((lock == null) && includeSiblings) {
            // check if siblings are exclusively locked
            List<CmsResource> siblings = internalReadSiblings(dbc, resource);
            lock = getSiblingsLock(siblings, resource.getRootPath());
        }
        if (lock == null) {
            // if there is no parent lock, this will be the null lock as well
            lock = getParentLock(resource.getRootPath());
        }
        if (!lock.getSystemLock().isUnlocked()) {
            lock = lock.getSystemLock();
        } else {
            lock = lock.getEditionLock();
        }
        return lock;
    }

    /**
     * Returns all exclusive locked resources matching the given resource and filter.<p>
     *
     * @param dbc the database context
     * @param resource the resource
     * @param filter the lock filter
     *
     * @return a list of resources
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> getLockedResources(CmsDbContext dbc, CmsResource resource, CmsLockFilter filter)
    throws CmsException {

        List<CmsResource> lockedResources = new ArrayList<CmsResource>();
        Iterator<CmsLock> itLocks = OpenCms.getMemoryMonitor().getAllCachedLocks().iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = itLocks.next();
            CmsResource lockedResource;
            boolean matchesFilter = filter.match(resource.getRootPath(), lock);
            if (!matchesFilter && !filter.isSharedExclusive()) {
                // we don't need to read the resource if the filter didn't match and we don't need to look at the siblings
                continue;
            }
            try {
                lockedResource = m_driverManager.readResource(dbc, lock.getResourceName(), CmsResourceFilter.ALL);
            } catch (CmsVfsResourceNotFoundException e) {
                OpenCms.getMemoryMonitor().uncacheLock(lock.getResourceName());
                continue;
            }
            if (filter.isSharedExclusive() && (lockedResource.getSiblingCount() > 1)) {
                Iterator<CmsResource> itSiblings = internalReadSiblings(dbc, lockedResource).iterator();
                while (itSiblings.hasNext()) {
                    CmsResource sibling = itSiblings.next();
                    CmsLock siblingLock = internalSiblingLock(lock, sibling.getRootPath());
                    if (filter.match(resource.getRootPath(), siblingLock)) {
                        lockedResources.add(sibling);
                    }
                }
            }
            if (matchesFilter) {
                lockedResources.add(lockedResource);
            }
        }
        Collections.sort(lockedResources, I_CmsResource.COMPARE_ROOT_PATH);
        return lockedResources;
    }

    /**
     * Returns all exclusive locked resources matching the given resource and filter, but uses a cache for resource loookups.<p>
     *
     * @param dbc the database context
     * @param resource the resource
     * @param filter the lock filter
     * @param cache a cache to use for resource lookups
     *
     * @return a list of resources
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsResource> getLockedResourcesWithCache(
        CmsDbContext dbc,
        CmsResource resource,
        CmsLockFilter filter,
        Map<String, CmsResource> cache) throws CmsException {

        List<CmsResource> lockedResources = new ArrayList<CmsResource>();
        Iterator<CmsLock> itLocks = OpenCms.getMemoryMonitor().getAllCachedLocks().iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = itLocks.next();
            CmsResource lockedResource;
            boolean matchesFilter = filter.match(resource.getRootPath(), lock);
            if (!matchesFilter && !filter.isSharedExclusive()) {
                // we don't need to read the resource if the filter didn't match and we don't need to look at the siblings
                continue;
            }
            lockedResource = cache.get(lock.getResourceName());
            if (lockedResource == null) {
                try {
                    lockedResource = m_driverManager.readResource(dbc, lock.getResourceName(), CmsResourceFilter.ALL);
                    cache.put(lock.getResourceName(), lockedResource);
                } catch (CmsVfsResourceNotFoundException e) {
                    OpenCms.getMemoryMonitor().uncacheLock(lock.getResourceName());
                    // we put a dummy resource object in the map so we won't need to read the nonexistent resource again
                    CmsResource dummy = new CmsResource(
                        null,
                        null,
                        "",
                        0,
                        false,
                        0,
                        null,
                        null,
                        0,
                        null,
                        0,
                        null,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0);
                    cache.put(lock.getResourceName(), dummy);
                    continue;
                }
            } else if (lockedResource.getStructureId() == null) {
                // dummy resource, i.e. the resource was not found in a previous readResource call
                continue;
            }
            if (filter.isSharedExclusive() && (lockedResource != null) && (lockedResource.getSiblingCount() > 1)) {
                Iterator<CmsResource> itSiblings = internalReadSiblings(dbc, lockedResource).iterator();
                while (itSiblings.hasNext()) {
                    CmsResource sibling = itSiblings.next();
                    CmsLock siblingLock = internalSiblingLock(lock, sibling.getRootPath());
                    if (filter.match(resource.getRootPath(), siblingLock)) {
                        lockedResources.add(sibling);
                    }
                }
            }
            if (matchesFilter) {
                lockedResources.add(lockedResource);
            }
        }
        Collections.sort(lockedResources, I_CmsResource.COMPARE_ROOT_PATH);
        return lockedResources;
    }

    /**
     * Returns all exclusive locked resources matching the given resource name and filter.<p>
     *
     * @param dbc the database context
     * @param resourceName the resource name
     * @param filter the lock filter
     *
     * @return a list of root paths
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsLock> getLocks(CmsDbContext dbc, String resourceName, CmsLockFilter filter) throws CmsException {

        List<CmsLock> locks = new ArrayList<CmsLock>();
        Iterator<CmsLock> itLocks = OpenCms.getMemoryMonitor().getAllCachedLocks().iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = itLocks.next();
            if (filter.isSharedExclusive()) {
                CmsResource resource;
                try {
                    resource = m_driverManager.readResource(dbc, lock.getResourceName(), CmsResourceFilter.ALL);
                } catch (CmsVfsResourceNotFoundException e) {
                    OpenCms.getMemoryMonitor().uncacheLock(lock.getResourceName());
                    continue;
                }
                if (resource.getSiblingCount() > 1) {
                    Iterator<CmsResource> itSiblings = internalReadSiblings(dbc, resource).iterator();
                    while (itSiblings.hasNext()) {
                        CmsResource sibling = itSiblings.next();
                        CmsLock siblingLock = internalSiblingLock(lock, sibling.getRootPath());
                        if (filter.match(resourceName, siblingLock)) {
                            locks.add(siblingLock);
                        }
                    }
                }
            }
            if (filter.match(resourceName, lock)) {
                locks.add(lock);
            }
        }
        return locks;
    }

    /**
     * Returns <code>true</code> if the given resource contains a resource that has a system lock.<p>
     *
     * This check is required for certain operations on folders.<p>
     *
     * @param dbc the database context
     * @param resource the resource to check the system locks for
     *
     * @return <code>true</code> if the given resource contains a resource that has a system lock
     *
     * @throws CmsException if something goes wrong
     */
    public boolean hasSystemLocks(CmsDbContext dbc, CmsResource resource) throws CmsException {

        if (resource == null) {
            return false;
        }
        Iterator<CmsLock> itLocks = OpenCms.getMemoryMonitor().getAllCachedLocks().iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = itLocks.next();
            if (lock.getSystemLock().isUnlocked()) {
                // only system locks matter here
                continue;
            }
            if (lock.getResourceName().startsWith(resource.getRootPath())) {
                if (lock.getResourceName().startsWith(resource.getRootPath())) {
                    return true;
                }
                try {
                    resource = m_driverManager.readResource(dbc, lock.getResourceName(), CmsResourceFilter.ALL);
                } catch (CmsVfsResourceNotFoundException e) {
                    OpenCms.getMemoryMonitor().uncacheLock(lock.getResourceName());
                    continue;
                }
                CmsResource lockedResource;
                try {
                    lockedResource = m_driverManager.readResource(dbc, lock.getResourceName(), CmsResourceFilter.ALL);
                } catch (CmsVfsResourceNotFoundException e) {
                    OpenCms.getMemoryMonitor().uncacheLock(lock.getResourceName());
                    continue;
                }
                if (lockedResource.getSiblingCount() > 1) {
                    Iterator<CmsResource> itSiblings = internalReadSiblings(dbc, lockedResource).iterator();
                    while (itSiblings.hasNext()) {
                        CmsResource sibling = itSiblings.next();
                        CmsLock siblingLock = internalSiblingLock(lock, sibling.getRootPath());
                        if (siblingLock.getResourceName().startsWith(resource.getRootPath())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Moves a lock during the move resource operation.<p>
     *
     * @param source the source root path
     * @param destination the destination root path
     */
    public void moveResource(String source, String destination) {

        CmsLock lock = OpenCms.getMemoryMonitor().getCachedLock(source);
        if (lock != null) {
            OpenCms.getMemoryMonitor().uncacheLock(lock.getResourceName());
            CmsLock newLock = new CmsLock(destination, lock.getUserId(), lock.getProject(), lock.getType());
            lock = lock.getRelatedLock();
            if ((lock != null) && !lock.isNullLock()) {
                CmsLock relatedLock = new CmsLock(destination, lock.getUserId(), lock.getProject(), lock.getType());
                newLock.setRelatedLock(relatedLock);
            }
            OpenCms.getMemoryMonitor().cacheLock(newLock);
        }
    }

    /**
     * Reads the latest saved locks from the database and installs them to
     * this lock manager.<p>
     *
     *  @param dbc the current database context
     *
     *  @throws CmsException if something goes wrong
     */
    public void readLocks(CmsDbContext dbc) throws CmsException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            // read the locks only if the wizard is not enabled
            Map<String, CmsLock> lockCache = new HashMap<String, CmsLock>();
            List<CmsLock> locks = m_driverManager.getProjectDriver(dbc).readLocks(dbc);
            Iterator<CmsLock> itLocks = locks.iterator();
            while (itLocks.hasNext()) {
                CmsLock lock = itLocks.next();
                internalLockResource(lock, lockCache);
            }
            OpenCms.getMemoryMonitor().flushLocks(lockCache);
            m_runningInServlet = true;
        }
    }

    /**
     * Removes a resource after it has been deleted by the driver manager.<p>
     *
     * @param dbc the current database context
     * @param resourceName the root path of the deleted resource
     * @throws CmsException if something goes wrong
     */
    public void removeDeletedResource(CmsDbContext dbc, String resourceName) throws CmsException {

        try {
            m_driverManager.getVfsDriver(dbc).readResource(dbc, dbc.currentProject().getUuid(), resourceName, false);
            throw new CmsLockException(
                Messages.get().container(
                    Messages.ERR_REMOVING_UNDELETED_RESOURCE_1,
                    dbc.getRequestContext().removeSiteRoot(resourceName)));
        } catch (CmsVfsResourceNotFoundException e) {
            // ok, ignore
        }
        unlockResource(resourceName, true);
        unlockResource(resourceName, false);
    }

    /**
     * Removes all locks of a user.<p>
     *
     * Edition and system locks are removed.<p>
     *
     * @param userId the id of the user whose locks should be removed
     */
    public void removeLocks(CmsUUID userId) {

        Iterator<CmsLock> itLocks = OpenCms.getMemoryMonitor().getAllCachedLocks().iterator();
        while (itLocks.hasNext()) {
            CmsLock currentLock = itLocks.next();
            boolean editLock = currentLock.getEditionLock().getUserId().equals(userId);
            boolean sysLock = currentLock.getSystemLock().getUserId().equals(userId);
            if (editLock) {
                unlockResource(currentLock.getResourceName(), false);
            }
            if (sysLock) {
                unlockResource(currentLock.getResourceName(), true);
            }
        }
    }

    /**
     * Removes a resource from the lock manager.<p>
     *
     * The forceUnlock option should be used with caution.<br>
     * forceUnlock will remove the lock by ignoring any rules which may cause wrong lock states.<p>
     *
     * @param dbc the current database context
     * @param resource the resource
     * @param forceUnlock <code>true</code>, if a resource is forced to get unlocked (only edition locks),
     *                    no matter by which user and in which project the resource is currently locked
     * @param removeSystemLock <code>true</code>, if you also want to remove system locks
     *
     * @return the previous {@link CmsLock} object of the resource,
     *          or <code>{@link CmsLock#getNullLock()}</code> if the resource was unlocked
     *
     * @throws CmsException if something goes wrong
     */
    public CmsLock removeResource(CmsDbContext dbc, CmsResource resource, boolean forceUnlock, boolean removeSystemLock)
    throws CmsException {

        String resourcename = resource.getRootPath();
        CmsLock lock = getLock(dbc, resource).getEditionLock();

        // check some abort conditions first
        if (!lock.isNullLock()) {
            // the resource is locked by another user or in other project
            if (!forceUnlock && (!lock.isOwnedInProjectBy(dbc.currentUser(), dbc.currentProject()))) {
                throw new CmsLockException(
                    Messages.get().container(Messages.ERR_RESOURCE_UNLOCK_1, dbc.removeSiteRoot(resourcename)));
            }

            // sub-resources of a locked folder can't be unlocked
            if (!forceUnlock && lock.isInherited()) {
                throw new CmsLockException(
                    Messages.get().container(Messages.ERR_UNLOCK_LOCK_INHERITED_1, dbc.removeSiteRoot(resourcename)));
            }
        }

        // remove the lock and clean-up stuff
        if (lock.isExclusive()) {
            if (resource.isFolder()) {
                // in case of a folder, remove any exclusive locks on sub-resources that probably have
                // been upgraded from an inherited lock when the user edited a resource
                Iterator<CmsLock> itLocks = OpenCms.getMemoryMonitor().getAllCachedLocks().iterator();
                while (itLocks.hasNext()) {
                    String lockedPath = (itLocks.next()).getResourceName();
                    if (lockedPath.startsWith(resourcename) && !lockedPath.equals(resourcename)) {
                        // remove the exclusive locked sub-resource
                        unlockResource(lockedPath, false);
                    }
                }
            }
            if (removeSystemLock) {
                unlockResource(resourcename, true);
            }
            unlockResource(resourcename, false);
            return lock;
        }

        if (lock.getType().isSharedExclusive()) {
            List<String> locks = OpenCms.getMemoryMonitor().getAllCachedLockPaths();
            // when a resource with a shared lock gets unlocked, fetch all siblings of the resource
            // to the same content record to identify the exclusive locked sibling
            List<CmsResource> siblings = internalReadSiblings(dbc, resource);
            for (int i = 0; i < siblings.size(); i++) {
                CmsResource sibling = siblings.get(i);
                if (locks.contains(sibling.getRootPath())) {
                    // remove the exclusive locked sibling
                    if (removeSystemLock) {
                        unlockResource(sibling.getRootPath(), true);
                    }
                    unlockResource(sibling.getRootPath(), false);
                    break; // it can only be one!
                }
            }
            return lock;
        }

        // remove system locks only if explicit required
        if (removeSystemLock && !getLock(dbc, resource).getSystemLock().isUnlocked()) {
            return unlockResource(resourcename, true);
        }
        return lock;
    }

    /**
     * Removes all resources locked in a project.<p>
     *
     * @param projectId the ID of the project where the resources have been locked
     * @param removeSystemLocks if <code>true</code>, also system locks are removed
     */
    public void removeResourcesInProject(CmsUUID projectId, boolean removeSystemLocks) {

        Iterator<CmsLock> itLocks = OpenCms.getMemoryMonitor().getAllCachedLocks().iterator();
        while (itLocks.hasNext()) {
            CmsLock currentLock = itLocks.next();
            if (removeSystemLocks && currentLock.getSystemLock().getProjectId().equals(projectId)) {
                unlockResource(currentLock.getResourceName(), true);
            }
            if (currentLock.getEditionLock().getProjectId().equals(projectId)) {
                unlockResource(currentLock.getResourceName(), false);
            }
        }
    }

    /**
     * Removes all exclusive temporary locks of a user.<p>
     *
     * Only edition lock can be temporary, so no system locks are removed.<p>
     *
     * @param userId the id of the user whose locks has to be removed
     */
    public void removeTempLocks(CmsUUID userId) {

        Iterator<CmsLock> itLocks = OpenCms.getMemoryMonitor().getAllCachedLocks().iterator();
        while (itLocks.hasNext()) {
            CmsLock currentLock = itLocks.next();
            if (currentLock.isTemporary() && currentLock.getUserId().equals(userId)) {
                unlockResource(currentLock.getResourceName(), false);
            }
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer buf = new StringBuffer();

        // bring the list of locked resources into a human readable order first
        List<CmsLock> lockedResources = OpenCms.getMemoryMonitor().getAllCachedLocks();
        Collections.sort(lockedResources);

        // iterate all locks
        Iterator<CmsLock> itLocks = lockedResources.iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = itLocks.next();
            buf.append(lock).append("\n");
        }
        return buf.toString();
    }

    /**
     * Writes the locks that are currently stored in-memory to the database to allow restoring them in
     * later startups.<p>
     *
     * This overwrites the locks previously stored in the underlying database table.<p>
     *
     *  @param dbc the current database context
     *
     *  @throws CmsException if something goes wrong
     */
    public void writeLocks(CmsDbContext dbc) throws CmsException {

        if (m_isDirty // only if something changed
            && m_runningInServlet // only if started in run level 4
            && OpenCms.getMemoryMonitor().requiresPersistency()) { // only if persistency is required

            List<CmsLock> locks = OpenCms.getMemoryMonitor().getAllCachedLocks();
            m_driverManager.getProjectDriver(dbc).writeLocks(dbc, locks);
            m_isDirty = false;
        }
    }

    /**
     * Checks if the given resource is lockable by the given user/project/lock type.<p>
     *
     * @param dbc just to get the site path of the resource
     * @param resource the resource to check lockability for
     * @param user the user to check
     * @param project the project to check
     * @param type the lock type to check
     * @param currentLock the resource current lock
     *
     * @throws CmsLockException if resource is not lockable
     */
    private void checkLockable(
        CmsDbContext dbc,
        CmsResource resource,
        CmsUser user,
        CmsProject project,
        CmsLockType type,
        CmsLock currentLock) throws CmsLockException {

        if (!currentLock.isLockableBy(user)) {
            // check type, owner and project for system locks
            // this is required if publishing several siblings
            if (currentLock.getSystemLock().isUnlocked()
                || (currentLock.getType() != type)
                || !currentLock.isOwnedInProjectBy(user, project)) {
                // display the right message
                CmsMessageContainer message = null;
                if (currentLock.getSystemLock().isPublish()) {
                    message = Messages.get().container(
                        Messages.ERR_RESOURCE_LOCKED_FORPUBLISH_1,
                        dbc.getRequestContext().getSitePath(resource));
                } else if (currentLock.getEditionLock().isInherited()) {
                    message = Messages.get().container(
                        Messages.ERR_RESOURCE_LOCKED_INHERITED_1,
                        dbc.getRequestContext().getSitePath(resource));
                } else {
                    message = Messages.get().container(
                        Messages.ERR_RESOURCE_LOCKED_BYOTHERUSER_1,
                        dbc.getRequestContext().getSitePath(resource));
                }
                throw new CmsLockException(message);
            }
        }
    }

    /**
     * Returns the direct lock of a resource.<p>
     *
     * @param resourcename the name of the resource
     *
     * @return the direct lock of the resource or <code>null</code>
     */
    private CmsLock getDirectLock(String resourcename) {

        return OpenCms.getMemoryMonitor().getCachedLock(resourcename);
    }

    /**
     * Returns the lock of a possible locked parent folder of a resource, system locks are ignored.<p>
     *
     * @param resourceName the name of the resource
     *
     * @return the lock of a parent folder, or {@link CmsLock#getNullLock()} if no parent folders are locked by a non system lock
     */
    private CmsLock getParentFolderLock(String resourceName) {

        Iterator<CmsLock> itLocks = OpenCms.getMemoryMonitor().getAllCachedLocks().iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = itLocks.next();
            if (lock.getResourceName().endsWith("/")
                && resourceName.startsWith(lock.getResourceName())
                && !resourceName.equals(lock.getResourceName())) {
                // system locks does not get inherited
                lock = lock.getEditionLock();
                // check the lock
                if (!lock.isUnlocked()) {
                    return lock;
                }
            }
        }
        return CmsLock.getNullLock();
    }

    /**
     * Returns the inherited lock of a resource.<p>
     *
     * @param resourcename the name of the resource
     * @return the inherited lock or the null lock
     */
    private CmsLock getParentLock(String resourcename) {

        CmsLock parentFolderLock = getParentFolderLock(resourcename);
        if (!parentFolderLock.isNullLock()) {
            return new CmsLock(
                resourcename,
                parentFolderLock.getUserId(),
                parentFolderLock.getProject(),
                CmsLockType.INHERITED);
        }
        return CmsLock.getNullLock();
    }

    /**
     * Returns the indirect lock of a resource depending on siblings lock state.<p>
     *
     * @param siblings the list of siblings
     * @param resourcename the name of the resource
     *
     * @return the indirect lock of the resource or the null lock
     */
    private CmsLock getSiblingsLock(List<CmsResource> siblings, String resourcename) {

        for (int i = 0; i < siblings.size(); i++) {
            CmsResource sibling = siblings.get(i);
            CmsLock exclusiveLock = getDirectLock(sibling.getRootPath());
            if (exclusiveLock != null) {
                // a sibling is already locked
                return internalSiblingLock(exclusiveLock, resourcename);
            }
        }
        // no locked siblings found
        return null;

    }

    /**
     * Finally set the given lock.<p>
     *
     * @param lock the lock to set
     * @param locks during reading the locks from db we need to operate on an extra map
     *
     * @throws CmsLockException if the lock is not compatible with the current lock
     */
    private void internalLockResource(CmsLock lock, Map<String, CmsLock> locks) throws CmsLockException {

        CmsLock currentLock = null;
        if (locks == null) {
            currentLock = OpenCms.getMemoryMonitor().getCachedLock(lock.getResourceName());
        } else {
            currentLock = locks.get(lock.getResourceName());
        }
        if (currentLock != null) {
            if (currentLock.getSystemLock().equals(lock) || currentLock.getEditionLock().equals(lock)) {
                return;
            }
            if (!currentLock.getSystemLock().isUnlocked() && lock.getSystemLock().isUnlocked()) {
                lock.setRelatedLock(currentLock);
                if (locks == null) {
                    OpenCms.getMemoryMonitor().cacheLock(lock);
                } else {
                    locks.put(lock.getResourceName(), lock);
                }
            } else if (currentLock.getSystemLock().isUnlocked() && !lock.getSystemLock().isUnlocked()) {
                currentLock.setRelatedLock(lock);
            } else {
                throw new CmsLockException(
                    Messages.get().container(Messages.ERR_LOCK_ILLEGAL_STATE_2, currentLock, lock));
            }
        } else {
            if (locks == null) {
                OpenCms.getMemoryMonitor().cacheLock(lock);
            } else {
                locks.put(lock.getResourceName(), lock);
            }
        }
    }

    /**
     * Reads all siblings from a given resource.<p>
     *
     * The result is a list of <code>{@link CmsResource}</code> objects.
     * It does NOT contain the resource itself, only the siblings of the resource.<p>
     *
     * @param dbc the current database context
     * @param resource the resource to find all siblings from
     *
     * @return a list of <code>{@link CmsResource}</code> Objects that
     *          are siblings to the specified resource,
     *          excluding the specified resource itself
     *
     * @throws CmsException if something goes wrong
     */
    private List<CmsResource> internalReadSiblings(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // reading siblings using the DriverManager methods while the lock state is checked would
        // result in an infinite loop, therefore we must access the VFS driver directly
        List<CmsResource> siblings = m_driverManager.getVfsDriver(dbc).readSiblings(
            dbc,
            dbc.currentProject().getUuid(),
            resource,
            true);
        siblings.remove(resource);
        return siblings;
    }

    /**
     * Returns a shared lock for the given excclusive lock and sibling.<p>
     *
     * @param exclusiveLock the exclusive lock to use (has to be set on a sibling of siblingName)
     * @param siblingName the siblings name
     *
     * @return the shared lock
     */
    private CmsLock internalSiblingLock(CmsLock exclusiveLock, String siblingName) {

        CmsLock lock = null;
        if (!exclusiveLock.getSystemLock().isUnlocked()) {
            lock = new CmsLock(
                siblingName,
                exclusiveLock.getUserId(),
                exclusiveLock.getProject(),
                exclusiveLock.getSystemLock().getType());
        }
        if ((lock == null) || !exclusiveLock.getEditionLock().isNullLock()) {
            CmsLockType type = CmsLockType.SHARED_EXCLUSIVE;
            if (!getParentLock(siblingName).isNullLock()) {
                type = CmsLockType.SHARED_INHERITED;
            }
            if (lock == null) {
                lock = new CmsLock(siblingName, exclusiveLock.getUserId(), exclusiveLock.getProject(), type);
            } else {
                CmsLock editionLock = new CmsLock(
                    siblingName,
                    exclusiveLock.getUserId(),
                    exclusiveLock.getProject(),
                    type);
                lock.setRelatedLock(editionLock);
            }
        }
        return lock;
    }

    /**
     * Sets the given lock to the resource.<p>
     *
     * @param lock the lock to set
     *
     * @throws CmsLockException if the lock is not compatible with the current lock
     */
    private void lockResource(CmsLock lock) throws CmsLockException {

        m_isDirty = true;
        internalLockResource(lock, null);
    }

    /**
     * Unlocks the the resource with the given name.<p>
     *
     * @param resourceName the name of the resource to unlock
     * @param systemLocks <code>true</code> if only system locks should be removed,
     *              and <code>false</code> if only exclusive locks should be removed
     *
     * @return the removed lock object
     */
    private CmsLock unlockResource(String resourceName, boolean systemLocks) {

        m_isDirty = true;

        // get the current lock
        CmsLock lock = OpenCms.getMemoryMonitor().getCachedLock(resourceName);
        if (lock == null) {
            return CmsLock.getNullLock();
        }

        // check the lock type (system or user) to remove
        if (systemLocks) {
            if (!lock.getSystemLock().isUnlocked()) {
                // if a system lock has to be removed
                // user locks are removed too
                OpenCms.getMemoryMonitor().uncacheLock(resourceName);
                return lock;
            } else {
                // if it is a edition lock, do nothing
                return CmsLock.getNullLock();
            }
        } else {
            if (lock.getSystemLock().isUnlocked()) {
                // if it is just an edition lock just remove it
                OpenCms.getMemoryMonitor().uncacheLock(resourceName);
                return lock;
            } else {
                // if it is a system lock check the edition lock
                if (!lock.getEditionLock().isUnlocked()) {
                    // remove the edition lock
                    CmsLock tmp = lock.getEditionLock();
                    CmsLock sysLock = lock.getSystemLock();
                    sysLock.setRelatedLock(null);
                    if (!sysLock.equals(lock)) {
                        // replace the lock entry if needed
                        OpenCms.getMemoryMonitor().cacheLock(sysLock);
                    }
                    return tmp;
                } else {
                    // if there is no edition lock, only a system lock, do nothing
                    return CmsLock.getNullLock();
                }
            }
        }
    }
}