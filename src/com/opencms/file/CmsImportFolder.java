/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsImportFolder.java,v $
* Date   : $Date: 2003/07/10 14:38:59 $
* Version: $Revision: 1.22 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org 
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.opencms.file;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.report.CmsShellReport;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Holds the functionaility to import resources from the filesystem
 * into the cms.
 *
 * @author Andreas Schouten
 * @version $Revision: 1.22 $ $Date: 2003/07/10 14:38:59 $
 */
public class CmsImportFolder implements I_CmsConstants {

    /**
     * The import-file to load resources from
     */
    private String m_importFile;

    /**
     * The import-resource (zip) to load resources from
     */
    private ZipInputStream m_zipStreamIn = null;

    /**
     *
     */
    private boolean m_validZipFile = false;

    /**
     * The import-path to write resources into the cms.
     */
    private String m_importPath;

    /**
     * The cms-object to do the operations.
     */
    private CmsObject m_cms;

    /**
     * The folder-object to import from.
     */
    private File m_importResource;

    /**
     * This constructs a new CmsImport-object which imports the resources.
     *
     * @param importFile the file or folder to import from.
     * @param importPath the path to the cms to import into.
     * @throws CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsImportFolder(String importFile, String importPath, CmsObject cms)
        throws CmsException {

        try {
            m_importFile = importFile;
            m_importPath = importPath;
            m_cms = cms;

            // open the import resource
            getImportResource();

            // frist lock the path to import into.
            m_cms.lockResource(m_importPath);

            // import the resources
            if( m_zipStreamIn == null) {
                importResources(m_importResource, m_importPath);
            } else {
                importZipResource(m_zipStreamIn, m_importPath, false);
            }

            // all is done, unlock the resources
            m_cms.unlockResource(m_importPath, false);

            // clean-up the link management
            cms.joinLinksToTargets( new CmsShellReport() );             
        } catch( Exception exc ) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }
    /**
     * This constructs a new CmsImport-object which imports the resources.
     *
     * @param content the zip file to import
     * @param importPath the path to the cms to import into.
     * @throws CmsException the CmsException is thrown if something goes wrong.
     */
    public CmsImportFolder(byte[] content, String importPath, CmsObject cms,
                            boolean noSubFolder) throws CmsException {

        m_importPath = importPath;
        m_cms = cms;

        try {
            // open the import resource
            m_zipStreamIn = new ZipInputStream(new ByteArrayInputStream(content));
            m_cms.readFolder(importPath);
            
            // import the resources
            importZipResource(m_zipStreamIn, m_importPath, noSubFolder);

            // clean-up the link management
            cms.joinLinksToTargets( new CmsShellReport() );            
        } catch( Exception exc ) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }


    /**
     * Returns a byte-array containing the content of the file.
     *
     * @param filename The name of the file to read.
     * @return bytes[] The content of the file.
     */
    private byte[] getFileBytes(File file)
        throws Exception{
        FileInputStream fileStream = new FileInputStream(file);
        int charsRead = 0;
        int size = new Long(file.length()).intValue();
        byte[] buffer = new byte[size];
        while(charsRead < size) {
            charsRead += fileStream.read(buffer, charsRead, size - charsRead);
        }
        fileStream.close();
        return buffer;
    }

    /**
     * Gets the file-type for the filename.
     */
    private String getFileType(String filename)
        throws CmsException {
        String suffix = filename.substring(filename.lastIndexOf('.')+1);
        suffix = suffix.toLowerCase(); // file extension of filename

        // read the known file extensions from the database
        Hashtable extensions = m_cms.readFileExtensions();
        String resType = new String();
        if (extensions != null) {
            resType = (String) extensions.get(suffix);
        }
        if (resType == null) {
            resType = "plain";
        }
        return resType;
    }

