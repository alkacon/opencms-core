/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContentProperty.java,v $
 * Date   : $Date: 2010/01/26 15:09:47 $
 * Version: $Revision: 1.8 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsMacroResolver;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.CmsXmlContentDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Contains the property configuration for a container-page element.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.8 $
 * 
 * @since 7.9.2
 */
public class CmsXmlContentProperty implements Cloneable {

    /** Property type constants. */
    public enum PropType {
        /** Property type constant string. */
        string,
        /** Property type constant VFS list. */
        vfslist;

        /**
         * Checks if the given type is {@link #vfslist}.<p>
         * 
         * @param type the type to check
         * 
         * @return <code>true</code> if the given type is {@link #vfslist}
         */
        public static boolean isVfsList(String type) {

            if (type == null) {
                return false;
            }
            return valueOf(type.toLowerCase()) == vfslist;
        }
    }

    /** XML node name constants. */
    public enum XmlNode {

        /** Value file list node name. */
        FileList,
        /** Container or property name node name. */
        Name,
        /** Element properties node name. */
        Properties,
        /** Value string node name. */
        String,
        /** File list URI node name. */
        Uri,
        /** Property value node name. */
        Value;
    }

    /** IDs separator constant. */
    public static final String PROP_SEPARATOR = ",";

    /** IDs separator constant. */
    public static final String CONF_KEYVALUE_SEPARATOR = ":";

