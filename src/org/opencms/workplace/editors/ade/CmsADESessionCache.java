/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADESessionCache.java,v $
 * Date   : $Date: 2009/11/04 13:53:48 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.xml.containerpage.CmsContainerElementBean;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.list.NodeCachingLinkedList;
import org.apache.commons.logging.Log;

/**
 * ADE's session cache.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 7.9.2
 */
public final class CmsADESessionCache {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADESessionCache.class);

    /** The ADE recent list. */
    private List<CmsContainerElementBean> m_recentLists;

    /** The ADE search options. */
    private CmsSearchOptions m_searchOptions;

    /** The container elements. */
    private Map<String, CmsContainerElementBean> m_containerElements;

    /** The ADE publish options. */
    private CmsPublishOptions m_publishOptions;

    /** Session attribute name constant. */
    public static final String SESSION_ATTR_ADE_CACHE = "__OCMS_ADE_CACHE__";

    /**
     * Initializes the session cache.<p>
     * 
     * @param cms the cms context
     */
    public CmsADESessionCache(CmsObject cms) {

        // container element cache
        Map<String, CmsContainerElementBean> lruMapCntElem = new HashMap<String, CmsContainerElementBean>();
        m_containerElements = Collections.synchronizedMap(lruMapCntElem);

        // ADE search options
        m_searchOptions = null;

        // ADE publish options
        m_publishOptions = new CmsPublishOptions();

        // ADE recent lists
        int maxElems = 10;
        try {
            maxElems = OpenCms.getADEManager().getRecentListMaxSize(cms);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        List<CmsContainerElementBean> adeRecentList = CmsCollectionsGenericWrapper.list(new NodeCachingLinkedList(
            maxElems));
        m_recentLists = Collections.synchronizedList(adeRecentList);
    }

    /**
     * Returns the cached recent list.<p>
     * 
     * @return the cached recent list
     */
    public List<CmsContainerElementBean> getRecentList() {

        return m_recentLists;
    }

    /**
     * Returns the search options.<p>
     * 
     * @return the cached search options
     */
    public CmsSearchOptions getSearchOptions() {

        return m_searchOptions;
    }

    /**
     * Returns the publish options.<p>
     * 
     * @return the cached publish options
     */
    public CmsPublishOptions getPublishOptions() {

        return m_publishOptions;
    }

    /**
     * Returns the cached container element under the given key.<p>
     * 
     * @param key the cache key
     * 
     * @return  the cached container element or <code>null</code> if not found
     */
    public CmsContainerElementBean getCacheContainerElement(String key) {

        return m_containerElements.get(key);
    }

    /**
     * Caches the given recent list.<p>
     * 
     * @param list the recent list to cache
     */
    public void setCacheRecentList(List<CmsContainerElementBean> list) {

        m_recentLists.clear();
        m_recentLists.addAll(list);
        for (CmsContainerElementBean element : m_recentLists) {
            setCacheContainerElement(element.getClientId(), element);
        }
    }

    /**
     * Caches the given search options.<p>
     * 
     * @param opts the search options to cache
     */
    public void setCacheSearchOptions(CmsSearchOptions opts) {

        if (opts == null) {
            m_searchOptions = null;
        } else {
            m_searchOptions = opts.resetPage();
        }
    }

    /**
     * Caches the given publish options.<p>
     * 
     * @param opts the publish options to cache
     */
    public void setCachePublishOptions(CmsPublishOptions opts) {

        if (opts == null) {
            m_publishOptions = new CmsPublishOptions();
        } else {
            m_publishOptions = opts;
        }
    }

    /**
     * Caches the given container element under the given key.<p>
     * 
     * @param key the cache key
     * @param containerElement the object to cache
     */
    public void setCacheContainerElement(String key, CmsContainerElementBean containerElement) {

        m_containerElements.put(key, containerElement);
    }
}
