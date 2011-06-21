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

package org.opencms.xml.containerpage;

import org.opencms.ade.config.CmsSitemapConfigurationData;
import org.opencms.ade.detailpage.CmsSitemapDetailPageFinder;
import org.opencms.ade.detailpage.I_CmsDetailPageFinder;
import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.util.CmsJspStandardContextBean;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentPropertyHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;

/**
 * Advanced Direct Edit Manager.<p>
 * 
 * Provides all relevant functions for ADE.<p>
 * 
 * @since 7.6
 */
public class CmsADEManager {

    /** JSON property name constant. */
    protected enum FavListProp {
        /** element property. */
        ELEMENT,
        /** formatter property. */
        FORMATTER,
        /** properties property. */
        PROPERTIES;
    }

    /** The client side id/property-hash seperator. */
    public static final String CLIENT_ID_SEPERATOR = "#";

    /** The name of the module parameter which may contain the name of the ADE configuration file. */
    public static final String MODULE_CONFIG_KEY = "ade.config";

    /** The path to the sitemap editor jsp. */
    public static final String PATH_SITEMAP_EDITOR_JSP = "/system/modules/org.opencms.ade.sitemap/pages/sitemap.jsp";

    /** User additional info key constant. */
    protected static final String ADDINFO_ADE_FAVORITE_LIST = "ADE_FAVORITE_LIST";

