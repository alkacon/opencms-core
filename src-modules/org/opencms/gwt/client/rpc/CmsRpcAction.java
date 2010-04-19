/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/rpc/Attic/CmsRpcAction.java,v $
 * Date   : $Date: 2010/04/19 11:47:45 $
 * Version: $Revision: 1.9 $
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

package org.opencms.gwt.client.rpc;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsConfirmDialog;
import org.opencms.gwt.client.ui.CmsPopupDialog;
import org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.util.CmsClientStringUtil;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

/**
 * Consistently manages RPCs errors and 'loading' state.<p>
 * 
 * @param <T> The type of the expected return value
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.9 $ 
 * 
 * @since 8.0
 */
public abstract class CmsRpcAction<T> implements AsyncCallback<T> {

    /** Nice overlay showing the 'loading' message. */
    private CmsPopupDialog m_loadingDialog;

    /** The timer to control the display of the 'loading' state, if the action takes too long. */
    private Timer m_timer;

    /**
     * Executes the current RPC call.<p>
     * 
     * Initializes client-server communication and will
     */
    public abstract void execute();

    /**
     * Handle errors.<p>
     * 
     * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
     */
    public void onFailure(Throwable t) {

        // send the ticket to the server
        String message = CmsClientStringUtil.getMessage(t);
        String ticket = CmsLog.log(message + "\n" + CmsClientStringUtil.getStackTrace(t, "\n"));

        // remove the nice overlay
        stop();

        // give feedback
        provideFeedback(ticket, message);
    }

    /**
     * Provides some feedback to the user in case of failure.<p>
     * 
     * @param ticket the generated ticket
     * @param message the error message
     */
    protected void provideFeedback(String ticket, String message) {

        String title = Messages.get().key(Messages.GUI_ERROR_0);
        String text = Messages.get().key(Messages.GUI_TICKET_MESSAGE_2, message, ticket);

        CmsConfirmDialog dialog = new CmsConfirmDialog(title, text);
        dialog.center();
        dialog.setHandler(new I_CmsConfirmDialogHandler() {

            /**
             * @see org.opencms.gwt.client.ui.I_CmsConfirmDialogHandler#onOk()
             */
            public void onOk() {

                execute();
            }

            /**
             * @see org.opencms.gwt.client.ui.I_CmsCloseDialogHandler#onClose()
             */
            public void onClose() {

                // do nothing
            }
        });
    }

    /**
     * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
     */
    public void onSuccess(T value) {

        try {
            onResponse(value);
        } catch (RuntimeException error) {
            onFailure(error);
        }
    }

    /**
     * Starts the timer for showing the 'loading' state.<p>
     * 
     * Note: Has to be called manually before calling the RPC service.<p>
     * 
     * @param delay the delay in milliseconds
     */
    public void start(int delay) {

        if (delay <= 0) {
            show();
            return;
        }
        m_timer = new Timer() {

            /**
             * @see com.google.gwt.user.client.Timer#run()
             */
            @Override
            public void run() {

                show();
            }
        };
        m_timer.schedule(delay);
    }

    /**
     * Stops the timer.<p>
     * 
     * Note: Has to be called manually on success.<p>
     */
    public void stop() {

        if (m_timer != null) {
            m_timer.cancel();
            m_timer = null;
        }
        hide();
    }

    /**
     * Creates a new dialog with the given title and text.<p>
     * 
     * @param title the dialog title
     * @param text the dialog text
     * 
     * @return a new dialog, already visible
     */
    protected CmsPopupDialog getDialog(String title, String text) {

        CmsPopupDialog dialog = new CmsPopupDialog(title, new Label(text));
        dialog.getContent().addStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupMainContent());
        dialog.setGlassEnabled(true);
        dialog.setAutoHideEnabled(true);
        dialog.setModal(true);
        dialog.center();
        return dialog;
    }

    /**
     * Hides the 'loading message'.<p>
     */
    protected void hide() {

        if ((m_loadingDialog == null) || !m_loadingDialog.isVisible()) {
            return;
        }
        // remove nice overlay
        m_loadingDialog.hide();
    }

    /**
     * Handles the result when received from server.<p>
     * 
     * @param result the result from server
     * 
     * @see AsyncCallback#onSuccess(Object)
     */
    protected abstract void onResponse(T result);

    /**
     * Shows the 'loading message'.<p>
     */
    protected void show() {

        if ((m_loadingDialog != null) && m_loadingDialog.isVisible()) {
            return;
        }
        // show nice overlay
        if (m_loadingDialog == null) {
            m_loadingDialog = getDialog(Messages.get().key(Messages.GUI_LOADING_0), Messages.get().key(
                Messages.GUI_PLEASE_WAIT_0));
            m_loadingDialog.setAutoHideEnabled(false);
        } else {
            m_loadingDialog.show();
        }
    }
}
