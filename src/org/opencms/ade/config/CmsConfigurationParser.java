/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/ade/config/CmsConfigurationParser.java,v $
 * Date   : $Date: 2011/05/02 14:21:13 $
 * Version: $Revision: 1.4 $
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

package org.opencms.ade.config;

import org.opencms.ade.detailpage.CmsDetailPageInfo;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.containerpage.CmsConfigurationItem;
import org.opencms.xml.containerpage.CmsFormatterBean;
import org.opencms.xml.containerpage.CmsFormatterConfiguration;
import org.opencms.xml.containerpage.CmsLazyFolder;
import org.opencms.xml.containerpage.Messages;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.CmsXmlContentRootLocation;
import org.opencms.xml.content.I_CmsXmlContentLocation;
import org.opencms.xml.content.I_CmsXmlContentValueLocation;
import org.opencms.xml.types.CmsXmlBooleanValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

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
 * @version $Revision: 1.4 $ 
 * 
 * @since 7.6 
 */
public class CmsConfigurationParser {

    /** The default maximum sitemap depth. */
    public static final int DEFAULT_MAX_DEPTH = 15;

    /** The tag name of the configuration for a single type. */
    public static final String N_ADE_TYPE = "ADEType";

    /** The tag name of the destination in the type configuration. */
    public static final String N_DESTINATION = "Destination";

    /** The detail page node name. */
    public static final String N_DETAIL_PAGE = "DetailPage";

    /** The tag name of the source file in the type configuration. */
    public static final String N_FOLDER = "Folder";

    /** The tag name of a formatter configuration. */
    public static final String N_FORMATTER = "Formatter";

    /** The Name node name. */
    public static final String N_IS_DEFAULT = "IsDefault";

    /** The tag name of the formatter jsp. */
    public static final String N_JSP = "Jsp";

    /** Node name for the maximum depth configuration. */
    public static final String N_MAXDEPTH = "MaxDepth";

    /** The tag name of the formatter maximum width. */
    public static final String N_MAXWIDTH = "MaxWidth";

    /** The tag name of the formatter that indicates if the content should be searched. */
    public static final String N_SEARCHCONTENT = "SearchContent";

    /** The Page node name. */
    public static final String N_PAGE = "Page";

    /** The tag name of the source file in the type configuration. */
    public static final String N_PATTERN = "Pattern";

    /** The tag name of the "prefer folder" option for properties. */
    public static final String N_PREFER_FOLDER = "PreferFolder";

    /** The tag name of the source file in the type configuration. */
    public static final String N_SOURCE = "Source";

    /** The tag name of the formatter container type. */
    public static final String N_TYPE = "Type";

