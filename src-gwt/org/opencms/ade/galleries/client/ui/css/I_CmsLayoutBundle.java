/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.ade.galleries.client.ui.css;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.resources.client.CssResource.Shared;

/**
 * Resource bundle to access CSS and image resources of the gallery dialog.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsLayoutBundle extends org.opencms.gwt.client.ui.css.I_CmsLayoutBundle {

    /** Cropping dialog CSS. */
    interface I_CmsCroppingDialogCss extends CssResource {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String bottomPanel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String button();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String croppingPanel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String info();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String panel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String topPanel();
    }

    /** Gallery dialog CSS. */
    @Shared
    interface I_CmsGalleryDialogCss extends CssResource {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String criteriaList();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String editorGallery();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String galleryBody();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String hasButton();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String infoLabel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String listIcon();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String paramsText();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String parentPanel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String quickFilterBox();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String resultTabUpload();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String searchTabBorderLayer();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String searchTabDesMargin();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String searchTabInputLeft();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String searchTabInputRight();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String searchTabInputWide();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String searchTabLabelLeft();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String searchTabLabelRight();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String searchTabRow();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String searchTabRowRightAlign();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String selectboxWidth();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String shouldOnlyShowInFullTypeList();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String showParams();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String showPreview();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String tabOptions();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String tabParamsPanel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String typeModeSwitch();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String typesImportant();
    }

    /** The base gallery field CSS. */
    @Shared
    interface I_CmsGalleryFieldBaseCss extends CssResource {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String fieldBox();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String galleryField();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String imagePreview();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String resourceInfo();
    }

    /** The gallery field CSS. */
    @Shared
    interface I_CmsGalleryFieldCss extends I_CmsGalleryFieldBaseCss {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String descriptionField();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String dropZoneHover();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String fader();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String formats();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String hasImage();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String hasUpload();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String inputContainer();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String opener();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String uploadButton();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String uploadDropZone();
    }

    /** The result item CSS. */
    @Shared
    interface I_CmsGalleryResultItemCss extends I_CmsListItemWidgetCss {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String bigImage();

        /**
         * Big image height constant.<p>
         *
         * @return the big image height
         */
        int bigImageHeight();

        /**
         * Big image width constant.<p>
         *
         * @return the big image width
         */
        int bigImageWidth();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String expiredImageOverlay();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String imageTile();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String smallImage();

        /**
         * Small image height constant.<p>
         *
         * @return the small image height
         */
        int smallImageHeight();

        /**
         * Small image width constant.<p>
         *
         * @return the small image width
         */
        int smallImageWidth();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String smallThumbnails();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String tilingItem();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String tilingList();
    }

    /** The advanced image editor form CSS. */
    interface I_CmsImageAdvancedFormCss extends CssResource {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String descriptionLabel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String input();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String linkWidget();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String main();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String selectBox();
    }

    /** The image editor form CSS. */
    interface I_CmsImageEditorFormCss extends CssResource {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String floatCheckbox();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String inlineLabel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String inputTextSmall();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String label();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String selectBox();
    }

    /** Preview dialog CSS. */
    @Shared
    interface I_CmsPreviewDialogCss extends CssResource {

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String clearFix();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String detailsHolder();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String formatButton();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String formatLabel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String formatSelectBox();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String formatsLine();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String formatsLineSize();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String formatsPanel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String formatText();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String hidePreview();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String hiding();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String imagePanel();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String infoTable();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String inputField();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String labelField();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String previewButton();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String previewButtonBar();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String previewCloseButton();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String previewDialog();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String previewHolder();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String previewPanel();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String previewTitle();

        /** Access method.<p>
        *
        * @return the CSS class name
        */
        String previewVisible();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String propertiesList();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String propertyLeft();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String propertyRight();

        /** Access method.<p>
         *
         * @return the CSS class name
         */
        String savePropertiesButton();
    }

    /** The bundle instance. */
    I_CmsLayoutBundle INSTANCE = GWT.create(I_CmsLayoutBundle.class);

    /**
     * Access method.<p>
     *
     * @return the gallery dialog CSS
     */
    @Source("croppingDialog.css")
    I_CmsCroppingDialogCss croppingDialogCss();

    /**
     * Access method.<p>
     *
     * @return the gallery dialog CSS
     */
    @Source("gallerydialog.css")
    I_CmsGalleryDialogCss galleryDialogCss();

    /**
     * Access method.<p>
     *
     * @return the gallery field CSS
     */
    @Source("galleryField.css")
    I_CmsGalleryFieldCss galleryFieldCss();

    /**
     * Access method.<p>
     *
     * @return the gallery result item CSS
     */
    @Source("galleryResultItem.css")
    I_CmsGalleryResultItemCss galleryResultItemCss();

    /**
     * Access method.<p>
     *
     * @return the image advanced form CSS
     */
    @Import(value = {org.opencms.gwt.client.ui.css.I_CmsInputCss.class})
    @Source("imageAdvancedForm.css")
    I_CmsImageAdvancedFormCss imageAdvancedFormCss();

    /**
     * Access method.<p>
     *
     * @return the image editor form CSS
     */
    @Import(value = {org.opencms.gwt.client.ui.css.I_CmsInputCss.class})
    @Source("imageEditorForm.css")
    I_CmsImageEditorFormCss imageEditorFormCss();

    /**
     * Access method.<p>
     *
     * @return the gallery dialog CSS
     */
    @Source("previewdialog.css")
    I_CmsPreviewDialogCss previewDialogCss();

}
