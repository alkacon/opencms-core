package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProjectDelete.java,v $
 * Date   : $Date: 2000/10/31 13:11:29 $
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

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;
import java.io.*;

import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace admin project resent.
 * <P>
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.8 $ $Date: 2000/10/31 13:11:29 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsAdminProjectDelete extends CmsWorkplaceDefault implements I_CmsConstants, I_CmsLogChannels {

	/**
	 * Gets the content of a defined section in a given template file and its subtemplates
	 * with the given parameters. 
	 * 
	 * @see getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters)
	 * @param cms CmsObject Object for accessing system resources.
	 * @param templateFile Filename of the template file.
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 */
	public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
		if(C_DEBUG && A_OpenCms.isLogging()) {
			A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "getting content of element " + ((elementName==null)?"<root>":elementName));
			A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: " + templateFile);
			A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
		}
		
		CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)
													getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
		
		int projectId =  Integer.parseInt((String)parameters.get("projectid"));
		
		CmsProject project = cms.readProject(projectId);
		
		if(parameters.get("ok") != null) {
			// delete the project
			try {
				// change to the online-project, if needed.
				if(project.equals(cms.getRequestContext().currentProject())) {
					cms.getRequestContext().setCurrentProject(cms.onlineProject().getId());
				}
				cms.deleteProject(project.getId());
				templateSelector = "done";
			} catch (CmsException exc) {
				// display error-message
				xmlTemplateDocument.setData("details", Utils.getStackTrace(exc));
				templateSelector = "error";
			}
		} else {
			// show details about the project
			CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
			CmsProjectlist.setListEntryData(cms, lang, xmlTemplateDocument, project);
			// Now load the template file and start the processing
		}
		return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
	}
	/**
	 * Indicates if the results of this class are cacheable.
	 * 
	 * @param cms CmsObject Object for accessing system resources
	 * @param templateFile Filename of the template file 
	 * @param elementName Element name of this template in our parent template.
	 * @param parameters Hashtable with all template class parameters.
	 * @param templateSelector template section that should be processed.
	 * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
	 */
	public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
		return false;
	}
}
