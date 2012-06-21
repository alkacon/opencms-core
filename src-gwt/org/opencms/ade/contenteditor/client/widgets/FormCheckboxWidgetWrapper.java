/*
 * This library is part of the Acacia Editor -
 * an open source inline and form based content editor for GWT.
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
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.contenteditor.client.widgets;

import com.alkacon.acacia.client.css.I_LayoutBundle;
import com.alkacon.acacia.client.widgets.I_EditWidget;
import com.alkacon.acacia.client.widgets.I_FormEditWidget;

import org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle.I_CmsWidgetCss;

import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

/** 
 * Wraps an edit widget to supply a widget label.<p>
 **/
public class FormCheckboxWidgetWrapper extends Composite implements I_FormEditWidget {

    /** The edit widget. */
    private I_EditWidget m_editWidget;

    /** The label. */
    private Label m_label;

    /** The main panel. */
    private FlowPanel m_mainPanel;

    /** The main css class name. */
    I_CmsWidgetCss m_cssClass = org.opencms.ade.contenteditor.client.css.I_CmsLayoutBundle.INSTANCE.widgetCss();

    /**
     * Constructor.<p>
     * 
     * @param editWidget the edit widget to wrap
     */
    public FormCheckboxWidgetWrapper(I_EditWidget editWidget) {

        m_mainPanel = new FlowPanel();
        m_label = new Label();
        //m_label.setStyleName(I_LayoutBundle.INSTANCE.form().label());
        m_label.setStyleName(m_cssClass.checkBoxLable());
        m_editWidget = editWidget;
        m_mainPanel.add(m_label);
        m_mainPanel.add(m_editWidget);
        m_editWidget.asWidget().addStyleName(I_LayoutBundle.INSTANCE.form().widget());
        m_editWidget.asWidget().setStyleName(m_cssClass.checkBox());
        initWidget(m_mainPanel);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return m_editWidget.addFocusHandler(handler);
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return m_editWidget.addValueChangeHandler(handler);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_editWidget.getValue();
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#isActive()
     */
    public boolean isActive() {

        return m_editWidget.isActive();
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        super.onAttach();
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        m_editWidget.setActive(active);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        m_editWidget.setValue(value);
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_EditWidget#setValue(java.lang.String, boolean)
     */
    public void setValue(String value, boolean fireEvent) {

        m_editWidget.setValue(value, fireEvent);
    }

    /**
     * @see com.alkacon.acacia.client.widgets.I_FormEditWidget#setWidgetInfo(java.lang.String, java.lang.String)
     */
    public void setWidgetInfo(String label, String help) {

        m_label.setText(label);
        m_label.setTitle(help);
    }

}
