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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Utilities related to log4j.<p>
 *
 */
public class CmsLog4jUtil {

    /**
     * Ensures parent loggers are part of the given map.<p>
     *
     * @param loggers the logger map
     * @param loggerName the logger name
     */
    public static void ensureLoggerAndAncestors(Map<String, Logger> loggers, String loggerName) {

        while (loggerName != null) {
            if (!loggers.containsKey(loggerName)) {
                Logger currentLogger = (LogManager.getLogger(loggerName));
                loggers.put(loggerName, currentLogger);
            }
            loggerName = getParentLoggerName(loggerName);

        }

    }

    /**
     * Gets list of all loggers.<p>
     *
     * @return the list of loggers
     */
    public static List<Logger> getAllLoggers() {

        @SuppressWarnings("unchecked")
        List<Logger> loggers = new ArrayList<Logger>(Collections.list(LogManager.getCurrentLoggers()));
        Map<String, Logger> loggersByName = new TreeMap<String, Logger>();
        for (Logger logger : loggers) {
            String loggerName = logger.getName();
            while (loggerName != null) {
                if (!loggersByName.containsKey(loggerName)) {
                    Logger currentLogger = (LogManager.getLogger(loggerName));
                    loggersByName.put(loggerName, currentLogger);
                }
                loggerName = getParentLoggerName(loggerName);

            }
        }
        return new ArrayList<Logger>(loggersByName.values());

    }

    /**
     * Gets the parent logger name of a logger.<p>
     *
     * @param loggerName the logger name
     * @return the parent logger name
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
