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

package org.opencms.gwt.client.js;

import elemental2.core.JsArray;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsType;
import jsinterop.base.JsPropertyMap;

/**
 * JS interop class for native ResizeObserver.
 */
@JsType(isNative = true, name = "ResizeObserver", namespace = "<window>")
public class ResizeObserver {

    /**
     * JS Callback for resize observers.
     */
    @JsFunction
    public interface Callback {

        /**
         * Called by the resize observer.
         *
         * @param entries the resize observer entries
         */
        void call(JsArray<JsPropertyMap<?>> entries);
    }

    /**
     * Creates a new instance.
     *
     * @param callback the callback to call when resizes happen
     */
    public ResizeObserver(Callback callback) {

        super();
    }

    /**
     * Disconnects the resize observer.
     */
    public native void disconnect();

    /**
     * Starts observation for an element.
     *
     * @param element the element to observer
     */
    public native void observe(elemental2.dom.Element element);

    /**
     * Stops observation for an element.
     *
     * @param element the element for which to stop observation
     */
    public native void unobserve(elemental2.dom.Element element);

}
