/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/lock/Attic/CmsLockDispatcher.java,v $
 * Date   : $Date: 2003/07/28 13:56:37 $
 * Version: $Revision: 1.13 $
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

import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRequestContext;
import com.opencms.file.CmsResource;
import com.opencms.flex.CmsEvent;
import com.opencms.flex.I_CmsEventListener;
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
 * are instances of CmsLock objects.<p>
 * 
 * The LockDispatcher implements the event listener interface to
 * re-initialize itself while the app. with a clear cache event.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.13 $ $Date: 2003/07/28 13:56:37 $
 * @since 5.1.4
 * @see com.opencms.file.CmsObject#getLock(CmsResource)
 * @see org.opencms.lock.CmsLock
 */
public final class CmsLockDispatcher extends Object implements I_CmsEventListener {

    /** The shared lock dispatcher instance */
    private static CmsLockDispatcher sharedInstance;

    /** A map holding the exclusive CmsLocks */
    private Map m_exclusiveLocks;

    /**
     * Default constructor.<p>
     * 
     * Since this class is a singleton object, only the class itself is allowed to invoke its constructor.
     */
    private CmsLockDispatcher() {
        super();

        m_exclusiveLocks = Collections.synchronizedMap(new HashMap());

        // add this class as an event listener to the Cms
        A_OpenCms.addCmsEventListener(this);
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
    public void addResource(String resourcename, CmsUUID userId, int projectId) {
        CmsLock newLock = new CmsLock(resourcename, userId, projectId, CmsLock.C_TYPE_EXCLUSIVE);
        m_exclusiveLocks.put(resourcename, newLock);
    }

    /**
     * Handles Cms events.<p>
     * 
     * @param event the event which is handled
     */
    public void cmsEvent(CmsEvent event) {
        switch (event.getType()) {
            case I_CmsEventListener.EVENT_CLEAR_CACHES :
                init(event.getCmsObject());
                break;

            default :
                break;
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
        if (context.currentProject().getId() == I_CmsConstants.C_PROJECT_ONLINE_ID) {
            return CmsLock.getNullLock();
        } else {
            // calculate the lock state of the resource...
            int lockState = getLockState(driverManager, context, resourcename);
            
            if (lockState != CmsLock.C_TYPE_UNLOCKED) {
                // ...and create a CmsLock object in case the resource is locked
                return new CmsLock(resourcename, context.currentUser().getId(), context.currentProject().getId(), lockState);
            }  
        }

        return CmsLock.getNullLock();
    }
    
    /**
     * Gets the lock state for a specified resource name.<p>
     * 
     * @param driverManager the driver manager of the Cms
     * @param context the current request context
     * @param resourcename the name of the resource
     * @return the lock state
     * @throws CmsException if something goes wrong
     */
    protected int getLockState(CmsDriverManager driverManager, CmsRequestContext context, String resourcename) throws CmsException {
        List siblings = null;
        
        if (m_exclusiveLocks.containsKey(resourcename)) {
            return CmsLock.C_TYPE_EXCLUSIVE;
        }         
        
        siblings = driverManager.getAllSiblings(context, resourcename);

        if (!parentFolderIsLocked(resourcename)) {
            for (int i = 0; i < siblings.size(); i++) {
                CmsResource sibling = (CmsResource) siblings.get(i);
                if (m_exclusiveLocks.containsKey(sibling.getFullResourceName())) {
                    return CmsLock.C_TYPE_SHARED_EXCLUSIVE;
                }
            }
            return CmsLock.C_TYPE_UNLOCKED;
        } else {
            for (int i = 0; i < siblings.size(); i++) {
                CmsResource sibling = (CmsResource) siblings.get(i);
                if (m_exclusiveLocks.containsKey(sibling.getFullResourceName())) {
                    return CmsLock.C_TYPE_SHARED_INHERITED;
                }
            }
            return CmsLock.C_TYPE_INHERITED;
        }
    }

    /**
     * Proves if a parent folder of a specified resource is locked.<p>
     * 
     * @param resourcename the name of the resource
     * @return true, if a parent folder of the specified resource has an exclusive lock
     */
    protected boolean parentFolderIsLocked(String resourcename) {
        String lockedPath = null;
        Iterator i = m_exclusiveLocks.keySet().iterator();

        while (i.hasNext()) {
            lockedPath = (String) i.next();

            if (resourcename.startsWith(lockedPath) && lockedPath.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                return true;
            }
        }

        return false;
    }    

    /**
     * Initializes the the lock dispatcher by reading all directly locked resources from the database.<p>
     * 
     * @param cms the current user's Cms object 
     * @return the number of directly locked resources
     * @throws CmsException if something goes wrong
     */
    public int init(CmsObject cms) {
        return 0;
        
        /*
        List lockedResources = null;
        Iterator i = null;
        CmsResource currentResource = null;
        String currentPath = null;
        int count = 0;

        m_exclusiveLocks.clear();

        try {
            lockedResources = cms.readLockedFileHeaders();
            i = lockedResources.iterator();

            while (i.hasNext()) {
                currentResource = (CmsResource) i.next();
                currentPath = currentResource.getFullResourceName();
                addResource(currentPath, currentResource.isLockedBy(), currentResource.getProjectId());
                count++;
            }

            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_DEBUG)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_DEBUG, "[" + this.getClass().getName() + "] initialized, found " + count + " directly locked resources");
            }
        } catch (CmsException e) {
            if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[" + this.getClass().getName() + "] error initializing: " + e.toString());
            }
        }

        return count;
        */
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
    public CmsLock removeResource(String resourcename) throws CmsLockException {
        return (CmsLock) m_exclusiveLocks.remove(resourcename);
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
