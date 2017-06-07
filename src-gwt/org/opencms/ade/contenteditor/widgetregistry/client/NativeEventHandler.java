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

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

/**
 * Handler to delegate events to a native java script connector.<p>
 */
public class NativeEventHandler implements ValueChangeHandler<String>, FocusHandler {

    /** The native connector. */
    NativeEditWidget m_connector;

    /**
     * Constructor.<p>
     *
     * @param connector the native connector object that needs to provide an onChange method
     */
    public NativeEventHandler(NativeEditWidget connector) {

        m_connector = connector;
    }

    /**
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(com.google.gwt.event.logical.shared.ValueChangeEvent)
     */
    public native void onValueChange(ValueChangeEvent<String> event) /*-{
                                                                     var connector = this.@org.opencms.ade.contenteditor.widgetregistry.client.NativeEventHandler::m_connector;
                                                                     if (connector.onChange != null) {
                                                                     connector.onChange();
                                                                     }
                                                                     }-*/;

    /**
     * @see com.google.gwt.event.dom.client.FocusHandler#onFocus(com.google.gwt.event.dom.client.FocusEvent)
     */
    public native void onFocus(FocusEvent event) /*-{

                                                 var connector = this.@org.opencms.ade.contenteditor.widgetregistry.client.NativeEventHandler::m_connector;
                                                 if (connector.onFocus != null) {
                                                 connector.onFocus();
                                                 }
                                                 }-*/;
}
