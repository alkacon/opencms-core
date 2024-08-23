/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.ade.configuration.CmsADEConfigDataInternal.AttributeValue;
import org.opencms.ade.configuration.CmsADEConfigDataInternal.ConfigReferenceMeta;
import org.opencms.ade.configuration.formatters.CmsFormatterBeanParser;
import org.opencms.ade.configuration.formatters.CmsFormatterChangeSet;
import org.opencms.ade.configuration.formatters.CmsFormatterConfigurationCacheState;
import org.opencms.ade.configuration.formatters.CmsFormatterIndex;
import org.opencms.ade.configuration.plugins.CmsSitePlugin;
import org.opencms.ade.containerpage.shared.CmsContainer;
import org.opencms.ade.containerpage.shared.CmsContainerElement;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.ade.galleries.CmsAddContentRestriction;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypeFunctionConfig;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.gwt.CmsIconUtil;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.jsp.util.CmsFunctionRenderer;
import org.opencms.loader.CmsLoaderException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsServlet;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.editors.directedit.CmsAdvancedDirectEditProvider.SitemapDirectEditPermissions;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsXmlDynamicFunctionHandler;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.logging.Log;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * A class which represents the accessible configuration data at a given point in a sitemap.<p>
 */
public class CmsADEConfigData {

    /**
     * Bean which contains the detail information for a single sub-sitemap and resource type.<p>
     *
     * This includes both  information about the detail page itself, as well as the path of the folder
     * which is used to store that content type in this subsitemap.<p>
     *
     */
    public class DetailInfo {

        /** The base path of the sitemap configuration where this information originates from. */
        private String m_basePath;

        /** The information about the detail page info itself. */
        private CmsDetailPageInfo m_detailPageInfo;

        /** The content folder path. */
        private String m_folderPath;

        /** The detail type. */
        private String m_type;

        /**
         * Creates a new instance.<p>
         *
         * @param folderPath the content folder path
         * @param detailPageInfo the detail page information
         * @param type the detail type
         * @param basePath the base path of the sitemap configuration
         */
        public DetailInfo(String folderPath, CmsDetailPageInfo detailPageInfo, String type, String basePath) {

            m_folderPath = folderPath;
            m_detailPageInfo = detailPageInfo;
            m_type = type;
            m_basePath = basePath;

        }

        /**
         * Gets the base path of the sitemap configuration from which this information is coming.<p>
         *
         * @return the base path
         */
        public String getBasePath() {

            return m_basePath;
        }

        /**
         * Gets the detail page information.<p>
         *
         * @return the detail page information
         */
        public CmsDetailPageInfo getDetailPageInfo() {

            return m_detailPageInfo;
        }

        /**
         * Gets the content folder path.<p>
         *
         * @return the content folder path
         */
        public String getFolderPath() {

            return m_folderPath;
        }

        /**
         * Gets the detail type.<p>
         *
         * @return the detail type
         */
        public String getType() {

            return m_type;
        }

        /**
         * Sets the base path.<p>
         *
         * @param basePath the new base path value
         */
        public void setBasePath(String basePath) {

            m_basePath = basePath;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return ReflectionToStringBuilder.toString(this);
        }
    }

    /** Sitemap attribute for the upload folder. */
    public static final String ATTR_BINARY_UPLOAD_TARGET = "binary.upload.target";

    /** Prefix for logging special request log messages. */
    public static final String REQ_LOG_PREFIX = "[CmsADEConfigData] ";

    /** Channel for logging special request log messages. */
    public static final String REQUEST_LOG_CHANNEL = "org.opencms.ade.configuration.CmsADEConfigData.request";

    /** The log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEConfigData.class);

    /** Prefixes for internal settings which might be passed as formatter keys to findFormatter(). */
    private static final HashSet<String> systemSettingPrefixes = new HashSet<>(
        Arrays.asList("element", "model", "source", "use", "cms", "is"));

    /** The wrapped configuration bean containing the actual data. */
    protected CmsADEConfigDataInternal m_data;

    /** Lazily initialized map of formatters. */
    private Map<CmsUUID, I_CmsFormatterBean> m_activeFormatters;

    /** Lazily initialized cache for active formatters by formatter key. */
    private Multimap<String, I_CmsFormatterBean> m_activeFormattersByKey;

    /** The sitemap attributes (may be null if not yet computed). */
    private Map<String, AttributeValue> m_attributes;

    /** The cache state to which the wrapped configuration bean belongs. */
    private CmsADEConfigCacheState m_cache;

    /** Current formatter configuration. */
    private CmsFormatterConfigurationCacheState m_cachedFormatters;

    /** The configuration sequence (contains the list of all sitemap configuration data beans to be used for inheritance). */
    private CmsADEConfigurationSequence m_configSequence;

    /** Cache for formatters by container type. */
    private Map<String, List<I_CmsFormatterBean>> m_formattersByContainerType = new HashMap<>();

    /** Cache for formatters by display type. */
    private Map<String, List<I_CmsFormatterBean>> m_formattersByDisplayType = new HashMap<>();

    /** Lazily initialized cache for formatters by JSP id. */
    private Multimap<CmsUUID, I_CmsFormatterBean> m_formattersByJspId;

    /** Lazily initialized cache for formatters by formatter key. */
    private Multimap<String, I_CmsFormatterBean> m_formattersByKey;

    /** Loading cache for for formatters grouped by type. */
    private LoadingCache<String, List<I_CmsFormatterBean>> m_formattersByTypeCache = CacheBuilder.newBuilder().build(
        new CacheLoader<String, List<I_CmsFormatterBean>>() {

            @Override
            public List<I_CmsFormatterBean> load(String typeName) throws Exception {

                List<I_CmsFormatterBean> result = new ArrayList<>();
                for (I_CmsFormatterBean formatter : getActiveFormatters().values()) {
                    if (formatter.getResourceTypeNames().contains(typeName)) {
                        result.add(formatter);
                    }
                }
                return result;
            }
        });

    /** Cached shared setting overrides. */
    private volatile ImmutableList<CmsUUID> m_sharedSettingOverrides;

    /** Set of names of active types.*/
    private Set<String> m_typesAddable;

    /** Cache of (active) resource type configurations by name. */
    private Map<String, CmsResourceTypeConfig> m_typesByName;

    /** Type names configured in this or ancestor sitemap configurations. */
    private Set<String> m_typesInAncestors;

    /**
     * Creates a new configuration data object, based on an internal configuration data bean and a
     * configuration cache state.<p>
     *
     * @param data the internal configuration data bean
     * @param cache the configuration cache state
     * @param configSequence the configuration sequence
     */
    public CmsADEConfigData(
        CmsADEConfigDataInternal data,
        CmsADEConfigCacheState cache,
        CmsADEConfigurationSequence configSequence) {

        m_data = data;
        m_cache = cache;
        m_configSequence = configSequence;
    }

    /**
     * Generic method to merge lists of named configuration objects.<p>
     *
     * The lists are merged such that the configuration objects from the child list rise to the front of the result list,
     * and two configuration objects will be merged themselves if they share the same name.<p>
     *
     * For example, if we have two lists of configuration objects:<p>
     *
     * parent: A1, B1, C1<p>
     * child: D2, B2<p>
     *
     * then the resulting list will look like:<p>
     *
     * D2, B3, A1, C1<p>
     *
     * where B3 is the result of merging B1 and B2.<p>
     *
     * @param <C> the type of configuration object
     * @param parentConfigs the parent configurations
     * @param childConfigs the child configurations
     * @param preserveDisabled if true, try to merge parents with disabled children instead of discarding them
     *
     * @return the merged configuration object list
     */
    public static <C extends I_CmsConfigurationObject<C>> List<C> combineConfigurationElements(
        List<C> parentConfigs,
        List<C> childConfigs,
        boolean preserveDisabled) {

        List<C> result = new ArrayList<C>();
        Map<String, C> map = new LinkedHashMap<String, C>();
        if (parentConfigs != null) {
            for (C parent : Lists.reverse(parentConfigs)) {
                map.put(parent.getKey(), parent);
            }
        }
        if (childConfigs == null) {
            childConfigs = Collections.emptyList();
        }
        for (C child : Lists.reverse(childConfigs)) {
            String childKey = child.getKey();
            if (child.isDisabled() && !preserveDisabled) {
                map.remove(childKey);
            } else {
                C parent = map.get(childKey);
                map.remove(childKey);
                C newValue;
                if (parent != null) {
                    newValue = parent.merge(child);
                } else {
                    newValue = child;
                }
                map.put(childKey, newValue);
            }
        }
        result = new ArrayList<C>(map.values());
        Collections.reverse(result);
        // those multiple "reverse" calls may a bit confusing. They are there because on the one hand we want to keep the
        // configuration items from one configuration in the same order as they are defined, on the other hand we want
        // configuration items from a child configuration to rise to the top of the configuration items.

        // so for example, if the parent configuration has items with the keys A,B,C,E
        // and the child configuration has items  with the keys C,B,D
        // we want the items of the combined configuration in the order C,B,D,A,E

        return result;
    }

