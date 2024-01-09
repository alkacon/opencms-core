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

package org.opencms.xml.content;

import org.opencms.ade.configuration.CmsADEConfigData;
import org.opencms.ade.containerpage.shared.CmsFormatterConfig;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.i18n.CmsMultiMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.search.galleries.CmsGalleryNameMacroResolver;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.util.I_CmsMacroResolver;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.containerpage.I_CmsFormatterBean;
import org.opencms.xml.content.CmsXmlContentProperty.PropType;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;

import javax.servlet.ServletRequest;

import org.apache.commons.logging.Log;

import org.dom4j.Element;

import com.google.common.base.Objects;
import com.google.common.base.Supplier;

/**
 * Provides common methods on XML property configuration.<p>
 *
 * @since 8.0.0
 */
public final class CmsXmlContentPropertyHelper implements Cloneable {

    /** Element Property json property  constants. */
    public enum JsonProperty {

        /** Property's default value. */
        defaultValue,
        /** Property's description. */
        description,
        /** Property's error message. */
        error,
        /** Property's nice name. */
        niceName,
        /** Property's validation regular expression. */
        ruleRegex,
        /** Property's validation rule type. */
        ruleType,
        /** Property's type. */
        type,
        /** Property's value. */
        value,
        /** Property's widget. */
        widget,
        /** Property's widget configuration. */
        widgetConf;
    }

    /** The prefix for macros used to acess properties of the current container page. */
    public static final String PAGE_PROPERTY_PREFIX = "page-property:";

    /** If a property has this value, the page-property macro for this property will expand to the empty string instead. */
    protected static final Object PROPERTY_EMPTY_MARKER = "-";

    /** Widget configuration key-value separator constant. */
    private static final String CONF_KEYVALUE_SEPARATOR = ":";

