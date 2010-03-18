/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/css/Attic/I_CmsImageBundle.java,v $
 * Date   : $Date: 2010/03/18 09:31:15 $
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

package org.opencms.gwt.client.ui.css;

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
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public interface I_CmsImageBundle extends ClientBundle {

    /** Bundles the image sprite CSS classes. */
    @Shared
    interface I_CmsImageStyle extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String deleteIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String deleteIconActive();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String deleteIconDeactivated();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String deleteIconInactive();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String editorIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String editorIconActive();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String editorIconDeactivated();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String editorIconInactive();

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
        String moveIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String moveIconActive();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String moveIconDeactivated();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String moveIconInactive();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String newIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String newIconActive();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String newIconDeactivated();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String newIconInactive();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String propertyIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String propertyIconActive();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String propertyIconDeactivated();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String propertyIconInactive();

    }

    /** The bundle instance. */
    I_CmsImageBundle INSTANCE = GWT.create(I_CmsImageBundle.class);

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_delete_a.png")
    ImageResource deleteIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_delete_sw.png")
    ImageResource deleteIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_delete_i.png")
    ImageResource deleteIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_editor_a.png")
    ImageResource editorIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_editor_sw.png")
    ImageResource editorIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_editor_i.png")
    ImageResource editorIconInactive();

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
    @Source("images/minus.png")
    ImageResource minus();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_move_a.png")
    ImageResource moveIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_move_sw.png")
    ImageResource moveIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_move_i.png")
    ImageResource moveIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_new_a.png")
    ImageResource newIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_new_sw.png")
    ImageResource newIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_new_i.png")
    ImageResource newIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/plus.png")
    ImageResource plus();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_prop_a.png")
    ImageResource propertyIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_prop_sw.png")
    ImageResource propertyIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_prop_i.png")
    ImageResource propertyIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @NotStrict
    @Source("imageSprites.css")
    I_CmsImageStyle style();

}
