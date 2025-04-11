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

package org.opencms.ui.apps;

import org.opencms.ui.components.OpenCmsTheme;

import com.vaadin.ui.Component;

/**
 * Generates app launcher buttons.<p>
 */
public interface I_CmsAppButtonProvider {

    /** Button color style. */
    String BUTTON_STYLE_BLUE = OpenCmsTheme.COLOR_BLUE;

    /** Button color style. */
    String BUTTON_STYLE_CYAN = OpenCmsTheme.COLOR_CYAN;

    /** Button color style. */
    String BUTTON_STYLE_GRAY = OpenCmsTheme.COLOR_GRAY;

    /** Button color style. */
    String BUTTON_STYLE_ORANGE = OpenCmsTheme.COLOR_ORANGE;

    /** Button color style. */
    String BUTTON_STYLE_RED = OpenCmsTheme.COLOR_RED;

    /** Button color style. */
    String BUTTON_STYLE_TRANSPARENT = OpenCmsTheme.IMAGE_TRANSPARENT;

    /** Button color style. */
    String BUTTON_STYLE_CLASSIC = OpenCmsTheme.IMAGE_GRADIENT;

    /**
     * Creates an app launcher button.<p>
     *
     * @param appConfig the app configuration
     *
     * @return the button component
     */
    Component createAppButton(I_CmsWorkplaceAppConfiguration appConfig);

    /**
     * Creates an app folder button.<p>
     *
     * @param node the folder configuration
     *
     * @return the button component
     */
    Component createAppFolderButton(CmsAppCategoryNode node);

}
