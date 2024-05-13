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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsSecurityException;
import org.opencms.util.CmsFileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.logging.Log;

/**
 * Allows to import resources from the filesystem or a ZIP file into the OpenCms VFS.<p>
 *
 * @since 6.0.0
 */
public class CmsImportFolder {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsImportFolder.class);

    /** The OpenCms context object that provides the permissions. */
    private CmsObject m_cms;

    /** The names of resources that have been created or replaced during the import. */
    private List<CmsResource> m_importedResources = new ArrayList<CmsResource>();

    /** The name of the import folder to load resources from. */
    private String m_importFolderName;

    /** The import path in the OpenCms VFS. */
    private String m_importPath;

    /** The resource (folder or ZIP file) to import from in the real file system. */
    private File m_importResource;

    /** Will be true if the import resource is a valid ZIP file. */
    private boolean m_validZipFile;

    /** The import resource ZIP to load resources from. */
    private ZipFile m_zipFile;

    /**
     * Default Constructor.<p>
     */
    public CmsImportFolder() {

        // noop
    }

    /**
     * Constructor for a new CmsImportFolder that will read from a ZIP file.<p>
     *
     * @param content the zip file to import
     * @param importPath the path to the OpenCms VFS to import to
     * @param cms a OpenCms context to provide the permissions
     * @param noSubFolder if <code>true</code> no sub folders will be created, if <code>false</code> the content of the
     * zip file is created 1:1 inclusive sub folders
     *
     * @throws CmsException if something goes wrong
     */
    public CmsImportFolder(byte[] content, String importPath, CmsObject cms, boolean noSubFolder)
    throws CmsException {

        importZip(content, importPath, cms, noSubFolder);
    }

    /**
     * Constructor for a new CmsImportFolder that will read from the real file system.<p>
     *
     * @param importFolderName the folder to import
     * @param importPath the path to the OpenCms VFS to import to
     * @param cms a OpenCms context to provide the permissions
     * @throws CmsException if something goes wrong
     */
    public CmsImportFolder(String importFolderName, String importPath, CmsObject cms)
    throws CmsException {

        importFolder(importFolderName, importPath, cms);
    }

    /**
     * Returns the list of imported resources.<p>
     *
     * @return the list of imported resources
     */
    public List<CmsResource> getImportedResources() {

        return m_importedResources;
    }

    /**
     * Import that will read from the real file system.<p>
     *
     * @param importFolderName the folder to import
     * @param importPath the path to the OpenCms VFS to import to
     * @param cms a OpenCms context to provide the permissions
     * @throws CmsException if something goes wrong
     */
    public void importFolder(String importFolderName, String importPath, CmsObject cms) throws CmsException {

        try {
            m_importedResources = new ArrayList<CmsResource>();
            m_importFolderName = importFolderName;
            m_importPath = importPath;
            m_cms = cms;
            // open the import resource
            getImportResource();
            // first lock the destination path
            m_cms.lockResource(m_importPath);
            // import the resources
            if (m_zipFile == null) {
                importResources(m_importResource, m_importPath);
            } else {
                importZipResource(m_zipFile, m_importPath, false);
            }
            // all is done, unlock the resources
            m_cms.unlockResource(m_importPath);
        } catch (Exception e) {
            throw new CmsVfsException(
                Messages.get().container(Messages.ERR_IMPORT_FOLDER_2, importFolderName, importPath),
                e);
        } finally {
            if (m_zipFile != null) {
                try {
                    m_zipFile.close();
                } catch (Exception e) {
                    LOG.info(e.getLocalizedMessage(), e);
                }
            }
        }

    }

    /**
     * Import that will read from a ZIP file.<p>
     *
     * @param content the zip file to import
     * @param importPath the path to the OpenCms VFS to import to
     * @param cms a OpenCms context to provide the permissions
     * @param noSubFolder if <code>true</code> no sub folders will be created, if <code>false</code> the content of the
     * zip file is created 1:1 inclusive sub folders
     *
     * @throws CmsException if something goes wrong
     */
    public void importZip(byte[] content, String importPath, CmsObject cms, boolean noSubFolder) throws CmsException {

        m_importPath = importPath;
        m_cms = cms;
        try {
            m_zipFile = ZipFile.builder().setByteArray(content).setBufferSize(65536).get();
            m_cms.readFolder(importPath, CmsResourceFilter.IGNORE_EXPIRATION);
            // import the resources
            importZipResource(m_zipFile, m_importPath, noSubFolder);
        } catch (Exception e) {
            throw new CmsVfsException(Messages.get().container(Messages.ERR_IMPORT_FOLDER_1, importPath), e);
        } finally {
            if (m_zipFile != null) {
                try {
                    m_zipFile.close();
                } catch (Exception e) {
                    LOG.info(e.getLocalizedMessage());
                }
            }
        }

    }

    /**
     * Returns true if a valid ZIP file was imported.<p>
     *
     * @return true if a valid ZIP file was imported
     */
    public boolean isValidZipFile() {

        return m_validZipFile;
    }

    /**
     * Stores the import resource in an Object member variable.<p>
     * @throws CmsVfsException if the file to import is no valid zipfile
     */
    private void getImportResource() throws CmsVfsException {

        // get the import resource
        m_importResource = new File(m_importFolderName);
        // check if this is a folder or a ZIP file
        if (m_importResource.isFile()) {
            try {
                m_zipFile = ZipFile.builder().setFile(m_importResource).setBufferSize(65536).get();
            } catch (IOException e) {
                // if file but no ZIP file throw an exception
                throw new CmsVfsException(
                    Messages.get().container(Messages.ERR_NO_ZIPFILE_1, m_importResource.getName()),
                    e);
            }
        }
    }

    /**
     * Imports the resources from the folder in the real file system to the OpenCms VFS.<p>
     *
     * @param folder the folder to import from
     * @param importPath the OpenCms VFS import path to import to
     * @throws Exception if something goes wrong during file IO
     */
    private void importResources(File folder, String importPath) throws Exception {

        String[] diskFiles = folder.list();
        File currentFile;

        for (int i = 0; i < diskFiles.length; i++) {
            currentFile = new File(folder, diskFiles[i]);

            if (currentFile.isDirectory()) {
                // create directory in cms
                m_importedResources.add(
                    m_cms.createResource(importPath + currentFile.getName(), CmsResourceTypeFolder.RESOURCE_TYPE_ID));
                importResources(currentFile, importPath + currentFile.getName() + "/");
            } else {
                // import file into cms
                int type = OpenCms.getResourceManager().getDefaultTypeForName(currentFile.getName()).getTypeId();
                byte[] content = CmsFileUtil.readFile(currentFile);
                // create the file
                try {
                    m_importedResources.add(
                        m_cms.createResource(importPath + currentFile.getName(), type, content, null));
                } catch (CmsSecurityException e) {
                    // in case of not enough permissions, try to create a plain text file
                    int plainId = OpenCms.getResourceManager().getResourceType(
                        CmsResourceTypePlain.getStaticTypeName()).getTypeId();
                    m_importedResources.add(
                        m_cms.createResource(importPath + currentFile.getName(), plainId, content, null));
                }
                content = null;
            }
        }
    }

    /**
     * Imports the resources from a ZIP file in the real file system to the OpenCms VFS.<p>
     *
     * @param zipStreamIn the input Stream
     * @param importPath the path in the vfs
     * @param noSubFolder if <code>true</code> no sub folders will be created, if <code>false</code> the content of the
     * zip file is created 1:1 inclusive sub folders
     *
     * @throws Exception if something goes wrong during file IO
     */
    private void importZipResource(ZipFile zipFile, String importPath, boolean noSubFolder) throws Exception {

        // HACK: this method looks very crude, it should be re-written sometime...

        boolean isFolder = false;
        int j, r, stop, size;
        int entries = 0;
        byte[] buffer = null;
        boolean resourceExists;
        for (ZipArchiveEntry entry : (Iterable<ZipArchiveEntry>)() -> zipFile.getEntries().asIterator()) {
            // handle the single entries ...
            j = 0;
            stop = 0;
            entries++; // count number of entries in zip
            String actImportPath = importPath;
            String title = CmsResource.getName(entry.getName());
            String filename = m_cms.getRequestContext().getFileTranslator().translateResource(entry.getName());
            // separate path in directories an file name ...
            StringTokenizer st = new StringTokenizer(filename, "/\\");
            int count = st.countTokens();
            String[] path = new String[count];

            if (filename.endsWith("\\") || filename.endsWith("/")) {
                isFolder = true; // last entry is a folder
            } else {
                isFolder = false; // last entry is a file
            }
            while (st.hasMoreTokens()) {
                // store the files and folder names in array ...
                path[j] = st.nextToken();
                j++;
            }
            stop = isFolder ? path.length : (path.length - 1);

            if (noSubFolder) {
                stop = 0;
            }
            // now write the folders ...
            for (r = 0; r < stop; r++) {
                try {
                    CmsResource createdFolder = m_cms.createResource(
                        actImportPath + path[r],
                        CmsResourceTypeFolder.RESOURCE_TYPE_ID);
                    m_importedResources.add(createdFolder);
                } catch (CmsException e) {
                    // of course some folders did already exist!
                }
                actImportPath += path[r];
                actImportPath += "/";
            }
            if (!isFolder) {
                // import file into cms
                int type = OpenCms.getResourceManager().getDefaultTypeForName(path[path.length - 1]).getTypeId();
                buffer = CmsFileUtil.readFully(zipFile.getInputStream(entry), true);
                size = Long.valueOf(entry.getSize()).intValue();
                filename = actImportPath + path[path.length - 1];
                try {
                    m_cms.lockResource(filename);
                    m_cms.readResource(filename);
                    resourceExists = true;
                } catch (CmsException e) {
                    resourceExists = false;
                }

                int plainId = OpenCms.getResourceManager().getResourceType(
                    CmsResourceTypePlain.getStaticTypeName()).getTypeId();
                if (resourceExists) {
                    CmsResource res = m_cms.readResource(filename, CmsResourceFilter.ALL);
                    CmsFile file = m_cms.readFile(res);
                    byte[] contents = file.getContents();
                    try {
                        m_cms.replaceResource(filename, res.getTypeId(), buffer, new ArrayList<CmsProperty>(0));
                        m_importedResources.add(res);
                    } catch (CmsSecurityException e) {
                        // in case of not enough permissions, try to create a plain text file
                        m_cms.replaceResource(filename, plainId, buffer, new ArrayList<CmsProperty>(0));
                        m_importedResources.add(res);
                    } catch (CmsDbSqlException sqlExc) {
                        // SQL error, probably the file is too large for the database settings, restore content
                        file.setContents(contents);
                        m_cms.writeFile(file);
                        throw sqlExc;
                    }
                } else {
                    String newResName = actImportPath + path[path.length - 1];
                    if (title.lastIndexOf('.') != -1) {
                        title = title.substring(0, title.lastIndexOf('.'));
                    }
                    List<CmsProperty> properties = new ArrayList<CmsProperty>(1);
                    CmsProperty titleProp = new CmsProperty();
                    titleProp.setName(CmsPropertyDefinition.PROPERTY_TITLE);
                    if (OpenCms.getWorkplaceManager().isDefaultPropertiesOnStructure()) {
                        titleProp.setStructureValue(title);
                    } else {
                        titleProp.setResourceValue(title);
                    }
                    properties.add(titleProp);
                    try {
                        m_importedResources.add(m_cms.createResource(newResName, type, buffer, properties));
                    } catch (CmsSecurityException e) {
                        // in case of not enough permissions, try to create a plain text file
                        m_importedResources.add(m_cms.createResource(newResName, plainId, buffer, properties));
                    } catch (CmsDbSqlException sqlExc) {
                        // SQL error, probably the file is too large for the database settings, delete file
                        m_cms.lockResource(newResName);
                        m_cms.deleteResource(newResName, CmsResource.DELETE_PRESERVE_SIBLINGS);
                        throw sqlExc;
                    }
                }
            }
        }
        if (entries > 0) {
            // at least one entry, got a valid zip file ...
            m_validZipFile = true;
        }
    }
}
