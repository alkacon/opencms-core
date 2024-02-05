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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.loader;

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypeUnknownFile;
import org.opencms.file.types.CmsResourceTypeUnknownFolder;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleManager;
import org.opencms.relations.CmsRelationType;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsDefaultSet;
import org.opencms.util.CmsHtmlConverter;
import org.opencms.util.CmsHtmlConverterJTidy;
import org.opencms.util.CmsHtmlConverterOption;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsHtmlConverter;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.xml.CmsXmlContentDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Collects all available resource loaders, resource types and resource collectors at startup and provides
 * methods to access them during OpenCms runtime.<p>
 *
 * @since 6.0.0
 */
public class CmsResourceManager {

    /**
     * Bean containing a template resource and the name of the template.<p>
     */
    public static class NamedTemplate {

        /** The template name. */
        private String m_name;

        /** The template resource. */
        private CmsResource m_resource;

        /**
         * Creates a new instance.<p>
         *
         * @param resource the template resource
         * @param name the template name
         */
        public NamedTemplate(CmsResource resource, String name) {

            m_resource = resource;
            m_name = name;
        }

        /**
         * Gets the template name.<p>
         *
         * @return the template name
         */
        public String getName() {

            return m_name;
        }

        /**
         * Gets the template resource.<p>
         *
         * @return the template resource
         */
        public CmsResource getResource() {

            return m_resource;
        }
    }

    /**
     * Contains the part of the resource manager configuration that can be changed
     * during runtime by the import / deletion of a module.<p>
     *
     * A module can add resource types and extension mappings to resource types.<p>
     */
    static final class CmsResourceManagerConfiguration {

        /** The mappings of file extensions to resource types. */
        protected Map<String, String> m_extensionMappings;

        /** A list that contains all initialized resource types. */
        protected List<I_CmsResourceType> m_resourceTypeList;

        /** A list that contains all initialized resource types, plus configured types for "unknown" resources. */
        protected List<I_CmsResourceType> m_resourceTypeListWithUnknown;

        /** A map that contains all initialized resource types mapped to their type id. */
        private Map<Integer, I_CmsResourceType> m_resourceTypeIdMap;

        /** A map that contains all initialized resource types mapped to their type name. */
        private Map<String, I_CmsResourceType> m_resourceTypeNameMap;

        /**
         * Creates a new resource manager data storage.<p>
         */
        protected CmsResourceManagerConfiguration() {

            m_resourceTypeIdMap = new HashMap<Integer, I_CmsResourceType>(128);
            m_resourceTypeNameMap = new HashMap<String, I_CmsResourceType>(128);
            m_extensionMappings = new HashMap<String, String>(128);
            m_resourceTypeList = new ArrayList<I_CmsResourceType>(32);
        }

        /**
         * Adds a resource type to the list of configured resource types.<p>
         *
         * @param type the resource type to add
         */
        protected void addResourceType(I_CmsResourceType type) {

            m_resourceTypeIdMap.put(Integer.valueOf(type.getTypeId()), type);
            m_resourceTypeNameMap.put(type.getTypeName(), type);
            m_resourceTypeList.add(type);
        }

        /**
         * Freezes the current configuration by making all data structures unmodifiable
         * that can be accessed form outside this class.<p>
         *
         * @param restypeUnknownFolder the configured default resource type for unknown folders
         * @param restypeUnknownFile the configured default resource type for unknown files
         */
        protected void freeze(I_CmsResourceType restypeUnknownFolder, I_CmsResourceType restypeUnknownFile) {

            // generate the resource type list with unknown resource types
            m_resourceTypeListWithUnknown = new ArrayList<I_CmsResourceType>(m_resourceTypeList.size() + 2);
            if (restypeUnknownFolder != null) {
                m_resourceTypeListWithUnknown.add(restypeUnknownFolder);
            }
            if (restypeUnknownFile != null) {
                m_resourceTypeListWithUnknown.add(restypeUnknownFile);
            }
            m_resourceTypeListWithUnknown.addAll(m_resourceTypeList);

            // freeze the current configuration
            m_resourceTypeListWithUnknown = Collections.unmodifiableList(m_resourceTypeListWithUnknown);
            m_resourceTypeList = Collections.unmodifiableList(m_resourceTypeList);
            m_extensionMappings = Collections.unmodifiableMap(m_extensionMappings);
        }

        /**
         * Returns the configured resource type with the matching type id, or <code>null</code>
         * if a resource type with that id is not configured.<p>
         *
         * @param typeId the type id to get the resource type for
         *
         * @return the configured resource type with the matching type id, or <code>null</code>
         */
        protected I_CmsResourceType getResourceTypeById(int typeId) {

            return m_resourceTypeIdMap.get(Integer.valueOf(typeId));
        }

        /**
         * Returns the configured resource type with the matching type name, or <code>null</code>
         * if a resource type with that name is not configured.<p>
         *
         * @param typeName the type name to get the resource type for
         *
         * @return the configured resource type with the matching type name, or <code>null</code>
         */
        protected I_CmsResourceType getResourceTypeByName(String typeName) {

            return m_resourceTypeNameMap.get(typeName);
        }
    }

    /** The path to the default template. */
    public static final String DEFAULT_TEMPLATE = CmsWorkplace.VFS_PATH_COMMONS + "template/default.jsp";

    /** The MIME type <code>"text/html"</code>. */
    public static final String MIMETYPE_HTML = "text/html";

