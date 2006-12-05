/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/lock/CmsLockManager.java,v $
 * Date   : $Date: 2006/12/05 16:31:07 $
 * Version: $Revision: 1.37.4.13 $
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

package org.opencms.lock;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
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
 * @author Michael Emmerich 
 * @author Thomas Weckert  
 * @author Andreas Zahner  
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.37.4.13 $ 
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.file.CmsObject#getLock(CmsResource)
 * @see org.opencms.lock.CmsLock
 */
public final class CmsLockManager {

    /** The driver manager instance. */
    private CmsDriverManager m_driverManager;

    /** The flag to indicate if the lcoks should be written to the db. */
    private boolean m_isDirty = false;

    /** A map holding all the {@link CmsLock}s. */
    private Map m_locks;

    /**
     * Default constructor, creates a new lock manager.<p>
     * 
     * @param driverManager the driver manager instance 
     */
    public CmsLockManager(CmsDriverManager driverManager) {

        m_locks = Collections.synchronizedMap(new HashMap());
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
     * @throws CmsException if somethong goes wrong
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
        if (!currentLock.isLockableBy(user)) {
            // check type, owner and project for system locks
            // this is required if publishing, or create a workflow for, several siblings
            if (!currentLock.isSystemLock()
                || currentLock.getType() != type
                || !currentLock.isOwnedInProjectBy(user, project)) {
                // display the right message
                CmsMessageContainer message = null;
                if (currentLock.isPublish()) {
                    message = Messages.get().container(
                        Messages.ERR_RESOURCE_LOCKED_FORPUBLISH_1,
                        dbc.getRequestContext().getSitePath(resource));
                } else if (currentLock.isWorkflow()) {
                    message = Messages.get().container(
                        Messages.ERR_RESOURCE_LOCKED_INWORKFLOW_1,
                        dbc.getRequestContext().getSitePath(resource));
                } else if (currentLock.isInherited()) {
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

        boolean needNewLock = true;
        // prevent shared locks get compromised
        if (type.isExclusive()) {
            if (!currentLock.isUnlocked() && !currentLock.isSystemLock()) {
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
        if (resource.isFolder() && !newLock.isSystemLock()) {
            String resourceName = resource.getRootPath();
            Iterator itLocks = new ArrayList(m_locks.values()).iterator(); // prevent CMExceptions
            while (itLocks.hasNext()) {
                CmsLock lock = (CmsLock)itLocks.next();
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
        Iterator itLocks = m_locks.values().iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = (CmsLock)itLocks.next();
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
     * You should check the edition lock by calling{@link CmsLock#getEditionLock()},
     * since you may get a system lock.<p>
     * 
     * @param dbc the current database context
     * @param resource the resource
     * 
     * @return the lock state of the given resource 

     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // resources are never locked in the online project
        // and non-existent resources are never locked
        if ((resource == null) || (dbc.currentProject().getId() == CmsProject.ONLINE_PROJECT_ID)) {
            return CmsLock.getNullLock();
        }

        // check exclusive direct locks first
        CmsLock lock = getDirectLock(resource.getRootPath());
        if (lock.isNullLock()) {
            // check if siblings are exclusively locked
            List siblings = internalReadSiblings(dbc, resource);
            lock = getSiblingsLock(siblings, resource.getRootPath());
        }
        if (lock.isNullLock()) {
            // if there is no parent lock, this will be the null lock as well
            lock = getParentLock(resource.getRootPath());
        }
        return lock;
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
     * @throws CmsException if somehting goes wrong 
     */
    public List getLocks(CmsDbContext dbc, String resourceName, CmsLockFilter filter) throws CmsException {

        List locks = new ArrayList();
        Iterator itLocks = m_locks.values().iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = (CmsLock)itLocks.next();
            if (filter.isSharedExclusive()) {
                CmsResource resource = m_driverManager.readResource(dbc, lock.getResourceName(), CmsResourceFilter.ALL);
                if (resource.getSiblingCount() > 1) {
                    Iterator itSiblings = internalReadSiblings(dbc, resource).iterator();
                    while (itSiblings.hasNext()) {
                        CmsResource sibling = (CmsResource)itSiblings.next();
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
        Iterator itLocks = m_locks.values().iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = (CmsLock)itLocks.next();
            if (!lock.isSystemLock()) {
                // only system locks matter here
                continue;
            }
            if (lock.getResourceName().startsWith(resource.getRootPath())) {
                if (lock.getResourceName().startsWith(resource.getRootPath())) {
                    return true;
                }
                CmsResource lockedResource = m_driverManager.readResource(
                    dbc,
                    lock.getResourceName(),
                    CmsResourceFilter.ALL);
                if (lockedResource.getSiblingCount() > 1) {
                    Iterator itSiblings = internalReadSiblings(dbc, lockedResource).iterator();
                    while (itSiblings.hasNext()) {
                        CmsResource sibling = (CmsResource)itSiblings.next();
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
     * Reads the latest saved locks from the database and installs them to 
     * this lock manager.<p>
     * 
     *  @param dbc the current database context
     *  
     *  @throws CmsException if something goes wrong
     */
    public void readLocks(CmsDbContext dbc) throws CmsException {

        m_locks.clear();
        List locks = m_driverManager.getProjectDriver().readLocks(dbc);
        Iterator itLocks = locks.iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = (CmsLock)itLocks.next();
            internalLockResource(lock);
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
            m_driverManager.getVfsDriver().readResource(dbc, dbc.currentProject().getId(), resourceName, false);
            throw new CmsLockException(Messages.get().container(
                Messages.ERR_REMOVING_UNDELETED_RESOURCE_1,
                dbc.getRequestContext().removeSiteRoot(resourceName)));
        } catch (CmsVfsResourceNotFoundException e) {
            // ok, ignore
        }
        unlockResource(resourceName, true);
        unlockResource(resourceName, false);
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
     * @param unlockSystemLock <code>true</code>, if you also want to remove system locks
     * 
     * @return the previous {@link CmsLock} object of the resource, 
     *          or <code>{@link CmsLock#getNullLock()}</code> if the resource was unlocked
     *
     * @throws CmsException if something goes wrong
     */
    public CmsLock removeResource(CmsDbContext dbc, CmsResource resource, boolean forceUnlock, boolean unlockSystemLock)
    throws CmsException {

        String resourcename = resource.getRootPath();
        CmsLock lock = getLock(dbc, resource);
        if (lock.isSystemLock()) {
            lock = lock.getEditionLock();
        }

        // check some abort conditions first
        if (!lock.isNullLock()) {
            // the resource is locked by another user or in other project
            if (!forceUnlock && (!lock.isOwnedInProjectBy(dbc.currentUser(), dbc.currentProject()))) {
                throw new CmsLockException(Messages.get().container(
                    Messages.ERR_RESOURCE_UNLOCK_1,
                    dbc.removeSiteRoot(resourcename)));
            }

            // sub-resources of a locked folder can't be unlocked
            if (!forceUnlock && lock.isInherited()) {
                throw new CmsLockException(Messages.get().container(
                    Messages.ERR_UNLOCK_LOCK_INHERITED_1,
                    dbc.removeSiteRoot(resourcename)));
            }
        }

        // remove the lock and clean-up stuff
        if (lock.isExclusive()) {
            if (resource.isFolder()) {
                // in case of a folder, remove any exclusive locks on sub-resources that probably have
                // been upgraded from an inherited lock when the user edited a resource                
                Iterator i = m_locks.keySet().iterator();
                String lockedPath = null;

                while (i.hasNext()) {
                    lockedPath = (String)i.next();
                    if (lockedPath.startsWith(resourcename) && !lockedPath.equals(resourcename)) {
                        // remove the exclusive locked sub-resource
                        unlockResource(lockedPath, false);
                    }
                }
            }
            return unlockResource(resourcename, false);
        }

        if (lock.getType().isSharedExclusive()) {
            // when a resource with a shared lock gets unlocked, fetch all siblings of the resource 
            // to the same content record to identify the exclusive locked sibling
            List siblings = internalReadSiblings(dbc, resource);
            for (int i = 0; i < siblings.size(); i++) {
                CmsResource sibling = (CmsResource)siblings.get(i);
                if (m_locks.containsKey(sibling.getRootPath())) {
                    // remove the exclusive locked sibling
                    unlockResource(sibling.getRootPath(), false);
                    break;
                }
            }
            return lock;
        }

        // remove system locks only if explicit required
        if (unlockSystemLock && getLock(dbc, resource).isSystemLock()) {
            return unlockResource(resourcename, true);
        }
        return lock;
    }

    /**
     * Removes all resources locked in a project.<p>
     * 
     * @param projectId the ID of the project where the resources have been locked
     * @param forceUnlock if <code>true</code>, exclusive locks in other projects are removed for resources locked with a system lock
     * @param removeWfLocks if <code>true</code>, workflow locks are removed
     */
    public void removeResourcesInProject(int projectId, boolean forceUnlock, boolean removeWfLocks) {

        Iterator itLocks = new ArrayList(m_locks.values()).iterator(); // prevent CME
        while (itLocks.hasNext()) {
            CmsLock currentLock = (CmsLock)itLocks.next();
            if (currentLock.isSystemLock()) {
                if (!currentLock.getChildLock().isNullLock()) {
                    if (forceUnlock || (currentLock.getChildLock().getProjectId() == projectId)) {
                        unlockResource(currentLock.getResourceName(), false);
                    }
                }
                if (removeWfLocks && currentLock.isWorkflow() && (currentLock.getProjectId() == projectId)) {
                    unlockResource(currentLock.getResourceName(), true);
                }
            } else {
                if (currentLock.getProjectId() == projectId) {
                    unlockResource(currentLock.getResourceName(), false);
                }
            }
        }
    }

    /**
     * Removes all exclusive temporary locks of a user.<p>
     * 
     * @param userId the id of the user whose locks has to be removed
     */
    public void removeTempLocks(CmsUUID userId) {

        Iterator itLocks = m_locks.values().iterator();
        while (itLocks.hasNext()) {
            CmsLock currentLock = (CmsLock)itLocks.next();
            if (currentLock.isTemporary() && currentLock.getUserId().equals(userId)) {
                unlockResource(currentLock.getResourceName(), false);
            }
        }
    }

    /** 
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer buf = new StringBuffer();

        // bring the list of locked resources into a human readable order first
        List lockedResources = new ArrayList(m_locks.keySet());
        Collections.sort(lockedResources);

        // iterate all locks
        Iterator itLocks = lockedResources.iterator();
        while (itLocks.hasNext()) {
            String lockedPath = (String)itLocks.next();
            CmsLock currentLock = (CmsLock)m_locks.get(lockedPath);
            buf.append(currentLock).append("\n");
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

        if (m_isDirty) {
            // write the locks only if really needed
            List locks = new ArrayList(m_locks.values());
            m_driverManager.getProjectDriver().writeLocks(dbc, locks);
            m_isDirty = false;
        }
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        try {
            if (m_locks != null) {
                m_locks.clear();
                m_locks = null;
            }
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
    }

    /**
     * Returns the direct lock of a resource.<p>
     * 
     * @param resourcename the name of the resource
     * 
     * @return the direct lock of the resource or the null lock
     */
    private CmsLock getDirectLock(String resourcename) {

        CmsLock directLock = (CmsLock)m_locks.get(resourcename);
        if (directLock != null) {
            return directLock;
        } else {
            return CmsLock.getNullLock();
        }
    }

    /**
     * Returns the lock of a possible locked parent folder of a resource, system locks are ignored.<p>
     * 
     * @param resourceName the name of the resource
     * 
     * @return the lock of a parent folder, or {@link CmsLock#getNullLock()} if no parent folders are locked by a non system lock
     */
    private CmsLock getParentFolderLock(String resourceName) {

        Iterator itLocks = m_locks.keySet().iterator();
        while (itLocks.hasNext()) {
            String lockedPath = (String)itLocks.next();
            if (lockedPath.endsWith("/") && resourceName.startsWith(lockedPath) && !resourceName.equals(lockedPath)) {
                CmsLock lock = (CmsLock)m_locks.get(lockedPath);
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
    private CmsLock getSiblingsLock(List siblings, String resourcename) {

        for (int i = 0; i < siblings.size(); i++) {
            CmsResource sibling = (CmsResource)siblings.get(i);
            CmsLock exclusiveLock = getDirectLock(sibling.getRootPath());
            if (!exclusiveLock.isNullLock()) {
                // a sibling is already locked 
                return internalSiblingLock(exclusiveLock, resourcename);
            }
        }
        // no locked siblings found
        return CmsLock.getNullLock();

    }

    /**
     * Finally set the given lock.<p>
     * 
     * @param lock the lock to set
     * 
     * @throws CmsLockException if the lock is not compatible with the current lock 
     */
    private void internalLockResource(CmsLock lock) throws CmsLockException {

        CmsLock currentLock = (CmsLock)m_locks.get(lock.getResourceName());
        if (currentLock != null) {
            if (currentLock.equals(lock)) {
                return;
            }
            if (currentLock.isSystemLock() && !lock.isSystemLock()) {
                currentLock.setChildLock(lock);
            } else if (!currentLock.isSystemLock() && lock.isSystemLock()) {
                lock.setChildLock(currentLock);
                m_locks.put(lock.getResourceName(), lock);
            } else {
                throw new CmsLockException(Messages.get().container(
                    Messages.ERR_LOCK_ILLEGAL_STATE_2,
                    currentLock,
                    lock));
            }
        } else {
            m_locks.put(lock.getResourceName(), lock);
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
    private List internalReadSiblings(CmsDbContext dbc, CmsResource resource) throws CmsException {

        // reading siblings using the DriverManager methods while the lock state is checked would
        // result in an infinite loop, therefore we must access the VFS driver directly
        List siblings = m_driverManager.getVfsDriver().readSiblings(dbc, dbc.currentProject(), resource, true);
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

        CmsLock sysLock = null;
        if (exclusiveLock.isSystemLock()) {
            sysLock = new CmsLock(
                siblingName,
                exclusiveLock.getUserId(),
                exclusiveLock.getProject(),
                exclusiveLock.getType());
        }
        if (sysLock == null || !exclusiveLock.getEditionLock().isNullLock()) {
            CmsLockType type = CmsLockType.SHARED_EXCLUSIVE;
            if (!getParentLock(siblingName).isNullLock()) {
                type = CmsLockType.SHARED_INHERITED;
            }
            CmsLock lock = new CmsLock(siblingName, exclusiveLock.getUserId(), exclusiveLock.getProject(), type);
            if (sysLock == null) {
                sysLock = lock;
            } else {
                sysLock.setChildLock(lock);
            }
        }
        return sysLock;
    }

    /**
     * Sets the given lock to the resource.<p>
     * 
     * @param lock the lock to set
     * 
     * @throws CmsLockException if the lock is not compatible with the current lock 
     */
    private void lockResource(CmsLock lock) throws CmsLockException {

        if (!m_isDirty) {
            // read the locks again fresh from DB before changing the state
            try {
                readLocks(new CmsDbContext());
            } catch (CmsException e) {
                // should never happen
                e.printStackTrace();
            }
        }
        m_isDirty = true;
        internalLockResource(lock);
    }

    /**
     * Unlocks the the resource with the given name.<p>
     * 
     * @param resourceName the name of the resource to unlock
     * @param systemLocks <code>true</code> if only system locks should be removed, 
     *              if only exclusive locks should be removed
     * 
     * @return the removed lock object
     */
    private CmsLock unlockResource(String resourceName, boolean systemLocks) {

        if (!m_isDirty) {
            // read the locks again fresh from DB before changing the state
            try {
                readLocks(new CmsDbContext());
            } catch (CmsException e) {
                // should never happen
                e.printStackTrace();
            }
        }
        m_isDirty = true;

        // get the current lock
        CmsLock lock = (CmsLock)m_locks.get(resourceName);
        if (lock == null) {
            return CmsLock.getNullLock();
        }

        // check the lock type (system or user) to remove
        if (systemLocks) {
            if (lock.isSystemLock()) {
                // if a system lock has to be removed
                // user locks are removed too
                m_locks.remove(resourceName);
                return lock;
            } else {
                // if it is a edition lock, do nothing
                return CmsLock.getNullLock();
            }
        } else {
            if (!lock.isSystemLock()) {
                // if it is a edition lock juut remove it
                m_locks.remove(resourceName);
                return lock;
            } else {
                // if it is a system lock check the edition lock
                if (!lock.getChildLock().isNullLock()) {
                    // remove the edition lock
                    CmsLock tmp = lock.getChildLock();
                    lock.setChildLock(CmsLock.getNullLock());
                    return tmp;
                } else {
                    // if there is no edition lock, do nothing
                    return CmsLock.getNullLock();
                }
            }
        }
    }
}