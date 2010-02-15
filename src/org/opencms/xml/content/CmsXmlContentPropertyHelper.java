/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContentPropertyHelper.java,v $
 * Date   : $Date: 2010/02/15 08:53:23 $
 * Version: $Revision: 1.2 $
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

package org.opencms.xml.content;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.relations.CmsLink;
import org.opencms.relations.CmsRelationType;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.CmsXmlGenericWrapper;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.content.CmsXmlContentProperty.PropType;
import org.opencms.xml.page.CmsXmlPage;
import org.opencms.xml.types.CmsXmlNestedContentDefinition;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import org.dom4j.Element;

/**
 * Provides common methods on XML property configuration.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 7.9.2
 */
public final class CmsXmlContentPropertyHelper implements Cloneable {

    /** Element Property json property constants. */
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
     * Returns the property information for the given resource (type) as a JSON object.<p>
     * 
     * @param cms the current CMS context 
     * @param resource the resource
     * 
     * @return the property information
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong generating the JSON
     */
    public static JSONObject getPropertyInfoJSON(CmsObject cms, CmsResource resource)
    throws CmsException, JSONException {

        JSONObject jsonProperties = new JSONObject();

        I_CmsXmlContentHandler contentHandler = CmsXmlContentDefinition.getContentHandlerForResource(cms, resource);
        CmsUserSettings settings = new CmsUserSettings(cms.getRequestContext().currentUser());
        CmsMessages messages = contentHandler.getMessages(settings.getLocale());
        CmsMacroResolver resolver = new CmsMacroResolver();
        resolver.setCmsObject(cms);
        resolver.setMessages(messages);
        resolver.setKeepEmptyMacros(true);

        Map<String, CmsXmlContentProperty> propertiesConf = contentHandler.getProperties();
        Iterator<Map.Entry<String, CmsXmlContentProperty>> itProperties = propertiesConf.entrySet().iterator();
        while (itProperties.hasNext()) {
            Map.Entry<String, CmsXmlContentProperty> entry = itProperties.next();
            String propertyName = entry.getKey();
            CmsXmlContentProperty conf = entry.getValue();
            JSONObject jsonProperty = new JSONObject();

            jsonProperty.put(JsonProperty.defaultValue.name(), conf.getDefault());
            jsonProperty.put(JsonProperty.type.name(), conf.getPropertyType());
            jsonProperty.put(JsonProperty.widget.name(), conf.getWidget());
            jsonProperty.put(
                JsonProperty.widgetConf.name(),
                getWidgetConfigurationAsJSON(resolver.resolveMacros(conf.getWidgetConfiguration())));
            jsonProperty.put(JsonProperty.ruleType.name(), conf.getRuleType());
            jsonProperty.put(JsonProperty.ruleRegex.name(), conf.getRuleRegex());
            jsonProperty.put(JsonProperty.niceName.name(), resolver.resolveMacros(conf.getNiceName()));
            jsonProperty.put(JsonProperty.description.name(), resolver.resolveMacros(conf.getDescription()));
            jsonProperty.put(JsonProperty.error.name(), resolver.resolveMacros(conf.getError()));
            jsonProperties.put(propertyName, jsonProperty);
        }
        return jsonProperties;
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
                LOG.error(Messages.get().container(
                    Messages.ERR_XMLCONTENT_UNKNOWN_ELEM_PATH_SCHEMA_1,
                    widgetConfiguration), e);
            }
        }
        return result;
    }

    /**
     * Extends the given properties with the default values 
     * from the resource's property configuration.<p>
     * 
     * @param cms the current CMS context
     * @param resource the resource to get the property configuration from 
     * @param properties the properties to extend
     *  
     * @return a merged map of properties
     */
    public static Map<String, String> mergeDefaults(CmsObject cms, CmsResource resource, Map<String, String> properties) {

        Map<String, String> result = new HashMap<String, String>();
        try {
            Map<String, CmsXmlContentProperty> propertyConfig = CmsXmlContentDefinition.getContentHandlerForResource(
                cms,
                resource).getProperties();
            for (Map.Entry<String, CmsXmlContentProperty> entry : propertyConfig.entrySet()) {
                CmsXmlContentProperty prop = entry.getValue();
                result.put(entry.getKey(), getPropValueIds(cms, prop.getPropertyType(), prop.getDefault()));
            }
        } catch (CmsException e) {
            // should never happen
            LOG.error(e.getLocalizedMessage(), e);
        }
        result.putAll(properties);
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
     * @see org.opencms.xml.sitemap.CmsXmlSitemap.XmlNode#SiteEntry
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
            String propPath = CmsXmlUtils.concatXpath(elemPath, CmsXmlUtils.createXpathElement(
                property.getName(),
                propIndex));
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
            String valuePath = CmsXmlUtils.concatXpath(propPath, CmsXmlUtils.createXpathElement(
                value.getName(),
                valueIndex));
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
                String valueFileListPath = CmsXmlUtils.concatXpath(valuePath, CmsXmlUtils.createXpathElement(
                    valueFileList.getName(),
                    valueFileListIndex));
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
                    }
                    idList.add(fileId);
                }
                // comma separated list of UUIDs
                val = CmsStringUtil.listAsString(idList, CmsXmlContentProperty.PROP_SEPARATOR);
            }

            propertiesMap.put(propName.getTextTrim(), val);
        }
        return propertiesMap;
    }

    /**
     * Saves the given properties to the given xml element.<p>
     * 
     * @param cms the current CMS context
     * @param parentElement the parent xml element
     * @param propertiesConf the property configuration
     * @param properties the properties to save
     */
    public static void saveProperties(
        CmsObject cms,
        Element parentElement,
        Map<String, CmsXmlContentProperty> propertiesConf,
        Map<String, String> properties) {

        Element propElement = null;

        for (Map.Entry<String, String> property : properties.entrySet()) {
            String propName = property.getKey();
            String propValue = property.getValue();
            if (!propertiesConf.containsKey(propName) || (propValue == null)) {
                continue;
            }
            // only if the property is configured in the schema we will save it
            if (propElement == null) {
                propElement = parentElement.addElement(CmsXmlContentProperty.XmlNode.Properties.name());
            }

            // the property name
            propElement.addElement(CmsXmlContentProperty.XmlNode.Name.name()).addCDATA(propName);
            Element valueElement = propElement.addElement(CmsXmlContentProperty.XmlNode.Value.name());

            // the property value
            if (!CmsXmlContentProperty.PropType.isVfsList(propertiesConf.get(propName).getPropertyType())) {
                // string value
                valueElement.addElement(CmsXmlContentProperty.XmlNode.String.name()).addCDATA(propValue);
            } else {
                // resource list value
                Element filelistElem = valueElement.addElement(CmsXmlContentProperty.XmlNode.FileList.name());
                for (String strId : CmsStringUtil.splitAsList(propValue, CmsXmlContentProperty.PROP_SEPARATOR)) {
                    try {
                        Element fileValueElem = filelistElem.addElement(CmsXmlContentProperty.XmlNode.Uri.name());
                        CmsResource res = cms.readResource(new CmsUUID(strId));
                        // HACK: here we assume weak relations, but it would be more robust to check it
                        // type = xmlContent.getContentDefinition().getContentHandler().getRelationType(fileValueElem.getPath());
                        CmsRelationType type = CmsRelationType.XML_WEAK;
                        CmsXmlVfsFileValue.fillResource(fileValueElem, res, type);
                    } catch (CmsException e) {
                        // should never happen
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
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
    private static String convertIdsToPaths(CmsObject cms, String value) {

        if (value == null) {
            return null;
        }
        String result = "";
        // represent vfslists as lists of path in JSON
        List<String> ids = CmsStringUtil.splitAsList(value, CmsXmlContentProperty.PROP_SEPARATOR);
        StringBuffer buffer = new StringBuffer();
        if (ids.size() > 0) {
            for (String id : ids) {
                try {
                    CmsResource propResource = cms.readResource(new CmsUUID(id));
                    buffer.append(cms.getSitePath(propResource));
                } catch (Exception e) {
                    // should never happen
                    LOG.error(e.getLocalizedMessage(), e);
                    continue;
                }
                buffer.append(CmsXmlContentProperty.PROP_SEPARATOR);
            }
            // don't include last comma (which exists since ids.size() isn't zero)  
            result = buffer.substring(0, buffer.length() - CmsXmlContentProperty.PROP_SEPARATOR.length());
        }
        return result;

    }

    /**
     * Converts a string containing zero or more VFS paths into a string containing the corresponding structure ids.<p>
     *   
     * @param cms the CmsObject to use for the VFS operations 
     * @param value a string representation of a list of paths
     * 
     * @return a string representation of a list of ids
     */
    private static String convertPathsToIds(CmsObject cms, String value) {

        if (value == null) {
            return null;
        }
        String result = "";
        // represent vfslists as lists of path in JSON
        List<String> paths = CmsStringUtil.splitAsList(value, CmsXmlContentProperty.PROP_SEPARATOR);
        StringBuffer buffer = new StringBuffer();
        if (paths.size() > 0) {
            for (String path : paths) {
                try {
                    CmsResource propResource = cms.readResource(path);
                    buffer.append(propResource.getStructureId().toString());
                } catch (CmsException e) {
                    // should never happen
                    LOG.error(e.getLocalizedMessage(), e);
                    continue;
                }
                buffer.append(CmsXmlContentProperty.PROP_SEPARATOR);
            }
            // don't include last comma (which exists since ids.size() isn't zero)  
            result = buffer.substring(0, buffer.length() - CmsXmlContentProperty.PROP_SEPARATOR.length());
        }
        return result;
    }
}
