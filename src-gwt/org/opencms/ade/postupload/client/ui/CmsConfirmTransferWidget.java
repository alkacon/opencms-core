/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.postupload.client.ui;

import org.opencms.ade.postupload.client.Messages;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;

/**
 * Widget for asking the user for confirmation whether a property value should be transferred to all uploaded files.
 *
 * <p>Has an additional checkbox for enabling/disabling overwrite mode, which controls whether properties should also be copied to
 * files on which the corresponding property is already set.
 */
public class CmsConfirmTransferWidget extends Composite {

    /**
     * Interface with message string constants.<p>
     */
    public static class MessageStrings {

        /**
         * Message string provider.<p>
         *
         * @return a message string
         */
        public static String cancel() {

            return org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_CANCEL_0);
        }

        /**
         * Message string provider.<p>
         *
         * @return a message string
         */
        public static String caption() {

            return Messages.get().key(Messages.GUI_APPLY_PROPERTY_TO_ALL_FILES_CAPTION_0);
        }

        /**
         * Message string provider.<p>
         *
         * @return a message string
         */
        public static String checkboxText() {

            return Messages.get().key(Messages.GUI_OVERWRITE_EXISTING_VALUES_0);
        }

        /**
         * Message string provider.<p>
         *
         * @return a message string
         */
        public static String confirmText() {

            return Messages.get().key(Messages.GUI_APPLY_PROPERTY_TO_ALL_FILES_0);
        }

        /**
         * Message string provider.<p>
         *
         * @return a message string
         */
        public static String emptyStringsNotAllowed() {

            return Messages.get().key(Messages.GUI_EMPTY_VALUES_CAN_NOT_BE_TRANSFERRED_0);
        }

        /**
         * Message string provider.<p>
         *
         * @return a message string
         */
        public static String ok() {

            return org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_OK_0);
        }
    }

    /**
     * UiBinder interface for this dialog.<p>
     */
    interface I_UiBinder extends UiBinder<Panel, CmsConfirmTransferWidget> {
        // empty uibinder interface
    }

    /** UiBinder instance for this dialog. */
    private static I_UiBinder uibinder = GWT.create(I_UiBinder.class);

    /** The Cancel button. */
    @UiField
    protected CmsPushButton m_cancelButton;

    /** Checkbox for selecting whether existing property values should be overwritten. */
    @UiField
    protected CmsCheckBox m_checkBox;

    /** The label with the dialog text. */
    @UiField
    protected HTML m_mainLabel;

    /** The OK button. */
    @UiField
    protected CmsPushButton m_okButton;

    /** Callback to call with the value of the 'overwrite' option. */
    private Consumer<Boolean> m_callback;

    /** The dialog instance which this widget appears in. */
    private CmsPopup m_popup;

    /**
     * Creates a new instance.<p>
     *
     * @param callback the callback to call with the value of the 'overwrite' option (only called if the user clicks OK)
     */
    public CmsConfirmTransferWidget(Consumer<Boolean> callback) {

        initWidget(uibinder.createAndBindUi(this));
        m_okButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        m_mainLabel.setHTML(CmsDomUtil.escapeXml(MessageStrings.confirmText()).replaceAll("\n", "<br>"));
        m_checkBox.setVisible(true);
        m_checkBox.setChecked(false);
        m_callback = callback;
    }

    /**
     * Opens the dialog for confirming a property transfer.
     *
     * @param callback the callback to call with the value of the 'overwrite' option
     */
    public static void showDialog(Consumer<Boolean> callback) {

        CmsPopup popup = new CmsPopup();
        popup.setModal(true);
        popup.setGlassEnabled(true);
        popup.setCaption(MessageStrings.caption());
        CmsConfirmTransferWidget widget = new CmsConfirmTransferWidget(callback);
        widget.setPopup(popup);
        popup.setMainContent(widget);
        widget.getButtons().forEach(button -> popup.addButton(button));
        popup.center();

    }

    /**
     * Gets the buttons for the dialog.<p>
     *
     * @return the buttons for the dialog
     */
    public List<CmsPushButton> getButtons() {

        return Arrays.asList(m_cancelButton, m_okButton);
    }

    /**
     * The click handler for the cancel button.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_cancelButton")
    public void onClickCancel(ClickEvent event) {

        m_popup.hide();
    }

    /**
     * The click handler for the OK button.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_okButton")
    public void onClickOk(ClickEvent event) {

        m_popup.hide();
        boolean overwrite = m_checkBox.isChecked();
        m_callback.accept(Boolean.valueOf(overwrite));

    }

    /**
     * Sets the popup which this widget is used in.<p>
     *
     * @param popup the popup
     */
    public void setPopup(CmsPopup popup) {

        m_popup = popup;
    }

}
