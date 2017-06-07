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
import org.opencms.acacia.client.widgets.I_CmsEditWidget;
import org.opencms.acacia.client.widgets.I_CmsFormEditWidget;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;

/**
 * A widget factory for native java script widgets.<p>
 */
public final class WidgetFactoryWrapper extends JavaScriptObject implements I_CmsWidgetFactory {

    /**
     * Constructor.<p>
     */
    protected WidgetFactoryWrapper() {

    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetFactory#createFormWidget(java.lang.String)
     */
    public I_CmsFormEditWidget createFormWidget(String configuration) {

        return new WidgetWrapper(createNativeWidget(configuration));
    }

    /**
     * @see org.opencms.acacia.client.I_CmsWidgetFactory#createInlineWidget(java.lang.String, com.google.gwt.dom.client.Element)
     */
    public I_CmsEditWidget createInlineWidget(String configuration, Element element) {

        return new WidgetWrapper(createNativeWrapedElement(configuration, element));
    }

    /**
     * Returns the widget name.<p>
     *
     * @return the widget name
     */
    public native String getName()/*-{
                                  return this.widgetName;
                                  }-*/;

    /**
     * Creates a native java script widget.<p>
     *
     * @param configuration the widget configuration
     *
     * @return the created widget
     */
    private native NativeEditWidget createNativeWidget(String configuration)/*-{
                                                                            return this.createNativeWidget(configuration);
                                                                            }-*/;

    /**
     * Creates a native java script widget wrapping an existing DOM element.<p>
     *
     * @param configuration the configuration
     * @param element the element to wrap
     *
     * @return the native widget
     */
    private native NativeEditWidget createNativeWrapedElement(String configuration, Element element)/*-{
                                                                                                    return this.createNativeWrapedElement(configuration, element);
                                                                                                    }-*/;
}