    /**
     * If the given formatter key has a sub-formatter suffix, returns the part before it,
     * otherwise returns null.
     *
     * @param key the formatter key
     * @return the parent formatter key
     */
    public static final String getParentFormatterKey(String key) {

        if (key == null) {
            return null;
        }
        int separatorPos = key.lastIndexOf(CmsGwtConstants.FORMATTER_SUBKEY_SEPARATOR);
        if (separatorPos == -1) {
            return null;
        }
        return key.substring(0, separatorPos);

    }

    /**
     * Applies the formatter change sets of this and all parent configurations to a formatter index
     *
     * @param formatterIndex the collection of formatters to apply the changes to
     *
     * @param formatterCacheState the formatter cache state from which new external formatters should be fetched
     */
    public void applyAllFormatterChanges(
        CmsFormatterIndex formatterIndex,
        CmsFormatterConfigurationCacheState formatterCacheState) {

        for (CmsFormatterChangeSet changeSet : getFormatterChangeSets()) {
            changeSet.applyToFormatters(formatterIndex, formatterCacheState);
        }
    }

    /**
     * Gets the 'best' formatter for the given ID.<p>
     *
     * If the formatter with the ID has a key, then the active formatter with the same key is returned.  Otherwise, the
     * formatter matching the ID is returned. So being active and having the same key is prioritized over an exact ID match.
     *
     * @param id the formatter ID
     * @return the best formatter the given ID
     */
    public I_CmsFormatterBean findFormatter(CmsUUID id) {

        return findFormatter(id, false);
    }

    /**
     * Gets the 'best' formatter for the given ID.<p>
     *
     * If the formatter with the ID has a key, then the active formatter with the same key is returned.  Otherwise, the
     * formatter matching the ID is returned. So being active and having the same key is prioritized over an exact ID match.
     *
     * @param id the formatter ID
     * @param noWarn if true, disables warnings
     * @return the best formatter the given ID
     */
    public I_CmsFormatterBean findFormatter(CmsUUID id, boolean noWarn) {

        if (id == null) {
            return null;
        }

        CmsFormatterConfigurationCacheState formatterState = getCachedFormatters();
        I_CmsFormatterBean originalResult = formatterState.getFormatters().get(id);
        I_CmsFormatterBean result = originalResult;
        if ((result != null) && (result.getKey() != null)) {
            String key = result.getKey();
            I_CmsFormatterBean resultForKey = getFormatterAndWarnIfAmbiguous(getActiveFormattersByKey(), key, noWarn);
            if (resultForKey != null) {
                result = resultForKey;
            } else {
                String parentKey = getParentFormatterKey(key);
                if (parentKey != null) {
                    resultForKey = getFormatterAndWarnIfAmbiguous(getActiveFormattersByKey(), parentKey, noWarn);
                    if (resultForKey != null) {
                        result = resultForKey;
                    }
                }
            }
        }

        if (result != originalResult) {
            String message = "Using substitute formatter "
                + getFormatterLabel(result)
                + " instead of "
                + getFormatterLabel(originalResult)
                + " because of matching key.";
            LOG.debug(message);
            OpenCmsServlet.withRequestCache(
                reqCache -> reqCache.addLog(REQUEST_LOG_CHANNEL, "debug", REQ_LOG_PREFIX + message));
        }
        return result;
    }

    /**
     * Gets the 'best' formatter for the given name.<p>
     *
     * The name can be either a formatter key, or a formatter UUID. If it's a key, an active formatter with that key is returned.
     * If it's a UUID, and the formatter with that UUID has no key, it will be returned. If it does have a key, the active formatter
     * with that key is returned (so being active and having the same key is prioritized over an exact ID match).
     *
     * @param name a formatter name (key or ID)
     * @return the best formatter for that name, or null if no formatter could be found
     */
    public I_CmsFormatterBean findFormatter(String name) {

        return findFormatter(name, false);
    }

    /**
     * Gets the 'best' formatter for the given name.<p>
     *
     * The name can be either a formatter key, or a formatter UUID. If it's a key, an active formatter with that key is returned.
     * If it's a UUID, and the formatter with that UUID has no key, it will be returned. If it does have a key, the active formatter
     * with that key is returned (so being active and having the same key is prioritized over an exact ID match).
     *
     * @param name a formatter name (key or ID)
     * @param noWarn if true, disables warnings
     * @return the best formatter for that name, or null if no formatter could be found
     */
    public I_CmsFormatterBean findFormatter(String name, boolean noWarn) {

        if (name == null) {
            return null;
        }

        if (systemSettingPrefixes.contains(name) || name.startsWith(CmsContainerElement.SYSTEM_SETTING_PREFIX)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("System setting prefix used: " + name, new Exception());
            }
            return null;
        }

        if (CmsUUID.isValidUUID(name)) {
            return findFormatter(new CmsUUID(name), noWarn);
        }

        if (name.startsWith(CmsFormatterConfig.SCHEMA_FORMATTER_ID)) {
            return null;
        }

        Multimap<String, I_CmsFormatterBean> active = getActiveFormattersByKey();
        I_CmsFormatterBean result = getFormatterAndWarnIfAmbiguous(active, name, noWarn);
        if (result != null) {
            return result;
        }

        String parentName = getParentFormatterKey(name);
        if (parentName != null) {
            result = getFormatterAndWarnIfAmbiguous(active, parentName, noWarn);
            if (result != null) {
                return result;
            }
        }

        if (!noWarn) {
            String message1 = "No local formatter found for key '"
                + name
                + "' at '"
                + getBasePath()
                + "', trying inactive formatters";
            LOG.warn(message1);
            OpenCmsServlet.withRequestCache(rc -> rc.addLog(REQUEST_LOG_CHANNEL, "warn", REQ_LOG_PREFIX + message1));
        }

        Multimap<String, I_CmsFormatterBean> all = getFormattersByKey();
        result = getFormatterAndWarnIfAmbiguous(all, name, noWarn);
        if (result != null) {
            return result;
        }

        if (parentName != null) {
            result = getFormatterAndWarnIfAmbiguous(all, parentName, noWarn);
            if (result != null) {
                return result;
            }
        }

