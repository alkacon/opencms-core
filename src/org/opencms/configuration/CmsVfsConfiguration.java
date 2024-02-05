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

package org.opencms.configuration;

import org.opencms.file.CmsProperty;
import org.opencms.file.collectors.I_CmsResourceCollector;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.loader.CmsDefaultFileNameGenerator;
import org.opencms.loader.CmsMimeType;
import org.opencms.loader.CmsResourceManager;
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsHtmlConverterOption;
import org.opencms.util.CmsResourceTranslator;
import org.opencms.util.CmsStringUtil;
import org.opencms.widgets.I_CmsWidget;
import org.opencms.xml.CmsXmlContentTypeManager;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.digester3.Digester;
import org.apache.commons.digester3.Rule;

import org.dom4j.Element;
import org.xml.sax.Attributes;

/**
 * VFS master configuration class.<p>
 *
 * @since 6.0.0
 */
public class CmsVfsConfiguration extends A_CmsXmlConfiguration {

    /** The adjust-links-folder attribute. */
    public static final String A_ADJUST_LINKS_FOLDER = "adjust-links-folder";

    /** The widget configuration attribute. */
    public static final String A_CONFIGURATION = "configuration";

    /** The widget attribute. */
    public static final String A_DEFAULTWIDGET = "defaultwidget";

    /** The extension attribute name. */
    public static final String A_EXTENSION = "extension";

    /** The source attribute name. */
    public static final String A_SOURCE = "source";

    /** The target attribute name. */
    public static final String A_TARGET = "target";

    /** The name of the DTD for this configuration. */
    public static final String CONFIGURATION_DTD_NAME = "opencms-vfs.dtd";

    /** The name of the default XML file for this configuration. */
    public static final String DEFAULT_XML_FILE_NAME = "opencms-vfs.xml";

    /** The collector node name. */
    public static final String N_COLLECTOR = "collector";

    /** The collectors node name. */
    public static final String N_COLLECTORS = "collectors";

    /** The copy-resource node name.*/
    public static final String N_COPY_RESOURCE = "copy-resource";

    /** The copy-resources node name.*/
    public static final String N_COPY_RESOURCES = "copy-resources";

    /** The defaultfile node name. */
    public static final String N_DEFAULTFILE = "defaultfile";

    /** The defaultfiles node name. */
    public static final String N_DEFAULTFILES = "defaultfiles";

    /** File translations node name. */
    public static final String N_FILETRANSLATIONS = "filetranslations";

    /** Folder translations node name. */
    public static final String N_FOLDERTRANSLATIONS = "foldertranslations";

    /** The html-converter node name.*/
    public static final String N_HTML_CONVERTER = "html-converter";

    /** The html-converters node name.*/
    public static final String N_HTML_CONVERTERS = "html-converters";

    /** The node name of an individual resource loader. */
    public static final String N_LOADER = "loader";

    /** The mapping node name. */
    public static final String N_MAPPING = "mapping";

    /** The mappings node name. */
    public static final String N_MAPPINGS = "mappings";

    /** The mimetype node name. */
    public static final String N_MIMETYPE = "mimetype";

    /** The mimetypes node name. */
    public static final String N_MIMETYPES = "mimetypes";

    /** The properties node name. */
    public static final String N_PROPERTIES = "properties";

    /** The relation type node name. */
    public static final String N_RELATIONTYPE = "relationtype";

    /** The relation types node name. */
    public static final String N_RELATIONTYPES = "relationtypes";

    /** The resource loaders node name. */
    public static final String N_RESOURCELOADERS = "resourceloaders";

    /** The main resource node name. */
    public static final String N_RESOURCES = "resources";

    /** The resource types node name. */
    public static final String N_RESOURCETYPES = "resourcetypes";

    /** The schematype node name. */
    public static final String N_SCHEMATYPE = "schematype";

    /** The schematypes node name. */
    public static final String N_SCHEMATYPES = "schematypes";

    /** Individual translation node name. */
    public static final String N_TRANSLATION = "translation";

    /** The translations master node name. */
    public static final String N_TRANSLATIONS = "translations";

