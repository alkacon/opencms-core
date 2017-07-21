/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db;

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * An alternative export point driver which replaces the RFS targets of some export points with locations in a temporary export folder.
 *
 * This is designed specifically for export points which map to WEB-INF/lib and WEB-INF/classes. The problem with these is that replacing
 * jar files or classes does not work at runtime. If these files are written to a temp folder (which is not picked up by the servlet coontainer's
 * class loader) instead, an external script can be used to update the actual export point locations based on the temp folder while the servlet
 * container is stopped.
 *
 * Since the script needs to know which files to delete in the 'real' (not temporary) export point locations, deletions of files which are mapped
 * to the temp export point folder are not handled by deleting the files in that folder, but rather by replacing them with "dummy" files containing
 * a specific marker string (which is contained in the DELETE_MARKER variable), so the script that updates the 'real' folder needs to delete files
 * if it comes across a file starting with that marker string.
 */
public class CmsTempFolderExportPointDriver extends CmsExportPointDriver {

    /** The log object for this class. */
    @SuppressWarnings("unused")
    private static final Log LOG = CmsLog.getLog(CmsTempFolderExportPointDriver.class);

    /** The content to be used for dummy files in the temporary export point folders which represent deleted files. */
    public static final String DELETE_MARKER = "--OCMS-TEMP-EXPORTPOINT-DELETED-FILE-MARKER--";

    /** The byte array used to mark files as deleted. */
    protected static byte[] DELETE_MARKER_BYTES;

    static {
        try {
            DELETE_MARKER_BYTES = DELETE_MARKER.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // shouldn't happen
        }
    }

    /** The temp folder for export points. */
    public static final String TEMP_EXPORTPOINT_FOLDER = "WEB-INF/exportpoint-temp";

    /** Map of export points by VFS path. */
    private Map<String, CmsExportPoint> m_exportPointMap = Maps.newHashMap();

    /** The URIs (VFS paths) of export points which are mapped to the temporary export point folder. */
    private Set<String> m_urisWithTempFolderDestinations = Sets.newHashSet();

    /** The list of RFS destinations which should be replaced with the corresponding destinations in the tmporary export point folder. */
    private List<String> m_tempFolderDestinations;

    /**
     * Constructor for a CmsExportPointDriver.<p>
     *
     * @param exportpoints the list of export points
     * @param tempFolderDestinations the export point destinations (relative to the web application folder) which should be replaced with
     */
    public CmsTempFolderExportPointDriver(Set<CmsExportPoint> exportpoints, List<String> tempFolderDestinations) {

        super(); // Uses empty superclass constructor, i.e. we have to initialize members from parent class by hand
        m_tempFolderDestinations = tempFolderDestinations;
        m_exportpoints = exportpoints;
        m_exportpointLookupMap = new HashMap<String, String>();
        for (CmsExportPoint point : m_exportpoints) {
            String dest = point.getConfiguredDestination();
            m_exportPointMap.put(point.getUri(), point);
            if (shouldUseTempFolderForDestination(dest)) {
                String realDest = OpenCms.getSystemInfo().getAbsoluteRfsPathRelativeToWebApplication(
                    CmsStringUtil.joinPaths(TEMP_EXPORTPOINT_FOLDER, point.getConfiguredDestination()));
                m_exportpointLookupMap.put(point.getUri(), realDest);
                m_urisWithTempFolderDestinations.add(point.getUri());
            } else {
                if (point.getDestinationPath() != null) {
                    // otherwise this point is not valid, but must be kept for serializing the configuration
                    m_exportpointLookupMap.put(point.getUri(), point.getDestinationPath());
                }
            }
        }
    }

    /**
     * Removes a leading slash from a path string.<p>
     *
     * @param path the input string
     *
     * @return the path without leading slashes
     */
    private static String removeLeadingSlash(String path) {

        return path.replaceFirst("^/+", "");
    }

    /**
     * @see org.opencms.db.I_CmsExportPointDriver#deleteResource(java.lang.String, java.lang.String)
     */
    @Override
    public void deleteResource(String resourceName, String exportpoint) {

        File file = getExportPointFile(resourceName, exportpoint);
        if (m_urisWithTempFolderDestinations.contains(exportpoint)) {
            if (resourceName.endsWith("/")) {
                // leave folders in the temp exportpoint dir alone
                return;
            }
            writeFile(resourceName, exportpoint, DELETE_MARKER_BYTES);
        } else {
            if (file.exists() && file.canWrite()) {
                // delete the file (or folder)
                file.delete();
                // also delete empty parent directories
                File parent = file.getParentFile();
                if (parent.canWrite()) {
                    parent.delete();
                }
            }
        }
    }

    /**
     * @see org.opencms.db.CmsExportPointDriver#writeFile(java.lang.String, java.lang.String, byte[])
     */
    @Override
    public void writeFile(String resourceName, String exportpoint, byte[] content) {

        StringBuffer exportpath = new StringBuffer(128);
        exportpath.append(m_exportpointLookupMap.get(exportpoint));
        exportpath.append(resourceName.substring(exportpoint.length()));
        File file = new File(exportpath.toString());
        writeResource(file, content);
        CmsExportPoint point = m_exportPointMap.get(exportpoint);
        // we can use object identity comparison because DELETE_MARKER_BYTES is not used from the outside
        if ((content != DELETE_MARKER_BYTES) && shouldUseTempFolderForDestination(point.getConfiguredDestination())) {
            // we already wrote the file to the temp folder location,
            // now write it to the real location, but only if it is new
            exportpath = new StringBuffer(128);
            exportpath.append(point.getDestinationPath());
            exportpath.append(resourceName.substring(exportpoint.length()));
            file = new File(exportpath.toString());
            if (!file.exists()) {
                writeResource(file, content);
            }
        }
    }

    /**
     * Returns true if the given RFS destination should be replaced with the corresponding destination in the temporary export point folder.<p>
     *
     * @param destination an export point RFS destination
     * @return true if the corresponding export point destination in the temporary export point folder should be used
     */
    private boolean shouldUseTempFolderForDestination(String destination) {

        destination = removeLeadingSlash(destination);
        for (String tempFolderDestination : m_tempFolderDestinations) {
            tempFolderDestination = removeLeadingSlash(tempFolderDestination);
            if (CmsStringUtil.isPrefixPath(tempFolderDestination, destination)) {
                return true;
            }
        }
        return false;
    }

}