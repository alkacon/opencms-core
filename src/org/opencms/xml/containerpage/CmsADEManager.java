/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsADEManager.java,v $
 * Date   : $Date: 2009/10/14 14:38:02 $
 * Version: $Revision: 1.1.2.3 $
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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.monitor.CmsMemoryMonitor;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.collections.list.NodeCachingLinkedList;
import org.apache.commons.logging.Log;

/**
 * Advanced Direct Edit Manager.<p>
 * 
 * Provides all relevant functions for ADE.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.3 $
 * 
 * @since 7.6
 */
public class CmsADEManager {

    /** The request attribute name for the current element-bean. */
    public static final String ATTR_CURRENT_ELEMENT = "__currentElement";

    /** User additional info key constant. */
    protected static final String ADDINFO_ADE_FAVORITE_LIST = "ADE_FAVORITE_LIST";

    /** HTML id prefix constant. */
    protected static final String ADE_ID_PREFIX = "ade_";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEManager.class);

    /** The admin cms context. */
    protected CmsObject m_adminCms;

    /** The cache instance. */
    protected CmsADECache m_cache;

    /** The configuration instance. */
    protected I_CmsADEConfiguration m_configuration;

    /**
     * Creates a new ADE manager.<p>
     * 
     * @param adminCms the admin cms context
     * @param memoryMonitor the memory monitor
     * @param cacheSettings the cache settings
     * @param configuration the configured configuration object
     * 
     * @throws CmsException if something goes wrong 
     */
    public CmsADEManager(
        CmsObject adminCms,
        CmsMemoryMonitor memoryMonitor,
        CmsADECacheSettings cacheSettings,
        I_CmsADEConfiguration configuration)
    throws CmsException {

        m_cache = new CmsADECache(memoryMonitor, cacheSettings);
        m_configuration = configuration;
        m_adminCms = OpenCms.initCmsObject(adminCms);
    }

    /**
     * Creates a valid html id from an uuid.<p>
     * 
     * @param id the uuid
     * 
     * @return the generated html id
     */
    public String convertToClientId(CmsUUID id) {

        return ADE_ID_PREFIX + id.toString();
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

        if ((id == null) || (!id.startsWith(ADE_ID_PREFIX))) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_ID_1, id));
        }
        String serverId = id;
        try {
            if (serverId.contains("#")) {
                serverId = serverId.substring(ADE_ID_PREFIX.length(), serverId.indexOf("#"));
            } else {
                serverId = serverId.substring(ADE_ID_PREFIX.length());
            }

            return new CmsUUID(serverId);
        } catch (NumberFormatException e) {
            throw new CmsIllegalArgumentException(Messages.get().container(Messages.ERR_INVALID_ID_1, id));
        }
    }

    /**
     * Returns the cached container page object for the given resource.<p>
     * 
     * @param cms the cms context
     * @param resource the resource to look for
     * @param locale the locale
     *  
     * @return the cached container page object
     */
    public I_CmsContainerPageBean getCache(CmsObject cms, CmsResource resource, Locale locale) {

        return m_cache.getCache(cms, resource, locale);
    }

    /**
     * Reads the cached element-bean for the given client-side-id from cache.<p>
     * 
     * @param clientId the client-side-id
     * @return the CmsContainerElementBean
     * @throws CmsException - if the resource could not be read for any reason
     */
    public I_CmsContainerElementBean getCachedElement(String clientId) throws CmsException {

        try {
            return m_cache.getCacheContainerElement(clientId);
        } catch (Exception e) {
            // may happen if element was not cached
        }
        I_CmsContainerElementBean element = null;
        if (clientId.contains("#")) {
            String id = clientId.substring(0, clientId.indexOf("#"));
            try {
                element = m_cache.getCacheContainerElement(id);
            } catch (Exception e) {
                // may happen if element was not cached
            }
            if (element != null) {
                return element;
            }
        }
        // TODO: this is out of question! remove it
        element = new CmsContainerElementBean(m_adminCms.readResource(convertToServerId(clientId)), null, null);
        m_cache.cacheContainerElement(clientId, element);
        return element;
    }

    /**
     * Returns the container page manager.<p>
     * 
     * @param cms the current cms context
     * @param cntPageUri the container page uri
     * @param request the current request
     * 
     * @return the container page manager for the given container page
     */
    public CmsCntPageManager getCntPageManager(CmsObject cms, String cntPageUri, ServletRequest request) {

        return new CmsCntPageManager(cms, cntPageUri, request, m_configuration);
    }

    /**
     * Reads the current element bean from the request.<p>
     * 
     * @param req the servlet request
     * 
     * @return the element bean
     * 
     * @throws CmsException if attribute {@value #ATTR_CURRENT_ELEMENT} not set, or if a type cast exception occurs
     */
    public I_CmsContainerElementBean getCurrentElement(ServletRequest req) throws CmsException {

        I_CmsContainerElementBean element = null;
        try {
            element = (I_CmsContainerElementBean)req.getAttribute(ATTR_CURRENT_ELEMENT);
        } catch (Exception e) {
            throw new CmsException(Messages.get().container(Messages.ERR_READING_ELEMENT_FROM_REQUEST_0), e);
        }
        if (element == null) {
            throw new CmsException(Messages.get().container(Messages.ERR_READING_ELEMENT_FROM_REQUEST_0));
        }
        return element;
    }

    /**
     * Returns an element properties, taking into account default values.<p>
     * 
     * @param element the element to get the properties for
     * 
     * @return the element properties
     */
    public Map<String, CmsProperty> getElementProperties(I_CmsContainerElementBean element) {

        Map<String, CmsProperty> properties = new HashMap<String, CmsProperty>();
        Iterator<Map.Entry<String, String>> itProperties = element.getProperties().entrySet().iterator();
        while (itProperties.hasNext()) {
            Map.Entry<String, String> entry = itProperties.next();
            String propertyName = entry.getKey();
            CmsProperty property = new CmsProperty(propertyName, entry.getValue(), null);
            properties.put(propertyName, property);
        }
        try {
            Map<String, CmsXmlContentProperty> propertyDefs = getElementPropertyConfiguration(element.getElement());
            Iterator<Map.Entry<String, CmsXmlContentProperty>> itPropertyDefs = propertyDefs.entrySet().iterator();
            while (itPropertyDefs.hasNext()) {
                Map.Entry<String, CmsXmlContentProperty> entry = itPropertyDefs.next();
                String propertyName = entry.getKey();
                String defaultValue = entry.getValue().getDefault();
                if (properties.containsKey(propertyName)) {
                    properties.get(propertyName).setResourceValue(defaultValue);
                } else {
                    properties.put(propertyName, new CmsProperty(propertyName, null, defaultValue));
                }
            }
        } catch (Exception e) {
            LOG.error(Messages.get().getBundle().key(
                Messages.ERR_READ_ELEMENT_PROPERTY_CONFIGURATION_1,
                element.getElement().getRootPath()), e);
        }
        return properties;
    }

    /**
     * Returns the property configuration for a given resource.<p>
     * 
     * @param resource the resource
     * 
     * @return the property configuration
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, CmsXmlContentProperty> getElementPropertyConfiguration(CmsResource resource) throws CmsException {

        return CmsXmlContentDefinition.getContentHandlerForResource(m_adminCms, resource).getProperties();
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
    @SuppressWarnings("cast")
    public List<I_CmsContainerElementBean> getFavoriteList(CmsObject cms) throws CmsException {

        CmsUser user = cms.getRequestContext().currentUser();
        List<I_CmsContainerElementBean> favList = null;
        try {
            favList = CmsCollectionsGenericWrapper.list(user.getAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST));
            if ((favList != null) && !favList.isEmpty()) {
                // this can happen when a list of CmsUUID was saved with an older version
                if (!(favList.get(0) instanceof I_CmsContainerElementBean)) {
                    favList = null;
                }
            }
        } catch (Throwable e) {
            // should never happen
            LOG.warn(e.getLocalizedMessage(), e);
        }
        if (favList == null) {
            favList = new ArrayList<I_CmsContainerElementBean>();
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
     * Returns the recent list, or creates it if not available.<p>
     * 
     * @param cms the cms context
     * 
     * @return the recent list
     * 
     * @throws CmsException if something goes wrong 
     */
    public List<I_CmsContainerElementBean> getRecentList(CmsObject cms) throws CmsException {

        CmsUser user = cms.getRequestContext().currentUser();
        List<I_CmsContainerElementBean> recentList = m_cache.getADERecentList(user.getId().toString());
        if (recentList == null) {
            int maxElems = getRecentListMaxSize(cms);
            recentList = CmsCollectionsGenericWrapper.list(new NodeCachingLinkedList(maxElems));
            m_cache.cacheADERecentList(user.getId().toString(), recentList);
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
     * Returns the cached search options.<p>
     * 
     * @param cms the cms context
     * 
     * @return the cached search options
     */
    public CmsSearchOptions getSearchOptions(CmsObject cms) {

        CmsUser user = cms.getRequestContext().currentUser();
        CmsSearchOptions searchOptions = m_cache.getADESearchOptions(user.getId().toString());
        return searchOptions;
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
     * Returns all formatters for a given (xml content) resource.<p>
     * 
     * @param resource the xml content to get the formatters for
     * 
     * @return a map where the keys are the formatter type names and the values the uris
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, String> getXmlContentFormatters(CmsResource resource) throws CmsException {

        // get the content definition
        List<CmsRelation> relations = m_adminCms.getRelationsForResource(
            resource,
            CmsRelationFilter.TARGETS.filterType(CmsRelationType.XSD));
        CmsXmlContentDefinition contentDef = null;
        if ((relations != null) && !relations.isEmpty()) {
            String xsd = m_adminCms.getSitePath(relations.get(0).getTarget(m_adminCms, CmsResourceFilter.ALL));
            contentDef = new CmsXmlEntityResolver(m_adminCms).getCachedContentDefinition(xsd);
        }
        if (contentDef == null) {
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_adminCms, m_adminCms.readFile(resource));
            contentDef = content.getContentDefinition();
        }

        // iterate the formatters
        Map<String, String> formatters = contentDef.getContentHandler().getFormatters();
        if (formatters.isEmpty()) {
            LOG.warn(Messages.get().getBundle().key(
                Messages.LOG_WARN_NO_FORMATTERS_DEFINED_1,
                contentDef.getSchemaLocation()));
        }
        return formatters;
    }

    /**
     * Saves the favorite list, user based.<p>
     * 
     * @param cms the cms context
     * @param favoriteList the element id list
     * 
     * @throws CmsException if something goes wrong 
     */
    public void saveFavoriteList(CmsObject cms, List<I_CmsContainerElementBean> favoriteList) throws CmsException {

        CmsUser user = cms.getRequestContext().currentUser();
        user.setAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST, favoriteList);
        cms.writeUser(user);
    }

    /**
     * Saves the recent list, user based.<p>
     * 
     * @param cms the cms context
     * @param recentList the element id list
     */
    public void saveRecentList(CmsObject cms, List<I_CmsContainerElementBean> recentList) {

        CmsUser user = cms.getRequestContext().currentUser();
        m_cache.cacheADERecentList(user.getId().toString(), recentList);
    }

    /**
     * Saves the search options, user based.<p>
     * 
     * @param cms the cms context
     * @param searchOptions the search options to save
     */
    public void saveSearchOptions(CmsObject cms, CmsSearchOptions searchOptions) {

        CmsUser user = cms.getRequestContext().currentUser();
        // cache the search options, but with page=0
        m_cache.cacheADESearchOptions(user.getId().toString(), searchOptions.resetPage());
    }

    /**
     * It first checks if the given container page is already cached, and only if not
     * the container page will be cached.<p>
     * 
     * @param cms the cms context
     * @param resource the container page itself
     * @param content the xml content of the container page
     * 
     * @throws CmsException if something goes wrong
     *
     * @see org.opencms.xml.containerpage.CmsADECache#setCache(org.opencms.file.CmsObject, org.opencms.file.CmsResource, org.opencms.xml.containerpage.CmsXmlContainerPage)
     */
    public void setCache(CmsObject cms, CmsResource resource, CmsXmlContainerPage content) throws CmsException {

        m_cache.setCache(cms, resource, content);
    }

    /**
     * Caches the given element.<p>
     * 
     * @param element the element to cache
     */
    public void setCachedElement(I_CmsContainerElementBean element) {

        m_cache.cacheContainerElement(element.getClientId(), element);
    }

    /**
     * Clean up at shutdown time. Only intended to be called at system shutdown.<p>
     * 
     * @see org.opencms.main.OpenCmsCore#shutDown
     */
    public void shutdown() {

        m_cache.shutdown();
    }
}
