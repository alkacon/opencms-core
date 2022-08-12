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

package org.opencms.gwt.shared.attributeselect;

import java.util.List;
import java.util.Map;

/**
 * AutoBean interface for the data used by the attribute select widget.
 *
 * <p>An attribute select widget consists of several attribute filter select boxes and one main select box, such
 * that choosing values from the attribute filters restricts the available options in the main select box to those which
 * have a matching value for every filter attribute.
 */
public interface I_CmsAttributeSelectData {

    /**
     * AutoBean interface for a filter attribute definition.
     */
    interface AttributeDefinition {

        /**
         * Gets the default option (initially selected).
         *
         * @return the default option
         */
        String getDefaultOption();

        /**
         * Gets the attribute label.
         *
         * @return the attribute label
         */
        String getLabel();

        /**
         * Gets the attribute name.
         *
         * @return the attribute name
         */
        String getName();

        /**
         * Gets the neutral option, which is chosen when a pre-existing value is not among the current options for the main select widget.
         *
         * @return the neutral optiob
         */
        String getNeutralOption();

        /**
         * Gets the list of all options.
         *
         * @return the list of all options
         */
        List<Option> getOptions();

        /**
         * Sets the default option (initially selected).
         *
         * @param defaultOption the default option
         */
        void setDefaultOption(String defaultOption);

        /**
         * Sets the label.
         *
         * @param label the label
         */
        void setLabel(String label);

        /**
         * Sets the attribute name.
         *
         * @param name the attribute name
         */
        void setName(String name);

        /**
         * Sets the neutral option (will be set when a pre-existing value is not among the current options)
         *
         * @param neutralOption the neutral option
         */
        void setNeutralOption(String neutralOption);

        /**
         * Sets the options.
         *
         * @param options the list of options
         */
        void setOptions(List<Option> options);
    }

    /**
     * Represents a single option.
     */
    interface Option {

        /**
         * Gets the help text.
         *
         * @return the help text
         */
        String getHelpText();

        /**
         * Gets the label text.
         *
         * @return the label text
         */
        String getLabel();

        /**
         * Gets the value.
         *
         * @return the value
         */
        String getValue();

        /**
         * Sets the help text.
         *
         * @param helpText the help text
         */
        void setHelpText(String helpText);

        /**
         * Sets the label.
         *
         * @param label the label
         */
        void setLabel(String label);

        /**
         * Sets the value.
         *
         * @param value the value
         */
        void setValue(String value);
    }

    /**
     * A choice option, but with (multi-valued) attributes added.
     */
    interface OptionWithAttributes extends Option {

        /**
         * Gets the attributes.
         *
         * <p>The set of keys should be equal to set of names of all attribute definitions.
         *
         * @return the attributes
         */
        Map<String, List<String>> getAttributes();

        /**
         * Sets the attributes.
         *
         * @param attributes the attributes
         */
        void setAttributes(Map<String, List<String>> attributes);

    }

    /**
     * Gets the attribute definitions.
     *
     * @return the attribute definitions
     */
    List<AttributeDefinition> getAttributeDefinitions();

    /**
     * Gets all options.
     *
     * @return the options
     */
    List<OptionWithAttributes> getOptions();

    /**
     * Sets the attribute definitions.
     *
     * @param attributeDefinitions the attribute definitions
     */
    void setAttributeDefinitions(List<AttributeDefinition> attributeDefinitions);

    /**
     * Sets the options.
     *
     * @param options the options
     */
    void setOptions(List<OptionWithAttributes> options);

}
