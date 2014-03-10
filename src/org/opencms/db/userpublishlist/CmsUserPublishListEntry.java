/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

import org.opencms.util.CmsUUID;

/**
 * User publish list entry.<p>
 */
public class CmsUserPublishListEntry {

    /** The change date. */
    private long m_dateChanged;

    /** The structure id of the publish list entry resource. */
    private CmsUUID m_structureId;

    /**The id of the user who the publish list entry belongs to. */
    private CmsUUID m_userId;

    /**
     * Creates a new user publish list entry.<p>
     * 
     * @param userId the user id of the owner of the entry (if this field is null, and this object is passed to a method which deletes user publsih list entries, this is interpreted as deleting user publish list entries for all users)
     * @param structureId  the structure id of the publish list entry resource 
     * @param dateChanged the date at which the publish list entry was updated
     */
    public CmsUserPublishListEntry(CmsUUID userId, CmsUUID structureId, long dateChanged) {

        m_userId = userId;
        m_structureId = structureId;
        m_dateChanged = dateChanged;
    }

    /**
     * Gets the modification date of the user publish list entry.<p>
     *  
     * @return the date changed 
     */
    public long getDateChanged() {

        return m_dateChanged;
    }

    /**
     * Gets the structure id of the resource in the publish list.<p>
     *  
     * @return the structure id of the resource in the publish list 
     */
    public CmsUUID getStructureId() {

        return m_structureId;
    }

    /**
     * Gets the id of the user to whom the publish list entry belongs.<p>
     * 
     * @return the user id of the publish list entry 
     */
    public CmsUUID getUserId() {

        return m_userId;
    }

}
