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

package org.opencms.ui.components;

import static org.opencms.workplace.Messages.GUI_LABEL_PROJECT_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_CACHE_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_COPYRIGHT_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_DATECREATED_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_DATEEXPIRED_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_DATELASTMODIFIED_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_DATERELEASED_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_LOCKEDBY_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_NAVTEXT_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_PATH_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_PERMISSIONS_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_SIZE_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_STATE_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_TITLE_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_TYPE_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_USERCREATED_0;
import static org.opencms.workplace.explorer.Messages.GUI_INPUT_USERLASTMODIFIED_0;

import org.opencms.db.CmsResourceState;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.CmsWorkplace;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

import com.google.common.collect.Maps;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Component;

/**
 * Class whose instances contain the static data needed for a table column.<p>
 */
public class CmsResourceTableProperty implements Serializable {

    /**
     * Converter for dates represented by their time stamp.<p>
     */
    public static class DateConverter implements Converter<String, Long> {

        /** The serial version id. */
        private static final long serialVersionUID = -54133335743460680L;

        /**
         * @see com.vaadin.data.util.converter.Converter#convertToModel(java.lang.Object, java.lang.Class, java.util.Locale)
         */
        public Long convertToModel(String value, Class<? extends Long> targetType, Locale locale)
        throws com.vaadin.data.util.converter.Converter.ConversionException {

            throw new UnsupportedOperationException();
        }

        /**
         * @see com.vaadin.data.util.converter.Converter#convertToPresentation(java.lang.Object, java.lang.Class, java.util.Locale)
         */
        public String convertToPresentation(Long value, Class<? extends String> targetType, Locale locale)
        throws com.vaadin.data.util.converter.Converter.ConversionException {

            return value != null
            ? CmsVaadinUtils.getWpMessagesForCurrentLocale().getDateTime(value.longValue())
            : CmsWorkplace.DEFAULT_DATE_STRING;
        }

        /**
         * @see com.vaadin.data.util.converter.Converter#getModelType()
         */
        public Class<Long> getModelType() {

            return Long.class;
        }

