/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsHistory.java,v $
 * Date   : $Date: 2000/04/04 10:28:48 $
 * Version: $Revision: 1.3 $
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

import javax.servlet.http.*;

import java.util.*;

/**
 * Template class for displaying the history file screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.3 $ $Date: 2000/04/04 10:28:48 $
 */
public class CmsHistory extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                             I_CmsConstants {
       
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
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, 
                             Hashtable parameters, String templateSelector)
        throws CmsException {
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
        
        // the template to be displayed
        String template=null;
        
        Enumeration enu=parameters.keys();
        System.err.println("#### PARAMETERS");
        while (enu.hasMoreElements()) {
            String key=(String)enu.nextElement();
            System.err.println(key+" : "+parameters.get(key));            
        }
        System.err.println("###############");
        
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
            A_CmsProject project=cms.readProject(id.intValue());
            xmlTemplateDocument.setXmlData("PROJECT",project.getName());
            String title=cms.readProperty(filename,C_PROPERTY_TITLE);
            if (title== null) {
                    title="";
            }            
            xmlTemplateDocument.setXmlData("TITLE",title);
            xmlTemplateDocument.setXmlData("SIZE",new Integer(file.getLength()).toString());
            xmlTemplateDocument.setXmlData("EDITEDBY","Not yet available");
            xmlTemplateDocument.setXmlData("EDITEDAT",Utils.getNiceDate(file.getDateLastModified()));
            xmlTemplateDocument.setXmlData("PUBLISHEDBY","Not yet available");
            String published="---";
            if (project.getFlags() == this.C_PROJECT_STATE_ARCHIVE) {
            published=Utils.getNiceDate(project.getPublishingDate());                
            }                             
            xmlTemplateDocument.setXmlData("PUBLISHEDAT",published);
            xmlTemplateDocument.setXmlData("PROJECTDESCRIPTION",project.getDescription());
        }
        
        
        
        xmlTemplateDocument.setXmlData("FILENAME",file.getName());
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
     * @param cms A_CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors.
     * @exception CmsException
     */
    public Integer getFiles(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
		throws CmsException {

        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
       
        String filename=(String)session.getValue(C_PARA_FILE);
        if (filename != null) {
            Vector allFiles=cms.readAllFileHeaders(filename);
            allFiles=Utils.sort(cms,allFiles,Utils.C_SORT_LASTMODIFIED_DOWN);                              
		  
		    // fill the names and values
		    for(int i = 0; i < allFiles.size(); i++) {
			    CmsFile file = ((CmsFile)allFiles.elementAt(i));
                A_CmsProject project=cms.readProject(file);
                String projectName="unknown Project";
				String projectId = "-1"; 
				
                if (project != null) {
                    projectName=project.getName();
					projectId=project.getId()+"";
                }
                
                long updated = file.getDateLastModified();                                                   
                String output=Utils.getNiceDate(updated)+": "+projectName;
                
			   	names.addElement(output);
    			values.addElement(projectId);       
	    	}
        }
	
        return new Integer(-1);
    }
    
}