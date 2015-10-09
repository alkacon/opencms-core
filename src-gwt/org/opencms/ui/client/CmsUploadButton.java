/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.client;

import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadButton;
import org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Event;
import com.vaadin.client.ui.VButton;

/**
 * A Vaadin based upload button.<p>
 */
public class CmsUploadButton extends VButton implements I_CmsUploadButton {

    /** The button handler. */
    private I_CmsUploadButtonHandler m_buttonHandler;

    /** The current file input element. */
    private CmsFileInput m_fileInput;

    /** List of used input elements. */
    private List<CmsFileInput> m_usedInputs;

    /**
     * Constructor.<p>
     *
     * @param buttonHandler the button handler
     */
    public CmsUploadButton(I_CmsUploadButtonHandler buttonHandler) {
        super();
        addStyleName("o-upload-button");
        m_buttonHandler = buttonHandler;
        m_buttonHandler.setButton(this);
        m_usedInputs = new ArrayList<CmsFileInput>();
        createFileInput();
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploadButton#createFileInput()
     */
    public CmsFileInput createFileInput() {

        // remove the current file input field and add a new one
        CmsFileInput previous = m_fileInput;
        if (m_fileInput != null) {
            m_fileInput.getElement().getStyle().setDisplay(Display.NONE);
        }
        m_fileInput = new CmsFileInput();
        m_usedInputs.add(m_fileInput);
        m_fileInput.addChangeHandler(new ChangeHandler() {

            public void onChange(ChangeEvent event) {

                onInputChange();
            }
        });
        m_buttonHandler.initializeFileInput(m_fileInput);
        getElement().appendChild(m_fileInput.getElement());
        m_fileInput.sinkEvents(Event.ONCHANGE);
        m_fileInput.onAttach();
        return previous;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploadButton#getButtonHandler()
     */
    public I_CmsUploadButtonHandler getButtonHandler() {

        return m_buttonHandler;
    }

    /**
     * @see org.opencms.gwt.client.ui.input.upload.I_CmsUploadButton#reinitButton(org.opencms.gwt.client.ui.input.upload.I_CmsUploadButtonHandler)
     */
    public void reinitButton(I_CmsUploadButtonHandler buttonHandler) {

        for (CmsFileInput input : m_usedInputs) {
            input.onDetach();
            input.getElement().removeFromParent();
        }
        m_usedInputs.clear();
        m_buttonHandler = buttonHandler;
        m_buttonHandler.setButton(this);
        createFileInput();
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#doAttachChildren()
     */
    @Override
    protected void doAttachChildren() {

        super.doAttachChildren();
        if (m_fileInput != null) {
            m_fileInput.onAttach();
        }
    }

    /**
     * Handles the input change.<p>
     */
    void onInputChange() {

        // hack to reset the hover state
        CmsDomUtil.clearHover(getElement());
        m_buttonHandler.onChange(m_fileInput);
    }
}
