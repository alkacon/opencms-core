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

package org.opencms.gwt.client.ui.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource.Shared;

/**
 * Resource bundle to access CSS and image resources for tool-bar buttons.<p>
 * 
 * @since 8.0.0
 */
public interface I_CmsToolbarButtonLayoutBundle extends I_CmsBigIconBundle {

    /** The extended tool-bar button CSS. */
    @Shared
    interface I_CmsExtendedToolbarButtonCss extends I_CmsToolbarButtonCss {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarBack();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarContext();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarCopyLocale();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarDeleteLocale();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarElementInfo();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarGallery();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarPublish();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarRedo();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarRefresh();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarReset();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarSave();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarSaveExit();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarShowSmall();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarSitemap();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarToggleHelp();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarUndo();
    }

    /** The button CSS. */
    @Shared
    public interface I_CmsToolbarButtonCss extends I_CmsLayoutBundle.I_CmsStateCss {

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

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarAdd();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarClipboard();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarDelete();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarEdit();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarInfo();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarInherited();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarMove();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarNew();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarProperties();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarRemove();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarSelection();
    }

    /** The bundle instance. */
    I_CmsToolbarButtonLayoutBundle INSTANCE = GWT.create(I_CmsToolbarButtonLayoutBundle.class);

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @Source("toolbarButton.css")
    I_CmsExtendedToolbarButtonCss toolbarButtonCss();

}
