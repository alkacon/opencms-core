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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Helper class for programmatically building zip files for test cases.<p>
 */
public class CmsZipBuilder {

    /** The file contents, with the paths as keys. */
    private Map<String, byte[]> m_entries = new HashMap<>();

    /**
     * Adds parent paths of a path to a set of paths.<p>
     *
     * @param path the path
     * @param parents the set to add the parent paths to
     */
    private static void addParents(String path, Set<String> parents) {

        String parent = new File(path).getParent();
        if (parent != null) {
            parent = normalizePath(parent);
            if (!parents.contains(parent)) {
                parents.add(parent);
                addParents(parent, parents);
            }
        }
    }

    /**
     * Converts paths to a normal form.<p>
     * @param path the path to normalize
     *
     * @return the normalized path
     */
    private static String normalizePath(String path) {

        String result = CmsFileUtil.removeLeadingSeparator(CmsFileUtil.removeTrailingSeparator(path));
        return result;

    }

    /**
     * Adds a file entry.<p>
     *
     * @param path the file path
     * @param content the file content
     */
    public void addFile(String path, byte[] content) {

        path = normalizePath(path);
        m_entries.put(path, content);
    }

    /**
     * Adds a file entry.<p>
     *
     * @param path the file path
     * @param content the file content (gets encoded as UTF-8)
     */
    public void addFile(String path, String content) {

        try {
            addFile(path, content.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Writes the entry to a zip stream.<p>
     *
     * @param zip the stream to write the entries to
     * @throws IOException if something goes wrong
     */
    public void write(ZipOutputStream zip) throws IOException {

        Set<String> parents = new TreeSet<>();

        for (String path : m_entries.keySet()) {
            addParents(path, parents);
        }
        parents.remove("/"); // implicitly created
        System.out.println(parents);

        for (String parent : parents) {
            ZipEntry entry = new ZipEntry(CmsStringUtil.joinPaths(parent, "/"));
            zip.putNextEntry(entry);
        }

        for (Map.Entry<String, byte[]> entry : m_entries.entrySet()) {
            ZipEntry zipEntry = new ZipEntry(entry.getKey());
            zip.putNextEntry(zipEntry);
            zip.write(entry.getValue());
        }

    }

    /**
     * Generates the zip file and returns it.<p>
     *
     * @return the zip file
     * @throws IOException if something goes wrong when generating the zip file
     */
    public File writeZip() throws IOException {

        File file = File.createTempFile("CmsZipBuilderTempFile-", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            write(zos);
        }
        return file;
    }

}
