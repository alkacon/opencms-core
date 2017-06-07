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
 * The default file input implementation.<p>
 *
 * @since 8.0.0
 */
public class CmsFileInputImpl implements I_CmsFileInputService {

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsFileInputService#getFiles(com.google.gwt.dom.client.InputElement)
     */
    public native JsArray<CmsFileInfo> getFiles(InputElement inputElement) /*-{
                                                                           var name = inputElement.value.replace(/^.*\\/, '')
                                                                           return inputElement.value && inputElement.value != "" ? [ {
                                                                           name : name,
                                                                           size : -1,
                                                                           input : inputElement
                                                                           } ] : [];
                                                                           }-*/;

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsFileInputService#isAllowMultipleFiles(com.google.gwt.dom.client.InputElement)
     */
    public boolean isAllowMultipleFiles(InputElement inputElement) {

        return false;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsFileInputService#setAllowMultipleFiles(com.google.gwt.dom.client.InputElement, boolean)
     */
    public void setAllowMultipleFiles(InputElement inputElement, boolean allow) {

        // noop
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsFileInputService#supportsFileAPI()
     */
    public boolean supportsFileAPI() {

        return false;
    }
}