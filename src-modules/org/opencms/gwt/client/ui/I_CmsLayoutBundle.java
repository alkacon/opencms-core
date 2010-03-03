/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/I_CmsLayoutBundle.java,v $
 * Date   : $Date: 2010/03/03 15:32:37 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.CssResource.Shared;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

/**
 * Resource bundle to access CSS and image resources.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsLayoutBundle extends ClientBundle {

    /** The bundle instance. */
    I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

    /** Button CSS. */
    interface I_ButtonCss extends CssResource, I_CmsStateCss {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String cmsTextButton();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String cmsImageButton();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String cmsImageButtonTransparent();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String cmsButtonBig();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String cmsButtonSmall();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String cmsButtonMedium();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String cmsMinWidth();
    }

    /** Constants CSS. */
    interface I_CmsConstantsCss extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String fontFamily();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String fontSize();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String fontSizeSmall();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String fontSizeBig();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String color();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String colorDisabled();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String borderColor();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String borderColorInactive();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String borderColorActive();
    }

    /** General CSS. */
    @Shared
    interface I_CmsStateCss extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String cmsState();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        @ClassName("cmsState-up")
        String cmsStateup();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        @ClassName("cmsState-up-hovering")
        String cmsStateUpHovering();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        @ClassName("cmsState-up-disabled")
        String cmsStateUpDisabled();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        @ClassName("cmsState-down")
        String cmsStateDown();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        @ClassName("cmsState-down-hovering")
        String cmsStateDownHovering();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        @ClassName("cmsState-down-disabled")
        String cmsStateDownDisabled();

    }

    /** Dialog CSS. */
    @Shared
    interface I_CmsDialogCss extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String popup();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String popupContent();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String popupMainContent();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String popupHead();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String popupShadow();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String popupOverlay();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String popupButtonPanel();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String menuPopup();
    }

    /**
     * Access method.<p>
     * 
     * @return the constants CSS
     */
    @Source("constants.css")
    I_CmsConstantsCss constantsCss();

    /**
     * Access method.<p>
     * 
     * @return the dialog CSS
     */
    @Source("dialog.css")
    I_CmsDialogCss dialogCss();

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @Source("button.css")
    I_ButtonCss buttonCss();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ui-bg_glass_65_ffffff_1x400.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource backgroundActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ui-bg_glass_75_e6e6e6_1x400.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource backgroundHover();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ui-bg_glass_75_cccccc_1x400.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource backgroundDefault();

}
