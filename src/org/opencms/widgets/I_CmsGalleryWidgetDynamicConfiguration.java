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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.i18n.CmsMessages;

/**
 * Enables a dynamic configuration of values for the {@link A_CmsGalleryWidget}.<p>
 *
 * The following values can be configured dynamically:
 * <ul>
 * <li>The type of the preselected item list (gallery or category)</li>
 * <li>The preselected item list (i.e. a gallery folder or category)</li>
 * </ul>
 *
 * @since 7.5.0
 */

public interface I_CmsGalleryWidgetDynamicConfiguration {

    /**
     * Returns the required information for the initial item list to load.<p>
     *
     * If a gallery should be shown, the path to the gallery must be specified,
     * for a category the category path.<p>
     *
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     * @return the required information for the initial item list to load
     */
    String getStartup(CmsObject cms, CmsMessages widgetDialog, I_CmsWidgetParameter param);

    /**
     * Returns the type of the initial item list to load, either gallery or category.<p>
     *
     * @param cms an initialized instance of a CmsObject
     * @param widgetDialog the dialog where the widget is used on
     * @param param the widget parameter to generate the widget for
     * @return the type of the initial image list to load, either gallery or category
     */
    String getType(CmsObject cms, CmsMessages widgetDialog, I_CmsWidgetParameter param);
}
