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

package org.opencms.gwt.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Contains the broadcast message data.<p>
 *
 * @since 9.5.0
 */
public class CmsBroadcastMessage implements IsSerializable {

    /** The user. */
    private String m_user;

    /** The date. */
    private String m_time;

    /** The message. */
    private String m_message;

    /**
     * Constructor.<p>
     *
     * @param user the user
     * @param time the time
     * @param message the message
     */
    public CmsBroadcastMessage(String user, String time, String message) {

        m_user = user;
        m_time = time;
        m_message = message;
    }

    /**
     * Constructor.<p>
     */
    protected CmsBroadcastMessage() {

        // for serialization only
    }

    /**
     * Returns the message.<p>
     *
     * @return the message
     */
    public String getMessage() {

        return m_message;
    }

    /**
     * Returns the time.<p>
     *
     * @return the time
     */
    public String getTime() {

        return m_time;
    }

    /**
     * Returns the user.<p>
     *
     * @return the user
     */
    public String getUser() {

        return m_user;
    }
}
