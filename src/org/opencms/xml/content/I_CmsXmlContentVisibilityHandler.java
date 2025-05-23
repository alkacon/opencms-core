/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.xml.types.I_CmsXmlSchemaType;

import java.util.Locale;

/**
 * Handles the visibility of fields in the XML content editor.<p>
 */
public interface I_CmsXmlContentVisibilityHandler {

    /**
     * Returns if the given content value field should be visible to the current user.<p>
     *
     * @param cms the cms context
     * @param value the content value
     * @param elementPath the path to the element
     * @param params configuration parameters
     * @param resource the edited resource
     * @param contentLocale the locale being edited
     *
     * @return <code>true</code> if the given content value field should be visible to the current user
     */
    boolean isValueVisible(
        CmsObject cms,
        I_CmsXmlSchemaType value,
        String elementPath,
        String params,
        CmsResource resource,
        Locale contentLocale);
}
