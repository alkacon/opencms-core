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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.main;

import org.opencms.util.CmsUUID;

import java.util.List;

/**
 * This interface is used to define the session storage implementation provider.<p>
 *
 * @since 6.5.5
 */
public interface I_CmsSessionStorageProvider {

    /**
     * Validates all session info objects removing any session that have became invalidated.<p>
     */
    void validate();

    /**
     * Returns the stored session info object with the given id.<p>
     *
     * @param sessionId the id to lookup
     *
     * @return the stored session info object, or <code>null</code> if not found
     */
    CmsSessionInfo get(CmsUUID sessionId);

    /**
     * Returns all current stored session info objects.<p>
     *
     * @return all current stored session info objects
     */
    List<CmsSessionInfo> getAll();

    /**
     * Returns all current stored session info objects for the given user.<p>
     *
     * @param userId the id of the user to retrieve the session info objects for
     *
     * @return all current stored session info objects for the given user
     */
    List<CmsSessionInfo> getAllOfUser(CmsUUID userId);

    /**
     * Returns the current number of stored session info objects.<p>
     *
     * @return the current number of stored session info objects, or zero if empty
     */
    int getSize();

    /**
     * Initializes the storage.<p>
     *
     * @throws CmsInitException if initialization fails
     */
    void initialize() throws CmsInitException;

    /**
     * Stores the given session info object.<p>
     *
     * @param sessionInfo the session info object to be stored
     *
     * @return the session info object previously stored with the same session id, or <code>null</code> if none
     */
    CmsSessionInfo put(CmsSessionInfo sessionInfo);

    /**
     * Removes the stored session info object identified by the given session id.<p>
     *
     * @param sessionId the id that identifies the stored session info object to remove
     *
     * @return the removed cached entry or <code>null</code> if none
     */
    CmsSessionInfo remove(CmsUUID sessionId);

    /**
     * Last cleanup possibility.<p>
     *
     * @throws Exception if something goes wrong
     */
    void shutdown() throws Exception;
}
