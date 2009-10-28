/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADESessionCache.java,v $
 * Date   : $Date: 2009/10/28 15:38:11 $
 * Version: $Revision: 1.3 $
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
 * @version $Revision: 1.3 $ 
 * 
 * @since 7.9.2
 */
public final class CmsADESessionCache {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADESessionCache.class);

    /** The ADE recent list. */
    private List<CmsContainerElementBean> m_adeRecentLists;

    /** The ADE search options. */
    private CmsSearchOptions m_adeSearchOptions;

    /** The container elements. */
    private Map<String, CmsContainerElementBean> m_containerElements;

    /** The ADE publish options. */
    private CmsPublishOptions m_adePublishOptions;

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
        m_adeSearchOptions = null;

        // ADE publish options
        m_adePublishOptions = new CmsPublishOptions();

        // ADE recent lists
        int maxElems = 10;
        try {
            maxElems = OpenCms.getADEManager().getRecentListMaxSize(cms);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
        List<CmsContainerElementBean> adeRecentList = CmsCollectionsGenericWrapper.list(new NodeCachingLinkedList(
            maxElems));
        m_adeRecentLists = Collections.synchronizedList(adeRecentList);
    }

    /**
     * Returns the cached ADE recent list.<p>
     * 
     * @return the cached recent list or <code>null</code> if not found
     */
    public List<CmsContainerElementBean> getADERecentList() {

        return m_adeRecentLists;
    }

    /**
     * Returns the ADE search options.<p>
     * 
     * @return the cached search options or <code>null</code> if not found
     */
    public CmsSearchOptions getADESearchOptions() {

        return m_adeSearchOptions;
    }

    /**
     * Returns the ADE publish options.<p>
     * 
     * @return the cached publish options or <code>null</code> if not found
     */
    public CmsPublishOptions getADEPublishOptions() {

        return m_adePublishOptions;
    }

    /**
     * Returns the cached container element under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * 
     * @return  the cached container element or <code>null</code> if not found
     */
    public CmsContainerElementBean getCacheContainerElement(String key) {

        return m_containerElements.get(key);
    }

    /**
     * Caches the given ADE recent list.<p>
     * 
     * @param list the recent list to cache
     */
    public void setCacheADERecentList(List<CmsContainerElementBean> list) {

        m_adeRecentLists.clear();
        m_adeRecentLists.addAll(list);
        for (CmsContainerElementBean element : m_adeRecentLists) {
            setCacheContainerElement(element.getClientId(), element);
        }
    }

    /**
     * Caches the given ADE search options.<p>
     * 
     * @param opts the search options to cache
     */
    public void setCacheADESearchOptions(CmsSearchOptions opts) {

        if (opts == null) {
            m_adeSearchOptions = null;
        } else {
            m_adeSearchOptions = opts.resetPage();
        }
    }

    /**
     * Caches the given ADE publish options.<p>
     * 
     * @param opts the publish options to cache
     */
    public void setCacheADEPublishOptions(CmsPublishOptions opts) {

        if (opts == null) {
            m_adePublishOptions = new CmsPublishOptions();
        } else {
            m_adePublishOptions = opts;
        }
    }

    /**
     * Caches the given container element under the given key and for the given project.<p>
     * 
     * @param key the cache key
     * @param containerElement the object to cache
     */
    public void setCacheContainerElement(String key, CmsContainerElementBean containerElement) {

        m_containerElements.put(key, containerElement);
    }
}
