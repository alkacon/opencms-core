/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/Attic/CmsBroadcastMessageQueue.java,v $
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

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.BoundedFifoBuffer;

/**
 * Holds all incoming messages for one OpenCms user.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.1 $
 * 
 * @since 5.7.2
 */
public class CmsBroadcastMessageQueue {

    /** Maximum size of the broadcast message queue for one user. */
    public static final int C_MESSAGE_QUEUE_SIZE = 10;

    /** The buffer that holds all broadcast messages. */
    private Buffer m_messages;

    /** The owner (reciever) of the broadcast messages. */
    private CmsUser m_user;

    /**
     * Creates a new broadcast message queue for the given user.<p>
     * 
     * @param user the user to create the broadcast message queue for
     */
    public CmsBroadcastMessageQueue(CmsUser user) {

        m_messages = BufferUtils.synchronizedBuffer(new BoundedFifoBuffer(C_MESSAGE_QUEUE_SIZE));
        m_user = user;
    }

    /**
     * Adds the given message to this broadcast message queue.<p>
     * 
     * @param message the message to add
     */
    public void addBroadcastMessage(CmsBroadcastMessage message) {

        m_messages.add(message);
    }

    /**
     * Returns the next broadcast message from this message queue, or <code>null</code>
     * if the queue is empty.<p>
     * 
     * @return the next broadcast message from this message queue
     */
    public CmsBroadcastMessage getNextBroadcastMessage() {

        if (m_messages.size() <= 0) {
            return null;
        }

        return (CmsBroadcastMessage)m_messages.remove();
    }

    /**
     * Returns the user that owns this broadcast message queue.<p>
     *
     * @return the user that owns this broadcast message queue
     */
    public CmsUser getUser() {

        return m_user;
    }

    /**
     * Returns <code>true</code> if this queue contains one or more broadcast messages.<p>
     * 
     * @return <code>true</code> if this queue contains one or more broadcast messages
     */
    public boolean hasBroadcastMessagesPending() {

        return m_messages.size() > 0;
    }

    /**
     * Returns the size of this broadcast message queue.<p>
     * 
     * @return the size of this broadcast message queue
     */
    public int size() {

        return m_messages.size();
    }
}