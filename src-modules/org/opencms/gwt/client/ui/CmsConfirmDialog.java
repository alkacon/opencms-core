/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsConfirmDialog.java,v $
 * Date   : $Date: 2010/03/11 08:07:18 $
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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

/**
 * Provides a confirmation dialog with ok and cancel button.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsConfirmDialog extends CmsPopupDialog {

    /**
     * Click handler for dialog buttons.<p>
     */
    protected class ButtonClickHandler implements ClickHandler {

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            getOkButton().setEnabled(false);
            getCancelButton().setEnabled(false);
            if (getHandler() != null) {
                if (event.getSource().equals(getOkButton())) {
                    getHandler().onOk();
                } else {
                    getHandler().onCancel();
                }
            }
            hide();
        }

    }

    /** The 'Cancel' button. */
    private CmsTextButton m_cancelButton;

    private Label m_content;

    private I_CmsConfirmDialogHandler m_handler;

    /** The 'Ok' button. */
    private CmsTextButton m_okButton;

    /** 
     * Constructor.<p>
     */
    public CmsConfirmDialog() {

        super();
        ClickHandler clickHandler = new ButtonClickHandler();
        // TODO: add localized messages 

        m_okButton = new CmsTextButton(Messages.get().key(Messages.GUI_OK_0), null);
        m_okButton.useMinWidth(true);
        m_okButton.addClickHandler(clickHandler);
        addButton(m_okButton);
        m_cancelButton = new CmsTextButton(Messages.get().key(Messages.GUI_CANCEL_0), null);
        m_cancelButton.useMinWidth(true);
        m_cancelButton.addClickHandler(clickHandler);
        addButton(m_cancelButton);
    }

    /**
     * The constructor.<p>
     * 
     * @param title the title and heading of the dialog
     * @param content the content text
     */
    public CmsConfirmDialog(String title, String content) {

        this();
        m_content = new Label(content);
        this.setContent(m_content);
        getDialog().setText(title);
    }

    /**
     * Adds a secondary style name to the content widget.<p>
     * 
     * @param style the style name to add
     * 
     * @see com.google.gwt.user.client.ui.UIObject#addStyleName(java.lang.String)
     */
    public void addContentStyleName(String style) {

        m_content.addStyleName(style);
    }

    /**
     * Gets all of the content's style names, as a space-separated list.<p>
     * 
     * @return the style names
     * 
     * @see com.google.gwt.user.client.ui.UIObject#getStyleName()
     */
    public String getContentStyleName() {

        return m_content.getStyleName();
    }

    /**
     * Gets all of the content's primary style name.<p>
     * 
     * @return the primary style name
     * 
     * @see com.google.gwt.user.client.ui.UIObject#getStylePrimaryName()
     */
    public String getContentStylePrimaryName() {

        return m_content.getStylePrimaryName();
    }

    /**
     * Clears all of the content's style names and sets it to the given style.<p>
     * 
     * @param style the style name to set
     * 
     * @see com.google.gwt.user.client.ui.UIObject#setStyleName(java.lang.String)
     */
    public void setContentStyleName(String style) {

        m_content.setStyleName(style);
    }

    /**
     * Sets the primary style name of the content widget.<p>
     * 
     * @param style the style name to set
     * 
     * @see com.google.gwt.user.client.ui.UIObject#setStylePrimaryName(java.lang.String)
     */
    public void setContentStylePrimaryName(String style) {

        m_content.setStylePrimaryName(style);
    }

    /**
     * Sets the dialog handler.<p>
     * 
     * @param handler the handler to set
     */
    public void setHandler(I_CmsConfirmDialogHandler handler) {

        m_handler = handler;
    }

    /**
     * Returns the 'Cancel' button widget.<p>
     * 
     * @return the 'Cancel' button
     */
    protected CmsTextButton getCancelButton() {

        return m_cancelButton;
    }

    /**
     * Returns the dialog handler.<p>
     * 
     * @return the dialog handler
     */
    protected I_CmsConfirmDialogHandler getHandler() {

        return m_handler;
    }

    /**
     * Returns the 'OK' button widget.<p>
     * 
     * @return the 'OK' button
     */
    protected CmsTextButton getOkButton() {

        return m_okButton;
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#center()
     */
    @Override
    public void center() {

        super.center();
        getOkButton().setEnabled(true);
        getCancelButton().setEnabled(true);

    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        super.show();
        getOkButton().setEnabled(true);
        getCancelButton().setEnabled(true);
    }

}
