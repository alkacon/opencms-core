/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsSynchronizeList.java,v $
* Date   : $Date: 2003/04/01 15:20:18 $
* Version: $Revision: 1.10 $
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

import java.util.*;
import java.io.*;
import com.opencms.core.*;
import com.opencms.util.*;

/**
 * Describes the synchronize list for synchronizing the resources
 * in the virtual filesystem (VFS) and the server filesystem (SFS)
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.10 $ $Date: 2003/04/01 15:20:18 $
 */
public class CmsSynchronizeList implements I_CmsConstants, Serializable {

    /**
     * the name of the synchronize list in the filesystem
     */
    public static String C_SYNCLIST_FILE = "synchronize.list";

    /**
     * the name of the key for the resource date in the virtual filesystem
     */
    static final String C_VFS_DATE = "VFSDATE";

    /**
     * the name of the key for the resource date in the server filesystem
     */
    static final String C_SFS_DATE = "SFSDATE";

    /**
     * the path on the filesystem for synchronization
     */
    private String m_synchronizePath = null;

    /**
     * the hashtable which holds the dates of the resource in both filesystems
     */
    private Hashtable m_synchronizeList = null;

    /**
     * Constructor, creates a new CmsSynchronizeList object.
     *
     * @param syncPath The path on the filesystem for synchronization.
     */
    public CmsSynchronizeList(String syncPath) throws CmsException{
        m_synchronizePath = syncPath;
        try {
            m_synchronizeList = (Hashtable)readSyncList();
            if (m_synchronizeList == null){
                m_synchronizeList = new Hashtable();
            }
        } catch (Exception e){
            throw new CmsException("Could not read synchronize.list in path "+m_synchronizePath, e);
        }
    }

    /**
     * Creates the new file on the SFS
     *
     * @param newFile the file that has to be created in SFS.
     * @throws CmsException the CmsException is thrown if something goes wrong.
     */
    private void createNewLocalFile(File newFile) throws CmsException {
        FileOutputStream fOut = null;
        if (newFile.exists()){
            throw new CmsException(newFile.getName()+" already exists on filesystem.");
        }
        try {
            String pathName = newFile.getPath().substring(0, (int)newFile.getPath().lastIndexOf(File.separatorChar)+1);
            File directory = new File(pathName);
            if (directory.exists()){
                fOut = new FileOutputStream(newFile);
            } else {
                throw new CmsException("["+this.getClass().getName()+"]"+" Cannot create file "+newFile.getName()+" on filesystem");
            }
        } catch (IOException e){
            throw new CmsException("["+this.getClass().getName()+"]"+" Cannot create file "+newFile.getName()+" on filesystem", e);
        } finally {
            if (fOut != null){
                try {
                    fOut.close();
                } catch (IOException e){
                }
            }
        }
    }

    /**
     * This returns the date of the last synchronized version of the resource
     * from the server filesystem
     *
     * @param resourceName the resource to get the date.
     * @return Date the date of the resource
     */
    public long getSfsDate(String resourceName){
        long lastSyncDate = 0;
        Hashtable dateOfFile = (Hashtable)m_synchronizeList.get(resourceName);
        if (dateOfFile != null){
            lastSyncDate = Long.parseLong((String)dateOfFile.get(C_SFS_DATE));
        }
        return lastSyncDate;
    }

    /**
     * This returns the date of the last synchronized version of the resource
     * from the virtual filesystem
     *
     * @param resourceName the resource to get the date.
     * @return Date the date of the resource
     */
    public long getVfsDate(String resourceName){
        long lastSyncDate = 0;
        Hashtable dateOfFile = (Hashtable)m_synchronizeList.get(resourceName);
        if (dateOfFile != null){
            lastSyncDate = Long.parseLong((String)dateOfFile.get(C_VFS_DATE));
        }
        return lastSyncDate;
    }

    /**
     * This writes the dates of the last synchronized version of the resource
     * to the synchronize list hashtable
     *
     * @param resourceName the name of the resource to put into the hashtable.
     * @param vfsDate the date of the resource in VFS.
     * @param sfsDate the date of the resource in SFS.
     */
    public void putDates(String resourceName, long vfsDate, long sfsDate){
        Hashtable dateOfFile = new Hashtable();
        dateOfFile.put(C_VFS_DATE, Long.toString(vfsDate));
        dateOfFile.put(C_SFS_DATE, Long.toString(sfsDate));
        m_synchronizeList.put(resourceName, dateOfFile);
    }

