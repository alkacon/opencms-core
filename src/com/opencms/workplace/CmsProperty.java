/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsProperty.java,v $
 * Date   : $Date: 2000/03/27 09:54:25 $
 * Version: $Revision: 1.2 $
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
 * Template class for displaying the property screens of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.2 $ $Date: 2000/03/27 09:54:25 $
 */
public class CmsProperty extends CmsWorkplaceDefault implements I_CmsWpConstants,
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
     * Gets the content of the property template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The property template file
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
  
        // get all parameters and put them into the session
        String filename=(String)parameters.get(C_PARA_FILE);
        if (filename != null) {
            session.putValue(C_PARA_FILE,filename);        
        }
        String metadef =(String)parameters.get("selectmeta");
        if (metadef != null) {
            session.putValue(C_PARA_METADEF,metadef);        
        } 
        
        filename=(String)session.getValue(C_PARA_FILE);
        metadef=(String)session.getValue(C_PARA_METADEF);    
		
        CmsFile file=(CmsFile)cms.readFileHeader(filename);
        
        String edit=(String)parameters.get("EDIT");
        String delete=(String)parameters.get("DELETE");
        String newmetainfo=(String)parameters.get("NEWMETAINFO");
        String newmetadef=(String)parameters.get("ORGANIZE"); 
        
        
        // select the displayed template
        
        // check if the file is locked by the current user.
        // if so, display a different dialog with more functions is shown
        if (file.isLockedBy()==cms.getRequestContext().currentUser().getId()) {                       
            if (edit != null) {
                // display the edit metainfo dialog  
                template="editmetainfo";
            } else if (delete != null) {
                // display the delete metainfo dialog  
                template="deletemetainfo";
             } else if (newmetainfo != null) {
                // display the newmetainfo metainfo dialog  
                template="newmetainfo";
             } else if (newmetadef != null) {
                // display the newmetadef metainfo dialog  
                template="newmetadef";
            } else {
                // set the default display
                template="ownlocked";      
            }
        } 
      
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);
     
        
        System.err.println("******");
        Enumeration enu=parameters.keys();
        while (enu.hasMoreElements()) {
            String key=(String)enu.nextElement();
            String values=(String)parameters.get(key);
            System.err.println(key+" : "+values);
        }
        System.err.println("******"); 

        // now process the data taken form the dialog
        
        // edit was selected
        if (edit != null) {
            // check if a metainfo was selected          
            if (metadef != null) {
                xmlTemplateDocument.setXmlData("METADEF",metadef);
                // check if a edited metainfo is available
                String newValue=(String)parameters.get("EDITEDMETAINFO");
                if (newValue != null) {
                    // update the metainfomration
                    cms.writeMetainformation(filename,metadef,newValue);
                    template="ownlocked";    
                    session.removeValue(C_PARA_METADEF);   
                }
            } else {
                template="ownlocked";    
            }
        }
        
        // delete was selected
        if (delete != null) {
            // check if the ok button was selected
            if (delete.equals("true")) {
                // delete the metadefinition
                if (metadef != null) {
                    cms.deleteMetainformation(filename,metadef);
                    template="ownlocked";    
                    session.removeValue(C_PARA_METADEF);   
                }
            }
        }
        
        // new metainfo was selected
        if (newmetainfo != null) {
            // check if the ok button was selected
            if (newmetainfo.equals("true")) {
                String newValue=(String)parameters.get("NEWMETAVALUE");
                if ((newValue != null) && (metadef !=null)) {
                    // test if this metainfo is already existing
                    String testValue=cms.readMetainformation(filename,metadef);
                    if (testValue == null) {
                        // add the metainfomration                    
                        cms.writeMetainformation(filename,metadef,newValue);
                        template="ownlocked";    
                        session.removeValue(C_PARA_METADEF);   
                    } else {
                        // todo: add an error message that this key is already exisitng
                    }
 
                } else {
                 template="ownlocked";    
                 session.removeValue(C_PARA_METADEF);   
                }                           
            }
        }
        
        // new metadef was selected
        if (newmetadef != null) {
             // check if the ok button was selected
            if (newmetadef.equals("true")) {
                 String newValue=(String)parameters.get("NEWMETADEF");
                 if (newValue != null) {
                    // test if this metainfo is already existing
                    String testValue=cms.readMetainformation(filename,metadef);
                    if (testValue == null) {
                        // add the metainfomration   
                        A_CmsResourceType type=cms.getResourceType(file.getType());
                        A_CmsMetadefinition def=cms.createMetadefinition(newValue,
                                                                          type.getResourceName(),
                                                                          C_METADEF_TYPE_NORMAL);
                        cms.writeMetadefinition(def);
                        template="ownlocked";    
                        session.removeValue(C_PARA_METADEF);   
                    } else {
                        // todo: add an error message that this key is already exisitng
                    }
                     
                 } else {
                 template="ownlocked";    
                 session.removeValue(C_PARA_METADEF);   
                 }
            }
            
        }
        
         // set the required datablocks
        String title=cms.readMetainformation(file.getAbsolutePath(),C_METAINFO_TITLE);
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
     * Gets a formated file state string.
     * @param cms The CmsObject.
     * @param file The CmsResource.
     * @param lang The content definition language file.
     * @return Formated state string.
     */
     private String getState(A_CmsObject cms, CmsResource file,CmsXmlLanguageFile lang)
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
     
     /**
     * Gets all metainformations the file.
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
    public Integer getMetainfo(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
		throws CmsException {
	
		int retValue = -1;
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
       
        String filename=(String)session.getValue(C_PARA_FILE);       
        if (filename != null) {
             Hashtable metainfo = cms.readAllMetainformations(filename);
    
             Enumeration enu=metainfo.keys();
             while (enu.hasMoreElements()) {
                String key=(String)enu.nextElement();
                String value=(String)metainfo.get(key);
	   
                names.addElement(key+":"+value);
		    	values.addElement(key);
             }
		    
        }
        
		// no current user, set index to -1
        return new Integer(retValue);
    }
    
     /**
     * Gets the value of selected metainfo and sets it in the input field of the dialog.
     * This method is directly called by the content definiton.
     * @param Cms The CmsObject.
     * @param lang The language file.
     * @param parameters User parameters.
     * @return Value that is set into the input field.
     * @exception CmsExeption if something goes wrong.
     */
    public String getMetainfoValue(A_CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters)
        throws CmsException {
        
        String metaValue=null;
        
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true); 
        
        // get the filename
        String filename=(String)session.getValue(C_PARA_FILE);        
        if (filename != null) {
             //get the metadefinition
            String metadef=(String)session.getValue(C_PARA_METADEF);  
            if (metadef != null) {
                // everything is there, so try to read the meteainfo
                metaValue=cms.readMetainformation(filename,metadef);
                if (metaValue == null) {
                    metaValue="";
                }   
            }            
        }        
      return metaValue;
    }   
    
     /**
     * Gets all unused metadefintions for the file.
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
    public Integer getMetadef(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
		throws CmsException {
	
		int retValue = -1;
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
       
        String filename=(String)session.getValue(C_PARA_FILE);       
        if (filename != null) {
             CmsFile file=(CmsFile)cms.readFileHeader(filename);
             A_CmsResourceType type=cms.getResourceType(file.getType());
             // get all metadefinitions for this type
             Vector metadef =cms.readAllMetadefinitions(type.getResourceName());
             // get all existing metafinfos of this file
             Hashtable metainfo = cms.readAllMetainformations(filename);   
    
             Enumeration enu=metadef.elements();
             while (enu.hasMoreElements()) {
                CmsMetadefinition meta=(CmsMetadefinition)enu.nextElement();
                 
                String metavalue=(String)metainfo.get(meta.getName());
                if (metavalue == null ) {                  
                    names.addElement(meta.getName());
		    	    values.addElement(meta.getName());
                }
             }
        }
        
		// no current user, set index to -1
        return new Integer(retValue);
    }
    
     /**
     * Gets all  metadefintions for the file.
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
    public Integer getAllMetadef(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
		throws CmsException {
	
		int retValue = -1;
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
       
        String filename=(String)session.getValue(C_PARA_FILE);       
        if (filename != null) {
             CmsFile file=(CmsFile)cms.readFileHeader(filename);
             A_CmsResourceType type=cms.getResourceType(file.getType());
             // get all metadefinitions for this type
             Vector metadef =cms.readAllMetadefinitions(type.getResourceName());
        
             Enumeration enu=metadef.elements();
             while (enu.hasMoreElements()) {
                CmsMetadefinition meta=(CmsMetadefinition)enu.nextElement();                         
                names.addElement(meta.getName());
		    	values.addElement(meta.getName());
             }
        }
        
		// no current user, set index to -1
        return new Integer(retValue);
    }
    
}