    /** IDs separator constant. */
    public static final String CONF_PARAM_SEPARATOR = "\\|";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsXmlContentProperty.class);

    /** Default value. */
    private String m_default;

    /** The description. */
    private String m_description;

    /** The error message. */
    private String m_error;

    /** The nice name. */
    private String m_niceName;

    /** The name of the property. */
    private String m_propertyName;

    /** The property type. */
    private String m_propertyType;

    /** The validation rule regex. */
    private String m_ruleRegex;

    /** The validation rule type. */
    private String m_ruleType;

    /** The widget to use in the editor. */
    private String m_widget;

    /** The widget configuration. */
    private String m_widgetConfiguration;

    /**
     * Public constructor.
     * 
     * @param propertyName the property name
     * @param propertyType the property type (string|uri)
     * @param widget the widget
     * @param widgetConfiguration the widget configuration
     * @param ruleRegex the validation rule regex
     * @param ruleType the validation rule type
     * @param default1 the default value
     * @param niceName the nice-name
     * @param description  the description
     * @param error the error message
     */
    public CmsXmlContentProperty(
        String propertyName,
        String propertyType,
        String widget,
        String widgetConfiguration,
        String ruleRegex,
        String ruleType,
        String default1,
        String niceName,
        String description,
        String error) {

        super();
        m_propertyName = propertyName;
        m_propertyType = propertyType;
        m_widget = widget;
        m_widgetConfiguration = widgetConfiguration;
        m_ruleRegex = ruleRegex;
        m_ruleType = ruleType;
        m_default = default1;
        m_niceName = niceName;
        m_description = description;
        m_error = error;
    }

    /**
     * Returns a converted property value depending on the given type.<p>
     * 
     * If the type is {@link PropType#vfslist}, the value is parsed as a 
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
     * If the type is {@link PropType#vfslist}, the value is parsed as a 
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
        List<String> ids = CmsStringUtil.splitAsList(value, PROP_SEPARATOR);
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
                buffer.append(PROP_SEPARATOR);
            }
            // don't include last comma (which exists since ids.size() isn't zero)  
            result = buffer.substring(0, buffer.length() - PROP_SEPARATOR.length());
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
        List<String> paths = CmsStringUtil.splitAsList(value, PROP_SEPARATOR);
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
                buffer.append(PROP_SEPARATOR);
            }
            // don't include last comma (which exists since ids.size() isn't zero)  
            result = buffer.substring(0, buffer.length() - PROP_SEPARATOR.length());
        }
        return result;

    }

    /**
     * 
     * @see java.lang.Object#clone()
     */
    @Override
    public CmsXmlContentProperty clone() {

        return new CmsXmlContentProperty(
            m_propertyName,
            m_propertyType,
            m_widget,
            m_widgetConfiguration,
            m_ruleRegex,
            m_ruleType,
            m_default,
            m_niceName,
            m_description,
            m_error);
    }

    /**
     * Returns the default.<p>
     *
     * @return the default
     */
    public String getDefault() {

        return m_default;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the error.<p>
     *
     * @return the error
     */
    public String getError() {

        return m_error;
    }

    /**
     * Returns the niceName.<p>
     *
     * @return the niceName
     */
    public String getNiceName() {

        return m_niceName;
    }

    /**
     * Returns the propertyName.<p>
     *
     * @return the propertyName
     */
    public String getPropertyName() {

        return m_propertyName;
    }

    /**
     * Returns the propertyType.<p>
     *
     * @return the propertyType
     */
    public String getPropertyType() {

        return m_propertyType;
    }

    /**
     * Returns the ruleRegex.<p>
     *
     * @return the ruleRegex
     */
    public String getRuleRegex() {

        return m_ruleRegex;
    }

    /**
     * Returns the ruleType.<p>
     *
     * @return the ruleType
     */
    public String getRuleType() {

        return m_ruleType;
    }

    /**
     * Returns the widget.<p>
     *
     * @return the widget
     */
    public String getWidget() {

        return m_widget;
    }

    /**
     * Returns the widgetConfiguration.<p>
     *
     * @return the widgetConfiguration
     */
    public String getWidgetConfiguration() {

        return m_widgetConfiguration;
    }

    /**
     * Returns the widget configuration string parsed into a JSONObject.<p>
     * 
     * The configuration string should be a map of key value pairs separated by ':' and '|': KEY_1:VALUE_1|KEY_2:VALUE_2 ...
     * 
     * @param cms the current CmsObject instance
     * @param messages the messages used to resolve macros
     * @return the configuration JSON
     */
    public JSONObject getWidgetConfigurationAsJSON(CmsObject cms, CmsMessages messages) {

        String conf = CmsMacroResolver.resolveMacros(m_widgetConfiguration, cms, messages);
        JSONObject result = new JSONObject();
        if (!CmsStringUtil.isEmptyOrWhitespaceOnly(conf)) {
            String[] confEntries = conf.split(CONF_PARAM_SEPARATOR);
            for (int i = 0; i < confEntries.length; i++) {

                try {
                    String entry = confEntries[i];
                    String key, value;
                    if (entry.contains(CONF_KEYVALUE_SEPARATOR)) {
                        key = entry.substring(0, entry.indexOf(CONF_KEYVALUE_SEPARATOR));
                        value = entry.substring(entry.indexOf(CONF_KEYVALUE_SEPARATOR));
                    } else {
                        key = entry;
                        value = "";
                    }
                    result.put(key, value);
                } catch (Exception e) {
                    LOG.error(Messages.get().container(
                        Messages.ERR_XMLCONTENT_UNKNOWN_ELEM_PATH_SCHEMA_1,
                        m_propertyName), e);
                }
            }

        }
        return result;
    }

    /**
     * Sets the default.<p>
     *
     * @param default1 the default to set
     */
    public void setDefault(String default1) {

        m_default = default1;
    }

    /**
     * Sets the description.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the error.<p>
     *
     * @param error the error to set
     */
    public void setError(String error) {

        m_error = error;
    }

    /**
     * Sets the niceName.<p>
     *
     * @param niceName the niceName to set
     */
    public void setNiceName(String niceName) {

        m_niceName = niceName;
    }

    /**
     * Sets the propertyName.<p>
     *
     * @param propertyName the propertyName to set
     */
    public void setPropertyName(String propertyName) {

        m_propertyName = propertyName;
    }

    /**
     * Sets the propertyType.<p>
     *
     * @param propertyType the propertyType to set
     */
    public void setPropertyType(String propertyType) {

        m_propertyType = propertyType;
    }

    /**
     * Sets the ruleRegex.<p>
     *
     * @param ruleRegex the ruleRegex to set
     */
    public void setRuleRegex(String ruleRegex) {

        m_ruleRegex = ruleRegex;
    }

    /**
     * Sets the ruleType.<p>
     *
     * @param ruleType the ruleType to set
     */
    public void setRuleType(String ruleType) {

        m_ruleType = ruleType;
    }

    /**
     * Sets the widget.<p>
     *
     * @param widget the widget to set
     */
    public void setWidget(String widget) {

        m_widget = widget;
    }

    /**
     * Sets the widgetConfiguration.<p>
     *
     * @param widgetConfiguration the widgetConfiguration to set
     */
    public void setWidgetConfiguration(String widgetConfiguration) {

        m_widgetConfiguration = widgetConfiguration;
    }

}
