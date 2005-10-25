/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/CmsFileUtil.java,v $
 * Date   : $Date: 2005/10/25 18:38:50 $
 * Version: $Revision: 1.21.2.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import org.apache.commons.logging.Log;

/**
 * Provides File utility functions.<p>
 * 
 * @author  Alexander Kandzior 
 * 
 * @version $Revision: 1.21.2.4 $ 
 * 
 * @since 6.0.0 
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
     * Checks if all resources are present.<p>
     * 
     * @param cms an initialized OpenCms user context which must have read access to all resources
     * @param resources a list of vfs resource names to check
     * 
     * @throws CmsIllegalArgumentException in case not all resources exist or can be read with the given OpenCms user context
     */
    public static void checkResources(CmsObject cms, List resources) throws CmsIllegalArgumentException {

        StringBuffer result = new StringBuffer(128);
        ListIterator it = resources.listIterator();
        while (it.hasNext()) {
            String resourcePath = (String)it.next();
            try {
                CmsResource resource = cms.readResource(resourcePath);
                // append folder separator, if resource is a folder and does not and with a slash
                if (resource.isFolder() && !resourcePath.endsWith("/")) {
                    it.set(resourcePath + "/");
                }
            } catch (CmsException e) {
                result.append(resourcePath);
                result.append('\n');
            }
        }
        if (result.length() > 0) {
            throw new CmsIllegalArgumentException(Messages.get().container(
                Messages.ERR_MISSING_RESOURCES_1,
                result.toString()));
        }
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
     * Returns a comma separated list of resource paths names, with the site root 
     * from the given OpenCms user context removed.<p> 
     * 
     * @param context the current users OpenCms context (optional, may be <code>null</code>)
     * @param resources a List of <code>{@link CmsResource}</code> instances to get the names from
     * 
     * @return a comma separated list of resource paths names
     */
    public static String formatResourceNames(CmsRequestContext context, List resources) {

        if (resources == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(128);
        Iterator i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = (CmsResource)i.next();
            String path = res.getRootPath();
            if (context != null) {
                path = context.removeSiteRoot(path);
            }
            result.append(path);
            if (i.hasNext()) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    /**
     * Returns the extension of the given file name, that is the part behind the last '.' char,
     * converted to lower case letters.<p>
     * 
     * The result does contain the '.' char. For example, if the input is <code>"opencms.html"</code>,
     * then the result will be <code>".html"</code>.<p>
     * 
     * If the given file name does not contain a '.' char, the empty String <code>""</code> is returned.<p>
     * 
     * Please note: No check is performed to ensure the given file name is not <code>null</code>.<p>
     * 
     * @param filename the file name to get the extension for
     * @return the extension of the given file name
     */
    public static String getFileExtension(String filename) {

        int pos = filename.lastIndexOf('.');
        return (pos >= 0) ? filename.substring(pos).toLowerCase() : "";
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
     * Returns the file name for a given VFS name that has to be written to a repository in the "real" file system, 
     * by appending the VFS root path to the given base repository path, also adding an 
     * folder for the "online" or "offline" project.<p>
     *
     * @param repository the base repository path
     * @param vfspath the VFS root path to write to use 
     * @param online flag indicates if the result should be used for the online project (<code>true</code>) or not
     * 
     * @return The full uri to the JSP
     */
    public static String getRepositoryName(String repository, String vfspath, boolean online) {

        StringBuffer result = new StringBuffer(64);
        result.append(repository);
        result.append(online ? "online" : "offline");
        result.append(vfspath);
        return result.toString();
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
     * Creates unique, valid RFS name for the given filename that contains 
     * a coded version of the given parameters, with the given file extension appended.<p>
     *    
     * This is used to create file names for the static export, 
     * or in a vfs disk cache.<p>
     * 
     * @param filename the base file name
     * @param extension the extension to use
     * @param parameters the parameters to code in the result file name
     * 
     * @return a unique, valid RFS name for the given parameters
     * 
     * @see org.opencms.staticexport.CmsStaticExportManager
     */
    public static String getRfsPath(String filename, String extension, String parameters) {

        StringBuffer buf = new StringBuffer(128);
        buf.append(filename);
        buf.append('_');
        int h = parameters.hashCode();
        // ensure we do have a positive id value
        buf.append(h > 0 ? h : -h);
        buf.append(extension);
        return buf.toString();
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
     * @param file the file to read 
     * @return the read file content
     * 
     * @throws IOException in case of file access errors
     */
    public static byte[] readFile(File file) throws IOException {

        // create input and output stream
        FileInputStream in = new FileInputStream(file);
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
     * Reads a file with the given name from the RFS and returns the file content.<p> 
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
     * Removes all resource names in the given List that are "redundant" because the parent folder name 
     * is also contained in the List.<p> 
     * 
     * The content of the input list is not modified.<p>
     * 
     * @param resourcenames a list of VFS pathnames to check for redundencies (Strings)
     *  
     * @return a the given list with all redundancies removed
     * 
     * @see #removeRedundantResources(List)
     */
    public static List removeRedundancies(List resourcenames) {

        if ((resourcenames == null) || (resourcenames.isEmpty())) {
            return new ArrayList();
        }
        if (resourcenames.size() == 1) {
            // if there is only one resource name in the list, there can be no redundancies
            return new ArrayList(resourcenames);
        }
        // check all resources names and see if a parent folder name is contained
        List result = new ArrayList(resourcenames.size());
        List base = new ArrayList(resourcenames);
        Collections.sort(base);
        Iterator i = base.iterator();
        while (i.hasNext()) {
            // check all resource names in the list
            String resourcename = (String)i.next();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(resourcename)) {
                // skip empty strings
                continue;
            }
            boolean valid = true;
            for (int j = (result.size() - 1); j >= 0; j--) {
                // check if this resource name is indirectly contained because a parent folder name is contained
                String check = (String)result.get(j);
                if (resourcename.startsWith(check)) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                // a parent folder name is not already contained in the result
                result.add(resourcename);
            }
        }
        return result;
    }

    /** 
     * Removes all resources in the given List that are "redundant" because the parent folder 
     * is also contained in the List.<p> 
     * 
     * The content of the input list is not modified.<p>
     * 
     * @param resources a list of <code>{@link CmsResource}</code> objects to check for redundencies
     *  
     * @return a the given list with all redundancies removed
     * 
     * @see #removeRedundancies(List)
     */
    public static List removeRedundantResources(List resources) {

        if ((resources == null) || (resources.isEmpty())) {
            return new ArrayList();
        }
        if (resources.size() == 1) {
            // if there is only one resource in the list, there can be no redundancies
            return new ArrayList(resources);
        }
        // check all resources and see if a parent folder name is contained
        List result = new ArrayList(resources.size());
        List base = new ArrayList(resources);
        Collections.sort(base);
        Iterator i = base.iterator();
        while (i.hasNext()) {
            // check all folders in the list
            CmsResource resource = (CmsResource)i.next();
            boolean valid = true;
            for (int j = (result.size() - 1); j >= 0; j--) {
                // check if this resource is indirectly contained because a parent folder is contained
                String check = ((CmsResource)result.get(j)).getRootPath();
                if (resource.getRootPath().startsWith(check)) {
                    valid = false;
                    break;
                }
            }
            if (valid) {
                // the parent folder is not already contained in the result
                result.add(resource);
            }
        }
        return result;
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