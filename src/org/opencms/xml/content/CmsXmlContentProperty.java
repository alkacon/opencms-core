/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/content/Attic/CmsXmlContentProperty.java,v $
 * Date   : $Date: 2009/10/12 15:24:29 $
 * Version: $Revision: 1.1.2.2 $
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

/**
 * Contains the property configuration for a container-page element.
 * 
 * @author Tobias Herrmann
 * @version $Revision: 1.0
 * 
 */
public class CmsXmlContentProperty {

    /** Property type constant uri. */
    public static final String T_URI = "uri";

    /** Property type constant string. */
    public static final String T_STRING = "string";

    /** The name of the property. */
    private String m_propertyName;

    /** The property type. */
    private String m_propertyType;

    /** The widget to use in the editor. */
    private String m_widget;

    /** The widget configuration. */
    private String m_widgetConfiguration;

    /** The validation rule regex. */
    private String m_ruleRegex;

    /** The validation rule type. */
    private String m_ruleType;

    /** Default value. */
    private String m_default;

    /** The nice name. */
    private String m_niceName;

    /** The description. */
    private String m_description;

    /** The error message. */
    private String m_error;

    /**
     * @param propertyName
     * @param propertyType
     * @param widget
     * @param widgetConfiguration
     * @param ruleRegex
     * @param ruleType
     * @param default1
     */
    public CmsXmlContentProperty(
        String propertyName,
        String propertyType,
        String widget,
        String widgetConfiguration,
        String ruleRegex,
        String ruleType,
        String default1) {

        super();
        m_propertyName = propertyName;
        m_propertyType = propertyType;
        m_widget = widget;
        m_widgetConfiguration = widgetConfiguration;
        m_ruleRegex = ruleRegex;
        m_ruleType = ruleType;
        m_default = default1;
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
     * Sets the propertyName.<p>
     *
     * @param propertyName the propertyName to set
     */
    public void setPropertyName(String propertyName) {

        m_propertyName = propertyName;
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
     * Sets the propertyType.<p>
     *
     * @param propertyType the propertyType to set
     */
    public void setPropertyType(String propertyType) {

        m_propertyType = propertyType;
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
     * Sets the widget.<p>
     *
     * @param widget the widget to set
     */
    public void setWidget(String widget) {

        m_widget = widget;
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
     * Sets the widgetConfiguration.<p>
     *
     * @param widgetConfiguration the widgetConfiguration to set
     */
    public void setWidgetConfiguration(String widgetConfiguration) {

        m_widgetConfiguration = widgetConfiguration;
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
     * Sets the ruleRegex.<p>
     *
     * @param ruleRegex the ruleRegex to set
     */
    public void setRuleRegex(String ruleRegex) {

        m_ruleRegex = ruleRegex;
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
     * Sets the ruleType.<p>
     *
     * @param ruleType the ruleType to set
     */
    public void setRuleType(String ruleType) {

        m_ruleType = ruleType;
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
     * Sets the default.<p>
     *
     * @param default1 the default to set
     */
    public void setDefault(String default1) {

        m_default = default1;
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
     * Sets the niceName.<p>
     *
     * @param niceName the niceName to set
     */
    public void setNiceName(String niceName) {

        m_niceName = niceName;
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
     * Sets the description.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
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
     * Sets the error.<p>
     *
     * @param error the error to set
     */
    public void setError(String error) {

        m_error = error;
    }

}
