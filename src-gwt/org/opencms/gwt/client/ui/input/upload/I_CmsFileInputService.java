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

package org.opencms.gwt.client.ui.input.upload;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.InputElement;

/**
 * The interface for various implementations of a file input field.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsFileInputService {

    /**
     * Returns a JsArray of CmsFile objects.<p>
     *
     * @param inputElement the input element
     *
     * @return a JsArray of CmsFile objects
     */
    JsArray<CmsFileInfo> getFiles(InputElement inputElement);

    /**
     * The default implementation of the file input field does not support multiple file selection.<p>
     *
     * @param inputElement the input element
     *
     * @return <code>false</code> by default
     */
    boolean isAllowMultipleFiles(InputElement inputElement);

    /**
     * A dummy method, only used for sub classes.<p>
     *
     * @param inputElement the input element
     *
     * @param allow the flag that indicates if multiple file selection is supported
     */
    void setAllowMultipleFiles(InputElement inputElement, boolean allow);

    /**
     * The default implementation of the file input field does not support multiple file selection.<p>
     *
     * @return <code>false</code> by default
     */
    boolean supportsFileAPI();

}