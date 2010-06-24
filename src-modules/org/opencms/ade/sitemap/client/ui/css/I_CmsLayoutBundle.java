/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/css/Attic/I_CmsLayoutBundle.java,v $
 * Date   : $Date: 2010/06/24 09:05:26 $
 * Version: $Revision: 1.3 $
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

package org.opencms.ade.sitemap.client.ui.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;

/**
 * Resource bundle to access CSS and image resources.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public interface I_CmsLayoutBundle extends org.opencms.gwt.client.ui.css.I_CmsLayoutBundle {

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
        String clipboardTabPanel();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String description();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String itemList();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String menuTabContainer();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String menuContent();
    }

    /** Page CSS. */
    interface I_CmsPageCss extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String page();
    }

    /** Root CSS. */
    interface I_CmsRootCss extends CssResource {

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
    I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

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
     * @return the page CSS
     */
    @Source("page.css")
    I_CmsPageCss pageCss();

    /**
     * Access method.<p>
     * 
     * @return the root CSS
     */
    @Source("root.css")
    I_CmsRootCss rootCss();

    /**
     * Access method.<p>
     * 
     * @return the root CSS
     **/
    @Source("sitemapItem.css")
    I_CmsSitemapItemCss sitemapItemCss();
}
