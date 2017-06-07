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

package org.opencms.xml.containerpage;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.configuration.CmsElementView;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.sitemap.shared.CmsSitemapData.EditorMode;
import org.opencms.configuration.preferences.CmsElementViewPreference;
import org.opencms.file.CmsObject;
import org.opencms.jsp.util.CmsJspStandardContextBean.TemplateBean;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.content.CmsXmlContent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;

/**
 * ADE's session cache.<p>
 *
 * @since 8.0.0
 */
public final class CmsADESessionCache {

    /**
     * Stores information about the container page which was last edited, so we can jump back to it later.<p>
     */
    public static class LastPageBean {

        /** The detail id (may be null). */
        private CmsUUID m_detailId;

        /** The page structure id. */
        private CmsUUID m_pageId;

        /** The site root. */
        private String m_siteRoot;

        /**
         * Creates a new instance.<p>
         *
         * @param siteRoot the site root
         * @param pageId the page id
         * @param detailId the detail content id (may be null)
         */
        public LastPageBean(String siteRoot, CmsUUID pageId, CmsUUID detailId) {
            super();
            m_siteRoot = siteRoot;
            m_pageId = pageId;
            m_detailId = detailId;
        }

        /**
         * Returns the detailId.<p>
         *
         * @return the detailId
         */
        public CmsUUID getDetailId() {

            return m_detailId;
        }

        /**
         * Returns the pageId.<p>
         *
         * @return the pageId
         */
        public CmsUUID getPageId() {

            return m_pageId;
        }

        /**
         * Returns the siteRoot.<p>
         *
         * @return the siteRoot
         */
        public String getSiteRoot() {

            return m_siteRoot;
        }

    }

