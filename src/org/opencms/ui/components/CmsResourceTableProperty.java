/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.components;

import static org.opencms.workplace.Messages.GUI_LABEL_PROJECT_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_DATECREATED_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_DATEEXPIRED_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_DATELASTMODIFIED_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_DATERELEASED_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_LOCKEDBY_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_NAVTEXT_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_PERMISSIONS_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_SIZE_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_STATE_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_TITLE_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_TYPE_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_USERCREATED_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_USERLASTMODIFIED_0;

import org.opencms.db.CmsResourceState;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.google.common.collect.Maps;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;

/**
 * Class whose instances contain the static data needed for a table column.<p>
 */
public class CmsResourceTableProperty {

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_DATE_CREATED = new CmsResourceTableProperty(
        "PROPERTY_DATE_CREATED",
        String.class,
        null,
        GUI_INPUT_DATECREATED_0,
        true,
        0,
        150);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_DATE_EXPIRED = new CmsResourceTableProperty(
        "PROPERTY_DATE_EXPIRED",
        String.class,
        "-",
        GUI_INPUT_DATEEXPIRED_0,
        true,
        0,
        150);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_DATE_MODIFIED = new CmsResourceTableProperty(
        "PROPERTY_DATE_MODIFIED",
        String.class,
        null,
        GUI_INPUT_DATELASTMODIFIED_0,
        true,
        0,
        150);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_DATE_RELEASED = new CmsResourceTableProperty(
        "PROPERTY_DATE_RELEASED",
        String.class,
        "-",
        GUI_INPUT_DATERELEASED_0,
        true,
        0,
        150);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_IS_FOLDER = new CmsResourceTableProperty(
        "PROPERTY_IS_FOLDER",
        Boolean.class,
        null,
        null,
        true,
        0,
        0);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_NAVIGATION_TEXT = new CmsResourceTableProperty(
        "PROPERTY_NAVIGATION_TEXT",
        String.class,
        null,
        GUI_INPUT_NAVTEXT_0,
        true,
        2,
        0);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_PERMISSIONS = new CmsResourceTableProperty(
        "PROPERTY_PERMISSIONS",
        String.class,
        null,
        GUI_INPUT_PERMISSIONS_0,
        true,
        0,
        100);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_PROJECT = new CmsResourceTableProperty(
        "PROPERTY_PROJECT",
        Component.class,
        null,
        GUI_LABEL_PROJECT_0,
        true,
        0,
        32);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_INSIDE_PROJECT = new CmsResourceTableProperty(
        "PROPERTY_INSIDE_PROJECT",
        Boolean.class,
        Boolean.TRUE,
        null,
        true,
        0,
        0);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_RESOURCE_NAME = new CmsResourceTableProperty(
        "PROPERTY_RESOURCE_NAME",
        String.class,
        null,
        GUI_INPUT_NAME_0,
        false,
        2,
        0);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_RESOURCE_TYPE = new CmsResourceTableProperty(
        "PROPERTY_RESOURCE_TYPE",
        String.class,
        null,
        GUI_INPUT_TYPE_0,
        true,
        0,
        0);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_SITE_PATH = new CmsResourceTableProperty(
        "PROPERTY_SITE_PATH",
        String.class,
        null,
        null,
        true,
        0,
        0);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_SIZE = new CmsResourceTableProperty(
        "PROPERTY_SIZE",
        Integer.class,
        null,
        GUI_INPUT_SIZE_0,
        true,
        0,
        100);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_STATE = new CmsResourceTableProperty(
        "PROPERTY_STATE",
        CmsResourceState.class,
        null,
        null,
        true,
        0,
        0);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_STATE_NAME = new CmsResourceTableProperty(
        "PROPERTY_STATE_NAME",
        String.class,
        null,
        GUI_INPUT_STATE_0,
        true,
        0,
        100);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_TITLE = new CmsResourceTableProperty(
        "PROPERTY_TITLE",
        String.class,
        null,
        GUI_INPUT_TITLE_0,
        true,
        2,
        0);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_TYPE_ICON = new CmsResourceTableProperty(
        "PROPERTY_TYPE_ICON",
        Component.class,
        null,
        "",
        false,
        0,
        40);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_TYPE_ICON_RESOURCE = new CmsResourceTableProperty(
        "PROPERTY_TYPE_ICON_RESOURCE",
        Resource.class,
        null,
        "",
        false,
        0,
        40);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_USER_CREATED = new CmsResourceTableProperty(
        "PROPERTY_USER_CREATED",
        String.class,
        null,
        GUI_INPUT_USERCREATED_0,
        true,
        0,
        0);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_USER_LOCKED = new CmsResourceTableProperty(
        "PROPERTY_USER_LOCKED",
        String.class,
        null,
        GUI_INPUT_LOCKEDBY_0,
        true,
        0,
        0);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_USER_MODIFIED = new CmsResourceTableProperty(
        "PROPERTY_USER_MODIFIED",
        String.class,
        null,
        GUI_INPUT_USERLASTMODIFIED_0,
        true,
        0,
        0);

