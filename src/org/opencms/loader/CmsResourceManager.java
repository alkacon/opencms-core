/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsResourceManager.java,v $
 * Date   : $Date: 2005/05/03 15:44:14 $
 * Version: $Revision: 1.23 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.loader;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.configuration.CmsVfsConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.module.CmsModule;
import org.opencms.module.CmsModuleManager;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.util.CmsStringUtil;

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
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Collects all available resource loaders, resource types and resource collectors at startup and provides
 * methods to access them during OpenCms runtime.<p> 
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.23 $
 * @since 5.1
 */
public class CmsResourceManager {

    /**
     * Contains the resource manager data that can be initialized during runtime by a module.<p>
     */
    final class CmsResourceManagerConfiguration {

        /** Array with all configured resource types. */
        protected I_CmsResourceType[] m_resourceTypes;

        /** The mappings of file extensions to resource types. */
        private Map m_mappings;

        /** A list that contains all initialized resource types. */
        private List m_resourceTypeList;

        /** A map that contains all initialized resource types mapped to their type name. */
        private Map m_resourceTypeMap;

        /**
         * Creates a new resource manager data storage.<p>
         */
        protected CmsResourceManagerConfiguration() {

            m_resourceTypes = new I_CmsResourceType[100];
            m_resourceTypeMap = new HashMap();
            m_mappings = new HashMap();
            m_resourceTypeList = new ArrayList();
        }

        /**
         * Freezes the current configuration by making all configured data sructures unmodifiable.<p>         *
         */
        protected void freeze() {

            // freeze the current configuration
            m_resourceTypeList = Collections.unmodifiableList(m_resourceTypeList);
            m_resourceTypeMap = Collections.unmodifiableMap(m_resourceTypeMap);
            m_mappings = Collections.unmodifiableMap(m_mappings);
        }

        /**
         * Returns the mappings of file extensions to resource types.<p>
         *
         * @return the mappings of file extensions to resource types
         */
        protected Map getMappings() {

            return m_mappings;
        }

        /**
         * Returns the list that contains all initialized resource types.<p>
         *
         * @return the list that contains all initialized resource types
         */
        protected List getResourceTypeList() {

            return m_resourceTypeList;
        }

