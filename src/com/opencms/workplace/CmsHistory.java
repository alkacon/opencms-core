package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsHistory.java,v $
 * Date   : $Date: 2000/08/08 14:08:31 $
 * Version: $Revision: 1.15 $
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

import javax.servlet.http.*;

import java.util.*;

/**
 * Template class for displaying the history file screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.15 $ $Date: 2000/08/08 14:08:31 $
 */
public class CmsHistory extends CmsWorkplaceDefault implements I_CmsWpConstants,
															 I_CmsConstants {
	   
	/**
	 * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
	 * Gets the content of the history template and processed the data input.
	 * @param cms The CmsObject.
	 * @param templateFile The history template file
	 * @param elementName not used
	 * @param parameters Parameters of the request and the template.
	 * @param templateSelector Selector of the template tag to be displayed.
	 * @return Bytearre containgine the processed data of the template.
	 * @exception Throws CmsException if something goes wrong.
	 */
	public byte[] getContent(CmsObject cms, String templateFile, String elementName, 
							 Hashtable parameters, String templateSelector)
		throws CmsException {
		I_CmsSession session= cms.getRequestContext().getSession(true);
		
		// the template to be displayed
		String template=null;
		
		   
		// clear session values on first load
		String initial=(String)parameters.get(C_PARA_INITIAL);
		if (initial!= null) {
			// remove all session values
			session.removeValue(C_PARA_FILE);
			session.removeValue(C_PARA_PROJECT);
			session.removeValue("lasturl");
		}
		
		
		// get the filename
		String filename=(String)parameters.get(C_PARA_FILE);
		if (filename != null) {
			session.putValue(C_PARA_FILE,filename);        
		}
		filename=(String)session.getValue(C_PARA_FILE);
		
		// get the project
		String projectId=(String)parameters.get(C_PARA_PROJECT);
		Integer id = null;
		if (projectId != null) {
			id = new Integer( Integer.parseInt(projectId) );
			session.putValue(C_PARA_PROJECT,id);
		}
			
		CmsFile file=(CmsFile)cms.readFileHeader(filename);
		
		CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);          
			  
		// test if the prohject paremeter was included, display the detail dialog.
		if (id != null) {
			template="detail";
			CmsProject project=cms.readProject(id.intValue());
			xmlTemplateDocument.setData("PROJECT",project.getName());
			String title=cms.readProperty(filename,C_PROPERTY_TITLE);
			if (title== null) {
					title="";
			}            
			xmlTemplateDocument.setData("TITLE",title);
			xmlTemplateDocument.setData("SIZE",new Integer(file.getLength()).toString());
			xmlTemplateDocument.setData("EDITEDBY","Not yet available");
			xmlTemplateDocument.setData("EDITEDAT",Utils.getNiceDate(file.getDateLastModified()));
			xmlTemplateDocument.setData("PUBLISHEDBY","Not yet available");
			String published="---";
			if (project.getFlags() == this.C_PROJECT_STATE_ARCHIVE) {
			published=Utils.getNiceDate(project.getPublishingDate());                
			}                             
			xmlTemplateDocument.setData("PUBLISHEDAT",published);
			xmlTemplateDocument.setData("PROJECTDESCRIPTION",project.getDescription());
		}
		
		
		
		xmlTemplateDocument.setData("FILENAME",file.getName());
		// process the selected template 
		return startProcessing(cms,xmlTemplateDocument,"",parameters,template);   
	}
	  /**
	 * Gets all groups that can new group of the resource.
	 * <P>
	 * The given vectors <code>names</code> and <code>values</code> will 
	 * be filled with the appropriate information to be used for building
	 * a select box.
	 * 
	 * @param cms CmsObject Object for accessing system resources.
	 * @param names Vector to be filled with the appropriate values in this method.
	 * @param values Vector to be filled with the appropriate values in this method.
	 * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
	 * @return Index representing the current value in the vectors.
	 * @exception CmsException
	 */
	public Integer getFiles(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
		throws CmsException {

		I_CmsSession session= cms.getRequestContext().getSession(true);
	   
		String filename=(String)session.getValue(C_PARA_FILE);
		if (filename != null) {
			Vector allFiles=cms.readAllFileHeaders(filename);
		 
			if (allFiles.size() >0) {
				allFiles=Utils.sort(cms,allFiles,Utils.C_SORT_PUBLISHED_DOWN);                              
			}
		  
		    // fill the names and values
		    for(int i = 0; i < allFiles.size(); i++) {
			    CmsFile file = ((CmsFile)allFiles.elementAt(i));
				if (file.getState() != C_STATE_UNCHANGED) {
					CmsProject project=cms.readProject(file);
					if (project.getFlags() ==  this.C_PROJECT_STATE_ARCHIVE){
					
						String projectName="unknown Project";
				        String projectId = "-1"; 
				
						if (project != null) {
							projectName=project.getName();
		        			projectId=project.getId()+"";
						}
					
						//long updated = file.getDateLastModified();                                                   
						long updated = cms.readProject(file).getPublishingDate();
						String output=Utils.getNiceDate(updated)+": "+projectName;
					
			   	        names.addElement(output);
				        values.addElement(projectId);       
					}
				}
	    	}
		}
	
		return new Integer(-1);
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
