/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/lock/CmsLockManager.java,v $
 * Date   : $Date: 2005/06/21 15:50:00 $
 * Version: $Revision: 1.28 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.lock;

import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDriverManager;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
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
 * @author Michael Emmerich (m.emmerich@alkacon.com)
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @author Andreas Zahner (a.zahner@alkacon.com) 
 * @version $Revision: 1.28 $
 * 
 * @since 5.1.4
 * 
 * @see org.opencms.file.CmsObject#getLock(CmsResource)
 * @see org.opencms.lock.CmsLock
 */
public final class CmsLockManager {

    /** The shared lock manager instance. */
    private static CmsLockManager sharedInstance;

    /** A map holding the exclusive CmsLocks. */
    private Map m_exclusiveLocks;

    /**
     * Default constructor.<p>
     */
    private CmsLockManager() {

        super();
        m_exclusiveLocks = Collections.synchronizedMap(new HashMap());
    }

    /**
     * Returns the shared instance of the lock manager.<p>
     * 
     * @return the shared instance of the lock manager
     */
    public static synchronized CmsLockManager getInstance() {

        // synchronized to avoid "Double Checked Locking"
        if (sharedInstance == null) {
            sharedInstance = new CmsLockManager();
        }

        return sharedInstance;
    }

    /**
     * Adds a resource to the lock manager.<p>
     * 
     * @param driverManager the driver manager
     * @param dbc the current database context
     * @param resource the resource
     * @param userId the ID of the user who locked the resource
     * @param projectId the ID of the project where the resource is locked
     * @param mode flag indicating the mode (temporary or common) of a lock
     * 
     * @throws CmsLockException if the resource is locked
     * @throws CmsException if somethong goes wrong
     */
    public void addResource(
        CmsDriverManager driverManager,
        CmsDbContext dbc,
        CmsResource resource,
        CmsUUID userId,
        int projectId,
        int mode) throws CmsLockException, CmsException {

        CmsLock lock = getLock(driverManager, dbc, resource);
        String resourceName = resource.getRootPath();

        if (!lock.isNullLock() && !lock.getUserId().equals(dbc.currentUser().getId())) {
            throw new CmsLockException(Messages.get().container(
                Messages.ERR_RESOURCE_LOCKED_1,
                dbc.getRequestContext().getSitePath(resource)));
        }

        if (lock.isNullLock()) {
            // create a new exclusive lock unless the resource has already a shared lock due to a
            // exclusive locked sibling
            CmsLock newLock = new CmsLock(resourceName, userId, projectId, CmsLock.C_TYPE_EXCLUSIVE, mode);
            m_exclusiveLocks.put(resourceName, newLock);
        }

        // handle collisions with exclusive locked sub-resources in case of a folder
        if (CmsResource.isFolder(resourceName)) {
            Iterator i = m_exclusiveLocks.keySet().iterator();
            String lockedPath = null;

            while (i.hasNext()) {
                lockedPath = (String)i.next();

                if (lockedPath.startsWith(resourceName) && !lockedPath.equals(resourceName)) {
                    i.remove();
                }
            }
        }
    }