        /**
         * Returns the map that contains all initialized resource types mapped to their type name.<p>
         *
         * @return the map that contains all initialized resource types mapped to their type name
         */
        protected Map getResourceTypeMap() {

            return m_resourceTypeMap;
        }
    }

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsResourceManager.class);  
    
    /** The default mimetype. */
    private static final String C_DEFAULT_MIMETYPE = "text/html";

    /** The map for all configured collector names, mapped to their collector class. */
    private Map m_collectorNameMappings;

    /** The list of all currently configured content collector instances. */
    private List m_collectors;

    /** The current resource manager configuration. */
    private CmsResourceManagerConfiguration m_configuration;

    /** Filename translator, used only for the creation of new files. */
    private CmsResourceTranslator m_fileTranslator;

    /** Folder translator, used to translate all accesses to resources. */
    private CmsResourceTranslator m_folderTranslator;

    /** Indicates if the configuration is finalized (frozen). */
    private boolean m_frozen;

    /** Contains all loader extensions to the include process. */
    private List m_includeExtensions;

    /** A list that contains all initialized resource loaders. */
    private List m_loaderList;

    /** All initialized resource loaders, mapped to their id. */
    private I_CmsResourceLoader[] m_loaders;

    /** The OpenCms map of configured mime types. */
    private Map m_mimeTypes;

    /** A list that contains all resource types added from the XML configuration. */
    private List m_resourceTypesFromXml;

    /**
     * Creates a new instance for the resource manager, 
     * will be called by the vfs configuration manager.<p>
     */
    public CmsResourceManager() {

        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.INIT_STARTING_LOADER_CONFIG_0));
        }

        m_resourceTypesFromXml = new ArrayList();
        m_loaders = new I_CmsResourceLoader[16];
        m_loaderList = new ArrayList();
        m_includeExtensions = new ArrayList();

        Properties mimeTypes = new Properties();
        try {
            // first try: read mime types from default package
            mimeTypes.load(getClass().getClassLoader().getResourceAsStream("mimetypes.properties"));
        } catch (Throwable t) {
            try {
                // second try: read mime types from loader package
                mimeTypes.load(getClass().getClassLoader().getResourceAsStream(
                    "org/opencms/loader/mimetypes.properties"));
            } catch (Throwable t2) {
                    LOG.error(Messages.get().container(Messages.LOG_READ_MIMETYPES_FAILED_0), t);
            }
        }
        // initalize the Map with all available mimetypes
        m_mimeTypes = new HashMap(mimeTypes.size());
        Iterator i = mimeTypes.keySet().iterator();
        while (i.hasNext()) {
            // ensure all mime type entries are lower case
            String key = (String)i.next();
            String value = (String)mimeTypes.get(key);
            value = value.toLowerCase(Locale.ENGLISH);
            m_mimeTypes.put(key, value);
        }
        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.INIT_NUM_MIMETYPES_1, new Integer(m_mimeTypes.size())));
        }
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

        Class classClazz;
        // init class for content collector
        try {
            classClazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOG.error(Messages.get().key(Messages.LOG_CONTENT_COLLECTOR_CLASS_NOT_FOUND_1, className), e);
            return null;
        }

        I_CmsResourceCollector collector;
        try {
            collector = (I_CmsResourceCollector)classClazz.newInstance();
        } catch (InstantiationException e) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_INVALID_COLLECTOR_NAME_1, className));
        } catch (IllegalAccessException e) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_INVALID_COLLECTOR_NAME_1, className));
        } catch (ClassCastException e) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_INVALID_COLLECTOR_NAME_1, className));
        }

        // set the configured order for the collector
        int ord = 0;
        try {
            ord = Integer.valueOf(order).intValue();
        } catch (NumberFormatException e) {
            LOG.error(Messages.get().key(Messages.LOG_COLLECTOR_BAD_ORDER_NUMBER_1, className), e);
        }
        collector.setOrder(ord);

        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(
                Messages.INIT_ADD_COLLECTOR_CLASS_2, className, order));
        }

        // extend or init the current list of configured collectors
        if (m_collectors != null) {
            m_collectors = new ArrayList(m_collectors);
            m_collectorNameMappings = new HashMap(m_collectorNameMappings);
        } else {
            m_collectors = new ArrayList();
            m_collectorNameMappings = new HashMap();
        }

        if (!m_collectors.contains(collector)) {
            // this is a collector not currently configured
            m_collectors.add(collector);

            Iterator i = collector.getCollectorNames().iterator();
            while (i.hasNext()) {
                String name = (String)i.next();
                if (m_collectorNameMappings.containsKey(name)) {
                    // this name is already configured, check the order of the collector
                    I_CmsResourceCollector otherCollector = (I_CmsResourceCollector)m_collectorNameMappings.get(name);
                    if (collector.getOrder() > otherCollector.getOrder()) {
                        // new collector has a greater order than the old collector in the Map
                        m_collectorNameMappings.put(name, collector);
                        if (CmsLog.LOG.isInfoEnabled()) {
                            CmsLog.LOG.info(Messages.get().key(
                                Messages.INIT_COLLECTOR_REPLACED_1, name));
                        }
                    } else {
                        if (CmsLog.LOG.isInfoEnabled()) {
                            CmsLog.LOG.info(Messages.get().key(Messages.INIT_DUPLICATE_COLLECTOR_SKIPPED_1, name));
                        }
                    }
                } else {
                    m_collectorNameMappings.put(name, collector);
                    if (CmsLog.LOG.isInfoEnabled()) {
                        CmsLog.LOG.info(Messages.get().key(Messages.INIT_ADD_COLLECTOR_1, name));
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
     * Adds a new loader to the internal list of loaded loaders.<p>
     *
     * @param loader the loader to add
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public synchronized void addLoader(I_CmsResourceLoader loader) throws CmsConfigurationException {

        // check if new loaders can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }

        // add the loader to the internal list of loaders
        int pos = loader.getLoaderId();
        if (pos > m_loaders.length) {
            I_CmsResourceLoader[] buffer = new I_CmsResourceLoader[pos * 2];
            System.arraycopy(m_loaders, 0, buffer, 0, m_loaders.length);
            m_loaders = buffer;
        }
        m_loaders[pos] = loader;
        if (loader instanceof I_CmsLoaderIncludeExtension) {
            // this loader requires special processing during the include process
            m_includeExtensions.add(loader);
        }
        m_loaderList.add(loader);
        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(
                Messages.INIT_ADD_LOADER_2, loader.getClass().getName(), new Integer(pos)));
        }
    }

    /**
     * Adds a new resource type from the XML configuration to the internal list of loaded resource types.<p>
     * 
     * Resource types can also be added from a module.<p>
     *
     * @param resourceType the resource type to add
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public synchronized void addResourceType(I_CmsResourceType resourceType) throws CmsConfigurationException {

        // check if new resource types can still be added
        if (m_frozen) {
            throw new CmsConfigurationException(Messages.get().container(Messages.ERR_NO_CONFIG_AFTER_STARTUP_0));
        }

        m_resourceTypesFromXml.add(resourceType);
    }

    /**
     * Returns the configured content collector with the given name, or <code>null</code> if 
     * no collector with this name is configured.<p>
     *  
     * @param collectorName the name of the collector to get
     * @return the configured content collector with the given name
     */
    public I_CmsResourceCollector getContentCollector(String collectorName) {

        return (I_CmsResourceCollector)m_collectorNameMappings.get(collectorName);
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
                    typeName = (String)m_configuration.getMappings().get(suffix);

                }
            }
        }

        if (typeName == null) {
            // use default type "plain"
            typeName = CmsResourceTypePlain.getStaticTypeName();
        }

        if (CmsLog.LOG.isDebugEnabled()) {
            CmsLog.LOG.debug(Messages.get().key(Messages.INIT_GET_RESTYPE_2, typeName, suffix));
        }
        // look up and return the resource type
        return getResourceType(typeName);
    }

    /**
     * Returns the file extensions (suffixes) mappings to resource types.<p>
     *
     * @return a Map with all known file extensions as keys and their resource types as values.
     */
    public Map getExtensionMapping() {

        return m_configuration.getMappings();
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
     * Returns the (unmodifyable array) list with all initialized resource loaders.<p>
     * 
     * @return the (unmodifyable array) list with all initialized resource loaders
     */
    public List getLoaders() {

        return m_loaderList;
    }

    /**
     * Returns the mime type for a specified file name.<p>
     * 
     * If an encoding parameter that is not <code>null</code> is provided,
     * the returned mime type is extended with a <code>; charset={encoding}</code> setting.<p> 
     * 
     * @param filename the file name to check the mime type for
     * @param encoding the default encoding (charset) in case of mime types is of type "text"
     * @return the mime type for a specified file
     */
    public String getMimeType(String filename, String encoding) {

        String mimetype = null;
        int lastDot = filename.lastIndexOf('.');
        // check the mime type for the file extension 
        if ((lastDot > 0) && (lastDot < (filename.length() - 1))) {
            mimetype = (String)m_mimeTypes.get(filename.substring(lastDot + 1));
        }
        if (mimetype == null) {
            mimetype = C_DEFAULT_MIMETYPE;
        }
        StringBuffer result = new StringBuffer(mimetype);
        if ((encoding != null) && mimetype.startsWith("text") && (mimetype.indexOf("charset") == -1)) {
            result.append("; charset=");
            result.append(encoding);
        }
        return result.toString();
    }

    /**
     * Returns an (unmodifiable) list of class names of all currently registered content collectors.<p>
     *   
     * @return an (unmodifiable) list of class names of all currently registered content collectors
     */
    public List getRegisteredContentCollectors() {

        return m_collectors;
    }

    /**
     * Returns the initialized resource type instance for the given id.<p>
     * 
     * @param typeId the id of the resource type to get
     * @return the initialized resource type instance for the given id
     * @throws CmsLoaderException if no resource type is available for the given id
     */
    public I_CmsResourceType getResourceType(int typeId) throws CmsLoaderException {

        I_CmsResourceType result = null;
        if (typeId < m_configuration.m_resourceTypes.length) {
            result = m_configuration.m_resourceTypes[typeId];
        }
        if (result == null) {
            throw new CmsLoaderException(Messages.get().container(
                Messages.ERR_UNKNOWN_RESTYPE_ID_REQ_1, new Integer(typeId)));            
        }
        return result;
    }

    /**
     * Returns the initialized resource type instance for the given resource type name.<p>
     * 
     * @param typeName the name of the resource type to get
     * @return the initialized resource type instance for the given name
     * @throws CmsLoaderException if no resource type is available for the given name
     */
    public I_CmsResourceType getResourceType(String typeName) throws CmsLoaderException {

        I_CmsResourceType result = (I_CmsResourceType)m_configuration.getResourceTypeMap().get(typeName);
        if (result != null) {
            return result;
        }
        throw new CmsLoaderException(Messages.get().container(
            Messages.ERR_UNKNOWN_RESTYPE_NAME_REQ_1, typeName));          
    }

    /**
     * Returns the (unmodifyable array) list with all initialized resource types.<p>
     * 
     * @return the (unmodifyable array) list with all initialized resource types
     */
    public List getResourceTypes() {

        // return the list of resource types
        return m_configuration.getResourceTypeList();
    }

    /**
     * Returns a template loader facade for the given file.<p>
     * @param cms the current cms context
     * @param resource the requested file
     * @param templateProperty the property to read for the template
     * 
     * @return a resource loader facade for the given file
     * @throws CmsException if something goes wrong
     */
    public CmsTemplateLoaderFacade getTemplateLoaderFacade(CmsObject cms, CmsResource resource, String templateProperty)
    throws CmsException {

        String absolutePath = cms.getSitePath(resource);

        String templateProp = cms.readPropertyObject(absolutePath, templateProperty, true).getValue();

        if (templateProp == null) {
            // no template property defined, this is a must for facade loaders
            throw new CmsLoaderException(Messages.get().container(Messages.ERR_NONDEF_PROP_2, templateProperty, absolutePath));
        }

        CmsResource template = cms.readFile(templateProp, CmsResourceFilter.IGNORE_EXPIRATION);
        return new CmsTemplateLoaderFacade(getLoader(template), resource, template);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.INIT_LOADER_CONFIG_FINISHED_0));
        }

        m_resourceTypesFromXml = Collections.unmodifiableList(m_resourceTypesFromXml);
        m_loaderList = Collections.unmodifiableList(m_loaderList);

        // initalize the resource types
        initResourceTypes();
    }

    /**
     * Initializes all additional resource types stored in the modules.<p>
     * 
     * @param cms an initialized OpenCms user context with "module manager" role permissions
     * 
     * @throws CmsRoleViolationException in case the provided OpenCms user context did not have "module manager" role permissions
     */
    public synchronized void initialize(CmsObject cms) throws CmsRoleViolationException {

        if (OpenCms.getRunLevel() > OpenCms.RUNLEVEL_1_CORE_OBJECT) {
            // some simple test cases don't require this check       
            cms.checkRole(CmsRole.RESOURCE_TYPE_MANAGER);
        }

        // initalize the resource types
        initResourceTypes();

        // call initialize method on all resource types
        Iterator i = m_configuration.getResourceTypeList().iterator();
        while (i.hasNext()) {
            I_CmsResourceType type = (I_CmsResourceType)i.next();
            type.initialize(cms);
        }

        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.INIT_LOADER_CONFIG_FINISHED_0));
        }
    }

    /**    
     * Loads the requested resource and writes the contents to the response stream.<p>
     * 
     * @param req the current http request
     * @param res the current http response
     * @param cms the curren cms context
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
     * Extension method for handling special, loader depended actions during the include process.<p>
     * 
     * Note: If you have multiple loaders configured that require include extensions, 
     * all loaders are called in the order they are configured in.<p> 
     * 
     * @param target the target for the include, might be <code>null</code>
     * @param element the element to select form the target might be <code>null</code>
     * @param editable the flag to indicate if the target is editable
     * @param paramMap a map of parameters for the include, can be modified, might be <code>null</code>
     * @param req the current request
     * @param res the current response
     * @throws CmsException in case something goes wrong
     * @return the modified target URI
     */
    public String resolveIncludeExtensions(
        String target,
        String element,
        boolean editable,
        Map paramMap,
        ServletRequest req,
        ServletResponse res) throws CmsException {

        if (m_includeExtensions == null) {
            return target;
        }
        String result = target;
        for (int i = 0; i < m_includeExtensions.size(); i++) {
            // offer the element to every include extension
            I_CmsLoaderIncludeExtension loader = (I_CmsLoaderIncludeExtension)m_includeExtensions.get(i);
            result = loader.includeExtension(target, element, editable, paramMap, req, res);
        }
        return result;
    }

    /**
     * Sets the folder and the file translator.<p>
     * 
     * @param folderTranslator the folder translator to set
     * @param fileTranslator the file translator to set
     */
    public void setTranslators(CmsResourceTranslator folderTranslator, CmsResourceTranslator fileTranslator) {

        m_folderTranslator = folderTranslator;
        m_fileTranslator = fileTranslator;
    }

    /**
     * Adds a new resource type to the internal list of loaded resource types and initializes 
     * options for the resource type.<p>
     *
     * @param resourceType the resource type to add
     */
    private synchronized void initResourceType(
        I_CmsResourceType resourceType,
        CmsResourceManagerConfiguration configuration) {

        // add the loader to the internal list of loaders
        int pos = resourceType.getTypeId();
        if (pos > configuration.m_resourceTypes.length) {
            I_CmsResourceType[] types = new I_CmsResourceType[pos * 2];
            System.arraycopy(configuration.m_resourceTypes, 0, types, 0, configuration.m_resourceTypes.length);
            configuration.m_resourceTypes = types;
        }
        configuration.m_resourceTypes[pos] = resourceType;
        configuration.getResourceTypeList().add(resourceType);
        configuration.getResourceTypeMap().put(resourceType.getTypeName(), resourceType);
        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.INIT_ADD_RESTYPE_3, 
                resourceType.getTypeName(), new Integer(resourceType.getTypeId()), resourceType.getClass().getName()));
        }

        // add the mappings
        List mappings = resourceType.getConfiguredMappings();
        Iterator i = mappings.iterator();
        while (i.hasNext()) {
            String mapping = (String)i.next();
            // only add this mapping if a mapping with this file extension does not
            // exist already
            if (!configuration.getMappings().containsKey(mapping)) {
                configuration.getMappings().put(mapping, resourceType.getTypeName());
                if (CmsLog.LOG.isInfoEnabled()) {
                    CmsLog.LOG.info(Messages.get().key(Messages.INIT_MAP_RESTYPE_2, mapping, resourceType.getTypeName()));
                }
            }
        }
    }

    /**
     * Initializes member variables required for storing the resource types.<p>
     */
    private synchronized void initResourceTypes() {

        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.INIT_STARTING_LOADER_CONFIG_0));
        }

        CmsResourceManagerConfiguration newConfiguration = new CmsResourceManagerConfiguration();

        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.INIT_ADD_RESTYPE_FROM_FILE_2, 
                new Integer(m_resourceTypesFromXml.size()), CmsVfsConfiguration.C_DEFAULT_XML_FILE_NAME));
        }

        // build a new resource type list from the resource types of the XML configuration
        Iterator i;
        i = m_resourceTypesFromXml.iterator();
        while (i.hasNext()) {
            I_CmsResourceType resourceType = (I_CmsResourceType)i.next();
            initResourceType(resourceType, newConfiguration);
        }

        // add all resource types declared in the modules
        CmsModuleManager moduleManager = OpenCms.getModuleManager();
        if (moduleManager != null) {
            i = moduleManager.getModuleNames().iterator();
            while (i.hasNext()) {
                CmsModule module = moduleManager.getModule((String)i.next());
                if (module.getResourceTypes().size() > 0) {
                    // module contains resource types                
                    if (CmsLog.LOG.isInfoEnabled()) {
                        CmsLog.LOG.info(Messages.get().key(
                            Messages.INIT_ADD_NUM_RESTYPES_FROM_MOD_2, new Integer(module.getResourceTypes().size()), module.getName()));
                    }

                    Iterator j = module.getResourceTypes().iterator();
                    while (j.hasNext()) {
                        I_CmsResourceType resourceType = (I_CmsResourceType)j.next();
                        initResourceType(resourceType, newConfiguration);
                    }
                }
            }
        }

        // freeze the current configuration
        newConfiguration.freeze();
        m_configuration = newConfiguration;
        m_frozen = true;

        if (CmsLog.LOG.isInfoEnabled()) {
            CmsLog.LOG.info(Messages.get().key(Messages.INIT_RESOURCE_TYPE_INITIALIZED_0));
        }
    }
}