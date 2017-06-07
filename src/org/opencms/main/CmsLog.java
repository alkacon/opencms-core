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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.helpers.Loader;

/**
 * Provides the OpenCms logging mechanism.<p>
 *
 * The OpenCms logging mechanism is based on Apache Commons Logging.
 * However, log4j is shipped with OpenCms and assumed to be used as default logging mechanism.
 * Since apparently Commons Logging may cause issues in more complex classloader scenarios,
 * we may switch the logging interface to log4j <code>UGLI</code> once the final release is available.<p>
 *
 * The log4j configuration file shipped with OpenCms is located
 * in <code>${opencms.WEB-INF}/classes/log4j.properties</code>. OpenCms will auto-configure itself
 * to write it's log file to <code>${opencms.WEB-INF}/logs/opencms.log</code>. This default behaviour
 * can be supressed by either using a log4j configuration file from another location, or by setting the
 * special property <code>${opencms.set.logfile}</code> in the log4j configuration file to <code>false</code>.
 *
 * @since 6.0.0
 */
public final class CmsLog {

    /** The name of the opencms.log file. */
    public static final String FILE_LOG = "opencms.log";

    /** Path to the "logs" folder relative to the "WEB-INF" directory of the application. */
    public static final String FOLDER_LOGS = "logs" + File.separatorChar;

    /** Log for initialization messages. */
    public static Log INIT;

    /** The absolute path to the folder of the main OpenCms log file (in the "real" file system). */
    private static String m_logFileRfsFolder;

    /** The absolute path to the OpenCms log file (in the "real" file system). */
    private static String m_logFileRfsPath;

    /**
     * Initializes the OpenCms logger configuration.<p>
     */
    static {
        //
        // DO NOT USE ANY OPENCMS CLASSES THAT USE STATIC LOGGER INSTANCES IN THIS STATIC BLOCK
        // OTHERWISE THEIR LOGGER WOULD NOT BE INITIALIZED PROPERLY
        //
        try {
            // look for the log4j.properties that shipped with OpenCms
            URL url = Loader.getResource("log4j.properties");
            if (url != null) {
                // found some log4j properties, let's see if these are the ones used by OpenCms
                File log4jProps = new File(URLDecoder.decode(url.getPath(), Charset.defaultCharset().name()));
                String path = log4jProps.getAbsolutePath();
                // in a default OpenCms configuration, the following path would point to the OpenCms "WEB-INF" folder
                String webInfPath = log4jProps.getParent();
                webInfPath = webInfPath.substring(0, webInfPath.lastIndexOf(File.separatorChar) + 1);

                // check for the OpenCms configuration file
                // check for the OpenCms tld file
                String tldFilePath = webInfPath + CmsSystemInfo.FILE_TLD;
                File tldFile = new File(tldFilePath);
                if (tldFile.exists()) {
                    // assume this is a default OpenCms log configuration
                    ExtendedProperties configuration = new ExtendedProperties(path);
                    // check if OpenCms should set the log file environment variable
                    boolean setLogFile = configuration.getBoolean("opencms.set.logfile", false);
                    if (setLogFile) {
                        // set "opencms.log" variable
                        String logFilePath = webInfPath + FOLDER_LOGS + FILE_LOG;
                        File logFile = new File(logFilePath);
                        m_logFileRfsPath = logFile.getAbsolutePath();
                        m_logFileRfsFolder = logFile.getParent() + File.separatorChar;
                        System.setProperty("opencms.logfile", m_logFileRfsPath);
                        System.setProperty("opencms.logfolder", m_logFileRfsFolder);
                        // re-read the configuration with the new environment variable available
                        PropertyConfigurator.configure(path);
                    }
                }
                // can't localize this message since this would end in an endless logger init loop
                INIT = LogFactory.getLog("org.opencms.init");
                INIT.info(". Log4j config file    : " + path);
            }
        } catch (SecurityException e) {
            // may be caused if environment can't be written
            e.printStackTrace(System.err);
        } catch (Exception e) {
            // unexpected but nothing we can do about it, print stack trace and continue
            e.printStackTrace(System.err);
        }
    }

    /**
     * Hides the public constructor.<p>
     */
    private CmsLog() {

        // hides the public constructor
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
    public static Log getLog(Object obj) {

        if (obj instanceof String) {
            return LogFactory.getLog((String)obj);
        } else if (obj instanceof Class<?>) {
            return LogFactory.getLog((Class<?>)obj);
        } else {
            return LogFactory.getLog(obj.getClass());
        }
    }

    /**
     * Returns the filename of the log file (in the "real" file system).<p>
     *
     * If the method returns <code>null</code>, this means that the log
     * file is not managed by OpenCms.<p>
     *
     * @return the filename of the log file (in the "real" file system)
     */
    protected static String getLogFileRfsPath() {

        return m_logFileRfsPath;
    }

    /**
     * Helper method to get the java.io.File corresponding to the given URL.<p>
     *
     * This is needed to deal with escaped spaces in URLs.
     *
     * @param url the URL
     * @return the file
     */
    private static File getFileForURL(URL url) {

        File result;
        try {
            result = new File(url.toURI());
        } catch (URISyntaxException e) {
            result = new File(url.getPath());
        }
        return result;
    }

}