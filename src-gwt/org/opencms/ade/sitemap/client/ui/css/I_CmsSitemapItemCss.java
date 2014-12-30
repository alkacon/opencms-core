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

package org.opencms.ade.sitemap.client.ui.css;

import com.google.gwt.resources.client.CssResource;

/**
 * The CSS bundle for sitemap items.<p>
 * 
 * @since 8.0.0
 */

public interface I_CmsSitemapItemCss extends CssResource {

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String contentHide();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String expiredOrNotReleased();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String galleriesMode();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String hasChildren();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String hasNavChildren();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String hasNoChildren();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String hasNoNavChildren();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String hiddenNavEntry();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String itemTitle();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String marker();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String markUnchanged();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String modelPageMode();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String navigationLevelIcon();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String navMode();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String notInNavigationEntry();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String positionIndicator();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     */
    String sitemapEntryDecoration();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String treeItemOpener();

    /**
     * CSS class accessor.<p>
     * 
     * @return a CSS class
     **/
    String vfsMode();

}
