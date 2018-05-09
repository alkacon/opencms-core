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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.shared.CmsImageInfoBean;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.Size;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Widget for displaying image information.<p>
 */
public class CmsImageInfoDisplay extends Composite {

    /**
     * @see com.google.gwt.uibinder.client.UiBinder
     */
    protected interface I_CmsImageInfosTabUiBinder extends UiBinder<Widget, CmsImageInfoDisplay> {
        // GWT interface, nothing to do here
    }

    /** The ui-binder instance for this class. */
    private static I_CmsImageInfosTabUiBinder m_uiBinder = GWT.create(I_CmsImageInfosTabUiBinder.class);

    /** Label. */
    @UiField
    protected Label m_displayCropFormat;

    /** The format label. */
    @UiField
    protected Label m_displayFormat;

    /** The path label. */
    @UiField
    protected Label m_displayPath;

    /** Label. */
    @UiField
    protected Label m_displayPoint;

    /** The type label. */
    @UiField
    protected Label m_displayType;

    /** Label. */
    @UiField
    protected Label m_labelCropFormat;

    /** The format label. */
    @UiField
    protected Label m_labelFormat;

    /** The path label. */
    @UiField
    protected Label m_labelPath;

    /** Label. */
    @UiField
    protected Label m_labelPoint;

    /** The type label. */
    @UiField
    protected Label m_labelType;

    /** Button for removing crop. */
    @UiField
    protected CmsPushButton m_removeCrop;

    /** Button for removing focal point. */
    @UiField
    protected CmsPushButton m_removePoint;

    /**
     * The constructor.<p>
     *
     * @param removeCropAction action to remove the cropping
     * @param removePointAction action to remove the image point
    */
    public CmsImageInfoDisplay(Runnable removeCropAction, Runnable removePointAction) {

        initWidget(m_uiBinder.createAndBindUi(this));

        m_labelPath.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_PATH_0));
        m_labelFormat.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_FORMAT_0));
        m_labelType.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_TYPE_0));
        m_labelPoint.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_FOCALPOINT_0));
        m_labelCropFormat.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_CROPFORMAT_0));
        m_removeCrop.addClickHandler(e -> removeCropAction.run());
        m_removePoint.addClickHandler(e -> removePointAction.run());
        setButtonStyle(m_removeCrop);
        setButtonStyle(m_removePoint);
    }

    /**
     * Initializes a button.<p>
     *
     * @param button the button to initialize
     */
    private static void setButtonStyle(CmsPushButton button) {

        button.setSize(Size.small);
        button.setHeight("20px");

        button.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_REMOVE_0));
    }

    /**
     * Fills the contend data.<p>
     *
     * @param infoBean the image info bean
     * @param crop the cropping text
     * @param point the focal point text
     */
    public void fillContent(CmsResourceInfoBean infoBean, String crop, String point) {

        m_displayPath.setText(infoBean.getResourcePath());
        if (infoBean instanceof CmsImageInfoBean) {
            CmsImageInfoBean imageInfo = (CmsImageInfoBean)infoBean;
            m_displayFormat.setText(imageInfo.getWidth() + " x " + imageInfo.getHeight());
        }
        String path = infoBean.getResourcePath();
        String typePrefix = "???";
        int slashPos = path.lastIndexOf("/");
        String name;
        if (slashPos >= 0) {
            name = path.substring(slashPos + 1);
        } else {
            name = path;
        }
        int dotPos = name.lastIndexOf(".");
        if (dotPos >= 0) {
            String extension = name.substring(dotPos + 1).toLowerCase();
            if ("jpg".equals(extension) || "jpeg".equals(extension)) {
                typePrefix = "JPEG";
            } else {
                typePrefix = extension.toUpperCase();
            }
        }
        m_removePoint.setEnabled(CmsStringUtil.isEmptyOrWhitespaceOnly(infoBean.getNoEditReason()));
        m_displayType.setText(Messages.get().key(Messages.GUI_PREVIEW_VALUE_TYPE_1, typePrefix));
        setCropFormat(crop);
        setFocalPoint(point);
    }

    /**
     * Sets the crop format.<p>
     *
     * @param cropFormat the crop format
     */
    public void setCropFormat(String cropFormat) {

        boolean visible = (cropFormat != null);
        if (cropFormat == null) {
            cropFormat = "";
        }
        m_labelCropFormat.setVisible(visible);
        m_removeCrop.setVisible(visible);
        m_displayCropFormat.setText(cropFormat);
    }

    /**
     * Sets the focal point.<p>
     *
     * @param focalPoint the focal point
     */
    public void setFocalPoint(String focalPoint) {

        boolean visible = (focalPoint != null);
        if (focalPoint == null) {
            focalPoint = "";
        }
        m_labelPoint.setVisible(visible);
        m_removePoint.setVisible(visible);
        m_displayPoint.setText(focalPoint);
    }

}
