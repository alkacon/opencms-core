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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.Messages;
import org.opencms.ade.galleries.client.preview.CmsCroppingParamBean;
import org.opencms.gwt.client.ui.CmsAreaSelectPanel;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Image cropping dialog.<p>
 * 
 * @since 8.0.0
 */
public class CmsCroppingDialog extends Composite
implements ValueChangeHandler<CmsPositionBean>, HasValueChangeHandlers<CmsCroppingParamBean> {

    /** The ui-binder for this widget. */
    interface I_CmsCroppingDialogUiBinder extends UiBinder<Widget, CmsCroppingDialog> {
        // GWT interface, nothing to do
    }

    /** The empty field string. */
    private static final String EMPTY_FIELD = "---";

    /** The ui-binder interface. */
    private static I_CmsCroppingDialogUiBinder m_uiBinder = GWT.create(I_CmsCroppingDialogUiBinder.class);

    /** The cancel button. */
    @UiField
    protected CmsPushButton m_cancelButton;

    /** The cropping panel. */
    @UiField
    protected CmsAreaSelectPanel m_croppingPanel;

    /** The height label. */
    @UiField
    protected Label m_heightDisplay;

    /** The height label. */
    @UiField
    protected Label m_heightLabel;

    /** The image. */
    @UiField
    protected Image m_image;

    /** The OK button. */
    @UiField
    protected CmsPushButton m_okButton;

    /** The height label. */
    @UiField
    protected Label m_scaleDisplay;

    /** The height label. */
    @UiField
    protected Label m_scaleLabel;

    /** The height label. */
    @UiField
    protected Label m_widthDisplay;
    /** The height label. */
    @UiField
    protected Label m_widthLabel;

    /** The cropping parameters. */
    private CmsCroppingParamBean m_croppingParam;

    /** The cropping parameters of the displayed image. */
    private CmsCroppingParamBean m_displayCropping;

    /** The ratio from original image height to display height. */
    private double m_heightRatio;

    /** The image path. */
    private String m_imagePath;

    /** The ratio from original image width to display width. */
    private double m_widthRatio;

    /**
     * Constructor.<p>
     * 
     * @param imagePath the image path
     */
    public CmsCroppingDialog(String imagePath) {

        initWidget(m_uiBinder.createAndBindUi(this));

        m_imagePath = imagePath;

        m_croppingPanel.addValueChangeHandler(this);
        m_croppingPanel.setFireAll(true);

        m_widthLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_WIDTH_0));
        m_widthDisplay.setText(EMPTY_FIELD);
        m_heightLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_HEIGHT_0));
        m_heightDisplay.setText(EMPTY_FIELD);
        m_scaleLabel.setText(Messages.get().key(Messages.GUI_IMAGE_SCALE_0));
        m_scaleDisplay.setText(EMPTY_FIELD);
        m_okButton.setText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0));
        m_okButton.setUseMinWidth(true);
        m_cancelButton.setText(org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0));
        m_cancelButton.setUseMinWidth(true);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<CmsCroppingParamBean> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public void onValueChange(ValueChangeEvent<CmsPositionBean> event) {

        CmsPositionBean pos = event.getValue();
        if (pos != null) {
            calculateCropping(pos);
            if (m_croppingParam.getTargetWidth() > 0) {
                if (m_croppingParam.getTargetHeight() > 0) {
                    m_heightDisplay.setText(String.valueOf(m_croppingParam.getTargetHeight()));
                    m_widthDisplay.setText(String.valueOf(m_croppingParam.getTargetWidth()));
                } else {
                    m_widthDisplay.setText(String.valueOf(m_croppingParam.getTargetWidth()));
                    m_heightDisplay.setText(String.valueOf((int)Math.floor((1.00 * m_croppingParam.getTargetWidth() * m_croppingParam.getCropHeight())
                        / m_croppingParam.getCropWidth())));
                }
            } else if (m_croppingParam.getTargetHeight() > 0) {
                m_heightDisplay.setText(String.valueOf(m_croppingParam.getTargetHeight()));
                m_widthDisplay.setText(String.valueOf((int)Math.floor((1.00 * m_croppingParam.getTargetHeight() * m_croppingParam.getCropWidth())
                    / m_croppingParam.getCropHeight())));
            } else {
                m_heightDisplay.setText(String.valueOf(m_croppingParam.getCropHeight()));
                m_widthDisplay.setText(String.valueOf(m_croppingParam.getCropWidth()));
            }

            String scale = "100%";
            if (m_croppingParam.getTargetHeight() > 0) {
                scale = String.valueOf((int)Math.floor((100.00 * m_croppingParam.getCropHeight())
                    / m_croppingParam.getTargetHeight()))
                    + "%";
            } else if (m_croppingParam.getTargetWidth() > 0) {
                scale = String.valueOf((int)Math.floor((100.00 * m_croppingParam.getCropWidth())
                    / m_croppingParam.getTargetWidth()))
                    + "%";
            }
            m_scaleDisplay.setText(scale);
            m_okButton.enable();
        } else {
            m_okButton.disable(Messages.get().key(Messages.GUI_IMAGE_NO_AREA_SELECTED_0));
            m_heightDisplay.setText(EMPTY_FIELD);
            m_widthDisplay.setText(EMPTY_FIELD);
            m_scaleDisplay.setText(EMPTY_FIELD);
        }

    }

    /**
     * Shows the dialog.<p>
     * 
     * @param targetParam the target cropping parameter, containing the target size restriction
     */
    public void show(CmsCroppingParamBean targetParam) {

        getElement().getStyle().setDisplay(Display.BLOCK);
        m_croppingParam = targetParam;
        m_displayCropping = new CmsCroppingParamBean();
        m_displayCropping.setTargetHeight(m_croppingParam.getOrgHeight());
        m_displayCropping.setTargetWidth(m_croppingParam.getOrgWidth());
        m_displayCropping = m_displayCropping.getRestrictedSizeParam(
            getElement().getOffsetHeight() - 29,
            getElement().getOffsetWidth() - 4);
        m_image.setUrl(m_imagePath + "?" + m_displayCropping.toString());
        m_croppingPanel.getElement().getStyle().setWidth(m_displayCropping.getTargetWidth(), Unit.PX);
        if ((targetParam.getTargetHeight() > 0) && (targetParam.getTargetWidth() > 0)) {
            m_croppingPanel.setRatio((1.00 * targetParam.getTargetHeight()) / targetParam.getTargetWidth());
        }

        m_heightRatio = (1.00 * m_croppingParam.getOrgHeight()) / m_displayCropping.getTargetHeight();
        m_widthRatio = (1.00 * m_croppingParam.getOrgWidth()) / m_displayCropping.getTargetWidth();
        if (m_croppingParam.isCropped()) {
            m_croppingPanel.setAreaPosition(true, calculateSelectPosition());
        } else {
            m_croppingPanel.clearSelection();
        }
    }

    /**
     * Handles the click event for cancel button. Hides the cropping dialog.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_cancelButton")
    protected void onCancel(ClickEvent event) {

        hide();
    }

    /**
     * Handles the click event for ok button. Sets the selected cropping parameters.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_okButton")
    protected void onOk(ClickEvent event) {

        if (!((m_croppingParam.getTargetWidth() > 0) && (m_croppingParam.getTargetHeight() > 0))) {
            if (m_croppingParam.getTargetWidth() > 0) {
                m_croppingParam.setTargetHeight((int)Math.floor((1.00 * m_croppingParam.getTargetWidth() * m_croppingParam.getCropHeight())
                    / m_croppingParam.getCropWidth()));
            } else if (m_croppingParam.getTargetHeight() > 0) {
                m_croppingParam.setTargetWidth((int)Math.floor((1.00 * m_croppingParam.getTargetHeight() * m_croppingParam.getCropWidth())
                    / m_croppingParam.getCropHeight()));
            } else {
                m_croppingParam.setTargetHeight(m_croppingParam.getCropHeight());
                m_croppingParam.setTargetWidth(m_croppingParam.getCropWidth());
            }
        }
        ValueChangeEvent.fire(this, m_croppingParam);
        hide();
    }

    /**
     * Calculates the resulting cropping parameter from the supplied selection position.<p>
     * 
     * @param position the selection position
     */
    private void calculateCropping(CmsPositionBean position) {

        m_croppingParam.setCropHeight((int)Math.round(m_heightRatio * position.getHeight()));
        m_croppingParam.setCropWidth((int)Math.round(m_widthRatio * position.getWidth()));
        m_croppingParam.setCropY((int)Math.round(m_heightRatio * position.getTop()));
        m_croppingParam.setCropX((int)Math.round(m_widthRatio * position.getLeft()));
    }

    /**
     * Calculates the select area position for the current cropping parameter.<p>
     * 
     * @return the select area position
     */
    private CmsPositionBean calculateSelectPosition() {

        CmsPositionBean result = new CmsPositionBean();
        result.setHeight((int)Math.round(m_croppingParam.getCropHeight() / m_heightRatio));
        result.setWidth((int)Math.round(m_croppingParam.getCropWidth() / m_widthRatio));
        result.setTop((int)Math.round(m_croppingParam.getCropY() / m_heightRatio));
        result.setLeft((int)Math.round(m_croppingParam.getCropX() / m_widthRatio));
        return result;
    }

    /**
     * Hides the cropping dialog.<p>
     */
    private void hide() {

        getElement().getStyle().setDisplay(Display.NONE);
    }

}
