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

package org.opencms.gwt.client.util;

import jsinterop.annotations.JsType;

/**
 * Interface to expose the embedded frame methods as Javascript methods so they can be called from a different GWT context.
 */
@JsType(isNative = true)
public interface I_CmsEmbeddedDialogFrame {

    /**
     * Hides the iframe.
     */
    void hide();

    /**
     * Sets the dialog loader.
     *
     * <p>This is called by the Javascript code in the iframe.
     *
     * @param loader the class used to load dialogs in the iframe itself
     */
    void installEmbeddedDialogLoader(I_CmsEmbeddedDialogLoader loader);

    /**
     * Triggers loading of a new dialog in the iframe.
     *
     * <p>If the iframe has not been created/initialized yet, this will trigger the initialization and load the dialog afterwards.
     *
     * @param dialogInfoJson the serialized dialog info JSON from an I_CmsEmbeddedDialogInfo bean
     * @param handler the embedded dialog handler
     */
    void loadDialog(String dialogInfoJson, I_CmsEmbeddedDialogHandlerJsCallbacks handler);

    /**
     * Triggers initialization of the iframe.
     */
    void preload();

}
