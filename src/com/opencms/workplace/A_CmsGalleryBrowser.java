/*
 * File : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/A_CmsGalleryBrowser.java,v $ 
 * Date : $Date: 2004/01/07 16:44:15 $ 
 * Version: $Revision: 1.1.2.1 $
 * 
 * This library is part of OpenCms - the Open Source Content Mananagement
 * System
 * 
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or (at your
 * option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * For further information about Alkacon Software, please see the company
 * website: http://www.alkacon.com
 * 
 * For further information about OpenCms, please see the project website:
 * http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package com.opencms.workplace;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * An abstract gallery browser.<p>
 * 
 * Each {download|picture|HTML} gallery workplace class has to extend this
 * browser class.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.1.2.1 $ $Date: 2004/01/07 16:44:15 $
 */
public abstract class A_CmsGalleryBrowser extends CmsWorkplaceDefault {

    /**
     * Reads the names of all galleries for a specified gallery path.
     * <P>
     * 
     * The vectors <code>names</code> and <code>values</code> will be
     * filled with the information to be used for building a select box. The
     * values will are the paths of the galleries.
     * <p>
     * 
     * @param cms the current user's CmsObject instance
     * @param galleryPath the VFS paath of the gallery, e.g. /system/galleries/pics/
     * @param lang XML language file for internationalization, <em>(not used here)</em>
     * @param names filled with the URIs of the galleries
     * @param values filled with the names of the galleries
     * @param parameters contains user parameters, <em>(not used here)</em>
     * @return the number of galleries
     * @throws CmsException if something goes wrong
     */
    public Integer getGalleryNames(CmsObject cms, String galleryPath, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String ident = "";
        int ret = -1;
        // which folder is the gallery?
        String chosenFolder = (String) parameters.get(C_PARA_FOLDER);
        if (chosenFolder == null) {
            chosenFolder = (String) session.getValue(C_PARA_FOLDER);
        }
        if (chosenFolder == null) {
            chosenFolder = "";
        }
        List folders = getGallerySubFolders(cms, new ArrayList(), galleryPath);
        int numFolders = folders.size();
        for (int i = 0; i < numFolders; i++) {
            CmsResource currFolder = (CmsResource) folders.get(i);
            ident = "";
            String name = currFolder.getName();
            if (chosenFolder.equals(currFolder.getAbsolutePath())) {
                ret = i;
            }
            StringTokenizer tokenizer = new StringTokenizer(currFolder.getAbsolutePath(), "/");
            for (int j = 0; j < (tokenizer.countTokens() - 4); j++) {
                ident = ident + "-";
            }
            values.addElement(currFolder.getAbsolutePath());
            names.addElement(ident + name);
        }
        return new Integer(ret);
    }

    /**
     * Reads all subfolders of a gallery recursively.<p>
     * 
     * @param cms the current user's CmsObject instance
     * @param folders filled with the appropriate values in this method.
     * @param foldername String the folder to start from.
     * @return all folders
     * @throws CmsException
     */
    protected List getGallerySubFolders(CmsObject cms, List folders, String foldername) throws CmsException {
        List tempFolders = cms.getSubFolders(foldername);
        for (int i = 0; i < tempFolders.size(); i++) {
            CmsResource currFolder = (CmsResource) tempFolders.get(i);
            if (this.checkAccess(cms, currFolder)) {
                folders.add(tempFolders.get(i));
            }
            this.getGallerySubFolders(cms, folders, currFolder.getAbsolutePath());
        }
        return folders;
    }

    /**
     * Checks if a resource should be displayed in the gallery selection.<p>
     * 
     * @param cms the current user's CmsObject instance
     * @param res the resource
     * @return true if a resource should be displayed in the gallery selection
     * @throws CmsException if something goes wrong.
     */
    protected boolean checkAccess(CmsObject cms, CmsResource res) throws CmsException {
        boolean access = false;
        if (res.getState() == C_STATE_DELETED) {
            return false;
        }
        int accessflags = res.getAccessFlags();

        // First check if the user may have access by one of his groups.
        boolean groupAccess = false;
        Enumeration allGroups = cms.getGroupsOfUser(cms.getRequestContext().currentUser().getName()).elements();
        while ((!groupAccess) && allGroups.hasMoreElements()) {
            groupAccess = cms.readGroup(res).equals((CmsGroup) allGroups.nextElement());
        }
        if (((accessflags & C_ACCESS_PUBLIC_VISIBLE) > 0) || (cms.readOwner(res).equals(cms.getRequestContext().currentUser()) && (accessflags & C_ACCESS_OWNER_VISIBLE) > 0) || (groupAccess && (accessflags & C_ACCESS_GROUP_VISIBLE) > 0) || (cms.getRequestContext().isAdmin())) {
            access = true;
        }
        return access;
    }
    
    /**
     * Checks, if the given filename matches the filter string.<p>
     * 
     * @param filename filename to be checked
     * @param filter filter to be checked
     * @return <code>true</code> if the filename matches the filter, <code>false</code> otherwise.
     */
    protected boolean inFilter(String filename, String filter) {
        String compareName = filename.toLowerCase();
        String compareFilter = filter.toLowerCase();
        
        return ("".equals(compareFilter) || (compareName.indexOf(compareFilter) != -1));
    }    

}
