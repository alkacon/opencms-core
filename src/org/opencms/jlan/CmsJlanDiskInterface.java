/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.jlan;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceAlreadyExistsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.wrapper.CmsObjectWrapper;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.I_CmsRegexSubstitution;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import org.alfresco.jlan.server.SrvSession;
import org.alfresco.jlan.server.core.DeviceContext;
import org.alfresco.jlan.server.filesys.AccessDeniedException;
import org.alfresco.jlan.server.filesys.DiskInterface;
import org.alfresco.jlan.server.filesys.FileExistsException;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.FileOpenParams;
import org.alfresco.jlan.server.filesys.FileStatus;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.server.filesys.SearchContext;
import org.alfresco.jlan.server.filesys.TreeConnection;
import org.alfresco.jlan.util.WildCard;
import org.springframework.extensions.config.ConfigElement;

import com.google.common.base.Joiner;

/**
 * OpenCms implementation of the JLAN DiskInterface interface.<p>
 * 
 * This class, together with the CmsJlanNetworkFile class, contains the main repository access functionality.<p>
 */
public class CmsJlanDiskInterface implements DiskInterface {

    /** The standard resource filter used for reading resources. */
    public static final CmsResourceFilter STANDARD_FILTER = CmsResourceFilter.ONLY_VISIBLE_NO_DELETED;

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJlanDiskInterface.class);

    /** 
     * Tries to convert a CmsException to the matching exception type from JLAN.<p>
     * 
     * @param e the exception to convert 
     * @return the converted exception 
     */
    public static IOException convertCmsException(CmsException e) {

        LOG.error(e.getLocalizedMessage(), e);
        if (e instanceof CmsSecurityException) {
            return new AccessDeniedException(e.getMessage(), e);
        } else if (e instanceof CmsVfsResourceAlreadyExistsException) {
            return new FileExistsException("File exists: " + e);
        } else if (e instanceof CmsVfsResourceNotFoundException) {
            return new FileNotFoundException("File does not exist: " + e);
        } else {
            return new IOException(e);
        }
    }

    /**
     * Converts a CIFS path to an OpenCms path by converting backslashes to slashes and translating special characters in the file name.<p>
     * 
     * @param path the path to transform 
     * @return the OpenCms path for the given path 
     */
    protected static String getCmsPath(String path) {

        String slashPath = path.replace('\\', '/');

        // split path into components, translate each of them separately, then combine them again at the end 
        String[] segments = slashPath.split("/");
        List<String> nonEmptySegments = new ArrayList<String>();
        for (String segment : segments) {
            if (segment.length() > 0) {
                String translatedSegment = OpenCms.getResourceManager().getFileTranslator().translateResource(segment);
                nonEmptySegments.add(translatedSegment);
            }
        }
        String result = "/" + Joiner.on("/").join(nonEmptySegments);
        return result;
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#closeFile(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, org.alfresco.jlan.server.filesys.NetworkFile)
     */
    public void closeFile(SrvSession session, TreeConnection connection, NetworkFile file) throws IOException {

        file.close();
    }

    /**
     * @see org.alfresco.jlan.server.core.DeviceInterface#createContext(java.lang.String, org.springframework.extensions.config.ConfigElement)
     */
    public DeviceContext createContext(String shareName, ConfigElement args) {

        return null; // not used, since the repository creates the device context 

    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#createDirectory(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, org.alfresco.jlan.server.filesys.FileOpenParams)
     */
    public void createDirectory(SrvSession session, TreeConnection connection, FileOpenParams params)
    throws IOException {

        internalCreateFile(session, connection, params, "folder");
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#createFile(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, org.alfresco.jlan.server.filesys.FileOpenParams)
     */
    public NetworkFile createFile(SrvSession session, TreeConnection connection, FileOpenParams params)
    throws IOException {

        return internalCreateFile(session, connection, params, null);
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#deleteDirectory(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, java.lang.String)
     */
    public void deleteDirectory(SrvSession session, TreeConnection connection, String path) throws IOException {

        deleteFile(session, connection, path);
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#deleteFile(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, java.lang.String)
     */
    public void deleteFile(SrvSession session, TreeConnection connection, String path) throws IOException {

        // note: deletion of a file may not necessarily go through this method, instead the client program may open the 
        // file, set a "delete on close" flag, and then close it.
        try {
            CmsJlanNetworkFile file = getFileForPath(session, connection, path);
            if (file == null) {
                // Only log a warning, since if the file doesn't exist, it doesn't really need to be deleted anymore 
                LOG.warn("Couldn't delete file " + path + " because it doesn't exist anymore.");
            } else {
                file.delete();
            }
        } catch (CmsException e) {
            throw convertCmsException(e);

        }
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#fileExists(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, java.lang.String)
     */
    public int fileExists(SrvSession session, TreeConnection connection, String path) {

        try {
            CmsJlanNetworkFile file = getFileForPath(session, connection, path);
            if (file == null) {
                return FileStatus.NotExist;
            } else {
                return file.isDirectory() ? FileStatus.DirectoryExists : FileStatus.FileExists;
            }
        } catch (Exception e) {
            System.out.println(e);
            return FileStatus.NotExist;
        }
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#flushFile(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, org.alfresco.jlan.server.filesys.NetworkFile)
     */
    public void flushFile(SrvSession session, TreeConnection connection, NetworkFile file) throws IOException {

        file.flushFile();

    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#getFileInformation(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, java.lang.String)
     */
    public FileInfo getFileInformation(SrvSession session, TreeConnection connection, String path) throws IOException {

        try {
            if (path == null) {
                throw new FileNotFoundException("file not found: " + path);
            }
            CmsJlanNetworkFile file = getFileForPath(session, connection, path);
            if (file == null) {
                return null;
                //throw new FileNotFoundException("path not found: " + path);
            } else {
                return file.getFileInfo();
            }
        } catch (CmsException e) {
            throw convertCmsException(e);
        }
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#isReadOnly(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.core.DeviceContext)
     */
    public boolean isReadOnly(SrvSession session, DeviceContext context) {

        return false;
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#openFile(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, org.alfresco.jlan.server.filesys.FileOpenParams)
     */
    public NetworkFile openFile(SrvSession session, TreeConnection connection, FileOpenParams params)
    throws IOException {

        String path = params.getPath();
        String cmsPath = getCmsPath(path);
        // TODO: Check access control
        try {
            CmsObjectWrapper cms = getCms(session, connection);
            CmsResource resource = cms.readResource(cmsPath, STANDARD_FILTER);

            return new CmsJlanNetworkFile(cms, resource, path);
        } catch (CmsException e) {
            throw convertCmsException(e);
        }

    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#readFile(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, org.alfresco.jlan.server.filesys.NetworkFile, byte[], int, int, long)
     */
    public int readFile(
        SrvSession sess,
        TreeConnection tree,
        NetworkFile file,
        byte[] buf,
        int bufPos,
        int siz,
        long filePos) throws java.io.IOException {

        //    Check if the file is a directory

        if (file.isDirectory()) {
            throw new AccessDeniedException();
        }

        //  Read the file

        int rdlen = file.readFile(buf, siz, bufPos, filePos);

        //  If we have reached end of file return a zero length read

        if (rdlen < 0) {
            rdlen = 0;
        }

        //  Return the actual read length

        return rdlen;
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#renameFile(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, java.lang.String, java.lang.String)
     */
    public void renameFile(SrvSession session, TreeConnection connection, String oldName, String newName)
    throws IOException {

        String cmsNewPath = getCmsPath(newName);
        try {
            CmsJlanNetworkFile file = getFileForPath(session, connection, oldName);
            file.moveTo(cmsNewPath);
        } catch (CmsException e) {
            throw convertCmsException(e);
        }
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#seekFile(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, org.alfresco.jlan.server.filesys.NetworkFile, long, int)
     */
    public long seekFile(SrvSession session, TreeConnection connection, NetworkFile file, long pos, int seekMode)
    throws IOException {

        return file.seekFile(pos, seekMode);
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#setFileInformation(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, java.lang.String, org.alfresco.jlan.server.filesys.FileInfo)
     */
    public void setFileInformation(SrvSession session, TreeConnection connection, String path, FileInfo info)
    throws IOException {

        try {
            CmsObjectWrapper cms = getCms(session, connection);
            String cmsPath = getCmsPath(path);
            CmsResource resource = cms.readResource(cmsPath, STANDARD_FILTER);
            CmsJlanNetworkFile file = new CmsJlanNetworkFile(cms, resource, path);
            file.setFileInformation(info);
        } catch (CmsException e) {
            throw convertCmsException(e);
        }
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#startSearch(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, java.lang.String, int)
     */
    public SearchContext startSearch(
        SrvSession session,
        TreeConnection connection,
        String searchPath,
        int searchAttributes) {

        try {

            String cmsPath = getCmsPath(searchPath);
            if (cmsPath.endsWith("/")) {
                cmsPath = cmsPath + "*";
            }
            String name = CmsResource.getName(cmsPath);
            String parent = CmsResource.getParentFolder(cmsPath);

            if (WildCard.containsWildcards(name)) {
                CmsJlanNetworkFile parentFile = getFileForPath(session, connection, parent);
                return new CmsJlanSearch(parentFile.search(name, searchAttributes));
            } else {
                CmsJlanNetworkFile file = getFileForPath(session, connection, cmsPath);
                return new CmsJlanSearch(Collections.singletonList(file));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.alfresco.jlan.server.core.DeviceInterface#treeClosed(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection)
     */
    public void treeClosed(SrvSession sess, TreeConnection tree) {

        // ignore

    }

    /**
     * @see org.alfresco.jlan.server.core.DeviceInterface#treeOpened(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection)
     */
    public void treeOpened(SrvSession arg0, TreeConnection arg1) {

        // ignore 
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#truncateFile(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, org.alfresco.jlan.server.filesys.NetworkFile, long)
     */
    public void truncateFile(SrvSession session, TreeConnection connection, NetworkFile file, long size)
    throws IOException {

        file.truncateFile(size);
    }

    /**
     * @see org.alfresco.jlan.server.filesys.DiskInterface#writeFile(org.alfresco.jlan.server.SrvSession, org.alfresco.jlan.server.filesys.TreeConnection, org.alfresco.jlan.server.filesys.NetworkFile, byte[], int, int, long)
     */
    public int writeFile(
        SrvSession session,
        TreeConnection connection,
        NetworkFile file,
        byte[] data,
        int bufferOffset,
        int length,
        long fileOffset) throws IOException {

        if (file.isDirectory()) {
            throw new AccessDeniedException("Can't write data to a directory!");
        }
        file.writeFile(data, length, bufferOffset, fileOffset);
        return length;
    }

    /**
     * Creates a CmsObjectWrapper for the current session.<p>
     * 
     * @param session the current session 
     * @param connection the tree connection 
     * 
     * @return the correctly configured CmsObjectWrapper for this session 
     *  
     * @throws CmsException if something goes wrong 
     */
    protected CmsObjectWrapper getCms(SrvSession session, TreeConnection connection) throws CmsException {

        CmsJlanRepository repository = ((CmsJlanDeviceContext)connection.getContext()).getRepository();
        CmsObjectWrapper result = repository.getCms(session, connection);
        return result;
    }

    /**
     * Helper method to get a network file object given a path.<p>
     * 
     * @param session the current session 
     * @param connection the current connection 
     * @param path the file path 
     * 
     * @return the network file object for the given path 
     * @throws CmsException if something goes wrong
     */
    protected CmsJlanNetworkFile getFileForPath(SrvSession session, TreeConnection connection, String path)
    throws CmsException {

        try {
            CmsObjectWrapper cms = getCms(session, connection);
            String cmsPath = getCmsPath(path);
            CmsResource resource = cms.readResource(cmsPath, STANDARD_FILTER);
            CmsJlanNetworkFile result = new CmsJlanNetworkFile(cms, resource, path);
            return result;
        } catch (CmsVfsResourceNotFoundException e) {
            return null;
        }
    }

    /**
     * Internal method for creating a new file.<p>
     *  
     * @param session the session 
     * @param connection the tree connection 
     * @param params the parameters for opening the file 
     * @param typeName the name of the resource type for the new file 
     * 
     * @return a NetworkFile instance representing the newly created file 
     * 
     * @throws IOException if something goes wrong 
     */
    protected NetworkFile internalCreateFile(
        SrvSession session,
        TreeConnection connection,
        FileOpenParams params,
        String typeName) throws IOException {

        String path = params.getPath();
        String cmsPath = getCmsPath(path);
        try {
            CmsObjectWrapper cms = getCms(session, connection);
            if (typeName == null) {
                typeName = OpenCms.getResourceManager().getDefaultTypeForName(cmsPath).getTypeName();
            }
            CmsResource createdResource = cms.createResource(
                cmsPath,
                OpenCms.getResourceManager().getResourceType(typeName).getTypeId());
            tryUnlock(cms, cmsPath);
            CmsJlanNetworkFile result = new CmsJlanNetworkFile(cms, createdResource, path);
            result.setFullName(params.getPath());
            return result;
        } catch (CmsVfsResourceAlreadyExistsException e) {
            throw new FileExistsException("File exists: " + path);
        } catch (CmsException e) {
            throw new IOException(e);
        }

    }

    /**
     * Translates the last path segment of a path using the configured OpenCms file translations.<p>
     * 
     * @param path the path for which the last segment should be translated 
     * 
     * @return the path with the translated last segment 
     */
    protected String translateName(String path) {

        return CmsStringUtil.substitute(Pattern.compile("/([^/]+)$"), path, new I_CmsRegexSubstitution() {

            public String substituteMatch(String text, Matcher matcher) {

                String name = text.substring(matcher.start(1), matcher.end(1));
                return "/" + OpenCms.getResourceManager().getFileTranslator().translateResource(name);
            }
        });
    }

    /**
     * Tries to unlock the file at the given path.<p>
     * 
     * @param cms the CMS context wrapper 
     * @param path the path of the resource to unlock
     */
    private void tryUnlock(CmsObjectWrapper cms, String path) {

        try {
            cms.unlockResource(path);
        } catch (Throwable e) {
            LOG.info(e.getLocalizedMessage(), e);
        }

    }

}
