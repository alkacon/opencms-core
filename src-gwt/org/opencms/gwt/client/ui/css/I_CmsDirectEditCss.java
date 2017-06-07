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

package org.opencms.gwt.client.ui.css;

import org.opencms.gwt.client.ui.css.I_CmsToolbarButtonLayoutBundle.I_CmsToolbarButtonCss;

import com.google.gwt.resources.client.CssResource.Shared;

/**
 * CSS resource interface for the classic Direct Edit Buttons.<p>
 *
 * @since 8.0.0
 */
@Shared
public interface I_CmsDirectEditCss extends I_CmsToolbarButtonCss {

    /**
     * CSS class accessor.<p>
     *
     * @return a CSS class name
     */
    String directEditButtons();

    /**
     * CSS class accessor.<p>
     *
     * @return a CSS class name
     */
    String editableElement();

    /**
     * CSS class accessor.<p>
     *
     * @return a CSS class name
     */
    String expiredListElementOverlay();

    /**
     * CSS class accessor.<p>
     *
     * @return a CSS class name
     */
    String hideButtons();

    /**
     * CSS class accessor.<p>
     *
     * @return a CSS class name
     */
    String optionBar();

    /**
     * CSS class accessor.<p>
     *
     * @return a CSS class name
     */
    String showButtons();
}