    /** Widget configuration parameter separator constant. */
    private static final String CONF_PARAM_SEPARATOR = "\\|";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContentPropertyHelper.class);

    /**
     * Hidden constructor.<p>
     */
    private CmsXmlContentPropertyHelper() {

        // prevent instantiation
    }

    /**
     * Converts a map of properties from server format to client format.<p>
     *
     * @param cms the CmsObject to use for VFS operations
     * @param props the map of properties
     * @param propConfig the property configuration
     *
     * @return the converted property map
     */
    public static Map<String, String> convertPropertiesToClientFormat(
        CmsObject cms,
        Map<String, String> props,
        Map<String, CmsXmlContentProperty> propConfig) {

        return convertProperties(cms, props, propConfig, true);
    }

    /**
     * Converts a map of properties from client format to server format.<p>
     *
     * @param cms the CmsObject to use for VFS operations
     * @param props the map of properties
     * @param propConfig the property configuration
     *
     * @return the converted property map
     */
    public static Map<String, String> convertPropertiesToServerFormat(
        CmsObject cms,
        Map<String, String> props,
        Map<String, CmsXmlContentProperty> propConfig) {

        return convertProperties(cms, props, propConfig, false);
    }

    /**
     * Creates a deep copy of a property configuration map.<p>
     *
     * @param propConfig the property configuration which should be copied
     *
     * @return a copy of the property configuration
     */
    public static Map<String, CmsXmlContentProperty> copyPropertyConfiguration(
        Map<String, CmsXmlContentProperty> propConfig) {

        Map<String, CmsXmlContentProperty> result = new LinkedHashMap<String, CmsXmlContentProperty>();
        for (Map.Entry<String, CmsXmlContentProperty> entry : propConfig.entrySet()) {
            String key = entry.getKey();
            CmsXmlContentProperty propDef = entry.getValue();
            result.put(key, propDef.copy());
        }
        return result;
    }

    /**
     * Looks up an URI in the sitemap and returns either a sitemap entry id (if the URI is a sitemap URI)
     * or the structure id of a resource (if the URI is a VFS path).<p>
     *
     * @param cms the current CMS context
     * @param uri the URI to look up
     * @return a sitemap entry id or a structure id
     *
     * @throws CmsException if something goes wrong
     */
    public static CmsUUID getIdForUri(CmsObject cms, String uri) throws CmsException {

        return cms.readResource(uri).getStructureId();
    }

    /**
     * Creates and configures a new macro resolver for resolving macros which occur in property definitions.<p>
     *
     * @param cms the CMS context
     * @param contentHandler the content handler which contains the message bundle that should be available in the macro resolver
     * @param content the XML content object
     * @param stringtemplateSource provides stringtemplate templates for use in %(stringtemplate:...) macros
     * @param containerPage the current container page
     *
     * @return a new macro resolver
     */
    public static CmsMacroResolver getMacroResolverForProperties(
        final CmsObject cms,
        final I_CmsXmlContentHandler contentHandler,
        final CmsXmlContent content,
        final Function<String, String> stringtemplateSource,
        final CmsResource containerPage) {

        Locale locale = OpenCms.getLocaleManager().getBestAvailableLocaleForXmlContent(cms, content.getFile(), content);
        final CmsGalleryNameMacroResolver resolver = new CmsGalleryNameMacroResolver(cms, content, locale) {

            @SuppressWarnings("synthetic-access")
            @Override
            public String getMacroValue(String macro) {

                if (macro.startsWith(PAGE_PROPERTY_PREFIX)) {
                    String remainder = macro.substring(PAGE_PROPERTY_PREFIX.length());
                    int secondColonPos = remainder.indexOf(":");
                    String defaultValue = "";
                    String propName = null;
                    if (secondColonPos >= 0) {
                        propName = remainder.substring(0, secondColonPos);
                        defaultValue = remainder.substring(secondColonPos + 1);
                    } else {
                        propName = remainder;
                    }
                    if (containerPage != null) {
                        try {
                            CmsProperty prop = cms.readPropertyObject(containerPage, propName, true);
                            String propValue = prop.getValue();
                            if ((propValue == null) || PROPERTY_EMPTY_MARKER.equals(propValue)) {
                                propValue = defaultValue;
                            }
                            return propValue;
                        } catch (CmsException e) {
                            LOG.error(e.getLocalizedMessage(), e);
                            return defaultValue;
                        }
                    }

                }
                return super.getMacroValue(macro);
            }

        };

        resolver.setStringTemplateSource(stringtemplateSource);
        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        CmsMultiMessages messages = new CmsMultiMessages(wpLocale);
        messages.addMessages(OpenCms.getWorkplaceManager().getMessages(wpLocale));
        messages.addMessages(content.getContentDefinition().getContentHandler().getMessages(wpLocale));
        resolver.setCmsObject(cms);
        resolver.setKeepEmptyMacros(true);
        resolver.setMessages(messages);
        return resolver;
    }

    /**
     * Returns the property information for the given resource (type) AND the current user.<p>
     *
     * @param cms the current CMS context
     * @param page the current container page
     * @param resource the resource
     *
     * @return the property information
     *
     * @throws CmsException if something goes wrong
     */
    public static Map<String, CmsXmlContentProperty> getPropertyInfo(
        CmsObject cms,
        CmsResource page,
        CmsResource resource)
    throws CmsException {

        if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
            I_CmsXmlContentHandler contentHandler = CmsXmlContentDefinition.getContentHandlerForResource(cms, resource);
            Map<String, CmsXmlContentProperty> propertiesConf = contentHandler.getSettings(cms, resource);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(cms, cms.readFile(resource));
            CmsMacroResolver resolver = getMacroResolverForProperties(cms, contentHandler, content, null, page);
            return resolveMacrosInProperties(propertiesConf, resolver);
        }
        return Collections.<String, CmsXmlContentProperty> emptyMap();
    }

    /**
     * Returns a converted property value depending on the given type.<p>
     *
     * If the type is {@link CmsXmlContentProperty.PropType#vfslist}, the value is parsed as a
     * list of paths and converted to a list of IDs.<p>
     *
     * @param cms the current CMS context
     * @param type the property type
     * @param value the raw property value
     *
     * @return a converted property value depending on the given type
     */
    public static String getPropValueIds(CmsObject cms, String type, String value) {

        if (PropType.isVfsList(type)) {
            return convertPathsToIds(cms, value);
        }
        return value;
    }

    /**
     * Returns a converted property value depending on the given type.<p>
     *
     * If the type is {@link CmsXmlContentProperty.PropType#vfslist}, the value is parsed as a
     * list of IDs and converted to a list of paths.<p>
     *
     * @param cms the current CMS context
     * @param type the property type
     * @param value the raw property value
     *
     * @return a converted property value depending on the given type
     */
    public static String getPropValuePaths(CmsObject cms, String type, String value) {

        if (PropType.isVfsList(type)) {
            return convertIdsToPaths(cms, value);
        }
        return value;
    }

    /**
     * Returns a sitemap or VFS path given a sitemap entry id or structure id.<p>
     *
     * This method first tries to read a sitemap entry with the given id. If this succeeds,
     * the sitemap entry's sitemap path will be returned. If it fails, the method interprets
     * the id as a structure id and tries to read the corresponding resource, and then returns
     * its VFS path.<p>
     *
     * @param cms the CMS context
     * @param id a sitemap entry id or structure id
     *
     * @return a sitemap or VFS uri
     *
     * @throws CmsException if something goes wrong
     */
    public static String getUriForId(CmsObject cms, CmsUUID id) throws CmsException {

        CmsResource res = cms.readResource(id);
        return cms.getSitePath(res);
    }

    /**
     * Returns the widget configuration string parsed into a JSONObject.<p>
     *
     * The configuration string should be a map of key value pairs separated by ':' and '|': KEY_1:VALUE_1|KEY_2:VALUE_2 ...
     *
     * @param widgetConfiguration the configuration to parse
     *
     * @return the configuration JSON
     */
    public static JSONObject getWidgetConfigurationAsJSON(String widgetConfiguration) {

        JSONObject result = new JSONObject();
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(widgetConfiguration)) {
            return result;
        }
        Map<String, String> confEntries = CmsStringUtil.splitAsMap(
            widgetConfiguration,
            CONF_PARAM_SEPARATOR,
            CONF_KEYVALUE_SEPARATOR);
        for (Map.Entry<String, String> entry : confEntries.entrySet()) {
            try {
                result.put(entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                // should never happen
                LOG.error(
                    Messages.get().container(Messages.ERR_XMLCONTENT_UNKNOWN_ELEM_PATH_SCHEMA_1, widgetConfiguration),
                    e);
            }
        }
        return result;
    }

    /**
     * Extends the given properties with the default values
     * from the resource's property configuration.<p>
     *
     * @param cms the current CMS context
     * @param config the current sitemap configuration
     * @param resource the resource to get the property configuration from
     * @param properties the properties to extend
     * @param locale the content locale
     * @param request the current request, if available
     *
     * @return a merged map of properties
     */
    public static Map<String, String> mergeDefaults(
        CmsObject cms,
        CmsADEConfigData config,
        CmsResource resource,
        Map<String, String> properties,
        Locale locale,
        ServletRequest request) {

        Map<String, CmsXmlContentProperty> propertyConfig = null;
        if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
            I_CmsFormatterBean formatter = null;
            // check formatter configuration setting
            for (Entry<String, String> property : properties.entrySet()) {
                if (property.getKey().startsWith(CmsFormatterConfig.FORMATTER_SETTINGS_KEY)) {
                    I_CmsFormatterBean dynamicFmt = config.findFormatter(property.getValue());
                    if (dynamicFmt != null) {
                        formatter = dynamicFmt;
                        break;
                    }
                }

            }

            try {

                if (formatter != null) {
                    propertyConfig = OpenCms.getADEManager().getFormatterSettings(
                        cms,
                        config,
                        formatter,
                        resource,
                        locale,
                        request);
                } else {
                    // fall back to schema configuration
                    propertyConfig = CmsXmlContentDefinition.getContentHandlerForResource(cms, resource).getSettings(
                        cms,
                        resource);
                }
            } catch (CmsException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        return mergeDefaults(cms, propertyConfig, properties);
    }

    /**
     * Extends the given properties with the default values
     * from property configuration.<p>
     *
     * @param cms the current CMS context
     * @param propertyConfig the property configuration
     * @param properties the properties to extend
     *
     * @return a merged map of properties
     */
    public static Map<String, String> mergeDefaults(
        CmsObject cms,
        Map<String, CmsXmlContentProperty> propertyConfig,
        Map<String, String> properties) {

        Set<String> hidden = new HashSet<>();
        Map<String, String> result = new HashMap<String, String>();
        if (propertyConfig != null) {
            for (Map.Entry<String, CmsXmlContentProperty> entry : propertyConfig.entrySet()) {
                CmsXmlContentProperty prop = entry.getValue();
                String value = getPropValueIds(cms, prop.getType(), prop.getDefault());
                if (CmsGwtConstants.HIDDEN_SETTINGS_WIDGET_NAME.equals(prop.getWidget())) {
                    hidden.add(entry.getKey());
                }
                if (value != null) {
                    result.put(entry.getKey(), value);
                }
            }
        }
        properties.forEach((key, value) -> {
            if (!hidden.contains(key)) {
                result.put(key, value);
            } else {
                // 'hidden' widget, but we still got a setting value. The setting value is probably left over
                // from a previous formatter - ignore it. This can happen with list display formatters, because
                // they are selected in the list configuration content, and editing the content itself doesn't affect
                // the settings of the corresponding container page element(s).
                if (!Objects.equal(value, result.get(key))) {
                    LOG.info(
                        "Discarding setting value because configured widget is 'hidden': key = "
                            + key
                            + ", value = "
                            + value
                            + ", original value = "
                            + result.get(key));
                }
            }
        });

        return result;
    }

    /**
     * Reads property nodes from the given location.<p>
     *
     * @param cms the current cms context
     * @param baseLocation the base location
     *
     * @return the properties
     */
    public static Map<String, String> readProperties(CmsObject cms, I_CmsXmlContentLocation baseLocation) {

        Map<String, String> result = new HashMap<String, String>();
        String elementName = CmsXmlContentProperty.XmlNode.Properties.name();
        String nameElementName = CmsXmlContentProperty.XmlNode.Name.name();
        List<I_CmsXmlContentValueLocation> propertyLocations = baseLocation.getSubValues(elementName);
        for (I_CmsXmlContentValueLocation propertyLocation : propertyLocations) {
            I_CmsXmlContentValueLocation nameLocation = propertyLocation.getSubValue(nameElementName);
            String name = nameLocation.asString(cms).trim();
            String value = null;
            I_CmsXmlContentValueLocation valueLocation = propertyLocation.getSubValue(
                CmsXmlContentProperty.XmlNode.Value.name());
            I_CmsXmlContentValueLocation stringLocation = valueLocation.getSubValue(
                CmsXmlContentProperty.XmlNode.String.name());
            I_CmsXmlContentValueLocation fileListLocation = valueLocation.getSubValue(
                CmsXmlContentProperty.XmlNode.FileList.name());
            if (stringLocation != null) {
                value = stringLocation.asString(cms).trim();
            } else if (fileListLocation != null) {
                List<CmsUUID> idList = new ArrayList<CmsUUID>();
                List<I_CmsXmlContentValueLocation> fileLocations = fileListLocation.getSubValues(
                    CmsXmlContentProperty.XmlNode.Uri.name());
                for (I_CmsXmlContentValueLocation fileLocation : fileLocations) {
                    CmsUUID structureId = fileLocation.asId(cms);
                    idList.add(structureId);
                }
                value = CmsStringUtil.listAsString(idList, CmsXmlContentProperty.PROP_SEPARATOR);
            }
            if (value != null) {
                result.put(name, value);
            }
        }
        return result;
    }

    /**
     * Reads the properties from property-enabled xml content values.<p>
     *
     * @param xmlContent the xml content
     * @param locale the current locale
     * @param element the xml element
     * @param elemPath the xpath
     * @param elemDef the element definition
     *
     * @return the read property map
     *
     * @see org.opencms.xml.containerpage.CmsXmlContainerPage.XmlNode#Elements
     */
    public static Map<String, String> readProperties(
        CmsXmlContent xmlContent,
        Locale locale,
        Element element,
        String elemPath,
        CmsXmlContentDefinition elemDef) {

        Map<String, String> propertiesMap = new HashMap<String, String>();
        // Properties
        for (Iterator<Element> itProps = CmsXmlGenericWrapper.elementIterator(
            element,
            CmsXmlContentProperty.XmlNode.Properties.name()); itProps.hasNext();) {
            Element property = itProps.next();

            // property itself
            int propIndex = CmsXmlUtils.getXpathIndexInt(property.getUniquePath(element));
            String propPath = CmsXmlUtils.concatXpath(
                elemPath,
                CmsXmlUtils.createXpathElement(property.getName(), propIndex));
            I_CmsXmlSchemaType propSchemaType = elemDef.getSchemaType(property.getName());
            I_CmsXmlContentValue propValue = propSchemaType.createValue(xmlContent, property, locale);
            xmlContent.addBookmarkForValue(propValue, propPath, locale, true);
            CmsXmlContentDefinition propDef = ((CmsXmlNestedContentDefinition)propSchemaType).getNestedContentDefinition();

            // name
            Element propName = property.element(CmsXmlContentProperty.XmlNode.Name.name());
            xmlContent.addBookmarkForElement(propName, locale, property, propPath, propDef);

            // choice value
            Element value = property.element(CmsXmlContentProperty.XmlNode.Value.name());
            if (value == null) {
                // this can happen when adding the elements node to the xml content
                continue;
            }
            int valueIndex = CmsXmlUtils.getXpathIndexInt(value.getUniquePath(property));
            String valuePath = CmsXmlUtils.concatXpath(
                propPath,
                CmsXmlUtils.createXpathElement(value.getName(), valueIndex));
            I_CmsXmlSchemaType valueSchemaType = propDef.getSchemaType(value.getName());
            I_CmsXmlContentValue valueValue = valueSchemaType.createValue(xmlContent, value, locale);
            xmlContent.addBookmarkForValue(valueValue, valuePath, locale, true);
            CmsXmlContentDefinition valueDef = ((CmsXmlNestedContentDefinition)valueSchemaType).getNestedContentDefinition();

            String val = null;
            Element string = value.element(CmsXmlContentProperty.XmlNode.String.name());
            if (string != null) {
                // string value
                xmlContent.addBookmarkForElement(string, locale, value, valuePath, valueDef);
                val = string.getTextTrim();
            } else {
                // file list value
                Element valueFileList = value.element(CmsXmlContentProperty.XmlNode.FileList.name());
                if (valueFileList == null) {
                    // this can happen when adding the elements node to the xml content
                    continue;
                }
                int valueFileListIndex = CmsXmlUtils.getXpathIndexInt(valueFileList.getUniquePath(value));
                String valueFileListPath = CmsXmlUtils.concatXpath(
                    valuePath,
                    CmsXmlUtils.createXpathElement(valueFileList.getName(), valueFileListIndex));
                I_CmsXmlSchemaType valueFileListSchemaType = valueDef.getSchemaType(valueFileList.getName());
                I_CmsXmlContentValue valueFileListValue = valueFileListSchemaType.createValue(
                    xmlContent,
                    valueFileList,
                    locale);
                xmlContent.addBookmarkForValue(valueFileListValue, valueFileListPath, locale, true);
                CmsXmlContentDefinition valueFileListDef = ((CmsXmlNestedContentDefinition)valueFileListSchemaType).getNestedContentDefinition();

                List<CmsUUID> idList = new ArrayList<CmsUUID>();
                // files
                for (Iterator<Element> itFiles = CmsXmlGenericWrapper.elementIterator(
                    valueFileList,
                    CmsXmlContentProperty.XmlNode.Uri.name()); itFiles.hasNext();) {

                    Element valueUri = itFiles.next();
                    xmlContent.addBookmarkForElement(
                        valueUri,
                        locale,
                        valueFileList,
                        valueFileListPath,
                        valueFileListDef);
                    Element valueUriLink = valueUri.element(CmsXmlPage.NODE_LINK);
                    CmsUUID fileId = null;
                    if (valueUriLink == null) {
                        // this can happen when adding the elements node to the xml content
                        // it is not dangerous since the link has to be set before saving
                    } else {
                        fileId = new CmsLink(valueUriLink).getStructureId();
                        idList.add(fileId);
                    }
                }
                // comma separated list of UUIDs
                val = CmsStringUtil.listAsString(idList, CmsXmlContentProperty.PROP_SEPARATOR);
            }

            propertiesMap.put(propName.getTextTrim(), val);
        }
        return propertiesMap;
    }

    /**
     * Resolves macros in the given property information for the given resource (type) AND the current user.<p>
     *
     * @param cms the current CMS context
     * @param page the current container page
     * @param resource the resource
     * @param contentGetter loads the actual content
     * @param stringtemplateSource provider for stringtemplate templates
     * @param propertiesConf the property information
     *
     * @return the property information
     *
     * @throws CmsException if something goes wrong
     */
    public static Map<String, CmsXmlContentProperty> resolveMacrosForPropertyInfo(
        CmsObject cms,
        CmsResource page,
        CmsResource resource,
        Supplier<CmsXmlContent> contentGetter,
        Function<String, String> stringtemplateSource,
        Map<String, CmsXmlContentProperty> propertiesConf)
    throws CmsException {

        if (CmsResourceTypeXmlContent.isXmlContent(resource)) {
            I_CmsXmlContentHandler contentHandler = CmsXmlContentDefinition.getContentHandlerForResource(cms, resource);
            CmsMacroResolver resolver = getMacroResolverForProperties(
                cms,
                contentHandler,
                contentGetter.get(),
                stringtemplateSource,
                page);
            return resolveMacrosInProperties(propertiesConf, resolver);
        }
        return propertiesConf;
    }

    /**
     * Resolves macros in all properties in a map.<p>
     *
     * @param properties the map of properties in which macros should be resolved
     * @param resolver the macro resolver to use
     *
     * @return a new map of properties with resolved macros
     */
    public static Map<String, CmsXmlContentProperty> resolveMacrosInProperties(
        Map<String, CmsXmlContentProperty> properties,
        I_CmsMacroResolver resolver) {

        Map<String, CmsXmlContentProperty> result = new LinkedHashMap<String, CmsXmlContentProperty>();
        for (Map.Entry<String, CmsXmlContentProperty> entry : properties.entrySet()) {
            String key = entry.getKey();
            CmsXmlContentProperty prop = entry.getValue();
            result.put(key, resolveMacrosInProperty(prop, resolver));
        }
        return result;
    }

    /**
     * Resolves the macros in a single property.<p>
     *
     * @param property the property in which macros should be resolved
     * @param resolver the macro resolver to use
     *
     * @return a new property with resolved macros
     */
    public static CmsXmlContentProperty resolveMacrosInProperty(
        CmsXmlContentProperty property,
        I_CmsMacroResolver resolver) {

        String propName = property.getName();
        CmsXmlContentProperty result = new CmsXmlContentProperty(
            propName,
            property.getType(),
            resolver.resolveMacros(property.getWidget()),
            resolver.resolveMacros(property.getWidgetConfiguration()),
            property.getRuleRegex(),
            property.getRuleType(),
            property.getDefault(),
            resolver.resolveMacros(property.getNiceName()),
            resolver.resolveMacros(property.getDescription()),
            resolver.resolveMacros(property.getError()),
            property.isPreferFolder() ? "true" : "false");
        result.m_visibility = property.m_visibility;
        return result;
    }

    /**
     * Saves the given properties to the given xml element.<p>
     *
     * @param cms the current CMS context
     * @param parentElement the parent xml element
     * @param properties the properties to save, if there is a list of resources, every entry can be a site path or a UUID
     * @param propertiesConf the configuration of the properties
     * @param sort if true, properties will be sorted by map keys via string co
     */
    public static void saveProperties(
        CmsObject cms,
        Element parentElement,
        Map<String, String> properties,
        Map<String, CmsXmlContentProperty> propertiesConf,
        boolean sort) {

        // remove old entries
        for (Object propElement : parentElement.elements(CmsXmlContentProperty.XmlNode.Properties.name())) {
            parentElement.remove((Element)propElement);
        }

        // use a sorted map to force a defined order
        Map<String, String> props;
        if (sort) {
            props = new TreeMap<String, String>(properties);
        } else {
            props = properties;
        }

        // create new entries
        for (Map.Entry<String, String> property : props.entrySet()) {
            String propName = property.getKey();
            String propValue = property.getValue();
            if ((propValue == null) || (propValue.length() == 0)) {
                continue;
            }
            // only if the property is configured in the schema we will save it
            Element propElement = parentElement.addElement(CmsXmlContentProperty.XmlNode.Properties.name());

            // the property name
            propElement.addElement(CmsXmlContentProperty.XmlNode.Name.name()).addCDATA(propName);
            Element valueElement = propElement.addElement(CmsXmlContentProperty.XmlNode.Value.name());
            boolean isVfs = false;
            CmsXmlContentProperty propDef = propertiesConf.get(propName);
            if (propDef != null) {
                isVfs = CmsXmlContentProperty.PropType.isVfsList(propDef.getType());
            }
            if (!isVfs) {
                // string value
                valueElement.addElement(CmsXmlContentProperty.XmlNode.String.name()).addCDATA(propValue);
            } else {
                addFileListPropertyValue(cms, valueElement, propValue);
            }
        }
    }

    /**
     * Adds the XML for a property value of a property of type 'vfslist' to the DOM.<p>
     *
     * @param cms the current CMS context
     * @param valueElement the element to which the vfslist property value should be added
     * @param propValue the property value which should be saved
     */
    protected static void addFileListPropertyValue(CmsObject cms, Element valueElement, String propValue) {

        // resource list value
        Element filelistElem = valueElement.addElement(CmsXmlContentProperty.XmlNode.FileList.name());
        for (String strId : CmsStringUtil.splitAsList(propValue, CmsXmlContentProperty.PROP_SEPARATOR)) {
            try {
                Element fileValueElem = filelistElem.addElement(CmsXmlContentProperty.XmlNode.Uri.name());
                CmsVfsFileValueBean fileValue = getFileValueForIdOrUri(cms, strId);
                // HACK: here we assume weak relations, but it would be more robust to check it, with smth like:
                // type = xmlContent.getContentDefinition().getContentHandler().getRelationType(fileValueElem.getPath());
                CmsRelationType type = CmsRelationType.XML_WEAK;
                CmsXmlVfsFileValue.fillEntry(fileValueElem, fileValue.getId(), fileValue.getPath(), type);
            } catch (CmsException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
    }

    /**
     * Converts a string containing zero or more structure ids into a string containing the corresponding VFS paths.<p>
     *
     * @param cms the CmsObject to use for the VFS operations
     * @param value a string representation of a list of ids
     *
     * @return a string representation of a list of paths
     */
    protected static String convertIdsToPaths(CmsObject cms, String value) {

        if (value == null) {
            return null;
        }
        // represent vfslists as lists of path in JSON
        List<String> ids = CmsStringUtil.splitAsList(value, CmsXmlContentProperty.PROP_SEPARATOR);
        List<String> paths = new ArrayList<String>();
        for (String id : ids) {
            try {
                String path = getUriForId(cms, new CmsUUID(id));
                paths.add(path);
            } catch (Exception e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
                continue;
            }
        }
        return CmsStringUtil.listAsString(paths, CmsXmlContentProperty.PROP_SEPARATOR);
    }

    /**
     * Converts a string containing zero or more VFS paths into a string containing the corresponding structure ids.<p>
     *
     * @param cms the CmsObject to use for the VFS operations
     * @param value a string representation of a list of paths
     *
     * @return a string representation of a list of ids
     */
    protected static String convertPathsToIds(CmsObject cms, String value) {

        if (value == null) {
            return null;
        }
        // represent vfslists as lists of path in JSON
        List<String> paths = CmsStringUtil.splitAsList(value, CmsXmlContentProperty.PROP_SEPARATOR);
        List<String> ids = new ArrayList<String>();
        for (String path : paths) {
            try {
                CmsUUID id = getIdForUri(cms, path);
                ids.add(id.toString());
            } catch (CmsException e) {
                // should never happen
                LOG.error(e.getLocalizedMessage(), e);
                continue;
            }
        }
        return CmsStringUtil.listAsString(ids, CmsXmlContentProperty.PROP_SEPARATOR);
    }

    /**
     * Helper method for converting a map of properties from client format to server format or vice versa.<p>
     *
     * @param cms the CmsObject to use for VFS operations
     * @param props the map of properties
     * @param propConfig the property configuration
     * @param toClient if true, convert from server to client, else from client to server
     *
     * @return the converted property map
     */
    protected static Map<String, String> convertProperties(
        CmsObject cms,
        Map<String, String> props,
        Map<String, CmsXmlContentProperty> propConfig,
        boolean toClient) {

        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : props.entrySet()) {
            String propName = entry.getKey();
            String propValue = entry.getValue();
            String type = "string";
            CmsXmlContentProperty configEntry = getPropertyConfig(propConfig, propName);
            if (configEntry != null) {
                type = configEntry.getType();
            }
            String newValue = convertStringPropertyValue(cms, propValue, type, toClient);
            result.put(propName, newValue);
        }
        return result;
    }

    /**
     * Converts a property value given as a string between server format and client format.<p>
     *
     * @param cms the current CMS context
     * @param propValue the property value to convert
     * @param type the type of the property
     * @param toClient if true, convert to client format, else convert to server format
     *
     * @return the converted property value
     */
    protected static String convertStringPropertyValue(CmsObject cms, String propValue, String type, boolean toClient) {

        if (propValue == null) {
            return null;
        }
        if (toClient) {
            return CmsXmlContentPropertyHelper.getPropValuePaths(cms, type, propValue);
        } else {
            return CmsXmlContentPropertyHelper.getPropValueIds(cms, type, propValue);
        }
    }

    /**
     * Given a string which might be a id or a (sitemap or VFS) URI, this method will return
     * a bean containing the right (sitemap or vfs) root path and (sitemap entry or structure) id.<p>
     *
     * @param cms the current CMS context
     * @param idOrUri a string containing an id or an URI
     *
     * @return a bean containing a root path and an id
     *
     * @throws CmsException if something goes wrong
     */
    protected static CmsVfsFileValueBean getFileValueForIdOrUri(CmsObject cms, String idOrUri) throws CmsException {

        CmsVfsFileValueBean result;
        if (CmsUUID.isValidUUID(idOrUri)) {
            CmsUUID id = new CmsUUID(idOrUri);
            String uri = getUriForId(cms, id);
            result = new CmsVfsFileValueBean(cms.getRequestContext().addSiteRoot(uri), id);
        } else {
            String uri = idOrUri;
            CmsUUID id = getIdForUri(cms, idOrUri);
            result = new CmsVfsFileValueBean(cms.getRequestContext().addSiteRoot(uri), id);
        }
        return result;

    }

    /**
     * Helper method for accessing the property configuration for a single property.<p>
     *
     * This method uses the base name of the property to access the property configuration,
     * i.e. if propName starts with a '#', the part after the '#' will be used as the key for
     * the property configuration.<p>
     *
     * @param propertyConfig the property configuration map
     * @param propName the name of a property
     * @return the property configuration for the given property name
     */
    protected static CmsXmlContentProperty getPropertyConfig(
        Map<String, CmsXmlContentProperty> propertyConfig,
        String propName) {

        return propertyConfig.get(propName);
    }

}
