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

package org.opencms.util;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsResource;
import org.opencms.flex.CmsFlexCache;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsSystemInfo;
import org.opencms.staticexport.CmsLinkManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

/**
 * Provides File utility functions.<p>
 * 
 * @since 6.0.0 
 */
public final class CmsFileUtil {

    /**
     * Hides the public constructor.<p> 
     */
    private CmsFileUtil() {

        // empty
    }

    /**
     * Adds a trailing separator to a path if required.<p>
     * 
     * @param path the path to add the trailing separator to
     * @return the path with a trailing separator
     */
    public static String addTrailingSeparator(String path) {

        int l = path.length();
        if ((l == 0) || (path.charAt(l - 1) != '/')) {
            return path.concat("/");
        } else {
            return path;
        }
    }

    /**
     * Checks if all resources are present.<p>
     * 
     * @param cms an initialized OpenCms user context which must have read access to all resources
     * @param resources a list of vfs resource names to check
     * 
     * @throws CmsIllegalArgumentException in case not all resources exist or can be read with the given OpenCms user context
     */
    public static void checkResources(CmsObject cms, List<String> resources) throws CmsIllegalArgumentException {

        StringBuffer result = new StringBuffer(128);
        ListIterator<String> it = resources.listIterator();
        while (it.hasNext()) {
            String resourcePath = it.next();
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

        // transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
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
            result = Messages.get().getBundle(locale).key(Messages.GUI_FILEUTIL_FILESIZE_BYTES_1, new Long(filesize));
        } else if (Math.abs(filesize) < 1048576) {
            // 1048576 = 1024.0 * 1024.0
            result = Messages.get().getBundle(locale).key(
                Messages.GUI_FILEUTIL_FILESIZE_KBYTES_1,
                new Double(filesize / 1024.0));
        } else if (Math.abs(filesize) < 1073741824) {
            // 1024.0^3 =  1073741824
            result = Messages.get().getBundle(locale).key(
                Messages.GUI_FILEUTIL_FILESIZE_MBYTES_1,
                new Double(filesize / 1048576.0));
        } else {
            result = Messages.get().getBundle(locale).key(
                Messages.GUI_FILEUTIL_FILESIZE_GBYTES_1,
                new Double(filesize / 1073741824.0));
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
    public static String formatResourceNames(CmsRequestContext context, List<CmsResource> resources) {

        if (resources == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(128);
        Iterator<CmsResource> i = resources.iterator();
        while (i.hasNext()) {
            CmsResource res = i.next();
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
     * Returns the extension of the given resource name, that is the part behind the last '.' char,
     * converted to lower case letters.<p>
     * 
     * The extension of a file is the part of the name after the last dot, including the dot.
     * The extension of a folder is empty.
     * All extensions are returned as lower case<p>
     * 
     * Please note: No check is performed to ensure the given file name is not <code>null</code>.<p>
     * 
     * Examples:<br> 
     * <ul>
     *   <li><code>/folder.test/</code> has an empty extension.
     *   <li><code>/folder.test/config</code> has an empty extension.
     *   <li><code>/strange.filename.</code> has an empty extension.
     *   <li><code>/document.PDF</code> has the extension <code>.pdf</code>.
     * </ul>
     * 
     * @param resourceName the resource to get the extension for
     * 
     * @return the extension of a resource
     */
    public static String getExtension(String resourceName) {

        // if the resource name indicates a folder
        if (resourceName.charAt(resourceName.length() - 1) == '/') {
            // folders have no extensions
            return "";
        }
        // get just the name of the resource
        String name = CmsResource.getName(resourceName);
        // get the position of the last dot
        int pos = name.lastIndexOf('.');
        // if no dot or if no chars after the dot
        if ((pos < 0) || ((pos + 1) == name.length())) {
            return "";
        }
        // return the extension
        return name.substring(pos).toLowerCase();
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
     * 
     * @deprecated use {@link #getExtension(String)} instead, it is better implemented
     */
    @Deprecated
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
    public static List<File> getFiles(String name, FileFilter filter, boolean includeSubtree) {

        List<File> ret = new ArrayList<File>();

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
        result.append(online ? CmsFlexCache.REPOSITORY_ONLINE : CmsFlexCache.REPOSITORY_OFFLINE);
        result.append(vfspath);
        return result.toString();
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
     * Normalizes a file path that might contain <code>'../'</code> or <code>'./'</code> or <code>'//'</code> 
     * elements to a normal absolute path, the path separator char used is {@link File#separatorChar}.<p>
     * 
     * @param path the path to normalize
     * 
     * @return the normalized path
     * 
     * @see #normalizePath(String, char)
     */
    public static String normalizePath(String path) {

        return normalizePath(path, File.separatorChar);
    }

    /**
     * Normalizes a file path that might contain <code>'../'</code> or <code>'./'</code> or <code>'//'</code> 
     * elements to a normal absolute path.<p>
     * 
     * Can also handle Windows like path information containing a drive letter, 
     * like <code>C:\path\..\</code>.<p>
     * 
     * @param path the path to normalize
     * @param separatorChar the file separator char to use, for example {@link File#separatorChar}
     * 
     * @return the normalized path
     */
    public static String normalizePath(String path, char separatorChar) {

        if (CmsStringUtil.isNotEmpty(path)) {

            // handle windows paths including drive-information first
            String drive = null;
            if ((path.length() > 1) && (path.charAt(1) == ':')) {
                // windows path like C:\home\
                drive = path.substring(0, 2);
                path = path.substring(2);
            } else if ((path.length() > 1) && (path.charAt(0) == '\\') && (path.charAt(1) == '\\')) {
                // windows path like \\home\ (network mapped drives)
                drive = path.substring(0, 2);
                path = path.substring(2);
            }
            // ensure all File separators are '/'
            path = path.replace('\\', '/');
            if (drive != null) {
                drive = drive.replace('\\', '/');
            }
            if (path.charAt(0) == '/') {
                // trick to resolve all ../ inside a path
                path = '.' + path;
            }
            // resolve all '../' or './' elements in the path
            path = CmsLinkManager.getAbsoluteUri(path, "/");
            // still some '//' elements might persist
            path = CmsStringUtil.substitute(path, "//", "/");
            // re-append drive if required
            if (drive != null) {
                path = drive.concat(path);
            }
            // switch '/' back to OS dependend File separator if required
            if (separatorChar != '/') {
                path = path.replace('/', separatorChar);
            }
        }
        return path;
    }

    /**
     * Returns the normalized file path created from the given URL.<p>
     * 
     * The path part {@link URL#getPath()} is used, unescaped and 
     * normalized using {@link #normalizePath(String, char)} using {@link File#separatorChar}.<p>
     * 
     * @param url the URL to extract the path information from
     * 
     * @return the normalized file path created from the given URL using {@link File#separatorChar}
     * 
     * @see #normalizePath(URL, char)
     */
    public static String normalizePath(URL url) {

        return normalizePath(url, File.separatorChar);
    }

    /**
     * Returns the normalized file path created from the given URL.<p>
     * 
     * The path part {@link URL#getPath()} is used, unescaped and 
     * normalized using {@link #normalizePath(String, char)}.<p>
     * 
     * @param url the URL to extract the path information from
     * @param separatorChar the file separator char to use, for example {@link File#separatorChar}
     * 
     * @return the normalized file path created from the given URL
     */
    public static String normalizePath(URL url, char separatorChar) {

        // get the path part from the URL
        String path = new File(url.getPath()).getAbsolutePath();
        // trick to get the OS default encoding, taken from the official Java i18n FAQ
        String systemEncoding = (new OutputStreamWriter(new ByteArrayOutputStream())).getEncoding();
        // decode url in order to remove spaces and escaped chars from path
        return CmsFileUtil.normalizePath(CmsEncoder.decode(path, systemEncoding), separatorChar);
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

        // read the content
        return readFully(in, (int)file.length());
    }

    /**
     * Reads a file with the given name from the class loader and returns the file content.<p> 
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

        return readFully(in);
    }

    /**
     * Reads a file from the class loader and converts it to a String with the specified encoding.<p> 
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
     * Reads all bytes from the given input stream, closes it
     * and returns the result in an array.<p> 
     * 
     * @param in the input stream to read the bytes from 
     * @return the byte content of the input stream
     * 
     * @throws IOException in case of errors in the underlying java.io methods used
     */
    public static byte[] readFully(InputStream in) throws IOException {

        return readFully(in, true);
    }

    /**
     * Reads all bytes from the given input stream, conditionally closes the given input stream 
     * and returns the result in an array.<p> 
     * 
     * @param in the input stream to read the bytes from 
     * @return the byte content of the input stream
     * @param closeInputStream if true the given stream will be closed afterwards
     * 
     * @throws IOException in case of errors in the underlying java.io methods used
     */
    public static byte[] readFully(InputStream in, boolean closeInputStream) throws IOException {

        if (in instanceof ByteArrayInputStream) {
            // content can be read in one pass
            return readFully(in, in.available(), closeInputStream);
        }

        // copy buffer
        byte[] xfer = new byte[2048];
        // output buffer
        ByteArrayOutputStream out = new ByteArrayOutputStream(xfer.length);

        // transfer data from input to output in xfer-sized chunks.
        for (int bytesRead = in.read(xfer, 0, xfer.length); bytesRead >= 0; bytesRead = in.read(xfer, 0, xfer.length)) {
            if (bytesRead > 0) {
                out.write(xfer, 0, bytesRead);
            }
        }
        if (closeInputStream) {
            in.close();
        }
        out.close();
        return out.toByteArray();
    }

    /**
     * Reads the specified number of bytes from the given input stream and returns the result in an array.<p> 
     * 
     * @param in the input stream to read the bytes from
     * @param size the number of bytes to read 
     *  
     * @return the byte content read from the input stream
     * 
     * @throws IOException in case of errors in the underlying java.io methods used
     */
    public static byte[] readFully(InputStream in, int size) throws IOException {

        return readFully(in, size, true);
    }

    /**
     * Reads the specified number of bytes from the given input stream, conditionally closes the stream 
     * and returns the result in an array.<p> 
     * 
     * @param in the input stream to read the bytes from
     * @param size the number of bytes to read 
     * @param closeStream if true the given stream will be closed 
     *  
     * @return the byte content read from the input stream
     * 
     * @throws IOException in case of errors in the underlying java.io methods used
     */
    public static byte[] readFully(InputStream in, int size, boolean closeStream) throws IOException {

        // create the byte array to hold the data
        byte[] bytes = new byte[size];

        // read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < size) {
            numRead = in.read(bytes, offset, size - offset);
            if (numRead >= 0) {
                offset += numRead;
            } else {
                break;
            }
        }

        // close the input stream and return bytes
        if (closeStream) {
            in.close();
        }

        // ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not read requested " + size + " bytes from input stream");
        }

        return bytes;
    }

    /** 
     * Removes all resource names in the given List that are "redundant" because the parent folder name 
     * is also contained in the List.<p> 
     * 
     * The content of the input list is not modified.<p>
     * 
     * @param resourcenames a list of VFS pathnames to check for redundencies (Strings)
     *  
     * @return a new list with all redundancies removed
     * 
     * @see #removeRedundantResources(List)
     */
    public static List<String> removeRedundancies(List<String> resourcenames) {

        if ((resourcenames == null) || (resourcenames.isEmpty())) {
            return new ArrayList<String>();
        }
        if (resourcenames.size() == 1) {
            // if there is only one resource name in the list, there can be no redundancies
            return new ArrayList<String>(resourcenames);
        }
        // check all resources names and see if a parent folder name is contained
        List<String> result = new ArrayList<String>(resourcenames.size());
        List<String> base = new ArrayList<String>(resourcenames);
        Collections.sort(base);
        Iterator<String> i = base.iterator();
        while (i.hasNext()) {
            // check all resource names in the list
            String resourcename = i.next();
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(resourcename)) {
                // skip empty strings
                continue;
            }
            boolean valid = true;
            for (int j = (result.size() - 1); j >= 0; j--) {
                // check if this resource name is indirectly contained because a parent folder name is contained
                String check = result.get(j);
                if ((CmsResource.isFolder(check) && resourcename.startsWith(check)) || resourcename.equals(check)) {
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
     * @param resources a list of <code>{@link CmsResource}</code> objects to check for redundancies
     *  
     * @return a the given list with all redundancies removed
     * 
     * @see #removeRedundancies(List)
     */
    public static List<CmsResource> removeRedundantResources(List<CmsResource> resources) {

        if ((resources == null) || (resources.isEmpty())) {
            return new ArrayList<CmsResource>();
        }
        if (resources.size() == 1) {
            // if there is only one resource in the list, there can be no redundancies
            return new ArrayList<CmsResource>(resources);
        }
        // check all resources and see if a parent folder name is contained
        List<CmsResource> result = new ArrayList<CmsResource>(resources.size());
        List<CmsResource> base = new ArrayList<CmsResource>(resources);
        Collections.sort(base);
        Iterator<CmsResource> i = base.iterator();
        while (i.hasNext()) {
            // check all folders in the list
            CmsResource resource = i.next();
            boolean valid = true;
            for (int j = (result.size() - 1); j >= 0; j--) {
                // check if this resource is indirectly contained because a parent folder is contained
                CmsResource check = result.get(j);
                if ((check.isFolder() && resource.getRootPath().startsWith(check.getRootPath()))
                    || resource.getRootPath().equals(check.getRootPath())) {
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
     * Removes a trailing separator from a path if required.<p>
     * 
     * In case we have the root folder "/", the separator is not removed.<p>
     * 
     * @param path the path to remove the trailing separator from
     * @return the path without a trailing separator
     */
    public static String removeTrailingSeparator(String path) {

        int l = path.length();
        if ((l <= 1) || (path.charAt(l - 1) != '/')) {
            return path;
        } else {
            return path.substring(0, l - 1);
        }
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

        File configFile = new File(f, CmsSystemInfo.FILE_TLD);
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