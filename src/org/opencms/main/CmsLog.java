/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsLog.java,v $
 * Date   : $Date: 2003/09/16 12:06:10 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Provides the OpenCms logging mechanism.<p>
 * 
 * Different logging channels are supported and can be
 * activated by the log settings in the property file.
 * For every log channel a log level is supported.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.1 $
 */
public class CmsLog {
    
    /** Critical messages that stop further processing */
    public static final String C_OPENCMS_CRITICAL = "org.opencms";

    /** Debugging messages */
    public static final String C_OPENCMS_DEBUG = "org.opencms";

    /** Informational messages */
    public static final String C_OPENCMS_INFO = "org.opencms";

    /** Initialization messages */
    public static final String C_OPENCMS_INIT = "org.opencms";



    /** Messages of the OpenCms Scheduler */
    public static final String CHANNEL_CRON = "org.opencms.cron";

    /** Flex loader messages */
    public static final String CHANNEL_FLEX = "org.opencms.flex";

    /** Informational messages */
    public static final String CHANNEL_MODULE = "org.opencms.module";
    
    /** Messages of the static export */
    public static final String CHANNEL_STATICEXPORT = "org.opencms.staticexport";

    /** Messages of the workplace */
    public static final String CHANNEL_WORKPLACE = "org.opencms.workplace"; 
    
    /** Messages of the (legacy) XML workplace */
    public static final String CHANNEL_WORKPLACE_XML = "org.opencms.workplace.xml"; 

    /** Messages of the new OpenCms element cache */
    public static final String CHANNEL_XMLTEMPLATE = "org.opencms.template.xml";        

    /** Debug log level (2 - between trace and info) */
    public static final int LEVEL_DEBUG = 2;

    /** Error log level (5 - between warn and fatal) */
    public static final int LEVEL_ERROR = 5;

    /** Fatal log level (6 - the least verbost level) */
    public static final int LEVEL_FATAL = 6;

    /** Info log level (2 - between debug and warn) */
    public static final int LEVEL_INFO  = 3;
    
    /** Trace log level (1 - the most verbose level) */
    public static final int LEVEL_TRACE = 1;

    /** Warn log level (4 - between info and error) */
    public static final int LEVEL_WARN  = 4;

    /** Map that contains the different loggers */
    private Map m_loggers;

    /**
     * Creates a new OpenCms logger.<p>
     */
    public CmsLog() {
        m_loggers = new HashMap();
    }
    
    /**
     * Returns the log for the selected channel.<p>
     *  
     * @param channel the channel to look up
     * @return the log for the selected channel
     */
    private Log getLogger(String channel) {
        Object log = m_loggers.get(channel);
        if (log == null) {
            log = LogFactory.getLog(channel);
            m_loggers.put(channel, log);
        }
        return (Log)log;
    }    
    
    /**
     * Checks if a log channel is active for the selected level.<p>
     * 
     * @param channel the channel to log the message on
     * @param level the log level to use
     * @return <code>true</code> the given channel with the given level is active, <code>false</code> otherwise.
     */
    public boolean isActive(String channel, int level) {
        Log log = getLogger(channel);
        if (log == null) { 
            return false;
        }
        switch (level) {
            case LEVEL_TRACE:
                return log.isTraceEnabled();
            case LEVEL_DEBUG:
                return log.isDebugEnabled();
            case LEVEL_INFO:
                return log.isInfoEnabled();
            case LEVEL_WARN:
                return log.isWarnEnabled();
            case LEVEL_ERROR:
                return log.isErrorEnabled();
            case LEVEL_FATAL:
            default:       
                return log.isFatalEnabled();
        }                  
    }

    /**
     * Prints a message on the selected channel for the selected level.<p>
     * 
     * @param channel the channel to log the message on
     * @param level the log level to use
     * @param message the message to log
     */
    public void log(String channel, int level, String message) {
        Log log = getLogger(channel);
        if (log == null) { 
            return;
        }
        switch (level) {
            case LEVEL_TRACE:
                log.trace(message);
            case LEVEL_DEBUG:
                log.debug(message);
            case LEVEL_INFO:
                log.info(message);
            case LEVEL_WARN:
                log.warn(message);
            case LEVEL_ERROR:
                log.error(message);
            case LEVEL_FATAL:
            default:
                log.fatal(message);
        } 
    }
    
    /**
     * Prints a message and a Throwable on the selected channel for the selected level.<p>
     * 
     * @param channel the channel to log the message on
     * @param level the log level to use
     * @param message the message to log
     * @param throwable the Throwable to log
     */
    public void log(String channel, int level, String message, Throwable throwable) {
        Log log = getLogger(channel);
        if (log == null) { 
            return;
        }
        switch (level) {
            case LEVEL_TRACE:
                log.trace(message, throwable);
            case LEVEL_DEBUG:
                log.debug(message, throwable);
            case LEVEL_INFO:
                log.info(message, throwable);
            case LEVEL_WARN:
                log.warn(message, throwable);
            case LEVEL_ERROR:
                log.error(message, throwable);
            case LEVEL_FATAL:
            default:
                log.fatal(message, throwable);             
        } 
    }
}
