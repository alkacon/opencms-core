/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/css/Attic/I_CmsConstantsBundle.java,v $
 * Date   : $Date: 2011/02/22 16:34:06 $
 * Version: $Revision: 1.4 $
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

package org.opencms.gwt.client.ui.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Resource bundle to access CSS constants.
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public interface I_CmsConstantsBundle extends ClientBundle {

    /** Constants CSS. */
    interface I_CmsConstantsCss extends CssResource {

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String backgroundColorDialog();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String backgroundColorDisabled();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String backgroundColorEmptyContainer();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String backgroundColorHighlight();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String backgroundColorInfo();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String backgroundColorInputError();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String backgroundColorMenu();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String backgroundColorOverlay();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String backgroundColorPopupShadow();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String backgroundColorSitemap();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String backgroundColorSitemapHighlight();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String backgroundColorToolBarMenuConnect();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String borderColor();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String borderColorHighlight();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String borderRadius();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String boxShadowColorIE();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String boxShadowColorOther();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String dateboxAnotherMonthBg();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String dateboxDayFontSize();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String dateboxHoveredDayBg();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String dateboxSelectedDayBg();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String dateboxTodayColor();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String dateboxWeekendBg();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String fontFamily();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String fontSize();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String fontSizeBig();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String fontSizeSmall();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String notificationErrorBg();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String notificationErrorColor();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String notificationNormalBg();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String notificationNormalColor();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String notificationWarningBg();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String notificationWarningColor();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String textColor();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String textColorChanged();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String textColorDisabled();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String textColorImportant();

        /** 
         * Access method.<p>
         * 
         * @return the CSS constant value
         */
        String textColorNew();
    }

    /** The bundle instance. */
    I_CmsConstantsBundle INSTANCE = GWT.create(I_CmsConstantsBundle.class);

    /**
     * Access method.<p>
     * 
     * @return the constants CSS
     */
    @Source("constants.css")
    I_CmsConstantsCss css();
}
