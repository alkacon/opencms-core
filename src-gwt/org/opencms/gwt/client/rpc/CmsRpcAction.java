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

package org.opencms.gwt.client.rpc;

import org.opencms.gwt.CmsRpcException;
import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.CmsErrorDialog;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.util.CmsClientStringUtil;

import com.google.gwt.event.shared.UmbrellaException;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Consistently manages RPCs errors and 'loading' state.<p>
 * 
 * @param <T> The type of the expected return value
 * 
 * @since 8.0
 */
public abstract class CmsRpcAction<T> implements AsyncCallback<T> {

    /** The message displayed when loading. */
    private String m_loadingMessage = Messages.get().key(Messages.GUI_LOADING_0);

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

        String message;
        StackTraceElement[] trace;
        if (t instanceof CmsRpcException) {
            CmsRpcException ex = (CmsRpcException)t;
            message = ex.getOriginalMessage();
            trace = ex.getOriginalStackTrace();
        } else {
            message = CmsClientStringUtil.getMessage(t);
            trace = t.getStackTrace();
        }
        // send the ticket to the server
        String ticket = CmsLog.log(message + "\n" + CmsClientStringUtil.getStackTraceAsString(trace, "\n"));

        // remove the overlay
        stop(false);
        provideFeedback(ticket, t);
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
        } catch (RuntimeException error) {
            onFailure(error);
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
        CmsNotification.get().hide();
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
     * Provides some feedback to the user in case of failure.<p>
     * 
     * @param ticket the generated ticket
     * @param throwable the thrown error
     */
    protected void provideFeedback(String ticket, Throwable throwable) {

        String message;
        String cause = null;
        String className;
        StackTraceElement[] trace;
        if (throwable instanceof CmsRpcException) {
            CmsRpcException ex = (CmsRpcException)throwable;
            message = ex.getOriginalMessage();
            cause = ex.getOriginalCauseMessage();
            className = ex.getOriginalClassName();
            trace = ex.getOriginalStackTrace();
        } else {
            message = CmsClientStringUtil.getMessage(throwable);
            if (throwable.getCause() != null) {
                cause = CmsClientStringUtil.getMessage(throwable.getCause());
            }
            className = throwable.getClass().getName();
            trace = throwable.getStackTrace();
        }

        String lineBreak = "<br />\n";
        String errorMessage = message == null
        ? className + ": " + Messages.get().key(Messages.GUI_NO_DESCIPTION_0)
        : message;
        if (cause != null) {
            errorMessage += lineBreak + Messages.get().key(Messages.GUI_REASON_0) + ":" + cause;
        }

        String details = Messages.get().key(Messages.GUI_TICKET_MESSAGE_3, ticket, className, message)
            + CmsClientStringUtil.getStackTraceAsString(trace, lineBreak);
        new CmsErrorDialog(errorMessage, details).center();
    }

    /**
     * Shows the 'loading message'.<p>
     * 
     * Overwrite to customize the message.<p>
     * 
     * @param blocking shows an blocking overlay if <code>true</code> 
     */
    protected void show(boolean blocking) {

        if (blocking) {
            CmsNotification.get().sendBlocking(CmsNotification.Type.NORMAL, m_loadingMessage);
        } else {
            CmsNotification.get().sendSticky(CmsNotification.Type.NORMAL, m_loadingMessage);
        }
    }
}
