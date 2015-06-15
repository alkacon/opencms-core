/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.gwt.Messages;
import org.opencms.lock.CmsLockActionRecord.LockChange;
import org.opencms.main.CmsException;

import java.util.List;

public class CmsLockUtil {

    /**
     * Static helper method to lock a resource.<p>
     *
     * @param cms the CMS context to use
     * @param resource the resource to lock
     * @return the action that was taken
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsLockActionRecord ensureLock(CmsObject cms, CmsResource resource) throws CmsException {
    
        LockChange change = LockChange.unchanged;
        List<CmsResource> blockingResources = cms.getBlockingLockedResources(resource);
        if ((blockingResources != null) && !blockingResources.isEmpty()) {
            throw new CmsException(
                Messages.get().container(
                    Messages.ERR_RESOURCE_HAS_BLOCKING_LOCKED_CHILDREN_1,
                    cms.getSitePath(resource)));
        }
        CmsUser user = cms.getRequestContext().getCurrentUser();
        CmsLock lock = cms.getLock(resource);
        if (!lock.isOwnedBy(user)) {
            cms.lockResourceTemporary(resource);
            change = LockChange.locked;
            lock = cms.getLock(resource);
        } else if (!lock.isOwnedInProjectBy(user, cms.getRequestContext().getCurrentProject())) {
            cms.changeLock(resource);
            change = LockChange.changed;
            lock = cms.getLock(resource);
        }
        return new CmsLockActionRecord(lock, change);
    }

}
