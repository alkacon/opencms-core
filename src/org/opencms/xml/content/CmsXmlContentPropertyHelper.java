/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContentPropertyHelper.java,v $
 * Date   : $Date: 2010/01/27 08:20:23 $
 * Version: $Revision: 1.1 $
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
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;
import org.opencms.xml.content.CmsXmlContentProperty.PropType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Provides common methods on XML property configuration.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1 $
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
