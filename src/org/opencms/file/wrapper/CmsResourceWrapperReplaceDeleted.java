/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.file.wrapper;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;

import java.util.List;

/**
 * Resource wrapper which intercepts createResource calls and substitutes them with replaceResource if there is already a deleted file at the same path.<p>
 *
 * This is useful because when accessing a repository, users can neither see nor publish deleted files, and thus can not re-create a file of state "deleted".
 */
public class CmsResourceWrapperReplaceDeleted extends A_CmsResourceWrapper {

    /**
     * @see org.opencms.file.wrapper.A_CmsResourceWrapper#createResource(org.opencms.file.CmsObject, java.lang.String, int, byte[], java.util.List)
     */
    @Override
    public CmsResource createResource(
        CmsObject cms,
        String resourcename,
        int type,
        byte[] content,
        List<CmsProperty> properties) throws CmsException, CmsIllegalArgumentException {

        try {
            CmsResource resource = cms.readResource(resourcename, CmsResourceFilter.ALL);
            if (!resource.getState().isDeleted()) {
                return null;
            }
            CmsLock lock = cms.getLock(resource);
            if (lock.isUnlocked()) {
                cms.lockResourceTemporary(resourcename);
            }
            cms.undeleteResource(resourcename, false);
            cms.replaceResource(resourcename, type, content, properties);
            CmsResource result = cms.readResource(resourcename);
            return result;
        } catch (CmsVfsResourceNotFoundException e) {
            return null;
        }

    }

    /**
     * @see org.opencms.file.wrapper.I_CmsResourceWrapper#isWrappedResource(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public boolean isWrappedResource(CmsObject cms, CmsResource res) {

        return false;
    }

}
