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

package org.opencms.gwt.client.ui.input.category.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Shared;

/**
 * Resource bundle to access CSS and image resources of the gallery dialog.<p>
 * 
 * @since 8.0.0
 */
public interface I_CmsLayoutBundle extends org.opencms.gwt.client.ui.css.I_CmsLayoutBundle {

    /** Gallery dialog CSS. */
    @Shared
    interface I_CmsGalleryDialogCss extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String criteriaList();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String editorGallery();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String galleryBody();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String hasButton();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String infoLabel();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String listIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String listOnlyTab();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String noParamsMessage();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String paramsText();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String parentPanel();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String quickFilterBox();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String resultTabUpload();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String searchTabBorderLayer();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String searchTabDesMargin();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String searchTabInputLeft();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String searchTabInputRight();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String searchTabInputWide();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String searchTabLabelLeft();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String searchTabLabelRight();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String searchTabRow();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String searchTabRowRightAlign();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String selectboxWidth();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String showParams();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String showPreview();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String tabOptions();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String tabParamsPanel();
    }

    /** The bundle instance. */
    I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

    /**
     * Access method.<p>
     * 
     * @return the gallery dialog CSS
     */
    @Source("gallerydialog.css")
    I_CmsGalleryDialogCss galleryDialogCss();

}
