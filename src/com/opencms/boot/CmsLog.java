/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsLog.java,v $
* Date   : $Date: 2003/06/13 10:56:35 $
* Version: $Revision: 1.4 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.boot;

import source.org.apache.java.io.I_Logger;
import source.org.apache.java.io.LogWriter;
import source.org.apache.java.util.Configurations;
import java.io.*;

/**
 * This class enables the OpenCms logging feature.
 * Different logging channels are supported and can be
 * (de)activated by the log settings in the proerty file.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.4 $ $Date: 2003/06/13 10:56:35 $
 */
public class CmsLog implements I_Logger {

    /** The internal m_logger */
    private LogWriter m_logger = null;

    /** Shows if this log is m_active or not */
    private boolean m_active = false;

    /** The main constructor */
    public CmsLog(String identifier, Configurations confs) {
        m_logger = null;
        try {
            m_logger = new LogWriter(identifier, confs);
            m_active = m_logger.m_active;
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes any pending messages into the log media.
     */
    public void flush() {
        if(m_logger != null) {
            m_logger.flush();
        }
    }

    /**
     * Check if this log is active.
     * @return <code>true</code> the log is active, <code>false</code> otherwise.
     */
    public boolean isActive() {
        return (m_logger != null) ? m_logger.m_active : false;
    }

    /**
     * Tells if the given channel is active.
     * @param channel the channel to test.
     * @return <code>true</code> the given channel is active, <code>false</code> otherwise.
     */
    public boolean isActive(String channel) {
        return (m_logger != null) ? m_logger.isActive(channel) : false;
    }

    /**
     * Prints the log message on the right channel.
     * <p>
     * A "channel" is a virtual log that may be enabled or disabled by
     * setting the property "identifier".channel.???=true where ??? is the
     * channel identifier that must be passed with the message.
     * If a channel is not recognized or its property is set to false
     * the message is not written.
     *
     * @param channel the channel to put the message on.
     * @param name the message to log.
     */
    public void log(String channel, String message) {
        if(m_logger != null) {
            m_logger.log(channel, message);
        }
    }

    /**
     * Prints the error message and stack trace if channel enabled.
     * @param t the error thrown.
     */
    public void log(String channel, Throwable t) {
        if(m_logger != null) {
            m_logger.log(channel, t);
        }
    }
}
