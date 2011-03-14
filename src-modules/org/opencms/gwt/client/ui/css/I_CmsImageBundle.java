/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/css/Attic/I_CmsImageBundle.java,v $
 * Date   : $Date: 2011/03/14 16:07:26 $
 * Version: $Revision: 1.23 $
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
 * @version $Revision: 1.23 $
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
        String availability();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String bump();

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
        String refresh();

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
        String addIcon();

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
        String editIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String lockedIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String moveIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String opencmsSymbol();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String propertyIcon();

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
        String searchIcon();

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

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String uploadIcon();
    }

    /** The bundle instance. */
    I_CmsImageBundle INSTANCE = GWT.create(I_CmsImageBundle.class);

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/arrowBottom.png")
    ImageResource arrowBottom();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/arrowRight.png")
    ImageResource arrowRight();

    /**
     * Accessor for the big ícon resource bundle.<p>
     * 
     * @return the big icon resource bundle 
     */
    I_CmsBigIconBundle bigIcons();

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
    ImageResource crop();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/error.png")
    ImageResource error();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/group.png")
    ImageResource group();

    /**
     * Accessor for the icon resource bundle.<p>
     * 
     * @return the icon resource bundle
     */
    I_CmsIconBundle icons();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/inherited.png")
    ImageResource inherited();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/loadingSmall.gif")
    ImageResource loadingSmall();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/lockOther.gif")
    ImageResource lockOther();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/lockShared.gif")
    ImageResource lockShared();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ratioLocked.gif")
    ImageResource ratioLocked();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ratioUnlocked.gif")
    ImageResource ratioUnlocked();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/lockUser.gif")
    ImageResource lockUser();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/opencmsSymbol.png")
    ImageResource opencmsSymbol();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/cropRemove.png")
    ImageResource cropRemove();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/resetSize.gif")
    ImageResource resetSize();

    /**
     * Access method.<p>
     * 
     * @return the button CSS
     */
    @NotStrict
    @Source("imageSprites.css")
    I_CmsImageStyle style();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/user.png")
    ImageResource user();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/warningBig.png")
    ImageResource warningBig();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/warningSmall.png")
    ImageResource warningSmall();

    /** 
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/iconsActive.png")
    ImageResource iconsActive();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/iconsDefault.png")
    ImageResource iconsDefault();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/loadingBig.gif")
    ImageResource loadingBig();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/listItemMinus.png")
    ImageResource listItemMinus();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/placeholderOverlayTrans.png")
    @ImageOptions(repeatStyle = RepeatStyle.Both)
    ImageResource placeholderOverlayTrans();

    /**
     * Access method.<p>
     * 
     * @return the image resource 
     */
    @Source("images/listItemPlus.png")
    ImageResource listItemPlus();

}
