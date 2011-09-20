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

package org.opencms.ade.upload.client.ui;

import org.opencms.ade.upload.client.Messages;
import org.opencms.gwt.client.ui.CmsFlowPanel;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonColor;
import org.opencms.gwt.client.ui.I_CmsButton.ButtonStyle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.upload.CmsFileInput;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsStringUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Provides a upload button.<p>
 * 
 * @since 8.0.0
 */
public class CmsUploadButton extends Composite implements HasHorizontalAlignment {

    /**
     * The handler implementation for this class.<p>
     */
    protected class CmsUploadButtonHandler implements ChangeHandler {

        /**
         * @see com.google.gwt.event.dom.client.ChangeHandler#onChange(com.google.gwt.event.dom.client.ChangeEvent)
         */
        public void onChange(ChangeEvent event) {

            CmsDomUtil.ensureMouseOut(m_main.getElement());
            onChangeAction();
        }
    }

    /** The ui-binder interface. */
    protected interface I_CmsUploadButtonUiBinder extends UiBinder<CmsFlowPanel, CmsUploadButton> {
        // GWT interface, nothing to do
    }

    /** The ui-binder for this widget. */
    private static I_CmsUploadButtonUiBinder m_uiBinder = GWT.create(I_CmsUploadButtonUiBinder.class);

    /** The button face. */
    @UiField
    protected HTML m_buttonFace;

    /** The main panel. */
    @UiField
    protected CmsFlowPanel m_main;

    /** The horizontal alignment. */
    private HorizontalAlignmentConstant m_align;

    /** Stores the button style. */
    private ButtonStyle m_buttonStyle;

    /** The dialog close handler. */
    private CloseHandler<PopupPanel> m_closeHandler;

    /** Stores the button color. */
    private I_CmsButton.ButtonColor m_color;

    /** Flag if button is enabled. */
    private boolean m_enabled;

    /** The file input field. */
    private CmsFileInput m_fileInput;

    /** The handler for this panel. */
    private CmsUploadButtonHandler m_handler;

    /** The icon image css class. */
    private String m_imageClass;

    /** The button size. */
    private I_CmsButton.Size m_size;

    /** The current style dependent name. */
    private String m_styleDependent;

    /** The target folder for the file upload. */
    private String m_targetFolder;

    /** The button text. */
    private String m_text;

    /** The button title. */
    private String m_title;

    /** The upload dialog. */
    private A_CmsUploadDialog m_uploadDialog;

    /** Flag if a button minimum width should be used. */
    private boolean m_useMinWidth;

