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
import org.opencms.ade.galleries.client.preview.CmsImageFormatHandler;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsToggleButton;
import org.opencms.gwt.client.ui.css.I_CmsImageBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Image format form.<p>
 * 
 * @since 8.0.0
 */
public class CmsImageFormatsForm extends Composite implements ValueChangeHandler<String>, KeyPressHandler {

    /** GWT ui-binder. */
    protected interface I_CmsImageFormatsFormUiBinder extends UiBinder<Widget, CmsImageFormatsForm> {
        // nothing to do
    }

    /** The label width. */
    private static final int LABEL_WIDTH = 80;

    /** Text metrics key. */
    private static final String TM_PREVIEW_TAB_IMAGEFORMATS = "ImageFormatsTab";

    /** The ui-binder instance for this class. */
    private static I_CmsImageFormatsFormUiBinder uiBinder = GWT.create(I_CmsImageFormatsFormUiBinder.class);
    /** The cropping button. */
    @UiField
    protected CmsPushButton m_cropButton;

    /** The height text box. */
    @UiField
    protected CmsTextBox m_heightBox;

    /** The height label. */
    @UiField
    protected CmsLabel m_heightLabel;

    /** The panel holding the content. */
    @UiField
    protected HTMLPanel m_panel;

    /** The ratio lock button. */
    @UiField
    protected CmsToggleButton m_ratioLock;

    /** The remove cropping button. */
    @UiField
    protected CmsPushButton m_removeCropButton;

    /** The reset size button. */
    @UiField
    protected CmsPushButton m_resetSize;

    /** The select box. */
    @UiField
    protected CmsSelectBox m_selectBox;

    /** The select box label. */
    @UiField
    protected CmsLabel m_selectBoxLabel;

    /** The width text box. */
    @UiField
    protected CmsTextBox m_widthBox;

    /** The width label. */
    @UiField
    protected CmsLabel m_widthLabel;

    /** The image format handler. */
    private CmsImageFormatHandler m_formatHandler;

    /** Flag to indicate if the formats form is enabled. */
    private boolean m_formEnabled;

