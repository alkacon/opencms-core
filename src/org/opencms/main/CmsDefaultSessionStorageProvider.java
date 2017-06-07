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

import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.FastHashMap;

/**
 * The default session storage provider implementation.<p>
 *
 * Implementation based on a {@link FastHashMap}.<p>
 *
 * @since 6.5.5
 */
public class CmsDefaultSessionStorageProvider implements I_CmsSessionStorageProvider {

    /** Stores the session info objects mapped to the session id. */
    private FastHashMap m_sessions;

    /**
     * @see org.opencms.main.I_CmsSessionStorageProvider#get(org.opencms.util.CmsUUID)
     */
    public CmsSessionInfo get(CmsUUID sessionId) {

        return (CmsSessionInfo)m_sessions.get(sessionId);
    }

    /**
     * @see org.opencms.main.I_CmsSessionStorageProvider#getAll()
     */
    public List<CmsSessionInfo> getAll() {

        return getAllOfUser(null);
    }

    /**
     * @see org.opencms.main.I_CmsSessionStorageProvider#getAllOfUser(org.opencms.util.CmsUUID)
     */
    public List<CmsSessionInfo> getAllOfUser(CmsUUID userId) {

        try {
            return getAllSelected(CmsCollectionsGenericWrapper.<CmsUUID, CmsSessionInfo> map(m_sessions), userId);
        } catch (ConcurrentModificationException e) {
            // try again with a clone this time
            return getAllSelected(
                CmsCollectionsGenericWrapper.<CmsUUID, CmsSessionInfo> map(m_sessions.clone()),
                userId);
        }
    }

    /**
     * @see org.opencms.main.I_CmsSessionStorageProvider#getSize()
     */
    public int getSize() {

        return m_sessions.size();
    }

    /**
     * @see org.opencms.main.I_CmsSessionStorageProvider#initialize()
     */
    public void initialize() {

        // create a map for all sessions, these will be mapped using their session id
        m_sessions = new FastHashMap();
        // set to "fast" mode (will be reset for write access)
        m_sessions.setFast(true);
    }

    /**
     * @see org.opencms.main.I_CmsSessionStorageProvider#put(org.opencms.main.CmsSessionInfo)
     */
    public CmsSessionInfo put(CmsSessionInfo sessionInfo) {

        return (CmsSessionInfo)m_sessions.put(sessionInfo.getSessionId(), sessionInfo);
    }

    /**
     * @see org.opencms.main.I_CmsSessionStorageProvider#remove(org.opencms.util.CmsUUID)
     */
    public CmsSessionInfo remove(CmsUUID sessionId) {

        return (CmsSessionInfo)m_sessions.remove(sessionId);
    }

    /**
     * @see org.opencms.main.I_CmsSessionStorageProvider#shutdown()
     */
    public void shutdown() {

        m_sessions.clear();
        m_sessions = null;
    }

    /**
     * @see org.opencms.main.I_CmsSessionStorageProvider#validate()
     */
    public void validate() {

        try {
            // change session map to full synchronization or "slow" mode
            m_sessions.setFast(false);
            Iterator<Map.Entry<CmsUUID, CmsSessionInfo>> sessions = CmsCollectionsGenericWrapper.<CmsUUID, CmsSessionInfo> map(
                m_sessions).entrySet().iterator();
            while (sessions.hasNext()) {
                Map.Entry<CmsUUID, CmsSessionInfo> entry = sessions.next();
                CmsUUID sessionId = entry.getKey();
                CmsSessionInfo sessionInfo = entry.getValue();
                if ((sessionInfo != null) && (m_sessions.get(sessionId) != null)) {
                    // may be the case in case of concurrent modification
                    if (sessionInfo.isExpired()) {
                        // session is invalid, try to remove it
                        try {
                            sessions.remove();
                        } catch (ConcurrentModificationException ex) {
                            // ignore, better luck next time...
                        }
                    }
                }
            }
        } catch (ConcurrentModificationException ex) {
            // CME can also be triggered from the Iterator#next() method.
            // ignore, better luck next time...
        } finally {
            // it may be null during shutdown
            if (m_sessions != null) {
                // reset session map to "fast" mode
                m_sessions.setFast(true);
            }
        }
    }

    /**
     * Returns all sessions or all sessions matching the user id from the provided Map.<p>
     *
     * @param allSessions the Map of existing sessions
     * @param userId the id of the user, if null all sessions will be returned
     *
     * @return all sessions or all sessions matching the user id from the provided Map
     */
    private List<CmsSessionInfo> getAllSelected(Map<CmsUUID, CmsSessionInfo> allSessions, CmsUUID userId) {

        List<CmsSessionInfo> userSessions = new ArrayList<CmsSessionInfo>();
        Iterator<Map.Entry<CmsUUID, CmsSessionInfo>> i = allSessions.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<CmsUUID, CmsSessionInfo> entry = i.next();
            CmsSessionInfo sessionInfo = entry.getValue();
            if ((sessionInfo != null) && ((userId == null) || userId.equals(sessionInfo.getUserId()))) {
                // sessionInfo == null may be the case in case of concurrent modification
                userSessions.add(sessionInfo);
            }
        }
        return userSessions;
    }
}
