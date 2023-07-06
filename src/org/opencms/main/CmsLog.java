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
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.util.Loader;

/**
 * Provides the OpenCms logging mechanism.<p>
 *
 * The OpenCms logging mechanism is based on Apache Commons Logging.
 * However, log4j is shipped with OpenCms and assumed to be used as default logging mechanism.
 * Since apparently Commons Logging may cause issues in more complex classloader scenarios,
 * we may switch the logging interface to log4j <code>UGLI</code> once the final release is available.<p>
 *
 * The log4j configuration file shipped with OpenCms is located in <code>WEB-INF/classes/log4j2.xml</code>.
 * By default, OpenCms will configure log4j using the first {@code log4j2.xml} available found by
 * {@link Loader#getResource(String, ClassLoader)}.<p>
 *
 * The following system properties are available for fine-tunning the logging configuration:
 * <dl>
 *     <dt>{@code opencms.set.logfile} (default: {@code true})</dt>
 *     <dd>Set to {@code false} to bypass the OpenCms standard logging configuration mechanism, delegating this to the
 *     logging framework of chose. Note that in this case,
 *         <ul>
 *             <li>the Workplace logging visualization tools will not be available, and</li>
 *             <li>you may need to enable automatic configuration of log4j2 (see <a
 *             href="http://logging.apache.org/log4j/2.x/manual/webapp.html">Using Log4j 2 in Web Applications</a>.
 *             This configuration takes place before OpenCms is correctly initialized, and has been disabled in the
 *             standard OpenCms distribution because at that point, it has not yet set default values for the
 *             relevant system properties</li>
 *         </ul></dd>
 *     <dt>{@code opencms.logfolder} (default: "{@code WEB-INF/logs/}")</dt>
 *     <dd>Path to the folder where OpenCms will look for logging files to show in the Workplace GUI.</dd>
 *     <dt>{@code opencms.logfile} (default: "{@code WEB-INF/logs/opencms.log}")</dt>
 *     <dd>Absolute path to the OpenCms log file.</dd>
 * </dl>
 *
 * If the logging configuration is not bypassed by setting {@code opencms.set.logfile=false}, OpenCms will first assing
 * them default values if needed and then will make these system properties available for the configuration of log4j.
 * In the {@code log4j2.xml} configuration file, system properties can be referenced using the prefix "{@code sys:}" as
 * in this example:
 * <pre>
 *     fileName="${sys:opencms.logfolder}opencms-search.log"
 * </pre>
 *
 * @since 6.0.0
 */
public final class CmsLog {

    /** The name of the opencms.log file. */
    public static final String FILE_LOG = "opencms.log";

    /** Path to the "logs" folder relative to the "WEB-INF" directory of the application. */
    public static final String FOLDER_LOGS = "logs" + File.separatorChar;

    /** Name of the system property that configures the path where OpenCms looks for logs to show in the Workplace App. */
    public static final String PROPERTY_LOGFOLDER = "opencms.logfolder";

    /** Name of the system property that configures the absolute path to the file where OpenCms will write its log. */
    public static final String PROPERTY_LOGFILE = "opencms.logfile";

    /** Log for initialization messages. */
    public static Log INIT;

    /** The absolute path to the folder of the main OpenCms log file (in the "real" file system). */
    private static String m_logFileRfsFolder;

    /** The absolute path to the OpenCms log file (in the "real" file system). */
    private static String m_logFileRfsPath;

    /** Set of names of channels that should not be managed via the GUI. */
    private static CopyOnWriteArraySet<String> NON_MANAGEABLE_CHANNELS = new CopyOnWriteArraySet<>();