    /**
     * This reads the synchronize list which containts the resources which were
     * already synchronized and the date of the last synchronized file
     *
     * @throws Exception the Exception is thrown if something goes wrong.
     * @return Serializable the content for the hashtable
     */
    private Serializable readSyncList()
        throws Exception {
        Serializable hashSyncList = null;
        File file = null;
        FileInputStream fileIn = null;
        ByteArrayInputStream byteIn = null;
        ObjectInputStream objectIn = null;
        try {
            // read the synchronize list from the filesystem
            file = new File(m_synchronizePath, C_SYNCLIST_FILE);

            if (file.exists()) {
                fileIn = new FileInputStream(file);
                int charsRead = 0;
                int size = new Long(file.length()).intValue();
                byte[] buffer = new byte[size];
                while (charsRead < size) {
                    charsRead += fileIn.read(buffer, charsRead, size - charsRead);
                }
                // serialize the file
                byteIn = new ByteArrayInputStream(buffer);
                objectIn = new ObjectInputStream(byteIn);
                hashSyncList = (Serializable) objectIn.readObject();
            }
            return hashSyncList;
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                if (fileIn != null)
                    fileIn.close();
            } catch (IOException e) {
            }
            try {
                if (byteIn != null)
                    byteIn.close();
            } catch (IOException e) {
            }
            try {
                if (objectIn != null)
                    objectIn.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * This removes the resource from the hashtable
     *
     * @param resourceName the resource that has to be removed
     */
    public void remove(String resourceName){
        m_synchronizeList.remove(resourceName);
    }

    /**
     * This saves the synchronization list after the synchronization
     * and clears the hashtable
     *
     * @throws CmsException the CmsException is thrown if something goes wrong.
     */
    public void saveSyncList() throws CmsException{
        try {
            writeSyncList(m_synchronizeList);
        } catch (CmsException e) {
            throw e;
        } finally {
            m_synchronizeList.clear();
        }
    }

    /**
     * Returns a string-representation for this object.
     * This can be used for debugging.
     *
     * @return string-representation for this object.
     */
    public String toString(){
        StringBuffer output = new StringBuffer();
        String key;
        Hashtable value;
                String date;
        Enumeration enu = m_synchronizeList.keys();
        output.append("[CmsSynchronizeList]:\n");
        while (enu.hasMoreElements()){
            key = (String)enu.nextElement();
            output.append(key+": VFS ");
            value = (Hashtable)m_synchronizeList.get(key);
            date = Utils.getNiceDate(Long.parseLong((String)value.get(C_VFS_DATE)));
            output.append(date+", SFS ");
            date = Utils.getNiceDate(Long.parseLong((String)value.get(C_SFS_DATE)));
            output.append(date+"\n");
        }
        return output.toString();
    }

    /**
     * This writes the synchronize list which containts the resources which were
     * already synchronized and the date of the last synchronized file
     *
     * @param hashSyncList the hashtable which has to be written into the file
     * @throws CmsException the CmsException is thrown if something goes wrong.
     * @return Serializable
     */
    private Serializable writeSyncList(Serializable hashSyncList)
        throws CmsException {
        ByteArrayOutputStream bOut = null;
        ObjectOutputStream oOut = null;
        FileOutputStream fOut = null;
        DataOutputStream dOut = null;
        File file = new File(m_synchronizePath, C_SYNCLIST_FILE);
        if (!file.exists()){
            createNewLocalFile(file);
        }
        try {
                        // serialize the hashtable
                        bOut = new ByteArrayOutputStream();
                        oOut = new ObjectOutputStream(bOut);
                        oOut.writeObject(hashSyncList);
            byte[] value = bOut.toByteArray();
            // write the synchronize list to the filesystem
            fOut = new FileOutputStream(file);
            dOut = new DataOutputStream(fOut);
            dOut.write(value);
            dOut.flush();
        } catch (IOException e) {
            throw new CmsException("["+this.getClass().getName()+"]"+" Cannot write Synchronize List", e);
        } finally {
            try {
                if (bOut != null)
                    bOut.close();
            } catch (IOException e) {
            }
            try {
                if (oOut != null)
                    oOut.close();
            } catch (IOException e) {
            }
            try {
                if (dOut != null)
                    dOut.close();
            } catch (IOException e) {
            }
            try {
                if (fOut != null)
                    fOut.close();
            } catch (IOException e) {
            }
        }
        return hashSyncList;
    }
}
