/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsAlertDialog.java,v $
 * Date   : $Date: 2010/11/15 15:34:21 $
 * Version: $Revision: 1.4 $
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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;

/**
 * Provides an alert dialog with a button.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsAlertDialog extends CmsPopupDialog {

    /** The 'close' button. */
    private CmsPushButton m_closeButton;

    /** The content text. */
    private HTML m_content;

    /** The action handler. */
    private I_CmsCloseDialogHandler m_handler;

    /** 
     * Constructor.<p>
     */
    public CmsAlertDialog() {

        this("", "");
    }

    /**
     * Constructor.<p>
     * 
     * @param title the title and heading of the dialog
     * @param content the content text
     */
    public CmsAlertDialog(String title, String content) {

        this(title, content, Messages.get().key(Messages.GUI_CLOSE_0));
    }

    /**
     * Constructor.<p>
     * 
     * @param title the title and heading of the dialog
     * @param content the content text
     * @param buttonText the button text
     */
    public CmsAlertDialog(String title, String content, String buttonText) {

        this(title, content, buttonText, null);
    }

    /**
     * Constructor.<p>
     * 
     * @param title the title and heading of the dialog
     * @param content the content text
     * @param buttonText the button text
     * @param buttonIconClass the button icon class
     */
    public CmsAlertDialog(String title, String content, String buttonText, String buttonIconClass) {

        super();
        super.setAutoHideEnabled(false);
        super.setModal(true);
        setGlassEnabled(true);
        m_closeButton = new CmsPushButton();
        m_closeButton.setText(buttonText);
        m_closeButton.setImageClass(buttonIconClass);
        m_closeButton.setUseMinWidth(true);
        m_closeButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                onClose();
            }
        });
        addButton(m_closeButton);
        m_content = new HTML(content);
        setContent(m_content);
        m_content.addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().contentSpacer());
        setText(title);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#center()
     */
    @Override
    public void center() {

        super.center();
        getCloseButton().setEnabled(true);

    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#setAutoHideEnabled(boolean)
     */
    @Override
    public void setAutoHideEnabled(boolean autoHide) {

        // prevent enabling auto hide
    }

    /**
     * Sets the cancel/close button icon class.<p>
     * 
     * @param iconClass the icon class
     */
    public void setCloseIconClass(String iconClass) {

        getCloseButton().setImageClass(iconClass);
    }

    /**
     * Sets the close button text.<p>
     * 
     * @param text the button text
     */
    public void setCloseText(String text) {

        m_closeButton.setText(text);
    }

    /**
     * Sets the dialog handler.<p>
     * 
     * @param handler the handler to set
     */
    public void setHandler(I_CmsCloseDialogHandler handler) {

        m_handler = handler;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#setModal(boolean)
     */
    @Override
    public void setModal(boolean modal) {

        // it is always modal
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        super.show();
        getCloseButton().setEnabled(true);
    }

    /**
     * Returns the button widget.<p>
     * 
     * @return the button
     */
    protected CmsPushButton getCloseButton() {

        return m_closeButton;
    }

    /**
     * Returns the dialog handler.<p>
     * 
     * @return the dialog handler
     */
    protected I_CmsCloseDialogHandler getHandler() {

        return m_handler;
    }

    /**
     * Executed on 'close' click. <p>
     */
    protected void onClose() {

        getCloseButton().setEnabled(false);
        if (getHandler() != null) {
            getHandler().onClose();
        }
        hide();
    }
}