    /** Session attribute name constant. */
    public static final String SESSION_ATTR_ADE_CACHE = "__OCMS_ADE_CACHE__";

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADESessionCache.class);

    /** The list size for recently used formatters. */
    private static final int RECENT_FORMATTERS_SIZE = 10;

    /** The container elements. */
    private Map<String, CmsContainerElementBean> m_containerElements;

    /** The current values of dynamically loaded attributes in the Acacia editor. */
    private Map<String, String> m_dynamicValues;

    /** The current element view id. */
    private CmsUUID m_elementView;

    /** Flag which controls whether small elements should be shown. */
    private boolean m_isEditSmallElements;

    /** Bean containing last page info. */
    private LastPageBean m_lastPage;

    /** The last stored gallery search for the page editor. */
    private CmsGallerySearchBean m_lastPageEditorGallerySearch;

    /** The recently used formatters by resource type. */
    private Map<String, List<CmsUUID>> m_recentFormatters = new ConcurrentHashMap<String, List<CmsUUID>>();

    /** The sitemap editor mode. */
    private EditorMode m_sitemapEditorMode;

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
        m_containerElements = new ConcurrentHashMap<String, CmsContainerElementBean>();

        // XML content cache, used during XML content edit
        m_xmlContents = new ConcurrentHashMap<CmsUUID, CmsXmlContent>();

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
                LOG.warn("Malformed element view id '" + elementView + "'.", e);
            }
        }
        // toolbar should be visible initially
        m_toolbarVisible = true;
    }

    /**
     * Gets the session cache for the current session.<p>
     * In case the request is not editable, <code>null</code> will be returned.<p>
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
     * Adds the formatter id to the recently used list for the given type.<p>
     *
     * @param resType the resource type
     * @param formatterId the formatter id
     */
    public void addRecentFormatter(String resType, CmsUUID formatterId) {

        List<CmsUUID> formatterIds = m_recentFormatters.get(resType);
        if (formatterIds == null) {
            formatterIds = new ArrayList<CmsUUID>();
            m_recentFormatters.put(resType, formatterIds);
        }
        formatterIds.remove(formatterId);
        if (formatterIds.size() >= (RECENT_FORMATTERS_SIZE)) {
            formatterIds.remove(RECENT_FORMATTERS_SIZE - 1);
        }
        formatterIds.add(0, formatterId);
    }

    /**
     * Clear the cache values that are dynamically loaded in the Acacia content editor.
     */
    public void clearDynamicValues() {

        m_dynamicValues = null;
    }

    /**
     * Removes the information about the last edited container page.<p>
     */
    public void clearLastPage() {

        m_lastPage = null;
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
     * Get cached value that is dynamically loaded by the Acacia content editor.
     *
     * @param attribute the attribute to load the value to
     * @return the cached value
     */
    public String getDynamicValue(String attribute) {

        return null == m_dynamicValues ? null : m_dynamicValues.get(attribute);
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
     * Returns the lastPage.<p>
     *
     * @return the lastPage
     */
    public LastPageBean getLastPage() {

        return m_lastPage;
    }

    /**
     * Returns the lastPageEditorGallerySearch.<p>
     *
     * @return the lastPageEditorGallerySearch
     */
    public CmsGallerySearchBean getLastPageEditorGallerySearch() {

        return m_lastPageEditorGallerySearch;
    }

    /**
     * Returns the least recently used matching formatter for the given resource type.<p>
     *
     * @param resType the resource type
     * @param container the container to match
     * @param allowNested in case nested containers are allowed
     * @param config the config data
     *
     * @return the formatter if any
     */
    public I_CmsFormatterBean getRecentFormatter(
        String resType,
        CmsContainer container,
        boolean allowNested,
        CmsADEConfigData config) {

        I_CmsFormatterBean result = null;
        List<CmsUUID> formatterIds = m_recentFormatters.get(resType);
        if (formatterIds != null) {
            Map<CmsUUID, I_CmsFormatterBean> availableFormatters = config.getActiveFormatters();
            Set<String> types = new HashSet<String>(Arrays.asList(container.getType().trim().split(" *, *")));
            for (CmsUUID id : formatterIds) {
                I_CmsFormatterBean formatter = availableFormatters.get(id);
                if ((formatter != null)
                    && CmsFormatterConfiguration.matchFormatter(formatter, types, container.getWidth(), allowNested)) {
                    result = formatter;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Returns the sitemap editor mode.<p>
     *
     * @return the sitemap editor mode
     */
    public EditorMode getSitemapEditorMode() {

        return m_sitemapEditorMode;
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
     * Caches the given XML content document.<p>
     *
     * @param structureId the structure id
     * @param xmlContent the XML document
     */
    public void setCacheXmlContent(CmsUUID structureId, CmsXmlContent xmlContent) {

        m_xmlContents.put(structureId, xmlContent);
    }

    /**
     * Set cached value for the attribute. Used for dynamically loaded values in the Acacia content editor.
     *
     * @param attribute the attribute for which the value should be cached
     * @param value the value to cache
     */
    public void setDynamicValue(String attribute, String value) {

        if (null == m_dynamicValues) {
            m_dynamicValues = new ConcurrentHashMap<String, String>();
        }
        m_dynamicValues.put(attribute, value);
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
     * Stores information about the last edited container page.<p>
     *
     * @param cms the CMS context
     * @param pageId the page id
     * @param detailId the detail content id
     */
    public void setLastPage(CmsObject cms, CmsUUID pageId, CmsUUID detailId) {

        m_lastPage = new LastPageBean(cms.getRequestContext().getSiteRoot(), pageId, detailId);

    }

    /**
     * Sets the last stored gallery search from the page editor.<p>
     *
     * @param searchObj the search to store
     */
    public void setLastPageEditorGallerySearch(CmsGallerySearchBean searchObj) {

        m_lastPageEditorGallerySearch = searchObj;
    }

    /**
     * Sets the sitemap editor mode.<p>
     *
     * @param sitemapEditorMode the sitemap editor mode
     */
    public void setSitemapEditorMode(EditorMode sitemapEditorMode) {

        m_sitemapEditorMode = sitemapEditorMode;
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
        m_dynamicValues = null;
    }
}
