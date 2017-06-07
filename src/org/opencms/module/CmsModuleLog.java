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

package org.opencms.module;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;

/**
 * This class is responsible for reading and writing module import/export log messages, to be used by CmsResourceWrapperModules.
 */
public class CmsModuleLog {

    /**
     * Action type.<p>
     */
    public enum Action {
        /** import. */
        importModule("import"),

        /** export. */
        exportModule("export"),

        /** delete. */
        deleteModule("delete");

        /** The action name to be written to the log. */
        private String m_printName;

        /**
         * Creates new action type with the given print name.<p>
         *
         * @param printName the name to be written to the log
         */
        Action(String printName) {
            m_printName = printName;
        }

        /**
         * Gets the name to be written to the log.<p>
         *
         * @return the name to be written to the log
         */
        public String getPrintName() {

            return m_printName;
        }
    }

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsModuleLog.class);

    /** Date format for log messages. */
    public static final String DATE_FORMAT_STRING = "yyyy-MM-dd/HH:mm:ss";

    /**
     * Gets the log file for the given module name.<p>
     *
     * @param moduleName the module
     *
     * @return the log file
     */
    public File getLogFile(String moduleName) {

        return new File(
            OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebInf("packages/_modulelogs/" + moduleName + ".log"));
    }

    /**
     * Logs a module action.<p>
     *
     * @param moduleName the module name
     * @param action the action
     * @param ok true if the action was successful
     */
    public synchronized void log(String moduleName, Action action, boolean ok) {

        if (moduleName == null) {
            return;
        }
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_STRING);
        String now = format.format(new Date());
        String message = now + " " + action.getPrintName() + " " + (ok ? "0" : "1");
        log(moduleName, message);
    }

    /**
     * Reads the log file and returns the data.<p>
     *
     * @param moduleName the module name
     * @return the log contents
     *
     * @throws IOException if something goes wrong
     */
    public synchronized byte[] readLog(String moduleName) throws IOException {

        File logFile = getLogFile(moduleName);
        if (logFile.exists()) {
            return CmsFileUtil.readFile(logFile);
        } else {
            return new byte[] {};
        }
    }

    /**
     * Logs a message for the given module.<p>
     *
     * @param moduleName the module name
     * @param message the message to log
     */
    private synchronized void log(String moduleName, String message) {

        // We re-open the file every time. This should be OK performance-wise, since modules are usually not
        // imported/exported/deleted so frequently that this would matter.
        File logFile = getLogFile(moduleName);
        logFile.getParentFile().mkdirs();
        FileOutputStream out = null;
        PrintStream ps = null;
        try {
            out = new FileOutputStream(logFile, true); // open file with append=true
            ps = new PrintStream(out);
            ps.println(message);
        } catch (IOException e) {
            LOG.error(e.getLocalizedMessage(), e);
        } finally {
            if (ps != null) {
                ps.close();
            }
        }
    }
}
