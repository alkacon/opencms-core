/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;

import org.opencms.ade.configuration.CmsElementView;
import org.opencms.configuration.preferences.CmsElementViewPreference;
import org.opencms.file.CmsObject;
import org.opencms.jsp.util.CmsJspStandardContextBean.TemplateBean;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsXmlContent;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.list.NodeCachingLinkedList;

/**
 * ADE's session cache.<p>
 * 
 * @since 8.0.0
 */
public final class CmsADESessionCache {

    /** Session attribute name constant. */
    public static final String SESSION_ATTR_ADE_CACHE = "__OCMS_ADE_CACHE__";

    /** The container elements. */
    private Map<String, CmsContainerElementBean> m_containerElements;

    /** The current element view id. */
    private CmsUUID m_elementView;

    /** Flag which controls whether small elements should be shown. */
    private boolean m_isEditSmallElements;

    /** The ADE recent list. */
    private List<CmsContainerElementBean> m_recentLists;

    /** Template bean cache. */
    private Map<String, TemplateBean> m_templateBeanCache = new HashMap<String, TemplateBean>();

    /** The tool-bar visibility flag. */
    private boolean m_toolbarVisible;

    /** The cached XML content documents by structure id. */
    private Map<CmsUUID, CmsXmlContent> m_xmlContents;

    /**
     * Initializes the session cache.<p>
     * 
     * @param cms the cms context
     * @param request the current request
     */
    protected CmsADESessionCache(CmsObject cms, HttpServletRequest request) {

        // container element cache
        Map<String, CmsContainerElementBean> lruMapCntElem = new HashMap<String, CmsContainerElementBean>();
        m_containerElements = Collections.synchronizedMap(lruMapCntElem);

        // ADE recent lists
        int maxElems = 10;
        maxElems = OpenCms.getADEManager().getRecentListMaxSize(cms.getRequestContext().getCurrentUser());
        List<CmsContainerElementBean> adeRecentList = CmsCollectionsGenericWrapper.list(new NodeCachingLinkedList(
            maxElems));
        m_recentLists = Collections.synchronizedList(adeRecentList);

        // XML content cache, used during XML content edit
        m_xmlContents = Collections.synchronizedMap(new HashMap<CmsUUID, CmsXmlContent>());

        String elementView = null;
        // within the test cases the request will be null 
        if (request != null) {
            elementView = CmsWorkplace.getWorkplaceSettings(cms, request).getUserSettings().getAdditionalPreference(
                CmsElementViewPreference.PREFERENCE_NAME,
                false);
        }
        if (elementView == null) {
            // use the default element view
            m_elementView = CmsElementView.DEFAULT_ELEMENT_VIEW.getId();
        } else {
            try {
                m_elementView = new CmsUUID(elementView);
            } catch (NumberFormatException e) {
                // use the default element view
                m_elementView = CmsElementView.DEFAULT_ELEMENT_VIEW.getId();
            }
        }
    }

    /**
     * Gets the session cache for the current session.<p>
     * 
     * @param request the current request
     * @param cms the current CMS context
     *  
     * @return the ADE session cache for the current session 
     */
    public static CmsADESessionCache getCache(HttpServletRequest request, CmsObject cms) {

        CmsADESessionCache cache = (CmsADESessionCache)request.getSession().getAttribute(
            CmsADESessionCache.SESSION_ATTR_ADE_CACHE);
        if (cache == null) {
            cache = new CmsADESessionCache(cms, request);
            request.getSession().setAttribute(CmsADESessionCache.SESSION_ATTR_ADE_CACHE, cache);
        }
        return cache;
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
     * Returns the cached XML content document.<p>
     * 
     * @param structureId the structure id
     * 
     * @return the XML document
     */
    public CmsXmlContent getCacheXmlContent(CmsUUID structureId) {

        return m_xmlContents.get(structureId);
    }

    /**
     * Returns the current element view id.<p>
     * 
     * @return the current element view id
     */
    public CmsUUID getElementView() {

        return m_elementView;
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
     * Gets the cached template bean for a given container page uri.<p>
     * 
     * @param uri the container page uri 
     * @param safe if true, return a valid template bean even if it hasn't been cached before 
     * 
     * @return the template bean 
     */
    public TemplateBean getTemplateBean(String uri, boolean safe) {

        TemplateBean templateBean = m_templateBeanCache.get(uri);
        if ((templateBean != null) || !safe) {
            return templateBean;
        }
        return new TemplateBean("", "");
    }

    /** 
     * Returns true if, in this session, a newly opened container page editor window should display edit points for 
     * small elements initially.<p>
     * 
     * @return true if small elements should be editable initially 
     */
    public boolean isEditSmallElements() {

        return m_isEditSmallElements;
    }

    /**
     * Returns the tool-bar visibility.<p>
     *
     * @return the tool-bar visibility
     */
    public boolean isToolbarVisible() {

        return m_toolbarVisible;
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

    /**
     * Caches the given recent list.<p>
     * 
     * @param list the recent list to cache
     */
    public void setCacheRecentList(List<CmsContainerElementBean> list) {

        m_recentLists.clear();
        m_recentLists.addAll(list);
        for (CmsContainerElementBean element : m_recentLists) {
            setCacheContainerElement(element.editorHash(), element);
        }
    }

    /**
     * Caches the given XML content document.<p>
     * 
     * @param structureId the structure id
     * @param xmlContent the XML document
     */
    public void setCacheXmlContent(CmsUUID structureId, CmsXmlContent xmlContent) {

        m_xmlContents.put(structureId, xmlContent);
    }

    /** 
     * Sets the default initial setting for small element editability in this session.<p>
     * 
     * @param editSmallElements true if small elements should be initially editable 
     */
    public void setEditSmallElements(boolean editSmallElements) {

        m_isEditSmallElements = editSmallElements;
    }

    /**
     * Sets the current element view id.<p>
     * 
     * @param elementView the current element view id
     */
    public void setElementView(CmsUUID elementView) {

        m_elementView = elementView;
    }

    /**
     * Caches a template bean for a given container page URI.<p>
     * 
     * @param uri the container page uri 
     * @param templateBean the template bean to cache 
     */
    public void setTemplateBean(String uri, TemplateBean templateBean) {

        m_templateBeanCache.put(uri, templateBean);
    }

    /**
     * Sets the tool-bar visibility flag.<p>
     *
     * @param toolbarVisible the tool-bar visibility to set
     */
    public void setToolbarVisible(boolean toolbarVisible) {

        m_toolbarVisible = toolbarVisible;
    }

    /**
     * Purges the XML content document by the given id from the cache.<p>
     * 
     * @param structureId the structure id
     */
    public void uncacheXmlContent(CmsUUID structureId) {

        m_xmlContents.remove(structureId);
    }
}
