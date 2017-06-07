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

package org.opencms.ade.sitemap.client.ui.css;

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Import;

/**
 * Resource bundle to access CSS and image resources.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsSitemapLayoutBundle extends org.opencms.gwt.client.ui.css.I_CmsLayoutBundle {

    /** Clipboard CSS. */
    interface I_CmsClipboardCss extends CssResource {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String clipboardList();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String listClearButton();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String menuTabContainer();
    }

    /** Root CSS. */
    interface I_CmsSitemapCss extends CssResource {

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String headerContainer();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String headerContainerVaadinMode();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String hiddenHeader();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String page();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String pageCenter();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String root();
    }

    /** The bundle instance. */
    I_CmsSitemapLayoutBundle INSTANCE = GWT.create(I_CmsSitemapLayoutBundle.class);

    /**
     * Access method.<p>
     *
     * @return the root CSS
     **/
    @Source("clipboard.css")
    I_CmsClipboardCss clipboardCss();

    /**
     * Access method.<p>
     *
     * @return the image bundle for the sitemap
     */
    I_CmsImageBundle images();

    /**
     * Access method.<p>
     *
     * @return the root CSS
     */
    @Source("sitemap.css")
    @Import(value = I_CmsLayoutBundle.I_CmsToolbarCss.class)
    I_CmsSitemapCss sitemapCss();

    /**
     * Access method.<p>
     *
     * @return the root CSS
     **/
    @Source("sitemapItem.css")
    I_CmsSitemapItemCss sitemapItemCss();
}