    /** The tag name of the formatter width. */
    public static final String N_MINWIDTH = "Width";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsConfigurationParser.class);

    /** The tag name for elements containing field configurations. */
    private static final String N_ADE_FIELD = "ADEField";

    /** Configuration data, read from xml content. */
    private Map<String, CmsConfigurationItem> m_configuration = new LinkedHashMap<String, CmsConfigurationItem>();

    /** The xml document. */
    private CmsXmlContent m_content;

    /** The detail pages from the configuration file. */
    private List<CmsDetailPageInfo> m_detailPages;

    /** The formatter configurations. */
    private Map<String, CmsFormatterConfiguration> m_formatterConfiguration = new HashMap<String, CmsFormatterConfiguration>();

    /** The maximum sitemap depth. */
    private int m_maxDepth = DEFAULT_MAX_DEPTH;

    /** New elements. */
    private Collection<CmsConfigurationItem> m_newElements = new LinkedHashSet<CmsConfigurationItem>();

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
     * Returns the container page configuration data.<p>
     * 
     * @param sourceInfo the source information 
     * 
     * @return the container page configuration data 
     */
    public CmsContainerPageConfigurationData getContainerPageConfigurationData(CmsConfigurationSourceInfo sourceInfo) {

        return new CmsContainerPageConfigurationData(m_configuration, m_formatterConfiguration, sourceInfo);
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
     * Returns the detail pages from the configuration.<p>
     * 
     * @return the detail pages from the configuration 
     */
    public List<CmsDetailPageInfo> getDetailPages() {

        return Collections.unmodifiableList(m_detailPages);
    }

    /**
     * Returns the formatter configuration for a given element type.<p>
     * 
     * @param type a type name 
     * 
     * @return a pair of maps containing the formatter configuration for the type 
     */
    public CmsFormatterConfiguration getFormatterConfigurationForType(String type) {

        return m_formatterConfiguration.get(type);
    }

    /**
     * Returns the maximum sitemap depth.<p>
     * 
     * @return the maximum sitemap depth 
     */
    public int getMaxDepth() {

        return m_maxDepth;
    }

    /** 
     * Returns the property configuration.<p>
     * 
     * @return the property configuration indexed by property name
     */
    public Map<String, CmsXmlContentProperty> getPropertyConfigMap() {

        Map<String, CmsXmlContentProperty> props = new HashMap<String, CmsXmlContentProperty>();
        for (CmsXmlContentProperty propDef : m_props) {
            props.put(propDef.getName(), propDef);
        }
        return props;
    }

    /**
     * Creates a sitemap configuration data bean.<p>
     * 
     * @param sourceInfo the source information 
     * 
     * @return the sitemap configuration data bean 
     */
    public CmsSitemapConfigurationData getSitemapConfigurationData(CmsConfigurationSourceInfo sourceInfo) {

        return new CmsSitemapConfigurationData(
            m_configuration,
            m_newElements,
            getPropertyConfigMap(),
            groupDetailPagesByType(),
            m_maxDepth,
            sourceInfo);
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
     * Gets the detail page configuration, grouped by type.<p>
     * 
     * @return a map from type names to lists of detail page information beans 
     */
    public Map<String, List<CmsDetailPageInfo>> groupDetailPagesByType() {

        Map<String, List<CmsDetailPageInfo>> result = new HashMap<String, List<CmsDetailPageInfo>>();
        for (CmsDetailPageInfo info : m_detailPages) {
            String type = info.getType();
            if (!result.containsKey(type)) {
                result.put(type, new ArrayList<CmsDetailPageInfo>());
            }
            result.get(type).add(info);
        }
        return result;
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
        CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, configFile);
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
        for (CmsConfigurationItem item : parser.m_newElements) {
            m_newElements.add(item);
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
     * Convenience method to retrieve the sub-value of an xml-element as boolean.<p>
     * 
     * @param cms the current cms object
     * @param field the xml-element
     * @param fieldName the element name
     * 
     * @return the value or <code>null</code> if the sub element by the given name is not present
     */
    private boolean getSubValueBoolean(CmsObject cms, I_CmsXmlContentLocation field, String fieldName) {

        I_CmsXmlContentValueLocation subValue = field.getSubValue(fieldName);
        if ((subValue != null) && (subValue.getValue() instanceof CmsXmlBooleanValue)) {
            return ((CmsXmlBooleanValue)subValue.getValue()).getBooleanValue();
        }
        return false;
    }

    /**
     * Convenience method to retrieve the sub-value of an xml-element as id.<p>
     * 
     * @param cms the current cms object
     * @param field the xml-element
     * @param fieldName the element name
     * 
     * @return the value or <code>null</code> if the sub element by the given name is not present
     */
    private CmsUUID getSubValueID(CmsObject cms, I_CmsXmlContentLocation field, String fieldName) {

        I_CmsXmlContentValueLocation subValue = field.getSubValue(fieldName);
        if (subValue != null) {
            return subValue.asId(cms);
        }
        return null;
    }

    /**
     * Convenience method to retrieve the sub-value of an xml-element as string.<p>
     * 
     * @param cms the current cms object
     * @param field the xml-element
     * @param fieldName the element name
     * 
     * @return the value or <code>null</code> if the sub element by the given name is not present
     */
    private String getSubValueString(CmsObject cms, I_CmsXmlContentLocation field, String fieldName) {

        I_CmsXmlContentValueLocation subValue = field.getSubValue(fieldName);
        if (subValue != null) {
            return subValue.asString(cms);
        }
        return null;
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
    private void parseConfiguration(CmsObject cms, CmsXmlContent content) throws CmsException {

        Locale locale = getLocale(cms, content);
        m_content = content;
        I_CmsXmlContentLocation root = new CmsXmlContentRootLocation(content, locale);

        List<I_CmsXmlContentValueLocation> typeValues = root.getSubValues(N_ADE_TYPE);

        for (I_CmsXmlContentValueLocation xmlType : typeValues) {
            try {
                parseType(cms, xmlType, locale);
            } catch (Exception e) {
                LOG.error(Messages.get().container(Messages.ERR_CONFIG_MALFORMED_TYPE_0), e);
            }
        }

        List<I_CmsXmlContentValueLocation> fieldValues = root.getSubValues(N_ADE_FIELD);
        for (I_CmsXmlContentValueLocation xmlField : fieldValues) {
            parseField(cms, xmlField, locale);
        }

        List<I_CmsXmlContentValueLocation> detailPageValues = root.getSubValues(N_DETAIL_PAGE);
        for (I_CmsXmlContentValueLocation detailPageValue : detailPageValues) {
            parseDetailPage(cms, detailPageValue);
        }

        m_detailPages = parseDetailPages(cms, root);

        I_CmsXmlContentValue maxDepthNode = content.getValue(N_MAXDEPTH, locale);
        if (maxDepthNode != null) {
            try {
                m_maxDepth = Integer.parseInt(maxDepthNode.getStringValue(cms));
            } catch (NumberFormatException e) {
                // ignore, leave max depth at its default value 
            }
        }

    }

    /**
     * Parses a single detail page bean from the configuration.<p>
     * 
     * @param cms the current CMS context 
     * @param detailPageNode the location from which to read the detail page bean 
     * 
     * @return the parsed detail page bean  
     * 
     * @throws CmsException if something goes wrong 
     */
    private CmsDetailPageInfo parseDetailPage(CmsObject cms, I_CmsXmlContentValueLocation detailPageNode)
    throws CmsException {

        String type = getSubValueString(cms, detailPageNode, N_TYPE);
        I_CmsXmlContentValueLocation target = detailPageNode.getSubValue(N_PAGE);
        if ((target == null) || (type == null) || (target.asId(null) == null)) {
            return null;
        }
        CmsUUID targetId = target.asId(null);
        String targetPath = cms.getRequestContext().addSiteRoot(target.asString(cms));
        CmsDetailPageInfo result = new CmsDetailPageInfo(targetId, targetPath, type);
        try {
            cms.readResource(targetId);
        } catch (CmsVfsResourceNotFoundException e) {
            // if the resource doesn't exist, we set the result to null so it won't get added to the configuration data 
            result = null;
        }
        return result;
    }

    /** 
     * Parses the detail pages from the configuration file.<p>
     * 
     * @param cms the current CMS context 
     * @param root the location from which to read the detail pages
     *  
     * @return the parsed detail page beans
     * 
     * @throws CmsException if something goes wrong 
     */
    private List<CmsDetailPageInfo> parseDetailPages(CmsObject cms, I_CmsXmlContentLocation root) throws CmsException {

        List<I_CmsXmlContentValueLocation> values = root.getSubValues(N_DETAIL_PAGE);
        List<CmsDetailPageInfo> result = new ArrayList<CmsDetailPageInfo>();
        for (I_CmsXmlContentValueLocation detailPageNode : values) {
            CmsDetailPageInfo info = parseDetailPage(cms, detailPageNode);
            if (info != null) {
                result.add(info);
            }
        }
        return result;
    }

    /**
     * Parses a single field definition from a content value.<p>
     * 
     * @param cms the CMS context 
     * @param field the content value to parse the field from 
     * @param locale the locale to use 
     */
    private void parseField(CmsObject cms, I_CmsXmlContentLocation field, Locale locale) {

        String name = getSubValueString(cms, field, "Name");
        String type = getSubValueString(cms, field, "Type");
        String widget = getSubValueString(cms, field, "Widget");
        String widgetConfig = getSubValueString(cms, field, "WidgetConfig");

        String ruleRegex = getSubValueString(cms, field, "RuleRegex");
        String ruleType = getSubValueString(cms, field, "RuleType");
        String default1 = getSubValueString(cms, field, "Default");
        String error = getSubValueString(cms, field, "Error");
        String niceName = getSubValueString(cms, field, "NiceName");
        String description = getSubValueString(cms, field, "Description");
        String preferFolder = getSubValueString(cms, field, "PreferFolder");
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
            preferFolder);
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
    private void parseType(CmsObject cms, I_CmsXmlContentLocation xmlType, Locale locale) throws CmsException {

        boolean isDefault = getSubValueBoolean(cms, xmlType, N_IS_DEFAULT);
        CmsUUID source = getSubValueID(cms, xmlType, N_SOURCE);
        CmsResource resource = cms.readResource(source);

        String type = getTypeName(resource.getTypeId());

        CmsUUID folder = getSubValueID(cms, xmlType, CmsXmlUtils.concatXpath(N_DESTINATION, N_FOLDER));
        String pattern = getSubValueString(cms, xmlType, CmsXmlUtils.concatXpath(N_DESTINATION, N_PATTERN));

        CmsResource folderRes = null;
        CmsLazyFolder lazyFolder = null;
        if (folder == null) {
            String path = "/" + type;
            lazyFolder = new CmsLazyFolder(path);
        } else {
            folderRes = cms.readResource(folder);
            lazyFolder = new CmsLazyFolder(folderRes);
        }

        CmsConfigurationItem configItem = new CmsConfigurationItem(resource, folderRes, lazyFolder, pattern, isDefault);
        List<I_CmsXmlContentValueLocation> fmtValues = xmlType.getSubValues(N_FORMATTER);
        CmsFormatterConfiguration formatterConfiguration = new CmsFormatterConfiguration();
        for (I_CmsXmlContentValueLocation fmtValue : fmtValues) {
            String jsp = getSubValueString(cms, fmtValue, N_JSP);
            String fmtType = getSubValueString(cms, fmtValue, N_TYPE);
            String minWidth = getSubValueString(cms, fmtValue, N_MINWIDTH);
            String maxwidth = getSubValueString(cms, fmtValue, N_MAXWIDTH);
            String searchContent = getSubValueString(cms, fmtValue, N_SEARCHCONTENT);
            formatterConfiguration.addFormatter(new CmsFormatterBean(
                jsp,
                fmtType,
                minWidth,
                maxwidth,
                searchContent,
                m_content));
        }
        if (formatterConfiguration.hasFormatters()) {
            m_formatterConfiguration.put(type, formatterConfiguration);
        }

        m_newElements.add(configItem);
        if (!isDefault && m_configuration.containsKey(type)) {
            // this type is not marked as default, so don't override any previous type configuration
            return;
        }
        m_configuration.put(type, configItem);
    }

}
