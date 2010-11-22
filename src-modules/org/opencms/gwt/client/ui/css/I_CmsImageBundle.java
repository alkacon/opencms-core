/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/css/Attic/I_CmsImageBundle.java,v $
 * Date   : $Date: 2010/11/22 13:50:23 $
 * Version: $Revision: 1.13 $
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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsContextmenuItemCss;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.CssResource.Shared;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

/**
 * Resource bundle to access CSS and image resources.
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.13 $
 * 
 * @since 8.0.0
 */
public interface I_CmsImageBundle extends ClientBundle {

    /** The context menu item CSS classes. */
    @Shared
    public interface I_CmsContextMenuIcons extends I_CmsContextmenuItemCss {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String delete();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String edit();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String gotoPage();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String gotoParent();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String gotoSub();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String mergeSitemap();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String move();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String newElement();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String properties();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String selection();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String subSitemap();

    }

    /** Bundles the image sprite CSS classes. */
    @Shared
    interface I_CmsImageStyle extends CssResource {

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String croppingIcon();

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
        String lockedIcon();

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
        String opencmsLogo();

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

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String removeCroppingIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String resetIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarAdd();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarClipboard();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarEdit();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarExit();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarMove();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarNew();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarProperties();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarPublish();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarRecent();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarRemove();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarSave();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String toolbarSitemap();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String unlockedIcon();

    }

    /** The bundle instance. */
    I_CmsImageBundle INSTANCE = GWT.create(I_CmsImageBundle.class);

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ade_favorites_20x20_a.png")
    ImageResource addFavoriteIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ade_favorites_20x20_sw.png")
    ImageResource addFavoriteIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ade_favorites_20x20_i.png")
    ImageResource addFavoriteIconInactive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ui-bg_highlight-soft_75_aaaaaa_1x100.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource backgroundSoft();

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @Source("contextMenuIcons.css")
    I_CmsContextMenuIcons contextMenuIcons();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/crop.png")
    ImageResource croppingIcon();

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
    @Source("images/goto_20x20.png")
    ImageResource gotoPageIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/goto_disabled_20x20.png")
    ImageResource gotoPageIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/parent_20x20.png")
    ImageResource gotoParentIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/parent_disabled_20x20.png")
    ImageResource gotoParentIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/goto_sub_20x20.png")
    ImageResource gotoSubIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/loading.gif")
    ImageResource loading();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/locked.gif")
    ImageResource locked();

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
    @Source("images/merge_sitemap_20x20.png")
    ImageResource mergeSitemapIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/merge_sitemap_disabled_20x20.png")
    ImageResource mergeSitemapIconDeactivated();

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
    @Source("images/opencms_logo_16.png")
    ImageResource opencmsLogo();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/placeholderOverlay_trans.png")
    @ImageOptions(repeatStyle = RepeatStyle.Both)
    ImageResource overlayImage();

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
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/cropremove.png")
    ImageResource removeCroppingIcon();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/reset.gif")
    ImageResource reset();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_selection_a.png")
    ImageResource selectionIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_selection_sw.png")
    ImageResource selectionIconDeactivated();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ocms_de_selection_i.png")
    ImageResource selectionIconInactive();

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
    @Source("images/subsitemap_20x20.png")
    ImageResource subSitemapIconActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/subsitemap_disabled_20x20.png")
    ImageResource subSitemapIconDeactivated();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_add.png")
    ImageResource toolbarAdd();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_add_sw.png")
    ImageResource toolbarAddSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_favorites.png")
    ImageResource toolbarClipboard();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_favorites_sw.png")
    ImageResource toolbarClipboardSW();

    /** 
     * Access method.<p>
     * @return an image resource
     * 
     * TODO: change the image
     */
    @Source("images/toolbaricons/ade_add.png")
    ImageResource toolbarContext();

    /** 
     * Access method.<p>
     * @return an image resource
     * 
     * TODO: change the image
     */
    @Source("images/toolbaricons/ade_add_sw.png")
    ImageResource toolbarContextSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_edit.png")
    ImageResource toolbarEdit();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_edit_sw.png")
    ImageResource toolbarEditSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_exit.png")
    ImageResource toolbarExit();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_exit_sw.png")
    ImageResource toolbarExitSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_move.png")
    ImageResource toolbarMove();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_move_sw.png")
    ImageResource toolbarMoveSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_new.png")
    ImageResource toolbarNew();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_new_sw.png")
    ImageResource toolbarNewSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_prop.png")
    ImageResource toolbarProperties();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_prop_sw.png")
    ImageResource toolbarPropertiesSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_publish.png")
    ImageResource toolbarPublish();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_publish_sw.png")
    ImageResource toolbarPublishSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_recent.png")
    ImageResource toolbarRecent();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_recent_sw.png")
    ImageResource toolbarRecentSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_delete.png")
    ImageResource toolbarRemove();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_delete_sw.png")
    ImageResource toolbarRemoveSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_reset.png")
    ImageResource toolbarReset();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_reset_sw.png")
    ImageResource toolbarResetSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_save.png")
    ImageResource toolbarSave();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_save_sw.png")
    ImageResource toolbarSaveSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_selection.png")
    ImageResource toolbarSelection();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_selection_sw.png")
    ImageResource toolbarSelectionSW();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_sitemap.png")
    ImageResource toolbarSitemap();

    /** 
     * Access method.<p>
     * @return an image resource
     */
    @Source("images/toolbaricons/ade_sitemap_sw.png")
    ImageResource toolbarSitemapSW();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/unlocked.gif")
    ImageResource unlocked();

}
