/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsHelperMastertemplates.java,v $
* Date   : $Date: 2001/09/10 08:32:51 $
* Version: $Revision: 1.2 $
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

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import java.util.*;

/**
 * Helper class to receive all mastertemplates that are currently in the system.
 * @version $Revision: 1.2 $ $Date: 2001/09/10 08:32:51 $
 */

public class CmsHelperMastertemplates {

    /**
     * Gets the templates displayed in the template select box.
     * @param cms The CmsObject.
     * @param names The names of the new rescources.
     * @param values The links that are connected with each resource.
     * @returns The vectors names and values are filled with the information found in the
     * workplace.ini.
     * @exception Throws CmsException if something goes wrong.
     */
    public static Integer getTemplates(CmsObject cms, Vector names, Vector values, String currentTemplate) throws CmsException {

        Vector files = cms.getFilesInFolder(I_CmsWpConstants.C_CONTENTTEMPLATEPATH);

        // get all module Templates
        Vector modules = new Vector();
        modules = cms.getSubFolders(I_CmsConstants.C_MODULES_PATH);
        for(int i = 0;i < modules.size();i++) {
            Vector moduleTemplateFiles = new Vector();
            moduleTemplateFiles = cms.getFilesInFolder(((CmsFolder)modules.elementAt(i)).getAbsolutePath() + "templates/");
            for(int j = 0;j < moduleTemplateFiles.size();j++) {
                files.addElement(moduleTemplateFiles.elementAt(j));
            }
        }
        Enumeration enum = files.elements();
        while(enum.hasMoreElements()) {
            CmsFile file = (CmsFile)enum.nextElement();
            if(file.getState() != I_CmsConstants.C_STATE_DELETED && checkVisible(cms, file)) {
                String nicename = cms.readProperty(file.getAbsolutePath(), I_CmsConstants.C_PROPERTY_TITLE);
                if(nicename == null) {
                    nicename = file.getName();
                }
                names.addElement(nicename);
                values.addElement(file.getAbsolutePath());
            }
        }
        Utils.bubblesort(names, values);

        // find the correct index for the current template

        if(currentTemplate != null) {
            for(int i = 0; i < values.size(); i++) {
                String template = (String) values.get(i);
                if(currentTemplate.equals(template)) {
                    // found the correct index - return it
                    return new Integer(i);
                }
            }
        }

        return new Integer(0);
    }


    /**
     * Check if this template should be displayed in the selectbox (tis is only if
     *  the visible flag is set for the current user or if he is admin).
     * @param cms The CmsObject
     * @param res The resource to be checked.
     * @return True or false.
     * @exception CmsException if something goes wrong.
     */
    private static boolean checkVisible(CmsObject cms, CmsResource res) throws CmsException {
        boolean access = false;
        int accessflags = res.getAccessFlags();

        // First check if the user may have access by one of his groups.
        boolean groupAccess = false;
        Enumeration allGroups = cms.getGroupsOfUser(cms.getRequestContext().currentUser().getName()).elements();
        while((!groupAccess) && allGroups.hasMoreElements()) {
            groupAccess = cms.readGroup(res).equals((CmsGroup)allGroups.nextElement());
        }
        if(((accessflags & I_CmsConstants.C_ACCESS_PUBLIC_VISIBLE) > 0) || (cms.readOwner(res).equals(cms.getRequestContext().currentUser()) && (accessflags & I_CmsConstants.C_ACCESS_OWNER_VISIBLE) > 0) || (groupAccess && (accessflags & I_CmsConstants.C_ACCESS_GROUP_VISIBLE) > 0) || (cms.getRequestContext().currentUser().getName().equals(I_CmsConstants.C_USER_ADMIN))) {
            access = true;
        }
        return access;
    }
}
