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

package org.opencms.gwt.client.ui.input.form;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.input.I_CmsFormField;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.Map;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * A dialog containing a form.<p>
 *
 * @since 8.0.0
 */
public class CmsFormDialog extends CmsPopup {

    /** The maximum dialog width. */
    public static final int MAX_DIALOG_WIDTH = 930;

    /** The dialog width. */
    public static final int STANDARD_DIALOG_WIDTH = 700;

    /** The widget containing the form fields. */
    protected CmsForm m_form;

    /** The form handler for this dialog. */
    protected I_CmsFormHandler m_formHandler;

    /** The OK button of this dialog. */
    private CmsPushButton m_okButton;

    /** The event preview handler registration. */
    private HandlerRegistration m_previewHandlerRegistration;

    /**
     * Constructs a new form dialog with a given title.<p>
     *
     * @param title the title of the form dialog
     * @param form the form to use
     */
    public CmsFormDialog(String title, CmsForm form) {

        this(title, form, -1);
    }

    /**
     * Constructs a new form dialog with a given title.<p>
     *
     * @param title the title of the form dialog
     * @param form the form to use
     * @param dialogWidth the dialog width
     */
    public CmsFormDialog(String title, CmsForm form, int dialogWidth) {

        super(title);
        setGlassEnabled(true);
        setAutoHideEnabled(false);
        setModal(true);
        // check the available width for this dialog
        int windowWidth = Window.getClientWidth();
        if (dialogWidth > 0) {
            // reduce the dialog width if necessary
            if ((windowWidth - 50) < dialogWidth) {
                dialogWidth = windowWidth - 50;
            }
        } else {
            dialogWidth = (windowWidth - 100) > STANDARD_DIALOG_WIDTH ? windowWidth - 100 : STANDARD_DIALOG_WIDTH;
            dialogWidth = dialogWidth > MAX_DIALOG_WIDTH ? MAX_DIALOG_WIDTH : dialogWidth;
        }
        setWidth(dialogWidth);
        addButton(createCancelButton());
        m_okButton = createOkButton();
        addButton(m_okButton);
        m_form = form;

        addCloseHandler(new CloseHandler<PopupPanel>() {

            public void onClose(CloseEvent<PopupPanel> event) {

                removePreviewHandler();
            }
        });
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#center()
     */
    @Override
    public void center() {

        initContent();
        registerPreviewHandler();
        super.center();
        notifyWidgetsOfOpen();
    }

    /**
     * Gets the form of this dialog.<p>
     *
     * @return the form of this dialog
     */
    public CmsForm getForm() {

        return m_form;
    }

    /**
     * Returns the 'OK' button.<p>
     *
     * @return the 'OK' button
     */
    public CmsPushButton getOkButton() {

        return m_okButton;
    }

    /**
     * Sets the form handler for this form dialog.<p>
     *
     * @param formHandler the new form handler
     */
    public void setFormHandler(I_CmsFormHandler formHandler) {

        m_form.setFormHandler(formHandler);
    }

    /**
     * Enables/disables the OK button.<p>
     *
     * @param enabled if true, enables the OK button, else disables it
     */
    public void setOkButtonEnabled(final boolean enabled) {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            /**
             * @see com.google.gwt.core.client.Scheduler.ScheduledCommand#execute()
             */
            public void execute() {

                // The event handling of GWT gets confused if we don't execute this as a scheduled command
                getOkButton().setDown(false);
                getOkButton().setEnabled(enabled);
            }
        });

    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        initContent();
        registerPreviewHandler();
        super.show();
        notifyWidgetsOfOpen();
    }

    /**
     * Initializes the form content.<p>
     */
    protected void initContent() {

        setMainContent(m_form.getWidget());
    }

    /**
     * Called when the cancel button is clicked.
     */
    protected void onClickCancel() {

        hide();
    }

    /**
     * The method which should be called when the user clicks on the OK button of the dialog.<p>
     */
    protected void onClickOk() {

        m_form.validateAndSubmit();
    }

    /**
     * Registers the 'Enter' and 'Esc' shortcut action handler.<p>
     */
    protected void registerPreviewHandler() {

        if (m_previewHandlerRegistration == null) {
            NativePreviewHandler eventPreviewHandler = new NativePreviewHandler() {

                public void onPreviewNativeEvent(NativePreviewEvent event) {

                    Event nativeEvent = Event.as(event.getNativeEvent());
                    if (DOM.eventGetType(nativeEvent) == Event.ONKEYDOWN) {
                        int keyCode = nativeEvent.getKeyCode();
                        if (keyCode == KeyCodes.KEY_ESCAPE) {
                            onClickCancel();
                        } else if (keyCode == KeyCodes.KEY_ENTER) {
                            Element element = CmsDomUtil.getActiveElement();
                            boolean isTextarea = (element != null) && element.getTagName().equalsIgnoreCase("textarea");
                            if (!isTextarea) {
                                onClickOk();
                            }
                        }
                    }
                }
            };
            m_previewHandlerRegistration = Event.addNativePreviewHandler(eventPreviewHandler);
        }
    }

    /**
     * Removes the 'Enter' and 'Esc' shortcut action handler.<p>
     */
    protected void removePreviewHandler() {

        if (m_previewHandlerRegistration != null) {
            m_previewHandlerRegistration.removeHandler();
            m_previewHandlerRegistration = null;
        }
    }

    /**
     * Creates the cancel button.<p>
     *
     * @return the cancel button
     */
    private CmsPushButton createCancelButton() {

        addDialogClose(null);

        CmsPushButton button = new CmsPushButton();
        button.setText(Messages.get().key(Messages.GUI_CANCEL_0));
        button.setUseMinWidth(true);
        button.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                onClickCancel();

            }
        });
        return button;
    }

    /**
     * Creates the OK button.<p>
     *
     * @return the OK button
     */
    private CmsPushButton createOkButton() {

        CmsPushButton button = new CmsPushButton();
        button.setText(Messages.get().key(Messages.GUI_OK_0));
        button.setUseMinWidth(true);
        button.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                onClickOk();
            }
        });
        return button;
    }

    /**
     * Tells all widgets that the dialog has been opened.<p>
     */
    private void notifyWidgetsOfOpen() {

        for (Map.Entry<String, I_CmsFormField> fieldEntry : m_form.getFields().entrySet()) {
            fieldEntry.getValue().getWidget().setAutoHideParent(this);
        }
    }

}
