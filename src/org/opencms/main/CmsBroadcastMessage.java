/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/Attic/CmsBroadcastMessage.java,v $
 * Date   : $Date: 2005/03/04 15:11:32 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.main;

import org.opencms.file.CmsUser;

/**
 * A single broadcast message, send from one OpenCms user to another.<p>
 * 
 * To addess a message to another user, it must be placed in the
 * {@link org.opencms.main.CmsBroadcastMessageQueue} of that user.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.7.2
 */
public class CmsBroadcastMessage {

    /** The message content. */
    private String m_message;

    /** The sender of the message. */
    private CmsUser m_sender;

    /** Time the message was send. */
    private long m_sendTime;

    /**
     * Creates a new message, with the current system time set as send time.<p> 
     * 
     * @param sender the sender of the message
     * @param message the message to send
     */
    public CmsBroadcastMessage(CmsUser sender, String message) {

        this(sender, message, System.currentTimeMillis());
    }

    /**
     * Creates a new message.<p>
     * 
     * @param sender the sender of the broadcast message
     * @param message the message to send
     * @param sendTime the time the message was send
     */
    public CmsBroadcastMessage(CmsUser sender, String message, long sendTime) {

        m_sender = sender;
        m_message = message;
        m_sendTime = sendTime;
    }

    /**
     * Returns the broadcast message content.<p>
     *
     * @return the broadcast message content
     */
    public String getMessage() {

        return m_message;
    }

    /**
     * Returns the user that was the sender of this broadcast message.<p>
     *
     * @return the user that was the sender of this broadcast message
     */
    public CmsUser getUser() {

        return m_sender;
    }

    /**
     * Returns the time this broadcast message was send.<p>
     *
     * @return the time this broadcast message was send
     */
    public long getSendTime() {

        return m_sendTime;
    }
}