package com.opencms.workplace;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourceLink.java,v $
 * Date   : $Date: 2000/08/22 13:33:57 $
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
 * Template class for displaying the new resource screen for a new link
 * of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.15 $ $Date: 2000/08/22 13:33:57 $
 */
public class CmsNewResourceLink extends CmsWorkplaceDefault implements I_CmsWpConstants,
																   I_CmsConstants {
	  
	/**
	 * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
	 * Gets the content of the new resource othertype template and processed the data input.
	 * @param cms The CmsObject.
	 * @param templateFile The lock template file
	 * @param elementName not used
	 * @param parameters Parameters of the request and the template.
	 * @param templateSelector Selector of the template tag to be displayed.
	 * @return Bytearry containing the processed data of the template.
	 * @exception Throws CmsException if something goes wrong.
	 */
	public byte[] getContent(CmsObject cms, String templateFile, String elementName, 
							 Hashtable parameters, String templateSelector)
		throws CmsException {
		// the template to be displayed
		String template=null;
		String filename=null;
		// String title=null;
		String link=null;
		String foldername=null;
		String type=null;
		I_CmsSession session= cms.getRequestContext().getSession(true);
		CmsXmlLanguageFile lang=new CmsXmlLanguageFile(cms);
		
		// get the document to display
		CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);          
	
		// clear session values on first load
		String initial=(String)parameters.get(C_PARA_INITIAL);
		if (initial!= null) {
			// remove all session values 
			session.removeValue(C_PARA_FILE); 
			session.removeValue(C_PARA_LINK);    
			session.removeValue(C_PARA_VIEWFILE); 
			session.removeValue("lasturl");
		}
		
		getLastUrl(cms, parameters);
			
		link=cms.getRequestContext().getRequest().getParameter(C_PARA_LINK);
		if (link!= null) {
			session.putValue(C_PARA_LINK,link);
		}
		
		
		// get the parameters
		String notChange=(String)parameters.get("newlink");
		String linkName=null;
		CmsFile editFile = null;
		String content = null;
		String step=cms.getRequestContext().getRequest().getParameter("step");
	   	if (notChange!=null && notChange.equals("false")&& step==null)	{ 
			
			linkName =(String)parameters.get("file");
			editFile = cms.readFile(linkName);
			content = new String(editFile.getContents()); 
			xmlTemplateDocument.setData("LINKNAME", editFile.getName()); 
			xmlTemplateDocument.setData("LINK", editFile.getAbsolutePath());
			xmlTemplateDocument.setData("LINKVALUE", content);  
			template="change"; 
		}	
		filename=cms.getRequestContext().getRequest().getParameter(C_PARA_FILE);
		if (filename!= null) {
			session.putValue(C_PARA_FILE,filename); 
		}

		link=cms.getRequestContext().getRequest().getParameter(C_PARA_LINK);
		if (link!= null) {
			session.putValue(C_PARA_LINK,link);
		}
		
		// get the current phase of this wizard
	   
		if (step != null) {
			// step 1 - show the final selection screen
			if (step.equals("1")) {
				// step 1 - create the link
	            // get folder- and filename
		 
		        foldername=(String)session.getValue(C_PARA_FILELIST);
				if (foldername==null) {
				   foldername=cms.rootFolder().getAbsolutePath();
				}   
				filename=(String)session.getValue(C_PARA_FILE);
				link=(String)session.getValue(C_PARA_LINK); 
				String title= lang.getLanguageValue("explorer.linkto") + " " + link;
				type="link";
   
				if (notChange!=null && notChange.equals("false")){
					// change old file
					linkName =(String)parameters.get("file");
					editFile = cms.readFile(linkName);
					editFile.setContents(link.getBytes());
					cms.writeFile(editFile);    
					cms.writeProperty(linkName,C_PROPERTY_TITLE, title);   

				} else{
					// create the new file

					cms.createFile(foldername,filename,link.getBytes(),type);  

					cms.lockResource(foldername+filename);

					cms.writeProperty(foldername+filename,C_PROPERTY_TITLE, title);    
	 
				}   
				// remove values from session
					session.removeValue(C_PARA_FILE);   
				session.removeValue(C_PARA_VIEWFILE); 
				session.removeValue(C_PARA_LINK);  
				// TODO: ErrorHandling
				
				// now return to appropriate filelist
				try {

					String lastUrl = (String) session.getValue("lasturl");
					String redirectUrl;
  		if (lastUrl != null) {
						cms.getRequestContext().getResponse().sendRedirect(lastUrl);	
					} else {
						cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);	
					} 
				} catch (Exception e) {
 
					  throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST,CmsException.C_UNKNOWN_EXCEPTION,e);
				}
				return null;
			}
		} else {
			session.removeValue(C_PARA_FILE);
			session.removeValue(C_PARA_VIEWFILE); 
		}
		String cancelUrl;
		cancelUrl= (String) session.getValue("lasturl");
		if (cancelUrl== null) {
			cancelUrl = C_WP_EXPLORER_FILELIST;
		}
		xmlTemplateDocument.setData("lasturl", cancelUrl); 
 
		// process the selected template 
		return startProcessing(cms,xmlTemplateDocument,elementName,parameters,template);  
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
	/**
	 * Sets the value of the new file input field of dialog.
	 * This method is directly called by the content definiton.
	 * @param Cms The CmsObject.
	 * @param lang The language file.
	 * @param parameters User parameters.
	 * @return Value that is set into the new file dialod.
	 * @exception CmsExeption if something goes wrong.
	 */
	public String setValue(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters)
		throws CmsException {
		I_CmsSession session= cms.getRequestContext().getSession(true);
		
		// get a previous value from the session
		String filename=(String)session.getValue(C_PARA_FILE);
		if ( filename == null){
			filename="";
		}
		return filename;       
	}
}
