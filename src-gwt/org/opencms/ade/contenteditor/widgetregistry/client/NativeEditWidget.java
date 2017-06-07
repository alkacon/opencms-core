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

import org.opencms.acacia.client.widgets.I_CmsEditWidget;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

/**
 * Overlay object for native java script widgets.<p>
 */
public final class NativeEditWidget extends JavaScriptObject {

    /**
     * Constructor.<p>
     */
    protected NativeEditWidget() {

    }

    /**
     * Wraps a GWT edit widget instance to be used from within another GWT module.<p>
     *
     * @param widget the widget to wrap
     * @param isFormWidget in case of a form widget
     *
     * @return the wrapping native java script object
     */
    public static native NativeEditWidget wrapWidget(I_CmsEditWidget widget, boolean isFormWidget)/*-{
                                                                                                  var nat = {
                                                                                                  instance : widget
                                                                                                  };
                                                                                                  nat.getElement = function() {
                                                                                                  return this.instance.@org.opencms.acacia.client.widgets.I_CmsEditWidget::asWidget()().@com.google.gwt.user.client.ui.Widget::getElement()();
                                                                                                  }
                                                                                                  nat.isActive = function() {
                                                                                                  return this.instance.@org.opencms.acacia.client.widgets.I_CmsEditWidget::isActive()();
                                                                                                  }
                                                                                                  nat.setActive = function(active) {
                                                                                                  return this.instance.@org.opencms.acacia.client.widgets.I_CmsEditWidget::setActive(Z)(active);
                                                                                                  }
                                                                                                  nat.getValue = function() {
                                                                                                  return this.instance.@org.opencms.acacia.client.widgets.I_CmsEditWidget::getValue()();
                                                                                                  }
                                                                                                  nat.setValue = function(value, fireEvent) {
                                                                                                  this.instance.@org.opencms.acacia.client.widgets.I_CmsEditWidget::setValue(Ljava/lang/String;Z)(value, fireEvent);
                                                                                                  }
                                                                                                  if (isFormWidget) {
                                                                                                  nat.setWidgetInfo = function(label, help){
                                                                                                  this.instance.@org.opencms.acacia.client.widgets.I_CmsFormEditWidget::setWidgetInfo(Ljava/lang/String;Ljava/lang/String;)(label,help);
                                                                                                  }
                                                                                                  } else {
                                                                                                      nat.setWidgetInfo=function(){}
                                                                                                  }
                                                                                                  nat.onAttachWidget = function() {
                                                                                                  this.instance.@org.opencms.acacia.client.widgets.I_CmsEditWidget::onAttachWidget()();
                                                                                                  }
                                                                                                  nat.onChange = function() {
                                                                                                  if (this.onChangeCommand != null) {
                                                                                                  this.onChangeCommand();
                                                                                                  }
                                                                                                  };
                                                                                                  nat.onFocus = function() {
                                                                                                  if (this.onFocusCommand != null) {
                                                                                                  this.onFocusCommand();
                                                                                                  }
                                                                                                  };
                                                                                                  var nativeHandler = @org.opencms.ade.contenteditor.widgetregistry.client.NativeEditWidget::getNativeHandler(Lorg/opencms/ade/contenteditor/widgetregistry/client/NativeEditWidget;)(nat);
                                                                                                  widget.@org.opencms.acacia.client.widgets.I_CmsEditWidget::addValueChangeHandler(Lcom/google/gwt/event/logical/shared/ValueChangeHandler;)(nativeHandler);
                                                                                                  widget.@org.opencms.acacia.client.widgets.I_CmsEditWidget::addFocusHandler(Lcom/google/gwt/event/dom/client/FocusHandler;)(nativeHandler);
                                                                                                  return nat;
                                                                                                  }-*/;

    /**
     * Returns an event handler that delegates to a native java script object.<p>
     *
     * @param connector the native java script object
     *
     * @return the event handler
     */
    private static NativeEventHandler getNativeHandler(NativeEditWidget connector) {

        return new NativeEventHandler(connector);
    }

    /**
     * Returns the widget element.<p>
     *
     * @return the widget element
     */
    protected native Element getElement() /*-{
                                          return this.getElement();
                                          }-*/;

    /**
     * Returns the widget value.<p>
     *
     * @return the widget value
     */
    protected native String getValue()/*-{
                                      return this.getValue();
                                      }-*/;

    /**
     * Returns if the widget is active.<p>
     *
     * @return <code>true</code> if the widget is active
     */
    protected native boolean isActive() /*-{
                                        return this.isActive();
                                        }-*/;

    /**
     * Call when the widget was added into the window document.<p>
     */
    protected native void onAttachWidget()/*-{
                                          this.onAttachWidget();
                                          }-*/;

    /**
     * Sets the widget active.<p>
     *
     * @param active <code>true</code> to activate the widget
     */
    protected native void setActive(boolean active)/*-{
                                                   this.setActive(active);
                                                   }-*/;

    /**
     * Sets the widget value.<p>
     *
     * @param value the value
     */
    protected native void setValue(String value)/*-{
                                                this.setValue(value, false);
                                                }-*/;

    /**
     * Sets the widget value.<p>
     *
     * @param value the value
     * @param fireEvent <code>true</code> to fire the value change event
     */
    protected native void setValue(String value, boolean fireEvent)/*-{
                                                                   this.setValue(value, fireEvent);
                                                                   }-*/;

    /**
     * Sets the widget label and help text.<p>
     *
     * @param label the widget label text
     * @param help the widget help text
     */
    protected native void setWidgetInfo(String label, String help)/*-{
                                                                  this.setWidgetInfo(label, help);
                                                                  }-*/;
}
