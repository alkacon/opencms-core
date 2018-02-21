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

package org.opencms.acacia.client.widgets.serialdate;

import org.opencms.acacia.client.css.I_CmsLayoutBundle.I_Widgets;
import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource.Import;

/**
 * Content editor CSS resources bundle.<p>
 */
public interface I_CmsLayoutBundle extends org.opencms.gwt.client.ui.css.I_CmsLayoutBundle {

    /** The XML content widget CSS. */
    interface I_CmsWidgetCss extends I_Widgets {

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String button();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String ordinalTextBox();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String patternInput();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String patternRadio();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String patternRow();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String radioButtonStyle();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String serialDateWidget();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String shortTextBox();

        /**
         * Css class reader.<p>
         *
         * @return the css class
         */
        String statusLabel();
    }

    /** The bundle instance. */
    I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

    /**
     * @see org.opencms.gwt.client.ui.css.I_CmsLayoutBundle#constants()
     */
    I_CmsConstantsBundle constants();

    /**
     * Access method.<p>
     *
     * @return the XML content widget CSS
     */
    @Source("widget.gss")
    @Import(value = {
        I_CmsFieldsetCss.class,
        org.opencms.acacia.client.css.I_CmsWidgetsLayoutBundle.I_CmsWidgetCss.class,
        org.opencms.acacia.client.css.I_CmsLayoutBundle.I_Style.class,
        I_CmsButtonCss.class})
    I_CmsWidgetCss widgetCss();

}
