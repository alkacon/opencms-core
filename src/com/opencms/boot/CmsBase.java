/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsBase.java,v $
* Date   : $Date: 2003/08/15 13:51:56 $
* Version: $Revision: 1.12 $
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

import java.io.File;

import source.org.apache.java.util.Configurations;

/**
 * OpenCms Base class for static access to system wide properties
 * and helper functions, e.g. OpenCms logging oder OpenCms base path.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.12 $ $Date: 2003/08/15 13:51:56 $
 */
public final class CmsBase extends Object {

    /** Reference to the OpenCms base path ("home directory" of OpenCms) */
    private static String c_basePath = null;

    /** Reference to the system log */
    private static CmsLog c_cmsLog = null;

    /** Indicates if the system log is initialized */
    protected static boolean c_servletLogging = false;
    
    /**
     * Default constructor. Nobody is allowed to create an instance of this class!
     */
    private CmsBase() {
        super();
    }    

    /**
     * Initialize the logging mechanism.<p>
     * 
     * @param config the configurations needed for initialization
     */
    public static void initializeServletLogging(Configurations config) {
        c_cmsLog = new CmsLog("log", config);
        c_servletLogging = true;
    }

    /**
     * Check if the system logging is active.
     * @return <code>true</code> if the logging is active, <code>false</code> otherwise.
     */
    public static boolean isLogging() {
        if (c_servletLogging) {
            return c_cmsLog.isActive();
        } else {
            return true;
        }
    }
    
    /**
     * Check if the system logging is active for the selected channel.
     *
     * @param channel the channel to check     
     * @return <code>true</code> if the logging is active for the selected channel, <code>false</code> otherwise.
     */
    public static boolean isLogging(String channel) {
        if (c_servletLogging) {
            return c_cmsLog.isActive(channel);
        } else {
            return true;
        }
    }    

    /**
     * Log a message into the OpenCms logfile.
     * If the logfile was not initialized (e.g. due tue a missing
     * ServletConfig while working with the console)
     * any log output will be written to the apache error log.
     * @param channel The channel the message is logged into
     * @param message The message to be logged,
     */
    public static void log(String channel, String message) {
        if (c_servletLogging) {
            c_cmsLog.log(channel, message);
        } else {
            System.err.println(message);
        }
    }

    /** 
     * Set the base path to the given value.<p>
     * 
     * @param s the base path
     * @return the (corrected) base path 
     */
    public static String setBasePath(String s) {
        if (s != null) {
            s = s.replace('\\', '/');
            s = s.replace('/', File.separatorChar);

            if (!s.endsWith(File.separator)) {
                s = s + File.separator;
            }

            if (c_servletLogging) log(I_CmsLogChannels.C_OPENCMS_INIT, "[CmsBase] Setting OpenCms home folder to " + s + ". ");
            c_basePath = s;
        }
        return s;
    }

    /**
     * Get the OpenCms base path.<p>
     * 
     * @return the current base path 
     */
    public static String getBasePath() {
        return c_basePath;
    }

    /**
     * Get the OpenCms web-base path.<p> 
     * 
     * @return the current web base path
     */
    public static String getWebBasePath() {
        File basePath = new File(c_basePath);
        String webBasePath = basePath.getParent();
        if (!webBasePath.endsWith(File.separatorChar+"")) {
            webBasePath += File.separatorChar;
        }
        return webBasePath;
    }

    /** 
     * Get the OpenCms WebApplicationName.<p> 
     * 
     * @return the web application name
     */
    public static String getWebAppName() {
        File basePath = new File(c_basePath);
        String webAppName = basePath.getParentFile().getName();
        return webAppName;
    }

    /**
     * Get the absolute web path for a given path.<p>
     * 
     * @param s the path
     * @return the absolute path
     */
    public static String getAbsoluteWebPath(String s) {
        if (s == null) {
            return null;
        }

        File f = new File(s);
        if (! f.isAbsolute()) {
            if (c_basePath == null) {
                return null;
            } else {
                return getWebBasePath() + s;
            }
        } else {
            return s;
        }
    }

    /**
     * Gets the absolute path for a given path.<p>
     * 
     * @param s the path
     * @return the absolute path
     */
    public static String getAbsolutePath(String s) {
        if (s == null) {
            return null;
        }

        File f = new File(s);
        if (! f.isAbsolute()) {
            if (c_basePath == null) {
                return null;
            } else {
                return c_basePath + s;
            }
        } else {
            return s;
        }
    }

    /**
     * Gets the path to the properties file.<p>
     * 
     * @param absolute flag to indicate if absolute path is wanted
     * @return the relative or absolute path to opencms.properties
     */
    public static String getPropertiesPath(boolean absolute) {
        String result = "config/opencms.properties";
        if (absolute) {
            if (c_basePath == null) {
                result = null;
            } else {
                result = c_basePath + result;
            }
        }
        return result;
    }
}
