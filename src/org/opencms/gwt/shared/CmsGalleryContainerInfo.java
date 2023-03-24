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

package org.opencms.gwt.shared;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ComparisonChain;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Holds information about current container types/sizes for the purpose of filtering gallery search results (currently only for functions).
 */
public class CmsGalleryContainerInfo implements IsSerializable {

    /**
     * A single container type/width combination.
     */
    public static class Item implements IsSerializable, Comparable<Item> {

        /** The container type. */
        private String m_type;

        /** The container width. */
        private int m_width;

        /**
         * Creates a new instance.
         *
         * @param type the container type
         * @param width the container width
         */
        public Item(String type, int width) {

            super();
            m_type = type;
            m_width = width;
        }

        /**
         * Hidden default constructor for serialization
         */
        protected Item() {

            // do nothing
        }

        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Item other) {

            return ComparisonChain.start().compare(m_type, other.m_type).compare(m_width, other.m_width).result();
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {

            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Item other = (Item)obj;
            return Objects.equals(m_type, other.m_type) && (m_width == other.m_width);
        }

        /**
         * Gets the container type.
         *
         * @return the container type
         */
        public String getType() {

            return m_type;
        }

        /**
         * Gets the container width.
         *
         * @return the container width
         */
        public int getWidth() {

            return m_width;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {

            return Objects.hash(m_type, Integer.valueOf(m_width));
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {

            return "[type=" + m_type + ", width=" + m_width + "]";
        }
    }

    /** The set of type/width combinations of the page. */
    private TreeSet<Item> m_items;

    /**
     * Creates a new instance.
     *
     * @param items the set of type/width combinations of the page
     */
    public CmsGalleryContainerInfo(Set<Item> items) {

        super();
        m_items = new TreeSet<>(items);
    }

    /**
     * Hidden default constructor for serialization.
     */
    protected CmsGalleryContainerInfo() {}

    /**
     * Gets the type/width combinations of the page.
     *
     * @return the type/width combinations
     */
    public TreeSet<Item> getItems() {

        return m_items;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return "ContainerInfo[" + m_items + "]";
    }

}
