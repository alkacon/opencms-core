/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsBroadcast.java,v $
 * Date   : $Date: 2005/06/23 11:11:38 $
 * Version: $Revision: 1.7 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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
 * To addess a broadcast to another user, it must be placed in the
 * broadcast queue of that user using for example 
 * {@link org.opencms.main.CmsSessionManager#sendBroadcast(org.opencms.file.CmsObject, String, CmsUser)}.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.7 $ 
 * 
 * @since 6.0.0 
 */
public class CmsBroadcast {

    /** The broadcast content. */
    private String m_message;

    /** The sender of the broadcast. */
    private CmsUser m_sender;

    /** Time the broadcast was send. */
    private long m_sendTime;

    /**
     * Creates a new broadcast, with the current system time set as send time.<p> 
     * 
     * @param sender the sender of the broadcast
     * @param message the message to send
     */
    public CmsBroadcast(CmsUser sender, String message) {

        m_sender = sender;
        m_message = message;
        m_sendTime = System.currentTimeMillis();
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
     * Returns the time this broadcast was send.<p>
     *
     * @return the time this broadcast was send
     */
    public long getSendTime() {

        return m_sendTime;
    }

    /**
     * Returns the user that was the sender of this broadcast.<p>
     *
     * @return the user that was the sender of this broadcast
     */
    public CmsUser getUser() {

        return m_sender;
    }
}