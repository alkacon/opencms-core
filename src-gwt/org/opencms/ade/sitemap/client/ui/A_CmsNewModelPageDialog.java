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

package org.opencms.ade.sitemap.client.ui;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.form.CmsFieldsetFormFieldPanel;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The create new gallery folder dialog.<p>
 */
public abstract class A_CmsNewModelPageDialog extends CmsPopup {

    /** The text metrics key. */
    private static final String METRICS_KEY = "CREATE_NEW_GALLERY_DIALOG";

    /** The text box for the description. */
    protected CmsTextBox m_descriptionInput;

    /** The title input. */
    protected CmsTextBox m_titleInput;

    /** The dialog content panel. */
    private CmsFieldsetFormFieldPanel m_dialogContent;

    /** The OK button. */
    private CmsPushButton m_okButton;

    /**
     * Constructor.<p>
     *
     * @param title the title of the dialog
     * @param infoBean the resource info bean to display
     */
    public A_CmsNewModelPageDialog(String title, CmsListInfoBean infoBean) {

        super(title);
        initialize(infoBean);
    }

    /**
     * Initializes the dialog.<p>
     *
     * @param listInfo the resource info to display
     */
    public void initialize(CmsListInfoBean listInfo) {

        m_dialogContent = new CmsFieldsetFormFieldPanel(listInfo, null);
        m_dialogContent.addStyleName(I_CmsInputLayoutBundle.INSTANCE.inputCss().highTextBoxes());
        m_dialogContent.getFieldSet().setOpenerVisible(false);
        m_dialogContent.getFieldSet().getElement().getStyle().setMarginTop(4, Style.Unit.PX);
        setMainContent(m_dialogContent);
        m_titleInput = new CmsTextBox();
        m_titleInput.setTriggerChangeOnKeyPress(true);
        m_titleInput.addValueChangeHandler(new ValueChangeHandler<String>() {

            public void onValueChange(ValueChangeEvent<String> event) {

                setOkEnabled(CmsStringUtil.isNotEmptyOrWhitespaceOnly(event.getValue()));
            }
        });
        addInputRow(
            org.opencms.ade.sitemap.client.Messages.get().key(
                org.opencms.ade.sitemap.client.Messages.GUI_MODEL_PAGE_TITLE_LABEL_0),

            m_titleInput);

        m_descriptionInput = new CmsTextBox();

        addDialogClose(null);
        addInputRow(
            org.opencms.ade.sitemap.client.Messages.get().key(
                org.opencms.ade.sitemap.client.Messages.GUI_MODEL_PAGE_DESCRIPTION_LABEL_0),

            m_descriptionInput);

        CmsPushButton closeButton = new CmsPushButton();
        closeButton.setText(Messages.get().key(Messages.GUI_CANCEL_0));
        closeButton.setUseMinWidth(true);
        closeButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
        closeButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                hide();
            }
        });
        addButton(closeButton);

        m_okButton = new CmsPushButton();
        m_okButton.setText(Messages.get().key(Messages.GUI_OK_0));
        m_okButton.setUseMinWidth(true);
        m_okButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.RED);
        m_okButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                onOk();
            }
        });
        addButton(m_okButton);
        setOkEnabled(false);
        m_dialogContent.truncate(METRICS_KEY, CmsPopup.DEFAULT_WIDTH - 20);
    }

    /**
     * Creates the new gallery folder.<p>
     */
    protected abstract void onOk();

    /**
     * Enables or disables the OK button.<p>
     *
     * @param enabled <code>true</code> to enable the button
     */
    protected void setOkEnabled(boolean enabled) {

        if (enabled) {
            m_okButton.enable();
        } else {
            m_okButton.disable("Invalid title");
        }
    }

    /**
     * Adds a row to the form.<p>
     *
     * @param label the label
     * @param inputWidget the input widget
     */
    private void addInputRow(String label, Widget inputWidget) {

        FlowPanel row = new FlowPanel();
        row.setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().simpleFormRow());
        CmsLabel labelWidget = new CmsLabel(label);
        labelWidget.setStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().simpleFormLabel());
        row.add(labelWidget);
        inputWidget.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().simpleFormInputBox());
        row.add(inputWidget);
        m_dialogContent.getFieldSet().add(row);
    }

}
