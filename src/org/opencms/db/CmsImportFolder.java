/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/CmsImportFolder.java,v $
 * Date   : $Date: 2004/07/05 16:32:42 $
 * Version: $Revision: 1.19 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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
 
package org.opencms.db;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeFolder;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Allows to import resources from the filesystem or a ZIP file into the OpenCms VFS.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 *
 * @version $Revision: 1.19 $
 */
public class CmsImportFolder {

    /** The OpenCms context object that provides the permissions. */
    private CmsObject m_cms;

    /** The name of the import folder to load resources from. */
    private String m_importFolderName;

    /** The import path in the OpenCms VFS. */
    private String m_importPath;

    /** The resource (folder or ZIP file) to import from in the real file system. */
    private File m_importResource;

    /** Will be true if the import resource is a valid ZIP file. */
    private boolean m_validZipFile;

    /** The import resource ZIP stream to load resources from. */    
    private ZipInputStream m_zipStreamIn;
    
    /**
     * Constructor for a new CmsImportFolder that will read from a ZIP file.<p>
     *
     * @param content the zip file to import
     * @param importPath the path to the OpenCms VFS to import to
     * @param cms a OpenCms context to provide the permissions
     * @param noSubFolder if false no sub folder will be created 
     * @throws CmsException if something goes wrong
     */
    public CmsImportFolder(
        byte[] content, 
        String importPath, 
        CmsObject cms, 
        boolean noSubFolder
    ) throws CmsException {
        m_importPath = importPath;
        m_cms = cms;
        try {
            // open the import resource
            m_zipStreamIn = new ZipInputStream(new ByteArrayInputStream(content));
            m_cms.readFolder(importPath, CmsResourceFilter.IGNORE_EXPIRATION);
            // import the resources
            importZipResource(m_zipStreamIn, m_importPath, noSubFolder);
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Constructor for a new CmsImportFolder that will read from the real file system.<p>
     *
     * @param importFolderName the folder to import
     * @param importPath the path to the OpenCms VFS to import to
     * @param cms a OpenCms context to provide the permissions
     * @throws CmsException if something goes wrong
     */
    public CmsImportFolder(String importFolderName, String importPath, CmsObject cms) throws CmsException {
        try {
            m_importFolderName = importFolderName;
            m_importPath = importPath;
            m_cms = cms;
            // open the import resource
            getImportResource();
            // first lock the destination path
            m_cms.lockResource(m_importPath);
            // import the resources
            if (m_zipStreamIn == null) {
                importResources(m_importResource, m_importPath);
            } else {
                importZipResource(m_zipStreamIn, m_importPath, false);
            }
            // all is done, unlock the resources
            m_cms.unlockResource(m_importPath);
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }

    /**
     * Returns a byte array containing the content of a file from the real file system.<p>
     *
     * @param file the file to read
     * @return the content of the read file
     * @throws Exception if something goes wrong during file IO
     */
    private byte[] getFileBytes(File file) throws Exception {
        FileInputStream fileStream = new FileInputStream(file);
        int charsRead = 0;
        int size = new Long(file.length()).intValue();
        byte[] buffer = new byte[size];
        while (charsRead < size) {
            charsRead += fileStream.read(buffer, charsRead, size - charsRead);
        }
        fileStream.close();
        fileStream = null;
        return buffer;
    }

    /**
     * Stores the import resource in an Object member variable.<p>
     * @throws CmsException if something goes wrong 
     */
    private void getImportResource() throws CmsException {
        try {
            // get the import resource
            m_importResource = new File(m_importFolderName);
            // check if this is a folder or a ZIP file
            if (m_importResource.isFile()) {
                try {
                    m_zipStreamIn = new ZipInputStream(new FileInputStream(m_importResource));
                } catch (IOException e) {
                    // if file but no ZIP file throw an exception
                    throw new CmsException(e.toString());
                }
            }
        } catch (Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
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
                m_cms.createResource(importPath + currentFile.getName(), CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
                importResources(currentFile, importPath + currentFile.getName() + "/");
            } else {
                // import file into cms
                int type = OpenCms.getResourceManager().getDefaultTypeForName(currentFile.getName()).getTypeId();
                byte[] content = getFileBytes(currentFile);
                // create the file
                m_cms.createResource(importPath + currentFile.getName(), type, content, null);
                content = null;
            }
        }
    }

    /**
     * Imports the resources from a ZIP file in the real file system to the OpenCms VFS.<p>
     *
     * @param zipStreamIn the input Stream
     * @param importPath the path in the vfs
     * @param noSubFolder create subFolders or not
     * @throws Exception if something goes wrong during file IO 
     */
    private void importZipResource(ZipInputStream zipStreamIn, String importPath, boolean noSubFolder) throws Exception {
        boolean isFolder = false;
        boolean exit = false;
        int j, r, stop, charsRead, size;
        int entries = 0;
        int totalBytes = 0;
        int offset = 0;
        byte[] buffer = null;
        boolean resourceExists;
        
        while (true) {
            // handle the single entries ...
            j = 0;
            stop = 0;
            charsRead = 0;
            totalBytes = 0;
            // open the entry ...
            ZipEntry entry = zipStreamIn.getNextEntry();
            if (entry == null) {
                break;
            }
            entries++; // count number of entries in zip
            String actImportPath = importPath;
            String filename = m_cms.getRequestContext().getFileTranslator().translateResource(entry.getName());
            // separete path in direcotries an file name ...
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
            stop = isFolder?path.length:(path.length - 1);

            if (noSubFolder) {
                stop = 0;
            }
            // now write the folders ...
            for (r = 0; r < stop; r++) {
                try {
                    m_cms.createResource(actImportPath + path[r], CmsResourceTypeFolder.C_RESOURCE_TYPE_ID);
                } catch (CmsException e) {
                    // of course some folders did already exist!
                }
                actImportPath += path[r];
                actImportPath += "/";
            }
            if (! isFolder) {
                // import file into cms
                int type = OpenCms.getResourceManager().getDefaultTypeForName(path[path.length - 1]).getTypeId();
                size = new Long(entry.getSize()).intValue();
                if (size == -1) {
                    Vector v = new Vector();
                    while (true) {
                        buffer = new byte[512];
                        offset = 0;
                        while (offset < buffer.length) {
                            charsRead = zipStreamIn.read(buffer, offset, buffer.length - offset);
                            if (charsRead == -1) {
                                exit = true;
                                break; // end of stream
                            }
                            offset += charsRead;
                            totalBytes += charsRead;
                        }
                        if (offset > 0) {
                            v.addElement(buffer);
                        }
                        if (exit) {
                            exit = false;
                            break;
                        }
                    }
                    buffer = new byte[totalBytes];
                    offset = 0;
                    byte[] act = null;
                    for (int z = 0; z < v.size() - 1; z++) {
                        act = (byte[])v.elementAt(z);
                        System.arraycopy(act, 0, buffer, offset, act.length);
                        offset += act.length;
                    }
                    act = (byte[])v.lastElement();
                    if ((totalBytes > act.length) && (totalBytes % act.length != 0)) {
                        totalBytes = totalBytes % act.length;
                    } else if ((totalBytes > act.length) && (totalBytes % act.length == 0)) {
                        totalBytes = act.length;
                    }
                    System.arraycopy(act, 0, buffer, offset, totalBytes);
                    // handle empty files ...
                    if (totalBytes == 0) {
                        buffer = " ".getBytes();
                    }
                } else {
                    // size was read clearly ...
                    buffer = new byte[size];
                    while (charsRead < size) {
                        charsRead += zipStreamIn.read(buffer, charsRead, size - charsRead);
                    }
                    // handle empty files ...
                    if (size == 0) {
                        buffer = " ".getBytes();
                    }
                }

                filename = actImportPath + path[path.length - 1];
                
                try {
                    m_cms.lockResource(filename);
                    
                    m_cms.readResource(filename);
                    resourceExists = true;
                } catch (CmsException e) {
                    resourceExists = false;
                }
                
                if (resourceExists) {
                    CmsResource res = m_cms.readResource(filename, CmsResourceFilter.ALL);
                    
                    //m_cms.deleteAllProperties(filename);
                    //m_cms.replaceResource(filename, type, Collections.EMPTY_MAP, buffer);
                    m_cms.replaceResource(filename, res.getTypeId(), buffer, Collections.EMPTY_LIST);
                    
                    OpenCms.fireCmsEvent(new CmsEvent(new CmsObject(), I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED, Collections.singletonMap("resource", res)));
                } else {
                    m_cms.createResource(actImportPath + path[path.length - 1], type, buffer, Collections.EMPTY_LIST);
                }
            }

            // close the entry ...
            zipStreamIn.closeEntry();
        }
        zipStreamIn.close();
        if (entries > 0) {
            // at least one entry, got a valid zip file ...
            m_validZipFile = true;
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
}
