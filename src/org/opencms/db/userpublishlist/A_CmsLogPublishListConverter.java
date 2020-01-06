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

import org.opencms.db.CmsDbContext;
import org.opencms.db.I_CmsProjectDriver;
import org.opencms.db.log.CmsLogEntry;
import org.opencms.file.CmsDataAccessException;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class used for converting from CmsLogEntry instances to the necessary changes in the user's publish list.
 *
 * <p>Multiple log entries can be  added via the add() method, and then all changes to the database are performed
 * when the writeToDatabase() method is called.
 */
public abstract class A_CmsLogPublishListConverter {

    /** Map from structure ids to publish list state objects. */
    protected Map<CmsUUID, CmsPublishListResourceState> m_entries = new HashMap<>();

    /**
     * Processes a log entry.
     *
     * @param entry the entry to process
     * */
    public abstract void add(CmsLogEntry entry);

    /**
     * Gets the state entry for the given structure id, creating it if it doesn't already exist.
     *
     * @param key the structure id of a resource
     * @return the state object for the structure id
     */
    public CmsPublishListResourceState getEntry(CmsUUID key) {

        CmsPublishListResourceState result = m_entries.get(key);
        if (result == null) {
            result = new CmsPublishListResourceState();
            m_entries.put(key, result);
        }
        return result;
    }

    /**
     * Writes the collected changes to the database.
     *
     * @param dbc the database context
     * @param projectDriver the project driver
     *
     * @throws CmsDataAccessException if something goes wrong
     */
    public void writeChangesToDatabase(CmsDbContext dbc, I_CmsProjectDriver projectDriver)
    throws CmsDataAccessException {

        List<CmsUserPublishListEntry> entriesToWrite = new ArrayList<>();
        for (CmsUUID structureId : m_entries.keySet()) {
            if (m_entries.get(structureId).isRemoveAll()) {
                entriesToWrite.add(new CmsUserPublishListEntry(null, structureId, 0));
            } else {
                for (CmsUUID userId : m_entries.get(structureId).getRemoveUsers()) {
                    entriesToWrite.add(new CmsUserPublishListEntry(userId, structureId, 0));
                }
            }
        }
        if (entriesToWrite.size() > 0) {
            projectDriver.deleteUserPublishListEntries(dbc, entriesToWrite);
        }
        entriesToWrite.clear();
        for (CmsUUID structureId : m_entries.keySet()) {
            CmsPublishListResourceState state = m_entries.get(structureId);
            for (CmsUUID userId : state.getUpdateUsers()) {
                entriesToWrite.add(new CmsUserPublishListEntry(userId, structureId, state.getTimestamp(userId)));
            }
        }
        if (entriesToWrite.size() > 0) {
            projectDriver.writeUserPublishListEntries(dbc, entriesToWrite);
        }
    }

}
