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

package org.opencms.acacia.client.widgets;

import org.opencms.acacia.client.css.I_LayoutBundle;
import org.opencms.gwt.client.I_HasResizeOnShow;
import org.opencms.gwt.client.util.CmsDomUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;

/** 
 * Wraps an edit widget to supply a widget label.<p>
 **/
public class FormWidgetWrapper extends Composite implements I_FormEditWidget, HasResizeHandlers, I_HasResizeOnShow {

    /** The edit widget. */
    private I_EditWidget m_editWidget;

    /** The label. */
    private HTML m_label;

    /** The main panel. */
    private FlowPanel m_mainPanel;

    /**
     * Constructor.<p>
     */
    public FormWidgetWrapper() {

        m_mainPanel = new FlowPanel();
        m_label = new HTML();
        m_label.setStyleName(I_LayoutBundle.INSTANCE.form().label());
        m_mainPanel.add(m_label);
        initWidget(m_mainPanel);
    }

    /**
     * Constructor.<p>
     * 
     * @param editWidget the edit widget to wrap
     */
    public FormWidgetWrapper(I_EditWidget editWidget) {

        this();
        setEditWidget(editWidget);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        // make sure the widget has been initialized
        assert m_editWidget != null;
        return m_editWidget.addFocusHandler(handler);
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasResizeHandlers#addResizeHandler(com.google.gwt.event.logical.shared.ResizeHandler)
     */
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {

        // make sure the widget has been initialized
        assert m_editWidget != null;
        if (m_editWidget instanceof HasResizeHandlers) {
            return ((HasResizeHandlers)m_editWidget).addResizeHandler(handler);
        }
        return null;
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_EditWidget#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        // make sure the widget has been initialized
        assert m_editWidget != null;
        return m_editWidget.addValueChangeHandler(handler);
    }

    /**
     * Gets the wrapped widget.<p>
     * 
     * @return the wrapped widget 
     */
    public I_EditWidget getEditWidget() {

        // make sure the widget has been initialized
        assert m_editWidget != null;
        return m_editWidget;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        // make sure the widget has been initialized
        assert m_editWidget != null;
        return m_editWidget.getValue();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_EditWidget#isActive()
     */
    public boolean isActive() {

        // make sure the widget has been initialized
        assert m_editWidget != null;
        return m_editWidget.isActive();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_EditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        super.onAttach();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_EditWidget#owns(com.google.gwt.dom.client.Element)
     */
    public boolean owns(Element element) {

        // make sure the widget has been initialized
        assert m_editWidget != null;
        return (m_editWidget != null) && m_editWidget.owns(element);
    }

    /**
     * @see org.opencms.gwt.client.I_HasResizeOnShow#resizeOnShow()
     */
    public void resizeOnShow() {

        // make sure the widget has been initialized
        assert m_editWidget != null;
        if (m_editWidget instanceof I_HasResizeOnShow) {
            ((I_HasResizeOnShow)m_editWidget).resizeOnShow();
        }
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_EditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        // make sure the widget has been initialized
        assert m_editWidget != null;
        m_editWidget.setActive(active);
    }

    /**
     * The edit widget needs to set, before the widget may be used.<p>
     * 
     * @param editWidget the edit widget to wrap
     */
    public void setEditWidget(I_EditWidget editWidget) {

        // the edit widget may be set only once
        assert m_editWidget == null;
        m_editWidget = editWidget;
        m_mainPanel.add(m_editWidget);
        m_editWidget.asWidget().addStyleName(I_LayoutBundle.INSTANCE.form().widget());
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_EditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        // make sure the widget has been initialized
        assert m_editWidget != null;
        m_editWidget.setName(name);

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        // make sure the widget has been initialized
        assert m_editWidget != null;
        m_editWidget.setValue(value);
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_EditWidget#setValue(java.lang.String, boolean)
     */
    public void setValue(String value, boolean fireEvent) {

        // make sure the widget has been initialized
        assert m_editWidget != null;
        m_editWidget.setValue(value, fireEvent);
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_FormEditWidget#setWidgetInfo(java.lang.String, java.lang.String)
     */
    public void setWidgetInfo(String label, String help) {

        m_label.setHTML(label);
        m_label.setTitle(CmsDomUtil.stripHtml(help));
    }

}