    /**
     * Constructor.<p>
     * 
     * @param formatHandler the image format handler
     */
    public CmsImageFormatsForm(CmsImageFormatHandler formatHandler) {

        initWidget(uiBinder.createAndBindUi(this));
        // form is enabled by default
        m_formEnabled = true;
        m_formatHandler = formatHandler;
        m_selectBoxLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_FORMAT_0));
        m_selectBoxLabel.truncate(TM_PREVIEW_TAB_IMAGEFORMATS, LABEL_WIDTH);

        // set localized values of the labels
        m_cropButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_CROP_0));
        m_cropButton.setImageClass(I_CmsImageBundle.INSTANCE.style().croppingIcon());

        m_removeCropButton.setText(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_REMOVECROP_0));
        m_removeCropButton.setImageClass(I_CmsImageBundle.INSTANCE.style().removeCroppingIcon());

        m_widthLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_WIDTH_0));
        m_widthLabel.truncate(TM_PREVIEW_TAB_IMAGEFORMATS, LABEL_WIDTH);

        m_heightLabel.setText(Messages.get().key(Messages.GUI_PREVIEW_LABEL_HEIGHT_0));
        m_heightLabel.truncate(TM_PREVIEW_TAB_IMAGEFORMATS, LABEL_WIDTH);

        m_ratioLock.setImageClass(I_CmsImageBundle.INSTANCE.style().lockedIcon());
        m_ratioLock.setDownImageClass(I_CmsImageBundle.INSTANCE.style().unlockedIcon());

        m_resetSize.setImageClass(I_CmsImageBundle.INSTANCE.style().resetIcon());
        m_selectBox.addValueChangeHandler(this);
        m_heightBox.addValueChangeHandler(this);
        m_heightBox.addKeyPressHandler(this);
        m_widthBox.addValueChangeHandler(this);
        m_widthBox.addKeyPressHandler(this);

        m_removeCropButton.setVisible(false);
    }

    /**
     * Parses <code>String</code> to <code>int</code>. Return -1 for invalid input.<p>
     * 
     * @param value the value to parse
     * 
     * @return the int-value
     */
    private static native int parseInt(String value) /*-{
        var ret = parseInt(value);
        if (isNaN(ret)) {
            return -1;
        }
        return ret;
    }-*/;

    /**
     * Adds a format select option.<p>
     * 
     * @param value the option value
     * @param label the option label
     */
    public void addFormatSelectOption(String value, String label) {

        m_selectBox.addOption(value, label);
    }

    /**
     * Returns the selected format value.<p>
     * 
     * @return the selected format value
     */
    public String getFormatSelectValue() {

        return m_selectBox.getFormValueAsString();
    }

    /**
     * Returns the height input or -1 if input is empty or not valid.<p>
     * 
     * @return the height input
     */
    public int getHeightInput() {

        return parseInt(m_heightBox.getFormValueAsString());
    }

    /**
     * Returns the width input or -1 if input is empty or not valid.<p>
     * 
     * @return the width input
     */
    public int getWidthInput() {

        return parseInt(m_widthBox.getFormValueAsString());
    }

    /**
     * Opens the cropping dialog on crop button click.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_cropButton")
    protected void openCropping(ClickEvent event) {

        m_formatHandler.openCropping();
    }

    /**
     * Removes the cropping on button click.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_removeCropButton")
    protected void removeCropping(ClickEvent event) {

        m_formatHandler.onRemoveCropping();
    }

    /**
     * Toggle the ratio lock on button click.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_ratioLock")
    protected void toggleRatioLock(ClickEvent event) {

        m_formatHandler.onLockRatio(!m_ratioLock.isDown());
    }

    /**
     * Resets the size on button click.<p>
     * 
     * @param event the click event
     */
    @UiHandler("m_resetSize")
    protected void resetSize(ClickEvent event) {

        m_formatHandler.onResetSize();
    }

    /**
     * @see com.google.gwt.event.dom.client.KeyPressHandler#onKeyPress(com.google.gwt.event.dom.client.KeyPressEvent)
     */
    public void onKeyPress(KeyPressEvent event) {

        //preventing any input but numbers
        char key = event.getCharCode();
        int code = event.getNativeEvent().getKeyCode();
        if (((key >= '0') && (key <= '9')) || (code == KeyCodes.KEY_BACKSPACE) || (code == KeyCodes.KEY_DELETE)) {
            // the value of the input box will probably have changed, so fire an event after the input has been processed
            final CmsTextBox source = (CmsTextBox)event.getSource();
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                public void execute() {

                    ValueChangeEvent.fire(source, source.getText());
                }
            });
            return;
        }
        if ((code == KeyCodes.KEY_TAB)
            || (code == KeyCodes.KEY_LEFT)
            || (code == KeyCodes.KEY_RIGHT)
            || (code == KeyCodes.KEY_ENTER)) {
            return;
        }
        // prevent all others
        event.stopPropagation();
        event.preventDefault();
    }

    /**
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public void onValueChange(ValueChangeEvent<String> event) {

        Object source = event.getSource();
        if (source == m_selectBox) {
            m_formatHandler.onFormatChange(event.getValue());
            return;
        }
        if (source == m_heightBox) {
            m_formatHandler.onHeightChange(event.getValue());
            return;
        }
        if (source == m_widthBox) {
            m_formatHandler.onWidthChange(event.getValue());
        }
    }

    /**
     * Enables/disables buttons and input fields necessary if the image if cropped.<p>
     * 
     * @param cropped <code>true</code> if the image is cropped
     */
    public void setCropped(boolean cropped) {

        if (cropped) {
            m_heightBox.setEnabled(false);
            m_widthBox.setEnabled(false);
            m_selectBox.setEnabled(false);
            m_resetSize.disable(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_DIS_CROPPED_0));
            m_ratioLock.disable(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_DIS_CROPPED_0));
            m_cropButton.disable(Messages.get().key(Messages.GUI_PREVIEW_BUTTON_DIS_CROPPED_0));
            if (m_formEnabled) {
                m_removeCropButton.enable();
            }
            m_removeCropButton.setVisible(true);
            return;
        }
        if (m_formEnabled) {
            m_cropButton.enable();
            m_heightBox.setEnabled(true);
            m_widthBox.setEnabled(true);
            m_selectBox.setEnabled(true);
            m_resetSize.enable();
            m_ratioLock.enable();
        }
        m_removeCropButton.setVisible(false);
    }

    /**
     * Sets the format select value.<p>
     * 
     * @param value the value
     */
    public void setFormatSelectValue(String value) {

        m_selectBox.setFormValueAsString(value);
    }

    /**
     * Sets the format form enabled.<p>
     * 
     * @param enabled if <code>true</code> the form will be enabled
     */
    public void setFormEnabled(boolean enabled) {

        m_formEnabled = enabled;
        m_selectBox.setEnabled(enabled);
        m_heightBox.setEnabled(enabled);
        m_widthBox.setEnabled(enabled);
        if (enabled) {
            m_cropButton.enable();
            m_removeCropButton.enable();
        } else {
            m_cropButton.disable(Messages.get().key(Messages.GUI_IMAGE_NO_FORMATS_AVAILABLE_0));
            m_removeCropButton.disable(Messages.get().key(Messages.GUI_IMAGE_NO_FORMATS_AVAILABLE_0));
        }
    }

    /**
     * Sets the height input field.<p>
     * 
     * @param height the value
     */
    public void setHeightInput(int height) {

        m_heightBox.setFormValueAsString(String.valueOf(height));
    }

    /**
     * Enables the height input field.<p>
     * 
     * @param enabled <code>true</code> to enable the input field
     */
    public void setHeightInputEnabled(boolean enabled) {

        m_heightBox.setEnabled(enabled);
    }

    /**
     * Sets the state of the ratio lock button.<p>
     * 
     * @param down if <code>true</code> button will be set down / lock open
     * @param enabled if <code>true</code> button will be enabled
     * @param disableMessage message to show for disabled button
     */
    public void setRatioButton(boolean down, boolean enabled, String disableMessage) {

        m_ratioLock.setDown(down);
        if (enabled) {
            m_ratioLock.enable();
        } else {
            m_ratioLock.disable(disableMessage);
        }
    }

    /**
     * Sets the width input field.<p>
     * 
     * @param width the value
     */
    public void setWidthInput(int width) {

        m_widthBox.setFormValueAsString(String.valueOf(width));
    }

    /**
     * Enables the width input field.<p>
     * 
     * @param enabled <code>true</code> to enable the input field
     */
    public void setWidthInputEnabled(boolean enabled) {

        m_widthBox.setEnabled(enabled);
    }
}
