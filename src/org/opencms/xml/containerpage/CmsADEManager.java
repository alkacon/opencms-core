/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/CmsADEManager.java,v $
 * Date   : $Date: 2009/12/14 09:41:04 $
 * Version: $Revision: 1.7 $
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

import org.opencms.configuration.CmsSystemConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsInitException;
import org.opencms.main.CmsLog;
import org.opencms.monitor.CmsMemoryMonitor;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;

/**
 * Advanced Direct Edit Manager.<p>
 * 
 * Provides all relevant functions for ADE.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.7 $
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

    /** The request attribute name for the current element-bean. */
    public static final String ATTR_CURRENT_ELEMENT = "__currentElement";

    /** The request attribute name for the formatter-info-bean. */
    public static final String ATTR_FORMATTER_INFO = "__formatterInfo";

    /** User additional info key constant. */
    protected static final String ADDINFO_ADE_FAVORITE_LIST = "ADE_FAVORITE_LIST";

    /** HTML id prefix constant. */
    protected static final String ADE_ID_PREFIX = "ade_";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEManager.class);

    /** The cache instance. */
    protected CmsADECache m_cache;

    /** The configuration instance. */
    protected I_CmsADEConfiguration m_configuration;

    /**
     * Creates a new ADE manager.<p>
     * 
     * @param memoryMonitor the memory monitor instance
     * @param systemConfiguration the system configuration
     */
    public CmsADEManager(CmsMemoryMonitor memoryMonitor, CmsSystemConfiguration systemConfiguration) {

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
        m_configuration = configuration;
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
     * Creates a new element of a given type at the configured location.<p>
     * 
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     * @param type the type of the element to be created
     * 
     * @return the CmsResource representing the newly created element
     * 
     * @throws CmsException if something goes wrong
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#createNewElement(org.opencms.file.CmsObject, java.lang.String, javax.servlet.ServletRequest, java.lang.String)
     */
    public CmsResource createNewElement(CmsObject cms, String cntPageUri, ServletRequest request, String type)
    throws CmsException {

        return m_configuration.createNewElement(cms, cntPageUri, request, type);
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
    public List<CmsResource> getCreatableElements(CmsObject cms, String cntPageUri, ServletRequest request)
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
     * @throws CmsException if attribute {@value #ATTR_CURRENT_ELEMENT} not set, or if a type cast exception occurs
     */
    public CmsContainerElementBean getCurrentElement(ServletRequest req) throws CmsException {

        // TODO: remove this and use always the getFormatterInfo method instead
        CmsContainerElementBean element = null;
        try {
            element = (CmsContainerElementBean)req.getAttribute(ATTR_CURRENT_ELEMENT);
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
     * @param cms the current cms context
     * @param element the element to get the properties for
     * 
     * @return the element properties
     */
    public Map<String, CmsProperty> getElementProperties(CmsObject cms, CmsContainerElementBean element) {

        Map<String, CmsProperty> properties = new HashMap<String, CmsProperty>();
        Iterator<Map.Entry<String, String>> itProperties = element.getProperties().entrySet().iterator();
        while (itProperties.hasNext()) {
            Map.Entry<String, String> entry = itProperties.next();
            String propertyName = entry.getKey();
            CmsProperty property = new CmsProperty(propertyName, entry.getValue(), null);
            properties.put(propertyName, property);
        }
        try {
            Map<String, CmsXmlContentProperty> propertyDefs = getElementPropertyConfiguration(
                cms,
                cms.readResource(element.getElementId()));
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
                element.getElementId()), e);
        }
        return properties;
    }

    /**
     * Returns the property configuration for a given resource.<p>
     * 
     * @param cms the current cms context
     * @param resource the resource
     * 
     * @return the property configuration
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, CmsXmlContentProperty> getElementPropertyConfiguration(CmsObject cms, CmsResource resource)
    throws CmsException {

        return CmsXmlContentDefinition.getContentHandlerForResource(cms, resource).getProperties();
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

        CmsUser user = cms.getRequestContext().currentUser();
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
                        if (!LOG.isDebugEnabled()) {
                            LOG.warn(e.getLocalizedMessage());
                        }
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }
            } catch (Throwable e) {
                // should never happen, catches json parsing
                if (!LOG.isDebugEnabled()) {
                    LOG.warn(e.getLocalizedMessage());
                }
                LOG.debug(e.getLocalizedMessage(), e);
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
     * Returns the name of the next new file of the given type to be created.<p>
     * 
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     * @param type the resource type name
     * 
     * @return the name of the next new file of the given type to be created
     * 
     * @throws CmsException if something goes wrong
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getNextNewFileName(org.opencms.file.CmsObject, java.lang.String, javax.servlet.ServletRequest, java.lang.String)
     */
    public String getNextNewFileName(CmsObject cms, String cntPageUri, ServletRequest request, String type)
    throws CmsException {

        return m_configuration.getNextNewFileName(cms, cntPageUri, request, type);
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
    public List<CmsResource> getSearchableResourceTypes(CmsObject cms, String cntPageUri, ServletRequest request)
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
     * Returns all formatters for a given (xml content) resource.<p>
     * 
     * @param cms the current cms context
     * @param resource the xml content to get the formatters for
     * 
     * @return a map where the keys are the formatter type names and the values the uris
     * 
     * @throws CmsException if something goes wrong
     */
    public Map<String, String> getXmlContentFormatters(CmsObject cms, CmsResource resource) throws CmsException {

        // get the content definition
        List<CmsRelation> relations = cms.getRelationsForResource(
            resource,
            CmsRelationFilter.TARGETS.filterType(CmsRelationType.XSD));
        CmsXmlContentDefinition contentDef = null;
        if ((relations != null) && !relations.isEmpty()) {
            String xsd = cms.getSitePath(relations.get(0).getTarget(cms, CmsResourceFilter.ALL));
            contentDef = new CmsXmlEntityResolver(cms).getCachedContentDefinition(xsd);
        }
        if (contentDef == null) {
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resource));
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
    public void saveFavoriteList(CmsObject cms, List<CmsContainerElementBean> favoriteList) throws CmsException {

        JSONArray data = new JSONArray();
        for (CmsContainerElementBean element : favoriteList) {
            data.put(elementToJson(element));
        }
        CmsUser user = cms.getRequestContext().currentUser();
        user.setAdditionalInfo(ADDINFO_ADE_FAVORITE_LIST, data.toString());
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

        return new CmsContainerElementBean(element, formatter, properties);
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
            data.put(FavListProp.ELEMENT.name().toLowerCase(), element.getElementId().toString());
            if (element.getFormatterId() != null) {
                data.put(FavListProp.FORMATTER.name().toLowerCase(), element.getFormatterId().toString());
            }
            JSONObject properties = new JSONObject();
            for (Map.Entry<String, String> entry : element.getProperties().entrySet()) {
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
}
