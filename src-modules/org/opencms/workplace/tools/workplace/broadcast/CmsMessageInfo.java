/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/workplace/broadcast/CmsMessageInfo.java,v $
 * Date   : $Date: 2005/06/15 16:01:31 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.workplace.tools.workplace.broadcast;

/**
 * Bean class for message information.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com)
 * @version $Revision: 1.1 $
 * @since 5.7.3
 */
public class CmsMessageInfo {

    /** From header string. */
    private String m_from;

    /** Message string. */
    private String m_msg;

    /** To header string. */
    private String m_to;

    /**
     * Default Constructor.<p> 
     */
    public CmsMessageInfo() {

        // noop
    }

    /**
     * Returns the from string.<p>
     * 
     * @return the from string
     */
    public String getFrom() {

        return m_from;
    }

    /**
     * Returns the message string.<p>
     * 
     * @return the message string
     */
    public String getMsg() {

        return m_msg;
    }

    /**
     * Returns the to string.<p>
     * 
     * @return the to string
     */
    public String getTo() {

        return m_to;
    }

    /**
     * Sets the from string.<p>
     * 
     * @param from the from string
     */
    public void setFrom(String from) {

        m_from = from;
    }

    /**
     * Sets the message string.<p>
     * 
     * @param msg the message string
     */
    public void setMsg(String msg) {

        m_msg = msg;
    }

    /**
     * Sets the to string.<p>
     * 
     * @param to the to string
     */
    public void setTo(String to) {

        m_to = to;
    }
}
