/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/ui/Attic/CmsCroppingDialog.java,v $
 * Date   : $Date: 2010/07/26 06:40:50 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.galleries.client.preview.ui;

import org.opencms.ade.galleries.client.preview.CmsCroppingParamBean;
import org.opencms.ade.galleries.client.ui.Messages;
import org.opencms.gwt.client.ui.CmsAreaSelectPanel;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.util.CmsPositionBean;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsCroppingDialog extends Composite implements ValueChangeHandler<CmsPositionBean> {

    /** The ui-binder for this widget. */
    interface I_CmsCroppingDialogUiBinder extends UiBinder<Widget, CmsCroppingDialog> {
        // GWT interface, nothing to do
    }

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

    /* The cropping parameters. */
    private CmsCroppingParamBean m_croppingParam;

    /** The cropping parameters of the displayed image. */
    private CmsCroppingParamBean m_displayCropping;

    /** The image path. */
    private String m_imagePath;

    /** The original image height. */
    private int m_orgHeight;

    /** The original image width. */
    private int m_orgWidth;

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

        // TODO: add localization
        m_widthLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_WIDTH_0));
        m_widthDisplay.setText("---");
        m_heightLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_HEIGHT_0));
        m_heightDisplay.setText("---");
        m_scaleLabel.setText("Scale:");
        m_scaleDisplay.setText("---");
        m_okButton.setText("OK");
        m_okButton.setUseMinWidth(true);
        m_cancelButton.setText("Cancel");
        m_cancelButton.setUseMinWidth(true);
    }

    /**
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public void onValueChange(ValueChangeEvent<CmsPositionBean> event) {

        CmsPositionBean pos = event.getValue();
        if (pos != null) {
            CmsCroppingParamBean result = getResultCropping(pos);
            m_heightDisplay.setText(String.valueOf(result.getCropHeight()));
            m_widthDisplay.setText(String.valueOf(result.getCropWidth()));
            String scale = "100%";
            if (m_croppingParam.getTargetHeight() > 0) {
                scale = String.valueOf((int)Math.floor(100.00
                    * result.getCropHeight()
                    / m_croppingParam.getTargetHeight()))
                    + "%";
            } else if (m_croppingParam.getTargetWidth() > 0) {
                scale = String.valueOf((int)Math.floor(100.00
                    * result.getCropWidth()
                    / m_croppingParam.getTargetWidth()))
                    + "%";
            }
            m_scaleDisplay.setText(scale);
            m_okButton.enable();
        } else {
            m_okButton.disable("No image area selected");
            m_heightDisplay.setText("---");
            m_widthDisplay.setText("---");
            m_scaleDisplay.setText("---");
        }

    }

    /**
     * Shows the dialog.<p>
     * 
     * @param targetParam the target cropping parameter, containing the target size restriction
     * @param orgHeight the original image height
     * @param orgWidth the original image width
     */
    public void show(CmsCroppingParamBean targetParam, int orgHeight, int orgWidth) {

        getElement().getStyle().setDisplay(Display.BLOCK);
        m_croppingParam = targetParam;
        m_orgHeight = orgHeight;
        m_orgWidth = orgWidth;
        m_displayCropping = new CmsCroppingParamBean();
        m_displayCropping.setTargetHeight(m_orgHeight);
        m_displayCropping.setTargetWidth(m_orgWidth);
        m_displayCropping = m_displayCropping.getRestrictedSizeParam(
            getElement().getOffsetHeight() - 29,
            getElement().getOffsetWidth() - 4);
        m_image.setUrl(m_imagePath + "?" + m_displayCropping.toString());
        m_croppingPanel.getElement().getStyle().setWidth(m_displayCropping.getTargetWidth(), Unit.PX);
        if ((targetParam.getTargetHeight() > 0) && (targetParam.getTargetWidth() > 0)) {
            m_croppingPanel.setRatio(1.00 * targetParam.getTargetHeight() / targetParam.getTargetWidth());
        }
    }

    /**
     * Handles the click event for cancel button. Hides the cropping dialog.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_cancelButton")
    protected void onCancel(ClickEvent event) {

        getElement().getStyle().setDisplay(Display.NONE);
    }

    /**
     * Calculates the resulting cropping parameter from the supplied selection position.<p>
     * 
     * @param position the selection position
     * 
     * @return the resulting cropping parameter
     */
    private CmsCroppingParamBean getResultCropping(CmsPositionBean position) {

        double heightRatio = 1.00 * m_orgHeight / m_displayCropping.getTargetHeight();
        double widthRatio = 1.00 * m_orgWidth / m_displayCropping.getTargetWidth();
        CmsCroppingParamBean result = new CmsCroppingParamBean();
        result.setCropHeight((int)Math.round(heightRatio * position.getHeight()));
        result.setCropWidth((int)Math.round(widthRatio * position.getWidth()));
        result.setCropY((int)Math.round(heightRatio * position.getTop()));
        result.setCropX((int)Math.round(widthRatio * position.getLeft()));

        return result;
    }

}
