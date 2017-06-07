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

package org.opencms.workplace.editors;

import org.opencms.file.CmsObject;

/**
 * Provides methods to determine the CSS style sheet URI for the editors based on the edited resource path.<p>
 *
 * The method {@link #matches(CmsObject, String)} can be used to check
 * if the handler matches the currently edited resource.<p>
 *
 * @since 6.9.2
 */
public interface I_CmsEditorCssHandler {

    /**
     * Returns the absolute VFS path of the CSS style sheet to use.<p>
     *
     * @param cms the current OpenCms user context
     * @param editedResourcePath the absolute VFS path of the currently edited resource
     * @return the absolute VFS path of the CSS style sheet to use
     */
    String getUriStyleSheet(CmsObject cms, String editedResourcePath);

    /**
     * Checks if the handler can be used to determine the CSS style sheet based on the edited resource.<p>
     *
     * @param cms the current OpenCms user context
     * @param editedResourcePath the absolute VFS path of the currently edited resource
     * @return  true if the handler matches, otherwise false
     */
    boolean matches(CmsObject cms, String editedResourcePath);

}
