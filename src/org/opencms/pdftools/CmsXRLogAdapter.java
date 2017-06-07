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

package org.opencms.pdftools;

import org.opencms.main.CmsLog;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.logging.Log;

import org.xhtmlrenderer.util.XRLog;
import org.xhtmlrenderer.util.XRLogger;

/**
 * Adaspter for sending xhtmlrenderer (flyingsaucer) log messages to the OpenCms log.<p>
 *
 * The log channels to which the log messages are sent start with the prefix 'org.opencms.' so that no
 * change in the log configuration is needed to make error messages appear in the log.<p>
 */
public class CmsXRLogAdapter implements XRLogger {

    /** The prefix used for log channels. */
    public static final String OCMS_PREFIX = "org.opencms.";

    /** Default logger name. */
    private static final String DEFAULT_LOGGER_NAME = "org.xhtmlrenderer.other";

    /** The logger name map. */
    private static final Map<Object, String> LOGGER_NAME_MAP;

    static {
        LOGGER_NAME_MAP = new HashMap<Object, String>();
        LOGGER_NAME_MAP.put(XRLog.CONFIG, "org.xhtmlrenderer.config");
        LOGGER_NAME_MAP.put(XRLog.EXCEPTION, "org.xhtmlrenderer.exception");
        LOGGER_NAME_MAP.put(XRLog.GENERAL, "org.xhtmlrenderer.general");
        LOGGER_NAME_MAP.put(XRLog.INIT, "org.xhtmlrenderer.init");
        LOGGER_NAME_MAP.put(XRLog.JUNIT, "org.xhtmlrenderer.junit");
        LOGGER_NAME_MAP.put(XRLog.LOAD, "org.xhtmlrenderer.load");
        LOGGER_NAME_MAP.put(XRLog.MATCH, "org.xhtmlrenderer.match");
        LOGGER_NAME_MAP.put(XRLog.CASCADE, "org.xhtmlrenderer.cascade");
        LOGGER_NAME_MAP.put(XRLog.XML_ENTITIES, "org.xhtmlrenderer.load.xmlentities");
        LOGGER_NAME_MAP.put(XRLog.CSS_PARSE, "org.xhtmlrenderer.cssparse");
        LOGGER_NAME_MAP.put(XRLog.LAYOUT, "org.xhtmlrenderer.layout");
        LOGGER_NAME_MAP.put(XRLog.RENDER, "org.xhtmlrenderer.render");
    }

    /**
     * @see org.xhtmlrenderer.util.XRLogger#log(java.lang.String, java.util.logging.Level, java.lang.String)
     */
    public void log(String where, Level level, String msg) {

        Log log = CmsLog.getLog(getLoggerName(where));
        sendMessageToLogger(level, msg, log, null);

    }

    /**
     * @see org.xhtmlrenderer.util.XRLogger#log(java.lang.String, java.util.logging.Level, java.lang.String, java.lang.Throwable)
     */
    public void log(String where, Level level, String msg, Throwable th) {

        Log log = CmsLog.getLog(getLoggerName(where));
        sendMessageToLogger(level, msg, log, th);
    }

    /**
     * @see org.xhtmlrenderer.util.XRLogger#setLevel(java.lang.String, java.util.logging.Level)
     */
    public void setLevel(String logger, Level level) {

        throw new UnsupportedOperationException("setLevel not supported");
    }

    /**
     * Gets the real logger name.<p>
     *
     * @param xrLoggerName the XR logger name
     *
     * @return the real logger name
     */
    private String getLoggerName(String xrLoggerName) {

        String result = LOGGER_NAME_MAP.get(xrLoggerName);
        if (result != null) {
            return OCMS_PREFIX + result;
        } else {
            return OCMS_PREFIX + DEFAULT_LOGGER_NAME;
        }
    }

    /**
     * Sends a log message to the commons-logging interface.<p>
     *
     * @param level the log level
     * @param message the message
     * @param log the commons logging log object
     * @param e a throwable or null
     */
    private void sendMessageToLogger(Level level, String message, Log log, Throwable e) {

        if (e == null) {
            if (level == Level.SEVERE) {
                log.error(message);
            } else if (level == Level.WARNING) {
                log.warn(message);
            } else if (level == Level.INFO) {
                log.info(message);
            } else if (level == Level.CONFIG) {
                log.info(message);
            } else if ((level == Level.FINE) || (level == Level.FINER) || (level == Level.FINEST)) {
                log.debug(message);
            } else {
                log.info(message);
            }
        } else {
            if (level == Level.SEVERE) {
                log.error(message, e);
            } else if (level == Level.WARNING) {
                log.warn(message, e);
            } else if (level == Level.INFO) {
                log.info(message, e);
            } else if (level == Level.CONFIG) {
                log.info(message, e);
            } else if ((level == Level.FINE) || (level == Level.FINER) || (level == Level.FINEST)) {
                log.debug(message, e);
            } else {
                log.info(message, e);
            }
        }
    }

}
