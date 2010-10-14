/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsConfigurationParser.java,v $
 * Date   : $Date: 2010/10/14 06:10:06 $
 * Version: $Revision: 1.11 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.cache.CmsVfsMemoryObjectCache;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFormatterUtil;
import org.opencms.util.CmsPair;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.Transformer;

/**
 * Class for managing the creation of new content elements in ADE.<p>
 * 
 * XML files in the VFS can be used to configure which files are used as
 * prototypes for new elements, and which file names are used for the new
 * elements.<p> 
 * 
 * TODO: separate the parser from the data it parses 
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 7.6 
 */
public class CmsConfigurationParser {

    /** The tag name for the export name configuration. */
    public static final String N_ADE_EXPORTNAME = "ADEExportName";

    /** The tag name of the configuration for a single type. */
    public static final String N_ADE_TYPE = "ADEType";

    /** The tag name of the destination in the type configuration. */
    public static final String N_DESTINATION = "Destination";

    /** The tag name of the source file in the type configuration. */
    public static final String N_FOLDER = "Folder";

    /** The tag name of a formatter configuration. */
    public static final String N_FORMATTER = "Formatter";

    /** The tag name of the formatter jsp. */
    public static final String N_JSP = "Jsp";

    /** The tag name of the source file in the type configuration. */
    public static final String N_PATTERN = "Pattern";

    /** The tag name of the source file in the type configuration. */
    public static final String N_SOURCE = "Source";

    /** The tag name of the formatter container type. */
    public static final String N_TYPE = "Type";

    /** The tag name of the formatter width. */
    public static final String N_WIDTH = "Width";

    /** The instance cache. */
    private static CmsVfsMemoryObjectCache m_cache = new CmsVfsMemoryObjectCache();

    /** The tag name for elements containing field configurations. */
    private static final String N_ADE_FIELD = "ADEField";

    /** Configuration data, read from xml content. */
    private Map<String, CmsConfigurationItem> m_configuration = new HashMap<String, CmsConfigurationItem>();

    /** The configured export name. */
    private String m_exportName;

    /** The formatter configuration maps. */
    private Map<String, CmsPair<Map<String, String>, Map<Integer, String>>> m_formatterConfiguration = new HashMap<String, CmsPair<Map<String, String>, Map<Integer, String>>>();

    /** New elements. */
    private Collection<CmsResource> m_newElements = new LinkedHashSet<CmsResource>();

    /** The list of properties read from the configuration file. */
    private List<CmsXmlContentProperty> m_props = new ArrayList<CmsXmlContentProperty>();

    /**
     * Default constructor.<p>
     */
    public CmsConfigurationParser() {

        // do nothing

    }

    /**
     * Constructs a new instance.<p>
     * 
     * @param cms the cms context used for reading the configuration
     * @param config the configuration file
     *  
     * @throws CmsException if something goes wrong
     */
    public CmsConfigurationParser(CmsObject cms, CmsResource config)
    throws CmsException {

        processFile(cms, config);
    }

    /**
     * Constructs a parser and caches it so that subsequent calls to this method with the same resource
     * will return the same object if the resource hasn'T changed.<p>
     * 
     * @param cms the CMS context 
     * @param config the configuration resource 
     * @return the configuration parser 
     * 
     * @throws CmsException if something goes wrong 
     */
    public static CmsConfigurationParser getParser(final CmsObject cms, final CmsResource config) throws CmsException {

        try {
            Object cachedObj = m_cache.loadVfsObject(cms, config.getRootPath(), new Transformer() {

                /**
                 * @see org.apache.commons.collections.Transformer#transform(java.lang.Object)
                 */
                public Object transform(Object o) {

                    try {
                        return new CmsConfigurationParser(cms, config);
                    } catch (CmsException e) {
                        // the Transformer interface does not allow checked exceptions, so we wrap
                        // them into runtime exceptions and then unwrap them again later.
                        throw new CmsRuntimeException(e.getMessageContainer(), e);
                    }
                }
            });
            return (CmsConfigurationParser)cachedObj;
        } catch (CmsRuntimeException e) {
            throw (CmsException)e.getCause();
        }
    }

    /**
     * Returns an unmodifiable list of properties defined in the configuration file.<p>
     *  
     * @return the list of properties defined in the configuration file 
     */
    public List<CmsXmlContentProperty> getDefinedProperties() {

        return Collections.unmodifiableList(m_props);
    }

    /**
     * Returns the configured export name.<p>
     * 
     * @return the configured export name 
     */
    public String getExportName() {

        return m_exportName;
    }

    /**
     * Returns the formatter configuration for a given element type.<p>
     * 
     * @param type a type name 
     * 
     * @return a pair of maps containing the formatter configuration for the type 
     */
    public CmsPair<Map<String, String>, Map<Integer, String>> getFormatterConfigurationForType(String type) {

        return m_formatterConfiguration.get(type);
    }