    /**
     * Counts the exclusive locked resources inside a folder.<p>
     * 
     * @param foldername the folder
     * 
     * @return the number of exclusive locked resources in the specified folder
     */
    public int countExclusiveLocksInFolder(String foldername) {

        Iterator i = m_exclusiveLocks.values().iterator();
        CmsLock lock = null;
        int count = 0;

        while (i.hasNext()) {
            lock = (CmsLock)i.next();
            if (lock.getResourceName().startsWith(foldername)) {
                count++;
            }
        }

        return count;
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
            if (lock.getProjectId() == project.getId()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Returns the lock of the exclusive locked sibling pointing to the resource record of a 
     * specified resource name.<p>
     * 
     * @param driverManager the driver manager
     * @param dbc the current database context
     * @param resource the specified resource
     * 
     * @return the lock of the exclusive locked sibling
     * @throws CmsException if somethong goes wrong
     */
    public CmsLock getExclusiveLockedSibling(CmsDriverManager driverManager, CmsDbContext dbc, CmsResource resource)
    throws CmsException {

        CmsResource sibling = null;
        String resourcename = resource.getRootPath();

        // check first if the specified resource itself is already the exclusive locked sibling
        if (m_exclusiveLocks.containsKey(resourcename)) {
            // yup...
            return (CmsLock)m_exclusiveLocks.get(resourcename);
        }

        // nope, fetch all siblings of the resource to the same content record
        List siblings = internalReadSiblings(driverManager, dbc, resource);

        for (int i = 0; i < siblings.size(); i++) {
            sibling = (CmsResource)siblings.get(i);

            if (m_exclusiveLocks.containsKey(sibling.getRootPath())) {
                return (CmsLock)m_exclusiveLocks.get(sibling.getRootPath());
            }
        }

        return CmsLock.getNullLock();
    }

    /**
     * Returns the lock for a resource.<p>
     * 
     * @param driverManager the driver manager
     * @param dbc the current database context
     * @param resource the resource
     * 
     * @return the CmsLock if the specified resource is locked, or the shared Null lock if the resource is not locked
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsDriverManager driverManager, CmsDbContext dbc, CmsResource resource) throws CmsException {

        CmsLock parentFolderLock = null;
        CmsLock siblingLock = null;
        CmsResource sibling = null;
        String resourcename = resource.getRootPath();

        // check some abort conditions first

        if (dbc.currentProject().getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            // resources are never locked in the online project
            return CmsLock.getNullLock();
        }

        if (resource == null) {
            // non-existent resources are never locked
            return CmsLock.getNullLock();
        }

        // try to find an exclusive lock

        if (m_exclusiveLocks.containsKey(resourcename)) {
            // the resource is exclusive locked
            return (CmsLock)m_exclusiveLocks.get(resourcename);
        }

        // calculate the lock state

        // fetch all siblings of the resource to the same content record
        List siblings = internalReadSiblings(driverManager, dbc, resource);

        if ((parentFolderLock = getParentFolderLock(resourcename)) == null) {
            // all parent folders are unlocked

            for (int i = 0; i < siblings.size(); i++) {
                sibling = (CmsResource)siblings.get(i);
                siblingLock = (CmsLock)m_exclusiveLocks.get(sibling.getRootPath());

                if (siblingLock != null) {
                    // a sibling is already exclusive locked
                    return new CmsLock(
                        resourcename,
                        siblingLock.getUserId(),
                        siblingLock.getProjectId(),
                        CmsLock.C_TYPE_SHARED_EXCLUSIVE);
                }
            }

            // no locked siblings found
            return CmsLock.getNullLock();
        } else {
            // a parent folder is locked

            for (int i = 0; i < siblings.size(); i++) {
                sibling = (CmsResource)siblings.get(i);

                if (m_exclusiveLocks.containsKey(sibling.getRootPath())) {
                    // a sibling is already exclusive locked
                    return new CmsLock(
                        resourcename,
                        parentFolderLock.getUserId(),
                        parentFolderLock.getProjectId(),
                        CmsLock.C_TYPE_SHARED_INHERITED);
                }
            }

            // no locked siblings found
            return new CmsLock(
                resourcename,
                parentFolderLock.getUserId(),
                parentFolderLock.getProjectId(),
                CmsLock.C_TYPE_INHERITED);
        }
    }

    /**
     * Proves if a resource is locked.<p>
     * 
     * Use {@link org.opencms.lock.CmsLockManager#getLock(CmsDriverManager, CmsDbContext, CmsResource)} 
     * to obtain a CmsLock object for the specified resource to get further information 
     * about how the resource is locked.<p>
     * 
     * @param driverManager the driver manager
     * @param dbc the current database context
     * @param resource the resource
     * 
     * @return true, if and only if the resource is currently locked
     * @throws CmsException if something goes wrong
     */
    public boolean isLocked(CmsDriverManager driverManager, CmsDbContext dbc, CmsResource resource) throws CmsException {

        CmsLock lock = getLock(driverManager, dbc, resource);
        return !lock.isNullLock();
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
            && (!lock.getUserId().equals(dbc.currentUser().getId()) || lock.getProjectId() != dbc.currentProject().getId())) {
            // the resource is locked by another user
            throw new CmsLockException(Messages.get().container(
                Messages.ERR_RESOURCE_UNLOCK_1,
                dbc.removeSiteRoot(resourcename)));
        }

        if (!forceUnlock
            && (lock.getType() == CmsLock.C_TYPE_INHERITED || lock.getType() == CmsLock.C_TYPE_SHARED_INHERITED || (getParentFolderLock(resourcename) != null))) {
            // sub-resources of a locked folder can't be unlocked
            throw new CmsLockException(Messages.get().container(
                Messages.ERR_UNLOCK_LOCK_INHERITED_1,
                dbc.removeSiteRoot(resourcename)));
        }

        // remove the lock and clean-up stuff
        if (lock.getType() == CmsLock.C_TYPE_EXCLUSIVE) {
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
                    }
                }
            }

