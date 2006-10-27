/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/lock/CmsLockManager.java,v $
 * Date   : $Date: 2006/10/27 12:38:22 $
 * Version: $Revision: 1.37.4.10 $
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
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsMessageContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.workflow.I_CmsWorkflowManager;

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
 * 
 * @version $Revision: 1.37.4.10 $ 
 * 
 * @since 6.0.0 
 * 
 * @see org.opencms.file.CmsObject#getLock(CmsResource)
 * @see org.opencms.lock.CmsLock
 */
public final class CmsLockManager {

    /** A map holding the exclusive CmsLocks. */
    private Map m_exclusiveLocks;

    /** The flag to indicate if the lcoks should be written to the db. */
    private boolean m_isDirty = false;

    /** Indicates if the workflow manager is enabled or not. */
    private boolean m_isWorkflowEnabled;

    /** A map holding the workflow CmsLocks. */
    private Map m_workflowLocks;

    /** The reference to the (optional) workflow manager. */
    private I_CmsWorkflowManager m_workflowManager;

    /**
     * Default constructor, creates a new lock manager.<p>
     */
    public CmsLockManager() {

        m_exclusiveLocks = Collections.synchronizedMap(new HashMap());
        try {
            m_workflowManager = OpenCms.getWorkflowManager();
            m_isWorkflowEnabled = true;
            m_workflowLocks = Collections.synchronizedMap(new HashMap());
        } catch (CmsRuntimeException e) {
            m_isWorkflowEnabled = false;
        }
    }

