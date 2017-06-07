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

package org.opencms.workplace;

import org.opencms.widgets.CmsInputWidget;
import org.opencms.widgets.I_CmsWidget;

import java.lang.reflect.Constructor;

/**
 * Represents an user additional information entry.<p>
 *
 * @since 6.5.6
 */
public final class CmsWorkplaceUserInfoEntry {

    /** The entry's key. */
    private final String m_key;

    /** If entry is optional. */
    private final String m_optional;

    /** The widget. */
    private final String m_widget;

    /** The class name of the stored data type. */
    private String m_type;

    /** The widget parameters. */
    private String m_params;

    /**
     * Default constructor.<p>
     *
     * @param key the key for this entry
     * @param type the class name of the stored data type
     * @param widget the widget to use
     * @param params the widget parameters
     * @param optional if this entry is optional
     */
    public CmsWorkplaceUserInfoEntry(String key, String type, String widget, String params, String optional) {

        m_key = key;
        m_type = type;
        m_widget = widget;
        m_params = params;
        m_optional = optional;
    }

    /**
     * Returns the key.<p>
     *
     * @return the key
     */
    public String getKey() {

        return m_key;
    }

    /**
     * Returns the widget class name.<p>
     *
     * @return the widget class name
     */
    public String getWidget() {

        return m_widget;
    }

    /**
     * Returns a new widget object.<p>
     *
     * @return a new widget object
     */
    public I_CmsWidget getWidgetObject() {

        if (getWidget() == null) {
            if (m_params == null) {
                return new CmsInputWidget();
            } else {
                return new CmsInputWidget(m_params);
            }
        }
        try {
            if (m_params == null) {
                return (I_CmsWidget)Class.forName(getWidget()).newInstance();
            }
            Class<?> clazz = Class.forName(getWidget());
            Constructor<?> ctor = clazz.getConstructor(new Class[] {String.class});
            return (I_CmsWidget)ctor.newInstance(new Object[] {m_params});
        } catch (Exception e) {
            return new CmsInputWidget();
        }
    }

    /**
     * Returns the optional flag.<p>
     *
     * @return the optional flag
     */
    public String getOptional() {

        return m_optional;
    }

    /**
     * Returns the optional flag.<p>
     *
     * @return the optional flag
     */
    public boolean isOptional() {

        return Boolean.valueOf(m_optional).booleanValue();
    }

    /**
     * Returns the configured widget parameters.<p>
     *
     * @return the configured widget parameters
     */
    public String getParams() {

        return m_params;
    }

    /**
     * Returns the configured class name type.<p>
     *
     * @return the configured class name type
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the class type.<p>
     *
     * @return the class type
     */
    public Class<?> getClassType() {

        if (m_type == null) {
            return String.class;
        }
        try {
            return Class.forName(m_type);
        } catch (ClassNotFoundException e) {
            return String.class;
        }
    }
}