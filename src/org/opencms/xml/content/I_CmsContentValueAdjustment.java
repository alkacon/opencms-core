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
import org.opencms.xml.A_CmsXmlDocument;

import java.util.Locale;

/**
 * Interface for classes to adjust XML content values before using them, e.g. for indexing.
 */
public interface I_CmsContentValueAdjustment {

    /**
     * Returns the adjustment for the provided value.
     * @param cms the current context
     * @param content the XML content
     * @param locale the locale to adjust the value for
     * @param xpath the path to the value
     * @param value the original value
     * @return the adjusted value
     */
    String getAdjustedValue(CmsObject cms, A_CmsXmlDocument content, Locale locale, String xpath, String value);
}
