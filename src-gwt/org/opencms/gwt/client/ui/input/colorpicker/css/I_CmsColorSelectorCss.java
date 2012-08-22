/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui.input.colorpicker.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Css resource.<p>
 * 
 * @since 8.0.0
 */
public interface I_CmsColorSelectorCss extends ClientBundle {

    /** The XML content widget CSS. */
    public interface I_CmsColorPickerCss extends CssResource {

        /** 
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String sliderMap();

        /** 
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String sliderMapOverlay();

        /** 
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String sliderMapUnderlay();

        /** 
         * Css class reader.<p>
         * 
         * @return the css class
         */
        String sliderMapSlider();

    }

    /** The bundle instance. */
    I_CmsColorSelectorCss INSTANCE = GWT.create(I_CmsColorSelectorCss.class);

    /**
     * Access method.<p>
     * 
     * @return the XML content widget CSS
     */
    @Source("colorSelector.css")
    I_CmsColorPickerCss colorPickerCss();
}
