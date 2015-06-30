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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.file.wrapper.CmsObjectWrapper;
import org.opencms.file.wrapper.CmsWrappedResource;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;

import org.alfresco.jlan.server.filesys.AccessDeniedException;
import org.alfresco.jlan.server.filesys.FileAttribute;
import org.alfresco.jlan.server.filesys.FileInfo;
import org.alfresco.jlan.server.filesys.NetworkFile;
import org.alfresco.jlan.smb.SeekType;
import org.alfresco.jlan.util.WildCard;

/**
 * This class represents a file for use by the JLAN server component. It currently just
 * wraps an OpenCms resource.<p>
 */
public class CmsJlanNetworkFile extends NetworkFile {

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJlanNetworkFile.class);

    /** The buffer used for reading/writing file contents. */
    private CmsFileBuffer m_buffer = new CmsFileBuffer();

    /** Flag which indicates whether the buffer has been initialized. */
    private boolean m_bufferInitialized;

    /** The CMS context to use. */
    private CmsObjectWrapper m_cms;

    /** The write count after which the file was last flushed. */
    private int m_lastFlush;

    /** The wrapped resource. */
    private CmsResource m_resource;

    /** Creates a new network file instance.<p>
     *
     * @param cms the CMS object wrapper to use
     * @param resource the actual CMS resource
     * @param fullName the raw repository path
     */
    public CmsJlanNetworkFile(CmsObjectWrapper cms, CmsResource resource, String fullName) {

        super(resource.getName());
        m_resource = resource;
        m_cms = cms;
        updateFromResource();
        setFullName(normalizeName(fullName));
        setFileId(resource.getStructureId().hashCode());
    }

    /**
     * @see org.alfresco.jlan.server.filesys.NetworkFile#closeFile()
     */
    @Override
    public void closeFile() throws IOException {

        if (hasDeleteOnClose()) {
            delete();
        } else {
            flushFile();
            if (getWriteCount() > 0) {
                try {
                    m_cms.unlockResource(m_cms.getSitePath(m_resource));
                } catch (CmsException e) {
                    LOG.error("Couldn't unlock file: " + m_resource.getRootPath());
                }
            }
        }
    }

    /**
     * Deletes the file.<p>
     *
     * @throws IOException if something goes wrong
     */
    public void delete() throws IOException {

        try {
            load(false);
            ensureLock();
            m_cms.deleteResource(m_cms.getSitePath(m_resource), CmsResource.DELETE_PRESERVE_SIBLINGS);
        } catch (CmsException e) {
            throw CmsJlanDiskInterface.convertCmsException(e);
        }
    }

    /**
     * @see org.alfresco.jlan.server.filesys.NetworkFile#flushFile()
     */
    @Override
    public void flushFile() throws IOException {

        int writeCount = getWriteCount();
        try {
            if (writeCount > m_lastFlush) {
                CmsFile file = getFile();
                if (file != null) {
                    CmsWrappedResource wr = new CmsWrappedResource(file);
                    String rootPath = m_cms.getRequestContext().addSiteRoot(
                        CmsJlanDiskInterface.getCmsPath(getFullName()));
                    wr.setRootPath(rootPath);
                    file = wr.getFile();
                    file.setContents(m_buffer.getContents());
                    ensureLock();
                    m_cms.writeFile(file);
                }
            }
            m_lastFlush = writeCount;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new IOException(e);
        }

    }

    /**
     * Gets the file information record.<p>
     *
     * @return the file information for this file
     *
     * @throws IOException if reading the file information fails
     */
    public FileInfo getFileInfo() throws IOException {

        try {
            load(false);
            if (m_resource.isFile()) {

                //  Fill in a file information object for this file/directory

                long flen = m_resource.getLength();

                //long alloc = (flen + 512L) & 0xFFFFFFFFFFFFFE00L;
                long alloc = flen;
                int fattr = 0;
                if (m_cms.getRequestContext().getCurrentProject().isOnlineProject()) {
                    fattr += FileAttribute.ReadOnly;
                }
                //  Create the file information
                FileInfo finfo = new FileInfo(m_resource.getName(), flen, fattr);
                long fdate = m_resource.getDateLastModified();
                finfo.setModifyDateTime(fdate);
                finfo.setAllocationSize(alloc);
                finfo.setFileId(m_resource.getStructureId().hashCode());
                finfo.setCreationDateTime(m_resource.getDateCreated());
                finfo.setChangeDateTime(fdate);
                return finfo;
            } else {

                //  Fill in a file information object for this directory

                int fattr = FileAttribute.Directory;
                if (m_cms.getRequestContext().getCurrentProject().isOnlineProject()) {
                    fattr += FileAttribute.ReadOnly;
                }
                // Can't use negative file size here, since this stops Windows 7 from connecting
                FileInfo finfo = new FileInfo(m_resource.getName(), 1, fattr);
                long fdate = m_resource.getDateLastModified();
                finfo.setModifyDateTime(fdate);
                finfo.setAllocationSize(1);
                finfo.setFileId(m_resource.getStructureId().hashCode());
                finfo.setCreationDateTime(m_resource.getDateCreated());
                finfo.setChangeDateTime(fdate);
                return finfo;

            }
        } catch (CmsException e) {
            throw CmsJlanDiskInterface.convertCmsException(e);

        }
    }

    /**
     * Moves this file to a different path.<p>
     *
     * @param cmsNewPath the new path
     * @throws CmsException if something goes wrong
     */
    public void moveTo(String cmsNewPath) throws CmsException {

        ensureLock();
        m_cms.moveResource(m_cms.getSitePath(m_resource), cmsNewPath);
        CmsUUID id = m_resource.getStructureId();
        CmsResource updatedRes = m_cms.readResource(id, CmsJlanDiskInterface.STANDARD_FILTER);
        m_resource = updatedRes;
        updateFromResource();
    }

    /**
     * @see org.alfresco.jlan.server.filesys.NetworkFile#openFile(boolean)
     */
    @Override
    public void openFile(boolean arg0) {

        // not needed

    }

    /**
     * @see org.alfresco.jlan.server.filesys.NetworkFile#readFile(byte[], int, int, long)
     */
    @Override
    public int readFile(byte[] buffer, int length, int bufferOffset, long fileOffset) throws IOException {

        try {
            load(true);
            int result = m_buffer.read(buffer, length, bufferOffset, (int)fileOffset);
            return result;
        } catch (CmsException e) {
            throw CmsJlanDiskInterface.convertCmsException(e);
        }
    }

    /**
     * Collects all files matching the given name pattern and search attributes.<p>
     *
     * @param name the name pattern
     * @param searchAttributes the search attributes
     *
     * @return the list of file objects which match the given parameters
     *
     * @throws IOException if something goes wrong
     */
    public List<CmsJlanNetworkFile> search(String name, int searchAttributes) throws IOException {

        try {
            load(false);
            if (m_resource.isFolder()) {
                List<CmsJlanNetworkFile> result = new ArrayList<CmsJlanNetworkFile>();
                String regex = WildCard.convertToRegexp(name);
                Pattern pattern = Pattern.compile(regex);
                List<CmsResource> children = m_cms.getResourcesInFolder(
                    m_cms.getSitePath(m_resource),
                    CmsJlanDiskInterface.STANDARD_FILTER);
                for (CmsResource child : children) {
                    CmsJlanNetworkFile childFile = new CmsJlanNetworkFile(m_cms, child, getFullChildPath(child));
                    if (!matchesSearchAttributes(searchAttributes)) {
                        continue;
                    }
                    if (!pattern.matcher(child.getName()).matches()) {
                        continue;
                    }

                    result.add(childFile);
                }
                return result;
            } else {
                throw new AccessDeniedException("Can't search a non-directory!");
            }
        } catch (CmsException e) {
            throw CmsJlanDiskInterface.convertCmsException(e);
        }
    }

    /**
     * @see org.alfresco.jlan.server.filesys.NetworkFile#seekFile(long, int)
     */
    @Override
    public long seekFile(long pos, int typ) throws IOException {

        try {
            load(true);
            switch (typ) {

                //  From current position

                case SeekType.CurrentPos:
                    m_buffer.seek(m_buffer.getPosition() + pos);
                    break;

                //  From end of file

                case SeekType.EndOfFile:
                    long newPos = m_buffer.getLength() + pos;
                    m_buffer.seek(newPos);
                    break;

                //  From start of file

                case SeekType.StartOfFile:
                default:
                    m_buffer.seek(pos);
                    break;
            }
            return m_buffer.getPosition();
        } catch (CmsException e) {
            throw new IOException(e);
        }
    }

    /**
     * Sets the file information.<p>
     *
     * @param info the file information to set
     */
    public void setFileInformation(FileInfo info) {

        if (info.hasSetFlag(FileInfo.FlagDeleteOnClose)) {
            setDeleteOnClose(true);
        }
    }

    /**
     * @see org.alfresco.jlan.server.filesys.NetworkFile#truncateFile(long)
     */
    @Override
    public void truncateFile(long size) throws IOException {

        try {
            load(true);
            m_buffer.truncate((int)size);
            incrementWriteCount();
        } catch (CmsException e) {
            throw CmsJlanDiskInterface.convertCmsException(e);
        }
    }

    /**
     * @see org.alfresco.jlan.server.filesys.NetworkFile#writeFile(byte[], int, int, long)
     */
    @Override
    public void writeFile(byte[] data, int len, int pos, long offset) throws IOException {

        try {
            if (m_resource.isFolder()) {
                throw new AccessDeniedException("Can't write data to folder!");
            }
            load(true);
            m_buffer.seek(offset);
            byte[] dataToWrite = Arrays.copyOfRange(data, pos, pos + len);
            m_buffer.write(dataToWrite);
            incrementWriteCount();
        } catch (CmsException e) {
            throw CmsJlanDiskInterface.convertCmsException(e);
        }
    }

    /**
     * Make sure that this resource is locked.<p>
     *
     * @throws CmsException if something goes wrong
     */
    protected void ensureLock() throws CmsException {

        CmsLock lock = m_cms.getLock(m_resource);
        if (lock.isUnlocked() || !lock.isLockableBy(m_cms.getRequestContext().getCurrentUser())) {
            m_cms.lockResourceTemporary(m_cms.getSitePath(m_resource));
        }
    }

    /**
     * Gets the CmsFile instance for this file, or null if the file contents haven'T been loaded already.<p>
     *
     * @return the CmsFile instance
     */
    protected CmsFile getFile() {

        if (m_resource instanceof CmsFile) {
            return (CmsFile)m_resource;
        }
        return null;
    }

    /**
     * Adds the name of a child resource to this file's path.<p>
     *
     * @param child the child resource
     *
     * @return the path of the child
     */
    protected String getFullChildPath(CmsResource child) {

        String childName = child.getName();
        String sep = getFullName().endsWith("\\") ? "" : "\\";
        return getFullName() + sep + childName;
    }

    /**
     * Loads the file data from the VFS.<p>
     *
     * @param needContent true if we need the file content to be loaded
     *
     * @throws IOException if an IO error happens
     * @throws CmsException if a CMS operation fails
     */
    protected void load(boolean needContent) throws IOException, CmsException {

        try {
            if (m_resource.isFolder() && needContent) {
                throw new AccessDeniedException("Operation not supported for directories!");
            }
            if (m_resource.isFile() && needContent && (!(m_resource instanceof CmsFile))) {
                m_resource = m_cms.readFile(m_cms.getSitePath(m_resource), CmsJlanDiskInterface.STANDARD_FILTER);
            }
            if (!m_bufferInitialized && (getFile() != null)) {
                // readResource may already have returned a CmsFile, this is why we need to initialize the buffer
                // here and not in the if-block above
                m_buffer.init(getFile().getContents());
                m_bufferInitialized = true;
            }
        } catch (CmsException e) {
            throw e;
        }
    }

    /**
     * Checks if this file matches the given search attributes.<p>
     *
     * @param attributes the search attributes
     *
     * @return true if this file matches the search attributes given
     */
    protected boolean matchesSearchAttributes(int attributes) {

        if (isDirectory()) {
            return (attributes & FileAttribute.Directory) != 0;
        } else {
            return true;
        }
    }

    /**
     * Copies state information from the internal CmsResource object to this object.<p>
     */
    protected void updateFromResource() {

        setCreationDate(m_resource.getDateCreated());
        int length = m_resource.getLength();
        if (m_resource.isFolder()) {
            length = 1;
        }
        setFileSize(length);
        setModifyDate(m_resource.getDateLastModified());
        setAttributes(m_resource.isFile() ? FileAttribute.Normal : FileAttribute.Directory);
    }

    /**
     * Replace sequences of consecutive slashes/backslashes to a single backslash.<p>
     *
     * @param fullName the path to normalize
     * @return the normalized path
     */
    private String normalizeName(String fullName) {

        return fullName.replaceAll("[/\\\\]+", "\\\\");
    }

}
