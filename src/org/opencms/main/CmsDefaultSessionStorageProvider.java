/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsDefaultSessionStorageProvider.java,v $
 * Date   : $Date: 2007/07/09 16:00:57 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.FastHashMap;

/**
 * The default session storage provider implementation.<p>
 * 
 * Implementation based on a {@link FastHashMap}.<p> 
 * 
 * @author  Michael Moossen
 * 
 * @version $Revision: 1.3 $ 
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
    public List getAll() {

        List result = new ArrayList();
        Iterator i = m_sessions.keySet().iterator();
        while (i.hasNext()) {
            CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(i.next());
            if (sessionInfo != null) {
                // may be the case in case of concurrent modification
                result.add(sessionInfo);
            }
        }
        return result;
    }

    /**
     * @see org.opencms.main.I_CmsSessionStorageProvider#getAllOfUser(org.opencms.util.CmsUUID)
     */
    public List getAllOfUser(CmsUUID userId) {

        List userSessions = new ArrayList();
        Iterator i = m_sessions.keySet().iterator();
        while (i.hasNext()) {
            CmsSessionInfo sessionInfo = (CmsSessionInfo)m_sessions.get(i.next());
            if (userId.equals(sessionInfo.getUserId())) {
                userSessions.add(sessionInfo);
            }
        }
        return userSessions;
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
            Iterator itSessions = m_sessions.entrySet().iterator();
            while (itSessions.hasNext()) {
                Map.Entry entry = (Entry)itSessions.next();
                CmsUUID sessionId = (CmsUUID)entry.getKey();
                CmsSessionInfo sessionInfo = (CmsSessionInfo)entry.getValue();
                if (sessionInfo != null && m_sessions.get(sessionId) != null) {
                    // may be the case in case of concurrent modification
                    if (sessionInfo.isExpired()) {
                        // session is invalid, try to remove it
                        try {
                            itSessions.remove();
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
}
