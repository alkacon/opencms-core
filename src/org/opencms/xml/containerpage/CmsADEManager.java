/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsADEManager.java,v $
 * Date   : $Date: 2009/10/13 13:47:55 $
 * Version: $Revision: 1.1.2.2 $
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
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspTagContainer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlEntityResolver;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
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
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 7.6
 */
public class CmsADEManager {

    /** User additional info key constant. */
    protected static final String ADDINFO_ADE_FAVORITE_LIST = "ADE_FAVORITE_LIST";

    /** HTML id prefix constant. */
    protected static final String ADE_ID_PREFIX = "ade_";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEManager.class);

    /**
     * It first checks if the given container page is already cached, and only if not
     * the container page will be cached.<p>
     * 
     * @param cms the current cms context
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

    /** The cache instance. */
    protected CmsADECache m_cache;

    /** The current cms context. */
    protected CmsObject m_cms;

    /** The container page uri. */
    protected String m_cntPageUri;

    /** The configuration instance. */
    protected I_CmsADEConfiguration m_configuration;

    /** The request itself. */
    protected ServletRequest m_request;

    /** Request parameter name constant. */
    public static final String PARAMETER_CNTPAGE = "cntpage";

    /**
     * Creates a new ADE manager.<p>
     * 
     * @param cms the cms context 
     * @param cntPageUri the container page uri
     * @param request the request itself
     * @param cache the cache instance
     * @param configuration the configured configuration object
     */
    public CmsADEManager(
        CmsObject cms,
        String cntPageUri,
        ServletRequest request,
        CmsADECache cache,
        I_CmsADEConfiguration configuration) {

        m_cms = cms;
        m_cntPageUri = cntPageUri;
        m_request = request;
        m_cache = cache;
        m_configuration = configuration;
        m_configuration.init(m_cms, m_cntPageUri, m_request);
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
     * Creates a valid html id from an uuid.<p>
     * 
     * @param id the uuid
     * 
     * @return the generated html id
     */
    public static String convertToClientId(CmsUUID id) {

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
    public static CmsUUID convertToServerId(String id) throws CmsIllegalArgumentException {

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
     * Reads the current element bean from the request.<p>
     * 
     * @param req the servlet request
     * 
     * @return the element bean
     * 
     * @throws CmsException if attribute "__currentElement" not set, or if a type cast exception occurs
     */
    public static I_CmsContainerElementBean getCurrentElement(ServletRequest req) throws CmsException {

        I_CmsContainerElementBean element = null;
        try {
            element = (I_CmsContainerElementBean)req.getAttribute(CmsJspTagContainer.ATTR_CURRENT_ELEMENT);
        } catch (Exception e) {
            throw new CmsException(Messages.get().container(Messages.ERR_READING_ELEMENT_FROM_REQUEST_0), e);
        }
        if (element == null) {
            throw new CmsException(Messages.get().container(Messages.ERR_READING_ELEMENT_FROM_REQUEST_0));
        }
        return element;
    }

    /**
     * Creates a new CmsContainerElementBean from a CmsContainerElement.<p> 
     * 
     * @param elem the element
     * @return the element bean
     * @throws CmsException if something goes wrong reading the element resource
     */
    public I_CmsContainerElementBean createElement(CmsContainerListElement elem) throws CmsException {

        return new CmsContainerElementBean(m_cms, m_cms.readResource(elem.getStructureId()), elem.getProperties());
    }

    /**
     * Creates a new CmsContainerElementBean for a structure-id and given properties.<p> 
     * 
     * @param structureId the structure-id
     * @param properties the properties-map
     * @return the element bean
     * @throws CmsException if something goes wrong reading the element resource
     */
    public I_CmsContainerElementBean createElement(CmsUUID structureId, Map<String, String> properties)
    throws CmsException {

        return new CmsContainerElementBean(m_cms, m_cms.readResource(structureId), properties);
    }

    /**
     * Creates a new CmsContainerElementBean for a structure-id and given properties.<p> 
     * 
     * @param structureId the structure-id
     * @param properties the properties-map
     * 
     * @return the element bean
     * 
     * @throws CmsException if something goes wrong reading the element resource
     * @throws JSONException if something goes wrong parsing JSON
     */
    public I_CmsContainerElementBean createElement(CmsUUID structureId, JSONObject properties)
    throws CmsException, JSONException {

        return new CmsContainerElementBean(m_cms, m_cms.readResource(structureId), properties);
    }

    /**
     * Creates a new element of a given type at the configured location.<p>
     * 
     * @param type the type of the element to be created
     * 
     * @return the CmsResource representing the newly created element
     * 
     * @throws CmsException if something goes wrong
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#createNewElement(java.lang.String)
     */
    public CmsResource createNewElement(String type) throws CmsException {

        return m_configuration.createNewElement(type);
    }

    /**
     * Returns the list of creatable elements.<p>
     * 
     * @return the list of creatable elements
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getCreatableElements()
     */
    public List<CmsResource> getCreatableElements() throws CmsException {

        return m_configuration.getCreatableElements();
    }

    /**
     * Returns the cached list, or creates it if not available.<p>
     * 
     * @return the cached recent list
     * 
     * @throws CmsException if something goes wrong 
     */
    @SuppressWarnings("unchecked")
    public List<CmsContainerListElement> getFavoriteList() throws CmsException {

        CmsUser user = m_cms.getRequestContext().currentUser();
        List<CmsContainerListElement> favList = null;
        try {
            favList = (List<CmsContainerListElement>)user.getAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST);
            if ((favList != null) && !favList.isEmpty()) {
                // this can happen when a list of CmsUUID was saved with an older version
                if (!(favList.get(0) instanceof CmsContainerListElement)) {
                    favList = null;
                }
            }
        } catch (Throwable e) {
            // should never happen
            LOG.warn(e.getLocalizedMessage(), e);
        }
        if (favList == null) {
            favList = new ArrayList<CmsContainerListElement>();
            saveFavoriteList(favList);
        }
        return favList;
    }

    /**
     * Returns the maximal size of the favorite list.<p>
     * 
     * @return the maximal size of the favorite list
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getFavoriteListMaxSize()
     */
    public int getFavoriteListMaxSize() throws CmsException {

        return m_configuration.getFavoriteListMaxSize();
    }

    /**
     * Returns the name of the next new file of the given type to be created.<p>
     * 
     * @param type the resource type name
     * 
     * @return the name of the next new file of the given type to be created
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getNextNewFileName(java.lang.String)
     */
    public String getNextNewFileName(String type) throws CmsException {

        return m_configuration.getNextNewFileName(type);
    }

    /**
     * Returns the cached list, or creates it if not available.<p>
     * 
     * @return the cached recent list
     * 
     * @throws CmsException if something goes wrong 
     */
    @SuppressWarnings("unchecked")
    public List<CmsContainerListElement> getRecentList() throws CmsException {

        CmsUser user = m_cms.getRequestContext().currentUser();
        List<CmsContainerListElement> recentList = m_cache.getADERecentList(user.getId().toString());
        if (recentList == null) {
            int maxElems = m_configuration.getRecentListMaxSize();
            recentList = new NodeCachingLinkedList(maxElems);
            m_cache.cacheADERecentList(user.getId().toString(), recentList);
        }
        return recentList;
    }

    /**
     * Returns the property configuration for a given resource.<p>
     * 
     * @param resource the resource
     * @return the property configuration
     * @throws CmsException - if something goes wrong
     */
    public Map<String, CmsXmlContentProperty> getElementPropertyConfiguration(CmsResource resource) throws CmsException {

        return CmsContainerElementBean.getPropertyConfiguration(m_cms, resource);
    }

    /**
     * Reads the cached element-bean for the given client-side-id from cache.<p>
     * 
     * @param clientId the client-side-id
     * @return the CmsContainerElementBean
     * @throws CmsException - if the resource could not be read for any reason
     */
    public I_CmsContainerElementBean getCachedElement(String clientId) throws CmsException {

        String id = clientId;
        I_CmsContainerElementBean element = null;
        try {
            element = m_cache.getCacheContainerElement(id);
        } catch (Exception e) {
            // may happen if element was not cached
        }
        if (element != null) {
            return element;
        }
        if (id.contains("#")) {
            id = id.substring(0, id.indexOf("#"));
            try {
                element = m_cache.getCacheContainerElement(id);
            } catch (Exception e) {
                // may happen if element was not cached
            }
            if (element != null) {
                return element;
            }
        }
        element = new CmsContainerElementBean(m_cms, m_cms.readResource(convertToServerId(id)));
        m_cache.cacheContainerElement(id, element);
        return element;
    }

    /**
     * Writes the given element-bean to the cache.<p>
     * 
     * @param clientId the client-side-id as the cache key
     * @param element the element-bean
     */
    public void setCachedElement(String clientId, I_CmsContainerElementBean element) {

        m_cache.cacheContainerElement(clientId, element);
    }

    /**
     * Returns the maximal size of the recent list.<p>
     * 
     * @return the maximal size of the recent list
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getRecentListMaxSize()
     */
    public int getRecentListMaxSize() throws CmsException {

        return m_configuration.getRecentListMaxSize();
    }

    /**
     * Returns the list of searchable resource types.<p>
     * 
     * @return the list of searchable resource types
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getSearchableResourceTypes()
     */
    public List<String> getSearchableResourceTypes() throws CmsException {

        return m_configuration.getSearchableResourceTypes();
    }

    /**
     * Returns the cached search options.<p>
     * 
     * @return the cached search options
     */
    public CmsSearchOptions getSearchOptions() {

        CmsUser user = m_cms.getRequestContext().currentUser();
        CmsSearchOptions searchOptions = m_cache.getADESearchOptions(user.getId().toString());
        return searchOptions;
    }

    /**
     * Returns the size of a search page.<p>
     * 
     * @return the maximal size of the favorite list
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getSearchPageSize()
     */
    public int getSearchPageSize() throws CmsException {

        return m_configuration.getSearchPageSize();
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
        List<CmsRelation> relations = m_cms.getRelationsForResource(
            resource,
            CmsRelationFilter.TARGETS.filterType(CmsRelationType.XSD));
        CmsXmlContentDefinition contentDef = null;
        if ((relations != null) && !relations.isEmpty()) {
            String xsd = m_cms.getSitePath(relations.get(0).getTarget(m_cms, CmsResourceFilter.ALL));
            contentDef = new CmsXmlEntityResolver(m_cms).getCachedContentDefinition(xsd);
        }
        if (contentDef == null) {
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, m_cms.readFile(resource));
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
     * @param favoriteList the element id list
     * 
     * @throws CmsException if something goes wrong 
     */
    public void saveFavoriteList(List<CmsContainerListElement> favoriteList) throws CmsException {

        CmsUser user = m_cms.getRequestContext().currentUser();
        user.setAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST, favoriteList);
        m_cms.writeUser(user);
    }

    /**
     * Saves the recent list, user based.<p>
     * 
     * @param recentList the element id list
     */
    public void saveRecentList(List<CmsContainerListElement> recentList) {

        CmsUser user = m_cms.getRequestContext().currentUser();
        m_cache.cacheADERecentList(user.getId().toString(), recentList);
    }

    /**
     * Saves the search options, user based.<p>
     * 
     * @param searchOptions the search options to save
     */
    public void saveSearchOptions(CmsSearchOptions searchOptions) {

        CmsUser user = m_cms.getRequestContext().currentUser();
        // cache the search options, but with page=0
        m_cache.cacheADESearchOptions(user.getId().toString(), searchOptions.resetPage());
    }
}
