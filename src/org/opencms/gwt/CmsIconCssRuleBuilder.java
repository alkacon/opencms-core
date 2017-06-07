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

package org.opencms.gwt;

import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a helper class for creating the text of the CSS rule for a single icon based on resource type and file suffix.<p>
 *
 * @since 8.0.0
 */
public class CmsIconCssRuleBuilder {

    /** The uri of the icon image. */
    private String m_imageUri = "INVALID_ICON";

    /** The list of selector strings. */
    private List<String> m_selectors = new ArrayList<String>();

    /**
     * Adds a selector for a resource type and a file suffix.<p>
     *
     * @param type the resource type name
     * @param suffix the file suffix
     * @param small true if the selector should be for the small icon
     */
    public void addSelectorForSubType(String type, String suffix, boolean small) {

        String template = " .%1$s.%2$s.%3$s";
        String selector = String.format(
            template,
            CmsIconUtil.TYPE_ICON_CLASS,
            CmsIconUtil.getResourceTypeIconClass(type, small),
            CmsIconUtil.getResourceSubTypeIconClass(type, suffix, small));
        m_selectors.add(selector);
    }

    /**
     * Adds a selector for a resource type.<p>
     *
     * @param type the name of the resource type
     * @param small true if the selector should be for the small icon
     */
    public void addSelectorForType(String type, boolean small) {

        String template = " div.%1$s.%2$s, span.%1$s.%2$s";
        String selector = String.format(
            template,
            CmsIconUtil.TYPE_ICON_CLASS,
            CmsIconUtil.getResourceTypeIconClass(type, small));
        m_selectors.add(selector);
    }

    /**
     * Sets the URI of the icon image file.<p>
     *
     * @param imageUri the URI of the icon image file
     */
    public void setImageUri(String imageUri) {

        m_imageUri = imageUri;
    }

    /**
     * Writes the CSS to a string buffer.<p>
     *
     * @param buffer the string buffer to which the
     */
    public void writeCss(StringBuffer buffer) {

        buffer.append(CmsStringUtil.listAsString(m_selectors, ", "));
        buffer.append(" { background-image: url(\"");
        buffer.append(m_imageUri);
        buffer.append("\");} ");
    }

}
