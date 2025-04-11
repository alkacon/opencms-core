/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ui.client;

import org.opencms.gwt.client.util.I_CmsEmbeddedDialogHandlerJsCallbacks;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Helper class to provide access to embedded dialog handlers set by the parent frame.
 */
@JsType(name = "window", namespace = JsPackage.GLOBAL, isNative = true)
public class CmsEmbedWrapper {

    /**
     * The embedded dialog handler set by the parent frame.
     */
    public static I_CmsEmbeddedDialogHandlerJsCallbacks connector;
}
