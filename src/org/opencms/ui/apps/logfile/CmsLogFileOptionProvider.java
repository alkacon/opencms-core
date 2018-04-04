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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.File;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.logging.Log;

/**
 * Provides log files which should be available as options in the OpenCms log file viewer.<p>
 *
 */
public final class CmsLogFileOptionProvider {

    /** Environment variable used to configure additional log files. */
    public static final String ENV_LOGFILES = "OCCO_ADDITIONAL_LOG_DIRS";

    /** Logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsLogFileOptionProvider.class);

    /**
     * Hidden default constructor.<p>
     */
    private CmsLogFileOptionProvider() {
        // hidden default constructor
    }

    /**
     * Gets the additional configured log folders.<p>
     *
     * @return the additional configured log folders
     */
    public static List<String> getAdditionalLogDirectories() {

        return OpenCms.getWorkplaceManager().getAdditionalLogFolderConfiguration().getLogFolders();

    }

    /**
     * Gets the log file options.<p>
     *
     * @return the log file options
     */
    public static TreeSet<File> getLogFiles() {

        TreeSet<File> result = new TreeSet<>();
        for (File file : new File(CmsLogFileApp.LOG_FOLDER).listFiles()) {
            result.add(file);
        }
        for (String dir : getAdditionalLogDirectories()) {
            File file = new File(dir);
            if (file.exists()) {
                if (file.isDirectory()) {
                    for (File child : file.listFiles()) {
                        if (child.canRead()) {
                            result.add(child);
                        } else {
                            LOG.error("Can not read " + child.getAbsolutePath());
                        }
                    }
                }
            }
        }
        return result;
    }

}