    /**
     * Gets the list of 'prototype resources' which are used for creating new content elements.
     * 
     * @param cms the CMS context
     * 
     * @return the resources which are used as prototypes for creating new elements
     */
    public Collection<CmsResource> getNewElements(CmsObject cms) {

        Set<CmsResource> result = new LinkedHashSet<CmsResource>();
        for (Map.Entry<String, CmsConfigurationItem> entry : m_configuration.entrySet()) {
            CmsConfigurationItem item = entry.getValue();
            String type = entry.getKey();
            CmsResource source = item.getSourceFile();
            CmsResource folderRes = item.getFolder();
            CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(type);
            boolean editable = settings.isEditable(cms, folderRes);
            boolean controlPermission = settings.getAccess().getPermissions(cms, folderRes).requiresControlPermission();
            if (editable && controlPermission) {
                result.add(source);
            }
        }
        return result;
    }

    /**
     * Returns the configuration as an unmodifiable map.<p>
     * 
     * @return the configuration as an unmodifiable map
     */
    public Map<String, CmsConfigurationItem> getTypeConfiguration() {

        return Collections.unmodifiableMap(m_configuration);
    }

    /**
     * Parses a type configuration contained in an XML content.<p>
     * 
     * This method uses the first locale from the following list which has a corresponding
     * element in the XML content:
     * <ul>
     *  <li>the request context's locale</li>
     *  <li>the default locale</li>
     *  <li>the first locale available in the XML content</li>
     * </ul><p>
     *
     * @param cms the CmsObject to use for VFS operations
     * @param content the XML content with the type configuration
     * 
     * @throws CmsException if something goes wrong
     */
    public void parseConfiguration(CmsObject cms, I_CmsXmlDocument content) throws CmsException {

        Locale locale = getLocale(cms, content);
        List<I_CmsXmlContentValue> typeValues = content.getValues(N_ADE_TYPE, locale);

        for (I_CmsXmlContentValue xmlType : typeValues) {
            parseType(cms, xmlType, locale);
        }

        List<I_CmsXmlContentValue> fieldValues = content.getValues(N_ADE_FIELD, locale);
        for (I_CmsXmlContentValue xmlField : fieldValues) {
            parseField(cms, xmlField, locale);
        }

        I_CmsXmlContentValue exportNameNode = content.getValue(N_ADE_EXPORTNAME, locale);
        if (exportNameNode != null) {
            m_exportName = exportNameNode.getStringValue(cms);
        }

    }

    /**
     * Reads additional configuration data from a file.<p>
     * 
     * @param cms the CMS context 
     * @param config the configuration file 
     * 
     * @throws CmsException if something goes wrong 
     */
    public void processFile(CmsObject cms, CmsResource config) throws CmsException {

        CmsFile configFile = cms.readFile(config);
        I_CmsXmlDocument content = CmsXmlContentFactory.unmarshal(cms, configFile);
        parseConfiguration(cms, content);
    }

    /**
     * Adds the configuration from another parser to this one.<p>
     * 
     * @param parser the configuration parser whose data should be added to this one 
     */
    public void update(CmsConfigurationParser parser) {

        for (Map.Entry<String, CmsConfigurationItem> entry : parser.m_configuration.entrySet()) {
            m_configuration.put(entry.getKey(), entry.getValue());
        }
        for (CmsResource res : parser.m_newElements) {
            m_newElements.add(res);
        }
        m_formatterConfiguration.putAll(parser.m_formatterConfiguration);
    }

    /**
     * Helper method for finding the locale for accessing the XML content.<p>
     * 
     * @param cms the CMS context 
     * @param content the XML content 
     * 
     * @return the locale
     * 
     * @throws CmsException if something goes wrong 
     */
    protected Locale getLocale(CmsObject cms, I_CmsXmlDocument content) throws CmsException {

        Locale currentLocale = cms.getRequestContext().getLocale();
        Locale defaultLocale = CmsLocaleManager.getDefaultLocale();
        Locale locale = null;
        if (content.hasLocale(currentLocale)) {
            locale = currentLocale;
        } else if (content.hasLocale(defaultLocale)) {
            locale = defaultLocale;
        } else {
            List<Locale> locales = content.getLocales();
            if (locales.size() == 0) {
                throw new CmsException(Messages.get().container(
                    Messages.ERR_NO_TYPE_CONFIG_1,
                    content.getFile().getRootPath()));
            }
            locale = locales.get(0);
        }
        return locale;
    }

    /**
     * Returns a named child value of a given XML content value.<p> 
     * 
     * @param value the XML content value whose sub-element value should be read
     * @param subPath a path relative to the given value's path 
     * 
     * @return the XML content value  
     */
    protected I_CmsXmlContentValue getSubValue(I_CmsXmlContentValue value, String subPath) {

        Locale locale = value.getLocale();

        I_CmsXmlContentValue subValue = value.getDocument().getValue(
            CmsXmlUtils.concatXpath(value.getPath(), subPath),
            locale);
        return subValue;
    }

