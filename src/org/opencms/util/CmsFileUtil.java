/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsFileUtil.java,v $
 * Date   : $Date: 2005/06/22 10:38:11 $
 * Version: $Revision: 1.18 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.staticexport.CmsLinkManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Provides File utility functions.<p>
 * 
 * @author  Alexander Kandzior 
 * @since 5.5.0
 */
public final class CmsFileUtil {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFileUtil.class);

    /**
     * Hides the public constructor.<p> 
     */
    private CmsFileUtil() {

        // noop
    }

    /**
     * Simply version of a 1:1 binary file copy.<p>
     * 
     * @param fromFile the name of the file to copy
     * @param toFile the name of the target file
     * @throws IOException if any IO error occurs during the copy operation
     */
    public static void copy(String fromFile, String toFile) throws IOException {

        File inputFile = new File(fromFile);
        File outputFile = new File(toFile);

        FileInputStream in = new FileInputStream(inputFile);
        FileOutputStream out = new FileOutputStream(outputFile);
        int c;

        while ((c = in.read()) != -1) {
            out.write(c);
        }

        in.close();
        out.close();
    }

    /** 
     * Returns the formatted filesize to Bytes, KB, MB or GB depending on the given value.<p>
     * 
     * @param filesize in bytes 
     * @param locale the locale of the current OpenCms user or the System's default locale if the first choice 
     *               is not at hand. 
     * 
     * @return the formatted filesize to Bytes, KB, MB or GB depending on the given value 
     **/
    public static String formatFilesize(long filesize, Locale locale) {

        String result;
        filesize = Math.abs(filesize);

        if (Math.abs(filesize) < 1024) {
            result = Messages.get().key(
                locale,
                Messages.GUI_FILEUTIL_FILESIZE_BYTES_1,
                new Object[] {new Long(filesize)});
        } else if (Math.abs(filesize) < 1048576) {
            // 1048576 = 1024.0 * 1024.0
            result = Messages.get().key(
                locale,
                Messages.GUI_FILEUTIL_FILESIZE_KBYTES_1,
                new Object[] {new Double(filesize / 1024.0)});
        } else if (Math.abs(filesize) < 1073741824) {
            // 1024.0^3 =  1073741824
            result = Messages.get().key(
                locale,
                Messages.GUI_FILEUTIL_FILESIZE_MBYTES_1,
                new Object[] {new Double(filesize / 1048576.0)});
        } else {
            result = Messages.get().key(
                locale,
                Messages.GUI_FILEUTIL_FILESIZE_GBYTES_1,
                new Object[] {new Double(filesize / 1073741824.0)});
        }
        return result;
    }

    /**
     * Returns a list of all filtered files in the RFS.<p>
     * 
     * If the <code>name</code> is not a folder the folder that contains the
     * given file will be used instead.<p>
     * 
     * Despite the filter may not accept folders, every subfolder is traversed
     * if the <code>includeSubtree</code> parameter is set.<p>
     * 
     * @param name a folder or file name
     * @param filter a filter
     * @param includeSubtree if to include subfolders
     * 
     * @return a list of filtered <code>{@link File}</code> objects
     */
    public static List getFiles(String name, FileFilter filter, boolean includeSubtree) {

        List ret = new ArrayList();

        File file = new File(name);
        if (!file.isDirectory()) {
            file = new File(file.getParent());
            if (!file.isDirectory()) {
                return ret;
            }
        }
        File[] dirContent = file.listFiles();
        for (int i = 0; i < dirContent.length; i++) {
            File f = dirContent[i];
            if (filter.accept(f)) {
                ret.add(f);
            }
            if (includeSubtree && f.isDirectory()) {
                ret.addAll(getFiles(f.getAbsolutePath(), filter, true));
            }
        }

        return ret;
    }

    /**
     * Returns the absolute path name for the given relative 
     * path name if it was found by the context Classloader of the 
     * current Thread.<p>
     * 
     * The argument has to denote a resource within the Classloaders 
     * scope. A <code>{@link java.net.URLClassLoader}</code> implementation for example would 
     * try to match a given path name to some resource under it's URL 
     * entries.<p>
     * 
     * As the result is internally obtained as an URL it is reduced to 
     * a file path by the call to <code>{@link java.net.URL#getFile()}</code>. Therefore 
     * the returned String will start with a '/' (no problem for java.io).<p>
     * 
     * @param fileName the filename to return the path from the Classloader for
     * 
     * @return the absolute path name for the given relative 
     *   path name if it was found by the context Classloader of the 
     *   current Thread or an empty String if it was not found
     * 
     * @see Thread#getContextClassLoader()
     */
    public static String getResourcePathFromClassloader(String fileName) {

        boolean isFolder = CmsResource.isFolder(fileName);
        String result = "";
        URL inputUrl = Thread.currentThread().getContextClassLoader().getResource(fileName);
        if (inputUrl != null) {
            result = inputUrl.getFile();
            if (isFolder && !CmsResource.isFolder(result)) {
                result = result + '/';
            }
        } else {
            if (LOG.isErrorEnabled()) {
                try {
                    URLClassLoader cl = (URLClassLoader)Thread.currentThread().getContextClassLoader();
                    URL[] paths = cl.getURLs();
                    LOG.error(Messages.get().key(
                        Messages.ERR_MISSING_CLASSLOADER_RESOURCE_2,
                        fileName,
                        Arrays.asList(paths)));
                } catch (Throwable t) {
                    LOG.error(Messages.get().key(Messages.ERR_MISSING_CLASSLOADER_RESOURCE_1, fileName));
                }
            }
        }
        return result;
    }

    /**
     * Normalizes a file path that might contain '../' or './' or '//' elements to a normal absolute path,
     * the path separator char is {@link File#separatorChar}.<p>
     * 
     * @param path the path to normalize
     * @return the normalized path
     */
    public static String normalizePath(String path) {

        return normalizePath(path, File.separatorChar);
    }

    /**
     * Normalizes a file path that might contain '../' or './' or '//' elements to a normal absolute path.<p>
     * 
     * Can also handle Windows like path information containing a drive letter, 
     * like <code>C:\path\..\</code>.<p>
     * 
     * @param path the path to normalize
     * @param separatorChar the file separator char to use, for example {@link File#separatorChar}
     * @return the normalized path
     */
    public static String normalizePath(String path, char separatorChar) {

        if (CmsStringUtil.isNotEmpty(path)) {
            // ensure all File separators are '/'
            path = path.replace('\\', '/');
            String drive = null;
            if ((path.length() > 1) && (path.charAt(1) == ':')) {
                // windows path like C:\home\
                drive = path.substring(0, 2);
                path = path.substring(2);
            }
            if (path.charAt(0) == '/') {
                // trick to resolve all ../ inside a path
                path = '.' + path;
            }
            // resolve all '../' or './' elements in the path
            path = CmsLinkManager.getAbsoluteUri(path, "/");
            // re-append drive if required
            if (drive != null) {
                path = drive.concat(path);
            }
            // still some '//' elements might persist
            path = CmsStringUtil.substitute(path, "//", "/");
            // switch '/' back to OS dependend File separator if required
            if (separatorChar != '/') {
                path = path.replace('/', separatorChar);
            }
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
            File[] files = directory.listFiles();
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
     * Reads a file from the RFS and returns the file content.<p> 
     * 
     * @param filename the file to read 
     * @return the read file content
     * 
     * @throws IOException in case of file access errors
     */
    public static byte[] readFile(String filename) throws IOException {

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

        return out.toByteArray();
    }

    /**
     * Reads a file from the RFS and converts it to a String with the specified encoding.<p> 
     * 
     * @param filename the file to read 
     * @param encoding the encoding to use when converting the file content to a String
     * @return the read file convered to a String
     * @throws IOException in case of file access errors
     */
    public static String readFile(String filename, String encoding) throws IOException {

        return new String(readFile(filename), encoding);
    }

    /**
     * Searches for the OpenCms web application 'WEB-INF' folder during system startup, code or 
     * <code>null</code> if the 'WEB-INF' folder can not be found.<p>
     * 
     * @param startFolder the folder where to start searching
     * 
     * @return String the path of the 'WEB-INF' folder in the 'real' file system, or <code>null</code>
     */
    public static String searchWebInfFolder(String startFolder) {

        if (CmsStringUtil.isEmpty(startFolder)) {
            return null;
        }

        File f = new File(startFolder);
        if (!f.exists() || !f.isDirectory()) {
            return null;
        }

        File configFile = new File(f, "config/opencms.xml".replace('/', File.separatorChar));
        if (configFile.exists() && configFile.isFile()) {
            return f.getAbsolutePath();
        }

        String webInfFolder = null;
        File[] subFiles = f.listFiles();
        for (int i = 0; i < subFiles.length; i++) {
            if (subFiles[i].isDirectory()) {
                webInfFolder = searchWebInfFolder(subFiles[i].getAbsolutePath());
                if (webInfFolder != null) {
                    break;
                }
            }
        }

        return webInfFolder;
    }
}