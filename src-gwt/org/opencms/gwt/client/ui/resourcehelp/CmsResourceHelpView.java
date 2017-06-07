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

package org.opencms.gwt.client.ui.resourcehelp;

import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.shared.CmsResourceTypeHelpBean;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The content widget for the help dialog.<p>
 */
public class CmsResourceHelpView extends Composite {

    /** The UiBinder class for this widget. */
    interface I_CmsResourceHelpViewUiBinder extends UiBinder<Widget, CmsResourceHelpView> {
        //empty
    }

    /** The UiBinder instance for this widget. */
    private static I_CmsResourceHelpViewUiBinder uiBinder = GWT.create(I_CmsResourceHelpViewUiBinder.class);

    /** The prev button. */
    @UiField
    protected CmsPushButton m_prevButton;

    /** The next button. */
    @UiField
    protected CmsPushButton m_nextButton;

    /** The close button. */
    @UiField
    protected CmsPushButton m_closeButton;

    /** The container for the file info box. */
    @UiField
    protected FlowPanel m_infoBoxContainer;

    /** The panel with help content. */
    protected HTMLPanel m_panel;

    /** The checkbox for including sibling resources. */
    @UiField
    protected CmsCheckBox m_checkboxNotShowOnStart;

    /** The popup in which this widget is contained. */
    protected CmsPopup m_popup;

    /** The bean containing help information about the resource type. */
    protected List<CmsResourceTypeHelpBean> m_helpBeans;

    /** The index of current help bean */
    protected int m_currentHelpIndex;

    /**
     * Creates a new widget instance.<p>
     * @param helpBeans the list of helpBeans
     *
     */
    public CmsResourceHelpView(final List<CmsResourceTypeHelpBean> helpBeans) {

        initWidget(uiBinder.createAndBindUi(this));

        m_panel = new HTMLPanel(helpBeans.get(m_currentHelpIndex).getContent());
        m_panel.getElement().getStyle().setOverflow(Overflow.AUTO);
        m_panel.getElement().getStyle().setPosition(Position.RELATIVE);
        m_panel.getElement().getStyle().setPaddingLeft(12, Unit.PX);
        m_panel.getElement().getStyle().setPaddingRight(12, Unit.PX);
        m_panel.getElement().getStyle().setProperty("maxHeight", 600, Unit.PX);
        m_panel.getElement().getStyle().setProperty("maxWidth", 800, Unit.PX);
        m_panel.getElement().getStyle().setProperty("minHeight", 320, Unit.PX);
        m_panel.getElement().getStyle().setProperty("minWidth", 480, Unit.PX);
        m_infoBoxContainer.add(m_panel);
        m_helpBeans = helpBeans;
    }

    /**
     * change the content of Help Dialog
     */
    protected void changeContent() {

        String caption = m_helpBeans.get(m_currentHelpIndex).getTitle();
        if ((m_helpBeans != null) && (m_helpBeans.size() > 1)) {
            caption += " (" + (m_currentHelpIndex + 1) + "/" + m_helpBeans.size() + ")";
        }
        m_popup.setCaption(caption);
        m_panel.getElement().setInnerHTML(m_helpBeans.get(m_currentHelpIndex).getContent());
        m_popup.center();
    }

    /**
     * @return m_checkboxNotShowOnStart
     */
    public CmsCheckBox getCheckboxNotShowOnStart() {

        return m_checkboxNotShowOnStart;
    }

    /**
     * Return the current help index
     * @return m_currentHelpIndex
     */
    public int getCurrentHelpIndex() {

        return m_currentHelpIndex;
    }

    /**
     * Gets the list of buttons for the dialog.<p>
     *
     * @return the list of buttons for the dialog
     */
    List<CmsPushButton> getDialogButtons() {

        List<CmsPushButton> result = new ArrayList<CmsPushButton>();
        m_nextButton.setVisible(true);
        m_prevButton.setVisible(true);
        if ((m_helpBeans == null) || (m_helpBeans.size() <= 1)) {
            m_nextButton.setVisible(false);
            m_prevButton.setVisible(false);
        }
        result.add(m_closeButton);
        result.add(m_nextButton);
        result.add(m_prevButton);
        return result;
    }

    /**
     * @return CmsPopup
     */
    public CmsPopup getPopup() {

        return m_popup;
    }

    /**
     * Handler for the cancel button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_closeButton")
    protected void onClickClose(ClickEvent e) {

        m_popup.hide();
    }

    /**
     * Click handler for the OK button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_nextButton")
    protected void onClickNext(ClickEvent e) {

        m_currentHelpIndex++;
        if (m_currentHelpIndex >= m_helpBeans.size()) {
            m_currentHelpIndex = 0;
        }
        changeContent();
    }

    /**
     * Handler for the cancel button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_prevButton")
    protected void onClickPrev(ClickEvent e) {

        m_currentHelpIndex--;
        if (m_currentHelpIndex < 0) {
            m_currentHelpIndex = m_helpBeans.size() - 1;
        }

        changeContent();
    }

    /**
     * Handles the click event for sibling resources check box.<p>
     *
     * @param event the click event
     *
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    @UiHandler("m_checkboxNotShowOnStart")
    protected void onDoNotShowClick(ClickEvent event) {

        CmsCoreProvider.get().setStartHelpActive(!m_checkboxNotShowOnStart.isChecked());
    }

    /**
     * Sets the popup in which this widget is displayed.<p>
     *
     * @param popup the popup in which this widget is displayed
     */
    public void setPopup(CmsPopup popup) {

        m_popup = popup;
    }
}