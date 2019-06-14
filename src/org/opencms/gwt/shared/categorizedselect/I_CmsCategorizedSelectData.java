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

package org.opencms.gwt.shared.categorizedselect;

import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * Configuration for the client-side categorized select widget.
 */
public interface I_CmsCategorizedSelectData {

    /**
     * Represents a filter category in the categorized select widget (not necessarily an OpenCms category).
     */
    public interface Category {

        /**
         * Gets the internal value representing the filter category.
         *
         * @return the key
         */
        public String getKey();

        /**
         * Gets the label.
         *
         * @return the label
         */
        public String getLabel();

        /**
         * Sets the key.
         *
         * @param key the key.
         */
        public void setKey(String key);

        /**
         * Sets the label.
         *
         * @param label the new label
         */
        public void setLabel(String label);

    }

    /**
     * The Interface Option.
     */
    public interface Option {

        /**
         * Gets the categories.
         *
         * @return the categories
         */
        List<String> getCategories();

        /**
         * Gets the key.
         *
         * @return the key
         */
        String getKey();

        /**
         * Gets the label.
         *
         * @return the label
         */
        String getLabel();

        /**
         * Sets the categories.
         *
         * @param categories the new categories
         */
        void setCategories(List<String> categories);

        /**
         * Sets the key.
         *
         * @param key the new key
         */
        void setKey(String key);

        /**
         * Sets the label.
         *
         * @param label the new label
         */
        void setLabel(String label);

    }

    /**
     * Gets the categories.
     *
     * @return the categories
     */
    List<Category> getCategories();

    /**
     * Gets the filter label.
     *
     * @return the filter label
     */
    String getFilterLabel();

    /**
     * Gets the options.
     *
     * @return the options
     */
    List<Option> getOptions();

    /**
     * Sets the categories.
     *
     * @param categories the new categories
     */
    void setCategories(List<Category> categories);

    /**
     * Sets the filter label.
     *
     * @param filterLabel the new filter label
     */
    void setFilterLabel(String filterLabel);

    /**
     * Sets the options.
     *
     * @param options the new options
     */
    void setOptions(List<Option> options);

}
