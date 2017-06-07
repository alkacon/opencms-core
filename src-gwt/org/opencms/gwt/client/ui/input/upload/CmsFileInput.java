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

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.CmsWidget;
import com.google.gwt.user.client.ui.HasName;
import com.google.gwt.user.client.ui.Widget;

/**
 * A file input field.<p>
 *
 * @since 8.0.0
 */
public class CmsFileInput extends CmsWidget implements HasName, HasChangeHandlers {

    /** The concrete file input implementation. */
    private I_CmsFileInputService m_impl;

    /** The input element. */
    private InputElement m_inputElement;

    /**
     * The default constructor.<p>
     */
    public CmsFileInput() {

        m_inputElement = Document.get().createFileInputElement();
        setElement(m_inputElement);
        setStyleName("gwt-FileUpload");
        m_impl = GWT.create(CmsFileInputImpl.class);
    }

    /**
     * @see com.google.gwt.event.dom.client.HasChangeHandlers#addChangeHandler(com.google.gwt.event.dom.client.ChangeHandler)
     */
    @Override
    public HandlerRegistration addChangeHandler(ChangeHandler handler) {

        return addDomHandler(handler, ChangeEvent.getType());
    }

    /**
     * Returns an array of CmsFile objects.<p>
     *
     * @return an array of CmsFile objects
     */
    public CmsFileInfo[] getFiles() {

        JsArray<CmsFileInfo> files = m_impl.getFiles(m_inputElement);
        CmsFileInfo[] result = new CmsFileInfo[files.length()];
        for (int i = 0; i < files.length(); ++i) {
            result[i] = files.get(i);
        }
        return result;
    }

    /**
     * @see com.google.gwt.user.client.ui.HasName#getName()
     */
    @Override
    public String getName() {

        return m_inputElement.getName();
    }

    /**
     * Returns <code>true</code> if multiple file selection is allowed, <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if multiple file selection is allowed, <code>false</code> otherwise
     */
    public boolean isAllowedMultipleFiles() {

        return m_impl.isAllowMultipleFiles(m_inputElement);
    }

    /**
     * Returns <code>true</code> if the input field is disabled <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if the input field is disabled <code>false</code> otherwise
     */
    public boolean isDisabled() {

        return m_inputElement.isDisabled();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onAttach()
     */
    @Override
    public void onAttach() {

        super.onAttach();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onDetach()
     */
    @Override
    public void onDetach() {

        super.onDetach();
    }

    /**
     * Sets the the flag for allowing multiple file selection.<p>
     *
     * @param allow <code>true</code> if the multiple file selection should be allowed
     */
    public void setAllowMultipleFiles(boolean allow) {

        m_impl.setAllowMultipleFiles(m_inputElement, allow);
    }

    /**
     * Sets the disabled flag.<p>
     *
     * @param disabled <code>true</code> if the input field should be disabled
     */
    public void setDisabled(boolean disabled) {

        m_inputElement.setDisabled(disabled);
    }

    /**
     * @see com.google.gwt.user.client.ui.HasName#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {

        m_inputElement.setName(name);
    }

    /**
     * @see com.google.gwt.user.client.ui.CmsWidget#setParent(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void setParent(Widget parent) {

        super.setParent(parent);
    }

    /**
     * Returns <code>true</code> if the control supports the HTML5 FileAPI and <code>false</code> otherwise.<p>
     *
     * @return <code>true</code> if the control supports the HTML5 FileAPI and <code>false</code> otherwise
     */
    public boolean supportsFileAPI() {

        return m_impl.supportsFileAPI();
    }

}
