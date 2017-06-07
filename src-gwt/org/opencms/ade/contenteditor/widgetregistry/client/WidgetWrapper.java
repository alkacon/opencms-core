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

package org.opencms.ade.contenteditor.widgetregistry.client;

import org.opencms.acacia.client.widgets.I_CmsFormEditWidget;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

/**
 * Wrapper for a native widget.<p>
 */
public final class WidgetWrapper extends Widget implements I_CmsFormEditWidget {

    /** The wrapped native widget. */
    private NativeEditWidget m_nativeWidget;

    /**
     * Constructor.<p>
     *
     * @param nativeWidget the native widget
     */
    protected WidgetWrapper(NativeEditWidget nativeWidget) {

        m_nativeWidget = nativeWidget;
        setElement(m_nativeWidget.getElement());
        initNativeWidget();
    }

    /**
     * @see com.google.gwt.event.dom.client.HasFocusHandlers#addFocusHandler(com.google.gwt.event.dom.client.FocusHandler)
     */
    public HandlerRegistration addFocusHandler(FocusHandler handler) {

        return addDomHandler(handler, FocusEvent.getType());
    }

    /**
     * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {

        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#getValue()
     */
    public String getValue() {

        return m_nativeWidget.getValue();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#isActive()
     */
    public boolean isActive() {

        return m_nativeWidget.isActive();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#onAttachWidget()
     */
    public void onAttachWidget() {

        onAttach();
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#owns(com.google.gwt.dom.client.Element)
     */
    public boolean owns(Element element) {

        // TODO implement this in case we want the delete behavior for optional fields
        return false;

    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setActive(boolean)
     */
    public void setActive(boolean active) {

        m_nativeWidget.setActive(active);
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setName(java.lang.String)
     */
    public void setName(String name) {

        // no input field so nothing to do

    }

    /**
     * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
     */
    public void setValue(String value) {

        setValue(value, true);
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsEditWidget#setValue(java.lang.String, boolean)
     */
    public void setValue(String value, boolean fireEvents) {

        m_nativeWidget.setValue(value, fireEvents);
    }

    /**
     * @see org.opencms.acacia.client.widgets.I_CmsFormEditWidget#setWidgetInfo(java.lang.String, java.lang.String)
     */
    public void setWidgetInfo(String label, String help) {

        m_nativeWidget.setWidgetInfo(label, help);
    }

    /**
     * Fires the value change event.<p>
     */
    protected void fireChangeEvent() {

        ValueChangeEvent.fire(this, getValue());
    }

    /**
     * Fires the focus event.<p>
     */
    protected void fireFocusEvent() {

        DomEvent.fireNativeEvent(Document.get().createFocusEvent(), this);
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onAttach()
     */
    @Override
    protected void onAttach() {

        super.onAttach();
        m_nativeWidget.onAttachWidget();
    }

    /**
     * Initializes the native widget by setting the on change and on focus functions.<p>
     */
    private native void initNativeWidget()/*-{
                                          var self = this;
                                          var nativeWidget = this.@org.opencms.ade.contenteditor.widgetregistry.client.WidgetWrapper::m_nativeWidget;
                                          nativeWidget.onChangeCommand = function() {
                                          self.@org.opencms.ade.contenteditor.widgetregistry.client.WidgetWrapper::fireChangeEvent()();
                                          }
                                          nativeWidget.onFocusCommand = function() {
                                          self.@org.opencms.ade.contenteditor.widgetregistry.client.WidgetWrapper::fireFocusEvent()();
                                          }
                                          }-*/;
}
