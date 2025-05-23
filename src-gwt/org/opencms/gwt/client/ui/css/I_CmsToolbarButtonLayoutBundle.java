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

package org.opencms.gwt.client.ui.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource.Shared;

/**
 * Resource bundle to access CSS and image resources for tool-bar buttons.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsToolbarButtonLayoutBundle extends ClientBundle {

    /** The button CSS. */
    @Shared
    public interface I_CmsToolbarButtonCss extends I_CmsLayoutBundle.I_CmsStateCss {

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("oc-edit-buttons-invisible")
        String editButtonsInvisible();

        /**
         *  Access method.<p>
         *
         * @return the CSS class name
         */
        @ClassName("oc-edit-buttons-visible")
        String editButtonsVisible();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String elementInfoChanged();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String elementInfoUnchanged();
    }

    /** The bundle instance. */
    I_CmsToolbarButtonLayoutBundle INSTANCE = GWT.create(I_CmsToolbarButtonLayoutBundle.class);

    /**
     * The CSS constants bundle.<p>
     *
     * @return a bundle of CSS constants
     */
    I_CmsConstantsBundle constants();

    /**
     * Access method.<p>
     *
     * @return the button CSS
     */
    @Source("toolbarButton.gss")
    I_CmsToolbarButtonCss toolbarButtonCss();
}
