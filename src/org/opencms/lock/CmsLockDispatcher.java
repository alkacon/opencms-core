/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/lock/Attic/CmsLockDispatcher.java,v $
 * Date   : $Date: 2003/07/29 10:43:47 $
 * Version: $Revision: 1.18 $
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
 * @version $Revision: 1.18 $ $Date: 2003/07/29 10:43:47 $
 * @since 5.1.4
 * @see com.opencms.file.CmsObject#getLock(CmsResource)
 * @see org.opencms.lock.CmsLock
 */
public final class CmsLockDispatcher extends Object {

    /** The shared lock dispatcher instance */
    private static CmsLockDispatcher sharedInstance;

    /** A map holding the exclusive CmsLocks */
    private transient Map m_exclusiveLocks;

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
     * @param resourcename the full resource name including the site root
     * @param userId the ID of the user who locked the resource
     * @param projectId the ID of the project where the resource is locked
     * @param hierachy flag indicating how the resource is locked
     * @return the new CmsLock object for the added resource
     */
    public void addResource(CmsDriverManager driverManager, CmsRequestContext context, String resourcename, CmsUUID userId, int projectId) throws CmsException {
        if (!getLock(driverManager, context, resourcename).isNullLock()) {
            throw new CmsLockException("Resource is already locked", CmsLockException.C_RESOURCE_LOCKED);
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
     * Returns the lock for a specified resource name.<p>
     * 
     * @param context the request context
     * @param resourcename the full resource name including the site root
     * @return the CmsLock if the specified resource is locked, or the shared Null lock if the resource is not locked
     */
    public CmsLock getLock(CmsDriverManager driverManager, CmsRequestContext context, String resourcename) throws CmsException {
        CmsLock parentFolderLock = null;
        CmsLock siblingLock = null;
        CmsResource sibling = null;

        if (context.currentProject().getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            // resources are never locked in the online project
            return CmsLock.getNullLock();
        }

        if (m_exclusiveLocks.containsKey(resourcename)) {
            // the resource is exclusive locked
            return (CmsLock) m_exclusiveLocks.get(resourcename);
        }

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
     * Returns the lock of a parent folder, if any are locked.<p>
     * 
     * @param resourcename the name of the resource
     * @return the lock of a parent folder, or null if no parent folders are locked
     */
    protected CmsLock getParentFolderLock(String resourcename) {
        String lockedPath = null;
        Iterator i = m_exclusiveLocks.keySet().iterator();

        while (i.hasNext()) {
            lockedPath = (String) i.next();

            if (resourcename.startsWith(lockedPath) && lockedPath.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                return (CmsLock) m_exclusiveLocks.get(lockedPath);
            }
        }

        return null;
    }

    /**
     * Counts the exclusive locked resources in a specified project.<p>
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
     * Proves if a resource is locked.<p>
     * 
     * Use {@link org.opencms.lock.CmsLockDispatcher#getLock(CmsRequestContext, String)} 
     * to obtain a CmsLock object for the specified resource to get further information 
     * about how the resource is locked.
     * 
     * @param context the request context
     * @param resourcename the full resource name including the site root
     * @return true, if and only if the resource is currently locked, either direct or indirect
     */
    public boolean isLocked(CmsDriverManager driverManager, CmsRequestContext context, String resourcename) throws CmsException {
        CmsLock lock = getLock(driverManager, context, resourcename);
        return !lock.isNullLock();
    }

    /**
     * Removes a resource from the lock dispatcher.<p>
     * 
     * @param resourcename the full resource name including the site root
     * @throws CmsLockException if the user tried to unlock a resource in a locked folder
     */
    public CmsLock removeResource(CmsDriverManager driverManager, CmsRequestContext context, String resourcename, boolean forceUnlock) throws CmsException {
        CmsLock lock = getLock(driverManager, context, resourcename);
        CmsResource sibling = null;

        if (lock.isNullLock()) {
            return null;
        } else if (!forceUnlock && (lock.getUserId() != context.currentUser().getId() || lock.getProjectId() != context.currentProject().getId())) {
            throw new CmsLockException("Unable to unlock resource, resource is locked by another user and/or in another project", CmsLockException.C_RESOURCE_LOCKED_BY_OTHER_USER);
        } else if (lock.getType() == CmsLock.C_TYPE_EXCLUSIVE) {
            return (CmsLock) m_exclusiveLocks.remove(resourcename);
        } else if (lock.getType() == CmsLock.C_TYPE_SHARED_EXCLUSIVE) {
            // fetch all siblings of the resource to the same content record
            // to identify the exclusive locked sibling
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
        } else if (lock.getType() == CmsLock.C_TYPE_INHERITED || lock.getType() == CmsLock.C_TYPE_SHARED_INHERITED) {
            throw new CmsLockException("Unable to unlock resource due to an inherited lock of a parent folder", CmsLockException.C_RESOURCE_LOCKED_INHERITED);
        }

        return null;
    }

    /**
     * Returns a list of direct locked sub resources of a folder.<p>
     * 
     * Use this method to identify direct locked sub resources of a folder (which gets unlocked)
     * to unlock these resources in a second step.
     * 
     * @param resourcename the full resource name including the site root
     * @return a list with resource names of direct locked sub resources
     */
    public List getLockedSubResources(String resourcename) {
        return (List) new ArrayList(0);
    }

    /**
     * Removes all resources that have been previously locked in a specified project.<p>
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
     * Builds a string representation of the current state.<p>
     * 
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

        buf.append("[").append(this.getClass().getName()).append(":\n");

        while (i.hasNext()) {
            lockedPath = (String) i.next();
            currentLock = (CmsLock) m_exclusiveLocks.get(lockedPath);
            buf.append(currentLock.getResourceName());
            buf.append(":");
            buf.append(currentLock.getType());
            buf.append(":");
            buf.append(currentLock.getUserId());
            buf.append("\n");
        }

        buf.append("]");

        return buf.toString();
    }

}
