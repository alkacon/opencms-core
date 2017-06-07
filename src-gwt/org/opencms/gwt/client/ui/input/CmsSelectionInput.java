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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.I_CmsButton.Size;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Text input field with value select opener button and value fader for overflowing values.<p>
 */
public class CmsSelectionInput extends Composite {

    /**
     * The UI Binder interface for this widget.<p>
     */
    interface I_CmsSelectionInputUiBinder extends UiBinder<Widget, CmsSelectionInput> {
        // binder interface
    }

    /** The ui binder for this widget. */
    private static I_CmsSelectionInputUiBinder uiBinder = GWT.create(I_CmsSelectionInputUiBinder.class);

    /** The fader element. */
    @UiField
    Label m_fader;

    /** The dialog opener. */
    @UiField
    CmsPushButton m_opener;

    /** The text input field. */
    @UiField
    CmsSimpleTextBox m_textbox;

    /** The command to open the value select dialog. */
    private Command m_openCommand;

    /**
     * Constructor.<p>
     *
     * @param openerIcon the image icon class
     */
    public CmsSelectionInput(String openerIcon) {

        initWidget(uiBinder.createAndBindUi(this));
        if (openerIcon == null) {
            m_opener.setImageClass(I_CmsButton.GALLERY);
        } else {
            m_opener.setImageClass(openerIcon);
        }
        m_opener.setButtonStyle(ButtonStyle.FONT_ICON, null);
        m_opener.setSize(Size.small);
    }

    /**
     * Returns the input box.<p>
     *
     * @return the input box
     */
    public CmsSimpleTextBox getTextBox() {

        return m_textbox;
    }

    /**
     * Hides the fader.<p>
     */
    public void hideFader() {

        m_fader.getElement().getStyle().setDisplay(Display.NONE);
    }

    /**
     * Sets the value select dialog open command.<p>
     *
     * @param openCommand the command
     */
    public void setOpenCommand(Command openCommand) {

        m_openCommand = openCommand;
    }

    /**
     * Shows the fader.<p>
     */
    public void showFader() {

        m_fader.getElement().getStyle().clearDisplay();
    }

    /**
     * Handles the opener clicks.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_fader")
    void onFaderClick(ClickEvent event) {

        m_textbox.setFocus(true);
    }

    /**
     * Handles the opener clicks.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_opener")
    void onOpen(ClickEvent event) {

        if (m_openCommand != null) {
            m_openCommand.execute();
        }
    }

}
