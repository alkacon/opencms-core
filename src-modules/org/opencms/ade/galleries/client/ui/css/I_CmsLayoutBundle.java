/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/css/Attic/I_CmsLayoutBundle.java,v $
 * Date   : $Date: 2010/05/21 14:27:39 $
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

package org.opencms.ade.galleries.client.ui.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;

/**
 * Resource bundle to access CSS and image resources of the gallery dialog.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public interface I_CmsLayoutBundle extends org.opencms.gwt.client.ui.css.I_CmsLayoutBundle {

    /** Gallery dialog CSS. */
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
        String galleryBody();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String listIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String marginBottom();

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
        String tabPanel();

    }

    /** Preview dialog CSS. */
    interface I_CmsPreviewDialogCss extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String clearFix();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String detailsHolder();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String inputField();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String labelField();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String previewButton();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String previewButtonBar();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String previewCloseButton();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String previewDialog();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String previewHolder();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String previewPanel();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String propertiesList();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String propertyLeft();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String propertyRight();
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

    /**
     * Access method.<p>
     * 
     * @return the gallery dialog CSS
     */
    @Source("previewdialog.css")
    I_CmsPreviewDialogCss previewDialogCss();

}
