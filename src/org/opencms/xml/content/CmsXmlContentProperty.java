/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/CmsXmlContentProperty.java,v $
 * Date   : $Date: 2010/09/03 13:27:35 $
 * Version: $Revision: 1.12 $
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

import java.io.Serializable;

/**
 * XML property configuration.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.12 $
 * 
 * @since 7.9.2
 */
public class CmsXmlContentProperty implements Cloneable, Serializable {

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

    /** The serialization uid. */
    private static final long serialVersionUID = -4588082362096864995L;

    /** The value of the "advanced" property. */
    private String m_advanced;

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
     * Public constructor.<p>
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
     * @param advanced the "advanced" property 
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
        String error,
        String advanced) {

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
        m_advanced = advanced;
    }

    /**
     * Serialization constructor.<p>
     */
    protected CmsXmlContentProperty() {

        // empty
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
            m_error,
            m_advanced);
    }

    /**
     * Returns the value of the "advanced" property.<p>
     *
     * @return the "advanced" property 
     */
    public String getAdvanced() {

        return m_advanced;
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