    /** The node name of an individual resource type. */
    public static final String N_TYPE = "type";

    /** The node name for the version history. */
    public static final String N_VERSIONHISTORY = "versionhistory";

    /** The main vfs configuration node name. */
    public static final String N_VFS = "vfs";

    /** The widget node name. */
    public static final String N_WIDGET = "widget";

    /** The widget alias node name. */
    public static final String N_WIDGET_ALIAS = "widget-alias";

    /** The widgets node name. */
    public static final String N_WIDGETS = "widgets";

    /** The xmlcontent node name. */
    public static final String N_XMLCONTENT = "xmlcontent";

    /** The xmlcontents node name. */
    public static final String N_XMLCONTENTS = "xmlcontents";

    /** XSD translations node name. */
    public static final String N_XSDTRANSLATIONS = "xsdtranslations";

    /** The namegenerator node name. */
    private static final String N_NAMEGENERATOR = "namegenerator";

    /** The configured XML content type manager. */
    CmsXmlContentTypeManager m_xmlContentTypeManager;

    /** The list of configured default files. */
    private List<String> m_defaultFiles;

    /** Controls if file translation is enabled. */
    private boolean m_fileTranslationEnabled;

    /** The list of file translations. */
    private List<String> m_fileTranslations;

    /** Controls if folder translation is enabled. */
    private boolean m_folderTranslationEnabled;

    /** The list of folder translations. */
    private List<String> m_folderTranslations;

    /** The configured resource manager. */
    private CmsResourceManager m_resourceManager;

    /** Controls if XSD translation is enabled. */
    private boolean m_xsdTranslationEnabled;

    /** The list of XSD translations. */
    private List<String> m_xsdTranslations;

