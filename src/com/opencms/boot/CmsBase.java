
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/boot/Attic/CmsBase.java,v $
* Date   : $Date: 2001/07/16 13:36:05 $
* Version: $Revision: 1.3 $
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.boot;

import source.org.apache.java.util.Configurations;
import java.io.File;

/**
 * OpenCms Base class for static access to system wide properties
 * and helper functions, e.g. OpenCms logging oder OpenCms base path.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.3 $ $Date: 2001/07/16 13:36:05 $
 */
public class CmsBase implements I_CmsLogChannels {

    /** Reference to the OpenCms base path ("home directory" of OpenCms) */
    private static String c_basePath = null;

    /** Reference to the system log */
    private static CmsLog c_cmsLog = null;

    /** Indicates if the system log is initialized */
    protected static boolean c_servletLogging = false;

    /**
     * Initialize the logging mechanism of the Jserv
     * @param configurations the configurations needed at initialization.
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
        if(c_servletLogging) {
            return c_cmsLog.isActive();
        }
        else {
            return true;
        }
    }

    /**
     * Log a message into the OpenCms logfile.
     * If the logfile was not initialized (e.g. due tue a missing
     * ServletConfig while working with the console)
     * any log output will be written to the apache error log.
     * @param channel The channel the message is logged into
     * @message The message to be logged,
     */
    public static void log(String channel, String message) {
        if(c_servletLogging) {
            c_cmsLog.log(channel, message);
        }
        else {
            System.err.println(message);
        }
    }

    /** Set the base path to the given value */
    protected static String setBasePath(String s) {
        if(s != null) {
            s = s.replace('\\', '/');
            s = s.replace('/', File.separatorChar);

            if(!s.endsWith(File.separator)) {
                s = s + File.separator;
            }

            log(C_OPENCMS_INIT, "[CmsBase] Setting OpenCms home folder to " + s + ". ");
            c_basePath = s;
        }
        return s;
    }

    /** Get the OpenCms base path */
    public static String getBasePath() {
        return c_basePath;
    }

    /** Get the OpenCms web-base path */
    public static String getWebBasePath() {
        File basePath = new File(c_basePath);
        String webBasePath = basePath.getParent();
        if(!webBasePath.endsWith(File.separatorChar+"")) {
            webBasePath += File.separatorChar;
        }
        return webBasePath;
    }

    public static String getAbsoluteWebPath(String s) {
        if(s == null) {
            return null;
        }

        File f = new File(s);
        if(! f.isAbsolute()) {
            if(c_basePath == null) {
                return null;
            } else {
                return getWebBasePath() + s;
            }
        } else {
            return s;
        }
    }

    public static String getAbsolutePath(String s) {
        if(s == null) {
            return null;
        }

        File f = new File(s);
        if(! f.isAbsolute()) {
            if(c_basePath == null) {
                return null;
            } else {
                return c_basePath + s;
            }
        } else {
            return s;
        }
    }

    public static String getPropertiesPath(boolean absolute) {
        String result = "config/opencms.properties";
        if(absolute) {
            if(c_basePath == null) {
                result = null;
            } else {
                result = c_basePath + result;
            }
        }
        return result;
    }
}