    /** Map to keep track of default columns by name. */
    private static Map<String, CmsResourceTableProperty> m_columnsByName;

    /** The column collapsible flag. */
    private boolean m_collapsible;

    /** The column type. */
    private Class<?> m_columnType;

    /** The column width. */
    private int m_columnWidth;

    /** Default value for the column. */
    private Object m_defaultValue;

    /** The column expand ratio. */
    private float m_expandRatio;

    /** The message key for the column header. */
    private String m_headerKey;

    /** The column id. */
    private String m_id;

    /**
     * Creates a new instance.<p>
     *
     * @param id the id (should be unique)
     * @param columnType the column type
     * @param defaultValue the default value
     * @param headerKey the message key for the header
     * @param collapsible the column collapsible flag
     * @param expandRation the column expand ratio
     * @param columnWidth the column width
     */
    public CmsResourceTableProperty(
        String id,
        Class<?> columnType,
        Object defaultValue,
        String headerKey,
        boolean collapsible,
        float expandRation,
        int columnWidth) {
        m_id = id;
        m_columnType = columnType;
        m_defaultValue = defaultValue;
        m_headerKey = headerKey;
        m_collapsible = collapsible;
        m_expandRatio = expandRation;
        m_columnWidth = columnWidth;
    }

    /**
     * Gets the list of default columns.<p>
     *
     * @return the default columns
     */
    public static List<CmsResourceTableProperty> defaultProperties() {

        return Arrays.asList(
            PROPERTY_DATE_CREATED,
            PROPERTY_DATE_EXPIRED,
            PROPERTY_DATE_MODIFIED,
            PROPERTY_DATE_RELEASED,
            PROPERTY_IS_FOLDER,
            PROPERTY_NAVIGATION_TEXT,
            PROPERTY_PERMISSIONS,
            PROPERTY_RESOURCE_NAME,
            PROPERTY_RESOURCE_TYPE,
            PROPERTY_SIZE,
            PROPERTY_STATE,
            PROPERTY_STATE_NAME,
            PROPERTY_TITLE,
            PROPERTY_TYPE_ICON,
            PROPERTY_USER_CREATED,
            PROPERTY_USER_LOCKED,
            PROPERTY_USER_MODIFIED);
    }

    /**
     * Gets a map of default columns by name.<p>
     *
     * @return the default columns with their names as keys
     */
    public static Map<String, CmsResourceTableProperty> getDefaultColumnsByName() {

        if (m_columnsByName == null) {
            m_columnsByName = Maps.newHashMap();
            for (CmsResourceTableProperty column : defaultProperties()) {
                m_columnsByName.put(column.getId(), column);
            }

        }
        return Collections.unmodifiableMap(m_columnsByName);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {

        return (other instanceof CmsResourceTableProperty) && ((CmsResourceTableProperty)other).m_id.equals(m_id);
    }

    /**
     * Returns the columnType.<p>
     *
     * @return the columnType
     */
    public Class<?> getColumnType() {

        return m_columnType;
    }

    /**
     * Returns the column width.<p>
     *
     * @return the column width
     */
    public int getColumnWidth() {

        return m_columnWidth;
    }

    /**
     * Returns the defaultValue.<p>
     *
     * @return the defaultValue
     */
    public Object getDefaultValue() {

        return m_defaultValue;
    }

    /**
     * Returns the expand ratio.<p>
     *
     * @return the expand ratio
     */
    public float getExpandRatio() {

        return m_expandRatio;
    }

    /**
     * Returns the headerKey.<p>
     *
     * @return the headerKey
     */
    public String getHeaderKey() {

        return m_headerKey;
    }

    /**
     * Gets the id of the column.<p>
     *
     * @return the id
     */
    public String getId() {

        return m_id;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return m_id.hashCode();
    }

    /**
     * Returns the column collapsible flag.<p>
     *
     * @return the column collapsible flag
     */
    public boolean isCollapsible() {

        return m_collapsible;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this);
    }

}
