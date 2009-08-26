/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADERecentListManager.java,v $
 * Date   : $Date: 2009/08/26 12:28:54 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.list.NodeCachingLinkedList;
import org.apache.commons.logging.Log;

/**
 * Maintains recent element lists.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 7.6
 */
public final class CmsADERecentListManager {

    /** User additional info key constant. */
    private static final String ADDINFO_ADE_RECENTLIST_SIZE = "ADE_RECENTLIST_SIZE";

    /** default recent list size constant. */
    private static final int DEFAULT_RECENT_LIST_SIZE = 10;

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADERecentListManager.class);

    /** Singleton instance. */
    private static CmsADERecentListManager m_instance;

    /** Recent list cache. */
    private Map<String, List<CmsUUID>> m_recentListCache;

    /**
     * Creates a new instance.<p>
     */
    private CmsADERecentListManager() {

        m_recentListCache = new HashMap<String, List<CmsUUID>>();
    }

    /**
     * Returns the singleton instance.<p>
     * 
     * @return the singleton instance
     */
    public static CmsADERecentListManager getInstance() {

        if (m_instance == null) {
            m_instance = new CmsADERecentListManager();
        }
        return m_instance;
    }

    /**
     * Returns the current user's recent list.<p>
     * 
     * @param cms the cms context 
     * @param resElements the current page's element list
     * @param types the supported container types
     * @param req the http request
     * @param res the http response
     * 
     * @return the current user's recent list
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong in the json manipulation
     */
    public JSONArray getRecentList(
        CmsObject cms,
        JSONObject resElements,
        Collection<String> types,
        HttpServletRequest req,
        HttpServletResponse res) throws JSONException, CmsException {

        CmsADEElementUtil elemUtil = new CmsADEElementUtil(cms, req, res);

        JSONArray result = new JSONArray();
        // get the cached list
        List<CmsUUID> recentList = getRecentListFromCache(cms);
        // iterate the list and create the missing elements
        for (CmsUUID structureId : recentList) {
            String id = CmsADEElementUtil.ADE_ID_PREFIX + structureId.toString();
            result.put(id);
            if ((resElements != null) && !resElements.has(id)) {
                resElements.put(id, elemUtil.getElementData(structureId, types));
            }
        }

        return result;
    }

    /**
     * Sets the recent list.<p>
     * 
     * @param cms the cms context
     * @param list the element id list
     */
    public void setRecentList(CmsObject cms, JSONArray list) {

        List<CmsUUID> recentList = getRecentListFromCache(cms);
        recentList.clear();
        for (int i = 0; i < list.length(); i++) {
            try {
                recentList.add(CmsADEElementUtil.parseId(list.optString(i)));
            } catch (CmsIllegalArgumentException t) {
                LOG.warn(Messages.get().container(Messages.ERR_INVALID_ID_1, list.optString(i)), t);
            }
        }
    }

    /**
     * Returns the cached list, or creates it if not available.<p>
     * 
     * @param cms the current cms context
     * 
     * @return the cached recent list
     */
    protected List<CmsUUID> getRecentListFromCache(CmsObject cms) {

        CmsUser user = cms.getRequestContext().currentUser();
        List<CmsUUID> recentList = m_recentListCache.get(user.getId().toString());
        if (recentList == null) {
            Integer maxElems = (Integer)user.getAdditionalInfo(ADDINFO_ADE_RECENTLIST_SIZE);
            if (maxElems == null) {
                maxElems = new Integer(DEFAULT_RECENT_LIST_SIZE);
            }
            recentList = new NodeCachingLinkedList(maxElems.intValue());
            m_recentListCache.put(user.getId().toString(), recentList);
        }
        return recentList;
    }
}
