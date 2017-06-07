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

import org.opencms.acacia.client.I_CmsWidgetFactory;
import org.opencms.acacia.client.widgets.I_CmsFormEditWidget;

import com.google.gwt.dom.client.Element;

/**
 * Use this widget factory to use stand alone widgets.<p>
 */
public abstract class A_NativeWidgetFactory implements I_CmsWidgetFactory {

    /**
     * Exports the widget factory.<p>
     */
    public native void exportFactory()/*-{
                                      var self = this;
                                      $wnd[this.@org.opencms.ade.contenteditor.widgetregistry.client.A_NativeWidgetFactory::getInitCallName()()] = function() {
                                      var factory = {
                                      instance : self,
                                      widgetName : self.@org.opencms.ade.contenteditor.widgetregistry.client.A_NativeWidgetFactory::getWidgetName()(),
                                      createNativeWidget : function(configuration) {

                                      return this.instance.@org.opencms.ade.contenteditor.widgetregistry.client.A_NativeWidgetFactory::createNativeWidget(Ljava/lang/String;)(configuration);
                                      },
                                      createNativeWrapedElement : function(configuration, element) {
                                      return this.instance.@org.opencms.ade.contenteditor.widgetregistry.client.A_NativeWidgetFactory::createNativeWrapedElement(Ljava/lang/String;Lcom/google/gwt/dom/client/Element;)(configuration, element);
                                      }
                                      };

                                      if ($wnd[@org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry::REGISTER_WIDGET_FACTORY_FUNCTION] != null
                                      && typeof $wnd[@org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry::REGISTER_WIDGET_FACTORY_FUNCTION] == 'function') {
                                      $wnd[@org.opencms.ade.contenteditor.widgetregistry.client.WidgetRegistry::REGISTER_WIDGET_FACTORY_FUNCTION]
                                      (factory);
                                      } else {
                                      throw 'Registry not available';
                                      }
                                      }
                                      }-*/;

    /**
     * Returns the name of the initialization call.<p>
     *
     * @return the name of the initialization call
     */
    protected abstract String getInitCallName();

    /**
     * Returns the widget name.<p>
     *
     * @return the widget name
     */
    protected abstract String getWidgetName();

    /**
     * Creates a widget wrapped in a native java script object.<p>
     *
     * @param configuration the widget configuration
     *
     * @return the wrapped widget
     */
    private NativeEditWidget createNativeWidget(String configuration) {

        I_CmsFormEditWidget widget = createFormWidget(configuration);
        return NativeEditWidget.wrapWidget(widget, true);
    }

    /**
     * Creates a widget wrapped in a native java script object.<p>
     *
     * @param configuration the widget configuration
     * @param element the element to use
     *
     * @return the wrapped widget
     */
    private NativeEditWidget createNativeWrapedElement(String configuration, Element element) {

        return NativeEditWidget.wrapWidget(createInlineWidget(configuration, element), false);
    }

}
