/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsUploadButton.java,v $
 * Date   : $Date: 2011/02/14 13:05:55 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;

import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * Provides a upload button.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsUploadButton extends FlowPanel implements HasMouseOverHandlers, HasMouseOutHandlers {

    /**
     * The handler implementation for this class.<p>
     */
    protected class CmsUploadButtonHandler implements MouseOverHandler, MouseOutHandler, ChangeHandler {

        /**
         * @see com.google.gwt.event.dom.client.ChangeHandler#onChange(com.google.gwt.event.dom.client.ChangeEvent)
         */
        public void onChange(ChangeEvent event) {

            addFileInput();
        }

        /**
         * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
         */
        public void onMouseOut(MouseOutEvent event) {

            setStyleDependentName("up-hovering", false);
        }

        /**
         * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
         */
        public void onMouseOver(MouseOverEvent event) {

            setStyleDependentName("up-hovering", true);
        }
    }

    /** The file input field. */
    private CmsFileInput m_fileInput;

    /** The handler for this panel. */
    private CmsUploadButtonHandler m_handler;

    /** The upload button. */
    private CmsPushButton m_uploadButton;

    /** The upload dialog. */
    private CmsUploadDialog m_uploadDialog;

    /**
     * The default constructor.<p>
     * 
     * Creates a new upload button. This upload button opens a new OS file selector on click.<p>
     * 
     * On change (user has selected one or more file(s)) a new Upload Dialog is created.<p>
     */
    public CmsUploadButton() {

        // create a handler for this button
        m_handler = new CmsUploadButtonHandler();
        // create the push button
        m_uploadButton = new CmsPushButton();

        setStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsState());
        addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().uploadButton());

        addMouseOutHandler(m_handler);
        addMouseOverHandler(m_handler);

        m_uploadButton.setText(Messages.get().key(Messages.GUI_UPLOAD_BUTTON_TITLE_0));
        add(m_uploadButton);

        createFileInput();
    }

    /**
     * The constructor for an already opened Upload Dialog.<p>
     * 
     * Creates a new upload button. This upload button is part of the given Upload Dialog.<p>
     * 
     * In difference to the default constructor it will not create and show a new Upload Dialog on change.<p> 
     * 
     * @param dialog the upload dialog
     */
    public CmsUploadButton(CmsUploadDialog dialog) {

        this();
        m_uploadDialog = dialog;
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler(com.google.gwt.event.dom.client.MouseOutHandler)
     */
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {

        return addDomHandler(m_handler, MouseOutEvent.getType());
    }

    /**
     * @see com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler(com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {

        return addDomHandler(m_handler, MouseOverEvent.getType());
    }

    /**
     * Sets the text of the upload button.<p>
     * 
     * @param text the text to set
     */
    public void setText(String text) {

        m_uploadButton.setText(text);
    }

    /**
     * Opens the dialog and creates a new input.<p>
     * 
     * On change the upload dialog is opened and a new file input field will be created.<p>
     */
    protected void addFileInput() {

        if (m_uploadDialog != null) {
            m_uploadDialog.addFileInput(m_fileInput);
        } else {
            new CmsUploadDialog().loadAndShow(m_fileInput);
        }
        createFileInput();
    }

    /**
     * Creates and adds a file input.<p>
     */
    private void createFileInput() {

        // hide the current file input field and add a new one
        if (m_fileInput != null) {
            m_fileInput.getElement().getStyle().setDisplay(Display.NONE);
        }

        m_fileInput = new CmsFileInput();
        m_fileInput.addChangeHandler(m_handler);
        m_fileInput.setAllowMultipleFiles(true);
        m_fileInput.setName("upload");
        m_fileInput.addStyleName(I_CmsLayoutBundle.INSTANCE.uploadCss().uploadFileInput());
        add(m_fileInput);
    }
}