    /**
     * Initializes the OpenCms logger configuration.<p>
     */
    static {
        //
        // DO NOT USE ANY OPENCMS CLASSES THAT USE STATIC LOGGER INSTANCES IN THIS STATIC BLOCK
        // OTHERWISE THEIR LOGGER WOULD NOT BE INITIALIZED PROPERLY
        //
        try {
            // look for the log4j2.xml that shipped with OpenCms
            URL log4j2Url = Loader.getResource("log4j2.xml", null);
            if (log4j2Url != null) {
                // found log4j properties
                File log4jProps = new File(URLDecoder.decode(log4j2Url.getPath(), Charset.defaultCharset().name()));
                String path = log4jProps.getAbsolutePath();

                // check if OpenCms should set the log file environment variable
                boolean setLogFile = Boolean.parseBoolean(System.getProperty("opencms.set.logfile", "true"));
                if (setLogFile) {
                    final String propertyLogfolder = System.getProperty(PROPERTY_LOGFOLDER);
                    final String propertyLogfile = System.getProperty(PROPERTY_LOGFILE);

                    // in a default OpenCms configuration, the following path would point to the OpenCms "WEB-INF" folder
                    String webInfPath = log4jProps.getParent();
                    webInfPath = webInfPath.substring(0, webInfPath.lastIndexOf(File.separatorChar) + 1);

                    String logFilePath = ((propertyLogfile != null)
                    ? propertyLogfile
                    : webInfPath + FOLDER_LOGS + FILE_LOG);
                    String logFolderPath = ((propertyLogfolder != null) ? propertyLogfolder : webInfPath + FOLDER_LOGS);
                    m_logFileRfsPath = new File(logFilePath).getAbsolutePath();
                    m_logFileRfsFolder = new File(logFolderPath).getAbsolutePath() + File.separatorChar;

                    // Set "opencms.log*" variables if not set. These should be used in the log42.xml config file
                    // like this: 'fileName="${sys:opencms.logfolder}opencms-search.log"'
                    if (propertyLogfile == null) {
                        System.setProperty(PROPERTY_LOGFILE, m_logFileRfsPath);
                    }
                    if (propertyLogfolder == null) {
                        System.setProperty(PROPERTY_LOGFOLDER, m_logFileRfsFolder);
                    }

                    // re-read the configuration with the new environment variable available
                    ConfigurationSource source = ConfigurationSource.fromUri(log4j2Url.toURI());
                    Configurator.initialize(null, source);

                    // In case we have multiple OpenCms instances running in the servlet container, we don't want them to use
                    // the same log file path, so clear the system properties. Users will have to modify the log4j configuration
                    // if they want to customize the log file locations for multiple instances.
                    for (String prop : new String[] {PROPERTY_LOGFILE, PROPERTY_LOGFOLDER}) {
                        System.clearProperty(prop);
                    }
                }
                // can't localize this message since this would end in an endless logger init loop
                INIT = LogFactory.getLog("org.opencms.init");
                INIT.info(". Log4j config file    : " + path);
                INIT.debug(". m_logFileRfsPath    : " + m_logFileRfsPath);
                INIT.debug(". m_logFileRfsFolder  : " + m_logFileRfsFolder);
            } else {
                System.err.println("'log4j2.xml' not found. (Default location: WEB-INF/classes/log4j2.xml)");
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
     * Helper for safely evaluating lambda functions to produce log output and catch exceptions they might throw.
     *
     * @param log the logger to use for logging errors
     * @param stringProvider the string provider (normally just given as a lambda function)
     *
     * @return the result of the function (or &lt;ERROR&gt; if an exception was thrown)
     */
    public static String eval(Log log, Callable<String> stringProvider) {

        try {
            return stringProvider.call();
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
            return "<ERROR>";
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
     * Checks whether a log channel should be manageable through the GUI.
     *
     * @param channel the name of the channel
     *
     * @return true if the channel should be manageable through the GUI
     */
    public static boolean isManageable(String channel) {

        return !NON_MANAGEABLE_CHANNELS.contains(channel);
    }

    /**
    * Adds a log channel that should not be manageable via the GUI.
    *
    * @param channel the channel to add
    */
    public static void makeChannelNonManageable(String channel) {

        NON_MANAGEABLE_CHANNELS.add(channel);
    }

    /**
     * Render throwable using Throwable.printStackTrace.
     * <p>This code copy from "org.apache.log4j.DefaultThrowableRenderer.render(Throwable throwable)"</p>
     * @param throwable throwable, may not be null.
     * @return string representation.
     */
    public static String[] render(final Throwable throwable) {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try {
            throwable.printStackTrace(pw);
        } catch (RuntimeException ex) {
            // nothing to do
        }
        pw.flush();
        LineNumberReader reader = new LineNumberReader(new StringReader(sw.toString()));
        ArrayList<String> lines = new ArrayList<>();
        try {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (IOException ex) {
            if (ex instanceof InterruptedIOException) {
                Thread.currentThread().interrupt();
            }
            lines.add(ex.toString());
        }
        //String[] tempRep = new String[lines.size()];
        return lines.toArray(new String[0]);
    }

    /**
     * Returns the absolute path to the folder of the main OpenCms log file
     * (in the "real" file system).<p>
     *
     * If the method returns <code>null</code>, this means that the log
     * file is not managed by OpenCms.<p>
     *
     * @return the absolute path to the folder of the main OpenCms log file (in
     * the "real" file system)
     */
    protected static String getLogFileRfsFolder() {

        return m_logFileRfsFolder;
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
}