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

package org.opencms.ugc.client;

import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsNotification.Type;
import org.opencms.gwt.client.ui.CmsNotificationMessage;
import org.opencms.ugc.client.export.I_CmsBooleanCallback;

/**
 * Keeps track of currently running requests for the purpose of enabling or disabling a waiting indicator.<p>
 */
public class CmsRequestCounter {

    /** Number of active requests. */
    private int m_counter;

    /** The callback used to enable or disable the wait indicator. */
    private I_CmsBooleanCallback m_callback = new I_CmsBooleanCallback() {

        private CmsNotificationMessage m_message;

        /**
         * @see org.opencms.ugc.client.export.I_CmsBooleanCallback#call(boolean)
         */
        public void call(boolean b) {

            if (b) {
                // check if the message is already showing
                if (m_message == null) {
                    m_message = CmsNotification.get().sendBusy(Type.NORMAL, "Loading");
                }
            } else if (m_message != null) {
                CmsNotification.get().removeMessage(m_message);
                m_message = null;
            }
        }
    };

    /**
     * Decrements the request counter.<p>
     */
    public void decrement() {

        m_counter -= 1;
        if (m_counter < 0) {
            m_counter = 0;
        }
        if (m_counter == 0) {
            m_callback.call(false);
        }
    }

    /**
     * Increments the request counter.<p>
     */
    public void increment() {

        m_counter += 1;
        m_callback.call(true);
    }

    /**
     * Sets the callback.<p>
     *
     * @param callback the callback
     */
    public void setCallback(I_CmsBooleanCallback callback) {

        m_callback = callback;
    }

}
