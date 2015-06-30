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

package org.opencms.db;

import org.opencms.db.log.CmsLogEntry;
import org.opencms.db.userpublishlist.CmsUserPublishListEntry;
import org.opencms.main.OpenCms;
import org.opencms.publish.CmsPublishManager;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class can be used to convert a series of log entry objects to a set of changes which should be applied to the user
 * publish list.<p>
 */
public class CmsLogToPublishListChangeConverter {

    /**
     * The possible actions for a publish list entry.<p>
     */
    private enum Action {
        /** Delete an entry. */
        delete,

        /** Update an entry. */
        update
    }

    /**
     * The state consists of a map from (user id, structure id) pairs to time stamps. Negative time stamps are used when
     * the user publish list entry for the given key should be deleted.<p>
     */
    private Map<CmsPair<CmsUUID, CmsUUID>, Long> m_state = new HashMap<CmsPair<CmsUUID, CmsUUID>, Long>();

    /**
     * Feeds a log entry to the converter.<p>
     *
     * @param entry the log entry to process
     */
    public void add(CmsLogEntry entry) {

        CmsUUID userId = entry.getUserId();
        CmsUUID structureId = entry.getStructureId();
        if ((userId == null) || (structureId == null)) {
            return;
        }
        CmsPair<CmsUUID, CmsUUID> key = CmsPair.create(userId, structureId);
        if (isDeleting(entry)) {
            m_state.put(key, Long.valueOf(-1));
        } else if (isChanging(entry)) {
            m_state.put(key, Long.valueOf(entry.getDate()));
        }
    }

    /**
     * Gets the list of publish list entries which should be updated in the database.<p>
     *
     * @return the list of publish list entries to update
     */
    public List<CmsUserPublishListEntry> getPublishListAdditions() {

        return filterEntries(Action.update);
    }

    /**
     * Gets the list of user publish list entries to delete.<p>
     *
     * In the objects returned, only the structure id and user id fields are meaningful.<p>
     *
     * @return the list of user publish list entries to delete
     */
    public List<CmsUserPublishListEntry> getPublishListDeletions() {

        return filterEntries(Action.delete);
    }

    /**
     * Gets all CmsUserPublishListEntry values from the internal state map whose action matches the parameter given.<p>
     *
     * @param action an action constant
     * @return all CmsUserPublishListEntry values from the internal state map whose action matches the parameter given
     */
    protected List<CmsUserPublishListEntry> filterEntries(Action action) {

        boolean isDeleteAll = (action == Action.delete)
            && (OpenCms.getPublishManager().getPublishListRemoveMode() == CmsPublishManager.PublishListRemoveMode.allUsers);
        List<CmsUserPublishListEntry> result = new ArrayList<CmsUserPublishListEntry>();
        for (Map.Entry<CmsPair<CmsUUID, CmsUUID>, Long> entry : m_state.entrySet()) {
            CmsPair<CmsUUID, CmsUUID> key = entry.getKey();
            Long value = entry.getValue();
            Action valueAction = getAction(value.longValue());
            if (valueAction.equals(action)) {
                result.add(
                    new CmsUserPublishListEntry(
                        isDeleteAll ? null : key.getFirst(),
                        key.getSecond(),
                        value.longValue()));
            }
        }
        return result;
    }

    /**
     * Checks whether the given log entry should update an entry in the publish list.<p>
     *
     * @param entry the log entry
     *
     * @return true if the corresponding publish list entry should be removed
     */
    protected boolean isChanging(CmsLogEntry entry) {

        return entry.getType().getId() > 20;
    }

    /**
     * Checks whether the given log entry should remove an entry from the publish list.<p>
     *
     * @param entry the log entry
     *
     * @return true if the corresponding publish list entry should be removed
     */
    protected boolean isDeleting(CmsLogEntry entry) {

        return entry.getType().getId() <= 20;
    }

    /**
     * Gets the publish list action corresponding to a given timestamp value.<p>
     *
     * @param value the timestamp value
     *
     * @return the action belonging to execute for this value
     */
    private Action getAction(long value) {

        if (value == -1) {
            return Action.delete;
        } else {
            return Action.update;
        }
    }

}
