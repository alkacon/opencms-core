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

package org.opencms.search.fields;

import org.opencms.util.A_CmsModeIntEnumeration;
import org.opencms.util.CmsStringUtil;

/**
 * Describes a possible mapping type for a piece of content used in building a search index.<p>
 *
 * The mapping type is responsible to select which content from the OpenCms resource is used for
 * a field.<p>
 *
 * @since 7.0.0
 */
public final class CmsSearchFieldMappingType extends A_CmsModeIntEnumeration {

    /** The "dynamic" mapping type, this is required if a special class is used to process the field mapping value. */
    public static final CmsSearchFieldMappingType ATTRIBUTE = new CmsSearchFieldMappingType(5);

    /** The "content" mapping type, maps the content of the resource (no parameters required). */
    public static final CmsSearchFieldMappingType CONTENT = new CmsSearchFieldMappingType(0);

    /** The "dynamic" mapping type, this is required if a special class is used to process the field mapping value. */
    public static final CmsSearchFieldMappingType DYNAMIC = new CmsSearchFieldMappingType(4);

    /** The "item" mapping type, maps the selected content item of the content. */
    public static final CmsSearchFieldMappingType ITEM = new CmsSearchFieldMappingType(3);

    /** The "property" mapping type, maps the selected property value of the resource. */
    public static final CmsSearchFieldMappingType PROPERTY = new CmsSearchFieldMappingType(1);

    /** The "property-search" mapping type, maps the selected property value of the resource with search upwards. */
    public static final CmsSearchFieldMappingType PROPERTY_SEARCH = new CmsSearchFieldMappingType(2);

    /** ID required for safe serialization. */
    private static final long serialVersionUID = 1959271063305411797L;

    /** String constant for the "attribute" type. */
    private static final String STR_ATTRIBUTE = "attribute";

    /** String constant for the "content" type. */
    private static final String STR_CONTENT = "content";

    /** String constant for the "dynamic" type. */
    private static final String STR_DYNAMIC = "dynamic";

    /** String constant for the "item" type. */
    private static final String STR_ITEM = "item";

    /** String constant for the "property" type. */
    private static final String STR_PROPERTY = "property";

    /** String constant for the "property-search" type. */
    private static final String STR_PROPERTY_SEARCH = "property-search";

    /**
     * Hides the public constructor.<p>
     *
     * @param mode the mode constant to use
     */
    private CmsSearchFieldMappingType(int mode) {

        super(mode);
    }

    /**
     * Returns the matching field mapping type, or <code>null</code> if the given value is
     * not a valid mapping type name.<p>
     *
     * @param value the name of the mapping type
     *
     * @return the matching field mapping type
     */
    public static CmsSearchFieldMappingType valueOf(String value) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(value)) {
            return null;
        }
        value = value.trim().toLowerCase();
        if (STR_CONTENT.equals(value)) {
            return CONTENT;
        } else if (STR_PROPERTY.equals(value)) {
            return PROPERTY;
        } else if (STR_PROPERTY_SEARCH.equals(value)) {
            return PROPERTY_SEARCH;
        } else if (STR_ITEM.equals(value)) {
            return ITEM;
        } else if (STR_DYNAMIC.equals(value)) {
            return DYNAMIC;
        } else if (STR_ATTRIBUTE.equals(value)) {
            return ATTRIBUTE;
        }
        return null;
    }

    /**
     * @see org.opencms.util.A_CmsModeIntEnumeration#toString()
     */
    @Override
    public String toString() {

        switch (getMode()) {
            case 1:
                return STR_PROPERTY;
            case 2:
                return STR_PROPERTY_SEARCH;
            case 3:
                return STR_ITEM;
            case 4:
                return STR_DYNAMIC;
            case 5:
                return STR_ATTRIBUTE;
            case 0:
            default:
                return STR_CONTENT;
        }
    }
}