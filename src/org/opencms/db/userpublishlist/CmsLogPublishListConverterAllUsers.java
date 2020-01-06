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

package org.opencms.db.userpublishlist;

import org.opencms.db.log.CmsLogEntry;
import org.opencms.util.CmsUUID;

/**
 * Implementation where resources get removed from all publish lists when they are published.
 */
public class CmsLogPublishListConverterAllUsers extends A_CmsLogPublishListConverter {

    /**
     * @see org.opencms.db.userpublishlist.A_CmsLogPublishListConverter#add(org.opencms.db.log.CmsLogEntry)
     */
    @Override
    public void add(CmsLogEntry entry) {

        CmsUUID structureId = entry.getStructureId();
        CmsUUID userId = entry.getUserId();
        if ((structureId == null) || (userId == null)) {
            return;
        }
        switch (entry.getType()) {

            case RESOURCE_NEW_DELETED:
            case RESOURCE_PUBLISHED_DELETED:
            case RESOURCE_PUBLISHED_MODIFIED:
            case RESOURCE_PUBLISHED_NEW:
            case RESOURCE_CHANGES_UNDONE:
                getEntry(structureId).setRemoveAll();
                break;
            case RESOURCE_HIDDEN:
                getEntry(structureId).addRemove(userId);
                break;
            default:
                getEntry(structureId).addUpdate(userId, entry.getDate());
                break;
        }
    }

}
