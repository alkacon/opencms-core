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

package org.opencms.ade.upload.client.ui.css;

import org.opencms.gwt.client.ui.css.I_CmsConstantsBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsPopupCss;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

/**
 * Resource bundle to access CSS and image resources.
 *
 * @since 8.0.0
 */
public interface I_CmsLayoutBundle extends ClientBundle {

    /** The upload dialog CSS classes. */
    public interface I_CmsUploadCss extends I_CmsPopupCss {

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dialogInfo();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String dragAndDropMessage();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String fileInputPanel();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String mainContentWidget();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String summary();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String unzipButton();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String unzipButtonDownFace();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String unzipButtonUpFace();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String uploadDialogButton();
    }

    /** The bundle instance. */
    I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

    /**
     * The CSS constants bundle.<p>
     *
     * @return a bundle of CSS constants
     */
    I_CmsConstantsBundle constants();

    /**
     * Access method.<p>
     *
     * @return the constants CSS
     */
    @Source("upload.css")
    I_CmsUploadCss uploadCss();

}
