/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsVfsConfiguration.java,v $
 * Date   : $Date: 2005/10/19 09:25:23 $
 * Version: $Revision: 1.38.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.configuration;

import org.opencms.file.CmsProperty;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsResourceManager;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.xml.CmsXmlContentTypeManager;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.digester.Digester;

import org.dom4j.Element;

/**
 * VFS master configuration class.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.38.2.2 $
 * 
 * @since 6.0.0
 */
public class CmsVfsConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {

    /** The mapping node name. */
    public static final String N_MAPPING = "mapping";

    /** The mappings node name. */
    public static final String N_MAPPINGS = "mappings";
    
    /** The properties node name. */
    public static final String N_PROPERTIES = "properties";    

    /** The resource types node name. */
    public static final String N_RESOURCETYPES = "resourcetypes";

    /** The node name of an individual resource type. */
    public static final String N_TYPE = "type";

    /** The widget attribute. */
    protected static final String A_DEFAULTWIDGET = "defaultwidget";
    
    /** The collector node name. */
    protected static final String N_COLLECTOR = "collector";

    /** The collectors node name. */
    protected static final String N_COLLECTORS = "collectors";

    /** The defaultfile node name. */
    protected static final String N_DEFAULTFILE = "defaultfile";

    /** The defaultfiles node name. */
    protected static final String N_DEFAULTFILES = "defaultfiles";

    /** File translations node name. */
    protected static final String N_FILETRANSLATIONS = "filetranslations";

    /** Folder translations node name. */
    protected static final String N_FOLDERTRANSLATIONS = "foldertranslations";

    /** The node name of an individual resource loader. */
    protected static final String N_LOADER = "loader";

    /** The resource loaders node name. */
    protected static final String N_RESOURCELOADERS = "resourceloaders";

    /** The main resource node name. */
    protected static final String N_RESOURCES = "resources";

    /** The schematype node name. */
    protected static final String N_SCHEMATYPE = "schematype";

    /** The schematypes node name. */
    protected static final String N_SCHEMATYPES = "schematypes";

    /** Individual translation node name. */
    protected static final String N_TRANSLATION = "translation";

    /** The translations master node name. */
    protected static final String N_TRANSLATIONS = "translations";

    /** The node name for the version history. */
    protected static final String N_VERSIONHISTORY = "versionhistory";

    /** The main vfs configuration node name. */
    protected static final String N_VFS = "vfs";
    
    /** The widget node name. */
    protected static final String N_WIDGET = "widget";    

    /** The widgets node name. */
    protected static final String N_WIDGETS = "widgets";    

    /** The xmlcontent node name. */
    protected static final String N_XMLCONTENT = "xmlcontent";
    
    /** The xmlcontents node name. */
    protected static final String N_XMLCONTENTS = "xmlcontents";
    
    /** The source attribute name. */
    private static final String A_SOURCE = "source";
    
    /** The target attribute name. */
    private static final String A_TARGET = "target";

    /** The name of the DTD for this configuration. */
    private static final String CONFIGURATION_DTD_NAME = "opencms-vfs.dtd";

    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms-vfs.xml";
    
    /** The copy-resource node name.*/
    private static final String N_COPY_RESOURCE = "copy-resource";
    
    /** The copy-resources node name.*/
    private static final String N_COPY_RESOURCES = "copy-resources";

    /** The configured XML content type manager. */
    CmsXmlContentTypeManager m_xmlContentTypeManager;

    /** The list of configured default files. */
    private List m_defaultFiles;

    /** Controls if file translation is enabled. */
    private boolean m_fileTranslationEnabled;

    /** The list of file translations. */
    private List m_fileTranslations;

    /** Controls if folder translation is enabled. */
    private boolean m_folderTranslationEnabled;

    /** The list of folder translations. */
    private List m_folderTranslations;

    /** The configured resource manager. */
    private CmsResourceManager m_resourceManager;

