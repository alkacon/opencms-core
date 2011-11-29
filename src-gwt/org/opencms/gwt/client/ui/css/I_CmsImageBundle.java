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
        String lock();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String logout();

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
        String remove();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String subSitemap();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String workplace();

    }

    /** Bundles the image sprite CSS classes. */
    @Shared
    interface I_CmsImageStyle extends CssResource {

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String addIcon();

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String changeOrderIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String closeIcon();

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

        /** 
         * Access method.<p>
         * 
         * @return the CSS class name
         */
        String removeIcon();

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
        String triangleDown();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String triangleRight();

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

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String warningBigIcon();

        /** Access method.<p>
         * 
         * @return the CSS class name
         */
        String warningIcon();
    }

    /** The bundle instance. */
    I_CmsImageBundle INSTANCE = GWT.create(I_CmsImageBundle.class);

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/arrowBottomImage.png")
    ImageResource arrowBottomImage();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/arrowRightImage.png")
    ImageResource arrowRightImage();

    /**
     * Accessor for the big icon resource bundle.<p>
     * 
     * @return the big icon resource bundle 
     */
    I_CmsBigIconBundle bigIcons();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/broken_image.png")
    ImageResource brokenImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource 
     */
    @Source("images/closeImage.png")
    ImageResource closeImage();

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
    @Source("images/cropImage.png")
    ImageResource cropImage();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/cropRemoveImage.png")
    ImageResource cropRemoveImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/dialogCloseImage.png")
    ImageResource dialogCloseImage();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/errorImage.png")
    ImageResource errorImage();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/groupImage.png")
    ImageResource groupImage();

    /**
     * Accessor for the icon resource bundle.<p>
     * 
     * @return the icon resource bundle
     */
    I_CmsIconBundle icons();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/informationBigImage.png")
    @ImageOptions(repeatStyle = RepeatStyle.Both)
    ImageResource informationBigImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/invalidElement.png")
    @ImageOptions(repeatStyle = RepeatStyle.Both)
    ImageResource invalidElement();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/loadingBigImage.gif")
    ImageResource loadingBigImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/loadingSmallImage.gif")
    ImageResource loadingSmallImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/lockOtherImage.png")
    ImageResource lockOtherImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/lockSharedImage.png")
    ImageResource lockSharedImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/lockUserImage.png")
    ImageResource lockUserImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/menuArrowBottomImage.png")
    ImageResource menuArrowBottomImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/menuArrowTopImage.png")
    ImageResource menuArrowTopImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/minusImage.png")
    ImageResource minusImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/opencmsSymbolImage.png")
    ImageResource opencmsSymbolImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/placeholderImage.png")
    @ImageOptions(repeatStyle = RepeatStyle.Both)
    ImageResource placeholderImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource 
     */
    @Source("images/plusImage.png")
    ImageResource plusImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ratioLockedImage.png")
    ImageResource ratioLockedImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/ratioUnlockedImage.png")
    ImageResource ratioUnlockedImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/resetSizeImage.png")
    ImageResource resetSizeImage();

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
    @Source("images/triangleDownImage.png")
    ImageResource triangleDownImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource 
     */
    @Source("images/triangleRightImage.png")
    ImageResource triangleRightImage();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/userImage.png")
    ImageResource userImage();

    /**
     * Image resource accessor.<p>
     * 
     * @return an image resource
     */
    @Source("images/warningBigImage.png")
    @ImageOptions(repeatStyle = RepeatStyle.Both)
    ImageResource warningBigImage();

    /**
     * Access method.<p>
     * 
     * @return the image resource
     */
    @Source("images/warningSmallImage.png")
    ImageResource warningSmallImage();
}