    /**
     * Adds a resource to the lock manager.<p>
     * 
     * @param driverManager the driver manager
     * @param dbc the current database context
     * @param resource the resource
     * @param user the user who locked the resource
     * @param project the project where the resource is locked
     * @param type the lock type
     * 
     * @throws CmsLockException if the resource is locked
     * @throws CmsException if somethong goes wrong
     */
    public void addResource(
        CmsDriverManager driverManager,
        CmsDbContext dbc,
        CmsResource resource,
        CmsUser user,
        CmsProject project,
        CmsLockType type) throws CmsLockException, CmsException {

        CmsLock lock = getLock(driverManager, dbc, resource);
        String resourceName = resource.getRootPath();

        if (!isLockableByUser(driverManager, dbc, lock, user)) {
            CmsMessageContainer message = null;
            if (lock.isWorkflow()) {
                message = Messages.get().container(
                    Messages.ERR_RESOURCE_LOCKED_INWORKFLOW_1,
                    dbc.getRequestContext().getSitePath(resource));
            } else if (lock.isInherited()) {
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

        if ((type == CmsLockType.EXCLUSIVE) || (type == CmsLockType.TEMPORARY)) {
            if (lock.isNullLock() || (lock.getType() == CmsLockType.WORKFLOW)) {
                // create a new exclusive lock unless the resource has already a shared lock due to a
                // exclusive locked sibling
                CmsLock newLock = new CmsLock(resourceName, user.getId(), project, type);
                lockResource(driverManager, resourceName, newLock);
            }
        } else if (m_isWorkflowEnabled && (type == CmsLockType.WORKFLOW)) {
            // create a new lock if the resource should be locked in a workflow
            CmsLock newLock = new CmsLock(resourceName, user.getId(), project, type);
            lockResource(driverManager, resourceName, newLock);
        } else {
            throw new CmsLockException(Messages.get().container(Messages.ERR_INVALID_LOCK_TYPE_1, type.toString()));
        }

        // handle collisions with exclusive locked sub-resources in case of a folder
        if (CmsResource.isFolder(resourceName)) {
            Iterator i = m_exclusiveLocks.keySet().iterator();
            String lockedPath = null;

            while (i.hasNext()) {
                lockedPath = (String)i.next();

                if (lockedPath.startsWith(resourceName) && !lockedPath.equals(resourceName)) {
                    i.remove();
                    unlockResource(driverManager, lockedPath, false);
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

        Iterator i = m_exclusiveLocks.values().iterator();
        CmsLock lock = null;
        int count = 0;

        while (i.hasNext()) {
            lock = (CmsLock)i.next();
            if (lock.isInProject(project)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns all exclusive locked resources matching the given resource name and filter.<p>
     * 
     * @param resourceName the resource name
     * @param filter the lock filter
     * 
     * @return a list of root paths
     */
    public List getLocks(String resourceName, CmsLockFilter filter) {

        List locks = new ArrayList();
        Iterator itLocks = m_exclusiveLocks.values().iterator();
        while (itLocks.hasNext()) {
            CmsLock lock = (CmsLock)itLocks.next();
            if (filter.match(resourceName, lock)) {
                locks.add(lock);
            }
        }
        if (m_isWorkflowEnabled) {
            Iterator itWfLocks = m_workflowLocks.values().iterator();
            while (itWfLocks.hasNext()) {
                CmsLock lock = (CmsLock)itWfLocks.next();
                if (filter.match(resourceName, lock)) {
                    locks.add(lock);
                }
            }
        }
        return locks;
    }

    /**
     * Returns the lock state of the given resource.<p>
     * 
     * In case no lock is set, the <code>null lock</code> which can be obtained by {@link CmsLock#getNullLock()}
     * is returned.<p> 
     * 
     * @param driverManager the driver manager
     * @param dbc the current database context
     * @param resource the resource
     * 
     * @return the lock state of the given resource 

     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsDriverManager driverManager, CmsDbContext dbc, CmsResource resource) throws CmsException {

        // check some abort conditions first
        if (resource == null) {
            // non-existent resources are never locked
            return CmsLock.getNullLock();
        }
        if (dbc.currentProject().getId() == CmsProject.ONLINE_PROJECT_ID) {
            // resources are never locked in the online project
            return CmsLock.getNullLock();
        }

        String resourcename = resource.getRootPath();
        CmsLock lock = CmsLock.getNullLock();
        CmsLock parentLock = getParentLock(resourcename);
        List siblings = null;

        // check exclusive direct locks first
        lock = getDirectLock(resourcename, CmsLockType.EXCLUSIVE);

        if (lock.isNullLock()) {
            // check if siblings are exclusively locked
            siblings = internalReadSiblings(driverManager, dbc, resource);
            lock = getSiblingsLock(
                siblings,
                resourcename,
                CmsLockType.EXCLUSIVE,
                (parentLock.isNullLock()) ? CmsLockType.SHARED_EXCLUSIVE : CmsLockType.SHARED_INHERITED);
        }

        if (lock.isNullLock()) {
            // if there is no parent lock, this will be the null lock as well
            lock = parentLock;
        }

        if (m_isWorkflowEnabled) {
            if (lock.isNullLock()) {
                // check workflow locks
                lock = getDirectLock(resourcename, CmsLockType.WORKFLOW);
            }

            if (lock.isNullLock()) {
                // check indirekt workflow locks
                lock = getSiblingsLock(siblings, resourcename, CmsLockType.WORKFLOW, CmsLockType.WORKFLOW);
            }
        }

        return lock;
    }

    /**
     * Returns the workflow lock for a resource.<p>
     * 
     * For this method, the semantic used by {@link #getLock(CmsDriverManager, CmsDbContext, CmsResource)} is changed.
     * For the various possible lock types (i.e. exclusive, workflow, indirect, inherited)
     * the direct/indirect workflow locks are looked up first.
     * 
     * In case no lock is set, the <code>null lock</code> which can be obtained by {@link CmsLock#getNullLock()}
     * is returned.<p> 
     * 
     * @param driverManager the driver manager
     * @param dbc the current database context
     * @param resource the resource
     * 
     * @return the lock state of the given resource 
     * 
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLockForWorkflow(CmsDriverManager driverManager, CmsDbContext dbc, CmsResource resource)
    throws CmsException {

        // check some abort conditions first
        if (resource == null) {
            // non-existent resources are never locked
            return CmsLock.getNullLock();
        }
        if (dbc.currentProject().getId() == CmsProject.ONLINE_PROJECT_ID) {
            // resources are never locked in the online project
            return CmsLock.getNullLock();
        }
        if (!m_isWorkflowEnabled) {
            // workflow is not enabled
            return CmsLock.getNullLock();
        }

        String resourcename = resource.getRootPath();
        CmsLock parentLock = getParentLock(resourcename);

        //  check workflow locks first
        CmsLock lock = getDirectLock(resourcename, CmsLockType.WORKFLOW);

        // check indirekt workflow locks
        List siblings = null;
        if (lock.isNullLock()) {
            siblings = internalReadSiblings(driverManager, dbc, resource);
            lock = getSiblingsLock(siblings, resourcename, CmsLockType.WORKFLOW, CmsLockType.WORKFLOW);
        }

        // check exclusive locks
        if (lock.isNullLock()) {
            lock = getDirectLock(resourcename, CmsLockType.EXCLUSIVE);
        }

        // check if siblings are exclusively locked
        if (lock.isNullLock()) {
            siblings = internalReadSiblings(driverManager, dbc, resource);
            lock = getSiblingsLock(
                siblings,
                resourcename,
                CmsLockType.EXCLUSIVE,
                (parentLock.isNullLock()) ? CmsLockType.SHARED_EXCLUSIVE : CmsLockType.SHARED_INHERITED);
        }

        // check parent lock
        if (lock.isNullLock()) {
            lock = parentLock;
        }

        return lock;
    }

    /**
     * Returns <code>true</code> if the given resource contains a resource that has a workflow lock.<p>
     * 
     * This check is required for certain operations on folders.<p> 
     * 
     * @param resource the resource to check the workflow locks for
     * 
     * @return <code>true</code> if the given resource contains a resource that has a workflow lock
     */
    public boolean hasWorkflowLocks(CmsResource resource) {

        if (m_isWorkflowEnabled && (resource != null)) {
            String rootPath = resource.getRootPath();
            Iterator i = m_workflowLocks.keySet().iterator();
            while (i.hasNext()) {
                String lockPath = (String)i.next();
                if (lockPath.startsWith(rootPath)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if a resource can be locked by a user.<p>
     * 
     * The resource is lockable either
     * - if it is currently unlocked
     * - if it already has a workflow lock set and the user is in the agent group
     * - if it already has a lock of another type set and the user is the lock owner
     * 
     * @param driverManager the driver manager
     * @param dbc the current database context 
     * @param lock the current lock of the resource
     * @param user the user who wants to lock the resource
     * 
     * @return <code>true</code> if the resource can be locked by the user
     * 
     * @throws CmsException if something goes wrong
     */
    public boolean isLockableByUser(CmsDriverManager driverManager, CmsDbContext dbc, CmsLock lock, CmsUser user)
    throws CmsException {

        boolean acceptLock = lock.isUnlocked();

        if (!acceptLock) {
            if (m_isWorkflowEnabled && lock.isWorkflow()) {
                // check for workflow locks

                if (driverManager.getSecurityManager().hasRole(dbc, user, CmsRole.VFS_MANAGER)) {
                    // VFS managers may lock resources in a workflow anyway
                    return true;
                }
                if (m_workflowManager != null) {
                    // in case of a workflow lock, check if the user requesting a lock is
                    // either the agent or in the provided agent group
                    I_CmsPrincipal agent = OpenCms.getWorkflowManager().getTaskAgent(lock.getProject());
                    if (agent.isGroup()) {
                        acceptLock = driverManager.userInGroup(dbc, user.getName(), agent.getName());
                    } else {
                        acceptLock = (user.getId().equals(agent.getId()));
                    }
                    if (!acceptLock) {
                        // alternatively, a member of the managers group may also lock the resource
                        I_CmsPrincipal manager = m_workflowManager.getTaskManager(lock.getProject());
                        if (manager.isGroup()) {
                            acceptLock = driverManager.userInGroup(dbc, user.getName(), manager.getName());
                        } else {
                            acceptLock = (user.getId().equals(agent.getId()));
                        }
                    }
                } else {
                    // should never happen since workflow locks should exist only if a workflow manager is present
                    acceptLock = false;
                }
            } else {
                // in any other case, accept lock owner
                acceptLock = lock.isOwnedBy(user);
            }
        }

        return acceptLock;
    }

    /**
     * Reads the latest saved locks from the database and installs them to 
     * this lock manager.<p>
     * 
     *  @param driverManager to get the needed driver for db access
     *  @param dbc the current database context
     *  
     *  @throws CmsException if something goes wrong
     */
    public void readLocks(CmsDriverManager driverManager, CmsDbContext dbc) throws CmsException {

        m_exclusiveLocks.clear();
        if (m_isWorkflowEnabled) {
            m_workflowLocks.clear();
        }
        List locks = driverManager.getProjectDriver().readLocks(dbc);
        Iterator i = locks.iterator();
        CmsLock lock;
        while (i.hasNext()) {
            lock = (CmsLock)i.next();
            if (m_isWorkflowEnabled && lock.isWorkflow()) {
                m_workflowLocks.put(lock.getResourceName(), lock);
            } else {
                m_exclusiveLocks.put(lock.getResourceName(), lock);
            }
        }
    }

    /**
     * Removes a resource after it has been deleted by the driver manager.<p>
     * 
     * @param driverManager the driver manager
     * @param dbc the current database context
     * @param resourceName the root path of the deleted resource
     * @throws CmsException if something goes wrong
     */
    public void removeDeletedResource(CmsDriverManager driverManager, CmsDbContext dbc, String resourceName)
    throws CmsException {

        boolean resourceExists;
        try {
            driverManager.getVfsDriver().readResource(dbc, dbc.currentProject().getId(), resourceName, false);
            resourceExists = true;
        } catch (CmsVfsResourceNotFoundException e) {
            resourceExists = false;
        }

        if (resourceExists) {
            throw new CmsLockException(Messages.get().container(
                Messages.ERR_REMOVING_UNDELETED_RESOURCE_1,
                dbc.getRequestContext().removeSiteRoot(resourceName)));
        }
        unlockResource(driverManager, resourceName, false);
    }

    /**
     * Removes a resource from the lock manager.<p>
     * 
     * The forceUnlock option should be used with caution. forceUnlock will remove the lock
     * by ignoring any rules which may cause wrong lock states.<p>
     * 
     * @param driverManager the driver manager
     * @param dbc the current database context
     * @param resource the resource
     * @param forceUnlock true, if a resource is forced to get unlocked, no matter by which user and in which project the resource is currently locked
     * 
     * @return the previous CmsLock object of the resource, or null if the resource was unlocked
     *
     * @throws CmsException if something goes wrong
     */
    public CmsLock removeResource(
        CmsDriverManager driverManager,
        CmsDbContext dbc,
        CmsResource resource,
        boolean forceUnlock) throws CmsException {

        String resourcename = resource.getRootPath();
        CmsLock lock = getLock(driverManager, dbc, resource);
        CmsResource sibling = null;

        // check some abort conditions first

        if (lock.isNullLock()) {
            // the resource isn't locked
            return null;
        }

        if (!forceUnlock
            && (!lock.getUserId().equals(dbc.currentUser().getId()) || (!lock.isInProject(dbc.currentProject())))) {
            // the resource is locked by another user
            throw new CmsLockException(Messages.get().container(
                Messages.ERR_RESOURCE_UNLOCK_1,
                dbc.removeSiteRoot(resourcename)));
        }

        if (!forceUnlock && (lock.isInherited() || (getParentFolderLock(resourcename) != null))) {
            // sub-resources of a locked folder can't be unlocked
            throw new CmsLockException(Messages.get().container(
                Messages.ERR_UNLOCK_LOCK_INHERITED_1,
                dbc.removeSiteRoot(resourcename)));
        }

        // remove the lock and clean-up stuff
        if (lock.isExclusive()) {
            if (resource.isFolder()) {
                // in case of a folder, remove any exclusive locks on sub-resources that probably have
                // been upgraded from an inherited lock when the user edited a resource                
                Iterator i = m_exclusiveLocks.keySet().iterator();
                String lockedPath = null;

                while (i.hasNext()) {
                    lockedPath = (String)i.next();
                    if (lockedPath.startsWith(resourcename) && !lockedPath.equals(resourcename)) {
                        // remove the exclusive locked sub-resource
                        i.remove();
                        unlockResource(driverManager, lockedPath, false);
                    }
                }
            }
            return unlockResource(driverManager, resourcename, false);
        }

        if (lock.getType() == CmsLockType.SHARED_EXCLUSIVE) {
            // when a resource with a shared lock gets unlocked, fetch all siblings of the resource 
            // to the same content record to identify the exclusive locked sibling
            List siblings = internalReadSiblings(driverManager, dbc, resource);

            for (int i = 0; i < siblings.size(); i++) {
                sibling = (CmsResource)siblings.get(i);

                if (m_exclusiveLocks.containsKey(sibling.getRootPath())) {
                    // remove the exclusive locked sibling
                    unlockResource(driverManager, sibling.getRootPath(), false);
                    break;
                }
            }

            return lock;
        }

        return lock;
    }

    /**
     * Removes all resources locked in a project.<p>
     * 
     * @param projectId the ID of the project where the resources have been locked
     * @param forceUnlock if <code>true</code>, exclusive locks in other projects are removed for resources locked with a workflow lock
     * @param keepWorkflowLocks if <code>true</code>, workflow locks are not removed
     */
    public void removeResourcesInProject(int projectId, boolean forceUnlock, boolean keepWorkflowLocks) {

        CmsLock currentLock = null;
        Iterator i;

        i = m_exclusiveLocks.keySet().iterator();
        while (i.hasNext()) {
            currentLock = (CmsLock)m_exclusiveLocks.get(i.next());

            if (currentLock.getProjectId() == projectId) {
                // iterators are fail-fast!
                i.remove();
                unlockResource(null, currentLock.getResourceName(), false);
            }
        }

        if (m_isWorkflowEnabled) {
            i = m_workflowLocks.keySet().iterator();
            while (i.hasNext()) {
                currentLock = (CmsLock)m_workflowLocks.get(i.next());

                if (currentLock.getProjectId() == projectId) {
                    // iterators are fail-fast!
                    if (!keepWorkflowLocks) {
                        i.remove();
                        unlockResource(null, currentLock.getResourceName(), true);
                    }
                }

                if (forceUnlock) {
                    currentLock = (CmsLock)m_exclusiveLocks.get(currentLock.getResourceName());
                    if (currentLock != null) {
                        unlockResource(null, currentLock.getResourceName(), false);
                    }
                }
            }
        }
    }

    /**
     * Removes all exclusive temporary locks of a user.<p>
     * 
     * @param user the user whose locks are removed
     */
    public void removeTempLocks(CmsUser user) {

        Iterator i = m_exclusiveLocks.keySet().iterator();
        CmsLock currentLock = null;

        while (i.hasNext()) {
            currentLock = (CmsLock)m_exclusiveLocks.get(i.next());

            if ((currentLock.getType() == CmsLockType.TEMPORARY) && currentLock.getUserId().equals(user.getId())) {
                // iterators are fail-fast!
                i.remove();
                unlockResource(null, currentLock.getResourceName(), false);
            }
        }
    }

    /**
     * Returns the number of exclusive locked resources.<p>
     * 
     * @return the number of exclusive locked resources
     */
    public int size() {

        return m_exclusiveLocks.size();
    }

    /** 
     * @see java.lang.Object#toString()
     */
    public String toString() {

        // bring the list of locked resources into a human readable order first
        List lockedResources = new ArrayList(m_exclusiveLocks.keySet());
        Collections.sort(lockedResources);

        Iterator i = lockedResources.iterator();
        StringBuffer buf = new StringBuffer();
        String lockedPath = null;
        CmsLock currentLock = null;

        while (i.hasNext()) {
            lockedPath = (String)i.next();
            currentLock = (CmsLock)m_exclusiveLocks.get(lockedPath);
            buf.append(currentLock.toString()).append("\n");
        }

        return buf.toString();
    }

    /**
     * Writes the locks that are currently stored in-memory to the database to allow restoring them in 
     * later startups.<p> 
     * 
     * This overwrites the locks previously stored in the underlying database table.<p>
     * 
     *  @param driverManager to get the needed driver for db access
     *  @param dbc the current database context
     *  
     *  @throws CmsException if something goes wrong
     */
    public void writeLocks(CmsDriverManager driverManager, CmsDbContext dbc) throws CmsException {

        if (m_isDirty) {
            // write the locks only if really needed
            List locks = new ArrayList(m_exclusiveLocks.values());
            if (m_isWorkflowEnabled) {
                locks.addAll(m_workflowLocks.values());
            }
            driverManager.getProjectDriver().writeLocks(dbc, locks);
            m_isDirty = false;
        }
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        try {
            if (m_exclusiveLocks != null) {
                m_exclusiveLocks.clear();
                m_exclusiveLocks = null;
            }
            if (m_workflowLocks != null) {
                m_workflowLocks.clear();
                m_workflowLocks = null;
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
     * @param type the lock type to check
     * @return the direct lock of the resource or the null lock
     */
    private CmsLock getDirectLock(String resourcename, CmsLockType type) {

        CmsLock directLock = null;

        if (type == CmsLockType.EXCLUSIVE) {
            directLock = (CmsLock)m_exclusiveLocks.get(resourcename);
        } else if (m_isWorkflowEnabled && (type == CmsLockType.WORKFLOW)) {
            directLock = (CmsLock)m_workflowLocks.get(resourcename);
        }

        if (directLock != null) {
            return directLock;
        } else {
            return CmsLock.getNullLock();
        }
    }

    /**
     * Returns the lock of a possible locked parent folder of a resource.<p>
     * 
     * @param resourcename the name of the resource
     * @return the lock of a parent folder, or null if no parent folders are locked
     */
    private CmsLock getParentFolderLock(String resourcename) {

        String lockedPath = null;
        List keys = new ArrayList(m_exclusiveLocks.keySet());

        for (int i = 0; i < keys.size(); i++) {
            lockedPath = (String)keys.get(i);

            if (resourcename.startsWith(lockedPath) && !resourcename.equals(lockedPath) && lockedPath.endsWith("/")) {
                return (CmsLock)m_exclusiveLocks.get(lockedPath);
            }

        }

        return null;
    }

    /**
     * Returns the inherited lock of a resource.<p>
     * 
     * @param resourcename the name of the resource
     * @return the inherited lock or the null lock
     */
    private CmsLock getParentLock(String resourcename) {

        CmsLock parentFolderLock = getParentFolderLock(resourcename);
        if (parentFolderLock != null) {
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
     * @param type the lock type to check
     * @param indirectType the indirect lock type to use
     * @return the indirect lock of the resource or the null lock
     */
    private CmsLock getSiblingsLock(List siblings, String resourcename, CmsLockType type, CmsLockType indirectType) {

        CmsLock siblingLock = null;

        for (int i = 0; i < siblings.size(); i++) {
            CmsResource sibling = (CmsResource)siblings.get(i);
            siblingLock = getDirectLock(sibling.getRootPath(), type);

            if (!siblingLock.isNullLock()) {
                // a sibling is already exclusive locked or locked in a workflow
                return new CmsLock(resourcename, siblingLock.getUserId(), siblingLock.getProject(), indirectType);
            }
        }

        // no locked siblings found
        return CmsLock.getNullLock();

    }

    /**
     * Reads all siblings from a given resource.<p>
     * 
     * The result is a list of <code>{@link CmsResource}</code> objects. 
     * It does NOT contain the resource itself, only the siblings of the resource.<p>
     * 
     * @param driverManager the driver manager
     * @param dbc the current database context
     * @param resource the resource to find all siblings from
     * 
     * @return a list of <code>{@link CmsResource}</code> Objects that 
     *          are siblings to the specified resource, 
     *          excluding the specified resource itself
     * 
     * @throws CmsException if something goes wrong
     */
    private List internalReadSiblings(CmsDriverManager driverManager, CmsDbContext dbc, CmsResource resource)
    throws CmsException {

        // reading siblings using the DriverManager methods while the lock state is checked would
        // result in an infinite loop, therefore we must access the VFS driver directly
        List siblings = driverManager.getVfsDriver().readSiblings(dbc, dbc.currentProject(), resource, true);
        siblings.remove(resource);
        return siblings;
    }

    /**
     * Sets the given lock to the resource.<p>
     * @param driverManager the driver manager
     * @param resourceName the name of the resource to lock
     * @param lock the lock to set
     */
    private void lockResource(CmsDriverManager driverManager, String resourceName, CmsLock lock) {

        if (!m_isDirty) {
            // read the locks again fresh from DB before changing the state
            try {
                readLocks(driverManager, new CmsDbContext());
            } catch (CmsException e) {
                // should never happen
                e.printStackTrace();
            }
        }
        m_isDirty = true;
        if (lock.getType() == CmsLockType.WORKFLOW) {
            m_workflowLocks.put(resourceName, lock);
        } else {
            m_exclusiveLocks.put(resourceName, lock);
        }
    }

    /**
     * Unlocks the the resource with the given name.<p>
     * @param driverManager the driver manager
     * @param resourceName the name of the resource to unlock
     * @param inWf <code>true</code> if a workflow lock should be removed, 
     *              if not a exclusive lock will be removed
     * 
     * @return the removed lock object
     */
    private CmsLock unlockResource(CmsDriverManager driverManager, String resourceName, boolean inWf) {

        if (!m_isDirty && (driverManager != null)) {
            // read the locks again fresh from DB before changing the state
            try {
                readLocks(driverManager, new CmsDbContext());
            } catch (CmsException e) {
                // should never happen
                e.printStackTrace();
            }
        }
        m_isDirty = true;
        if (inWf) {
            return (CmsLock)m_workflowLocks.remove(resourceName);
        } else {
            return (CmsLock)m_exclusiveLocks.remove(resourceName);
        }
    }
}