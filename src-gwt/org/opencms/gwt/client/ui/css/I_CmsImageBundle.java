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

import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsOpenerHoverCss;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource.NotStrict;
import com.google.gwt.resources.client.CssResource.Shared;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.resources.client.ImageResource.ImageOptions;
import com.google.gwt.resources.client.ImageResource.RepeatStyle;

/**
 * Resource bundle to access CSS and image resources.
 *
 * @since 8.0.0
 */
public interface I_CmsImageBundle extends ClientBundle {

    /** Bundles the image sprite CSS classes. */
    @Shared
    interface I_CmsImageStyle extends I_CmsOpenerHoverCss {

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
        String arrowDownIcon();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String arrowUpIcon();

        /**
         * Access method.<p>
         *
         * @return the CSS class name
         */
        String bullsEyeIcon();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String checkIcon();

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
        String dialogCloseIcon();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String directoryIcon();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String downloadGalleryIcon();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String editIcon();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String htmlGalleryIcon();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String imageSearchIcon();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String infoBigIcon();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String linkGalleryIcon();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String lockedIcon();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String lockIcon();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String menuIcon();

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
        String popupIcon();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String previewIcon();

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
        String tableGalleryIcon();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String tablePreviewIcon();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String tableReplaceIcon();

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
        String uploadSmallIcon();

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
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/addImage.png")
    ImageResource addImage();

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
     * Image resource accessor.<p>
     *
     * @return an image resource
     */
    @Source("images/icons/changedIcon.png")
    ImageResource changedIcon();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/closeImage.png")
    ImageResource closeImage();

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
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/filetypeNavlevelSmall.png")
    ImageResource directorySmallImage();

    /**
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/downloadGallery.png")
    ImageResource downloadGalleryIcon();

    /**
     * Returns the edit cursor icon resource.<p>
     *
     * @return the edit cursor icon
     */
    @Source("images/editCursor.gif")
    DataResource editCursorGif();

    /**
     * Returns the edit cursor icon resource.<p>
     *
     * @return the edit cursor icon
     */
    @Source("images/editCursor.ico")
    DataResource editCursorIco();

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
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/icons/htmlGallery.png")
    ImageResource htmlGalleryIcon();

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
    @Source("images/icons/imageSearchIcon.png")
    ImageResource imageSearchIcon();

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
    @Source("images/icons/linkGallery.png")
    ImageResource linkGalleryIcon();

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
    @Source("images/icons/tableGallery.png")
    ImageResource tableGalleryIcon();

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
     * Access method.<p>
     *
     * @return the image resource
     */
    @Source("images/triangleRightImageDisabled.png")
    ImageResource triangleRightImageDisabled();

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
