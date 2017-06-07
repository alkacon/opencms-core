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

import org.opencms.xml.I_CmsXmlDocument;

import java.util.Locale;

/**
 * Provides access to a <code>{@link org.opencms.xml.I_CmsXmlDocument}</code> document that was previously loaded by a parent tag.<p>
 *
 * @since 6.2.0
 */
public interface I_CmsXmlContentContainer extends I_CmsResourceContainer {

    /**
     * Returns the currently loaded OpenCms XML content document.<p>
     *
     * @return the currently loaded OpenCms XML content document
     */
    I_CmsXmlDocument getXmlDocument();

    /**
     * Returns the currently selected element name in the loaded XML content document.<p>
     *
     * @return the currently selected element name in the loaded XML content document
     */
    String getXmlDocumentElement();

    /**
     * Returns the currently selected locale used for acessing the content in the loaded XML content document.<p>
     *
     * @return the currently selected locale used for acessing the content in the loaded XML content document
     */
    Locale getXmlDocumentLocale();
}