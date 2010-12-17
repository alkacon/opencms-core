/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/util/Attic/CmsMultiResourceLock.java,v $
 * Date   : $Date: 2010/12/17 08:45:30 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.util.CmsPair;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for locking multiple resources.<p>
 * 
 * It handles a locking error by unlocking all resources it has already successfully locked again.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsMultiResourceLock {

    /** A list of lock requests. */
    private List<CmsPair<CmsSingleResourceLock, Long>> m_lockRequests = new ArrayList<CmsPair<CmsSingleResourceLock, Long>>();

    /** 
     * Adds a lock request.<p>
     * 
     * @param lock the lock which should be locked 
     */
    public void addLockRequest(CmsSingleResourceLock lock) {

        m_lockRequests.add(CmsPair.create(lock, new Long(-1)));
    }

    /**
     * Adds a lock request with a given timestamp.<p>
     * 
     * @param lock the lock to lock 
     * @param timestamp the timestamp to check against 
     */
    public void addLockRequest(CmsSingleResourceLock lock, long timestamp) {

        m_lockRequests.add(CmsPair.create(lock, new Long(timestamp)));
    }

    /**
     * Locks all resources for which lock requests have been added.<p>
     * 
     * @return true if the resources were locked successfully 
     */
    public boolean lockAll() {

        boolean failed = false;
        int lockCount = 0;
        for (CmsPair<CmsSingleResourceLock, Long> lockRequest : m_lockRequests) {
            CmsSingleResourceLock lock = lockRequest.getFirst();
            Long timestampObj = lockRequest.getSecond();
            long timestamp = timestampObj.longValue();
            boolean result;
            if (timestamp == -1) {
                result = lock.lock();
            } else {
                result = lock.lockAndCheck(timestamp);
            }
            if (!result) {
                failed = true;
                break;
            }
            lockCount += 1;
        }
        if (failed) {
            for (int j = 0; j < lockCount; j++) {
                m_lockRequests.get(j).getFirst().unlock();
            }
        }
        return !failed;
    }

}