    /**
     * Public constructor, will be called by configuration manager.<p> 
     */
    public CmsVfsConfiguration() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
        m_fileTranslations = new ArrayList();
        m_folderTranslations = new ArrayList();
        m_defaultFiles = new ArrayList();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_VFS_CONFIG_INIT_0));
        }
    }

    /**
     * Adds the resource type rules to the given digester.<p>
     * 
     * @param digester the digester to add the rules to
     */     
    public static void addResourceTypeXmlRules(Digester digester) {

        // add rules for resource types
        digester.addFactoryCreate("*/" + N_RESOURCETYPES + "/" + N_TYPE, CmsDigesterResourceTypeCreationFactory.class);

        digester.addCallMethod("*/" + N_RESOURCETYPES + "/" + N_TYPE, I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD, 3);
        // please note: the resource types use a special version of the init method with 3 parameters 
        digester.addCallParam("*/" + N_RESOURCETYPES + "/" + N_TYPE, 0, A_NAME);
        digester.addCallParam("*/" + N_RESOURCETYPES + "/" + N_TYPE, 1, A_ID);
        digester.addCallParam("*/" + N_RESOURCETYPES + "/" + N_TYPE, 2, A_CLASS);
        
        digester.addSetNext("*/" + N_RESOURCETYPES + "/" + N_TYPE, I_CmsResourceType.ADD_RESOURCE_TYPE_METHOD);   

        // add rules for default properties
        digester.addObjectCreate("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY, CmsProperty.class);
        digester.addCallMethod("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY + "/" + N_NAME, "setName", 1);
        digester.addCallParam("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY + "/" + N_NAME, 0);
        
        digester.addCallMethod("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY + "/" + N_VALUE, "setValue", 2);
        digester.addCallParam("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY + "/" + N_VALUE, 0);
        digester.addCallParam("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY + "/" + N_VALUE, 1,  A_TYPE);
        
        digester.addSetNext("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY, "addDefaultProperty");   

        // extension mapping rules
        digester.addCallMethod("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_MAPPINGS + "/" + N_MAPPING, I_CmsResourceType.ADD_MAPPING_METHOD, 1);
        digester.addCallParam ("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_MAPPINGS + "/" + N_MAPPING, 0, A_SUFFIX);       
        
        // copy resource rules
        digester.addCallMethod("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_COPY_RESOURCES + "/" + N_COPY_RESOURCE, "addCopyResource", 3);
        digester.addCallParam ("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_COPY_RESOURCES + "/" + N_COPY_RESOURCE, 0, A_SOURCE);  
        digester.addCallParam ("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_COPY_RESOURCES + "/" + N_COPY_RESOURCE, 1, A_TARGET);  
        digester.addCallParam ("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_COPY_RESOURCES + "/" + N_COPY_RESOURCE, 2, A_TYPE);  
    }   
    
    /**
     * Creates the xml output for resourcetype nodes.<p>
     * 
     * @param startNode the startnode to add all rescource types to
     * @param resourceTypes the list of resource types
     * @param module flag, signaling to add them module resource types or not
     */
    public static void generateResourceTypeXml(Element startNode, List resourceTypes, boolean module) {

        for (int i = 0; i < resourceTypes.size(); i++) {
            I_CmsResourceType resType = (I_CmsResourceType)resourceTypes.get(i);
            // only add this resource type to the xml output, if it is no additional type defined
            // in a module
            if (resType.isAdditionalModuleResourceType() == module) {
                Element resourceType = startNode.addElement(N_TYPE).addAttribute(A_CLASS, resType.getClassName());
                // add type id and type name
                resourceType.addAttribute(A_NAME, resType.getTypeName());
                resourceType.addAttribute(A_ID, String.valueOf(resType.getTypeId()));
                // add resource mappings
                List mappings = resType.getConfiguredMappings();
                if ((mappings != null) && (mappings.size() > 0)) {
                    Element mappingsNode = resourceType.addElement(N_MAPPINGS);
                    for (int j = 0; j < mappings.size(); j++) {
                        Element mapping = mappingsNode.addElement(N_MAPPING);
                        mapping.addAttribute(A_SUFFIX, (String)mappings.get(j));
                    }
                }
                // add default properties
                List properties = resType.getConfiguredDefaultProperties();
                if ((properties != null) && (properties.size() > 0)) {
                    Element propertiesNode = resourceType.addElement(N_PROPERTIES);
                    Iterator p = properties.iterator();
                    while (p.hasNext()) {
                        CmsProperty property = (CmsProperty)p.next();
                        Element propertyNode = propertiesNode.addElement(N_PROPERTY);
                        propertyNode.addElement(N_NAME).addText(property.getName());
                        if (property.getStructureValue() != null) {
                            propertyNode.addElement(N_VALUE).addCDATA(property.getStructureValue());
                        }
                        if (property.getResourceValue() != null) {
                            propertyNode.addElement(N_VALUE).addAttribute(A_TYPE, CmsProperty.TYPE_SHARED).addCDATA(
                                property.getResourceValue());
                        }
                    }
                }
                // add copy resources
                List copyRes = resType.getConfiguredCopyResources();
                if ((copyRes != null) && (copyRes.size() > 0)) {
                    Element copyResNode = resourceType.addElement(N_COPY_RESOURCES);
                    Iterator p = copyRes.iterator();
                    while (p.hasNext()) {
                        CmsConfigurationCopyResource cRes = (CmsConfigurationCopyResource)p.next();
                        Element cNode = copyResNode.addElement(N_COPY_RESOURCE);
                        cNode.addAttribute(A_SOURCE, cRes.getSource());
                        if (!cRes.isTargetWasNull()) {
                            cNode.addAttribute(A_TARGET, cRes.getTarget());
                        }
                        if (!cRes.isTypeWasNull()) {
                            cNode.addAttribute(A_TYPE, cRes.getTypeString());
                        }
                    }
                }
                // add optional parameters
                Map prop = resType.getConfiguration();
                if (prop != null) {
                    List sortedRuntimeProperties = new ArrayList(prop.keySet());
                    Collections.sort(sortedRuntimeProperties);
                    Iterator it = sortedRuntimeProperties.iterator();
                    while (it.hasNext()) {
                        String key = (String)it.next();
                        // create <param name="">value</param> subnodes
                        Object valueObject = prop.get(key);
                        String value = new String();
                        if (valueObject instanceof String) {
                            value = (String)valueObject;
                        } else if (valueObject instanceof Integer) {
                            value = ((Integer)valueObject).toString();
                        }
                        resourceType.addElement(N_PARAM).addAttribute(A_NAME, key).addText(value);
                    }
                }
            }
        }
    }

    /**
     * Adds a directory default file.<p>
     * 
     * @param defaultFile the directory default file to add
     */
    public void addDefaultFile(String defaultFile) {

        m_defaultFiles.add(defaultFile);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(
                Messages.get().key(
                Messages.INIT_VFS_DEFAULT_FILE_2,
                new Integer(m_defaultFiles.size()),
                defaultFile));
        }
    }

    /**
     * Adds one file translation rule.<p>
     * 
     * @param translation the file translation rule to add
     */
    public void addFileTranslation(String translation) {

        m_fileTranslations.add(translation);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(
                Messages.INIT_VFS_ADD_FILE_TRANSLATION_1,
                translation));
        }
    }

    /**
     * Adds one folder translation rule.<p>
     * 
     * @param translation the folder translation rule to add
     */
    public void addFolderTranslation(String translation) {

        m_folderTranslations.add(translation);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(
                Messages.INIT_VFS_ADD_FOLDER_TRANSLATION_1,
                translation));
        }
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        // add finish rule
        digester.addCallMethod("*/" + N_VFS, "initializeFinished");

        // creation of the resource manager
        digester.addObjectCreate("*/" + N_VFS + "/" + N_RESOURCES, CmsResourceManager.class);
        digester.addCallMethod("*/" + N_VFS + "/" + N_RESOURCES, I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD);
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCES, "setResourceManager");

        // add rules for resource loaders
        digester.addObjectCreate("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RESOURCELOADERS + "/" + N_LOADER, A_CLASS, CmsConfigurationException.class);
        digester.addCallMethod("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RESOURCELOADERS + "/" + N_LOADER, I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD);
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RESOURCELOADERS + "/" + N_LOADER, "addLoader");  

        // add rules for resource types
        addResourceTypeXmlRules(digester);
        
        // add rules for VFS content collectors
        digester.addCallMethod("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_COLLECTORS + "/" + N_COLLECTOR, "addContentCollector", 2);
        digester.addCallParam("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_COLLECTORS + "/" + N_COLLECTOR, 0, A_CLASS);
        digester.addCallParam("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_COLLECTORS + "/" + N_COLLECTOR, 1, A_ORDER);

        // generic <param> parameter rules
        digester.addCallMethod("*/" + I_CmsXmlConfiguration.N_PARAM, I_CmsConfigurationParameterHandler.ADD_PARAMETER_METHOD, 2);
        digester.addCallParam ("*/" +  I_CmsXmlConfiguration.N_PARAM, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam ("*/" +  I_CmsXmlConfiguration.N_PARAM, 1);         
                      
        // add rule for default files
        digester.addCallMethod("*/" + N_VFS + "/" + N_DEFAULTFILES + "/" + N_DEFAULTFILE, "addDefaultFile", 1);
        digester.addCallParam("*/" + N_VFS + "/" + N_DEFAULTFILES + "/" + N_DEFAULTFILE, 0, A_NAME);

        // add rules for file translations
        digester.addCallMethod("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FILETRANSLATIONS + "/" + N_TRANSLATION, "addFileTranslation", 0);
        digester.addCallMethod("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FILETRANSLATIONS, "setFileTranslationEnabled", 1);
        digester.addCallParam("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FILETRANSLATIONS, 0, A_ENABLED);        
        
        // add rules for file translations
        digester.addCallMethod("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FOLDERTRANSLATIONS + "/" + N_TRANSLATION, "addFolderTranslation", 0);
        digester.addCallMethod("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FOLDERTRANSLATIONS, "setFolderTranslationEnabled", 1);
        digester.addCallParam("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FOLDERTRANSLATIONS, 0, A_ENABLED);

        // XML content type manager creation rules
        digester.addObjectCreate("*/" + N_VFS + "/" + N_XMLCONTENT, CmsXmlContentTypeManager.class);
        digester.addSetNext("*/" + N_VFS + "/" + N_XMLCONTENT, "setXmlContentTypeManager");

        // XML content widgets add rules
        digester.addCallMethod("*/" + N_VFS + "/" + N_XMLCONTENT + "/" + N_WIDGETS + "/" + N_WIDGET, "addWidget", 2);
        digester.addCallParam("*/" + N_VFS + "/" + N_XMLCONTENT + "/" + N_WIDGETS + "/" + N_WIDGET, 0, A_CLASS);
        digester.addCallParam("*/" + N_VFS + "/" + N_XMLCONTENT + "/" + N_WIDGETS + "/" + N_WIDGET, 1, A_ALIAS);

        // XML content schema type add rules
        digester.addCallMethod("*/" + N_VFS + "/" + N_XMLCONTENT + "/" + N_SCHEMATYPES + "/" + N_SCHEMATYPE, "addSchemaType", 2);
        digester.addCallParam("*/" + N_VFS + "/" + N_XMLCONTENT + "/" + N_SCHEMATYPES + "/" + N_SCHEMATYPE, 0, A_CLASS);
        digester.addCallParam("*/" + N_VFS + "/" + N_XMLCONTENT + "/" + N_SCHEMATYPES + "/" + N_SCHEMATYPE, 1, A_DEFAULTWIDGET); 
    }
    
    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {

        if (OpenCms.getRunLevel() >= OpenCms.RUNLEVEL_3_SHELL_ACCESS) {
            m_resourceManager = OpenCms.getResourceManager();
            m_xmlContentTypeManager = OpenCms.getXmlContentTypeManager();
            m_defaultFiles = OpenCms.getDefaultFiles();
        }

        // generate vfs node and subnodes
        Element vfs = parent.addElement(N_VFS);

        // add resources main element
        Element resources = vfs.addElement(N_RESOURCES);

        // add resource loader
        Element resourceloadersElement = resources.addElement(N_RESOURCELOADERS);
        List loaders = m_resourceManager.getLoaders();
        for (int i = 0; i < loaders.size(); i++) {
            I_CmsResourceLoader loader = (I_CmsResourceLoader)loaders.get(i);
            // add the loader node
            Element loaderNode = resourceloadersElement.addElement(N_LOADER);
            loaderNode.addAttribute(A_CLASS, loader.getClass().getName());
            Map loaderConfiguration = loader.getConfiguration();
            if (loaderConfiguration != null) {
                Iterator it = loaderConfiguration.keySet().iterator();
                while (it.hasNext()) {
                    String name = (String)it.next();
                    String value = loaderConfiguration.get(name).toString();
                    Element paramNode = loaderNode.addElement(N_PARAM);
                    paramNode.addAttribute(A_NAME, name);
                    paramNode.addText(value);
                }
            }
        }

        // add resource types
        Element resourcetypesElement = resources.addElement(N_RESOURCETYPES);
        List resourceTypes = m_resourceManager.getResourceTypes();
        generateResourceTypeXml(resourcetypesElement, resourceTypes, false);

        // add VFS content collectors
        Element collectorsElement = resources.addElement(N_COLLECTORS);
        Iterator it = m_resourceManager.getRegisteredContentCollectors().iterator();
        while (it.hasNext()) {
            I_CmsResourceCollector collector = (I_CmsResourceCollector)it.next();
            collectorsElement.addElement(N_COLLECTOR)
                .addAttribute(A_CLASS, collector.getClass().getName())
                .addAttribute(A_ORDER, String.valueOf(collector.getOrder()));
        }

        // add default file names
        Element defaultFileElement = vfs.addElement(N_DEFAULTFILES);
        it = m_defaultFiles.iterator();
        while (it.hasNext()) {
            defaultFileElement.addElement(N_DEFAULTFILE).addAttribute(A_NAME, (String)it.next());
        }

        // add translation rules
        Element translationsElement = vfs.addElement(N_TRANSLATIONS);

        // file translation rules
        Element fileTransElement = translationsElement.addElement(N_FILETRANSLATIONS).addAttribute(
            A_ENABLED,
            new Boolean(m_fileTranslationEnabled).toString());
        it = m_fileTranslations.iterator();
        while (it.hasNext()) {
            fileTransElement.addElement(N_TRANSLATION).setText(it.next().toString());
        }

        // folder translation rules
        Element folderTransElement = 
            translationsElement.addElement(N_FOLDERTRANSLATIONS)
                .addAttribute(A_ENABLED, new Boolean(m_folderTranslationEnabled).toString());        
        it = m_folderTranslations.iterator();
        while (it.hasNext()) {
            folderTransElement.addElement(N_TRANSLATION).setText(it.next().toString());
        }

        // XML content configuration
        Element xmlContentsElement = vfs.addElement(N_XMLCONTENT);

        // XML widgets
        Element xmlWidgetsElement = xmlContentsElement.addElement(N_WIDGETS);
        it = m_xmlContentTypeManager.getRegisteredWidgetNames().iterator();
        while (it.hasNext()) {
            String widget = (String)it.next();
            Element widgetElement = xmlWidgetsElement.addElement(N_WIDGET).addAttribute(A_CLASS, widget);
            String alias = m_xmlContentTypeManager.getRegisteredWidgetAlias(widget);
            if (alias != null) {
                widgetElement.addAttribute(A_ALIAS, alias);
            }
        }

        // XML content types 
        Element xmlSchemaTypesElement = xmlContentsElement.addElement(N_SCHEMATYPES);
        it = m_xmlContentTypeManager.getRegisteredSchemaTypes().iterator();
        while (it.hasNext()) {
            I_CmsXmlSchemaType type = (I_CmsXmlSchemaType)it.next();
            I_CmsWidget widget = m_xmlContentTypeManager.getWidgetDefault(type.getTypeName());
            xmlSchemaTypesElement.addElement(N_SCHEMATYPE)
                .addAttribute(A_CLASS, type.getClass().getName())
                .addAttribute(A_DEFAULTWIDGET, widget.getClass().getName());
        }       

        // return the vfs node
        return vfs;
    }

    /**
     * Returns the (umodifiable) list of configured directory default files.<p>
     * 
     * @return the (umodifiable) list of configured directory default files
     */
    public List getDefaultFiles() {

        return Collections.unmodifiableList(m_defaultFiles);
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {

        return CONFIGURATION_DTD_NAME;
    }

    /**
     * Returns the file resource translator that has been initialized
     * with the configured file translation rules.<p>
     * 
     * @return the file resource translator 
     */
    public CmsResourceTranslator getFileTranslator() {

        String[] array = m_fileTranslationEnabled ? new String[m_fileTranslations.size()] : new String[0];
        for (int i = 0; i < m_fileTranslations.size(); i++) {
            array[i] = (String)m_fileTranslations.get(i);
        }
        return new CmsResourceTranslator(array, true);
    }

    /**
     * Returns the folder resource translator that has been initialized
     * with the configured folder translation rules.<p>
     * 
     * @return the folder resource translator 
     */
    public CmsResourceTranslator getFolderTranslator() {

        String[] array = m_folderTranslationEnabled ? new String[m_folderTranslations.size()] : new String[0];
        for (int i = 0; i < m_folderTranslations.size(); i++) {
            array[i] = (String)m_folderTranslations.get(i);
        }
        return new CmsResourceTranslator(array, false);
    }

    /**
     * Returns the initialized resource manager.<p>
     * 
     * @return the initialized resource manager
     */
    public CmsResourceManager getResourceManager() {

        return m_resourceManager;
    }

    /**
     * Returns the configured XML content type manager.<p>
     * 
     * @return the configured XML content type manager
     */
    public CmsXmlContentTypeManager getXmlContentTypeManager() {

        return m_xmlContentTypeManager;
    }

    /**
     * Will be called when configuration of this object is finished.<p> 
     */
    public void initializeFinished() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_VFS_CONFIG_FINISHED_0));
        }
    }

    /**
     * Enables or disables the file translation rules.<p>
     * 
     * @param value if <code>"true"</code>, file translation is enabled, otherwise it is disabled
     */
    public void setFileTranslationEnabled(String value) {

        m_fileTranslationEnabled = Boolean.valueOf(value).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            if (m_fileTranslationEnabled) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_VFS_FILE_TRANSLATION_ENABLE_0));
            } else {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_VFS_FILE_TRANSLATION_DISABLE_0));
            }
        }
    }

    /**
     * Enables or disables the folder translation rules.<p>
     * 
     * @param value if <code>"true"</code>, folder translation is enabled, otherwise it is disabled
     */
    public void setFolderTranslationEnabled(String value) {

        m_folderTranslationEnabled = Boolean.valueOf(value).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            if (m_folderTranslationEnabled) {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_VFS_FOLDER_TRANSLATION_ENABLE_0));
            } else {
                CmsLog.INIT.info(Messages.get().key(Messages.INIT_VFS_FOLDER_TRANSLATION_DISABLE_0));
            }
        }
    }

    /**
     * Sets the generated resource manager.<p>
     * 
     * @param manager the resource manager to set
     */
    public void setResourceManager(CmsResourceManager manager) {

        m_resourceManager = manager;
    }

    /**
     * Sets the generated XML content type manager.<p>
     * 
     * @param manager the generated XML content type manager to set
     */
    public void setXmlContentTypeManager(CmsXmlContentTypeManager manager) {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().key(Messages.INIT_VFS_XML_CONTENT_FINISHED_0));
        }
        m_xmlContentTypeManager = manager;
    }
    
}