    /** The MIME type <code>"text/plain"</code>. */
    public static final String MIMETYPE_TEXT = "text/plain";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceManager.class);

    /** The map for all configured collector names, mapped to their collector class. */
    private Map<String, I_CmsResourceCollector> m_collectorNameMappings;

    /** The list of all currently configured content collector instances. */
    private List<I_CmsResourceCollector> m_collectors;

    /** The current resource manager configuration. */
    private CmsResourceManagerConfiguration m_configuration;

    /** The list of all configured HTML converters. */
    private List<CmsHtmlConverterOption> m_configuredHtmlConverters;

    /** The list of all configured MIME types. */
    private List<CmsMimeType> m_configuredMimeTypes;

    /** The list of all configured relation types. */
    private List<CmsRelationType> m_configuredRelationTypes;

    /** Filename translator, used only for the creation of new files. */
    private CmsResourceTranslator m_fileTranslator;

    /** Folder translator, used to translate all accesses to resources. */
    private CmsResourceTranslator m_folderTranslator;

    /** Indicates if the configuration is finalized (frozen). */
    private boolean m_frozen;

    /** The OpenCms map of configured HTML converters. */
    private Map<String, String> m_htmlConverters;

    /** A list that contains all initialized resource loaders. */
    private List<I_CmsResourceLoader> m_loaderList;

    /** All initialized resource loaders, mapped to their id. */
    private I_CmsResourceLoader[] m_loaders;

    /** The OpenCms map of configured MIME types. */
    private Map<String, String> m_mimeTypes;

    /** The URL name generator for XML contents. */
    private I_CmsFileNameGenerator m_nameGenerator = new CmsDefaultFileNameGenerator();

    /** A list that contains all resource types added from the XML configuration. */
    private List<I_CmsResourceType> m_resourceTypesFromXml;

    /** The configured default type for files when the resource type is missing. */
    private I_CmsResourceType m_restypeUnknownFile;

    /** The configured default type for folders when the resource type is missing. */
    private I_CmsResourceType m_restypeUnknownFolder;

    /** Cache for template names. */
    private CmsVfsMemoryObjectCache m_templateNameCache = new CmsVfsMemoryObjectCache();

    /** XSD translator, used to translate all accesses to XML schemas from Strings. */
    private CmsResourceTranslator m_xsdTranslator;

    /**
     * Creates a new instance for the resource manager,
     * will be called by the VFS configuration manager.<p>
     */
    public CmsResourceManager() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_STARTING_LOADER_CONFIG_0));
        }

        m_resourceTypesFromXml = new ArrayList<I_CmsResourceType>();
        m_loaders = new I_CmsResourceLoader[16];
        m_loaderList = new ArrayList<I_CmsResourceLoader>();
        m_configuredMimeTypes = new ArrayList<CmsMimeType>();
        m_configuredRelationTypes = new ArrayList<CmsRelationType>();
        m_configuredHtmlConverters = new ArrayList<CmsHtmlConverterOption>();
    }

    /**
     * Adds a given content collector class to the type manager.<p>
     *
     * @param className the name of the class to add
     * @param order the order number for this collector
     *
     * @return the created content collector instance
     *
     * @throws CmsConfigurationException in case the collector could not be properly initialized
     */
    public synchronized I_CmsResourceCollector addContentCollector(String className, String order)
    throws CmsConfigurationException {

        Class<?> classClazz;
        // init class for content collector
        try {
            classClazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_CONTENT_COLLECTOR_CLASS_NOT_FOUND_1, className), e);
            return null;
        }

        I_CmsResourceCollector collector;
        try {
            collector = (I_CmsResourceCollector)classClazz.newInstance();
        } catch (InstantiationException e) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_INVALID_COLLECTOR_NAME_1, className));
        } catch (IllegalAccessException e) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_INVALID_COLLECTOR_NAME_1, className));
        } catch (ClassCastException e) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_INVALID_COLLECTOR_NAME_1, className));
        }

        // set the configured order for the collector
        int ord = 0;
        try {
            ord = Integer.valueOf(order).intValue();
        } catch (NumberFormatException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_COLLECTOR_BAD_ORDER_NUMBER_1, className), e);
        }
        collector.setOrder(ord);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ADD_COLLECTOR_CLASS_2, className, order));
        }

        // extend or init the current list of configured collectors
        if (m_collectors != null) {
            m_collectors = new ArrayList<I_CmsResourceCollector>(m_collectors);
            m_collectorNameMappings = new HashMap<String, I_CmsResourceCollector>(m_collectorNameMappings);
        } else {
            m_collectors = new ArrayList<I_CmsResourceCollector>();
            m_collectorNameMappings = new HashMap<String, I_CmsResourceCollector>();
        }

        if (!m_collectors.contains(collector)) {
            // this is a collector not currently configured
            m_collectors.add(collector);

            Iterator<String> i = collector.getCollectorNames().iterator();
            while (i.hasNext()) {
                String name = i.next();
                if (m_collectorNameMappings.containsKey(name)) {
                    // this name is already configured, check the order of the collector
                    I_CmsResourceCollector otherCollector = m_collectorNameMappings.get(name);
                    if (collector.getOrder() > otherCollector.getOrder()) {
                        // new collector has a greater order than the old collector in the Map
                        m_collectorNameMappings.put(name, collector);
                        if (CmsLog.INIT.isInfoEnabled()) {
                            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_COLLECTOR_REPLACED_1, name));
                        }
                    } else {
                        if (CmsLog.INIT.isInfoEnabled()) {
                            CmsLog.INIT.info(
                                Messages.get().getBundle().key(Messages.INIT_DUPLICATE_COLLECTOR_SKIPPED_1, name));
                        }
                    }
                } else {
                    m_collectorNameMappings.put(name, collector);
                    if (CmsLog.INIT.isInfoEnabled()) {
                        CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ADD_COLLECTOR_1, name));
                    }
                }
            }
        }

        // ensure list is unmodifiable to avoid potential misuse or accidental changes
        Collections.sort(m_collectors);
        m_collectors = Collections.unmodifiableList(m_collectors);
        m_collectorNameMappings = Collections.unmodifiableMap(m_collectorNameMappings);

        // return the created collector instance
        return collector;
    }

    /**
     * Adds a new HTML converter class to internal list of loaded converter classes.<p>
     *
     * @param name the name of the option that should trigger the HTML converter class
     * @param className the name of the class to add
     *
     * @return the created HTML converter instance
     *
     * @throws CmsConfigurationException in case the HTML converter could not be properly initialized
     */
    public I_CmsHtmlConverter addHtmlConverter(String name, String className) throws CmsConfigurationException {

        // check if new conversion option can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }

        Class<?> classClazz;
        // init class for content converter
        try {
            classClazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_HTML_CONVERTER_CLASS_NOT_FOUND_1, className), e);
            return null;
        }

        I_CmsHtmlConverter converter;
        try {
            converter = (I_CmsHtmlConverter)classClazz.newInstance();
        } catch (InstantiationException e) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_INVALID_HTMLCONVERTER_NAME_1, className));
        } catch (IllegalAccessException e) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_INVALID_HTMLCONVERTER_NAME_1, className));
        } catch (ClassCastException e) {
            throw new CmsConfigurationException(
                Messages.get().container(Messages.ERR_INVALID_HTMLCONVERTER_NAME_1, className));
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_ADD_HTML_CONVERTER_CLASS_2, className, name));
        }

        m_configuredHtmlConverters.add(new CmsHtmlConverterOption(name, className));
        return converter;
    }

    /**
     * Adds a new loader to the internal list of loaded loaders.<p>
     *
     * @param loader the loader to add
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public void addLoader(I_CmsResourceLoader loader) throws CmsConfigurationException {

        // check if new loaders can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }

        // add the loader to the internal list of loaders
        int pos = loader.getLoaderId();
        if (pos >= m_loaders.length) {
            I_CmsResourceLoader[] buffer = new I_CmsResourceLoader[pos * 2];
            System.arraycopy(m_loaders, 0, buffer, 0, m_loaders.length);
            m_loaders = buffer;
        }
        m_loaders[pos] = loader;
        m_loaderList.add(loader);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_ADD_LOADER_2,
                    loader.getClass().getName(),
                    Integer.valueOf(pos)));
        }
    }

    /**
     * Adds a new MIME type from the XML configuration to the internal list of MIME types.<p>
     *
     * @param extension the MIME type extension
     * @param type the MIME type description
     *
     * @return the created MIME type instance
     *
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public CmsMimeType addMimeType(String extension, String type) throws CmsConfigurationException {

        // check if new mime types can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }

        CmsMimeType mimeType = new CmsMimeType(extension, type);
        m_configuredMimeTypes.add(mimeType);
        return mimeType;
    }

    /**
     * Adds a new relation type from the XML configuration to the list of user defined relation types.<p>
     *
     * @param name the name of the relation type
     * @param type the type of the relation type, weak or strong
     *
     * @return the new created relation type instance
     *
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public CmsRelationType addRelationType(String name, String type) throws CmsConfigurationException {

        // check if new relation types can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }

        CmsRelationType relationType = new CmsRelationType(m_configuredRelationTypes.size(), name, type);
        m_configuredRelationTypes.add(relationType);
        return relationType;
    }

    /**
     * Adds a new resource type from the XML configuration to the internal list of loaded resource types.<p>
     *
     * Resource types can also be added from a module.<p>
     *
     * @param resourceType the resource type to add
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public void addResourceType(I_CmsResourceType resourceType) throws CmsConfigurationException {

        // check if new resource types can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }

        I_CmsResourceType conflictingType = null;
        if (resourceType.getTypeId() == CmsResourceTypeUnknownFile.RESOURCE_TYPE_ID) {
            // default unknown file resource type
            if (m_restypeUnknownFile != null) {
                // error: already set
                conflictingType = m_restypeUnknownFile;
            } else {
                m_restypeUnknownFile = resourceType;
                return;
            }
        } else if (resourceType.getTypeId() == CmsResourceTypeUnknownFolder.RESOURCE_TYPE_ID) {
            // default unknown folder resource type
            if (m_restypeUnknownFolder != null) {
                // error: already set
                conflictingType = m_restypeUnknownFolder;
            } else {
                m_restypeUnknownFolder = resourceType;
                return;
            }
        } else {
            // normal resource types
            int conflictIndex = m_resourceTypesFromXml.indexOf(resourceType);
            if (conflictIndex >= 0) {
                conflictingType = m_resourceTypesFromXml.get(conflictIndex);
            }
        }
        if (conflictingType != null) {
            // configuration problem: the resource type (or at least the id or the name) is already configured
            throw new CmsConfigurationException(
                Messages.get().container(
                    Messages.ERR_CONFLICTING_RESOURCE_TYPES_4,
                    new Object[] {
                        resourceType.getTypeName(),
                        Integer.valueOf(resourceType.getTypeId()),
                        conflictingType.getTypeName(),
                        Integer.valueOf(conflictingType.getTypeId())}));
        }

        m_resourceTypesFromXml.add(resourceType);
    }

    /**
     * Gets the map of forbidden contexts for resource types.<p>
     *
     * @param cms the current CMS context
     * @return the map from resource types to the forbidden contexts
     */
    public Map<String, CmsDefaultSet<String>> getAllowedContextMap(CmsObject cms) {

        Map<String, CmsDefaultSet<String>> result = new HashMap<String, CmsDefaultSet<String>>();
        for (I_CmsResourceType resType : getResourceTypes()) {
            if (resType instanceof CmsResourceTypeXmlContent) {
                String schema = null;
                try {
                    schema = ((CmsResourceTypeXmlContent)resType).getSchema();
                    if (schema != null) {
                        CmsXmlContentDefinition contentDefinition = CmsXmlContentDefinition.unmarshal(cms, schema);

                        CmsDefaultSet<String> allowedContexts = contentDefinition.getContentHandler().getAllowedTemplates();
                        result.put(resType.getTypeName(), allowedContexts);
                    } else {
                        LOG.info(
                            "No schema for XML type " + resType.getTypeName() + " / " + resType.getClass().getName());
                    }
                } catch (Exception e) {
                    LOG.error(
                        "Error in getAllowedContextMap, schema="
                            + schema
                            + ", type="
                            + resType.getTypeName()
                            + ", "
                            + e.getLocalizedMessage(),
                        e);
                }
            }
        }
        return result;
    }

    /**
     * Returns the configured content collector with the given name, or <code>null</code> if
     * no collector with this name is configured.<p>
     *
     * @param collectorName the name of the collector to get
     * @return the configured content collector with the given name
     */
    public I_CmsResourceCollector getContentCollector(String collectorName) {

        return m_collectorNameMappings.get(collectorName);
    }

    /**
     * Returns the default resource type for the given resource name, using the
     * configured resource type file extensions.<p>
     *
     * In case the given name does not map to a configured resource type,
     * {@link CmsResourceTypePlain} is returned.<p>
     *
     * This is only required (and should <i>not</i> be used otherwise) when
     * creating a new resource automatically during file upload or synchronization.
     * Only in this case, the file type for the new resource is determined using this method.
     * Otherwise the resource type is <i>always</i> stored as part of the resource,
     * and is <i>not</i> related to the file name.<p>
     *
     * @param resourcename the resource name to look up the resource type for
     *
     * @return the default resource type for the given resource name
     *
     * @throws CmsException if something goes wrong
     */
    public I_CmsResourceType getDefaultTypeForName(String resourcename) throws CmsException {

        String typeName = null;
        String suffix = null;
        if (CmsStringUtil.isNotEmpty(resourcename)) {
            int pos = resourcename.lastIndexOf('.');
            if (pos >= 0) {
                suffix = resourcename.substring(pos);
                if (CmsStringUtil.isNotEmpty(suffix)) {
                    suffix = suffix.toLowerCase();
                    typeName = m_configuration.m_extensionMappings.get(suffix);

                }
            }
        }

        if (typeName == null) {
            // use default type "plain"
            typeName = CmsResourceTypePlain.getStaticTypeName();
        }

        if (CmsLog.INIT.isDebugEnabled()) {
            CmsLog.INIT.debug(Messages.get().getBundle().key(Messages.INIT_GET_RESTYPE_2, typeName, suffix));
        }
        // look up and return the resource type
        return getResourceType(typeName);
    }

    /**
     * Returns the file extensions (suffixes) mappings to resource types.<p>
     *
     * @return a Map with all known file extensions as keys and their resource types as values.
     */
    public Map<String, String> getExtensionMapping() {

        return m_configuration.m_extensionMappings;
    }

    /**
     * Returns the file translator.<p>
     *
     * @return the file translator
     */
    public CmsResourceTranslator getFileTranslator() {

        return m_fileTranslator;
    }

    /**
     * Returns the folder translator.<p>
     *
     * @return the folder translator
     */
    public CmsResourceTranslator getFolderTranslator() {

        return m_folderTranslator;
    }

    /**
     * Returns the matching HTML converter class name for the specified option name.<p>
     *
     * @param name the name of the option that should trigger the HTML converter class
     *
     * @return the matching HTML converter class name for the specified option name or <code>null</code> if no match is found
     */
    public String getHtmlConverter(String name) {

        return m_htmlConverters.get(name);
    }

    /**
     * Returns an unmodifiable List of the configured {@link CmsHtmlConverterOption} objects.<p>
     *
     * @return an unmodifiable List of the configured {@link CmsHtmlConverterOption} objects
     */
    public List<CmsHtmlConverterOption> getHtmlConverters() {

        return m_configuredHtmlConverters;
    }

    /**
     * Returns the loader class instance for a given resource.<p>
     *
     * @param resource the resource
     * @return the appropriate loader class instance
     * @throws CmsLoaderException if something goes wrong
     */
    public I_CmsResourceLoader getLoader(CmsResource resource) throws CmsLoaderException {

        return getLoader(getResourceType(resource.getTypeId()).getLoaderId());
    }

    /**
     * Returns the loader class instance for the given loader id.<p>
     *
     * @param id the id of the loader to return
     * @return the loader class instance for the given loader id
     */
    public I_CmsResourceLoader getLoader(int id) {

        return m_loaders[id];
    }

    /**
     * Returns the (unmodifiable array) list with all initialized resource loaders.<p>
     *
     * @return the (unmodifiable array) list with all initialized resource loaders
     */
    public List<I_CmsResourceLoader> getLoaders() {

        return m_loaderList;
    }

    /**
     * Returns the MIME type for a specified file name.<p>
     *
     * If an encoding parameter that is not <code>null</code> is provided,
     * the returned MIME type is extended with a <code>; charset={encoding}</code> setting.<p>
     *
     * If no MIME type for the given filename can be determined, the
     * default <code>{@link #MIMETYPE_HTML}</code> is used.<p>
     *
     * @param filename the file name to check the MIME type for
     * @param encoding the default encoding (charset) in case of MIME types is of type "text"
     *
     * @return the MIME type for a specified file
     */
    public String getMimeType(String filename, String encoding) {

        return getMimeType(filename, encoding, MIMETYPE_HTML);
    }

    /**
     * Returns the MIME type for a specified file name.<p>
     *
     * If an encoding parameter that is not <code>null</code> is provided,
     * the returned MIME type is extended with a <code>; charset={encoding}</code> setting.<p>
     *
     * If no MIME type for the given filename can be determined, the
     * provided default is used.<p>
     *
     * @param filename the file name to check the MIME type for
     * @param encoding the default encoding (charset) in case of MIME types is of type "text"
     * @param defaultMimeType the default MIME type to use if no matching type for the filename is found
     *
     * @return the MIME type for a specified file
     */
    public String getMimeType(String filename, String encoding, String defaultMimeType) {

        String mimeType = null;
        int lastDot = filename.lastIndexOf('.');
        // check the MIME type for the file extension
        if ((lastDot > 0) && (lastDot < (filename.length() - 1))) {
            mimeType = m_mimeTypes.get(filename.substring(lastDot).toLowerCase(Locale.ENGLISH));
        }
        if (mimeType == null) {
            mimeType = defaultMimeType;
            if (mimeType == null) {
                // no default MIME type was provided
                return null;
            }
        }
        StringBuffer result = new StringBuffer(mimeType);
        if ((encoding != null)
            && (mimeType.startsWith("text") || mimeType.endsWith("javascript"))
            && (mimeType.indexOf("charset") == -1)) {
            result.append("; charset=");
            result.append(encoding);
        }
        return result.toString();
    }

    /**
     * Returns an unmodifiable List of the configured {@link CmsMimeType} objects.<p>
     *
     * @return an unmodifiable List of the configured {@link CmsMimeType} objects
     */
    public List<CmsMimeType> getMimeTypes() {

        return m_configuredMimeTypes;
    }

    /**
     * Returns the name generator for XML content file names.<p>
     *
     * @return the name generator for XML content file names.
     */
    public I_CmsFileNameGenerator getNameGenerator() {

        return m_nameGenerator;
    }

    /**
     * Returns an (unmodifiable) list of class names of all currently registered content collectors
     * ({@link I_CmsResourceCollector} objects).<p>
     *
     * @return an (unmodifiable) list of class names of all currently registered content collectors
     *      ({@link I_CmsResourceCollector} objects)
     */
    public List<I_CmsResourceCollector> getRegisteredContentCollectors() {

        return m_collectors;
    }

    /**
     * Returns an unmodifiable List of the configured {@link CmsRelationType} objects.<p>
     *
     * @return an unmodifiable List of the configured {@link CmsRelationType} objects
     */
    public List<CmsRelationType> getRelationTypes() {

        return m_configuredRelationTypes;
    }

    /**
     * Convenience method to get the initialized resource type instance for the given resource,
     * with a fall back to special "unknown" resource types in case the resource type is not configured.<p>
     *
     * @param resource the resource to get the type for
     *
     * @return the initialized resource type instance for the given resource
     */
    public I_CmsResourceType getResourceType(CmsResource resource) {

        I_CmsResourceType result = m_configuration.getResourceTypeById(resource.getTypeId());
        if (result == null) {
            // this resource type is unknown, return the default files instead
            if (resource.isFolder()) {
                // resource is a folder
                if (m_restypeUnknownFolder != null) {
                    result = m_restypeUnknownFolder;
                } else {
                    result = m_configuration.getResourceTypeByName(CmsResourceTypeFolder.getStaticTypeName());
                }
            } else {
                // resource is a file
                if (m_restypeUnknownFile != null) {
                    result = m_restypeUnknownFile;
                } else {
                    result = m_configuration.getResourceTypeByName(CmsResourceTypeBinary.getStaticTypeName());
                }
            }
        }
        return result;
    }

    /**
     * Returns the initialized resource type instance for the given id.<p>
     *
     * @param typeId the id of the resource type to get
     *
     * @return the initialized resource type instance for the given id
     *
     * @throws CmsLoaderException if no resource type is available for the given id
     */
    public I_CmsResourceType getResourceType(int typeId) throws CmsLoaderException {

        I_CmsResourceType result = m_configuration.getResourceTypeById(typeId);
        if (result == null) {
            throw new CmsLoaderException(
                Messages.get().container(Messages.ERR_UNKNOWN_RESTYPE_ID_REQ_1, Integer.valueOf(typeId)));
        }
        return result;
    }

    /**
     * Returns the initialized resource type instance for the given resource type name.<p>
     *
     * @param typeName the name of the resource type to get
     *
     * @return the initialized resource type instance for the given name
     *
     * @throws CmsLoaderException if no resource type is available for the given name
     */
    public I_CmsResourceType getResourceType(String typeName) throws CmsLoaderException {

        I_CmsResourceType result = m_configuration.getResourceTypeByName(typeName);
        if (result != null) {
            return result;
        }
        throw new CmsLoaderException(Messages.get().container(Messages.ERR_UNKNOWN_RESTYPE_NAME_REQ_1, typeName));
    }

    /**
     * Returns the (unmodifiable) list with all initialized resource types.<p>
     *
     * @return the (unmodifiable) list with all initialized resource types
     */
    public List<I_CmsResourceType> getResourceTypes() {

        return m_configuration.m_resourceTypeList;
    }

    /**
     * Returns the (unmodifiable) list with all initialized resource types including unknown types.<p>
     *
     * @return the (unmodifiable) list with all initialized resource types including unknown types
     */
    public List<I_CmsResourceType> getResourceTypesWithUnknown() {

        return m_configuration.m_resourceTypeListWithUnknown;
    }

    /**
     * The configured default type for files when the resource type is missing.<p>
     *
     * @return the configured default type for files
     */
    public I_CmsResourceType getResTypeUnknownFile() {

        return m_restypeUnknownFile;
    }

    /**
     * The configured default type for folders when the resource type is missing.<p>
     *
     * @return The configured default type for folders
     */
    public I_CmsResourceType getResTypeUnknownFolder() {

        return m_restypeUnknownFolder;
    }

    /**
     * Returns a template loader facade for the given file.<p>
     * @param cms the current OpenCms user context
     * @param resource the requested file
     * @param templateProperty the property to read for the template
     *
     * @return a resource loader facade for the given file
     * @throws CmsException if something goes wrong
     */
    public CmsTemplateLoaderFacade getTemplateLoaderFacade(CmsObject cms, CmsResource resource, String templateProperty)
    throws CmsException {

        return getTemplateLoaderFacade(cms, null, resource, templateProperty);
    }

    /**
     * Returns a template loader facade for the given file.<p>
     * @param cms the current OpenCms user context
     * @param request the current request
     * @param resource the requested file
     * @param templateProperty the property to read for the template
     *
     * @return a resource loader facade for the given file
     * @throws CmsException if something goes wrong
     */
    public CmsTemplateLoaderFacade getTemplateLoaderFacade(
        CmsObject cms,
        HttpServletRequest request,
        CmsResource resource,
        String templateProperty)
    throws CmsException {

        String templateProp = cms.readPropertyObject(resource, templateProperty, true).getValue();
        CmsTemplateContext templateContext = null;
        String templateName = null;
        if (templateProp == null) {

            // use default template, if template is not set
            templateProp = DEFAULT_TEMPLATE;
            NamedTemplate namedTemplate = readTemplateWithName(cms, templateProp);
            if (namedTemplate == null) {
                // no template property defined, this is a must for facade loaders
                throw new CmsLoaderException(
                    Messages.get().container(Messages.ERR_NONDEF_PROP_2, templateProperty, cms.getSitePath(resource)));
            }
            templateName = namedTemplate.getName();
        } else {
            if (CmsTemplateContextManager.hasPropertyPrefix(templateProp)) {
                templateContext = OpenCms.getTemplateContextManager().getTemplateContext(
                    templateProp,
                    cms,
                    request,
                    resource);
                if (templateContext != null) {
                    templateProp = templateContext.getTemplatePath();
                }
            }
            NamedTemplate namedTemplate = readTemplateWithName(cms, templateProp);
            if (namedTemplate == null) {
                namedTemplate = readTemplateWithName(cms, DEFAULT_TEMPLATE);
                if (namedTemplate != null) {
                    templateProp = DEFAULT_TEMPLATE;
                    templateName = namedTemplate.getName();
                }
            } else {
                templateName = namedTemplate.getName();
            }
        }
        CmsResource template = cms.readFile(templateProp, CmsResourceFilter.IGNORE_EXPIRATION);
        CmsTemplateLoaderFacade result = new CmsTemplateLoaderFacade(getLoader(template), resource, template);
        result.setTemplateContext(templateContext);
        result.setTemplateName(templateName);
        return result;

    }

    /**
     * Returns the XSD translator.<p>
     *
     * @return the XSD translator
     */
    public CmsResourceTranslator getXsdTranslator() {

        return m_xsdTranslator;
    }

    /**
     * Checks if an initialized resource type instance equal to the given resource type is available.<p>
     *
     * @param type the resource type to check
     * @return <code>true</code> if such a resource type has been configured, <code>false</code> otherwise
     *
     * @see #getResourceType(String)
     * @see #getResourceType(int)
     */
    public boolean hasResourceType(I_CmsResourceType type) {

        return hasResourceType(type.getTypeName());
    }

    /**
     * Checks if an initialized resource type instance for the given resource type is is available.<p>
     *
     * @param typeId the id of the resource type to check
     * @return <code>true</code> if such a resource type has been configured, <code>false</code> otherwise
     *
     * @see #getResourceType(int)
     *
     * @deprecated
     * Use {@link #hasResourceType(I_CmsResourceType)} or {@link #hasResourceType(I_CmsResourceType)} instead.
     * Resource types should always be referenced either by its type class (preferred) or by type name.
     * Use of int based resource type references will be discontinued in a future OpenCms release.
     */
    @Deprecated
    public boolean hasResourceType(int typeId) {

        return m_configuration.getResourceTypeById(typeId) != null;
    }

    /**
     * Checks if an initialized resource type instance for the given resource type name is available.<p>
     *
     * @param typeName the name of the resource type to check
     * @return <code>true</code> if such a resource type has been configured, <code>false</code> otherwise
     *
     * @see #getResourceType(String)
     */
    public boolean hasResourceType(String typeName) {

        return m_configuration.getResourceTypeByName(typeName) != null;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     *
     * @throws CmsConfigurationException in case of duplicate resource types in the configuration
     */
    public void initConfiguration() throws CmsConfigurationException {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LOADER_CONFIG_FINISHED_0));
        }

        m_resourceTypesFromXml = Collections.unmodifiableList(m_resourceTypesFromXml);
        m_loaderList = Collections.unmodifiableList(m_loaderList);
        Collections.sort(m_configuredMimeTypes);
        m_configuredMimeTypes = Collections.unmodifiableList(m_configuredMimeTypes);
        m_configuredRelationTypes = Collections.unmodifiableList(m_configuredRelationTypes);

        // initialize the HTML converters
        initHtmlConverters();
        m_configuredHtmlConverters = Collections.unmodifiableList(m_configuredHtmlConverters);

        // initialize the resource types
        initResourceTypes();
        // initialize the MIME types
        initMimeTypes();
    }

    /**
     * Initializes all additional resource types stored in the modules.<p>
     *
     * @param cms an initialized OpenCms user context with "module manager" role permissions
     *
     * @throws CmsRoleViolationException in case the provided OpenCms user context did not have "module manager" role permissions
     * @throws CmsConfigurationException in case of duplicate resource types in the configuration
     */
    public synchronized void initialize(CmsObject cms) throws CmsRoleViolationException, CmsConfigurationException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            // some simple test cases don't require this check
            OpenCms.getRoleManager().checkRole(cms, CmsRole.DATABASE_MANAGER);
        }

        // initialize the resource types
        initResourceTypes();

        // call initialize method on all resource types
        Iterator<I_CmsResourceType> i = m_configuration.m_resourceTypeList.iterator();
        while (i.hasNext()) {
            I_CmsResourceType type = i.next();
            type.initialize(cms);
        }

        // This only sets the CmsObject the first time it's called
        m_nameGenerator.setAdminCms(cms);

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_LOADER_CONFIG_FINISHED_0));
        }
    }

    /**
     * Loads the requested resource and writes the contents to the response stream.<p>
     *
     * @param req the current HTTP request
     * @param res the current HTTP response
     * @param cms the current OpenCms user context
     * @param resource the requested resource
     * @throws ServletException if something goes wrong
     * @throws IOException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    public void loadResource(CmsObject cms, CmsResource resource, HttpServletRequest req, HttpServletResponse res)
    throws ServletException, IOException, CmsException {

        res.setContentType(getMimeType(resource.getName(), cms.getRequestContext().getEncoding()));
        I_CmsResourceLoader loader = getLoader(resource);
        loader.load(cms, resource, req, res);
    }

    /**
     * Checks if there is a resource type with a given name whose id matches the given id.<p>
     *
     * This will return 'false' if no resource type with the given name is registered.<p>
     *
     * @param name a resource type name
     * @param id a resource type id
     *
     * @return true if a matching resource type with the given name and id was found
     */
    public boolean matchResourceType(String name, int id) {

        if (hasResourceType(name)) {
            try {
                return getResourceType(name).getTypeId() == id;
            } catch (Exception e) {
                // should never happen because we already checked with hasResourceType, still have to
                // catch it so the compiler is happy
                LOG.error(e.getLocalizedMessage(), e);
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Configures the URL name generator for XML contents.<p>
     *
     * @param nameGenerator the configured name generator class
     *
     * @throws CmsConfigurationException if something goes wrong
     */
    public void setNameGenerator(I_CmsFileNameGenerator nameGenerator) throws CmsConfigurationException {

        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }
        m_nameGenerator = nameGenerator;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_SET_NAME_GENERATOR_1, nameGenerator.getClass().getName()));
        }
    }

    /**
     * Sets the folder, the file and the XSD translator.<p>
     *
     * @param folderTranslator the folder translator to set
     * @param fileTranslator the file translator to set
     * @param xsdTranslator the XSD translator to set
     */
    public void setTranslators(
        CmsResourceTranslator folderTranslator,
        CmsResourceTranslator fileTranslator,
        CmsResourceTranslator xsdTranslator) {

        m_folderTranslator = folderTranslator;
        m_fileTranslator = fileTranslator;
        m_xsdTranslator = xsdTranslator;
    }

    /**
     * Shuts down this resource manage instance.<p>
     *
     * @throws Exception in case of errors during shutdown
     */
    public synchronized void shutDown() throws Exception {

        Iterator<I_CmsResourceLoader> it = m_loaderList.iterator();
        while (it.hasNext()) {
            // destroy all resource loaders
            I_CmsResourceLoader loader = it.next();
            loader.destroy();
        }

        m_loaderList = null;
        m_loaders = null;
        m_collectorNameMappings = null;
        m_mimeTypes = null;
        m_configuredMimeTypes = null;
        m_configuredRelationTypes = null;
        m_configuredHtmlConverters = null;
        m_htmlConverters = null;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_SHUTDOWN_1, this.getClass().getName()));
        }
    }

    /**
     * Gets the template name for a template resource, using a cache for efficiency.<p>
     *
     * @param cms the current CMS context
     * @param resource the template resource
     * @return the template name
     *
     * @throws CmsException if something goes wrong
     */
    private String getTemplateName(CmsObject cms, CmsResource resource) throws CmsException {

        String templateName = (String)(m_templateNameCache.getCachedObject(cms, resource.getRootPath()));
        if (templateName == null) {
            CmsProperty nameProperty = cms.readPropertyObject(
                resource,
                CmsPropertyDefinition.PROPERTY_TEMPLATE_ELEMENTS,
                false);
            String nameFromProperty = "";
            if (!nameProperty.isNullProperty()) {
                nameFromProperty = nameProperty.getValue();
            }
            m_templateNameCache.putCachedObject(cms, resource.getRootPath(), nameFromProperty);
            return nameFromProperty;
        } else {
            return templateName;
        }
    }

    /**
     * Initialize the HTML converters.<p>
     *
     * HTML converters are configured in the OpenCms <code>opencms-vfs.xml</code> configuration file.<p>
     *
     * For legacy reasons, the default JTidy HTML converter has to be loaded if no explicit HTML converters
     * are configured in the configuration file.<p>
     */
    private void initHtmlConverters() {

        // check if any HTML converter configuration were found
        if (m_configuredHtmlConverters.size() == 0) {
            // no converters configured, add default JTidy converter configuration
            String classJTidy = CmsHtmlConverterJTidy.class.getName();
            m_configuredHtmlConverters.add(
                new CmsHtmlConverterOption(CmsHtmlConverter.PARAM_ENABLED, classJTidy, true));
            m_configuredHtmlConverters.add(new CmsHtmlConverterOption(CmsHtmlConverter.PARAM_XHTML, classJTidy, true));
            m_configuredHtmlConverters.add(new CmsHtmlConverterOption(CmsHtmlConverter.PARAM_WORD, classJTidy, true));
            m_configuredHtmlConverters.add(
                new CmsHtmlConverterOption(CmsHtmlConverter.PARAM_REPLACE_PARAGRAPHS, classJTidy, true));
        }

        // initialize lookup map of configured HTML converters
        m_htmlConverters = new HashMap<String, String>(m_configuredHtmlConverters.size());
        for (Iterator<CmsHtmlConverterOption> i = m_configuredHtmlConverters.iterator(); i.hasNext();) {
            CmsHtmlConverterOption converterOption = i.next();
            m_htmlConverters.put(converterOption.getName(), converterOption.getClassName());
        }
    }

    /**
     * Initialize the MIME types.<p>
     *
     * MIME types are configured in the OpenCms <code>opencms-vfs.xml</code> configuration file.<p>
     *
     * For legacy reasons, the MIME types are also read from a file <code>"mimetypes.properties"</code>
     * that must be located in the default <code>"classes"</code> folder of the web application.<p>
     */
    private void initMimeTypes() {

        // legacy MIME type initialization: try to read properties file
        Properties mimeTypes = new Properties();
        try {
            // first try: read MIME types from default package
            mimeTypes.load(getClass().getClassLoader().getResourceAsStream("mimetypes.properties"));
        } catch (Throwable t) {
            try {
                // second try: read MIME types from loader package (legacy reasons, there are no types by default)
                mimeTypes.load(
                    getClass().getClassLoader().getResourceAsStream("org/opencms/loader/mimetypes.properties"));
            } catch (Throwable t2) {
                if (LOG.isInfoEnabled()) {
                    LOG.info(
                        Messages.get().getBundle().key(
                            Messages.LOG_READ_MIMETYPES_FAILED_2,
                            "mimetypes.properties",
                            "org/opencms/loader/mimetypes.properties"));
                }
            }
        }

        // initialize the Map with all available MIME types
        List<CmsMimeType> combinedMimeTypes = new ArrayList<CmsMimeType>(
            mimeTypes.size() + m_configuredMimeTypes.size());
        // first add all MIME types from the configuration
        combinedMimeTypes.addAll(m_configuredMimeTypes);
        // now add the MIME types from the properties
        Iterator<Map.Entry<Object, Object>> i = mimeTypes.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry<Object, Object> entry = i.next();
            CmsMimeType mimeType = new CmsMimeType(entry.getKey().toString(), entry.getValue().toString(), false);
            if (!combinedMimeTypes.contains(mimeType)) {
                // make sure no MIME types from the XML configuration are overwritten
                combinedMimeTypes.add(mimeType);
            }
        }

        // create a lookup Map for the MIME types
        m_mimeTypes = new HashMap<String, String>(mimeTypes.size());
        Iterator<CmsMimeType> j = combinedMimeTypes.iterator();
        while (j.hasNext()) {
            CmsMimeType mimeType = j.next();
            m_mimeTypes.put(mimeType.getExtension(), mimeType.getType());
        }

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(Messages.INIT_NUM_MIMETYPES_1, Integer.valueOf(m_mimeTypes.size())));
        }
    }

    /**
     * Adds a new resource type to the internal list of loaded resource types and initializes
     * options for the resource type.<p>
     *
     * @param resourceType the resource type to add
     * @param configuration the resource configuration
     */
    private synchronized void initResourceType(
        I_CmsResourceType resourceType,
        CmsResourceManagerConfiguration configuration) {

        // add the loader to the internal list of loaders
        configuration.addResourceType(resourceType);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_ADD_RESTYPE_3,
                    resourceType.getTypeName(),
                    Integer.valueOf(resourceType.getTypeId()),
                    resourceType.getClass().getName()));
        }

        // add the mappings
        List<String> mappings = resourceType.getConfiguredMappings();
        Iterator<String> i = mappings.iterator();
        while (i.hasNext()) {
            String mapping = i.next();
            // only add this mapping if a mapping with this file extension does not
            // exist already
            if (!configuration.m_extensionMappings.containsKey(mapping)) {
                configuration.m_extensionMappings.put(mapping, resourceType.getTypeName());
                if (CmsLog.INIT.isInfoEnabled()) {
                    CmsLog.INIT.info(
                        Messages.get().getBundle().key(
                            Messages.INIT_MAP_RESTYPE_2,
                            mapping,
                            resourceType.getTypeName()));
                }
            }
        }
    }

    /**
     * Initializes member variables required for storing the resource types.<p>
     *
     * @throws CmsConfigurationException in case of duplicate resource types in the configuration
     */
    private synchronized void initResourceTypes() throws CmsConfigurationException {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_STARTING_LOADER_CONFIG_0));
        }

        CmsResourceManagerConfiguration newConfiguration = new CmsResourceManagerConfiguration();

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().getBundle().key(
                    Messages.INIT_ADD_RESTYPE_FROM_FILE_2,
                    Integer.valueOf(m_resourceTypesFromXml.size()),
                    CmsVfsConfiguration.DEFAULT_XML_FILE_NAME));
        }

        // build a new resource type list from the resource types of the XML configuration
        Iterator<I_CmsResourceType> i;
        i = m_resourceTypesFromXml.iterator();
        while (i.hasNext()) {
            I_CmsResourceType resourceType = i.next();
            initResourceType(resourceType, newConfiguration);
        }

        // add all resource types declared in the modules
        CmsModuleManager moduleManager = OpenCms.getModuleManager();
        if (moduleManager != null) {
            Iterator<String> modules = moduleManager.getModuleNames().iterator();
            while (modules.hasNext()) {
                CmsModule module = moduleManager.getModule(modules.next());
                if ((module != null) && (module.getResourceTypes().size() > 0)) {
                    // module contains resource types
                    if (CmsLog.INIT.isInfoEnabled()) {
                        CmsLog.INIT.info(
                            Messages.get().getBundle().key(
                                Messages.INIT_ADD_NUM_RESTYPES_FROM_MOD_2,
                                Integer.valueOf(module.getResourceTypes().size()),
                                module.getName()));
                    }

                    Iterator<I_CmsResourceType> j = module.getResourceTypes().iterator();
                    while (j.hasNext()) {
                        I_CmsResourceType resourceType = j.next();
                        I_CmsResourceType conflictingType = null;
                        if (resourceType.getTypeId() == CmsResourceTypeUnknownFile.RESOURCE_TYPE_ID) {
                            // default unknown file resource type
                            if (m_restypeUnknownFile != null) {
                                // error: already set
                                conflictingType = m_restypeUnknownFile;
                            } else {
                                m_restypeUnknownFile = resourceType;
                                continue;
                            }
                        } else if (resourceType.getTypeId() == CmsResourceTypeUnknownFolder.RESOURCE_TYPE_ID) {
                            // default unknown folder resource type
                            if (m_restypeUnknownFolder != null) {
                                // error: already set
                                conflictingType = m_restypeUnknownFolder;
                            } else {
                                m_restypeUnknownFile = resourceType;
                                continue;
                            }
                        } else {
                            // normal resource types
                            conflictingType = newConfiguration.getResourceTypeById(resourceType.getTypeId());
                        }
                        if (conflictingType != null) {
                            throw new CmsConfigurationException(
                                Messages.get().container(
                                    Messages.ERR_CONFLICTING_MODULE_RESOURCE_TYPES_5,
                                    new Object[] {
                                        resourceType.getTypeName(),
                                        Integer.valueOf(resourceType.getTypeId()),
                                        module.getName(),
                                        conflictingType.getTypeName(),
                                        Integer.valueOf(conflictingType.getTypeId())}));
                        }
                        initResourceType(resourceType, newConfiguration);
                    }
                }
            }
        }

        // freeze the current configuration
        newConfiguration.freeze(m_restypeUnknownFile, m_restypeUnknownFile);
        m_configuration = newConfiguration;
        m_frozen = true;

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_RESOURCE_TYPE_INITIALIZED_0));
        }
    }

    /**
     * Reads a template resource together with its name.<p>
     *
     * @param cms the current CMS context
     * @param path the template path
     *
     * @return the template together with its name, or null if the template couldn't be read
     */
    private NamedTemplate readTemplateWithName(CmsObject cms, String path) {

        try {
            CmsResource resource = cms.readResource(path, CmsResourceFilter.IGNORE_EXPIRATION);
            String name = getTemplateName(cms, resource);
            return new NamedTemplate(resource, name);
        } catch (Exception e) {
            return null;
        }
    }

}