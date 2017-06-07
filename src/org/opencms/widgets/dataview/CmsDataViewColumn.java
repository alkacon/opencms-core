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

package org.opencms.widgets.dataview;

/**
 * This class represents the definition of a column for the table widget used to select data items provided by an I_CmsDataView implementation.<p>
 */
public class CmsDataViewColumn {

    /**
     * The column type.<p>
     */
    public static enum Type {

        /** Column contains a text to be displayed. */
        textType(String.class),

        /** Column contains a value of type Double. */
        doubleType(Double.class),

        /** Column contains a string representing an URL pointing to an image. */
        imageType(String.class),

        /** Column contains a value of type Boolean. */
        booleanType(Boolean.class);

        /** The value class of the column type. */
        private Class<?> m_class;

        /**
         * Private constructor for types.<p>
         *
         * @param valueClass the value class
         */
        private Type(Class<?> valueClass) {
            m_class = valueClass;
        }

        /**
         * Gets the value class for this column type.<p>
         *
         * When calling getColumnData on an implementation of I_CmsDataViewItem for a given column, the returned object's class should match the
         * value class of the column's type.
         *
         * @return the value class of the type
         */
        public Class<?> getValueClass() {

            return m_class;
        }
    }

    /** The preferred width. */
    private int m_preferredWidth;

    /** The column id. */
    private String m_id;

    /** True if column should be sortable. */
    private boolean m_sortable;

    /** The column type. */
    private Type m_type;

    /** The nice name to be displayed to the user. */
    private String m_niceName;

    /**
     * Creates a new column definition.<p>
     *
     * @param id the column id (should be unique among the list of columns for an I_CmsDataView implementation)
     * @param type the column type
     * @param niceName the user-readable name of the column
     * @param sortable true if the column should be sortable
     * @param preferredWidth the preferred width of the column
     */
    public CmsDataViewColumn(String id, Type type, String niceName, boolean sortable, int preferredWidth) {
        m_type = type;
        m_id = id;
        m_sortable = sortable;
        m_niceName = niceName;
        m_preferredWidth = preferredWidth;
    }

    /**
     * Gets the column id.<p>
     *
     * The column id is not directly shown to the user; it is used as an internal identifier for the column and should be unique.
     *
     * @return the column id
     */
    public String getId() {

        return m_id;
    }

    /**
     * Gets the 'nice name' to display for this column in the table header.<p>
     *
     * @return the nice name
     */
    public String getNiceName() {

        return m_niceName;
    }

    /**
     * Gets the preferred width of the column in pixels.<p>
     *
     * @return the preferred width
     */
    public int getPreferredWidth() {

        return m_preferredWidth;
    }

    /**
     * Gets the column type.<p>
     *
     * @return the column type
     */
    public Type getType() {

        return m_type;
    }

    /**
     * Returns true if this column should be sortable.<p>
     *
     * @return true if the column should be sortable
     */
    public boolean isSortable() {

        return m_sortable;
    }

}
