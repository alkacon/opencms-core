/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsHelperMastertemplates.java,v $
* Date   : $Date: 2003/07/31 13:19:36 $
* Version: $Revision: 1.19 $
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

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * Helper class to receive all mastertemplates that are currently in the system.
 * @version $Revision: 1.19 $ $Date: 2003/07/31 13:19:36 $
 */

public class CmsHelperMastertemplates {

    /**
     * Gets the templates displayed in the template select box.
     * @param cms The CmsObject.
     * @param names Will be filled with the display names of found the templates.
     * @param values Will be filled with the file names of the found templates.
     * @param currentTemplate The file name of the currently selected template.
     * @return The index of the currently selected template and the vectors names and values, filled with the information 
     * about the available templates. The index refers to the vectors.
     * @throws Throws CmsException if something goes wrong.
     */
    public static Integer getTemplates(CmsObject cms, Vector names, Vector values, String currentTemplate) throws CmsException {
        return getTemplates(cms, names, values, currentTemplate, 0);
    }

    /**
     * Gets the templates displayed in the template select box.
     * @param cms The CmsObject.
     * @param names Will be filled with the display names of found the templates.
     * @param values Will be filled with the file names of the found templates.
     * @param currentTemplate The file name of the currently selected template.
     * @param defaultReturnValue The index used if no currentTemplate was found.
     * @return The index of the currently selected template and the vectors names and values, filled with the information 
     * about the available templates. The index refers to the vectors.
     * @throws Throws CmsException if something goes wrong.
     */
    public static Integer getTemplates(CmsObject cms, Vector names, Vector values, String currentTemplate, int defaultReturnValue) throws CmsException {
        // first read the available templates from the VFS
        getTemplateElements(cms, I_CmsWpConstants.C_VFS_DIR_TEMPLATES, names, values);
         // find the correct index for the current template
        if(currentTemplate != null) {
           // it's required to do directory translation if comparing directory names 
           currentTemplate = cms.getRequestContext().getDirectoryTranslator().translateResource(I_CmsConstants.C_VFS_DEFAULT + currentTemplate);        
           for(int i = 0; i < values.size(); i++) {
                String template =  cms.getRequestContext().getDirectoryTranslator().translateResource(I_CmsConstants.C_VFS_DEFAULT + ((String)values.get(i)));
                if(currentTemplate.equals(template)) {
                    // found the correct index - return it
                    return new Integer(i);
                }
            }
        }
        return new Integer(defaultReturnValue);
    }

    /**
     * Gets the templates displayed in the template select box.
     * @param cms The CmsObject.
     * @param subFolder The sub folder name in the modules to look for information.
     * @param names Will be filled with the display names of found the templates.
     * @param values Will be filled with the file names of the found templates.
     * @throws Throws CmsException if something goes wrong.
     */
    public static void getTemplateElements(CmsObject cms, String subFolder, Vector names, Vector values) throws CmsException {       
        List files = (List) new ArrayList();  

        // get all selected template elements in the module folders
        List modules = (List) new ArrayList();
        modules = cms.getSubFolders(I_CmsWpConstants.C_VFS_PATH_MODULES);
        for(int i = 0;i < modules.size();i++) {
            List moduleTemplateFiles = (List) new ArrayList();
            String folder = cms.readAbsolutePath((CmsFolder)modules.get(i));
            moduleTemplateFiles = cms.getFilesInFolder(folder + subFolder);
            for(int j = 0;j < moduleTemplateFiles.size();j++) {
                files.add(moduleTemplateFiles.get(j));
            }
        }
        
        // now read the "nice name" (ie. title property) for the found elements
        Iterator enum = files.iterator();
        while(enum.hasNext()) {
            CmsFile file = (CmsFile)enum.next();
            if(file.getState() != I_CmsConstants.C_STATE_DELETED && checkVisible(cms, file)) {
                String nicename = cms.readProperty(cms.readAbsolutePath(file), I_CmsConstants.C_PROPERTY_TITLE);
                if(nicename == null) {
                    nicename = file.getResourceName();
                }
                names.addElement(nicename);
                values.addElement(cms.readAbsolutePath(file));
            }
        }
        
        // finally sort the found elemets
        Utils.bubblesort(names, values);        
        
        // no explicit return value, but the parameter vectors are filled with the found values
    }

    /**
     * Check if this template should be displayed in the selectbox (this is only 
     * true if the visible flag is set for the current user or if he is admin).
     * @param cms The CmsObject
     * @param res The resource to be checked.
     * @return True or false.
     * @throws CmsException if something goes wrong.
     */
    public static boolean checkVisible(CmsObject cms, CmsResource res) throws CmsException {
        return cms.hasPermissions(res, I_CmsConstants.C_VIEW_ACCESS);
    }
}
