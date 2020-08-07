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

package org.opencms.ui.apps.logfile;

import java.io.InputStream;
import java.util.Set;

/**
 * Provides log file download functionality.
 */
public interface I_CmsLogDownloadProvider {

    /**
     * Checks if the user can download a zip with all logs.
     *
     * @return true if the user can download a zip with all logs
     */
    boolean canDownloadAllLogs();

    /**
     * Gets the prefix to use for the download file name.
     *
     * @return the prefix for the download
     */
    String getDownloadPrefix();

    /**
     * Gets the set of log file paths.
     *
     * @return the set of log file paths
     */
    Set<String> getLogFiles();

    /**
     * Gets the input stream for the download with all logs (only works if canDownloadAllLogs() returned true)
     *
     * @return the input stream for the collected logs zip file
     */
    InputStream readAllLogs();

    /**
     * Gets the input stream for the download of a single log file
     *
     * @param path the full path of the log file (must be a value in the set returned by getLogFiles())
     *
     * @return the input stream for the download
     */
    InputStream readLog(String path);
}
