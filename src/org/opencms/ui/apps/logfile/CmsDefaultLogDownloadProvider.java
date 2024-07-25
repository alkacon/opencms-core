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
import org.opencms.ui.apps.logfile.CmsLogDownloadDialog.ZipGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Default implementation for the log download functionality.
 */
public class CmsDefaultLogDownloadProvider implements I_CmsLogDownloadProvider {

    /** Logger instance for the class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultLogDownloadProvider.class);

    /** Path to zip file.*/
    private static final String ZIP_PATH = CmsLogFileApp.LOG_FOLDER + "logs.zip";

    /**
     * @see org.opencms.ui.apps.logfile.I_CmsLogDownloadProvider#canDownloadAllLogs()
     */
    public boolean canDownloadAllLogs() {

        return true;
    }

    /**
     * @see org.opencms.ui.apps.logfile.I_CmsLogDownloadProvider#getDownloadPrefix()
     */
    public String getDownloadPrefix() {

        return "";
    }

    /**
     * @see org.opencms.ui.apps.logfile.I_CmsLogDownloadProvider#getLogFiles()
     */
    public Set<String> getLogFiles() {

        Set<String> result = new HashSet<>();
        for (File file : CmsLogFileOptionProvider.getLogFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            String path = file.getAbsolutePath();
            if (!path.endsWith(".zip") && !path.endsWith(".gz")) {
                result.add(file.getAbsolutePath());
            }
        }
        return result;
    }

    /**
     * @see org.opencms.ui.apps.logfile.I_CmsLogDownloadProvider#readAllLogs()
     */
    public InputStream readAllLogs() {

        FileOutputStream fos = null;
        ZipGenerator zipGen = null;
        try {
            fos = new FileOutputStream(ZIP_PATH);
            zipGen = new ZipGenerator(fos);
            for (File file : CmsLogFileOptionProvider.getLogFiles()) {
                if (!file.isDirectory() & !ZIP_PATH.equals(file.getAbsolutePath())) {
                    zipGen.addToZip(new File(CmsLogFileApp.LOG_FOLDER), file);
                }
            }
            zipGen.close();
            fos.close();
            return new FileInputStream(ZIP_PATH);

        } catch (IOException e) {
            LOG.error("unable to build zip file", e);
            return null;
        } finally {
            if (zipGen != null) {
                try {
                    zipGen.close();
                } catch (Exception e) {
                    LOG.info(e.getLocalizedMessage(), e);
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                    LOG.info(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * @see org.opencms.ui.apps.logfile.I_CmsLogDownloadProvider#readLog(java.lang.String)
     */
    public InputStream readLog(String path) {

        Set<String> files = getLogFiles();
        if (files.contains(path)) {
            try {
                return new FileInputStream(path);
            } catch (FileNotFoundException e) {
                return null;
            }
        } else {
            return null;
        }
    }

}
