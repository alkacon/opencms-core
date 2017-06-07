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

package org.opencms.gwt.client.rpc;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotificationMessage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.StatusCodeException;

/**
 * Consistently manages RPCs errors and 'loading' state.<p>
 *
 * @param <T> The type of the expected return value
 *
 * @since 8.0
 */
public abstract class CmsRpcAction<T> implements AsyncCallback<T> {

    /** The sync token value, used to allow synchronous RPC calls within vaadin, see also com.google.gwt.http.client.RequestBuilder within the super source */
    public static final String SYNC_TOKEN = "this_is_a_synchronous_rpc_call";

    /** The message displayed when loading. */
    private String m_loadingMessage;

    /** The current notification. */
    private CmsNotificationMessage m_notification;

    /** The result, used only for synchronized request. */
    private T m_result;

    /** The timer to control the display of the 'loading' state, if the action takes too long. */
    private Timer m_timer;

    /**
     * Executes the current RPC call.<p>
     *
     * Initializes client-server communication and will
     */
    public abstract void execute();

    /**
     * Executes a synchronized request.<p>
     *
     * @return the RPC result
     *
     * @see #execute()
     */
    public T executeSync() {

        execute();
        return m_result;
    }

    /**
     * Handle errors.<p>
     *
     * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
     */
    public void onFailure(Throwable t) {

        if ((t instanceof StatusCodeException) && (((StatusCodeException)t).getStatusCode() == 0)) {
            // a status code 0 indicates the client aborted the request, most likely when leaving the page, this should be ignored
            return;
        } else if ((t instanceof StatusCodeException) && (((StatusCodeException)t).getStatusCode() == 500)) {
            // a server error 500 most likely indicates an expired session and there for insufficient user rights to access any GWT service
            CmsErrorDialog dialog = new CmsErrorDialog(Messages.get().key(Messages.GUI_SESSION_EXPIRED_0), null);
            dialog.center();
        } else {
            CmsErrorDialog.handleException(t);
        }
        // remove the overlay
        stop(false);
    }

    /**
     * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(java.lang.Object)
     */
    public void onSuccess(T value) {

        try {
            m_result = value;
            onResponse(value);
        } catch (UmbrellaException exception) {
            Throwable wrappedException = exception.getCauses().iterator().next();
            onFailure(wrappedException);
            if (!GWT.isProdMode()) {
                throw exception;
            }
        } catch (RuntimeException error) {
            onFailure(error);
            if (!GWT.isProdMode()) {
                throw error;
            }
        }
    }

    /**
     * Sets the loading message.<p>
     *
     * @param loadingMessage the loading message to set
     */
    public void setLoadingMessage(String loadingMessage) {

        m_loadingMessage = loadingMessage;
    }

    /**
     * Starts the timer for showing the 'loading' state.<p>
     *
     * Note: Has to be called manually before calling the RPC service.<p>
     *
     * @param delay the delay in milliseconds
     * @param blocking shows an blocking overlay if <code>true</code>
     */
    public void start(int delay, final boolean blocking) {

        if (delay <= 0) {
            show(blocking);
            return;
        }
        m_timer = new Timer() {

            /**
             * @see com.google.gwt.user.client.Timer#run()
             */
            @Override
            public void run() {

                show(blocking);
            }
        };
        m_timer.schedule(delay);
    }

    /**
     * Stops the timer.<p>
     *
     * Note: Has to be called manually on success.<p>
     *
     * @param displayDone <code>true</code> if you want to tell the user that the operation was successful
     */
    public void stop(boolean displayDone) {

        if (m_timer != null) {
            m_timer.cancel();
            m_timer = null;
        }
        if (m_notification != null) {
            CmsNotification.get().removeMessage(m_notification);
            m_notification = null;
        }
        if (displayDone) {
            CmsNotification.get().send(CmsNotification.Type.NORMAL, Messages.get().key(Messages.GUI_DONE_0));
        }
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
     *
     * Overwrite to customize the message.<p>
     *
     * @param blocking shows an blocking overlay if <code>true</code>
     */
    protected void show(boolean blocking) {

        if (blocking) {
            m_notification = CmsNotification.get().sendBusy(CmsNotification.Type.NORMAL, m_loadingMessage);
        } else {
            m_notification = CmsNotification.get().sendSticky(CmsNotification.Type.NORMAL, m_loadingMessage);
        }
    }
}
