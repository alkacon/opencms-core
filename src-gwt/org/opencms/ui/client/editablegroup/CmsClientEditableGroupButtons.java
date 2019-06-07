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

package org.opencms.ui.client.editablegroup;

import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Client side button bar widget for multivalue widget groups.<p>
 */
public class CmsClientEditableGroupButtons extends Composite {

    /** The UI binder interface. */
    interface I_UiBinder extends UiBinder<FlowPanel, CmsClientEditableGroupButtons> {
        // nothing to do
    }

    /** Indicates whether we have already flushed the UiBinder style. */
    private static boolean flushedStyle;

    /** The UI binder instance. */
    private static I_UiBinder uiBinder = GWT.create(I_UiBinder.class);

    /** The 'add' button. */
    @UiField
    protected CmsPushButton m_addButton;

    /** The 'delete' button. */
    @UiField
    protected CmsPushButton m_deleteButton;

    /** The 'down' button. */
    @UiField
    protected CmsPushButton m_downButton;

    /** OpenCms 'bullseye' dummy button, does nothing. */
    @UiField
    protected CmsPushButton m_dummyButton;

    /** The 'edit' button. */
    @UiField
    protected CmsPushButton m_editButton;

    /** The 'up' button. */
    @UiField
    protected CmsPushButton m_upButton;

    /** The connector instance. */
    private CmsEditableGroupButtonsConnector m_connector;

    /**
     * Creates a new instance.<p>
     *
     * @param connector the connector for which the widget should be created
     */
    public CmsClientEditableGroupButtons(CmsEditableGroupButtonsConnector connector) {

        FlowPanel panel = uiBinder.createAndBindUi(this);
        if (!flushedStyle) {
            StyleInjector.flush(); // make sure UiBinder CSS is loaded synchronously, otherwise Vaadin width calculation will go wrong
            flushedStyle = true;
        }
        initWidget(panel);
        m_connector = connector;
        for (CmsPushButton button : new CmsPushButton[] {
            m_dummyButton,
            m_upButton,
            m_downButton,
            m_deleteButton,
            m_addButton,
            m_editButton}) {
            button.setButtonStyle(ButtonStyle.FONT_ICON, null);
        }

        m_downButton.setImageClass(I_CmsButton.EDIT_DOWN_SMALL);
        m_upButton.setImageClass(I_CmsButton.EDIT_UP_SMALL);
        m_deleteButton.setImageClass(I_CmsButton.CUT_SMALL);
        m_addButton.setImageClass(I_CmsButton.ADD_SMALL);
        m_editButton.setImageClass(I_CmsButton.EDIT_SMALL);
        m_dummyButton.setImageClass(I_CmsButton.EDIT_POINT_SMALL);

    }

    /**
     * Shows / hides the edit button.
     *
     * @param editEnabled true if edit button should be shown
     */
    public void setEditVisible(boolean editEnabled) {

        m_editButton.setVisible(editEnabled);
    }

    /**
     * Sets the 'first' status of the button bar.<p>
     *
     * @param first true if this is the button bar of the first row of a multivalue field
     */
    public void setFirst(boolean first) {

        m_upButton.setVisible(!first);
    }

    /**
     * Hides the add button.<p>
     *
     * @param hideAdd true-> hide the add button
     */
    public void setHideAdd(boolean hideAdd) {

        m_addButton.setVisible(!hideAdd);
    }

    /**
     * Sets the 'last' status of the button bar.<p>
     *
     * @param last true if this is the button bar of the last row of a multivalue field
     */
    public void setLast(boolean last) {

        m_downButton.setVisible(!last);
    }

    /**
     * UI handler for the 'add' button.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_addButton")
    void clickAdd(ClickEvent event) {

        //HANDLER.closeAll();
        m_connector.getRpc().onAdd();

    }

    /**
     * UI handler for the 'delete' button.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_deleteButton")
    void clickDelete(ClickEvent event) {

        //HANDLER.closeAll();
        m_connector.getRpc().onDelete();

    }

    /**
     * UI handler for the 'down' button.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_downButton")
    void clickDown(ClickEvent event) {

        //HANDLER.closeAll();
        m_connector.getRpc().onDown();
    }

    /**
     * Handler for the edit button.
     *
     * @param event the event
     */
    @UiHandler("m_editButton")
    void clickEdit(ClickEvent event) {

        m_connector.getRpc().onEdit();
    }

    /**
     * UI handler for the 'up' button.<p>
     *
     * @param event the click event
     */
    @UiHandler("m_upButton")
    void clickUp(ClickEvent event) {

        //HANDLER.closeAll();
        m_connector.getRpc().onUp();
    }

}
