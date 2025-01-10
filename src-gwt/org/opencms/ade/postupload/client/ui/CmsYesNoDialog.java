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

package org.opencms.ade.postupload.client.ui;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsMessageWidget;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;

import java.util.function.BiConsumer;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Provides an alert dialog with a button.<p>
 *
 * @since 8.0.0
 */
public class CmsYesNoDialog extends CmsPopup {

    /** The panel for the bottom widgets. */
    private FlowPanel m_bottomWidgets;

    /** The 'no' button. */
    private CmsPushButton m_noButton;

    /** The 'yes' button. */
    private CmsPushButton m_yesButton;

    /** The content text. */
    private FlowPanel m_content;

    /** The action handler. */
    private BiConsumer<CmsPopup, Boolean> m_action;

    /** The panel for the top widgets. */
    private FlowPanel m_topWidgets;

    /** The warning message. */
    private CmsMessageWidget m_warningMessage;

    /**
     * Constructor.<p>
     *
     * @param title the title and heading of the dialog
     * @param content the content text
     */
    public CmsYesNoDialog(String title, String content, BiConsumer<CmsPopup, Boolean> action) {

        super(title);
        setAutoHideEnabled(false);
        setModal(true);
        setGlassEnabled(true);

        // create the dialogs content panel
        m_content = new FlowPanel();
        m_content.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().alertMainContent());

        // create the top widget panel
        m_topWidgets = new FlowPanel();
        m_topWidgets.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().alertTopContent());
        m_topWidgets.getElement().getStyle().setDisplay(Display.NONE);
        m_content.add(m_topWidgets);

        // create the warning message
        m_warningMessage = new CmsMessageWidget();
        m_warningMessage.addStyleName(I_CmsLayoutBundle.INSTANCE.generalCss().border());
        m_warningMessage.setMessageHtml(content);
        m_action = action;

        m_content.add(m_warningMessage);

        // create the bottom widget panel
        m_bottomWidgets = new FlowPanel();
        m_bottomWidgets.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().alertBottomContent());
        m_bottomWidgets.getElement().getStyle().setDisplay(Display.NONE);
        m_content.add(m_bottomWidgets);

        // set the content to the popup
        setMainContent(m_content);

        m_noButton = new CmsPushButton();
        m_noButton.setText(Messages.get().key(Messages.GUI_NO_0));
        m_noButton.setUseMinWidth(true);
        m_noButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
        m_noButton.addClickHandler(event -> m_action.accept(this, false));

        m_yesButton = new CmsPushButton();
        m_yesButton.setText(Messages.get().key(Messages.GUI_YES_0));
        m_yesButton.setUseMinWidth(true);
        m_yesButton.setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
        m_yesButton.addClickHandler(event -> m_action.accept(this, true));
        addButton(m_noButton);
        addButton(m_yesButton);
    }

    /**
     * Adds a widget to this dialogs bottom content.<p>
     *
     * @param w the widget to add
     */
    public void addBottomWidget(Widget w) {

        m_content.removeStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().alertMainContent());
        m_bottomWidgets.getElement().getStyle().clearDisplay();
        m_bottomWidgets.add(w);

    }

    /**
     * Adds a widget to this dialogs top content.<p>
     *
     * @param w the widget to add
     */
    public void addTopWidget(Widget w) {

        m_content.removeStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().alertMainContent());
        m_topWidgets.getElement().getStyle().clearDisplay();
        m_topWidgets.add(w);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#center()
     */
    @Override
    public void center() {

        super.center();
    }

    public CmsPushButton getNoButton() {

        return m_noButton;
    }

    public CmsPushButton getYesButton() {

        return m_yesButton;
    }

    /**
     * Sets the warning text (HTML possible).<p>
     *
     * @param warningText the warning text to set
     */
    public void setWarningMessage(String warningText) {

        m_warningMessage.setMessageHtml(warningText);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        super.show();
    }

    /**
     * Returns the top widgets panel.<p>
     *
     * @return the top widgets panel
     */
    protected FlowPanel getTopWidgets() {

        return m_topWidgets;
    }

}