            return (CmsLock)m_exclusiveLocks.remove(resourcename);
        }

        if (lock.getType() == CmsLock.C_TYPE_SHARED_EXCLUSIVE) {
            // when a resource with a shared lock gets unlocked, fetch all siblings of the resource 
            // to the same content record to identify the exclusive locked sibling
            List siblings = internalReadSiblings(driverManager, dbc, resource);

            for (int i = 0; i < siblings.size(); i++) {
                sibling = (CmsResource)siblings.get(i);

                if (m_exclusiveLocks.containsKey(sibling.getRootPath())) {
                    // remove the exclusive locked sibling
                    m_exclusiveLocks.remove(sibling.getRootPath());
                    break;
                }
            }

            return lock;
        }

        return lock;
    }

    /**
     * Removes all resources that are exclusively locked in a project.<p>
     * 
     * @param projectId the ID of the project where the resources have been locked
     */
    public void removeResourcesInProject(int projectId) {

        Iterator i = m_exclusiveLocks.keySet().iterator();
        CmsLock currentLock = null;

        while (i.hasNext()) {
            currentLock = (CmsLock)m_exclusiveLocks.get(i.next());

            if (currentLock.getProjectId() == projectId) {
                // iterators are fail-fast!
                i.remove();
            }
        }
    }

    /**
     * Removes all exclusive temporary locks of a user.<p>
     * 
     * @param userId the ID of the user whose locks are removed
     */
    public void removeTempLocks(CmsUUID userId) {

        Iterator i = m_exclusiveLocks.keySet().iterator();
        CmsLock currentLock = null;

        while (i.hasNext()) {
            currentLock = (CmsLock)m_exclusiveLocks.get(i.next());

            if (currentLock.getUserId().equals(userId) && currentLock.getMode() == CmsLock.C_MODE_TEMP) {
                // iterators are fail-fast!
                i.remove();
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
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {

        try {
            if (m_exclusiveLocks != null) {
                m_exclusiveLocks.clear();

                m_exclusiveLocks = null;
                sharedInstance = null;
            }
        } catch (Throwable t) {
            // ignore
        }
        super.finalize();
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
     * Reads all siblings from a given resource.<p>
     * 
     * The result is a list of <code>{@link CmsResource}</code> objects. 
     * It does NOT contain the resource itself, only the siblings of the resource.<p>
     * 
     * @param driverManager the driver manager
     * @param dbc the current database context
     * @param runtimeInfo the current runtime info
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
        // inevitably result in an infinite loop...        

        List siblings = driverManager.getVfsDriver().readSiblings(dbc, dbc.currentProject(), resource, true);
        siblings.remove(resource);

        return driverManager.updateContextDates(dbc, siblings);
    }

}