    /**
     * The default constructor.<p>
     * 
     * Creates a new upload button. This upload button opens a new OS file selector on click.<p>
     * 
     * On change (user has selected one or more file(s)) a new Upload Dialog is created.<p>
     */
    public CmsUploadButton() {

        org.opencms.ade.upload.client.ui.css.I_CmsLayoutBundle.INSTANCE.uploadCss().ensureInjected();
        initWidget(m_uiBinder.createAndBindUi(this));
        m_align = HasHorizontalAlignment.ALIGN_RIGHT;
        updateState("up");
        m_enabled = true;
        // create a handler for this button
        m_handler = new CmsUploadButtonHandler();
        setSize(I_CmsButton.Size.medium);
        // create the push button
        setText(Messages.get().key(Messages.GUI_UPLOAD_BUTTON_TITLE_0));
        setButtonStyle(ButtonStyle.TEXT, ButtonColor.BLUE);
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
    public CmsUploadButton(A_CmsUploadDialog dialog) {

        this();
        m_uploadDialog = dialog;
    }

    /**
     * Disables the button and changes the button title attribute to the disabled reason.<p>
     *   
     * @param disabledReason the disabled reason
     */
    public void disable(String disabledReason) {

        m_enabled = false;
        // hide the current file input field
        if (m_fileInput != null) {
            m_fileInput.getElement().getStyle().setDisplay(Display.NONE);
        }

        updateState("up-disabled");
        super.setTitle(disabledReason);
    }

    /**
     * Enables the button, switching the button title attribute from the disabled reason to the original title.<p>
     */
    public void enable() {

        updateState("up");
        m_enabled = true;
        // show the current file input field
        if (m_fileInput != null) {
            m_fileInput.getElement().getStyle().clearDisplay();
        }
        super.setTitle(m_title);
    }

    /**
     * This is the alignment of the text in reference to the image, possible values are left or right.<p>
     * 
     * @see com.google.gwt.user.client.ui.HasHorizontalAlignment#getHorizontalAlignment()
     */
    public HorizontalAlignmentConstant getHorizontalAlignment() {

        return m_align;
    }

    /**
     * Returns the master image class.<p>
     *
     * @return the master image class
     */
    public String getImageClass() {

        return m_imageClass;
    }

    /**
     * Returns the size.<p>
     *
     * @return the size
     */
    public I_CmsButton.Size getSize() {

        return m_size;
    }

    /**
     * Returns the targetFolder.<p>
     *
     * @return the targetFolder
     */
    public String getTargetFolder() {

        return m_targetFolder;
    }

    /**
     * Returns the text.<p>
     *
     * @return the text
     */
    public String getText() {

        return m_text;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns if the upload button is enabled.<p>
     * 
     * @return <code>true</code> if the upload button is enabled
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Checks if the button is constraint to a minimal width.<p>
     *
     * @return <code>true</code> if the button is constraint to a minimal width
     */
    public boolean isUseMinWidth() {

        return m_useMinWidth;
    }

    /**
     * Sets the button style.<p>
     * 
     * @param style the style to set
     * @param color the color to set
     */
    public void setButtonStyle(I_CmsButton.ButtonStyle style, I_CmsButton.ButtonColor color) {

        if (m_buttonStyle != null) {
            for (String styleName : m_buttonStyle.getAdditionalClasses()) {
                removeStyleName(styleName);
            }
        }
        if (style == ButtonStyle.TRANSPARENT) {
            setSize(null);
        }
        addStyleName(style.getCssClassName());
        m_buttonStyle = style;

        if (m_color != null) {
            removeStyleName(m_color.getClassName());
        }
        if (color != null) {
            addStyleName(color.getClassName());
        }
        m_color = color;
    }

    /**
     * Sets the upload dialog close handler.<p>
     * 
     * @param closeHandler the close handler to set
     */
    public void setDialogCloseHandler(CloseHandler<PopupPanel> closeHandler) {

        m_closeHandler = closeHandler;
        if (m_uploadDialog != null) {
            m_uploadDialog.addCloseHandler(m_closeHandler);
        }
    }

    /**
     * This is the alignment of the text in reference to the image, possible values are left or right.<p>
     * 
     * @see com.google.gwt.user.client.ui.HasHorizontalAlignment#setHorizontalAlignment(com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant)
     */
    public void setHorizontalAlignment(HorizontalAlignmentConstant align) {

        if (align.equals(HasHorizontalAlignment.ALIGN_CENTER)) {
            // ignore center alignment
            return;
        }
        m_align = align;
    }

    /**
     * Sets the master image class.<p>
     *
     * @param imageClass the master image class to set
     */
    public void setImageClass(String imageClass) {

        setUpFace(m_text, imageClass);
    }

    /**
     * Sets the size.<p>
     *
     * @param size the size to set
     */
    public void setSize(I_CmsButton.Size size) {

        if (m_size != null) {
            removeStyleName(m_size.getCssClassName());
        }
        if (size != null) {
            addStyleName(size.getCssClassName());
        }
        m_size = size;
    }

    /**
     * Sets the targetFolder.<p>
     *
     * @param targetFolder the targetFolder to set
     */
    public void setTargetFolder(String targetFolder) {

        m_targetFolder = targetFolder;
        if (m_uploadDialog != null) {
            m_uploadDialog.setTargetFolder(m_targetFolder);
        }
    }

    /**
     * Sets the text.<p>
     *
     * @param text the text to set
     */
    public void setText(String text) {

        setUpFace(text, m_imageClass);
        setTitle(text);
    }

    /**
     * @see com.google.gwt.user.client.ui.UIObject#setTitle(java.lang.String)
     */
    @Override
    public void setTitle(String title) {

        m_main.setTitle(title);
        m_title = title;
    }

    /**
     * Sets the up face text and image.<p>
     * 
     * @param text the up face text to set, set to <code>null</code> to not show any
     * @param imageClass the up face image class to use, set to <code>null</code> to not show any
     */
    public void setUpFace(String text, String imageClass) {

        m_text = text;
        m_imageClass = imageClass;
        m_buttonFace.setHTML(getFaceHtml(text, imageClass));
    }

    /**
     * Tells the button to use a minimal width.<p>
     *
     * @param useMinWidth <code>true</code> to use a minimal width
     */
    public void setUseMinWidth(boolean useMinWidth) {

        if (useMinWidth != m_useMinWidth) {
            if (useMinWidth) {
                addStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsMinWidth());
            } else {
                removeStyleName(I_CmsLayoutBundle.INSTANCE.buttonCss().cmsMinWidth());
            }
            m_useMinWidth = useMinWidth;
        }
    }

    /**
     * Creates and adds a file input.<p>
     */
    protected void createFileInput() {

        // remove the current file input field and add a new one
        if (m_fileInput != null) {
            m_fileInput.getElement().getStyle().setDisplay(Display.NONE);
        }
        m_fileInput = new CmsFileInput();
        m_fileInput.addChangeHandler(m_handler);
        m_fileInput.setAllowMultipleFiles(true);
        m_fileInput.setName("upload");
        m_fileInput.addStyleName(org.opencms.ade.upload.client.ui.css.I_CmsLayoutBundle.INSTANCE.uploadCss().uploadFileInput());
        m_main.add(m_fileInput);
    }

    /**
     * Convenience method to assemble the HTML to use for a button face.<p>
     * 
     * @param text text the up face text to set, set to <code>null</code> to not show any
     * @param imageClass the up face image class to use, set to <code>null</code> to not show any
     * 
     * @return the HTML
     */
    protected String getFaceHtml(String text, String imageClass) {

        return CmsDomUtil.createFaceHtml(text, imageClass, m_align);
    }

    /**
     * Handles the mouse over event on the main panel.<p>
     * 
     * @param event the event
     * 
     * @see com.google.gwt.event.dom.client.MouseOutHandler#onMouseOut(com.google.gwt.event.dom.client.MouseOutEvent)
     */
    @UiHandler("m_main")
    protected void handleMouseOut(MouseOutEvent event) {

        if (isEnabled()) {
            updateState("up");
        }
    }

    /**
     * Handles the mouse over event on the main panel.<p>
     * 
     * @param event the event
     * 
     * @see com.google.gwt.event.dom.client.MouseOverHandler#onMouseOver(com.google.gwt.event.dom.client.MouseOverEvent)
     */
    @UiHandler("m_main")
    protected void handleMouseOver(MouseOverEvent event) {

        if (isEnabled()) {
            updateState("up-hovering");
        }
    }

    /**
     * Opens the dialog and creates a new input.<p>
     * 
     * On change the upload dialog is opened and a new file input field will be created.<p>
     */
    protected void onChangeAction() {

        if (m_uploadDialog == null) {
            m_uploadDialog = GWT.create(CmsUploadDialogImpl.class);
            m_uploadDialog.setTargetFolder(m_targetFolder);
            m_uploadDialog.addFileInput(m_fileInput);
            if (m_closeHandler != null) {
                m_uploadDialog.addCloseHandler(m_closeHandler);
            }
        } else {
            m_uploadDialog.addFileInput(m_fileInput);
        }
        createFileInput();
    }

    private void updateState(String styleDependent) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(styleDependent)) {
            // reseting to cmsState-up
            styleDependent = "up";
        }
        if (!styleDependent.equals(m_styleDependent)) {
            m_main.removeStyleDependentName(m_styleDependent);
            m_main.setStyleDependentName(styleDependent, true);
            m_styleDependent = styleDependent;
        }
    }
}
