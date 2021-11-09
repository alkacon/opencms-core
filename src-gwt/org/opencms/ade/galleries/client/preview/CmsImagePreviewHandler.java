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

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.client.preview.ui.CmsImagePreviewDialog;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.util.I_CmsSimpleCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * Image preview dialog controller handler.<p>
 *
 * Delegates the actions of the preview controller to the preview dialog.
 *
 * @since 8.0.0
 */
public class CmsImagePreviewHandler extends A_CmsPreviewHandler<CmsImageInfoBean>
implements ValueChangeHandler<CmsCroppingParamBean> {

    /** Enumeration of image tag attribute names. */
    public enum Attribute {
        /** Image align attribute. */
        align,
        /** Image alt attribute. */
        alt,
        /** Image class attribute. */
        clazz,
        /** Image copyright info. */
        copyright,
        /** Image direction attribute. */
        dir,
        /** No image selected if this attribute is present. */
        emptySelection,
        /** The image hash. */
        hash,
        /** Image height attribute. */
        height,
        /** Image hspace attribute. */
        hspace,
        /** Image id attribute. */
        id,
        /** Image copyright flag. */
        insertCopyright,
        /** Image link original flag. */
        insertLinkOrig,
        /** Image spacing flag. */
        insertSpacing,
        /** Image subtitle flag. */
        insertSubtitle,
        /** Image language attribute. */
        lang,
        /** Image link path. */
        linkPath,
        /** Image link target. */
        linkTarget,
        /** Image longDesc attribute. */
        longDesc,
        /** Image style attribute. */
        style,
        /** Image title attribute. */
        title,
        /** Image vspace attribute. */
        vspace,
        /** Image width attribute. */
        width
    }

    /** List of handlers for cropping changes. */
    private List<Runnable> m_croppingHandlers = new ArrayList<>();

    /** The cropping parameter. */
    private CmsCroppingParamBean m_croppingParam;

    /** The image format handler. */
    private CmsImageFormatHandler m_formatHandler;

    /** List of handlers for focal point changes. */
    private List<Runnable> m_imagePointHandlers = new ArrayList<>();

    /** The focal point controller. */
    private CmsFocalPointController m_pointController;

    /** The preview dialog. */
    private CmsImagePreviewDialog m_previewDialog;

    /** The image container width. */
    private int m_containerWidth;

    /** The image container height. */
    private int m_containerHeight;

    /**
     * Constructor.<p>
     *
     * @param resourcePreview the resource preview instance
     */
    public CmsImagePreviewHandler(CmsImageResourcePreview resourcePreview) {

        super(resourcePreview);
        m_previewDialog = resourcePreview.getPreviewDialog();
        m_pointController = new CmsFocalPointController(
            () -> m_croppingParam,
            this::getImageInfo,
            this::onImagePointChanged);
    }

    /**
     * Adds a handler for cropping changes.<p>
     *
     * @param action the handler to add
     */
    public void addCroppingChangeHandler(Runnable action) {

        m_croppingHandlers.add(action);
    }

    /**
     * Adds a handler for focal point changes.<p>
     *
     * @param onImagePointChanged the handler to add
     */
    public void addImagePointChangeHandler(Runnable onImagePointChanged) {

        m_imagePointHandlers.add(onImagePointChanged);
    }

    /**
     * Returns the image cropping parameter bean.<p>
     *
     * @return the image cropping parameter bean
     */
    public CmsCroppingParamBean getCroppingParam() {

        return m_croppingParam;
    }

    /**
     * Gets the focal point controller.<p>
     *
     * @return the focal point controller
     */
    public CmsFocalPointController getFocalPointController() {

        return m_pointController;
    }

    /**
     * Gets the format handler.<p>
     *
     * @return the format handler
     */
    public CmsImageFormatHandler getFormatHandler() {

        return m_formatHandler;

    }

    /**
     * Returns the name of the currently selected image format.<p>
     *
     * @return the format name
     */
    public String getFormatName() {

        String result = "";
        if ((m_formatHandler != null) && (m_formatHandler.getCurrentFormat() != null)) {
            result = m_formatHandler.getCurrentFormat().getName();
        }
        return result;
    }

    /**
     * Returns image tag attributes to set for editor plugins.<p>
     *
     * @param callback the callback to execute
     */
    public void getImageAttributes(I_CmsSimpleCallback<Map<String, String>> callback) {

        Map<String, String> result = new HashMap<String, String>();
        result.put(Attribute.hash.name(), String.valueOf(getImageIdHash()));
        m_formatHandler.getImageAttributes(result);
        m_previewDialog.getImageAttributes(result, callback);
    }

    /**
     * Returns the structure id hash of the previewed image.<p>
     *
     * @return the structure id hash
     */
    public int getImageIdHash() {

        return m_resourceInfo.getHash();
    }

    /**
     * Gets the image information.<p>
     *
     * @return the image information
     */
    public CmsImageInfoBean getImageInfo() {

        return m_resourceInfo;
    }

    /**
     * Returns the cropping parameter.<p>
     *
     * @param imageHeight the original image height
     * @param imageWidth the original image width
     *
     * @return the cropping parameter
     */
    public String getPreviewScaleParam(int imageHeight, int imageWidth) {

        int maxHeight = m_containerHeight;
        int maxWidth = m_containerWidth;

        if ((m_croppingParam != null) && (m_croppingParam.isCropped() || m_croppingParam.isScaled())) {
            // NOTE: getREstrictedSizeScaleParam does not work correctly if there isn't actually any cropping/scaling, so we explicitly don't use it in this case
            return m_croppingParam.getRestrictedSizeScaleParam(maxHeight, maxWidth);
        }
        if ((imageHeight <= maxHeight) && (imageWidth <= maxWidth)) {
            return ""; // dummy parameter, doesn't actually do anything
        }
        CmsCroppingParamBean restricted = new CmsCroppingParamBean();

        boolean tooHigh = imageHeight > maxHeight;
        boolean tooWide = imageWidth > maxWidth;
        double shrinkX = (1.0 * imageWidth) / maxWidth;
        double shrinkY = (1.0 * imageHeight) / maxHeight;
        double aspectRatio = (1.0 * imageWidth) / imageHeight;
        if (tooHigh && tooWide) {
            if (shrinkX > shrinkY) {
                restricted.setTargetWidth(maxWidth);
                restricted.setTargetHeight((int)(maxWidth / aspectRatio));
            } else {
                restricted.setTargetHeight(maxHeight);
                restricted.setTargetWidth((int)(maxHeight * aspectRatio));
            }
        } else if (tooWide) {
            restricted.setTargetWidth(maxWidth);
            restricted.setTargetHeight((int)(maxWidth / aspectRatio));
        } else if (tooHigh) {
            restricted.setTargetHeight(maxHeight);
            restricted.setTargetWidth((int)(maxHeight * aspectRatio));
        } else {
            restricted.setTargetWidth(imageWidth);
            restricted.setTargetHeight(imageHeight);
        }
        return restricted.toString();
    }

    /**
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public void onValueChange(ValueChangeEvent<CmsCroppingParamBean> event) {

        m_croppingParam = event.getValue();
        String viewLink = m_resourcePreview.getViewLink();
        if (viewLink == null) {
            viewLink = CmsCoreProvider.get().link(m_resourcePreview.getResourcePath());
        }
        m_previewDialog.resetPreviewImage(
            viewLink + "?" + getPreviewScaleParam(m_croppingParam.getOrgHeight(), m_croppingParam.getOrgWidth()));
        onCroppingChanged();
    }

    /**
     * Sets the image format handler.<p>
     *
     * @param formatHandler the format handler
     */
    public void setFormatHandler(CmsImageFormatHandler formatHandler) {

        m_formatHandler = formatHandler;
        m_croppingParam = m_formatHandler.getCroppingParam();
        m_formatHandler.addValueChangeHandler(this);
        onCroppingChanged();
    }

    /**
     *
     * Sets the dimensions of the area the image is going to be placed in.
     *
     * @param offsetWidth the container width
     * @param offsetHeight the container height
     */
    public void setImageContainerSize(int offsetWidth, int offsetHeight) {

        m_containerWidth = offsetWidth;
        m_containerHeight = offsetHeight;
    }

    /**
     * Calls all cropping change handlers.
     */
    private void onCroppingChanged() {

        for (Runnable action : m_croppingHandlers) {
            action.run();
        }
    }

    /**
     * Calls all focal point change handlers.<p>
     */
    private void onImagePointChanged() {

        for (Runnable handler : m_imagePointHandlers) {
            handler.run();
        }

    }

}