    /**
     * Adds the resource type rules to the given digester.<p>
     *
     * @param digester the digester to add the rules to
     */
    public static void addResourceTypeXmlRules(Digester digester) {

        // add rules for resource types
        digester.addFactoryCreate("*/" + N_RESOURCETYPES + "/" + N_TYPE, CmsDigesterResourceTypeCreationFactory.class);
        digester.addSetNext("*/" + N_RESOURCETYPES + "/" + N_TYPE, I_CmsResourceType.ADD_RESOURCE_TYPE_METHOD);

        // please note: the order of the rules is very important here,
        // the "set next" rule (above) must be added _before_ the "call method" rule (below)!
        // reason is digester will call the rule that was last added first
        // here we must make sure that the resource type is initialized first (with the "call method" rule)
        // before it is actually added to the resource type container (with the "set next" rule)
        // otherwise there will be an empty resource type added to the container, and validation will not work
        digester.addCallMethod(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE,
            I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD,
            3);
        // please note: the resource types use a special version of the init method with 3 parameters
        digester.addCallParam("*/" + N_RESOURCETYPES + "/" + N_TYPE, 0, A_NAME);
        digester.addCallParam("*/" + N_RESOURCETYPES + "/" + N_TYPE, 1, A_ID);
        digester.addCallParam("*/" + N_RESOURCETYPES + "/" + N_TYPE, 2, A_CLASS);

        // add rules for default properties
        digester.addObjectCreate(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY,
            CmsProperty.class);
        digester.addCallMethod(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY + "/" + N_NAME,
            "setName",
            1);
        digester.addCallParam(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY + "/" + N_NAME,
            0);

        digester.addCallMethod(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY + "/" + N_VALUE,
            "setValue",
            2);
        digester.addCallParam(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY + "/" + N_VALUE,
            0);
        digester.addCallParam(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY + "/" + N_VALUE,
            1,
            A_TYPE);

        digester.addSetNext(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_PROPERTIES + "/" + N_PROPERTY,
            "addDefaultProperty");

        // extension mapping rules
        digester.addCallMethod(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_MAPPINGS + "/" + N_MAPPING,
            I_CmsResourceType.ADD_MAPPING_METHOD,
            1);
        digester.addCallParam("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_MAPPINGS + "/" + N_MAPPING, 0, A_SUFFIX);

        digester.addCallMethod(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_COPY_RESOURCES,
            "setAdjustLinksFolder",
            1);
        digester.addCallParam("*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_COPY_RESOURCES, 0, A_ADJUST_LINKS_FOLDER);

        // copy resource rules
        digester.addCallMethod(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_COPY_RESOURCES + "/" + N_COPY_RESOURCE,
            "addCopyResource",
            3);
        digester.addCallParam(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_COPY_RESOURCES + "/" + N_COPY_RESOURCE,
            0,
            A_SOURCE);
        digester.addCallParam(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_COPY_RESOURCES + "/" + N_COPY_RESOURCE,
            1,
            A_TARGET);
        digester.addCallParam(
            "*/" + N_RESOURCETYPES + "/" + N_TYPE + "/" + N_COPY_RESOURCES + "/" + N_COPY_RESOURCE,
            2,
            A_TYPE);
    }

    /**
     * Creates the xml output for resourcetype nodes.<p>
     *
     * @param startNode the startnode to add all rescource types to
     * @param resourceTypes the list of resource types
     * @param module flag, signaling to add them module resource types or not
     */
    public static void generateResourceTypeXml(
        Element startNode,
        List<I_CmsResourceType> resourceTypes,
        boolean module) {

        for (int i = 0; i < resourceTypes.size(); i++) {
            I_CmsResourceType resType = resourceTypes.get(i);
            // only add this resource type to the xml output, if it is no additional type defined
            // in a module
            if (resType.isAdditionalModuleResourceType() == module) {
                Element resourceType = startNode.addElement(N_TYPE).addAttribute(A_CLASS, resType.getClassName());
                // add type id and type name
                resourceType.addAttribute(A_NAME, resType.getTypeName());
                resourceType.addAttribute(A_ID, String.valueOf(resType.getTypeId()));
                // add resource mappings
                List<String> mappings = resType.getConfiguredMappings();
                if ((mappings != null) && (mappings.size() > 0)) {
                    Element mappingsNode = resourceType.addElement(N_MAPPINGS);
                    for (int j = 0; j < mappings.size(); j++) {
                        Element mapping = mappingsNode.addElement(N_MAPPING);
                        mapping.addAttribute(A_SUFFIX, mappings.get(j));
                    }
                }
                // add default properties
                List<CmsProperty> properties = resType.getConfiguredDefaultProperties();
                if (properties != null) {
                    if (properties.size() > 0) {
                        Element propertiesNode = resourceType.addElement(N_PROPERTIES);
                        Iterator<CmsProperty> p = properties.iterator();
                        while (p.hasNext()) {
                            CmsProperty property = p.next();
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
                }
                // add copy resources
                List<CmsConfigurationCopyResource> copyRes = resType.getConfiguredCopyResources();
                if ((copyRes != null) && (copyRes.size() > 0)) {
                    Element copyResNode = resourceType.addElement(N_COPY_RESOURCES);
                    Iterator<CmsConfigurationCopyResource> p = copyRes.iterator();
                    String adjustLinksFolder = resType.getAdjustLinksFolder();
                    if (adjustLinksFolder != null) {
                        copyResNode.addAttribute(A_ADJUST_LINKS_FOLDER, adjustLinksFolder);
                    }
                    while (p.hasNext()) {
                        CmsConfigurationCopyResource cRes = p.next();
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
                CmsParameterConfiguration configuration = resType.getConfiguration();
                if (configuration != null) {
                    List<String> ignore = null;
                    if ((resType instanceof CmsResourceTypeXmlContainerPage)) {
                        ignore = new ArrayList<String>(1);
                        ignore.add(CmsResourceTypeXmlContent.CONFIGURATION_SCHEMA);
                    }
                    configuration.appendToXml(resourceType, ignore);
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
                Messages.get().getBundle().key(
                    Messages.INIT_VFS_DEFAULT_FILE_2,
                    Integer.valueOf(m_defaultFiles.size()),
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
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_ADD_FILE_TRANSLATION_1, translation));
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
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_ADD_FOLDER_TRANSLATION_1, translation));
        }
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester3.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {

        // add finish rule
        digester.addCallMethod("*/" + N_VFS, "initializeFinished");

        // creation of the resource manager
        digester.addObjectCreate("*/" + N_VFS + "/" + N_RESOURCES, CmsResourceManager.class);
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_RESOURCES,
            I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD);
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCES, "setResourceManager");

        // add rules for resource loaders
        digester.addObjectCreate(
            "*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RESOURCELOADERS + "/" + N_LOADER,
            CmsConfigurationException.class.getName(),
            A_CLASS);
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RESOURCELOADERS + "/" + N_LOADER,
            I_CmsConfigurationParameterHandler.INIT_CONFIGURATION_METHOD);
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RESOURCELOADERS + "/" + N_LOADER, "addLoader");

        // add rules for resource types
        addResourceTypeXmlRules(digester);

        // add rules for VFS content collectors
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_RESOURCES + "/" + N_COLLECTORS + "/" + N_COLLECTOR,
            "addContentCollector",
            2);
        digester.addCallParam("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_COLLECTORS + "/" + N_COLLECTOR, 0, A_CLASS);
        digester.addCallParam("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_COLLECTORS + "/" + N_COLLECTOR, 1, A_ORDER);

        // add the name generator
        digester.addObjectCreate(
            "*/" + N_VFS + "/" + N_RESOURCES + "/" + N_NAMEGENERATOR,
            CmsDefaultFileNameGenerator.class.getName(),
            A_CLASS);
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_NAMEGENERATOR, "setNameGenerator");

        // add MIME type rules
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_RESOURCES + "/" + N_MIMETYPES + "/" + N_MIMETYPE,
            "addMimeType",
            2);
        digester.addCallParam("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_MIMETYPES + "/" + N_MIMETYPE, 0, A_EXTENSION);
        digester.addCallParam("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_MIMETYPES + "/" + N_MIMETYPE, 1, A_TYPE);

        // add relation type rules
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RELATIONTYPES + "/" + N_RELATIONTYPE,
            "addRelationType",
            2);
        digester.addCallParam(
            "*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RELATIONTYPES + "/" + N_RELATIONTYPE,
            0,
            A_NAME);
        digester.addCallParam(
            "*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RELATIONTYPES + "/" + N_RELATIONTYPE,
            1,
            A_TYPE);

        // add html converter rules
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_RESOURCES + "/" + N_HTML_CONVERTERS + "/" + N_HTML_CONVERTER,
            "addHtmlConverter",
            2);
        digester.addCallParam(
            "*/" + N_VFS + "/" + N_RESOURCES + "/" + N_HTML_CONVERTERS + "/" + N_HTML_CONVERTER,
            0,
            A_NAME);
        digester.addCallParam(
            "*/" + N_VFS + "/" + N_RESOURCES + "/" + N_HTML_CONVERTERS + "/" + N_HTML_CONVERTER,
            1,
            A_CLASS);

        // generic <param> parameter rules
        digester.addCallMethod(
            "*/" + I_CmsXmlConfiguration.N_PARAM,
            I_CmsConfigurationParameterHandler.ADD_PARAMETER_METHOD,
            2);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam("*/" + I_CmsXmlConfiguration.N_PARAM, 1);

        // add rule for default files
        digester.addCallMethod("*/" + N_VFS + "/" + N_DEFAULTFILES + "/" + N_DEFAULTFILE, "addDefaultFile", 1);
        digester.addCallParam("*/" + N_VFS + "/" + N_DEFAULTFILES + "/" + N_DEFAULTFILE, 0, A_NAME);

        // add rules for file translations
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FILETRANSLATIONS + "/" + N_TRANSLATION,
            "addFileTranslation",
            0);
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FILETRANSLATIONS,
            "setFileTranslationEnabled",
            1);
        digester.addCallParam("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FILETRANSLATIONS, 0, A_ENABLED);

        // add rules for file translations
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FOLDERTRANSLATIONS + "/" + N_TRANSLATION,
            "addFolderTranslation",
            0);
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FOLDERTRANSLATIONS,
            "setFolderTranslationEnabled",
            1);
        digester.addCallParam("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FOLDERTRANSLATIONS, 0, A_ENABLED);

        // add rules for file translations
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_XSDTRANSLATIONS + "/" + N_TRANSLATION,
            "addXsdTranslation",
            0);
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_XSDTRANSLATIONS,
            "setXsdTranslationEnabled",
            1);
        digester.addCallParam("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_XSDTRANSLATIONS, 0, A_ENABLED);

        // XML content type manager creation rules
        digester.addObjectCreate("*/" + N_VFS + "/" + N_XMLCONTENT, CmsXmlContentTypeManager.class);
        digester.addSetNext("*/" + N_VFS + "/" + N_XMLCONTENT, "setXmlContentTypeManager");

        // XML content widgets add rules

        // Widget definitions.
        // 'aliases' list is used/reset by the rule for widgets, and filled by the rule for aliases.
        final List<String> aliases = new ArrayList<>();
        digester.addRule("*/" + N_VFS + "/" + N_XMLCONTENT + "/" + N_WIDGETS + "/" + N_WIDGET, new Rule() {

            private String m_className;
            private String m_config;

            @Override
            public void begin(String namespace, String name, Attributes attributes) throws Exception {

                m_className = attributes.getValue(A_CLASS);
                m_config = attributes.getValue(A_CONFIGURATION);
                String alias = attributes.getValue(A_ALIAS);

                aliases.clear();
                if (alias != null) {
                    aliases.add(alias.trim());
                }
            }

            @Override
            public void end(String namespace, String name) throws Exception {

                CmsXmlContentTypeManager manager = getDigester().peek();
                List<String> aliasesCopy = new ArrayList<>(aliases);
                manager.addWidget(m_className, aliasesCopy, m_config);
            }
        });

        digester.addRule(
            "*/" + N_VFS + "/" + N_XMLCONTENT + "/" + N_WIDGETS + "/" + N_WIDGET + "/" + N_WIDGET_ALIAS,
            new Rule() {

                @Override
                public void body(String namespace, String name, String text) throws Exception {

                    aliases.add(text.trim());
                }

            });

        // XML content schema type add rules
        digester.addCallMethod(
            "*/" + N_VFS + "/" + N_XMLCONTENT + "/" + N_SCHEMATYPES + "/" + N_SCHEMATYPE,
            "addSchemaType",
            2);
        digester.addCallParam("*/" + N_VFS + "/" + N_XMLCONTENT + "/" + N_SCHEMATYPES + "/" + N_SCHEMATYPE, 0, A_CLASS);
        digester.addCallParam(
            "*/" + N_VFS + "/" + N_XMLCONTENT + "/" + N_SCHEMATYPES + "/" + N_SCHEMATYPE,
            1,
            A_DEFAULTWIDGET);
    }

    /**
     * Adds one XSD translation rule.<p>
     *
     * @param translation the XSD translation rule to add
     */
    public void addXsdTranslation(String translation) {

        m_xsdTranslations.add(translation);
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_ADD_XSD_TRANSLATION_1, translation));
        }
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
        for (I_CmsResourceLoader loader : m_resourceManager.getLoaders()) {
            // add the loader node
            Element loaderNode = resourceloadersElement.addElement(N_LOADER);
            loaderNode.addAttribute(A_CLASS, loader.getClass().getName());
            CmsParameterConfiguration loaderConfiguration = loader.getConfiguration();
            if (loaderConfiguration != null) {
                loaderConfiguration.appendToXml(loaderNode);
            }
        }

        // add resource types
        Element resourcetypesElement = resources.addElement(N_RESOURCETYPES);
        List<I_CmsResourceType> resourceTypes = new ArrayList<I_CmsResourceType>();
        if (m_resourceManager.getResTypeUnknownFolder() != null) {
            resourceTypes.add(m_resourceManager.getResTypeUnknownFolder());
        }
        if (m_resourceManager.getResTypeUnknownFile() != null) {
            resourceTypes.add(m_resourceManager.getResTypeUnknownFile());
        }
        resourceTypes.addAll(m_resourceManager.getResourceTypes());
        generateResourceTypeXml(resourcetypesElement, resourceTypes, false);

        // add VFS content collectors
        Element collectorsElement = resources.addElement(N_COLLECTORS);
        for (I_CmsResourceCollector collector : m_resourceManager.getRegisteredContentCollectors()) {
            collectorsElement.addElement(N_COLLECTOR).addAttribute(
                A_CLASS,
                collector.getClass().getName()).addAttribute(A_ORDER, String.valueOf(collector.getOrder()));
        }

        Element namegeneratorElement = resources.addElement(N_NAMEGENERATOR);
        String nameGeneratorClass = m_resourceManager.getNameGenerator().getClass().getName();
        namegeneratorElement.addAttribute(A_CLASS, nameGeneratorClass);

        // add MIME types
        Element mimeTypesElement = resources.addElement(N_MIMETYPES);
        for (CmsMimeType type : m_resourceManager.getMimeTypes()) {
            mimeTypesElement.addElement(N_MIMETYPE).addAttribute(A_EXTENSION, type.getExtension()).addAttribute(
                A_TYPE,
                type.getType());
        }

        // add relation types
        Element relationTypesElement = resources.addElement(N_RELATIONTYPES);
        for (CmsRelationType type : m_resourceManager.getRelationTypes()) {
            relationTypesElement.addElement(N_RELATIONTYPE).addAttribute(A_NAME, type.getName()).addAttribute(
                A_TYPE,
                type.getType());
        }

        // HTML converter configuration
        boolean writeConfig = false;
        for (CmsHtmlConverterOption converter : m_resourceManager.getHtmlConverters()) {
            if (!converter.isDefault()) {
                // found a non default converter configuration, set flag to write configuration
                writeConfig = true;
                break;
            }
        }
        if (writeConfig) {
            // configuration is written because non default options were found
            Element htmlConvertersElement = resources.addElement(N_HTML_CONVERTERS);
            for (CmsHtmlConverterOption converter : m_resourceManager.getHtmlConverters()) {
                Element converterElement = htmlConvertersElement.addElement(N_HTML_CONVERTER).addAttribute(
                    A_NAME,
                    converter.getName());
                converterElement.addAttribute(A_CLASS, converter.getClassName());
            }
        }

        // add default file names
        Element defaultFileElement = vfs.addElement(N_DEFAULTFILES);
        for (String element : m_defaultFiles) {
            defaultFileElement.addElement(N_DEFAULTFILE).addAttribute(A_NAME, element);
        }

        // add translation rules
        Element translationsElement = vfs.addElement(N_TRANSLATIONS);

        // file translation rules
        Element fileTransElement = translationsElement.addElement(N_FILETRANSLATIONS).addAttribute(
            A_ENABLED,
            String.valueOf(m_fileTranslationEnabled));
        for (String translation : m_fileTranslations) {
            fileTransElement.addElement(N_TRANSLATION).setText(translation);
        }

        // folder translation rules
        Element folderTransElement = translationsElement.addElement(N_FOLDERTRANSLATIONS).addAttribute(
            A_ENABLED,
            String.valueOf(m_folderTranslationEnabled));
        for (String translation : m_folderTranslations) {
            folderTransElement.addElement(N_TRANSLATION).setText(translation);
        }

        // XSD translation rules
        Element xsdTransElement = translationsElement.addElement(N_XSDTRANSLATIONS).addAttribute(
            A_ENABLED,
            String.valueOf(m_xsdTranslationEnabled));
        for (String translation : m_xsdTranslations) {
            xsdTransElement.addElement(N_TRANSLATION).setText(translation);
        }

        // XML content configuration
        Element xmlContentsElement = vfs.addElement(N_XMLCONTENT);

        // XML widgets
        Element xmlWidgetsElement = xmlContentsElement.addElement(N_WIDGETS);
        for (String widget : m_xmlContentTypeManager.getRegisteredWidgetNames()) {
            Element widgetElement = xmlWidgetsElement.addElement(N_WIDGET).addAttribute(A_CLASS, widget);
            for (String alias : m_xmlContentTypeManager.getRegisteredWidgetAliases(widget)) {
                widgetElement.addElement(N_WIDGET_ALIAS).addText(alias);
            }
            String defaultConfiguration = m_xmlContentTypeManager.getWidgetDefaultConfiguration(widget);
            if (CmsStringUtil.isNotEmpty(defaultConfiguration)) {
                widgetElement.addAttribute(A_CONFIGURATION, defaultConfiguration);
            }
        }

        // XML content types
        Element xmlSchemaTypesElement = xmlContentsElement.addElement(N_SCHEMATYPES);
        for (I_CmsXmlSchemaType type : m_xmlContentTypeManager.getRegisteredSchemaTypes()) {
            I_CmsWidget widget = m_xmlContentTypeManager.getWidgetDefault(type.getTypeName());
            xmlSchemaTypesElement.addElement(N_SCHEMATYPE).addAttribute(
                A_CLASS,
                type.getClass().getName()).addAttribute(A_DEFAULTWIDGET, widget.getClass().getName());
        }

        // return the vfs node
        return vfs;
    }

    /**
     * Returns the (unmodifiable) list of configured directory default files.<p>
     *
     * @return the (unmodifiable) list of configured directory default files
     */
    public List<String> getDefaultFiles() {

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

        String[] array = new String[0];
        if (m_fileTranslationEnabled) {
            array = new String[m_fileTranslations.size()];
            for (int i = 0; i < m_fileTranslations.size(); i++) {
                array[i] = m_fileTranslations.get(i);
            }
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

        String[] array = new String[0];
        if (m_folderTranslationEnabled) {
            array = new String[m_folderTranslations.size()];
            for (int i = 0; i < m_folderTranslations.size(); i++) {
                array[i] = m_folderTranslations.get(i);
            }
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
     * Returns the XSD translator that has been initialized
     * with the configured XSD translation rules.<p>
     *
     * @return the XSD translator
     */
    public CmsResourceTranslator getXsdTranslator() {

        String[] array = m_xsdTranslationEnabled ? new String[m_xsdTranslations.size()] : new String[0];
        for (int i = 0; i < m_xsdTranslations.size(); i++) {
            array[i] = m_xsdTranslations.get(i);
        }
        return new CmsResourceTranslator(array, true);
    }

    /**
     * Will be called when configuration of this object is finished.<p>
     */
    public void initializeFinished() {

        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_CONFIG_FINISHED_0));
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
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_FILE_TRANSLATION_ENABLE_0));
            } else {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_FILE_TRANSLATION_DISABLE_0));
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
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_FOLDER_TRANSLATION_ENABLE_0));
            } else {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_FOLDER_TRANSLATION_DISABLE_0));
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
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_XML_CONTENT_FINISHED_0));
        }
        m_xmlContentTypeManager = manager;
    }

    /**
     * Enables or disables the XSD translation rules.<p>
     *
     * @param value if <code>"true"</code>, XSD translation is enabled, otherwise it is disabled
     */
    public void setXsdTranslationEnabled(String value) {

        m_xsdTranslationEnabled = Boolean.valueOf(value).booleanValue();
        if (CmsLog.INIT.isInfoEnabled()) {
            if (m_xsdTranslationEnabled) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_XSD_TRANSLATION_ENABLE_0));
            } else {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_XSD_TRANSLATION_DISABLE_0));
            }
        }
    }

    /**
     * @see org.opencms.configuration.A_CmsXmlConfiguration#initMembers()
     */
    @Override
    protected void initMembers() {

        setXmlFileName(DEFAULT_XML_FILE_NAME);
        m_fileTranslations = new ArrayList<String>();
        m_folderTranslations = new ArrayList<String>();
        m_xsdTranslations = new ArrayList<String>();
        m_defaultFiles = new ArrayList<String>();
        if (CmsLog.INIT.isInfoEnabled()) {
            CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_VFS_CONFIG_INIT_0));
        }
    }
}