    /**
     * Gets the import resource and stores it in object-member.
     */
    private void getImportResource() throws CmsException {
        try {
            // get the import resource
            m_importResource = new File(m_importFile);

            // if it is a file throw exception.
            if(m_importResource.isFile()) {
                try{
                    m_zipStreamIn = new ZipInputStream(new FileInputStream(
                                                        m_importResource) );
                } catch(IOException e) {
                    throw new CmsException("Exception: " + e.toString() );
                }
            }
        } catch(Exception exc) {
            throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, exc);
        }
    }
    /**
     * Imports the resources from the folder to the importPath.
     *
     * @param folder the file-object to import from.
     * @param importPath the import-path to import into.
     */
    private void importResources(File folder, String importPath)
        throws Exception {
        String[] diskFiles = folder.list();
        File currentFile;

        for(int i = 0; i < diskFiles.length; i++) {
            currentFile = new File(folder, diskFiles[i]);

            if(currentFile.isDirectory()) {
                // create directory in cms
                m_cms.createResource(importPath, currentFile.getName(), C_TYPE_FOLDER_NAME);
                importResources(currentFile, importPath + currentFile.getName() + "/");
            } else {
                // import file into cms
                String type = getFileType( currentFile.getName() );
                byte[] content = getFileBytes(currentFile);
                // create the file
                m_cms.createResource(importPath, currentFile.getName(), type, null, content);
            }
        }
    }

    /**
     * imports the zip File to the import path
     *
     * @param zipStreamIn the input Stream
     * @param importPath the path in the vfs
     * @param noSubFolder create subFolders or not
     */
     private void importZipResource(ZipInputStream zipStreamIn,
                                    String importPath, boolean noSubFolder)
            throws Exception {
        boolean isFolder = false;
        boolean exit = false;
        int j, r, stop, charsRead, size;
        int entries = 0;
        int totalBytes = 0;
        int offset = 0;
        byte[] buffer = null;
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

            if(filename.endsWith("\\") || filename.endsWith("/")){
                isFolder = true;  // last entry is a folder
            } else {
                isFolder = false; // last entry is a file
            }
            while( st.hasMoreTokens() ) {
                // store the files and folder names in array ...
                path[j] = st.nextToken();
                j++;
            }
            stop = isFolder==true ? path.length : path.length-1;

            if(noSubFolder == true) {stop = 0;}
            // now write the folders ...
            for(r=0; r < stop; r++) {
                try {
                    m_cms.createResource(actImportPath, path[r], C_TYPE_FOLDER_NAME);
                } catch(CmsException e) {
                    // of course some folders did already exist!
                }
                actImportPath += path[r] += "/";
            }
            if(isFolder == false) {
                // import file into cms
                String type = getFileType( path[path.length-1] );

                size = new Long(entry.getSize()).intValue();
                if(size == -1) {
                    Vector v = new Vector();
                    while(true) {
                        buffer = new byte[512];
                        offset = 0;
                        while(offset < buffer.length) {
                            charsRead = zipStreamIn.read(buffer, offset, buffer.length - offset);
                            if(charsRead == -1) {
                                exit = true;
                                break; // end of stream
                            }
                            offset += charsRead;
                            totalBytes += charsRead;
                        }
                        if(offset > 0) {
                            v.addElement(buffer);
                        }
                        if(exit == true) {
                            exit = false;
                            break;
                        }
                    }
                    buffer = new byte[totalBytes];
                    offset = 0;
                    byte[] act = null;
                    for(int z = 0; z < v.size()-1; z++) {
                        act = (byte[]) v.elementAt(z);
                        System.arraycopy(act, 0, buffer, offset, act.length);
                        offset += act.length;
                    }
                    act = (byte[]) v.lastElement();
                    if((totalBytes > act.length) && (totalBytes % act.length != 0)) {
                        totalBytes = totalBytes%act.length;
                    } else if ((totalBytes > act.length) && (totalBytes % act.length == 0)) {
                        totalBytes = act.length;
                    }
                    System.arraycopy(act, 0, buffer, offset, totalBytes);
                    // handle empty files ...
                    if(totalBytes ==0) { buffer = " ".getBytes(); }
                } else {
                    // size was read clearly ...
                    buffer = new byte[size];
                    while(charsRead < size) {
                        charsRead += zipStreamIn.read(buffer, charsRead,
                                                        size - charsRead);
                    }
                    // handle empty files ...
                    if(size == 0) { buffer = " ".getBytes(); }
                }

                filename = actImportPath + path[path.length-1];
                Map oldProperties = null;
                
                try { 
                    // lock the filename to see whether it already exists
                    m_cms.lockResource(filename, true);
                    
                    // save the properties of the old file
                    oldProperties = m_cms.readProperties(filename);
                    
                    // trash the old file
                    m_cms.deleteResource( filename );
                } 
                catch(CmsException e) {
                    // ignore the exception (did not exist)
                }
                
                try {
                    // create the new file ...
                    m_cms.createResource(actImportPath, path[path.length-1], type, null, buffer);
                    
                    // set the properties of the old file on the new file
                    if (oldProperties!=null) {
                        m_cms.writeProperties( filename, oldProperties );
                    }
                } catch(CmsException e) {
                    // ignore the exception
                    throw new CmsException(CmsException.C_UNKNOWN_EXCEPTION, e);
                }
            }
            
            // close the entry ...
            zipStreamIn.closeEntry();
        }
        zipStreamIn.close();
        if(entries > 0){
            // at least one entry, got a valid zip file ...
            setValidZipFile(true);
        }
     }

    /**
     * return true if we got a valid zip file
     */
     public boolean isValidZipFile() {
        return m_validZipFile;
     }

    /**
     * set valid or invalid
     */
     private void setValidZipFile(boolean valid) {
        m_validZipFile = valid;
     }
}
