/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsChown.java,v $
 * Date   : $Date: 2000/04/07 15:22:18 $
 * Version: $Revision: 1.6 $
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
 * Template class for displaying the chown screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.6 $ $Date: 2000/04/07 15:22:18 $
 */
public class CmsChown extends CmsWorkplaceDefault implements I_CmsWpConstants,
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
     * Gets the content of the chown template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The chown template file
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
		CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);        
		
        String newowner=(String)parameters.get(C_PARA_NEWOWNER);
        String filename=(String)parameters.get(C_PARA_FILE);
        if (filename != null) {
            session.putValue(C_PARA_FILE,filename);        
        }
        //check if the lock parameter was included in the request
        // if not, the lock page is shown for the first time
        filename=(String)session.getValue(C_PARA_FILE);
		A_CmsResource file=(A_CmsResource)cms.readFileHeader(filename);
		
        // a new owner was given in the request so try to change it
        if (newowner != null) {

            // check if the current user has the right to change the owner of the
            // resource. Only the owner of a file and the admin are allowed to do this.
            if ((cms.getRequestContext().currentUser().equals(cms.readOwner(file))) ||
                (cms.userInGroup(cms.getRequestContext().currentUser().getName(), C_GROUP_ADMIN))){
                cms.chown(file.getAbsolutePath(),newowner);
                //check if the file type name is page
			    //if so delete the file body and content
			    // else delete only file
			    if( (cms.getResourceType(file.getType()).getResourceName()).equals(C_TYPE_PAGE_NAME) ){
				    String bodyPath=getBodyPath(cms, (CmsFile)file);
				    int help = C_CONTENTBODYPATH.lastIndexOf("/");
				    String hbodyPath=(C_CONTENTBODYPATH.substring(0,help))+(file.getAbsolutePath());
				    if (hbodyPath.equals(bodyPath)){
					    cms.chown(hbodyPath,newowner);
				    }
                }      
                
                session.removeValue(C_PARA_FILE);
                // return to filelist
                try {
		           cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
			    } catch (Exception e) {
			        throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST,CmsException.C_UNKNOWN_EXCEPTION,e);
			    }
            } else {
                // the current user is not allowed to change the file owner
				xmlTemplateDocument.setData("details", "the current user is not allowed to change the file owner");
                template="error";
            }
        }

	    // set the required datablocks
        String title=cms.readProperty(file.getAbsolutePath(),C_PROPERTY_TITLE);
        if (title==null) {
            title="";
        }
        A_CmsUser owner=cms.readOwner(file);
        xmlTemplateDocument.setXmlData("TITLE",title);
        xmlTemplateDocument.setXmlData("STATE",getState(cms,file,new CmsXmlLanguageFile(cms)));
        xmlTemplateDocument.setXmlData("OWNER",owner.getFirstname()+" "+owner.getLastname()+"("+owner.getName()+")");
        xmlTemplateDocument.setXmlData("GROUP",cms.readGroup(file).getName());
		xmlTemplateDocument.setXmlData("FILENAME",file.getName());
   
        
        // process the selected template 
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
    }
	
     /**
     * Gets all users that can new owner of the file.
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
    public Integer getUsers(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
		throws CmsException {
		// get all groups
		Vector users = cms.getUsers();
		int retValue = -1;
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
       
        String filename=(String)session.getValue(C_PARA_FILE);

        if (filename != null) {
            A_CmsResource file=(A_CmsResource)cms.readFileHeader(filename);
    
	    	// fill the names and values
    		for(int z = 0; z < users.size(); z++) {
	    		String name = ((A_CmsUser)users.elementAt(z)).getName();
		    	if(cms.readOwner(file).getName().equals(name)) {
			        retValue = z;
    		    }
	    		names.addElement(name);
		    	values.addElement(((A_CmsUser)users.elementAt(z)).getName());
		    }
        }
        
		// no current user, set index to -1
        return new Integer(retValue);
    }
	
    	  
	/**
	 * method to check get the real body path from the content file
	 * 
	 * @param cms The CmsObject, to access the XML read file.
	 * @param file File in which the body path is stored.
	 */
	private String getBodyPath(A_CmsObject cms, CmsFile file)
		throws CmsException{
		file=cms.readFile(file.getAbsolutePath());
		CmsXmlControlFile hXml=new CmsXmlControlFile(cms, file);
		return hXml.getElementTemplate("body");
	}
	
         
     /**
     * Gets a formated file state string.
     * @param cms The CmsObject.
     * @param file The CmsResource.
     * @param lang The content definition language file.
     * @return Formated state string.
     */
     private String getState(A_CmsObject cms, A_CmsResource file,CmsXmlLanguageFile lang)
         throws CmsException {
         StringBuffer output=new StringBuffer();
         
         if (file.inProject(cms.getRequestContext().currentProject())) {
            int state=file.getState();
            output.append(lang.getLanguageValue("explorer.state"+state));
         } else {
            output.append(lang.getLanguageValue("explorer.statenip"));
         }
         return output.toString();
     }
    
}