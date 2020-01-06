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

import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of publish list changes for a single resource.
 */
public class CmsPublishListResourceState {

    /** Enum representing a state change. */
    enum StateChange {
        /** Resource should be removed from publish list. */
        delete,
        /** Resource should be added to publish list. */
        update;
    }

    /** Keeps track of whether all publish list entries for the resource should be removed. */
    private boolean m_removeAll;

    /** Map of time stamps of changes. */
    private Map<CmsUUID, Long> m_timestamps = new HashMap<>();

    /** Map from user id to state changes. */
    private Map<CmsUUID, StateChange> m_userChanges = new HashMap<>();

    /**
     * Adds a removal from the publish list for the given user id.
     *
     * @param userId a user id
     */
    public void addRemove(CmsUUID userId) {

        m_userChanges.put(userId, StateChange.delete);

    }

    /**
     * Adds an update for the publish list for the given user id.
     *
     * @param userId a user id
     * @param timestamp the change timestamp
     */
    public void addUpdate(CmsUUID userId, long timestamp) {

        m_userChanges.put(userId, StateChange.update);
        m_timestamps.put(userId, Long.valueOf(timestamp));

    }

    /**
     * Gets the users from whose publish list the resource should be removed.
     *
     * @return list of user ids
     */
    public List<CmsUUID> getRemoveUsers() {

        List<CmsUUID> result = new ArrayList<>();
        for (Map.Entry<CmsUUID, StateChange> entry : m_userChanges.entrySet()) {
            if (entry.getValue() == StateChange.delete) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Gets the log timestamp for the given user id.
     *
     * @param userId the user id
     * @return the timestamp for the change with the given user id
     */
    public long getTimestamp(CmsUUID userId) {

        Long timestamp = m_timestamps.get(userId);
        if (timestamp == null) {
            return 0;
        }
        return timestamp.longValue();
    }

    /**
     * Gets the users to whose publish list the resource should be added
     *
     * @return list of user ids
     */
    public List<CmsUUID> getUpdateUsers() {

        List<CmsUUID> result = new ArrayList<>();
        for (Map.Entry<CmsUUID, StateChange> entry : m_userChanges.entrySet()) {
            if (entry.getValue() == StateChange.update) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    /**
     * Returns true if the structure id should be removed for all users.
     *
     * @return true if resource should be removed from all publish lists
     */
    public boolean isRemoveAll() {

        return m_removeAll;
    }

    /**
     * Sets the 'remove all' status to 'true'.
     *
     * <p>All collected changes/removals done before this will be discarded.
     * Changes/removals done after this will not clear the removeAll status.
     */
    public void setRemoveAll() {

        m_removeAll = true;
        m_userChanges.clear();
    }

}