        /**
         * @see com.vaadin.data.util.converter.Converter#getPresentationType()
         */
        public Class<String> getPresentationType() {

            return String.class;
        }
    }

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_CACHE = new CmsResourceTableProperty(
        "PROPERTY_CACHE",
        CmsPropertyDefinition.PROPERTY_CACHE,
        String.class,
        null,
        GUI_INPUT_CACHE_0,
        true,
        2,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_COPYRIGHT = new CmsResourceTableProperty(
        "PROPERTY_COPYRIGHT",
        CmsPropertyDefinition.PROPERTY_COPYRIGHT,
        String.class,
        null,
        GUI_INPUT_COPYRIGHT_0,
        true,
        2,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_DATE_CREATED = new CmsResourceTableProperty(
        "PROPERTY_DATE_CREATED",
        Long.class,
        null,
        GUI_INPUT_DATECREATED_0,
        true,
        0,
        150,
        new DateConverter());

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_DATE_EXPIRED = new CmsResourceTableProperty(
        "PROPERTY_DATE_EXPIRED",
        Long.class,
        null,
        GUI_INPUT_DATEEXPIRED_0,
        true,
        0,
        150,
        new DateConverter());

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_DATE_MODIFIED = new CmsResourceTableProperty(
        "PROPERTY_DATE_MODIFIED",
        Long.class,
        null,
        GUI_INPUT_DATELASTMODIFIED_0,
        true,
        0,
        150,
        new DateConverter());

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_DATE_RELEASED = new CmsResourceTableProperty(
        "PROPERTY_DATE_RELEASED",
        Long.class,
        null,
        GUI_INPUT_DATERELEASED_0,
        true,
        0,
        150,
        new DateConverter());

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_DISABLED = new CmsResourceTableProperty(
        "PROPERTY_DISABLED",
        Boolean.class,
        Boolean.FALSE,
        "",
        true,
        0,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_INSIDE_PROJECT = new CmsResourceTableProperty(
        "PROPERTY_INSIDE_PROJECT",
        Boolean.class,
        Boolean.TRUE,
        null,
        true,
        0,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_IS_FOLDER = new CmsResourceTableProperty(
        "PROPERTY_IS_FOLDER",
        Boolean.class,
        null,
        null,
        true,
        0,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_NAVIGATION_POSITION = new CmsResourceTableProperty(
        "PROPERTY_NAVIGATION_POSITION",
        Float.class,
        null,
        null,
        true,
        0,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_NAVIGATION_TEXT = new CmsResourceTableProperty(
        "PROPERTY_NAVIGATION_TEXT",
        CmsPropertyDefinition.PROPERTY_NAVTEXT,
        String.class,
        null,
        GUI_INPUT_NAVTEXT_0,
        true,
        2,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_IN_NAVIGATION = new CmsResourceTableProperty(
        "PROPERTY_IN_NAVIGATION",
        Boolean.class,
        Boolean.FALSE,
        null,
        true,
        0,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_PERMISSIONS = new CmsResourceTableProperty(
        "PROPERTY_PERMISSIONS",
        String.class,
        null,
        GUI_INPUT_PERMISSIONS_0,
        true,
        0,
        100);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_PROJECT = new CmsResourceTableProperty(
        "PROPERTY_PROJECT",
        Component.class,
        null,
        GUI_LABEL_PROJECT_0,
        true,
        0,
        32);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_RELEASED_NOT_EXPIRED = new CmsResourceTableProperty(
        "PROPERTY_RELEASED_NOT_EXPIRED",
        Boolean.class,
        Boolean.TRUE,
        null,
        true,
        0,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_RESOURCE_NAME = new CmsResourceTableProperty(
        "PROPERTY_RESOURCE_NAME",
        String.class,
        null,
        GUI_INPUT_NAME_0,
        false,
        2,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_RESOURCE_TYPE = new CmsResourceTableProperty(
        "PROPERTY_RESOURCE_TYPE",
        String.class,
        null,
        GUI_INPUT_TYPE_0,
        true,
        0,
        180);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_SITE_PATH = new CmsResourceTableProperty(
        "PROPERTY_SITE_PATH",
        String.class,
        null,
        GUI_INPUT_PATH_0,
        true,
        0,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_SIZE = new CmsResourceTableProperty(
        "PROPERTY_SIZE",
        Integer.class,
        null,
        GUI_INPUT_SIZE_0,
        true,
        0,
        100);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_STATE = new CmsResourceTableProperty(
        "PROPERTY_STATE",
        CmsResourceState.class,
        null,
        null,
        true,
        0,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_STATE_NAME = new CmsResourceTableProperty(
        "PROPERTY_STATE_NAME",
        String.class,
        null,
        GUI_INPUT_STATE_0,
        true,
        0,
        100);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_TITLE = new CmsResourceTableProperty(
        "PROPERTY_TITLE",
        CmsPropertyDefinition.PROPERTY_TITLE,
        String.class,
        null,
        GUI_INPUT_TITLE_0,
        true,
        3,
        0);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_TREE_CAPTION = new CmsResourceTableProperty(
        "PROPERTY_TREE_CAPTION",
        String.class,
        null,
        "",
        false,
        0,
        40);

    /** Resoure table property. */
    public static final CmsResourceTableProperty PROPERTY_TYPE_ICON = new CmsResourceTableProperty(
        "PROPERTY_TYPE_ICON",
        Component.class,
        null,
        "",
        false,
        0,
        40);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_USER_CREATED = new CmsResourceTableProperty(
        "PROPERTY_USER_CREATED",
        String.class,
        null,
        GUI_INPUT_USERCREATED_0,
        true,
        0,
        150);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_USER_LOCKED = new CmsResourceTableProperty(
        "PROPERTY_USER_LOCKED",
        String.class,
        null,
        GUI_INPUT_LOCKEDBY_0,
        true,
        0,
        150);

    /** Resource table property. */
    public static final CmsResourceTableProperty PROPERTY_USER_MODIFIED = new CmsResourceTableProperty(
        "PROPERTY_USER_MODIFIED",
        String.class,
        null,
        GUI_INPUT_USERLASTMODIFIED_0,
        true,
        0,
        150);

    /** Map to keep track of default columns by name. */
    private static Map<String, CmsResourceTableProperty> m_columnsByName;

    /** The serial version id. */
    private static final long serialVersionUID = -8006568789417647500L;

    /** The column collapsible flag. */
    private boolean m_collapsible;

    /** The column type. */
    private Class<?> m_columnType;

    /** The column width. */
    private int m_columnWidth;

    /** The property to presentation string converter. */
    private Converter<String, ?> m_converter;

    /** Default value for the column. */
    private Object m_defaultValue;

    /** The editable property id. */
    private String m_editPropertyId;

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
        this(id, columnType, defaultValue, headerKey, collapsible, expandRation, columnWidth, null);
    }

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
     * @param converter the property converter
     */
    public CmsResourceTableProperty(
        String id,
        Class<?> columnType,
        Object defaultValue,
        String headerKey,
        boolean collapsible,
        float expandRation,
        int columnWidth,
        Converter<String, ?> converter) {
        m_id = id;
        m_columnType = columnType;
        m_defaultValue = defaultValue;
        m_headerKey = headerKey;
        m_collapsible = collapsible;
        m_expandRatio = expandRation;
        m_columnWidth = columnWidth;
        m_converter = converter;
    }

    /**
     * Creates a new instance.<p>
     *
     * @param id the id (should be unique)
     * @param editPropertyId the editable property id
     * @param columnType the column type
     * @param defaultValue the default value
     * @param headerKey the message key for the header
     * @param collapsible the column collapsible flag
     * @param expandRation the column expand ratio
     * @param columnWidth the column width
     */
    public CmsResourceTableProperty(
        String id,
        String editPropertyId,
        Class<?> columnType,
        Object defaultValue,
        String headerKey,
        boolean collapsible,
        float expandRation,
        int columnWidth) {
        this(id, columnType, defaultValue, headerKey, collapsible, expandRation, columnWidth, null);
        m_editPropertyId = editPropertyId;
    }

    /**
     * Gets the list of default columns.<p>
     *
     * @return the default columns
     */
    public static List<CmsResourceTableProperty> defaultProperties() {

        return Arrays.asList(
            PROPERTY_PROJECT,
            PROPERTY_DATE_CREATED,
            PROPERTY_DATE_EXPIRED,
            PROPERTY_DATE_MODIFIED,
            PROPERTY_DATE_RELEASED,
            PROPERTY_IS_FOLDER,
            PROPERTY_NAVIGATION_TEXT,
            PROPERTY_COPYRIGHT,
            PROPERTY_CACHE,
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
     * Returns the property converter.<p>
     *
     * @return the converter
     */
    public Converter<String, ?> getConverter() {

        return m_converter;
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
     * Returns the edit property id.<p>
     *
     * @return the edit property id
     */
    public String getEditPropertyId() {

        return m_editPropertyId;
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
     * Checks whether this is an edit property.<p>
     *
     * @return <code>true</code> if this is an edit property
     */
    public boolean isEditProperty() {

        return CmsStringUtil.isNotEmptyOrWhitespaceOnly(m_editPropertyId);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return ReflectionToStringBuilder.toString(this);
    }

}
