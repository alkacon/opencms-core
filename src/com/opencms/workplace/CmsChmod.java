/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsChmod.java,v $
 * Date   : $Date: 2000/04/17 16:11:35 $
 * Version: $Revision: 1.7 $
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
 * Template class for displaying the chmod screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.7 $ $Date: 2000/04/17 16:11:35 $
 */
public class CmsChmod extends CmsWorkplaceDefault implements I_CmsWpConstants,
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
     * Gets the content of the chmod template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The chmod template file
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
        
        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);

        // clear session values on first load
        String initial=(String)parameters.get(C_PARA_INITIAL);
        if (initial!= null) {
            // remove all session values
            session.removeValue(C_PARA_FILE);
        }
        
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);
        String newaccess=(String)parameters.get(C_PARA_NEWACCESS);
 
        // get the filename
        String filename=(String)parameters.get(C_PARA_FILE);
        if (filename != null) {
            session.putValue(C_PARA_FILE,filename);        
        }        
        filename=(String)session.getValue(C_PARA_FILE);
		A_CmsResource file=(A_CmsResource)cms.readFileHeader(filename);
	
        // get all access flags from the request
        String ur=(String)parameters.get("ur");
        String uw=(String)parameters.get("uw");
        String uv=(String)parameters.get("uv");
        String gr=(String)parameters.get("gr");
        String gw=(String)parameters.get("gw");
        String gv=(String)parameters.get("gv");
        String pr=(String)parameters.get("pr");
        String pw=(String)parameters.get("pw");
        String pv=(String)parameters.get("pv");
        String ir=(String)parameters.get("ir");
      
        // check if the newaccess parameter is available. This parameter is set when
        // the access flags are modified.
        if (newaccess != null) {

            // check if the current user has the right to change the group of the
            // resource. Only the owner of a file and the admin are allowed to do this.
            if ((cms.getRequestContext().currentUser().equals(cms.readOwner(file))) ||
               (cms.userInGroup(cms.getRequestContext().currentUser().getName(), C_GROUP_ADMIN))){
                // calculate the new access flags
                int flag=0;
                if (ur!= null) {
                    if (ur.equals("true")){
                        flag+=C_ACCESS_OWNER_READ;
                    }
                }
                if (uw != null) {
                    if (uw.equals("true")){
                        flag+=C_ACCESS_OWNER_WRITE;
                    }
                }           
                if (uv != null) {
                    if (uv.equals("true")){
                        flag+=C_ACCESS_OWNER_VISIBLE;
                    }
                }     
                if (gr != null) {
                    if (gr.equals("true")){
                        flag+=C_ACCESS_GROUP_READ;
                    }
                }
                if (gw != null) {
                    if (gw.equals("true")){
                        flag+=C_ACCESS_GROUP_WRITE;
                    }
                }           
                if (gv  != null) {
                    if (gv.equals("true")){
                        flag+=C_ACCESS_GROUP_VISIBLE;
                    }
                }   
                if (pr != null) {
                    if (pr.equals("true")){
                        flag+=C_ACCESS_PUBLIC_READ;
                    }
                }
                if (pw != null) {
                    if (pw.equals("true")){
                        flag+=C_ACCESS_PUBLIC_WRITE;
                    }
                }           
                if (pv  != null) {
                    if (pv.equals("true")){
                        flag+=C_ACCESS_PUBLIC_VISIBLE;
                    }
                }  
                if (ir != null) {
                    if (ir.equals("true")){                        
                        flag+=C_ACCESS_INTERNAL_READ;
                    }
                }  
                
                // modify the access flags
                cms.chmod(file.getAbsolutePath(),flag);
                
                //check if the file type name is page
			    //if so delete the file body and content
			    // else delete only file
			    if( (cms.getResourceType(file.getType()).getResourceName()).equals(C_TYPE_PAGE_NAME) ){
				    String bodyPath=getBodyPath(cms, (CmsFile)file);
				    int help = C_CONTENTBODYPATH.lastIndexOf("/");
				    String hbodyPath=(C_CONTENTBODYPATH.substring(0,help))+(file.getAbsolutePath());
				    if (hbodyPath.equals(bodyPath)){
                        // set the internal read flag if nescessary
                        if ((flag & C_ACCESS_INTERNAL_READ) ==0 ) {
                            flag+=C_ACCESS_INTERNAL_READ;
                        }
                        cms.chmod(hbodyPath,flag);
				    }
                }      
                session.removeValue(C_PARA_FILE);
                // return to filelist 
                try {
                    if(lasturl == null || "".equals(lasturl)) {
                        cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
                    } else {
                        ((HttpServletResponse)(cms.getRequestContext().getResponse().getOriginalResponse())).sendRedirect(lasturl);                       
                    }                            
			    } catch (Exception e) {
			        throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST,CmsException.C_UNKNOWN_EXCEPTION,e);
			    }
            } else {
                // the current user is not allowed to change the file owner
				xmlTemplateDocument.setData("details", "the current user is not allowed to change the file owner");
                template="error";
                session.removeValue(C_PARA_FILE);
            }
        } 

	    // set all required datablocks
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
   
        // now set the actual access flags i the dialog
        int flags = file.getAccessFlags();
        if ((flags & C_ACCESS_OWNER_READ) >0 ) {
            xmlTemplateDocument.setXmlData("CHECKUR","CHECKED");    
        } else {
            xmlTemplateDocument.setXmlData("CHECKUR"," ");    
        }
        if ((flags & C_ACCESS_OWNER_WRITE) >0 ) {
            xmlTemplateDocument.setXmlData("CHECKUW","CHECKED");    
        } else {
            xmlTemplateDocument.setXmlData("CHECKUW"," ");    
        }
        if ((flags & C_ACCESS_OWNER_VISIBLE) >0 ) {
            xmlTemplateDocument.setXmlData("CHECKUV","CHECKED");    
        } else {
            xmlTemplateDocument.setXmlData("CHECKUV"," ");    
        }     
        if ((flags & C_ACCESS_GROUP_READ) >0 ) {
            xmlTemplateDocument.setXmlData("CHECKGR","CHECKED");    
        } else {
            xmlTemplateDocument.setXmlData("CHECKGR"," ");    
        }
        if ((flags & C_ACCESS_GROUP_WRITE) >0 ) {
            xmlTemplateDocument.setXmlData("CHECKGW","CHECKED");    
        } else {
            xmlTemplateDocument.setXmlData("CHECKGW"," ");    
        }
        if ((flags & C_ACCESS_GROUP_VISIBLE) >0 ) {
            xmlTemplateDocument.setXmlData("CHECKGV","CHECKED");    
        } else {
            xmlTemplateDocument.setXmlData("CHECKGV"," ");    
        }  
        if ((flags & C_ACCESS_PUBLIC_READ) >0 ) {
            xmlTemplateDocument.setXmlData("CHECKPR","CHECKED");    
        } else {
            xmlTemplateDocument.setXmlData("CHECKPR"," ");    
        }
        if ((flags & C_ACCESS_PUBLIC_WRITE) >0 ) {
            xmlTemplateDocument.setXmlData("CHECKPW","CHECKED");    
        } else {
            xmlTemplateDocument.setXmlData("CHECKPW"," ");    
        }
        if ((flags & C_ACCESS_PUBLIC_VISIBLE) >0 ) {
            xmlTemplateDocument.setXmlData("CHECKPV","CHECKED");    
        } else {
            xmlTemplateDocument.setXmlData("CHECKPV"," ");    
        }  
        if ((flags & C_ACCESS_INTERNAL_READ) >0 ) {
            xmlTemplateDocument.setXmlData("CHECKIF","CHECKED");    
        } else {
            xmlTemplateDocument.setXmlData("CHECKIF"," ");    
        }  
        
        // process the selected template 
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
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