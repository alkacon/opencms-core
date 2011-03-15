/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/css/Attic/I_CmsIconBundle.java,v $
 * Date   : $Date: 2011/03/15 14:26:07 $
 * Version: $Revision: 1.7 $
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
import com.google.gwt.resources.client.ImageResource;

/**
 * Image bundle for smaller icons.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.7 $
 * 
 * @since 8.0.0
 */
public interface I_CmsIconBundle extends ClientBundle {

    /** Instance of this image bundle. */
    I_CmsIconBundle INSTANCE = GWT.create(I_CmsIconBundle.class);

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/addIcon.png")
    ImageResource addIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/availabilityIcon.png")
    ImageResource availabilityIcon();

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @Source("images/icons/bumpIcon.png")
    ImageResource bumpIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/deleteIcon.png")
    ImageResource deleteIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/inheritedIcon.png")
    ImageResource inheritedIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/editIcon.png")
    ImageResource editIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/favoriteIcon.png")
    ImageResource favoriteIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/gotoPageIcon.png")
    ImageResource gotoPageIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/gotoParentIcon.png")
    ImageResource gotoParentIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/gotoSubSitemapIcon.png")
    ImageResource gotoSubSitemapIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/mergeSitemapIcon.png")
    ImageResource mergeSitemapIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/moveIcon.png")
    ImageResource moveIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/propertyIcon.png")
    ImageResource propertyIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/refreshIcon.png")
    ImageResource refreshIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/searchIcon.png")
    ImageResource searchIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/selectionIcon.png")
    ImageResource selectionIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/subSitemapIcon.png")
    ImageResource subSitemapIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/icons/uploadIcon.png")
    ImageResource uploadIcon();
}
