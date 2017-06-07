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

import org.opencms.util.CmsStringUtil;

import java.io.Serializable;

/**
 * Describes both VFS properties and Container Page Element settings, used by the GWT client.<p>
 *
 * Warning: This class is used by GWT client-side code (See GwtBase.gwt.xml for a list of
 * classes used by GWT client-side code). If you change this class, either make sure that
 * your changes are compatible with GWT, or write a separate client version of the class
 * and put it into super_src.
 *
 * @since 8.0.0
 */
public class CmsXmlContentProperty implements Serializable {

    /** Type constants. */
    public enum PropType {
        /** Type constant string. */
        string, /** Type constant VFS list. */
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
        FileList, /** Container or property name node name. */
        Name, /** Element properties node name. */
        Properties, /** Value string node name. */
        String, /** File list URI node name. */
        Uri, /** Value node name. */
        Value;
    }

    /** IDs separator constant. */
    public static final String PROP_SEPARATOR = ",";

    /** The serialization uid. */
    private static final long serialVersionUID = -7718747702874213381L;

    /** Default value. */
    private String m_default;

    /** The description. */
    private String m_description;

    /** The error message. */
    private String m_error;

    /** The name of the property. */
    private String m_name;

    /** The nice name. */
    private String m_niceName;

    /** The "prefer folder" option. */
    private String m_preferFolder;

    /** The validation rule regex. */
    private String m_ruleRegex;

    /** The validation rule type. */
    private String m_ruleType;

    /** The value which indicates whether the user can influence how this property is going to be inherited. */
    private String m_selectInherit;

    /** The property type. */
    private String m_type;

    /** The widget to use in the editor. */
    private String m_widget;

    /** The widget configuration. */
    private String m_widgetConfiguration;

    /**
     * Public constructor.<p>
     *
     * @param name the property name
     * @param type the property type (string|uri)
     * @param widget the widget
     * @param widgetConfiguration the widget configuration
     * @param ruleRegex the validation rule regex
     * @param ruleType the validation rule type
     * @param default1 the default value
     * @param niceName the nice-name
     * @param description  the description
     * @param error the error message
     * @param preferFolder the "prefer folder" option
     */
    public CmsXmlContentProperty(
        String name,
        String type,
        String widget,
        String widgetConfiguration,
        String ruleRegex,
        String ruleType,
        String default1,
        String niceName,
        String description,
        String error,
        String preferFolder

    ) {

        super();
        m_name = name;
        m_type = type;
        m_widget = widget;
        m_widgetConfiguration = widgetConfiguration;
        m_ruleRegex = ruleRegex;
        m_ruleType = ruleType;
        m_default = default1;
        m_niceName = niceName;
        m_description = description;
        m_error = error;
        m_preferFolder = preferFolder;
    }

    /**
     * Serialization constructor.<p>
     */
    protected CmsXmlContentProperty() {

        // empty
    }

    /**
     * Copies this property definition.<p>
     *
     * @return a new copy of the current property definition
     */
    public CmsXmlContentProperty copy() {

        return new CmsXmlContentProperty(
            m_name,
            m_type,
            m_widget,
            m_widgetConfiguration,
            m_ruleRegex,
            m_ruleType,
            m_default,
            m_niceName,
            m_description,
            m_error,
            m_preferFolder);
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
     * Returns the property name.<p>
     *
     * @return the property name
     */
    public String getName() {

        return m_name;
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
     * Returns the ruleRegex.<p>
     *
     * @return the ruleRegex
     */
    public String getRuleRegex() {

        return m_ruleRegex;
    }

    /**
     * Returns the rule type.<p>
     *
     * @return the rule type
     */
    public String getRuleType() {

        return m_ruleType;
    }

    /**
     * Returns a value which indicates whether the user can control the inheritance of this property.<p>
     *
     * @return the "select-inherit" property
     */
    public String getSelectInherit() {

        return m_selectInherit;
    }

    /**
     * Returns the property type.<p>
     *
     * @return the property type
     */
    public String getType() {

        return m_type;
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
     * Returns the value of the "prefer folder" option.<p>
     *
     * This flag determines whether a property entered in the sitemap entry editor should be stored by default at the folder
     * or at the default file of a sitemap entry. It only has an effect if the sitemap entry being edited doesn't already
     * have a value for that property at either location.<p>
     *
     * @return the "prefer folder" flag
     */
    public boolean isPreferFolder() {

        return (m_preferFolder == null) || Boolean.valueOf(m_preferFolder).booleanValue();

    }

    /**
     * Copies a property definition, but replaces an empty widget with a given widget.<p>
     *
     * @param defaultWidget the widget to use if the set widget is empty
     *
     * @return the copied property definition
     */
    public CmsXmlContentProperty withDefaultWidget(String defaultWidget) {

        return new CmsXmlContentProperty(
            m_name,
            m_type,
            CmsStringUtil.isEmptyOrWhitespaceOnly(m_widget) ? defaultWidget : m_widget,
            m_widgetConfiguration,
            m_ruleRegex,
            m_ruleType,
            m_default,
            m_niceName,
            m_description,
            m_error,
            m_preferFolder);
    }

    /**
     * Copies a property definition, but replaces the nice name attribute.<p>
     *
     * @param niceName the new nice name attribute
     *
     * @return the copied property definition
     */
    public CmsXmlContentProperty withNiceName(String niceName) {

        return new CmsXmlContentProperty(
            m_name,
            m_type,
            m_widget,
            m_widgetConfiguration,
            m_ruleRegex,
            m_ruleType,
            m_default,
            niceName,
            m_description,
            m_error,
            m_preferFolder);
    }

}
