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

import org.opencms.ade.galleries.client.preview.ui.CmsFocalPoint;
import org.opencms.ade.galleries.client.preview.util.CmsBoxFit;
import org.opencms.ade.galleries.client.preview.util.CmsCompositeTransform;
import org.opencms.ade.galleries.client.preview.util.CmsRectangle;
import org.opencms.ade.galleries.client.preview.util.CmsTranslate;
import org.opencms.ade.galleries.client.preview.util.I_CmsTransform;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.CmsPoint;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.util.CmsClientStringUtil;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.property.CmsPropertyChangeSet;
import org.opencms.gwt.shared.property.CmsPropertyModification;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;

/**
 * Handles manipulation of the focal point in the gallery dialog.<p>
 */
public class CmsFocalPointController {

    /** Global static flag to enable / disable focal point modification. */
    public static final boolean ENABLED = true;

    /** Preview handler registration for the event handler used for drag / drop. */
    private static HandlerRegistration m_previewRegistration;

    /** The container. */
    private FlowPanel m_container;

    /** The transformation for transforming from the image parent's coordinate system to the true underlying image's native coordinate system. */
    private CmsCompositeTransform m_coordinateTransform;

    /** The source of the cropping parameter information. */
    private Supplier<CmsCroppingParamBean> m_croppingProvider;

    /** The current focal point location. */
    private CmsPoint m_focalPoint;

    /** The currently displayed image. */
    private Image m_image;

    /** The source of the image information. */
    private Supplier<CmsImageInfoBean> m_imageInfoProvider;

    /** The action to execute when the focal point is changed. */
    private Runnable m_nextAction;

    /** The widget representing the focal point. */
    private CmsFocalPoint m_pointWidget;

    /** The region in which the user should be able to move the focal point widget on the screen. */
    private CmsRectangle m_region;

    /** The focal point location which was last saved. */
    private CmsPoint m_savedFocalPoint;

    /**
     * Creates a new instance.<p>
     *
     * @param croppingProvider the source of the cropping information
     * @param infoProvider the source of the image info
     * @param nextAction the action to execute when the focal point is changed
     */
    public CmsFocalPointController(
        Supplier<CmsCroppingParamBean> croppingProvider,
        Supplier<CmsImageInfoBean> infoProvider,
        Runnable nextAction) {

        m_croppingProvider = croppingProvider;
        m_imageInfoProvider = infoProvider;
        m_nextAction = nextAction;
    }

    /**
     * Clears the static event handler for the drag and drop.<p>
     */
    private static void clearEventHandler() {

        if (m_previewRegistration != null) {
            m_previewRegistration.removeHandler();
        }
        m_previewRegistration = null;
    }

    /**
     * Gets the pageX offset for a mouse event.<p>
     *
     * @param event the event
     *
     * @return the pageX offset
     */
    private static native double pageX(NativeEvent event) /*-{
        return event.pageX;
    }-*/;

    /**
     * Gets the pageY offset for a mouse event.<p>
     *
     * @param event the event
     *
     * @return the pageY offset
     */
    private static native double pageY(NativeEvent event) /*-{
        return event.pageY;
    }-*/;

    /**
     * Transforms a rectangle with the inverse of a coordinate transform.<p>
     *
     * @param transform the coordinate transform
     * @param region the rectangle to transform
     * @return the transformed rectangle
     */
    private static CmsRectangle transformRegionBack(I_CmsTransform transform, CmsRectangle region) {

        CmsPoint topLeft = region.getTopLeft();
        CmsPoint bottomRight = region.getBottomRight();
        return CmsRectangle.fromPoints(transform.transformBack(topLeft), transform.transformBack(bottomRight));
    }

    /**
     * Called when the user clicks on the focal point widget.<p>
     *
     * This starts drag and drop.
     */
    public void onStartDrag() {

        if (ENABLED && isEditable()) {
            registerEventHandler();
        }
    }

