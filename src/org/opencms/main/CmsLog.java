/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/main/CmsLog.java,v $
 * Date   : $Date: 2004/06/14 14:25:56 $
 * Version: $Revision: 1.14 $
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

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;

/**
 * Provides the OpenCms logging mechanism.<p>
 * 
 * Different logging channels are supported and can be
 * activated by the log settings in the property file.
 * For every log channel a log level is supported.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.14 $
 */
public class CmsLog implements Log {

    /** Initialization messages. */
    public static final String CHANNEL_INIT = "org.opencms.init";    
    
    /** The path to the loggers configuration file. */
    private String m_configFile;
    
    /**
     * Creates a new OpenCms logger.<p>
     */
    protected CmsLog() {
        // check for "OpenCmsLog" system variable (used for testing)
        String openCmsLog = System.getProperty("OpenCmsLog");
        if (openCmsLog != null) {
            m_configFile = openCmsLog;
        }
    }

    /**
     * Returns true if the log already has been initialitzed.<p>
     * 
     * @return true if the log already has been initialitzed
     */
    protected boolean isInitialized() {
        return (m_configFile != null); 
    }
    
    /**
     * Initializes the OpenCms logger.<p>
     * 
     * @param configuration the OpenCms configuration
     * @param configFile the path to the logger configuration file 
     */    
    protected void init(ExtendedProperties configuration, String configFile) {
        m_configFile = configFile;
        // clean up previously initialized loggers
        LogFactory.releaseAll();
        try {
            // set "opencms.log" variable 
            System.setProperty("opencms.log", configuration.getString("log.file"));
            // set property values for log4j configuration, will be ignored if log4j is not used
            String log4jDebug = configuration.getString("log.log4j.debug");
            if (log4jDebug != null) {
                // enable log4j debug output 
                System.setProperty("log4j.debug", log4jDebug);
            }
            String log4jPath = configuration.getString("log.log4j.configuration");
            if (log4jPath != null) {               
                // set the log4j configuration path
                log4jPath = log4jPath.trim();
                if ("this".equalsIgnoreCase(log4jPath)) {
                    log4jPath = m_configFile;                                        
                }
                System.setProperty("log4j.configuration", "file:" + log4jPath);
                // Required for Tomcat5, or else log4j will not properly initialize
                PropertyConfigurator.configure(log4jPath);
            }
        } catch (SecurityException e) {
            // ignore, in this case log settings must be provided by environment or servlet context
        }           
    }
    
    /**
     * Returns the log for the selected object.<p>
     * 
     * If the provided object is a String, this String will
     * be used as channel name. Otherwise the objects 
     * class name will be used as channel name.<p>
     *  
     * @param obj the object channel to use
     * @return the log for the selected object channel
     */
    protected Log getLogger(Object obj) {
        if (obj instanceof String) {
            return LogFactory.getLog((String)obj);
        } else if (obj instanceof Class) {
            return LogFactory.getLog((Class)obj);
        } else {
            return LogFactory.getLog(obj.getClass());
        }
    }

    /**
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return false;
    }

    /**
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#isFatalEnabled()
     */
    public boolean isFatalEnabled() {
        return true;
    }

    /**
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return false;
    }

    /**
     * @see org.apache.commons.logging.Log#isTraceEnabled()
     */
    public boolean isTraceEnabled() {
        return false;
    }

    /**
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return false;
    }

    /**
     * @see org.apache.commons.logging.Log#trace(java.lang.Object)
     */
    public void trace(Object message) {
        doLog(message, null);
    }

    /**
     * @see org.apache.commons.logging.Log#trace(java.lang.Object, java.lang.Throwable)
     */
    public void trace(Object message, Throwable t) {
        doLog(message, t);
    }

    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object)
     */
    public void debug(Object message) {
        doLog(message, null);
    }

    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
     */
    public void debug(Object message, Throwable t) {
        doLog(message, t);
    }

    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object)
     */
    public void info(Object message) {
        doLog(message, null);
    }

    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
     */
    public void info(Object message, Throwable t) {
        doLog(message, t);
    }

    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object)
     */
    public void warn(Object message) {
        doLog(message, null);
    }

    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
     */
    public void warn(Object message, Throwable t) {
        doLog(message, t);
    }

    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object)
     */
    public void error(Object message) {
        doLog(message, null);
    }

    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
     */
    public void error(Object message, Throwable t) {
        doLog(message, t);
    }

    /**
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
     */
    public void fatal(Object message) {
        doLog(message, null);
    }

    /**
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
     */
    public void fatal(Object message, Throwable t) {
        doLog(message, t);
    }    
    
    /**
     * Writes a log message and an optional Throwable to System.err.<p>
     * 
     * @param message the message to log
     * @param t the Throwable to log (if null nothing is logged)
     */
    private void doLog(Object message, Throwable t) {
        System.err.println(message);
        if (t != null) {
            t.printStackTrace(System.err);
        }           
    }
}
