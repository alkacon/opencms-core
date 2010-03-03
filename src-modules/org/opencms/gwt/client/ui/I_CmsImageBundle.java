/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/I_CmsImageBundle.java,v $
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
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.CssResource.Shared;

/**
 * Resource bundle to access CSS and image resources.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsImageBundle extends ClientBundle {

    /** The bundle instance. */
    I_CmsImageBundle INSTANCE = GWT.create(I_CmsImageBundle.class);

    /** Bundles the image sprite CSS classes. */
    @Shared
    interface I_CmsImageStyle extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String magnifierIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String magnifierIconActive();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String magnifierIconInactive();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String closeIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String closeIconActive();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String closeIconInactive();
    }

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @NotStrict
    @Source("imageSprites.css")
    I_CmsImageStyle style();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ade_add.png")
    ImageResource magnifierIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ade_add_sw.png")
    ImageResource magnifierIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/close16x16.png")
    ImageResource closeIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/close16x16_light.png")
    ImageResource closeIconInactive();
}
