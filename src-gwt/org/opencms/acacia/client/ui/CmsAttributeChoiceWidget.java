/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.client.ui;

import org.opencms.acacia.client.CmsButtonBarHandler;
import org.opencms.acacia.client.CmsChoiceMenuEntryBean;
import org.opencms.acacia.client.I_CmsWidgetService;
import org.opencms.acacia.client.css.I_CmsLayoutBundle;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The attribute choice widget.<p>
 */
public class CmsAttributeChoiceWidget extends Composite implements HasMouseOverHandlers, HasMouseOutHandlers {

    /**
     * The UI binder interface.<p>
     */
    interface I_AttributeChoiceWidgetUiBinder extends UiBinder<HTMLPanel, CmsAttributeChoiceWidget> {
        // nothing to do
    }

    /** The UI binder instance. */
    private static I_AttributeChoiceWidgetUiBinder uiBinder = GWT.create(I_AttributeChoiceWidgetUiBinder.class);

    /** The button icon element. */
    @UiField
    SpanElement m_buttonIcon;

    /** The choices panel. */
    @UiField
    FlowPanel m_choices;

    /**
     * Constructor.<p>
     */
    public CmsAttributeChoiceWidget() {

        initWidget(uiBinder.createAndBindUi(this));
        addMouseOutHandler(CmsButtonBarHandler.INSTANCE);
        addMouseOverHandler(CmsButtonBarHandler.INSTANCE);
    }

    /**
     * Adds a new choice entry.<p>
     * 
     * @param widgetService the widget service to use for labels 
     * @param menuEntry the menu entry bean 
     * @param selectHandler the handler to use for selecting entries 
     */
    public void addChoice(
        I_CmsWidgetService widgetService,
        CmsChoiceMenuEntryBean menuEntry,
        AsyncCallback<CmsChoiceMenuEntryBean> selectHandler) {

        Widget choice = new CmsChoiceMenuEntryWidget(
            widgetService.getAttributeLabel(menuEntry.getPathComponent()),
            widgetService.getAttributeHelp(menuEntry.getPathComponent()),
            menuEntry,
            selectHandler,
            this,
            null);
        addChoice(choice);
    }

    /**
     * Adds a choice to the widget.<p>
     * 
     * @param choice the choice to add
     */
    public void addChoice(Widget choice) {

        choice.setStyleName(I_CmsLayoutBundle.INSTANCE.attributeChoice().choice());
        m_choices.add(choice);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {

        return addDomHandler(handler, MouseOutEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        return addDomHandler(handler, MouseOverEvent.getType());
    }

    /**
     * Gets the panel into which submenus of this menu should be inserted.<p>
     * 
     * @return the panel for submenus 
     */
    public Panel getSubmenuPanel() {

        return (Panel)getParent();
    }

    /**
     * Hides the choice menu.<p>
     */
    public void hide() {

        removeStyleName(I_CmsLayoutBundle.INSTANCE.attributeChoice().hovering());
    }

    /**
     * Shows the choice menu.<p>
     */
    public void show() {

        addStyleName(I_CmsLayoutBundle.INSTANCE.attributeChoice().hovering());
        if (displayAbove()) {
            addStyleName(I_CmsLayoutBundle.INSTANCE.attributeChoice().displayAbove());
        } else {
            removeStyleName(I_CmsLayoutBundle.INSTANCE.attributeChoice().displayAbove());
        }
    }

    /**
     * Evaluates if the choice select should be displayed above the button.<p>
     * 
     * @return <code>true</code> if the choice select should be displayed above the button
     */
    private boolean displayAbove() {

        int popupHeight = m_choices.getOffsetHeight();
        // Calculate top position for the choice select
        int top = m_buttonIcon.getAbsoluteTop();

        // Make sure scrolling is taken into account, since
        // box.getAbsoluteTop() takes scrolling into account.
        int windowTop = Window.getScrollTop();
        int windowBottom = Window.getScrollTop() + Window.getClientHeight();

        // Distance from the top edge of the window to the top edge of the
        // text box
        int distanceFromWindowTop = top - windowTop;

        // Distance from the bottom edge of the window to the bottom edge of
        // the text box
        int distanceToWindowBottom = windowBottom - (top + m_buttonIcon.getOffsetHeight());

        // If there is not enough space for the popup's height below the button
        // and there IS enough space for the popup's height above the button,
        // then then position the popup above the button. However, if there
        // is not enough space on either side, then stick with displaying the
        // popup below the button.
        boolean displayAbove = (distanceFromWindowTop > distanceToWindowBottom)
            && (distanceToWindowBottom < popupHeight);
        return displayAbove;
    }
}
