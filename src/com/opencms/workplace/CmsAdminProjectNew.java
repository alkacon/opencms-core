/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProjectNew.java,v $
 * Date   : $Date: 2000/03/10 14:11:34 $
 * Version: $Revision: 1.8 $
 *
 * Copyright (C) 2000  The OpenCms Group 
 * 
 * This File is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * For further information about OpenCms, please see the
 * OpenCms Website: http://www.opencms.com
 * 
 * You should have received a copy of the GNU General Public License
 * long with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;

import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace admin project screens.
 * <P>
 * 
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @version $Revision: 1.8 $ $Date: 2000/03/10 14:11:34 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsAdminProjectNew extends CmsWorkplaceDefault implements I_CmsConstants {

    /**
     * Indicates if the results of this class are cacheable.
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }    

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters. 
     * 
     * @see getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "getting content of element " + ((elementName==null)?"<root>":elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
		HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
       
		// read the parameters
        A_CmsRequestContext reqCont = cms.getRequestContext();  
        String newName = (String)parameters.get(C_PROJECTNEW_NAME);
        String newGroup = (String)parameters.get(C_PROJECTNEW_GROUP);
        String newDescription = (String)parameters.get(C_PROJECTNEW_DESCRIPTION);
        String newManagerGroup = (String)parameters.get(C_PROJECTNEW_MANAGERGROUP);
        String newFolder = (String)parameters.get(C_PROJECTNEW_FOLDER);
		
        // modify the folderaname if nescessary (the root folder is always given
        // as a nice name)
        if (newFolder!= null) {
            CmsXmlLanguageFile lang=new CmsXmlLanguageFile(cms);
            if (newFolder.equals(lang.getLanguageValue("title.rootfolder"))) {
                newFolder="/";
            }
        }
        
        // get the current project to restore it later again.
        // to create a project, the workplace must be set to the online project.
        String project=reqCont.currentProject().getName();
        session.putValue(C_PARA_PROJECT,project);
        reqCont.setCurrentProject(cms.onlineProject().getName());
        
		CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);

		// is there any data? 
		if( (newName != null) && (newGroup != null) &&  (newDescription != null) && 
			(newManagerGroup != null) && (newFolder != null) ) {
			// Yes: create new Project
			try {
                //test if the given folder is existing
                CmsFolder folder=cms.readFolder(newFolder);
         		cms.createProject(newName, newDescription, newGroup, newManagerGroup);
				// change the current project
            	reqCont.setCurrentProject(newName);
				// copy the resource the the project
              	cms.copyResourceToProject(newFolder);
				// try to copy the content resources to the project
				try {
					cms.copyResourceToProject(C_CONTENTBODYPATH);
				} catch (CmsException exc) {
					// the content is in the project already - ignore the exception
					A_OpenCms.log(C_OPENCMS_INFO, "Creating project " + newName + ": " + C_CONTENTBODYPATH + " was already in Project.");
				}
				templateSelector = C_PROJECTNEW_DONE;
			} catch(CmsException exc) {

				templateSelector = C_PROJECTNEW_ERROR;
			}
		}
		// Now load the template file and start the processing
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }
	
    /**
     * Gets all groups, that may work for a project.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors.
     * @exception CmsException
     */
    public Integer getGroups(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
		throws CmsException {
		// get all groups
		Vector groups = cms.getGroups();
		int retValue = -1;

		// fill the names and values
		for(int z = 0; z < groups.size(); z++) {
			String name = ((A_CmsGroup)groups.elementAt(z)).getName();
			if(C_GROUP_USERS.equals(name)) {
				retValue = z;
			}
			names.addElement(name);
			values.addElement(((A_CmsGroup)groups.elementAt(z)).getName());
		}
		
		// no current group, set index to -1
        return new Integer(retValue);
    }

    /**
     * Gets all groups, that may manage a project.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors.
     * @exception CmsException
     */
    public Integer getManagerGroups(A_CmsObject cms, CmsXmlLanguageFile lang, 
									Vector names, Vector values, Hashtable parameters) 
		throws CmsException {
		// get all groups
		Vector groups = cms.getGroups();
		int retValue = -1;

		// fill the names and values
		for(int z = 0; z < groups.size(); z++) {
			String name = ((A_CmsGroup)groups.elementAt(z)).getName();
			if(C_GROUP_PROJECTLEADER.equals(name)) {
				retValue = z;
			}
			names.addElement(name);
			values.addElement(((A_CmsGroup)groups.elementAt(z)).getName());
		}
		
		// no current group, set index to -1
        return new Integer(retValue);
    }

    /**
     * Gets all folders, from the cms.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors.
     * @exception CmsException
     */
    public Integer getAllFolders(A_CmsObject cms, CmsXmlLanguageFile lang, 
									Vector names, Vector values, Hashtable parameters) 
		throws CmsException {

		// add the root
		names.addElement(C_ROOT);
		values.addElement(C_ROOT);
		// get all folders		
		getFolders(cms, names, values, C_ROOT);
		// no current folder, set index to -1
        return new Integer(-1);
    }
	
	private void getFolders(A_CmsObject cms, Vector names, Vector values, 
							String currentFolder) 
		throws CmsException {
		
		Vector folders = cms.getSubFolders(currentFolder);

		String folder;

		// fill the names and values
		for(int z = 0; z < folders.size(); z++) {
			folder = ((A_CmsResource)folders.elementAt(z)).getAbsolutePath();
			names.addElement(folder);
			values.addElement(folder);
			getFolders(cms, names, values, folder);
		}
	}

    /**
     * Gets the selected folders.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors.
     * @exception CmsException
     */
    public Integer getSelectedFolders(A_CmsObject cms, CmsXmlLanguageFile lang, 
									Vector names, Vector values, Hashtable parameters) 
		throws CmsException {
		// no current folder, set index to -1
        return new Integer(-1);
    }
}
