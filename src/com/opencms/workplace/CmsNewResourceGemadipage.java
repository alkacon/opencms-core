/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourceGemadipage.java,v $
* Date   : $Date: 2001/10/12 11:57:15 $
* Version: $Revision: 1.1 $
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
import com.opencms.template.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import javax.servlet.http.*;
import java.util.*;
import java.io.*;

public class CmsNewResourceGemadipage extends CmsNewResourcePage {

    /**
     * Gets the templates displayed in the template select box.
     * @param cms The CmsObject.
     * @param lang The langauge definitions.
     * @param names The names of the new rescources.
     * @param values The links that are connected with each resource.
     * @param parameters Hashtable of parameters (not used yet).
     * @returns The vectors names and values are filled with the information found in the
     * workplace.ini.
     * @exception Throws CmsException if something goes wrong.
     */

    public Integer getTemplates(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        //get the current filelist
        String currentFilelist = (String)(cms.getRequestContext().getSession(true)).getValue(C_PARA_FILELIST);
        if(currentFilelist == null) {
            currentFilelist = cms.rootFolder().getAbsolutePath();
        }

        return getRelativeTemplates(cms, names, values, null, currentFilelist);
    }


    /**
     * Gets the templates displayed in the template select box. In this case that are
     * only the templates in the "baseFolder"/system/templates/ path.
     * @param cms The CmsObject.
     * @param names The names of the template (got from the property title if possible).
     * @param values The ablolute path of the templates.
     * @param currentTemplate The template used by the page (only used if called from the editor).
     * @param startFolder The folder where we start searching for the baseFolder.
     * @returns The vectors names and values are filled with the information from the templates
     * in the folder. The returnvalue is 0 or the template used by the page (only for editor).
     * @exception Throws CmsException if something goes wrong.
     */
    public static Integer getRelativeTemplates(CmsObject cms, Vector names, Vector values, String currentTemplate, String startFolder) throws CmsException {

        String C_PROPERTY_WEBC_ID = "WebcontestID";
        String C_GEMADI_PATH = "system/templates/";
        Vector absPaths = new Vector();
        if(!startFolder.endsWith("/")){
            startFolder = startFolder.substring(0, startFolder.lastIndexOf('/')+1);
        }
        // search for the baseFolder (the one with the property "WebcontestID")
        CmsFolder curFolder = cms.readFolder(startFolder);
        String prop = cms.readProperty(curFolder.getAbsolutePath(), C_PROPERTY_WEBC_ID);
        String relativePart = "";
        while(((prop == null) || ("".equals(prop))) && (curFolder.getParent() != null)){
            curFolder = cms.readFolder(curFolder.getParent());
            prop = cms.readProperty(curFolder.getAbsolutePath(), C_PROPERTY_WEBC_ID);
            relativePart += "../";
        }
        int basePathLength = curFolder.getAbsolutePath().length();

        Vector files = cms.getFilesInFolder(curFolder.getAbsolutePath() +C_GEMADI_PATH);

        Enumeration enum = files.elements();
        while(enum.hasMoreElements()) {
            CmsFile file = (CmsFile)enum.nextElement();
            if(file.getState() != I_CmsConstants.C_STATE_DELETED && CmsHelperMastertemplates.checkVisible(cms, file)) {
                String nicename = cms.readProperty(file.getAbsolutePath(), I_CmsConstants.C_PROPERTY_TITLE);
                if(nicename == null) {
                    nicename = file.getName();
                }
                names.addElement(nicename);
                // add the path relative
                values.addElement(relativePart + file.getAbsolutePath().substring(basePathLength));
                absPaths.addElement(file.getAbsolutePath());
            }
        }
        Utils.bubblesort(names, values);

        // find the correct index for the current template
        if(currentTemplate != null) {
            for(int i = 0; i < values.size(); i++) {
                String template = (String) absPaths.get(i);
                if(currentTemplate.equals(template)) {
                    // found the correct index - return it
                    return new Integer(i);
                }
            }
        }
        return new Integer(0);
    }

}