    /**
     * Saves the focal point to a property on the image.<p>
     */
    public void reset() {

        if (isEditable()) {

            String val = "";
            CmsUUID sid = m_imageInfoProvider.get().getStructureId();
            List<CmsPropertyModification> propChanges = new ArrayList<>();
            propChanges.add(new CmsPropertyModification(sid, CmsGwtConstants.PROPERTY_IMAGE_FOCALPOINT, val, false));
            propChanges.add(new CmsPropertyModification(sid, CmsGwtConstants.PROPERTY_IMAGE_FOCALPOINT, val, true));
            final CmsPropertyChangeSet changeSet = new CmsPropertyChangeSet(sid, propChanges);
            CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                @Override
                public void execute() {

                    CmsCoreProvider.getVfsService().saveProperties(changeSet, false, this);

                }

                @SuppressWarnings("synthetic-access")
                @Override
                protected void onResponse(Void result) {

                    m_focalPoint = null;
                    m_savedFocalPoint = null;
                    m_imageInfoProvider.get().setFocalPoint(null);
                    updatePoint();
                    if (m_nextAction != null) {
                        m_nextAction.run();
                    }
                }
            };
            action.execute();
        }
    }

    /**
     * Updates the image.<p>
     *
     * @param container the parent widget for the image
     * @param previewImage the image
     */
    public void updateImage(FlowPanel container, Image previewImage) {

        if (!ENABLED) {
            return;
        }
        String path = m_imageInfoProvider.get().getResourcePath();
        if (CmsClientStringUtil.checkIsPathOrLinkToSvg(path)) {
            return;
        }
        m_image = previewImage;
        clearImagePoint();
        m_savedFocalPoint = m_imageInfoProvider.get().getFocalPoint();
        m_focalPoint = m_savedFocalPoint;
        m_container = container;

        previewImage.addLoadHandler(new LoadHandler() {

            @SuppressWarnings("synthetic-access")
            public void onLoad(LoadEvent event) {

                updateScaling();
                updatePoint();
            }

        });
    }

    /**
     * Removes the focal point widget.<p>
     */
    private void clearImagePoint() {

        if (m_pointWidget != null) {
            m_pointWidget.removeFromParent();
            m_pointWidget = null;
        }
    }

    /**
     * Gets the point which is the center of the crop region, or the center of the original image if it isn't cropped, in the image's coordinate system.<p>
     *
     * @return the center point of the crop region
     */
    private CmsPoint getCropCenter() {

        CmsCroppingParamBean crop = m_croppingProvider.get();
        CmsImageInfoBean info = m_imageInfoProvider.get();
        if ((crop == null) || !crop.isCropped()) {
            return new CmsPoint(info.getWidth() / 2, info.getHeight() / 2);
        } else {
            return new CmsPoint(
                crop.getCropX() + (crop.getCropWidth() / 2),
                crop.getCropY() + (crop.getCropHeight() / 2));
        }
    }

    /**
     * Gets the rectangle in the image's coordinate system which corresponds to the crop region (or the whole image,
     * in case cropping is not used).<p>
     *
     * @return the crop region
     */
    private CmsRectangle getNativeCropRegion() {

        CmsCroppingParamBean crop = m_croppingProvider.get();
        CmsImageInfoBean info = m_imageInfoProvider.get();
        if ((crop == null) || !crop.isCropped()) {
            return CmsRectangle.fromLeftTopWidthHeight(0, 0, info.getWidth(), info.getHeight());
        } else {
            return CmsRectangle.fromLeftTopWidthHeight(
                crop.getCropX(),
                crop.getCropY(),
                crop.getCropWidth(),
                crop.getCropHeight());
        }
    }

    /**
     * Handles mouse drag.<p>
     *
     * @param nativeEvent the mousemove event
     */
    private void handleMove(NativeEvent nativeEvent) {

        Element imageElem = m_image.getElement();
        int offsetX = ((int)pageX(nativeEvent)) - imageElem.getParentElement().getAbsoluteLeft();
        int offsetY = ((int)pageY(nativeEvent)) - imageElem.getParentElement().getAbsoluteTop();
        if (m_coordinateTransform != null) {
            CmsPoint screenPoint = new CmsPoint(offsetX, offsetY);
            screenPoint = m_region.constrain(screenPoint); // make sure we remain in the screen region corresponding to original image (or crop).
            m_pointWidget.setCenterCoordsRelativeToParent((int)screenPoint.getX(), (int)screenPoint.getY());
            CmsPoint logicalPoint = m_coordinateTransform.transformForward(screenPoint);
            m_focalPoint = logicalPoint;
        }
    }

    /**
     * Check if the image is editable.<p>
     *
     * @return true if the image is editable
     */
    private boolean isEditable() {

        String noEditReason = m_imageInfoProvider.get().getNoEditReason();
        boolean result = CmsStringUtil.isEmptyOrWhitespaceOnly(noEditReason);
        return result;
    }

    /**
     * Registers the preview event handler used for drag and drop.<p>
     */
    private void registerEventHandler() {

        clearEventHandler();
        m_previewRegistration = Event.addNativePreviewHandler(new NativePreviewHandler() {

            @SuppressWarnings("synthetic-access")
            public void onPreviewNativeEvent(NativePreviewEvent event) {

                if (!(m_pointWidget.isAttached())) {
                    clearEventHandler();
                    return;
                }

                NativeEvent nativeEvent = event.getNativeEvent();
                if (nativeEvent == null) {
                    return;
                }
                int eventType = event.getTypeInt();
                switch (eventType) {
                    case Event.ONMOUSEUP:
                        clearEventHandler();
                        save();
                        break;
                    case Event.ONMOUSEMOVE:
                        handleMove(nativeEvent);
                        break;
                    default:
                        break;
                }
            }

        });
    }

    /**
     * Saves the focal point to a property on the image.<p>
     */
    private void save() {

        if ((m_focalPoint != null) && isEditable()) {
            int x = (int)m_focalPoint.getX();
            int y = (int)m_focalPoint.getY();
            String val = "" + x + "," + y;
            CmsUUID sid = m_imageInfoProvider.get().getStructureId();
            List<CmsPropertyModification> propChanges = new ArrayList<>();
            propChanges.add(new CmsPropertyModification(sid, CmsGwtConstants.PROPERTY_IMAGE_FOCALPOINT, val, false));
            final CmsPropertyChangeSet changeSet = new CmsPropertyChangeSet(sid, propChanges);
            CmsRpcAction<Void> action = new CmsRpcAction<Void>() {

                @Override
                public void execute() {

                    CmsCoreProvider.getVfsService().saveProperties(changeSet, false, this);

                }

                @SuppressWarnings("synthetic-access")
                @Override
                protected void onResponse(Void result) {

                    m_savedFocalPoint = m_focalPoint;

                    if (m_pointWidget != null) {
                        m_pointWidget.setIsDefault(false);
                    }
                    m_imageInfoProvider.get().setFocalPoint(m_focalPoint);
                    if (m_nextAction != null) {
                        m_nextAction.run();
                    }

                }
            };
            action.execute();

        }
    }

    /**
     * Updates the focal point widget.<p>
     */
    private void updatePoint() {

        clearImagePoint();
        CmsPoint nativePoint;
        if (m_focalPoint == null) {
            CmsPoint cropCenter = getCropCenter();
            nativePoint = cropCenter;
        } else if (!getNativeCropRegion().contains(m_focalPoint)) {
            return;
        } else {
            nativePoint = m_focalPoint;
        }
        m_pointWidget = new CmsFocalPoint(CmsFocalPointController.this);
        boolean isDefault = m_savedFocalPoint == null;
        m_pointWidget.setIsDefault(isDefault);
        m_container.add(m_pointWidget);
        CmsPoint screenPoint = m_coordinateTransform.transformBack(nativePoint);
        m_pointWidget.setCenterCoordsRelativeToParent((int)screenPoint.getX(), (int)screenPoint.getY());
    }

    /**
     * Sets up the coordinate transformations between the coordinate system of the parent element of the image element and the native coordinate system
     * of the original image.
     */
    private void updateScaling() {

        List<I_CmsTransform> transforms = new ArrayList<>();
        CmsCroppingParamBean crop = m_croppingProvider.get();
        CmsImageInfoBean info = m_imageInfoProvider.get();

        double wv = m_image.getElement().getParentElement().getOffsetWidth();
        double hv = m_image.getElement().getParentElement().getOffsetHeight();
        if (crop == null) {
            transforms.add(
                new CmsBoxFit(CmsBoxFit.Mode.scaleOnlyIfNecessary, wv, hv, info.getWidth(), info.getHeight()));
        } else {
            int wt, ht;
            wt = crop.getTargetWidth() >= 0 ? crop.getTargetWidth() : info.getWidth();
            ht = crop.getTargetHeight() >= 0 ? crop.getTargetHeight() : info.getHeight();
            transforms.add(new CmsBoxFit(CmsBoxFit.Mode.scaleOnlyIfNecessary, wv, hv, wt, ht));
            if (crop.isCropped()) {
                transforms.add(
                    new CmsBoxFit(CmsBoxFit.Mode.scaleAlways, wt, ht, crop.getCropWidth(), crop.getCropHeight()));
                transforms.add(new CmsTranslate(crop.getCropX(), crop.getCropY()));
            } else {
                transforms.add(
                    new CmsBoxFit(CmsBoxFit.Mode.scaleAlways, wt, ht, crop.getOrgWidth(), crop.getOrgHeight()));
            }
        }
        CmsCompositeTransform chain = new CmsCompositeTransform(transforms);
        m_coordinateTransform = chain;
        if ((crop == null) || !crop.isCropped()) {
            m_region = transformRegionBack(
                m_coordinateTransform,
                CmsRectangle.fromLeftTopWidthHeight(0, 0, info.getWidth(), info.getHeight()));
        } else {
            m_region = transformRegionBack(
                m_coordinateTransform,
                CmsRectangle.fromLeftTopWidthHeight(
                    crop.getCropX(),
                    crop.getCropY(),
                    crop.getCropWidth(),
                    crop.getCropHeight()));
        }
    }

}