    /** User additional info key constant. */
    protected static final String ADDINFO_ADE_RECENT_LIST = "ADE_RECENT_LIST";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEManager.class);

    /** The cache instance. */
    private CmsADECache m_cache;

    /** The configuration instance. */
    private I_CmsADEConfiguration m_configuration;

    /** The detail page finder. */
    private I_CmsDetailPageFinder m_detailPageFinder = new CmsSitemapDetailPageFinder();

    /**
     * Creates a new ADE manager.<p>
     *
     * @param adminCms a CMS context with admin privileges 
     * @param memoryMonitor the memory monitor instance
     * @param systemConfiguration the system configuration
     */
    public CmsADEManager(CmsObject adminCms, CmsMemoryMonitor memoryMonitor, CmsSystemConfiguration systemConfiguration) {

        // initialize the ade cache
        CmsADECacheSettings cacheSettings = systemConfiguration.getAdeCacheSettings();
        if (cacheSettings == null) {
            cacheSettings = new CmsADECacheSettings();
        }
        m_cache = new CmsADECache(memoryMonitor, cacheSettings);

        // initialize the ade configuration
        I_CmsADEConfiguration configuration;
        String adeConfigurationClassName = systemConfiguration.getAdeConfiguration();
        if (adeConfigurationClassName == null) {
            // use default implementation
            configuration = new CmsADEDefaultConfiguration();
        } else {
            // use configured ade configuration
            try {
                configuration = (I_CmsADEConfiguration)Class.forName(adeConfigurationClassName).newInstance();
            } catch (Exception e) {
                throw new CmsInitException(org.opencms.main.Messages.get().container(
                    org.opencms.main.Messages.ERR_CRITICAL_CLASS_CREATION_1,
                    adeConfigurationClassName), e);
            }
        }
        configuration.init(adminCms);
        m_configuration = configuration;
    }

    /**
     * Parses an element id.<p>
     * 
     * @param id the element id
     * 
     * @return the corresponding structure id
     * 
     * @throws CmsIllegalArgumentException if the id has not the right format
     */
    public CmsUUID convertToServerId(String id) throws CmsIllegalArgumentException {

        if (id == null) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_ID_1, id));
        }
        String serverId = id;
        try {
            if (serverId.contains(CLIENT_ID_SEPERATOR)) {
                serverId = serverId.substring(0, serverId.indexOf(CLIENT_ID_SEPERATOR));
            }
            return new CmsUUID(serverId);
        } catch (NumberFormatException e) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_ID_1, id));
        }
    }

    /**
     * Creates a new element of a given type at the configured location.<p>
     * 
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     * @param type the type of the element to be created
     * @param locale the content locale
     * 
     * @return the CmsResource representing the newly created element
     * 
     * @throws CmsException if something goes wrong
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#createNewElement(org.opencms.file.CmsObject, java.lang.String, javax.servlet.ServletRequest, java.lang.String, java.util.Locale)
     */
    public CmsResource createNewElement(
        CmsObject cms,
        String cntPageUri,
        ServletRequest request,
        String type,
        Locale locale) throws CmsException {

        return m_configuration.createNewElement(cms, cntPageUri, request, type, locale);
    }

    /**
     * Finds the entry point to a sitemap.<p>
     * 
     * @param cms the CMS context
     * @param openPath the resource path to find the sitemap to
     * 
     * @return the sitemap entry point
     * 
     * @throws CmsException
     */
    public String findEntryPoint(CmsObject cms, String openPath) throws CmsException {

        String openRootPath = cms.getRequestContext().addSiteRoot(openPath);
        CmsResource entryPoint = OpenCms.getADEConfigurationManager().getEntryPoint(cms, openRootPath);
        String result = cms.getSitePath(entryPoint);
        return result;
    }

    /**
     * Returns the list of creatable elements.<p>
     * 
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     * 
     * @return the list of creatable elements
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getCreatableElements(org.opencms.file.CmsObject, java.lang.String, javax.servlet.ServletRequest)
     */
    public Collection<CmsResource> getCreatableElements(CmsObject cms, String cntPageUri, ServletRequest request)
    throws CmsException {

        return m_configuration.getCreatableElements(cms, cntPageUri, request);
    }

    /**
     * Reads the current element bean from the request.<p>
     * 
     * @param req the servlet request
     * 
     * @return the element bean
     * 
     * @throws CmsException if no current element is set
     */
    public CmsContainerElementBean getCurrentElement(ServletRequest req) throws CmsException {

        CmsContainerElementBean element = CmsJspStandardContextBean.getInstance(req).getElement();
        if (element == null) {
            throw new CmsException(Messages.get().container(Messages.ERR_READING_ELEMENT_FROM_REQUEST_0));
        }
        return element;
    }

    /**
     * Gets the detail page finder.
     *
     * @return the detail page finder
     */
    public I_CmsDetailPageFinder getDetailPageFinder() {

        return m_detailPageFinder;
    }

    /**
     * Returns the property configuration for a given resource.<p>
     * 
     * @param cms the current cms context
     * @param entryPoint the the sitemap entry point
     * 
     * @return the property configuration
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, CmsXmlContentProperty> getElementPropertyConfiguration(CmsObject cms, String entryPoint)
    throws CmsException {

        CmsSitemapConfigurationData sitemapConfig = OpenCms.getADEConfigurationManager().getSitemapConfiguration(
            cms,
            cms.getRequestContext().addSiteRoot(entryPoint));
        return sitemapConfig.getPropertyConfiguration();
    }

    /**
     * Returns the element settings for a given resource.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource
     * 
     * @return the element settings for a given resource
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, CmsXmlContentProperty> getElementSettings(CmsObject cms, CmsResource resource)
    throws CmsException {

        if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
            Map<String, CmsXmlContentProperty> result = new LinkedHashMap<String, CmsXmlContentProperty>();
            Map<String, CmsXmlContentProperty> settings = CmsXmlContentDefinition.getContentHandlerForResource(
                cms,
                resource).getSettings();
            result.putAll(settings);
            return CmsXmlContentPropertyHelper.copyPropertyConfiguration(result);
        }
        return Collections.<String, CmsXmlContentProperty> emptyMap();
    }

    /**
     * Returns the favorite list, or creates it if not available.<p>
     *
     * @param cms the cms context
     * 
     * @return the favorite list
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<CmsContainerElementBean> getFavoriteList(CmsObject cms) throws CmsException {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        Object obj = user.getAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST);

        List<CmsContainerElementBean> favList = new ArrayList<CmsContainerElementBean>();
        if (obj instanceof String) {
            try {
                JSONArray array = new JSONArray((String)obj);
                for (int i = 0; i < array.length(); i++) {
                    try {
                        favList.add(elementFromJson(array.getJSONObject(i)));
                    } catch (Throwable e) {
                        // should never happen, catches wrong or no longer existing values
                        LOG.warn(e.getLocalizedMessage());
                    }
                }
            } catch (Throwable e) {
                // should never happen, catches json parsing
                LOG.warn(e.getLocalizedMessage());
            }
        } else {
            // save to be better next time
            saveFavoriteList(cms, favList);
        }

        return favList;
    }

    /**
     * Returns the maximal size of the favorite list.<p>
     * 
     * @param cms the cms context
     * 
     * @return the maximal size of the favorite list
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getFavoriteListMaxSize(CmsObject)
     */
    public int getFavoriteListMaxSize(CmsObject cms) throws CmsException {

        return m_configuration.getFavoriteListMaxSize(cms);
    }

    /**
     * Returns the formatter configuration for a given resource.<p>
     * 
     * @param cms the OpenCms user context 
     * @param containerPageRootPath the root path to the container page that includes the element resource
     * @param res the container page element resource 
     * 
     * @return the formatter configuration for a given resource
     *   
     * @throws CmsException if something goes wrong  
     */
    public CmsFormatterConfiguration getFormattersForResource(
        CmsObject cms,
        String containerPageRootPath,
        CmsResource res) throws CmsException {

        return m_configuration.getFormattersForResource(cms, containerPageRootPath, res);
    }

    /**
     * Returns the favorite list, or creates it if not available.<p>
     *
     * @param cms the cms context
     * 
     * @return the favorite list
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<CmsContainerElementBean> getRecentList(CmsObject cms) throws CmsException {

        CmsUser user = cms.getRequestContext().getCurrentUser();
        Object obj = user.getAdditionalInfo(ADDINFO_ADE_RECENT_LIST);

        List<CmsContainerElementBean> recentList = new ArrayList<CmsContainerElementBean>();
        if (obj instanceof String) {
            try {
                JSONArray array = new JSONArray((String)obj);
                for (int i = 0; i < array.length(); i++) {
                    try {
                        recentList.add(elementFromJson(array.getJSONObject(i)));
                    } catch (Throwable e) {
                        // should never happen, catches wrong or no longer existing values
                        LOG.warn(e.getLocalizedMessage());
                    }
                }
            } catch (Throwable e) {
                // should never happen, catches json parsing
                LOG.warn(e.getLocalizedMessage());
            }
        } else {
            // save to be better next time
            saveRecentList(cms, recentList);
        }

        return recentList;
    }

    /**
     * Returns the maximal size of the recent list.<p>
     * 
     * @param cms the cms context
     * 
     * @return the maximal size of the recent list
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getRecentListMaxSize(CmsObject)
     */
    public int getRecentListMaxSize(CmsObject cms) throws CmsException {

        return m_configuration.getRecentListMaxSize(cms);
    }

    /**
     * Returns the list of searchable resource types.<p>
     * 
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     * 
     * @return the list of searchable resource types, identified by a sample resource
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getSearchableResourceTypes(org.opencms.file.CmsObject, java.lang.String, javax.servlet.ServletRequest)
     */
    public Collection<CmsResource> getSearchableResourceTypes(CmsObject cms, String cntPageUri, ServletRequest request)
    throws CmsException {

        return m_configuration.getSearchableResourceTypes(cms, cntPageUri, request);
    }

    /**
     * Returns the size of a search page.<p>
     * 
     * @param cms the cms context
     * 
     * @return the maximal size of the search page
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getSearchPageSize(CmsObject)
     */
    public int getSearchPageSize(CmsObject cms) throws CmsException {

        return m_configuration.getSearchPageSize(cms);
    }

    /**
     * Returns if the given type has a valid configuration to be created.<p>
     * 
     * @param cms the CMS context
     * @param currentUri the current URI
     * @param typeName the resource type name
     * 
     * @return <code>true</code> if the type can be created as new
     * 
     * @throws CmsException if something goes wrong
     */
    public boolean isCreatableType(CmsObject cms, String currentUri, String typeName) throws CmsException {

        return m_configuration.isCreatableType(cms, currentUri, typeName);
    }

    /**
     * Saves the favorite list, user based.<p>
     * 
     * @param cms the cms context
     * @param favoriteList the element list
     * 
     * @throws CmsException if something goes wrong 
     */
    public void saveFavoriteList(CmsObject cms, List<CmsContainerElementBean> favoriteList) throws CmsException {

        JSONArray data = new JSONArray();
        for (CmsContainerElementBean element : favoriteList) {
            data.put(elementToJson(element));
        }
        CmsUser user = cms.getRequestContext().getCurrentUser();
        user.setAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST, data.toString());
        cms.writeUser(user);
    }

    /**
     * Saves the favorite list, user based.<p>
     * 
     * @param cms the cms context
     * @param recentList the element list
     * 
     * @throws CmsException if something goes wrong 
     */
    public void saveRecentList(CmsObject cms, List<CmsContainerElementBean> recentList) throws CmsException {

        JSONArray data = new JSONArray();
        for (CmsContainerElementBean element : recentList) {
            data.put(elementToJson(element));
        }
        CmsUser user = cms.getRequestContext().getCurrentUser();
        user.setAdditionalInfo(ADDINFO_ADE_RECENT_LIST, data.toString());
        cms.writeUser(user);
    }

    /**
     * Clean up at shutdown time. Only intended to be called at system shutdown.<p>
     * 
     * @see org.opencms.main.OpenCmsCore#shutDown
     */
    public void shutdown() {

        m_cache.shutdown();
    }

    /**
     * Creates an element from its serialized data.<p> 
     * 
     * @param data the serialized data
     * 
     * @return the restored element bean
     * 
     * @throws JSONException if the serialized data got corrupted
     */
    protected CmsContainerElementBean elementFromJson(JSONObject data) throws JSONException {

        CmsUUID element = new CmsUUID(data.getString(FavListProp.ELEMENT.name().toLowerCase()));
        CmsUUID formatter = null;
        if (data.has(FavListProp.FORMATTER.name().toLowerCase())) {
            formatter = new CmsUUID(data.getString(FavListProp.FORMATTER.name().toLowerCase()));
        }
        Map<String, String> properties = new HashMap<String, String>();

        JSONObject props = data.getJSONObject(FavListProp.PROPERTIES.name().toLowerCase());
        Iterator<String> keys = props.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            properties.put(key, props.getString(key));
        }

        return new CmsContainerElementBean(element, formatter, properties, false);
    }

    /**
     * Converts the given element to JSON.<p>
     * 
     * @param element the element to convert
     * 
     * @return the JSON representation
     */
    protected JSONObject elementToJson(CmsContainerElementBean element) {

        JSONObject data = null;
        try {
            data = new JSONObject();
            data.put(FavListProp.ELEMENT.name().toLowerCase(), element.getId().toString());
            if (element.getFormatterId() != null) {
                data.put(FavListProp.FORMATTER.name().toLowerCase(), element.getFormatterId().toString());
            }
            JSONObject properties = new JSONObject();
            for (Map.Entry<String, String> entry : element.getIndividualSettings().entrySet()) {
                properties.put(entry.getKey(), entry.getValue());
            }
            data.put(FavListProp.PROPERTIES.name().toLowerCase(), properties);
        } catch (JSONException e) {
            // should never happen
            if (!LOG.isDebugEnabled()) {
                LOG.warn(e.getLocalizedMessage());
            }
            LOG.debug(e.getLocalizedMessage(), e);
            return null;
        }
        return data;
    }

    /**
     * Returns the cache.<p>
     *
     * @return the cache
     */
    protected CmsADECache getCache() {

        return m_cache;
    }
}
