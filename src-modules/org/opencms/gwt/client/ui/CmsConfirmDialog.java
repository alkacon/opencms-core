/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsConfirmDialog.java,v $
 * Date   : $Date: 2010/04/29 09:31:56 $
 * Version: $Revision: 1.11 $
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
import org.opencms.gwt.client.rpc.CmsLog;
import org.opencms.gwt.client.util.CmsClientStringUtil;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * Provides a confirmation dialog with ok and cancel button.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.11 $
 * 
 * @since 8.0.0
 */
public class CmsConfirmDialog extends CmsAlertDialog {

    /** The action handler. */
    private I_CmsConfirmDialogHandler m_handler;

    /** The 'Ok' button. */
    private CmsPushButton m_okButton;

    /** 
     * Constructor.<p>
     */
    public CmsConfirmDialog() {

        this("", "");
    }

    /**
     * The constructor.<p>
     * 
     * @param title the title and heading of the dialog
     * @param content the content text
     */
    public CmsConfirmDialog(String title, String content) {

        super(title, content, Messages.get().key(Messages.GUI_CANCEL_0), null);
        m_okButton = new CmsPushButton();
        m_okButton.setText(Messages.get().key(Messages.GUI_OK_0));
        m_okButton.setUseMinWidth(true);
        m_okButton.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                getOkButton().setEnabled(false);
                if (getHandler() != null) {
                    try {
                        getHandler().onOk();
                    } catch (Throwable t) {
                        String message = CmsClientStringUtil.getMessage(t);
                        CmsNotification.get().send(CmsNotification.Type.WARNING, message);
                        CmsLog.log(message + "\n" + CmsClientStringUtil.getStackTrace(t, "\n"));
                    }
                }
                hide();
            }
        });
        addButton(m_okButton);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#center()
     */
    @Override
    public void center() {

        super.center();
        getOkButton().setEnabled(true);
    }

    /**
     * Sets the dialog handler.<p>
     * 
     * @param handler the handler to set
     */
    public void setHandler(I_CmsConfirmDialogHandler handler) {

        m_handler = handler;
        super.setHandler(handler);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsPopup#show()
     */
    @Override
    public void show() {

        super.show();
        getOkButton().setEnabled(true);
    }

    /**
     * @see org.opencms.gwt.client.ui.CmsAlertDialog#getHandler()
     */
    @Override
    protected I_CmsConfirmDialogHandler getHandler() {

        return m_handler;
    }

    /**
     * Returns the 'OK' button widget.<p>
     * 
     * @return the 'OK' button
     */
    protected CmsPushButton getOkButton() {

        return m_okButton;
    }
}
