/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/lock/Attic/CmsLockDispatcher.java,v $
 * Date   : $Date: 2003/08/15 17:38:04 $
 * Version: $Revision: 1.35 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.db.CmsDriverManager;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsRequestContext;
import com.opencms.file.CmsResource;
import com.opencms.flex.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The CmsLockDispatcher is used by the Cms application to detect 
 * the lock state of a resource.<p>
 * 
 * The lock state depends on the path of the resource, and probably 
 * locked parent folders. The result of a query to the lock dispatcher
 * are instances of CmsLock objects.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.35 $ $Date: 2003/08/15 17:38:04 $
 * @since 5.1.4
 * @see com.opencms.file.CmsObject#getLock(CmsResource)
 * @see org.opencms.lock.CmsLock
 */
public final class CmsLockDispatcher extends Object {

    /** The shared lock dispatcher instance */
    private static CmsLockDispatcher sharedInstance;

    /** A map holding the exclusive CmsLocks */
    private Map m_exclusiveLocks;

    /**
     * Default constructor.<p>
     */
    private CmsLockDispatcher() {
        super();
        m_exclusiveLocks = Collections.synchronizedMap(new HashMap());
    }

    /**
     * Returns the shared instance of the lock dispatcher.<p>
     * 
     * @return the shared instance of the lock dispatcher
     */
    public static synchronized CmsLockDispatcher getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new CmsLockDispatcher();
        }

        return sharedInstance;
    }

    /**
     * Adds a resource to the lock dispatcher.<p>
     * 
     * @param driverManager the driver manager
     * @param context the current request context
     * @param resourcename the full resource name including the site root
     * @param userId the ID of the user who locked the resource
     * @param projectId the ID of the project where the resource is locked
     * @throws CmsException if somethong goes wrong
     */
    public void addResource(CmsDriverManager driverManager, CmsRequestContext context, String resourcename, CmsUUID userId, int projectId) throws CmsException {
        CmsLock lock = getLock(driverManager, context, resourcename);

        if (!lock.isNullLock() && !lock.getUserId().equals(context.currentUser().getId())) {
            throw new CmsLockException("Resource is already locked by the current user", CmsLockException.C_RESOURCE_LOCKED_BY_CURRENT_USER);
        }

        CmsLock newLock = new CmsLock(resourcename, userId, projectId, CmsLock.C_TYPE_EXCLUSIVE);
        m_exclusiveLocks.put(resourcename, newLock);

        // handle collisions with exclusive locked sub-resources in case of a folder
        if (resourcename.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
            Iterator i = m_exclusiveLocks.keySet().iterator();
            String lockedPath = null;

            while (i.hasNext()) {
                lockedPath = (String) i.next();

                if (lockedPath.startsWith(resourcename) && !lockedPath.equals(resourcename)) {
                    i.remove();
                }
            }
        }
    }

    /**
     * Counts the exclusive locked resources in a project.<p>
     * 
     * @param project the project
     * @return the number of exclusive locked resources in the specified project
     */
    public int countExclusiveLocksInProject(CmsProject project) {
        Iterator i = m_exclusiveLocks.values().iterator();
        CmsLock lock = null;
        int count = 0;

        while (i.hasNext()) {
            lock = (CmsLock) i.next();
            if (lock.getProjectId() == project.getId()) {
                count++;
            }
        }

        return count;
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
System.err.println("CmsLockDeispatcher final");        
        if (m_exclusiveLocks != null) {
            m_exclusiveLocks.clear();

            m_exclusiveLocks = null;
            sharedInstance = null;
        }
    }

    /**
     * Returns the lock for a resource name.<p>
     * 
     * @param driverManager the driver manager
     * @param context the current request context
     * @param resourcename the full resource name including the site root
     * @return the CmsLock if the specified resource is locked, or the shared Null lock if the resource is not locked
     * @throws CmsException if something goes wrong
     */
    public CmsLock getLock(CmsDriverManager driverManager, CmsRequestContext context, String resourcename) throws CmsException {
        CmsLock parentFolderLock = null;
        CmsLock siblingLock = null;
        CmsResource sibling = null;
        CmsResource resource = null;

        // check some abort conditions first

        if (context.currentProject().getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            // resources are never locked in the online project
            return CmsLock.getNullLock();
        }

        resource = internalReadFileHeader(driverManager, context, resourcename);
        if (resource == null || resource.getState() == I_CmsConstants.C_STATE_DELETED) {
            // deleted, removed or non-existent resources are never locked
            return CmsLock.getNullLock();
        }

        // try to find an exclusive lock

        if (m_exclusiveLocks.containsKey(resourcename)) {
            // the resource is exclusive locked
            return (CmsLock) m_exclusiveLocks.get(resourcename);
        }

        // calculate the lock state

        // fetch all siblings of the resource to the same content record
        List siblings = driverManager.getAllSiblings(context, resourcename);

        if ((parentFolderLock = getParentFolderLock(resourcename)) == null) {
            // all parent folders are unlocked

            for (int i = 0; i < siblings.size(); i++) {
                sibling = (CmsResource) siblings.get(i);
                siblingLock = (CmsLock) m_exclusiveLocks.get(sibling.getFullResourceName());

                if (siblingLock != null) {
                    // a sibling is already exclusive locked
                    return new CmsLock(resourcename, siblingLock.getUserId(), siblingLock.getProjectId(), CmsLock.C_TYPE_SHARED_EXCLUSIVE);
                }
            }

            // no locked siblings found
            return CmsLock.getNullLock();
        } else {
            // a parent folder is locked

            for (int i = 0; i < siblings.size(); i++) {
                sibling = (CmsResource) siblings.get(i);

                if (m_exclusiveLocks.containsKey(sibling.getFullResourceName())) {
                    // a sibling is already exclusive locked
                    return new CmsLock(resourcename, parentFolderLock.getUserId(), parentFolderLock.getProjectId(), CmsLock.C_TYPE_SHARED_INHERITED);
                }
            }

            // no locked siblings found
            return new CmsLock(resourcename, parentFolderLock.getUserId(), parentFolderLock.getProjectId(), CmsLock.C_TYPE_INHERITED);
        }
    }
    
    /**
     * Returns the lock of the exclusive locked sibling pointing to the resource record of a 
     * specified resource name.<p>
     * 
     * @param driverManager the driver manager
     * @param context the current request context
     * @param resourcename the name of the specified resource
     * @return the lock of the exclusive locked sibling
     * @throws CmsException if somethong goes wrong
     */
    public CmsLock getExclusiveLockedSibling(CmsDriverManager driverManager, CmsRequestContext context, String resourcename) throws CmsException {
        CmsResource sibling = null;
        
        // check first if the specified resource itself is already the exclusive locked sibling
        if (m_exclusiveLocks.containsKey(resourcename)) {
            // yup...
            return (CmsLock) m_exclusiveLocks.get(resourcename);
        }        
                
        // nope, fetch all siblings of the resource to the same content record
        List siblings = driverManager.getAllSiblings(context, resourcename);
        
        for (int i = 0; i < siblings.size(); i++) {
            sibling = (CmsResource) siblings.get(i);
            
            if (m_exclusiveLocks.containsKey(sibling.getFullResourceName())) {
                return (CmsLock) m_exclusiveLocks.get(sibling.getFullResourceName());
            }
        }
        
        return CmsLock.getNullLock();
    }

    /**
     * Returns the lock of a possible locked parent folder of a resource.<p>
     * 
     * @param resourcename the name of the resource
     * @return the lock of a parent folder, or null if no parent folders are locked
     */
    private CmsLock getParentFolderLock(String resourcename) {
        String lockedPath = null;
        List keys = (List) new ArrayList(m_exclusiveLocks.keySet());

        for (int i = 0; i < keys.size(); i++) {
            lockedPath = (String)keys.get(i);

            if (resourcename.startsWith(lockedPath) && !resourcename.equals(lockedPath) && lockedPath.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                return (CmsLock) m_exclusiveLocks.get(lockedPath);
            }

        }

        return null;
    }

    /**
     * Reads a file header of a resource name without checking permissions.<p>
     * 
     * @param driverManager the driver manager
     * @param context the current request context
     * @param resourcename the full resource name including the site root
     * @return the CmsResource instance
     * @throws CmsException if something goes wrong
     */
    private CmsResource internalReadFileHeader(CmsDriverManager driverManager, CmsRequestContext context, String resourcename) throws CmsException {
        CmsResource resource = null;

        // reading a resource using readFileHeader while the lock state is checked would
        // inevitably result in an infinite loop...

        try {
            List path = driverManager.readPath(context, resourcename, false);
            resource = (CmsResource) path.get(path.size() - 1);
        } catch (CmsException e) {
            resource = null;
        }

        return resource;
    }

    /**
     * Proves if a resource is locked.<p>
     * 
     * Use {@link org.opencms.lock.CmsLockDispatcher#getLock(CmsRequestContext, String)} 
     * to obtain a CmsLock object for the specified resource to get further information 
     * about how the resource is locked.
     * 
     * @param driverManager the driver manager
     * @param context the current request context
     * @param resourcename the full resource name including the site root
     * @return true, if and only if the resource is currently locked
     * @throws CmsException if something goes wrong
     */
    public boolean isLocked(CmsDriverManager driverManager, CmsRequestContext context, String resourcename) throws CmsException {
        CmsLock lock = getLock(driverManager, context, resourcename);
        return !lock.isNullLock();
    }

    /**
     * Removes a resource from the lock dispatcher.<p>
     * 
     * The forceUnlock option should be used with caution. forceUnlock will remove the lock
     * by ignoring any rules which may cause wrong lock states.
     * 
     * @param driverManager the driver manager
     * @param context the current request context
     * @param resourcename the full resource name including the site root
     * @param forceUnlock true, if a resource is forced to get unlocked, no matter by which user and in which project the resource is currently locked
     * @return the previous CmsLock object of the resource, or null if the resource was unlocked
     * @throws CmsException if something goes wrong
     */
    public CmsLock removeResource(CmsDriverManager driverManager, CmsRequestContext context, String resourcename, boolean forceUnlock) throws CmsException {
        CmsLock lock = getLock(driverManager, context, resourcename);
        CmsResource sibling = null;

        // check some abort conditions first

        if (lock.isNullLock()) {
            // the resource isn't locked
            return null;
        }

        if (!forceUnlock && (!lock.getUserId().equals(context.currentUser().getId()) || lock.getProjectId() != context.currentProject().getId())) {
            // the resource is locked by another user
            throw new CmsLockException("Unable to unlock resource, resource is locked by another user and/or in another project", CmsLockException.C_RESOURCE_LOCKED_BY_OTHER_USER);
        }

        if (!forceUnlock && (lock.getType() == CmsLock.C_TYPE_INHERITED || lock.getType() == CmsLock.C_TYPE_SHARED_INHERITED || (getParentFolderLock(resourcename) != null))) {
            // sub-resources of a locked folder can't be unlocked
            throw new CmsLockException("Unable to unlock resource due to an inherited lock of a parent folder", CmsLockException.C_RESOURCE_LOCKED_INHERITED);
        }

        // remove the lock and clean-up stuff

        if (lock.getType() == CmsLock.C_TYPE_EXCLUSIVE) {
            if (resourcename.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                // in case of a folder, remove any exclusive locks on sub-resources that probably have
                // been upgraded from an inherited lock when the user edited a resource                
                Iterator i = m_exclusiveLocks.keySet().iterator();
                String lockedPath = null;

                while (i.hasNext()) {
                    lockedPath = (String) i.next();
                    if (lockedPath.startsWith(resourcename) && !lockedPath.equals(resourcename)) {
                        // remove the exclusive locked sub-resource
                        i.remove();
                    }
                }
            }

            return (CmsLock) m_exclusiveLocks.remove(resourcename);
        }

        if (lock.getType() == CmsLock.C_TYPE_SHARED_EXCLUSIVE) {
            // when a resource with a shared lock gets unlocked, fetch all siblings of the resource 
            // to the same content record to identify the exclusive locked sibling
            List siblings = driverManager.getAllSiblings(context, resourcename);

            for (int i = 0; i < siblings.size(); i++) {
                sibling = (CmsResource) siblings.get(i);

                if (m_exclusiveLocks.containsKey(sibling.getFullResourceName())) {
                    // remove the exclusive locked sibling
                    m_exclusiveLocks.remove(sibling.getFullResourceName());
                    break;
                }
            }

            return lock;
        }

        return lock;
    }

    /**
     * Removes all resources that are exclusive locked in a project.<p>
     * 
     * @param projectId the ID of the project where the resources have been locked
     */
    public void removeResourcesInProject(int projectId) {
        Iterator i = m_exclusiveLocks.keySet().iterator();
        CmsLock currentLock = null;

        while (i.hasNext()) {
            currentLock = (CmsLock) m_exclusiveLocks.get(i.next());

            if (currentLock.getProjectId() == projectId) {
                // iterators are fail-fast!
                i.remove();
            }
        }
    }

    /** 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        // bring the list of locked resources into a human readable order first
        List lockedResources = (List) new ArrayList(m_exclusiveLocks.keySet());
        Collections.sort(lockedResources);

        Iterator i = lockedResources.iterator();
        StringBuffer buf = new StringBuffer();
        String lockedPath = null;
        CmsLock currentLock = null;

        while (i.hasNext()) {
            lockedPath = (String) i.next();
            currentLock = (CmsLock) m_exclusiveLocks.get(lockedPath);
            buf.append(currentLock.toString()).append("\n");
        }

        return buf.toString();
    }
    
    /**
     * Returns the number of exclusive locked resources.<p>
     * 
     * @return the number of exclusive locked resources
     */
    public int size() {
        return m_exclusiveLocks.size();
    }

}
