/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsFileUtil.java,v $
 * Date   : $Date: 2004/08/03 07:19:04 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.staticexport.CmsLinkManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Provides File utility functions.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.5.0
 */
public final class CmsFileUtil {

    /**
     * Hides the public constructor.<p> 
     */
    private CmsFileUtil() {

        // noop
    }

    /**
     * Normalizes a file path that might contain '../' or './' or '//' elements to a normal absolute path.<p>
     * 
     * @param path the path to normalize
     * @return the normalized path
     */
    public static String normalizePath(String path) {

        if (path != null) {
            // ensure all File separators are '/'
            path = path.replace('\\', '/');
            String drive;
            if ((path.length() > 1) && (path.charAt(1) == ':')) {
                // windows path like C:\home\
                drive = path.substring(0, 2);
                path = path.substring(2);
            } else {
                drive = "";
            }
            if (path.charAt(0) == '/') {
                // trick to resolve all ../ inside a path
                path = "." + path;
            }
            // resolve all '../' or './' elements in the path
            path = CmsLinkManager.getAbsoluteUri(path, "/");
            // still some '//' elements might persist
            path = CmsStringUtil.substitute(drive.concat(path), "//", "/");
            // switch '/' back to OS dependend File separator
            path = path.replace('/', File.separatorChar);
        }
        return path;
    }

    /**
     * Deletes a directory in the file system and all subfolders of that directory.<p>
     * 
     * @param directory the directory to delete
     */
    public static void purgeDirectory(File directory) {

        if (directory.canRead() && directory.isDirectory()) {
            java.io.File[] files = directory.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f.isDirectory()) {
                    purgeDirectory(f);
                }
                if (f.canWrite()) {
                    f.delete();
                }
            }
            directory.delete();
        }
    }

    /**
     * Reads a file from test folder and converts it to a String with the specified encoding.<p> 
     * 
     * @param filename the file to read 
     * @param encoding the encodin to use when converting the file content to a String
     * @return the read file convered to a String
     * @throws IOException in case of file access errors
     */
    public static String readFile(String filename, String encoding) throws IOException {

        // create input and output stream
        InputStream in = CmsFileUtil.class.getClassLoader().getResourceAsStream(filename);
        if (in == null) {
            throw new FileNotFoundException(filename);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // read the file content
        int c;
        while ((c = in.read()) != -1) {
            out.write(c);
        }

        in.close();
        out.close();

        return new String(out.toByteArray(), encoding);
    }
}