    /**
     * Returns a child content value of a given content value as a string.<p>
     *  
     * @param cms the CMS context 
     * @param value the parent content value
     * @param subPath the name of the child content value, relative to the parent's path 
     * 
     * @return the child content value as a string 
     */
    protected String getSubValueAsString(CmsObject cms, I_CmsXmlContentValue value, String subPath) {

        I_CmsXmlContentValue subValue = getSubValue(value, subPath);
        return subValue != null ? subValue.getStringValue(cms) : null;
    }

    /**
     * Helper method for retrieving the sub-values of a given XML content value.
     * 
     * @param value the parent XML content value 
     * @param subPath the path relative to parent value 
     * 
     * @return the list of sub-values with the given path below the parent value 
     */
    protected List<I_CmsXmlContentValue> getSubValues(I_CmsXmlContentValue value, String subPath) {

        Locale locale = value.getLocale();
        String path = CmsXmlUtils.concatXpath(value.getPath(), subPath);
        return value.getDocument().getValues(path, locale);

    }

    /**
     * Helper method for retrieving the OpenCms type name for a given type id.<p>
     * 
     * @param typeId the id of the type
     * 
     * @return the name of the type
     * 
     * @throws CmsException if something goes wrong
     */
    protected String getTypeName(int typeId) throws CmsException {

        return OpenCms.getResourceManager().getResourceType(typeId).getTypeName();
    }

    /**
     * Parses a single field definition from a content value.<p>
     * 
     * @param cms the CMS context 
     * @param xmlField the content value to parse the field from 
     * @param locale the locale to use 
     */
    private void parseField(CmsObject cms, I_CmsXmlContentValue xmlField, Locale locale) {

        String name = getSubValueAsString(cms, xmlField, "Name");
        String type = getSubValueAsString(cms, xmlField, "Type");
        String widget = getSubValueAsString(cms, xmlField, "Widget");
        String widgetConfig = getSubValueAsString(cms, xmlField, "WidgetConfig");
        String ruleRegex = getSubValueAsString(cms, xmlField, "RuleRegex");
        String ruleType = getSubValueAsString(cms, xmlField, "RuleType");
        String default1 = getSubValueAsString(cms, xmlField, "Default");
        String error = getSubValueAsString(cms, xmlField, "Error");
        String niceName = getSubValueAsString(cms, xmlField, "NiceName");
        String description = getSubValueAsString(cms, xmlField, "Description");
        String advanced = getSubValueAsString(cms, xmlField, "Advanced");
        String selectInherit = getSubValueAsString(cms, xmlField, "SelectInherit");
        CmsXmlContentProperty prop = new CmsXmlContentProperty(
            name,
            type,
            widget,
            widgetConfig,
            ruleRegex,
            ruleType,
            default1,
            niceName,
            description,
            error,
            advanced,
            selectInherit);
        m_props.add(prop);
    }

    /**
     * Internal method for parsing the element types in the configuration file.<p>
     * 
     * @param cms the CMS context 
     * @param xmlType a content value representing an element type 
     * @param locale the locale to use 
     * 
     * @throws CmsException if something goes wrong 
     */
    private void parseType(CmsObject cms, I_CmsXmlContentValue xmlType, Locale locale) throws CmsException {

        String source = getSubValueAsString(cms, xmlType, N_SOURCE);
        String folder = getSubValueAsString(cms, xmlType, CmsXmlUtils.concatXpath(N_DESTINATION, N_FOLDER));
        cms.getRequestContext().addSiteRoot(folder);
        String pattern = getSubValueAsString(cms, xmlType, CmsXmlUtils.concatXpath(N_DESTINATION, N_PATTERN));
        CmsResource resource = cms.readResource(source);
        String type = getTypeName(resource.getTypeId());
        CmsConfigurationItem configItem = new CmsConfigurationItem(
            cms.readResource(source),
            cms.readResource(folder),
            pattern);
        List<I_CmsXmlContentValue> fmtValues = getSubValues(xmlType, N_FORMATTER);
        List<CmsFormatterConfigBean> formatterConfigBeans = new ArrayList<CmsFormatterConfigBean>();
        for (I_CmsXmlContentValue fmtValue : fmtValues) {
            String jsp = getSubValueAsString(cms, fmtValue, N_JSP);
            String width = getSubValueAsString(cms, fmtValue, N_WIDTH);
            String fmtType = getSubValueAsString(cms, fmtValue, N_TYPE);
            formatterConfigBeans.add(new CmsFormatterConfigBean(jsp, fmtType, width));
        }
        if (!formatterConfigBeans.isEmpty()) {
            CmsPair<Map<String, String>, Map<Integer, String>> formatterMaps = CmsFormatterUtil.getFormatterMapsFromConfigBeans(
                formatterConfigBeans,
                xmlType.getDocument().getFile().getRootPath());
            m_formatterConfiguration.put(type, formatterMaps);
        }
        m_configuration.put(type, configItem);
    }

}
