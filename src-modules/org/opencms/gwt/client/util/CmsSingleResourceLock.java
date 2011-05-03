/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsSingleResourceLock.java,v $
 * Date   : $Date: 2011/05/03 10:49:05 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.util;

import org.opencms.gwt.client.CmsCoreProvider;

/**
 * A wrapper class around the lock and unlock methods of {@link org.opencms.gwt.shared.rpc.I_CmsCoreService}.<p>
 * 
 * It keeps track of the state of a resource and will not unnecessarily lock or unlock the same resource twice. 
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsSingleResourceLock {

    /** True if the resource was locked by the current user through this object. */
    private boolean m_locked;

    /** The resource to be locked/unlocked. */
    private String m_uri;

    /**
     * Creates a new lock for the resource with the given path.<p>
     * 
     * @param uri the path of the resource to be locked/unlocked 
     */
    public CmsSingleResourceLock(String uri) {

        m_uri = uri;
    }

    /**
     * Returns true if the resource was successfully locked through this object.<p>
     * 
     * @return true if the resource was locked through this object  
     */
    public boolean isLocked() {

        return m_locked;
    }

    /**
     * Locks the resource.<p>
     * 
     * @return true if the locking succeeded 
     */
    public boolean lock() {

        if (!m_locked) {
            m_locked = CmsCoreProvider.get().lock(m_uri);
        }
        return m_locked;
    }

    /**
     * Locks the resource if its modification date is not newer than a given date.<p>
     * 
     * @param modificationDate the modification date to check against
     *  
     * @return true if the locking succeeded
     */
    public boolean lockAndCheck(long modificationDate) {

        if (!m_locked) {
            m_locked = CmsCoreProvider.get().lockAndCheckModification(m_uri, modificationDate);
        }
        return m_locked;
    }

    /**
     * Unlocks the resource.<p>
     */
    public void unlock() {

        if (m_locked) {
            if (CmsCoreProvider.get().unlock(m_uri)) {
                m_locked = false;
            }
        }
    }
}
