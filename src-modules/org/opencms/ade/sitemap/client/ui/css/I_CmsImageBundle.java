/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/ui/css/Attic/I_CmsImageBundle.java,v $
 * Date   : $Date: 2010/12/21 10:23:32 $
 * Version: $Revision: 1.15 $
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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsDragCss;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

/**
 * Resource bundle to access CSS and image resources.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.15 $
 * 
 * @since 8.0.0
 */
public interface I_CmsImageBundle extends ClientBundle {

    /** The button CSS. */
    public interface I_CmsButtonCss extends I_CmsDragCss {

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String context();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String hoverbar();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String hoverbarContext();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String hoverbarDelete();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String hoverbarEdit();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String hoverbarGoto();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String hoverbarGotoSub();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String hoverbarMergeSitemap();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String hoverbarMove();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String hoverbarNew();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String hoverbarParent();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String hoverbarSubsitemap();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarRedo();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarUndo();
    }

    /** The bundle instance. */
    I_CmsImageBundle INSTANCE = GWT.create(I_CmsImageBundle.class);

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/arrow_left_green.png")
    ImageResource arrowLeft();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/broken_link.png")
    ImageResource brokenLink();

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @Source("button.css")
    I_CmsButtonCss buttonCss();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/context.png")
    ImageResource hoverbarContext();

    /** 
     * Access method.<p>
     *  
     * @return an image resource
     */
    @Source("images/context_disabled.png")
    ImageResource hoverbarContextDisabled();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/context_down.png")
    ImageResource hoverbarContextDown();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/delete.png")
    ImageResource hoverbarDelete();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/delete_disabled.png")
    ImageResource hoverbarDeleteDisabled();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/edit.png")
    ImageResource hoverbarEdit();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/edit_disabled.png")
    ImageResource hoverbarEditDisabled();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/goto.png")
    ImageResource hoverbarGoto();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/goto_disabled.png")
    ImageResource hoverbarGotoDisabled();

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @Source("images/goto_sub.png")
    ImageResource hoverbarGotoSub();

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @Source("images/goto_disabled.png")
    ImageResource hoverbarGotoSubDisabled();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/merge_sitemap.png")
    ImageResource hoverbarMergeSitemap();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/merge_sitemap_disabled.png")
    ImageResource hoverbarMergeSitemapDisabled();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/move.png")
    ImageResource hoverbarMove();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/move_disabled.png")
    ImageResource hoverbarMoveDisabled();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/new.png")
    ImageResource hoverbarNew();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/new_disabled.png")
    ImageResource hoverbarNewDisabled();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/parent.png")
    ImageResource hoverbarParent();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/parent_disabled.png")
    ImageResource hoverbarParentDisabled();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/subsitemap.png")
    ImageResource hoverbarSubsitemap();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/subsitemap_disabled.png")
    ImageResource hoverbarSubsitemapDisabled();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/placeholderOverlay_trans.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource positionIndicator();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/redo.png")
    ImageResource toolbarRedo();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/redo_disabled.png")
    ImageResource toolbarRedoDisabled();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/undo.png")
    ImageResource toolbarUndo();

    /** 
     * Access method.<p>
     * 
     * @return an image resource
     */
    @Source("images/undo_disabled.png")
    ImageResource toolbarUndoDisabled();

}
