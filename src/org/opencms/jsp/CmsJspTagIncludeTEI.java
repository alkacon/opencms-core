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

package org.opencms.jsp;

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;

/**
 * This is a TagExtraInfo evaluation class that checks the attibutes of
 * the <code>&lt;cms:include /&gt;</code> tag.<p>
 *
 * @since 6.0.0
 */
public class CmsJspTagIncludeTEI extends TagExtraInfo {

    /** Attribute name. */
    private static final String ATTR_ATTRIBUTE = "attribute";

    /** Attribute name. */
    private static final String ATTR_FILE = "file";

    /** Attribute name. */
    private static final String ATTR_PAGE = "page";

    /** Attribute name. */
    private static final String ATTR_PROPERTY = "property";

    /** Attribute name. */
    private static final String ATTR_SUFFIX = "suffix";

    /**
     * Returns true if the given attribute name is specified, false otherwise.<p>
     *
     * @param data the tag data
     * @param attributeName the attribute name
     * @return  true if the given attribute name is specified, false otherwise
     */
    public static boolean isSpecified(TagData data, String attributeName) {

        return (data.getAttribute(attributeName) != null);
    }

    /**
     * Checks the validity of the <code>&lt;cms:include /&gt;</code> attributes.<p>
     *
     * The logic used is:
     * <pre>
     * if (hasFile && (hasSuffix || hasProperty || hasAttribute)) return false;
     * if (hasProperty && hasAttribute) return false;
     * if (hasSuffix && !(hasProperty || hasAttribute)) return false;
     * </pre>
     *
     * @param data the tag data
     * @return true if attributes are valid, false otherwise
     */
    @Override
    public boolean isValid(TagData data) {

        boolean hasFile = isSpecified(data, ATTR_FILE) || isSpecified(data, ATTR_PAGE);
        boolean hasSuffix = isSpecified(data, ATTR_SUFFIX);
        boolean hasProperty = isSpecified(data, ATTR_PROPERTY);
        boolean hasAttribute = isSpecified(data, ATTR_ATTRIBUTE);
        // boolean hasElement = isSpecified(data, C_ATTR_ELEMENT);

        if (hasFile && (hasSuffix || hasProperty || hasAttribute)) {
            return false;
        }
        if (hasProperty && hasAttribute) {
            return false;
        }
        if (hasSuffix && !(hasProperty || hasAttribute)) {
            return false;
        }

        return true;
    }
}
