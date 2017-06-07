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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.CmsResourceStatusBean;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/**
 * Confirm dialog with a resource info box.<p>
 */
public class CmsResourceInfoConfirmDialog {

    /** The cancel button. */
    private CmsPushButton m_cancelButton;

    /** Center panel. */
    private FlowPanel m_centerPanel = new FlowPanel();

    /** The dialog content. */
    private FlowPanel m_content = new FlowPanel();

    /** The dialog. */
    private CmsPopup m_dialog;

    /** The OK button. */
    private CmsPushButton m_okButton;

    /**
     * Creates a new dialog instance for the given resource info.<p>
     *
     * @param resourceStatus the resource information
     */
    public CmsResourceInfoConfirmDialog(CmsResourceStatusBean resourceStatus) {

        CmsListInfoBean info = resourceStatus.getListInfo();
        CmsListItemWidget itemWidget = new CmsListItemWidget(info);
        m_content.add(itemWidget);
        m_centerPanel.add(new Label(getText()));
        m_centerPanel.getElement().getStyle().setPadding(7, Unit.PX);

        m_content.add(m_centerPanel);
        m_okButton = createButton(getOkText());
        m_okButton.addClickHandler(new ClickHandler() {

            @SuppressWarnings("synthetic-access")
            public void onClick(ClickEvent event) {

                m_dialog.hide();
                onConfirm();
            }
        });
        m_cancelButton = createButton(getCancelText());
        m_cancelButton.addClickHandler(new ClickHandler() {

            @SuppressWarnings("synthetic-access")
            public void onClick(ClickEvent event) {

                m_dialog.hide();
            }
        });

        m_dialog = new CmsPopup();
        m_dialog.setModal(true);
        m_dialog.setGlassEnabled(true);
        m_dialog.setCaption(getCaption());

        m_dialog.addButton(m_cancelButton);
        m_dialog.addButton(m_okButton);
        m_okButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.GREEN);

        m_dialog.setMainContent(m_content);

    }

    /**
     * Displays the dialog.<p>
     */
    public void display() {

        m_dialog.center();
    }

    /**
     * Gets the cancel button text.<p>
     *
     * @return the cancel button text
     */
    public String getCancelText() {

        return "??? Cancel";
    }

    /**
     * Gets the dialog title.<p>
     *
     * @return the dialog title
     */
    public String getCaption() {

        return "??? Confirm";
    }

    /**
     * Gets the OK button text.<p>
     *
     * @return the OK button text
     */
    public String getOkText() {

        return "??? OK";
    }

    /**
     * Gets the dialog text.<p>
     *
     * @return the dialog text
     */
    public String getText() {

        return "??? Confirm";
    }

    /**
     * Method to execute when the user confirms the action.<p>
     */
    public void onConfirm() {

        // do nothing

    }

    /**
     * Creates a button for the dialog.<p>
     *
     * @param text the button text
     *
     * @return the created button
     */
    private CmsPushButton createButton(String text) {

        CmsPushButton result = new CmsPushButton();
        result.setText(text);
        result.setUseMinWidth(true);
        return result;
    }
}
