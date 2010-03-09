/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/rpc/Attic/CmsLog.java,v $
 * Date   : $Date: 2010/03/09 10:32:17 $
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

package org.opencms.gwt.client.rpc;

import org.opencms.gwt.shared.rpc.I_CmsLogService;
import org.opencms.gwt.shared.rpc.I_CmsLogServiceAsync;

import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Handles client side logging.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 * 
 * @see org.opencms.gwt.CmsLogService
 * @see org.opencms.gwt.shared.rpc.I_CmsLogService
 * @see org.opencms.gwt.shared.rpc.I_CmsLogServiceAsync
 */
public final class CmsLog {

    /** The service instance. */
    private static I_CmsLogServiceAsync m_loggingService;

    /**
     * Prevent instantiation.<p>
     */
    private CmsLog() {

        // Prevent instantiation
    }

    /**
     * Logs client messages on the server.<p>
     * 
     * @param message the message to log
     * 
     * @return the generated ticket
     */
    public static String log(final String message) {

        final String ticket = String.valueOf(new Date().getTime());
        // using a deferred command just to be more responsible 
        // since we do not expect any feed back from it
        DeferredCommand.addCommand(new Command() {

            /**
             * @see com.google.gwt.user.client.Command#execute()
             */
            public void execute() {

                getLoggingService().log(ticket, message, new AsyncCallback<Void>() {

                    /**
                     * @see com.google.gwt.user.client.rpc.AsyncCallback#onFailure(java.lang.Throwable)
                     */
                    public void onFailure(Throwable caught) {

                        // logging failed, really bad
                        // TODO: show a dialog box
                        Window.alert("failure:" + ticket);
                    }

                    /**
                     * @see com.google.gwt.user.client.rpc.AsyncCallback#onSuccess(Object)
                     */
                    public void onSuccess(Void result) {

                        // logged successfully
                    }
                });
            }
        });
        return ticket;
    }

    /**
     * Returns the service instance, using lazy initialization.<p>
     * 
     * @return the service instance
     */
    protected static I_CmsLogServiceAsync getLoggingService() {

        if (m_loggingService == null) {
            m_loggingService = GWT.create(I_CmsLogService.class);
        }
        return (m_loggingService);
    }
}