        if (!noWarn) {
            OpenCmsServlet.withRequestCache(
                rc -> rc.addLog(
                    REQUEST_LOG_CHANNEL,
                    "warn",
                    REQ_LOG_PREFIX + "No formatter found for key '" + name + "' at '" + getBasePath() + "'"));
        }
        return null;
    }

    /**
     * Gets the active external (non-schema) formatters for this sub-sitemap.<p>
     *
     * @return the map of active external formatters by structure id
     */
    public Map<CmsUUID, I_CmsFormatterBean> getActiveFormatters() {

        if (m_activeFormatters == null) {
            CmsFormatterIndex formatterIndex = new CmsFormatterIndex();
            for (I_CmsFormatterBean formatter : getCachedFormatters().getAutoEnabledFormatters().values()) {
                formatterIndex.addFormatter(formatter);
            }
            applyAllFormatterChanges(formatterIndex, getCachedFormatters());
            m_activeFormatters = Collections.unmodifiableMap(formatterIndex.getFormattersWithAdditionalKeys());
        }
        return m_activeFormatters;
    }

    /**
     * Gets the active formatters for a given container type.
     *
     * @param containerType a container type
     *
     * @return the active formatters for the container type
     */
    public List<I_CmsFormatterBean> getActiveFormattersWithContainerType(String containerType) {

        return m_formattersByContainerType.computeIfAbsent(
            containerType,
            type -> Collections.unmodifiableList(
                getActiveFormatters().values().stream().filter(
                    formatter -> formatter.getContainerTypes().contains(type)).collect(Collectors.toList())));
    }

    /**
     * Gets the active formatters for a given display type.
     *
     * @param displayType a display type
     * @return the active formatters for the display type
     */
    public List<I_CmsFormatterBean> getActiveFormattersWithDisplayType(String displayType) {

        return m_formattersByDisplayType.computeIfAbsent(
            displayType,
            type -> Collections.unmodifiableList(
                getActiveFormatters().values().stream().filter(
                    formatter -> Objects.equals(type, formatter.getDisplayType())).collect(Collectors.toList()))

        );
    }

    /**
     * Gets the set of names of types active in this sitemap configuration.
     *
     * @return the set of type names of active types
     */
    public Set<String> getAddableTypeNames() {

        Set<String> result = m_typesAddable;
        if (result != null) {
            return result;
        } else {
            Set<String> mutableResult = new HashSet<>();
            for (CmsResourceTypeConfig typeConfig : internalGetResourceTypes(true)) {
                if (!typeConfig.isAddDisabled()) {
                    mutableResult.add(typeConfig.getTypeName());
                }
            }
            result = Collections.unmodifiableSet(mutableResult);
            m_typesAddable = result;
            return result;
        }
    }

    /**
     * Gets the 'add content' restriction for this configuration.
     *
     * @return the 'add content' restriction
     */
    public CmsAddContentRestriction getAddContentRestriction() {

        getAncestorTypeNames();

        CmsADEConfigData parentConfig = parent();
        if (parentConfig == null) {
            return m_data.getAddContentRestriction();
        } else {
            return parentConfig.getAddContentRestriction().merge(m_data.getAddContentRestriction());
        }
    }

    /**
     * Gets the list of all detail pages.<p>
     *
     * @return the list of all detail pages
     */
    public List<CmsDetailPageInfo> getAllDetailPages() {

        return getAllDetailPages(true);
    }

    /**
     * Gets a list of all detail pages.<p>
     *
     * @param update if true, this method will try to correct the root paths in the returned objects if the corresponding resources have been moved
     *
     * @return the list of all detail pages
     */
    public List<CmsDetailPageInfo> getAllDetailPages(boolean update) {

        CmsADEConfigData parentData = parent();
        List<CmsDetailPageInfo> parentDetailPages;
        if (parentData != null) {
            parentDetailPages = parentData.getAllDetailPages(false);
        } else {
            parentDetailPages = Collections.emptyList();
        }
        List<CmsDetailPageInfo> result = mergeDetailPages(parentDetailPages, m_data.getOwnDetailPages());
        if (update) {
            result = updateUris(result);
        }
        return result;
    }

    /**
     * Gets the set of names of types configured in this or any ancestor sitemap configurations.
     *
     * @return the set of type names from all ancestor configurations
     */
    public Set<String> getAncestorTypeNames() {

        Set<String> result = m_typesInAncestors;
        if (result != null) {
            return result;
        } else {
            Set<String> mutableResult = new HashSet<>();
            for (CmsResourceTypeConfig typeConfig : internalGetResourceTypes(false)) {
                mutableResult.add(typeConfig.getTypeName());
            }
            result = Collections.unmodifiableSet(mutableResult);
            m_typesInAncestors = result;
            return result;
        }
    }

    /**
     * Gets the value of an attribute, or a default value
     *
     * @param key the attribute key
     * @param defaultValue the value to return if no attribute with the given name is found
     *
     * @return the attribute value
     */
    public String getAttribute(String key, String defaultValue) {

        AttributeValue value = getAttributes().get(key);
        if (value != null) {
            return value.getValue();
        } else {
            return defaultValue;
        }

    }

    /**
     * Gets the active sitemap attribute editor configuration.
     *
     * @return the active sitemap attribute editor configuration
     */
    public CmsSitemapAttributeEditorConfiguration getAttributeEditorConfiguration() {

        CmsUUID id = getAttributeEditorConfigurationId();
        CmsSitemapAttributeEditorConfiguration result = m_cache.getAttributeEditorConfiguration(id);
        if (result == null) {
            result = CmsSitemapAttributeEditorConfiguration.EMPTY;
        }
        return result;

    }

    /**
     * Gets the structure id of the configured sitemap attribute editor configuration.
     *
     * @return the structure id of the configured sitemap attribute editor configuration
     */
    public CmsUUID getAttributeEditorConfigurationId() {

        CmsADEConfigData parent = parent();
        CmsUUID result = m_data.getAttributeEditorConfigId();
        if ((result == null) && (parent != null)) {
            result = parent.getAttributeEditorConfigurationId();
        }
        return result;

    }

    /**
     * Gets the map of attributes configured for this sitemap, including values inherited from parent sitemaps.
     *
     * @return the map of attributes
     */
    public Map<String, AttributeValue> getAttributes() {

        if (m_attributes != null) {
            return m_attributes;
        }
        CmsADEConfigData parentConfig = parent();
        Map<String, AttributeValue> result = new HashMap<>();
        if (parentConfig != null) {
            result.putAll(parentConfig.getAttributes());
        }

        for (Map.Entry<String, AttributeValue> entry : m_data.getAttributes().entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }
        Map<String, AttributeValue> immutableResult = Collections.unmodifiableMap(result);
        m_attributes = immutableResult;
        return immutableResult;
    }

    /**
     * Gets the configuration base path.<p>
     *
     * For example, if the configuration file is located at /sites/default/.content/.config, the base path is /sites/default.<p>
     *
     * @return the base path of the configuration
     */
    public String getBasePath() {

        return m_data.getBasePath();
    }

    /**
     * Gets the cached formatters.<p>
     *
     * @return the cached formatters
     */
    public CmsFormatterConfigurationCacheState getCachedFormatters() {

        if (m_cachedFormatters == null) {
            m_cachedFormatters = OpenCms.getADEManager().getCachedFormatters(
                getCms().getRequestContext().getCurrentProject().isOnlineProject());
        }
        return m_cachedFormatters;
    }

    /**
     * Gets an (immutable) list of paths of configuration files in inheritance order.
     *
     * @return the list of configuration files
     */
    public List<String> getConfigPaths() {

        return m_configSequence.getConfigPaths();

    }

    /**
     * Returns the names of the bundles configured as workplace bundles in any module configuration.<p>
     *
     * @return the names of the bundles configured as workplace bundles in any module configuration.
     */
    public Set<String> getConfiguredWorkplaceBundles() {

        Set<String> result = new HashSet<String>();
        for (CmsResourceTypeConfig config : internalGetResourceTypes(false)) {
            String bundlename = config.getConfiguredWorkplaceBundle();
            if (null != bundlename) {
                result.add(bundlename);
            }
        }
        return result;
    }

    /**
     * Gets the content folder path.<p>
     *
     * For example, if the configuration file is located at /sites/default/.content/.config, the content folder path is /sites/default/.content
     *
     * @return the content folder path
     */
    public String getContentFolderPath() {

        return CmsStringUtil.joinPaths(m_data.getBasePath(), CmsADEManager.CONTENT_FOLDER_NAME);

    }

    /**
     * Returns a list of the creatable resource types.<p>
     *
     * @param cms the CMS context used to check whether the resource types are creatable
     * @param pageFolderRootPath the root path of the current container page
     * @return the list of creatable resource type
     *
     * @throws CmsException if something goes wrong
     */
    public List<CmsResourceTypeConfig> getCreatableTypes(CmsObject cms, String pageFolderRootPath) throws CmsException {

        List<CmsResourceTypeConfig> result = new ArrayList<CmsResourceTypeConfig>();
        for (CmsResourceTypeConfig typeConfig : getResourceTypes()) {
            if (typeConfig.checkCreatable(cms, pageFolderRootPath)) {
                result.add(typeConfig);
            }
        }
        return result;
    }

    /**
     * Returns the default detail page.<p>
     *
     * @return the default detail page
     */
    public CmsDetailPageInfo getDefaultDetailPage() {

        for (CmsDetailPageInfo detailpage : getAllDetailPages(true)) {
            if (CmsADEManager.DEFAULT_DETAILPAGE_TYPE.equals(detailpage.getType())) {
                return detailpage;
            }
        }
        return null;
    }

    /**
     * Returns the default model page.<p>
     *
     * @return the default model page
     */
    public CmsModelPageConfig getDefaultModelPage() {

        List<CmsModelPageConfig> modelPages = getModelPages();
        for (CmsModelPageConfig modelPageConfig : getModelPages()) {
            if (modelPageConfig.isDefault()) {
                return modelPageConfig;
            }
        }
        if (modelPages.isEmpty()) {
            return null;
        }
        return modelPages.get(0);
    }

    /**
     * Gets the detail information for this sitemap config data object.<p>
     *
     * @param cms the CMS context
     * @return the list of detail information
     */
    public List<DetailInfo> getDetailInfos(CmsObject cms) {

        List<DetailInfo> result = Lists.newArrayList();
        List<CmsDetailPageInfo> detailPages = getAllDetailPages(true);
        Collections.reverse(detailPages); // make sure primary detail pages come later in the list and override other detail pages for the same type
        Map<String, CmsDetailPageInfo> primaryDetailPageMapByType = Maps.newHashMap();
        for (CmsDetailPageInfo pageInfo : detailPages) {
            primaryDetailPageMapByType.put(pageInfo.getType(), pageInfo);
        }
        for (CmsResourceTypeConfig typeConfig : getResourceTypes()) {
            String typeName = typeConfig.getTypeName();
            if (((typeConfig.getFolderOrName() == null) || !typeConfig.getFolderOrName().isPageRelative())
                && primaryDetailPageMapByType.containsKey(typeName)) {
                String folderPath = typeConfig.getFolderPath(cms, null);
                CmsDetailPageInfo pageInfo = primaryDetailPageMapByType.get(typeName);
                result.add(new DetailInfo(folderPath, pageInfo, typeName, getBasePath()));
            }
        }
        return result;
    }

    /**
     * Gets the detail pages for a specific type.<p>
     *
     * @param type the type name
     *
     * @return the list of detail pages for that type
     */
    public List<CmsDetailPageInfo> getDetailPagesForType(String type) {

        List<CmsDetailPageInfo> result = new ArrayList<CmsDetailPageInfo>();
        CmsResourceTypeConfig typeConfig = getResourceType(type);
        if (type.startsWith(CmsDetailPageInfo.FUNCTION_PREFIX)
            || ((typeConfig != null) && !typeConfig.isDetailPagesDisabled())) {

            List<CmsDetailPageInfo> defaultPages = new ArrayList<>();
            for (CmsDetailPageInfo detailpage : getAllDetailPages(true)) {
                if (detailpage.getType().equals(type)) {
                    result.add(detailpage);
                } else if (CmsADEManager.DEFAULT_DETAILPAGE_TYPE.equals(detailpage.getType())) {
                    defaultPages.add(detailpage);
                }
            }
            result.addAll(defaultPages);
        }
        return result;
    }

    /**
     * Returns the direct edit permissions for e.g. list elements with the given type.
     *
     * @param type the resource type name
     * @return the permissions
     */
    public SitemapDirectEditPermissions getDirectEditPermissions(String type) {

        if (type == null) {
            LOG.error("Null type in checkListEdit");
            return SitemapDirectEditPermissions.all;
        }

        if (!getAncestorTypeNames().contains(type)) {
            // not configured anywhere for ADE
            return SitemapDirectEditPermissions.editAndCreate;
        }

        CmsResourceTypeConfig typeConfig = getResourceType(type);
        if (typeConfig == null) {
            return SitemapDirectEditPermissions.none;
        }

        if (typeConfig.isEnabledInLists()) {
            return SitemapDirectEditPermissions.editAndCreate;
        }

        if (typeConfig.isCreateDisabled() || typeConfig.isAddDisabled()) {
            return SitemapDirectEditPermissions.editOnly;
        }

        return SitemapDirectEditPermissions.all;
    }

    /**
     * Gets the display mode for deactivated functions in the gallery dialog.
     *
     * @param defaultValue the default value to return if it's not set
     * @return the display mode for deactivated types
     */
    public CmsGalleryDisabledTypesMode getDisabledFunctionsMode(CmsGalleryDisabledTypesMode defaultValue) {

        CmsADEConfigData parentData = parent();
        if (m_data.getGalleryDisabledFunctionsMode() != null) {
            return m_data.getGalleryDisabledFunctionsMode();
        } else if (parentData != null) {
            return parentData.getDisabledFunctionsMode(defaultValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Gets the display mode for deactivated types in the gallery dialog.
     *
     * @param defaultValue the default value to return if it's not set
     * @return the display mode for deactivated types
     */
    public CmsGalleryDisabledTypesMode getDisabledTypeMode(CmsGalleryDisabledTypesMode defaultValue) {

        CmsADEConfigData parentData = parent();
        if (m_data.getDisabledTypeMode() != null) {
            return m_data.getDisabledTypeMode();
        } else if (parentData != null) {
            return parentData.getDisabledTypeMode(defaultValue);
        } else {
            return defaultValue;
        }
    }

    /**
     * Returns all available display formatters.<p>
     *
     * @param cms the cms context
     *
     * @return the available display formatters
     */
    public List<I_CmsFormatterBean> getDisplayFormatters(CmsObject cms) {

        List<I_CmsFormatterBean> result = new ArrayList<I_CmsFormatterBean>();
        for (I_CmsFormatterBean formatter : getCachedFormatters().getFormatters().values()) {
            if (formatter.isDisplayFormatter()) {
                result.add(formatter);
            }
        }
        return result;
    }

    /**
     * Gets the bean that represents the dynamic function availability.
     *
     * @param formatterConfig the formatter configuration state
     *
     * @return the dynamic function availability
     */
    public CmsFunctionAvailability getDynamicFunctionAvailability(CmsFormatterConfigurationCacheState formatterConfig) {

        CmsADEConfigData parentData = parent();
        CmsFunctionAvailability result;
        if (parentData == null) {
            result = new CmsFunctionAvailability(formatterConfig);
        } else {
            result = parentData.getDynamicFunctionAvailability(formatterConfig);
        }
        Collection<CmsUUID> enabledIds = m_data.getDynamicFunctions();
        Collection<CmsUUID> disabledIds = m_data.getFunctionsToRemove();
        if (m_data.isRemoveAllFunctions() && !m_configSequence.getMeta().isSkipRemovals()) {
            result.removeAll();
        }
        if (enabledIds != null) {
            result.addAll(enabledIds);
        }
        if (disabledIds != null) {
            for (CmsUUID id : disabledIds) {
                result.remove(id);
            }
        }
        return result;
    }

    /**
     * Gets the root path of the closest subsite going up the tree which has the 'exclude external detail contents' option enabled, or '/' if no such subsite exists.
     *
     * @return the root path of the closest subsite with 'external detail contents excluded'
     */
    public String getExternalDetailContentExclusionFolder() {

        if (m_data.isExcludeExternalDetailContents()) {
            String basePath = m_data.getBasePath();
            if (basePath == null) {
                return "/";
            } else {
                return basePath;
            }
        } else {
            CmsADEConfigData parent = parent();
            if (parent != null) {
                return parent.getExternalDetailContentExclusionFolder();
            } else {
                return "/";
            }
        }
    }

    /**
     * Returns the formatter change sets for this and all parent sitemaps, ordered by increasing folder depth of the sitemap.<p>
     *
     * @return the formatter change sets for all ancestor sitemaps
     */
    public List<CmsFormatterChangeSet> getFormatterChangeSets() {

        CmsADEConfigData currentConfig = this;
        List<CmsFormatterChangeSet> result = Lists.newArrayList();
        while (currentConfig != null) {
            CmsFormatterChangeSet changes = currentConfig.getOwnFormatterChangeSet();
            if (changes != null) {
                if (currentConfig.getMeta().isSkipRemovals()) {
                    changes = changes.cloneWithNoRemovals();
                }
                result.add(changes);
            }
            currentConfig = currentConfig.parent();
        }
        Collections.reverse(result);
        return result;
    }

    /**
     * Gets the formatter configuration for a resource.<p>
     *
     * @param cms the current CMS context
     * @param res the resource for which the formatter configuration should be retrieved
     *
     * @return the configuration of formatters for the resource
     */
    public CmsFormatterConfiguration getFormatters(CmsObject cms, CmsResource res) {

        if (CmsResourceTypeFunctionConfig.isFunction(res)) {

            CmsFormatterConfigurationCacheState formatters = getCachedFormatters();
            I_CmsFormatterBean function = findFormatter(res.getStructureId());
            if (function != null) {
                return CmsFormatterConfiguration.create(cms, Collections.singletonList(function));
            } else {
                if ((!res.getStructureId().isNullUUID())
                    && cms.existsResource(res.getStructureId(), CmsResourceFilter.IGNORE_EXPIRATION)) {
                    // usually if it's just been created, but not added to the configuration cache yet
                    CmsFormatterBeanParser parser = new CmsFormatterBeanParser(cms, new HashMap<>());
                    try {
                        function = parser.parse(
                            CmsXmlContentFactory.unmarshal(cms, cms.readFile(res)),
                            res.getRootPath(),
                            "" + res.getStructureId());
                        return CmsFormatterConfiguration.create(cms, Collections.singletonList(function));
                    } catch (Exception e) {
                        LOG.warn(e.getLocalizedMessage(), e);
                        return CmsFormatterConfiguration.EMPTY_CONFIGURATION;
                    }

                } else {
                    // if a new function has been dragged on the page, it doesn't exist in the VFS yet, so we need a different
                    // instance as a replacement
                    CmsResource defaultFormatter = CmsFunctionRenderer.getDefaultFunctionInstance(cms);
                    if (defaultFormatter != null) {
                        I_CmsFormatterBean defaultFormatterBean = formatters.getFormatters().get(
                            defaultFormatter.getStructureId());
                        return CmsFormatterConfiguration.create(cms, Collections.singletonList(defaultFormatterBean));
                    } else {
                        LOG.warn("Could not read default formatter for functions.");
                        return CmsFormatterConfiguration.EMPTY_CONFIGURATION;
                    }
                }
            }
        } else {
            try {
                int resTypeId = res.getTypeId();
                return getFormatters(
                    cms,
                    OpenCms.getResourceManager().getResourceType(resTypeId),
                    getFormattersFromSchema(cms, res));
            } catch (CmsLoaderException e) {
                LOG.warn(e.getLocalizedMessage(), e);
                return CmsFormatterConfiguration.EMPTY_CONFIGURATION;
            }
        }
    }

    /**
     * Gets a named function reference.<p>
     *
     * @param name the name of the function reference
     *
     * @return the function reference for the given name
     */
    public CmsFunctionReference getFunctionReference(String name) {

        List<CmsFunctionReference> functionReferences = getFunctionReferences();
        for (CmsFunctionReference functionRef : functionReferences) {
            if (functionRef.getName().equals(name)) {
                return functionRef;
            }
        }
        return null;
    }

    /**
     * Gets the list of configured function references.<p>
     *
     * @return the list of configured function references
     */
    public List<CmsFunctionReference> getFunctionReferences() {

        return internalGetFunctionReferences();
    }

    /**
     * Gets the map of external (non-schema) formatters which are inactive in this sub-sitemap.<p>
     *
     * @return the map inactive external formatters
     */
    public Map<CmsUUID, I_CmsFormatterBean> getInactiveFormatters() {

        CmsFormatterConfigurationCacheState cacheState = getCachedFormatters();
        Map<CmsUUID, I_CmsFormatterBean> result = Maps.newHashMap(cacheState.getFormatters());
        result.keySet().removeAll(getActiveFormatters().keySet());
        return result;
    }

    /**
     * Gets the list of available model pages.<p>
     *
     * @return the list of available model pages
     */
    public List<CmsModelPageConfig> getModelPages() {

        return getModelPages(false);
    }

    /**
     * Gets the list of available model pages.<p>
     *
     * @param includeDisable <code>true</code> to include disabled model pages
     *
     * @return the list of available model pages
     */
    public List<CmsModelPageConfig> getModelPages(boolean includeDisable) {

        CmsADEConfigData parentData = parent();
        List<CmsModelPageConfig> parentModelPages;
        if ((parentData != null) && !m_data.isDiscardInheritedModelPages()) {
            parentModelPages = parentData.getModelPages();
        } else {
            parentModelPages = Collections.emptyList();
        }

        List<CmsModelPageConfig> result = combineConfigurationElements(
            parentModelPages,
            m_data.getOwnModelPageConfig(),
            includeDisable);
        return result;
    }

    /**
     * Gets the formatter changes for this sitemap configuration.<p>
     *
     * @return the formatter change set
     */
    public CmsFormatterChangeSet getOwnFormatterChangeSet() {

        return m_data.getFormatterChangeSet();
    }

    /**
     * Gets the configuration for the available properties.<p>
     *
     * @return the configuration for the available properties
     */
    public List<CmsPropertyConfig> getPropertyConfiguration() {

        CmsADEConfigData parentData = parent();
        List<CmsPropertyConfig> parentProperties;
        boolean removeInherited = m_data.isDiscardInheritedProperties() && !getMeta().isSkipRemovals();
        if ((parentData != null) && !removeInherited) {
            parentProperties = parentData.getPropertyConfiguration();
        } else {
            parentProperties = Collections.emptyList();
        }
        LinkedHashMap<String, CmsPropertyConfig> propMap = new LinkedHashMap<>();
        for (CmsPropertyConfig conf : parentProperties) {
            if (conf.isDisabled()) {
                continue;
            }
            propMap.put(conf.getName(), conf);
        }
        for (CmsPropertyConfig conf : m_data.getOwnPropertyConfigurations()) {
            if (conf.isDisabled()) {
                propMap.remove(conf.getName());
            } else if (propMap.containsKey(conf.getName())) {
                propMap.put(conf.getName(), propMap.get(conf.getName()).merge(conf));
            } else {
                propMap.put(conf.getName(), conf);
            }
        }
        List<CmsPropertyConfig> result = new ArrayList<>(propMap.values());
        return result;
    }

    /**
     * Computes the ordered map of properties to display in the property dialog, given the map of default property configurations passed as a parameter.
     *
     * @param defaultProperties the default property configurations
     * @return the ordered map of property configurations for the property dialog
     */
    public Map<String, CmsXmlContentProperty> getPropertyConfiguration(
        Map<String, CmsXmlContentProperty> defaultProperties) {

        List<CmsPropertyConfig> myPropConfigs = getPropertyConfiguration();
        Map<String, CmsXmlContentProperty> allProps = new LinkedHashMap<>(defaultProperties);
        Map<String, CmsXmlContentProperty> result = new LinkedHashMap<>();
        for (CmsPropertyConfig prop : myPropConfigs) {
            allProps.put(prop.getName(), prop.getPropertyData());
            if (prop.isTop()) {
                result.put(prop.getName(), prop.getPropertyData());
            }
        }
        for (Map.Entry<String, CmsXmlContentProperty> entry : allProps.entrySet()) {
            if (!result.containsKey(entry.getKey())) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;

    }

    /**
     * Gets the property configuration as a map of CmsXmlContentProperty instances.<p>
     *
     * @return the map of property configurations
     */
    public Map<String, CmsXmlContentProperty> getPropertyConfigurationAsMap() {

        Map<String, CmsXmlContentProperty> result = new LinkedHashMap<String, CmsXmlContentProperty>();
        for (CmsPropertyConfig propConf : getPropertyConfiguration()) {
            result.put(propConf.getName(), propConf.getPropertyData());
        }
        return result;
    }

    /**
     * Returns the resource from which this configuration was read.<p>
     *
     * @return the resource from which this configuration was read
     */
    public CmsResource getResource() {

        return m_data.getResource();
    }

    /**
     * Returns the configuration for a specific resource type.<p>
     *
     * @param typeName the name of the type
     *
     * @return the resource type configuration for that type
     */
    public CmsResourceTypeConfig getResourceType(String typeName) {

        for (CmsResourceTypeConfig type : getResourceTypes()) {
            if (typeName.equals(type.getTypeName())) {
                return type;
            }
        }
        return null;
    }

    /**
     * Gets a list of all available resource type configurations.<p>
     *
     * @return the available resource type configurations
     */
    public List<CmsResourceTypeConfig> getResourceTypes() {

        List<CmsResourceTypeConfig> result = internalGetResourceTypes(true);
        for (CmsResourceTypeConfig config : result) {
            config.initialize(getCms());
        }
        return result;
    }

    /**
     * Gets the searchable resource type configurations.<p>
     *
     * @param cms the current CMS context
     * @return the searchable resource type configurations
     */
    public Collection<CmsResourceTypeConfig> getSearchableTypes(CmsObject cms) {

        return getResourceTypes();
    }

    /**
     * Gets the list of structure ids of the shared setting overrides, ordered by increasing specificity.
     *
     * @return the list of structure ids of shared setting overrides
     */
    public ImmutableList<CmsUUID> getSharedSettingOverrides() {

        if (m_sharedSettingOverrides != null) {
            return m_sharedSettingOverrides;
        }

        CmsADEConfigData currentConfig = this;
        List<CmsADEConfigData> relevantConfigurations = new ArrayList<>();
        while (currentConfig != null) {
            relevantConfigurations.add(currentConfig);
            if (currentConfig.m_data.isRemoveSharedSettingOverrides()
                && !currentConfig.m_configSequence.getMeta().isSkipRemovals()) {
                // once we find a configuration where 'remove all shared setting overrides' is enabled,
                // all parent configurations become irrelevant
                break;
            }
            currentConfig = currentConfig.parent();
        }

        // order by ascending specificity
        Collections.reverse(relevantConfigurations);

        List<CmsUUID> ids = new ArrayList<>();
        for (CmsADEConfigData config : relevantConfigurations) {
            CmsUUID id = config.m_data.getSharedSettingOverride();
            if (id != null) {
                ids.add(id);
            }
        }
        ImmutableList<CmsUUID> result = ImmutableList.copyOf(ids);
        m_sharedSettingOverrides = result;
        return result;
    }

    /**
     * Gets the ids of site plugins which are active in this sitemap configuration.
     *
     * @return the ids of active site plugins
     */
    public Set<CmsUUID> getSitePluginIds() {

        CmsADEConfigData parent = parent();
        Set<CmsUUID> result;
        if ((parent == null) || (m_data.isRemoveAllPlugins() && !getMeta().isSkipRemovals())) {
            result = new HashSet<>();
        } else {
            result = parent.getSitePluginIds();
        }
        result.removeAll(m_data.getRemovedPlugins());
        result.addAll(m_data.getAddedPlugins());
        return result;
    }

    /**
     * Gets the list of site plugins active in this sitemap configuration.
     *
     * @return the list of active site plugins
     */
    public List<CmsSitePlugin> getSitePlugins() {

        Set<CmsUUID> pluginIds = getSitePluginIds();
        List<CmsSitePlugin> result = new ArrayList<>();
        Map<CmsUUID, CmsSitePlugin> plugins = m_cache.getSitePlugins();
        for (CmsUUID id : pluginIds) {
            CmsSitePlugin sitePlugin = plugins.get(id);
            if (sitePlugin != null) {
                result.add(sitePlugin);
            }
        }
        return result;
    }

    /**
     * Gets the type ordering mode.
     *
     * @return the type ordering mode
     */
    public CmsTypeOrderingMode getTypeOrderingMode() {

        CmsTypeOrderingMode ownOrderingMode = m_data.getTypeOrderingMode();
        if (ownOrderingMode != null) {
            return ownOrderingMode;
        } else {
            CmsADEConfigData parentConfig = parent();
            CmsTypeOrderingMode parentMode = null;
            if (parentConfig == null) {
                parentMode = CmsTypeOrderingMode.latestOnTop;
            } else {
                parentMode = parentConfig.getTypeOrderingMode();
            }
            return parentMode;
        }

    }

    /**
     * Gets a map of the active resource type configurations, with type names as keys.
     *
     * @return the map of active types
     */
    public Map<String, CmsResourceTypeConfig> getTypesByName() {

        if (m_typesByName != null) {
            return m_typesByName;
        }
        Map<String, CmsResourceTypeConfig> result = new HashMap<>();
        for (CmsResourceTypeConfig type : getResourceTypes()) {
            result.put(type.getTypeName(), type);
        }
        result = Collections.unmodifiableMap(result);
        m_typesByName = result;
        return result;
    }

    /**
     * Gets the set of resource type names for which schema formatters can be enabled or disabled and which are not disabled in this sub-sitemap.<p>
     *
     * @return the set of types for which schema formatters are active
     */
    public Set<String> getTypesWithActiveSchemaFormatters() {

        Set<String> result = Sets.newHashSet(getTypesWithModifiableFormatters());
        for (CmsFormatterChangeSet changeSet : getFormatterChangeSets()) {
            changeSet.applyToTypes(result);
        }
        return result;
    }

    /**
     * Gets the set of names of resource types which have schema-based formatters that can be enabled or disabled.<p>
     *
     * @return the set of names of resource types which have schema-based formatters that can be enabled or disabled
     */
    public Set<String> getTypesWithModifiableFormatters() {

        Set<String> result = new HashSet<String>();
        for (I_CmsResourceType type : OpenCms.getResourceManager().getResourceTypes()) {
            if (type instanceof CmsResourceTypeXmlContent) {
                CmsXmlContentDefinition contentDef = null;
                try {
                    contentDef = CmsXmlContentDefinition.getContentDefinitionForType(getCms(), type.getTypeName());
                    if ((contentDef != null) && contentDef.getContentHandler().hasModifiableFormatters()) {
                        result.add(type.getTypeName());
                    }
                } catch (Exception e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        return result;

    }

    /**
     * Checks if there are any matching formatters for the given set of containers.<p>
     *
     * @param cms the current CMS context
     * @param resType the resource type for which the formatter configuration should be retrieved
     * @param containers the page containers
     *
     * @return if there are any matching formatters
     */
    public boolean hasFormatters(CmsObject cms, I_CmsResourceType resType, Collection<CmsContainer> containers) {

        try {
            if (CmsXmlDynamicFunctionHandler.TYPE_FUNCTION.equals(resType.getTypeName())
                || CmsResourceTypeFunctionConfig.TYPE_NAME.equals(resType.getTypeName())) {
                // dynamic function may match any container
                return true;
            }
            CmsXmlContentDefinition def = CmsXmlContentDefinition.getContentDefinitionForType(
                cms,
                resType.getTypeName());
            CmsFormatterConfiguration schemaFormatters = def.getContentHandler().getFormatterConfiguration(cms, null);
            CmsFormatterConfiguration formatters = getFormatters(cms, resType, schemaFormatters);
            for (CmsContainer cont : containers) {
                if (cont.isEditable()
                    && (formatters.getAllMatchingFormatters(cont.getType(), cont.getWidth()).size() > 0)) {
                    return true;
                }
            }
        } catch (CmsException e) {
            LOG.warn(e.getLocalizedMessage(), e);

        }
        return false;
    }

    /**
     * Returns the value of the "create contents locally" flag.<p>
     *
     * If this flag is set, contents of types configured in a super-sitemap will be created in the sub-sitemap (if the user
     * creates them from the sub-sitemap).
     *
     * @return the "create contents locally" flag
     */
    public boolean isCreateContentsLocally() {

        return m_data.isCreateContentsLocally();
    }

    /**
     * Returns the value of the "discard inherited model pages" flag.<p>
     *
     * If this flag is set, inherited model pages will be discarded for this sitemap.<p>
     *
     * @return the "discard inherited model pages" flag
     */
    public boolean isDiscardInheritedModelPages() {

        return m_data.isDiscardInheritedModelPages();
    }

    /**
     * Returns the value of the "discard inherited properties" flag.<p>
     *
     * If this is flag is set, inherited property definitions will be discarded for this sitemap.<p>
     *
     * @return the "discard inherited properties" flag.<p>
     */
    public boolean isDiscardInheritedProperties() {

        return m_data.isDiscardInheritedProperties();
    }

    /**
     * Returns the value of the "discard inherited types" flag.<p>
     *
     * If this flag is set, inherited resource types from a super-sitemap will be discarded for this sitemap.<p>
     *
     * @return the "discard inherited types" flag
     */
    public boolean isDiscardInheritedTypes() {

        return m_data.isDiscardInheritedTypes();
    }

    /**
     * True if detail contents outside this sitemap should not be rendered in detail pages from this sitemap.
     *
     * @return true if detail contents outside this sitemap should not be rendered in detail pages from this sitemap.
     */
    public boolean isExcludeExternalDetailContents() {

        return m_data.isExcludeExternalDetailContents();
    }

    /**
     * Checks if dynamic functions not matching any containers should be hidden.
     *
     * @return true if dynamic functions not matching any containers should be hidden
     */
    public boolean isHideNonMatchingFunctions() {

        return getDisabledFunctionsMode(CmsGalleryDisabledTypesMode.hide) == CmsGalleryDisabledTypesMode.hide;
    }

    /**
     * Returns true if the subsite should be included in the site selector.
     *
     * @return true if the subsite should be included in the site selector
     */
    public boolean isIncludeInSiteSelector() {

        return m_configSequence.getConfig().isIncludeInSiteSelector();
    }

    /**
     * Returns true if this is a module configuration instead of a normal sitemap configuration.<p>
     *
     * @return true if this is a module configuration
     */
    public boolean isModuleConfiguration() {

        return m_data.isModuleConfig();
    }

    /**
     * Returns true if detail pages from this sitemap should be preferred for links to contents in this sitemap.<p>
     *
     * @return true if detail pages from this sitemap should be preferred for links to contents in this sitemap
     */
    public boolean isPreferDetailPagesForLocalContents() {

        return m_data.isPreferDetailPagesForLocalContents();
    }

    /**
     * Checks if any formatter with the given JSP id has the 'search content' option set to true.
     *
     * @param jspId the structure id of a formatter JSP
     * @return true if any of the formatters
     */
    public boolean isSearchContentFormatter(CmsUUID jspId) {

        for (I_CmsFormatterBean formatter : getFormattersByJspId().get(jspId)) {
            if (formatter.isSearchContent()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the new container page format, which uses formatter keys (but also is different in other ways from the new format
     *
     * @return true if formatter keys should be used
     */
    public boolean isUseFormatterKeys() {

        Boolean result = m_data.getUseFormatterKeys();
        if (result != null) {
            LOG.debug("isUseFormatterKeys - found value " + result + " at " + getBasePath());
            return result.booleanValue();
        }
        CmsADEConfigData parent = parent();
        if (parent != null) {
            return parent.isUseFormatterKeys();
        }
        boolean defaultValue = true;
        LOG.debug("isUseFormatterKeys - using defaultValue " + defaultValue);
        return defaultValue;
    }

    /**
     * Fetches the parent configuration of this configuration.<p>
     *
     * If this configuration is a sitemap configuration with no direct parent configuration,
     * the module configuration will be returned. If this configuration already is a module configuration,
     * null will be returned.<p>
     *
     * @return the parent configuration
     */
    public CmsADEConfigData parent() {

        Optional<CmsADEConfigurationSequence> parentPath = m_configSequence.getParent();
        if (parentPath.isPresent()) {
            CmsADEConfigDataInternal internalData = parentPath.get().getConfig();
            return new CmsADEConfigData(internalData, m_cache, parentPath.get());
        } else {
            return null;
        }
    }

    /**
     * Returns true if the sitemap attribute editor should be available in this subsite.
     *
     * @return true if the sitemap attribute editor dialog should be available
     */
    public boolean shouldShowSitemapAttributeDialog() {

        return getAttributeEditorConfiguration().getAttributeDefinitions().size() > 0;
    }

    /**
     * Clears the internal formatter caches.
     *
     * <p>This should only be used for test cases.
     */
    protected void clearCaches() {

        m_activeFormatters = null;
        m_activeFormattersByKey = null;
        m_formattersByKey = null;
        m_formattersByJspId = null;
        m_formattersByTypeCache.invalidateAll();
    }

    /**
     * Creates the content directory for this configuration node if possible.<p>
     *
     * @throws CmsException if something goes wrong
     */
    protected void createContentDirectory() throws CmsException {

        if (!isModuleConfiguration()) {
            String contentFolder = getContentFolderPath();
            if (!getCms().existsResource(contentFolder)) {
                getCms().createResource(
                    contentFolder,
                    OpenCms.getResourceManager().getResourceType(CmsResourceTypeFolder.getStaticTypeName()));
            }
        }
    }

    /**
     * Gets the CMS object used for VFS operations.<p>
     *
     * @return the CMS object used for VFS operations
     */
    protected CmsObject getCms() {

        return m_cache.getCms();
    }

    /**
     * Gets the CMS object used for VFS operations.<p>
     *
     * @return the CMS object
     */
    protected CmsObject getCmsObject() {

        return getCms();
    }

    /**
     * Helper method to converts a list of detail pages to a map from type names to lists of detail pages for each type.<p>
     *
     * @param detailPages the list of detail pages
     *
     * @return the map of detail pages
     */
    protected Map<String, List<CmsDetailPageInfo>> getDetailPagesMap(List<CmsDetailPageInfo> detailPages) {

        Map<String, List<CmsDetailPageInfo>> result = Maps.newHashMap();
        for (CmsDetailPageInfo detailpage : detailPages) {
            String type = detailpage.getType();
            if (!result.containsKey(type)) {
                result.put(type, new ArrayList<CmsDetailPageInfo>());
            }
            result.get(type).add(detailpage);
        }
        return result;
    }

    /**
     * Collects the folder types in a map.<p>
     *
     * @return the map of folder types
     *
     * @throws CmsException if something goes wrong
     */
    protected Map<String, String> getFolderTypes() throws CmsException {

        Map<String, String> result = new HashMap<String, String>();
        CmsObject cms = OpenCms.initCmsObject(getCms());
        if (m_data.isModuleConfig()) {
            Set<String> siteRoots = OpenCms.getSiteManager().getSiteRoots();
            for (String siteRoot : siteRoots) {
                cms.getRequestContext().setSiteRoot(siteRoot);
                for (CmsResourceTypeConfig config : getResourceTypes()) {
                    if (!config.isDetailPagesDisabled()) {
                        String typeName = config.getTypeName();
                        if (!config.isPageRelative()) { // elements stored with container pages can not be used as detail contents
                            String folderPath = config.getFolderPath(cms, null);
                            result.put(CmsStringUtil.joinPaths(folderPath, "/"), typeName);
                        }
                    }
                }
            }
        } else {
            for (CmsResourceTypeConfig config : getResourceTypes()) {
                if (!config.isDetailPagesDisabled()) {
                    String typeName = config.getTypeName();
                    if (!config.isPageRelative()) { // elements stored with container pages can not be used as detail contents
                        String folderPath = config.getFolderPath(getCms(), null);
                        result.put(CmsStringUtil.joinPaths(folderPath, "/"), typeName);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Gets the formatter configuration for a resource type.<p>
     *
     * @param cms the current CMS context
     * @param resType the resource type
     * @param schemaFormatters the resource schema formatters
     *
     * @return the configuration of formatters for the resource type
     */
    protected CmsFormatterConfiguration getFormatters(
        CmsObject cms,
        I_CmsResourceType resType,
        CmsFormatterConfiguration schemaFormatters) {

        String typeName = resType.getTypeName();
        List<I_CmsFormatterBean> formatters = new ArrayList<I_CmsFormatterBean>();
        Set<String> types = new HashSet<String>();
        types.add(typeName);
        for (CmsFormatterChangeSet changeSet : getFormatterChangeSets()) {
            if (changeSet != null) {
                changeSet.applyToTypes(types);
            }
        }

        if ((schemaFormatters != null) && types.contains(typeName)) {
            for (I_CmsFormatterBean formatter : schemaFormatters.getAllFormatters()) {
                formatters.add(formatter);
            }
        }

        try {
            List<I_CmsFormatterBean> formattersForType = m_formattersByTypeCache.get(typeName);
            formatters.addAll(formattersForType);
        } catch (ExecutionException e) {
            LOG.error(e.getLocalizedMessage(), e);

        }
        return CmsFormatterConfiguration.create(cms, formatters);
    }

    /**
     * Gets the formatters from the schema.<p>
     *
     * @param cms the current CMS context
     * @param res the resource for which the formatters should be retrieved
     *
     * @return the formatters from the schema
     */
    protected CmsFormatterConfiguration getFormattersFromSchema(CmsObject cms, CmsResource res) {

        try {
            return OpenCms.getResourceManager().getResourceType(res.getTypeId()).getFormattersForResource(cms, res);
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return CmsFormatterConfiguration.EMPTY_CONFIGURATION;
        }
    }

    /**
     * Gets the metadata about how this configuration was referenced.
     *
     * @return the metadata
     */
    protected ConfigReferenceMeta getMeta() {

        return m_configSequence.getMeta();
    }

    /**
     * Internal method for getting the function references.<p>
     *
     * @return the function references
     */
    protected List<CmsFunctionReference> internalGetFunctionReferences() {

        CmsADEConfigData parentData = parent();
        if ((parentData == null)) {
            if (m_data.isModuleConfig()) {
                return Collections.unmodifiableList(m_data.getFunctionReferences());
            } else {
                return Lists.newArrayList();
            }
        } else {
            return parentData.internalGetFunctionReferences();

        }
    }

    /**
     * Helper method for getting the list of resource types.<p>
     *
     * @param filterDisabled true if disabled types should be filtered from the result
     *
     * @return the list of resource types
     */
    protected List<CmsResourceTypeConfig> internalGetResourceTypes(boolean filterDisabled) {

        CmsADEConfigData parentData = parent();
        List<CmsResourceTypeConfig> parentResourceTypes = null;
        if (parentData == null) {
            parentResourceTypes = Lists.newArrayList();
        } else {
            parentResourceTypes = Lists.newArrayList();
            for (CmsResourceTypeConfig typeConfig : parentData.internalGetResourceTypes(false)) {
                CmsResourceTypeConfig copiedType = typeConfig.copy(
                    m_data.isDiscardInheritedTypes() && !getMeta().isSkipRemovals());
                parentResourceTypes.add(copiedType);
            }
        }
        String template = getMeta().getTemplate();
        List<CmsResourceTypeConfig> result = combineConfigurationElements(
            parentResourceTypes,
            m_data.getOwnResourceTypes().stream().map(type -> type.markWithTemplate(template)).collect(
                Collectors.toList()),
            true);
        if (m_data.isCreateContentsLocally()) {
            for (CmsResourceTypeConfig typeConfig : result) {
                typeConfig.updateBasePath(
                    CmsStringUtil.joinPaths(m_data.getBasePath(), CmsADEManager.CONTENT_FOLDER_NAME));
            }
        }
        if (filterDisabled) {
            Iterator<CmsResourceTypeConfig> iter = result.iterator();
            while (iter.hasNext()) {
                CmsResourceTypeConfig typeConfig = iter.next();
                if (typeConfig.isDisabled()) {
                    iter.remove();
                }
            }
        }
        if (getTypeOrderingMode() == CmsTypeOrderingMode.byDisplayOrder) {
            Collections.sort(result, (a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
        }
        return result;
    }

    /**
     * Merges two lists of detail pages, one from a parent configuration and one from a child configuration.<p>
     *
     * @param parentDetailPages the parent's detail pages
     * @param ownDetailPages the child's detail pages
     *
     * @return the merged detail pages
     */
    protected List<CmsDetailPageInfo> mergeDetailPages(
        List<CmsDetailPageInfo> parentDetailPages,
        List<CmsDetailPageInfo> ownDetailPages) {

        List<CmsDetailPageInfo> parentDetailPageCopies = Lists.newArrayList();
        for (CmsDetailPageInfo info : parentDetailPages) {
            parentDetailPageCopies.add(info.copyAsInherited());
        }

        List<CmsDetailPageInfo> result = new ArrayList<CmsDetailPageInfo>();
        Map<String, List<CmsDetailPageInfo>> resultDetailPageMap = Maps.newHashMap();
        Map<String, List<CmsDetailPageInfo>> parentPagesGroupedByType = getDetailPagesMap(parentDetailPageCopies);
        Map<String, List<CmsDetailPageInfo>> childPagesGroupedByType = getDetailPagesMap(ownDetailPages);
        Set<String> allTypes = new HashSet<>();
        allTypes.addAll(parentPagesGroupedByType.keySet());
        allTypes.addAll(childPagesGroupedByType.keySet());
        for (String type : allTypes) {

            List<CmsDetailPageInfo> parentPages = parentPagesGroupedByType.get(type);
            List<CmsDetailPageInfo> childPages = childPagesGroupedByType.get(type);
            List<CmsDetailPageInfo> merged = mergeDetailPagesForType(parentPages, childPages);
            resultDetailPageMap.put(type, merged);
        }
        result = new ArrayList<CmsDetailPageInfo>();
        for (List<CmsDetailPageInfo> pages : resultDetailPageMap.values()) {
            result.addAll(pages);
        }
        return result;
    }

    /**
     * Helper method to correct paths in detail page beans if the corresponding resources have been moved.<p>
     *
     * @param detailPages the original list of detail pages
     *
     * @return the corrected list of detail pages
     */
    protected List<CmsDetailPageInfo> updateUris(List<CmsDetailPageInfo> detailPages) {

        List<CmsDetailPageInfo> result = new ArrayList<CmsDetailPageInfo>();
        for (CmsDetailPageInfo page : detailPages) {
            CmsUUID structureId = page.getId();
            try {
                String rootPath = OpenCms.getADEManager().getRootPath(
                    structureId,
                    getCms().getRequestContext().getCurrentProject().isOnlineProject());
                String iconClasses;
                if (page.getType().startsWith(CmsDetailPageInfo.FUNCTION_PREFIX)) {
                    iconClasses = CmsIconUtil.getIconClasses(CmsXmlDynamicFunctionHandler.TYPE_FUNCTION, null, false);
                } else {
                    iconClasses = CmsIconUtil.getIconClasses(page.getType(), null, false);
                }
                CmsDetailPageInfo correctedPage = new CmsDetailPageInfo(
                    structureId,
                    rootPath,
                    page.getType(),
                    page.getQualifier(),
                    iconClasses);
                result.add(page.isInherited() ? correctedPage.copyAsInherited() : correctedPage);
            } catch (CmsException e) {
                LOG.warn(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Gets a multimap of active formatters for which a formatter key is defined, with the formatter keys as map keys.
     *
     * @return the map of active formatters by key
     */
    private Multimap<String, I_CmsFormatterBean> getActiveFormattersByKey() {

        if (m_activeFormattersByKey == null) {
            ArrayListMultimap<String, I_CmsFormatterBean> activeFormattersByKey = ArrayListMultimap.create();
            for (I_CmsFormatterBean formatter : getActiveFormatters().values()) {
                for (String key : formatter.getAllKeys()) {
                    activeFormattersByKey.put(key, formatter);
                }
            }
            m_activeFormattersByKey = activeFormattersByKey;
        }
        return m_activeFormattersByKey;
    }

    /**
     * Gets a formatter with the given key from a multimap, and warns if there are multiple values
     * for the key.
     *
     * @param formatterMap the formatter multimap
     * @param name the formatter key
     * @param noWarn if true, disables warnings
     * @return the formatter for the key (null if none are found, the first one if multiple are found)
     */
    private I_CmsFormatterBean getFormatterAndWarnIfAmbiguous(
        Multimap<String, I_CmsFormatterBean> formatterMap,
        String name,
        boolean noWarn) {

        I_CmsFormatterBean result;
        result = null;
        Collection<I_CmsFormatterBean> activeForKey = formatterMap.get(name);
        if (activeForKey.size() > 0) {
            if (activeForKey.size() > 1) {
                if (!noWarn) {
                    String labels = ""
                        + activeForKey.stream().map(this::getFormatterLabel).collect(Collectors.toList());
                    String message = "Ambiguous formatter for key '"
                        + name
                        + "' at '"
                        + getBasePath()
                        + "': found "
                        + labels;
                    LOG.warn(message);
                    OpenCmsServlet.withRequestCache(
                        rc -> rc.addLog(REQUEST_LOG_CHANNEL, "warn", REQ_LOG_PREFIX + message));
                }
            }
            result = activeForKey.iterator().next();
        }
        return result;
    }

    /**
     * Gets a user-friendly formatter label to use for logging.
     *
     * @param formatter a formatter bean
     * @return the formatter label for the log
     */
    private String getFormatterLabel(I_CmsFormatterBean formatter) {

        return formatter.getLocation() != null ? formatter.getLocation() : formatter.getId();
    }

    /**
     * Gets formatters by JSP id.
     *
     * @return the multimap from JSP id to formatter beans
     */
    private Multimap<CmsUUID, I_CmsFormatterBean> getFormattersByJspId() {

        if (m_formattersByJspId == null) {
            ArrayListMultimap<CmsUUID, I_CmsFormatterBean> formattersByJspId = ArrayListMultimap.create();
            for (I_CmsFormatterBean formatter : getCachedFormatters().getFormatters().values()) {
                formattersByJspId.put(formatter.getJspStructureId(), formatter);
            }
            m_formattersByJspId = formattersByJspId;
        }
        return m_formattersByJspId;
    }

    /**
     * Gets a multimap of the formatters for which a formatter key is defined, with the formatter keys as map keys.
     *
     * @return the map of formatters by key
     */
    private Multimap<String, I_CmsFormatterBean> getFormattersByKey() {

        if (m_formattersByKey == null) {
            ArrayListMultimap<String, I_CmsFormatterBean> formattersByKey = ArrayListMultimap.create();
            for (I_CmsFormatterBean formatter : getCachedFormatters().getFormatters().values()) {
                for (String key : formatter.getAllKeys()) {
                    formattersByKey.put(key, formatter);
                }
            }
            m_formattersByKey = formattersByKey;
        }
        return m_formattersByKey;
    }

    /**
     * Merges detail pages for a specific resource type from a parent and child sitemap.
     *
     * @param parentPages the detail pages from the parent sitemap
     * @param childPages the detail pages from the child sitemap
     * @return the merged detail pages
     */
    private List<CmsDetailPageInfo> mergeDetailPagesForType(
        List<CmsDetailPageInfo> parentPages,
        List<CmsDetailPageInfo> childPages) {

        List<CmsDetailPageInfo> merged = null;
        if ((parentPages != null) && (childPages != null)) {
            // the only nontrivial case. If the child detail pages contain one with an unqualified type, they completely override the parent detail pages.
            // otherwise they only override the parent detail pages for each matching qualifier.

            if (childPages.stream().anyMatch(page -> page.getQualifier() == null)) {
                merged = childPages;
            } else {
                Map<String, List<CmsDetailPageInfo>> pagesGroupedByQualifiedType = parentPages.stream().collect(
                    Collectors.groupingBy(page -> page.getQualifiedType()));
                pagesGroupedByQualifiedType.putAll(
                    childPages.stream().collect(Collectors.groupingBy(page -> page.getQualifiedType())));
                merged = pagesGroupedByQualifiedType.entrySet().stream().flatMap(
                    entry -> entry.getValue().stream()).collect(Collectors.toList());
            }
        } else if (parentPages != null) {
            merged = parentPages;
        } else if (childPages != null) {
            merged = childPages;
        } else {
            merged = new ArrayList<>();
        }
        return merged;
    }

}
