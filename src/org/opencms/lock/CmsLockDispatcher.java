/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/lock/Attic/CmsLockDispatcher.java,v $
 * Date   : $Date: 2003/07/17 12:00:40 $
 * Version: $Revision: 1.2 $
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

import java.util.HashMap;
import java.util.Iterator;

/**
 * The CmsLockDispatcher is used by the Cms application to detect 
 * the lock state of a resource.<p>
 * 
 * The lock dispatcher is used by the Cms app. to detect the exact 
 * lock state for a specified resource name.<p>
 * 
 * The lock state depends on the path of the resource, and probably 
 * locked parent folders. The result of a query to the lock dispatcher
 * are instances of CmsLock objects.<p>
 * 
 * It is impossible to create more than 1 instance of this class,
 * since this class is implemented as a singleton object.
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.2 $ $Date: 2003/07/17 12:00:40 $
 * @since 5.1.4
 */
public final class CmsLockDispatcher extends HashMap {

    /** The shared lock dispatcher instance */
    private static CmsLockDispatcher sharedInstance;

    /**
     * Default constructor.<p>
     * 
     * Since this class is a singleton object, only the class itself is allowed to invoke its constructor.
     */
    private CmsLockDispatcher() {
        super();
    }

    /**
     * Returns the shared instance of the lock dispatcher.<p>
     * 
     * @return the shared instance of the lock dispatcher
     */
    public static CmsLockDispatcher getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new CmsLockDispatcher();
        }

        return sharedInstance;
    }

    /**
     * Returns the lock state for a specified resource.<p>
     * 
     * @param resourcename the name of the resource
     * @return the lock state of the resource
     */
    public int getLockstate(String resourcename) {
        String lockedPath = null;
        Iterator i = keySet().iterator();

        while (i.hasNext()) {
            lockedPath = (String) i.next();

            if (resourcename.equals(lockedPath)) {
                return CmsLock.C_LOCK_STATE_DIRECT_LOCKED;
            }
            if (resourcename.indexOf(lockedPath) == 0) {
                return CmsLock.C_LOCK_STATE_INDIRECT_LOCKED;
            }
        }

        return CmsLock.C_LOCK_STATE_UNLOCKED;
    }

    /**
     * Returns the lock for specified resource.<p>
     * 
     * @param resourcename the name of the resource
     * @return the CmsLock if the specified resource is locked, or the shared Null lock if the resource is not locked
     */
    public CmsLock getLock(String resourcename) {
        String lockedPath = null;
        Iterator i = keySet().iterator();

        while (i.hasNext()) {
            lockedPath = (String) i.next();

            if (resourcename.indexOf(lockedPath) == 0) {
                return (CmsLock) get(lockedPath);
            }
        }

        return CmsLock.getNullLock();
    }

}
