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

package org.opencms.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

/**
 * Utilities for dealing with log4j loggers.<p>
 */
public final class CmsLog4jUtil {

    /**
     * Hidden default constructor.<P>
     */
    private CmsLog4jUtil() {

        // do nothing
    }

    /**
     * Gets the list of all loggers.<p>
     *
     * @return the list of all loggers
     */
    @SuppressWarnings("resource")
    public static List<Logger> getAllLoggers() {

        LoggerContext context = (LoggerContext)(LogManager.getContext(false));
        Map<String, Logger> loggersByName = new TreeMap<String, Logger>();
        for (Logger logger : context.getLoggers()) {
            String loggerName = logger.getName();
            while (loggerName != null) {
                if (!loggersByName.containsKey(loggerName)) {
                    Logger currentLogger = (Logger)(LogManager.getLogger(loggerName));
                    loggersByName.put(loggerName, currentLogger);
                }
                loggerName = getParentLoggerName(loggerName);

            }
        }
        return new ArrayList<Logger>(loggersByName.values());

    }

    /**
     * Gets the parent logger name for a given logger name, or null if there is no parent logger name.<p>
     *
     * @param loggerName the name of a logger
     * @return the parent name of the logger, or null if the logger name has no parent
     */
    public static String getParentLoggerName(String loggerName) {

        int dotIndex = loggerName.lastIndexOf(".");
        if (dotIndex < 0) {
            return null;
        } else {
            return loggerName.substring(0, dotIndex);
        }

    }

}
