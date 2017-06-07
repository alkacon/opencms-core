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

package org.opencms.xml.content;

import org.opencms.file.CmsObject;
import org.opencms.util.CmsUUID;
import org.opencms.xml.types.I_CmsXmlContentValue;

/**
 * Interface representing an XML content location which corresponds to an actual content value.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsXmlContentValueLocation extends I_CmsXmlContentLocation {

    /**
     * Returns the structure id of the content value (only valid for VfsFile values!).<p>
     *
     * @param cms the CMS context
     * @return the structure id of the content value
     */
    CmsUUID asId(CmsObject cms);

    /**
     * Returns the string value of the content value.<p>
     *
     * @param cms the CMS context
     *
     * @return the string value of the content value
     */
    String asString(CmsObject cms);

    /**
     * Returns the content value at the given location.<p>
     *
     * @return the content value at the given location
     */
    I_CmsXmlContentValue getValue();

}
