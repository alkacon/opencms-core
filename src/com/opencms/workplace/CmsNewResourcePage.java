/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourcePage.java,v $
 * Date   : $Date: 2000/03/09 17:01:27 $
 * Version: $Revision: 1.16 $
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

import org.w3c.dom.*;
import org.xml.sax.*;

import javax.servlet.http.*;

import java.util.*;
import java.io.*;

/**
 * Template class for displaying the new resource screen for a new page
 * of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.16 $ $Date: 2000/03/09 17:01:27 $
 */
public class CmsNewResourcePage extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                                   I_CmsConstants {
    
     /** Definition of the class */ 
     private final static String C_CLASSNAME="com.opencms.template.CmsXmlTemplate";
    
     
     private static final String C_DEFAULTBODY = "<?xml version=\"1.0\"?>\n<XMLTEMPLATE>\n<TEMPLATE/>\n</XMLTEMPLATE>";
   
     
     
    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the new resource page template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The new page template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearry containing the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, 
                             Hashtable parameters, String templateSelector)
        throws CmsException {
        // the template to be displayed
        String template=null;
        // TODO: check, if this is neede: String type=null;
        byte[] content=new byte[0];
        CmsFile contentFile=null;
        
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
        //get the current filelist
        String currentFilelist=(String)session.getValue(C_PARA_FILELIST);
        if (currentFilelist==null) {
                currentFilelist=cms.rootFolder().getAbsolutePath();
        }   
        // get request parameters
        String newFile=(String)parameters.get(C_PARA_NEWFILE);
        String title=(String)parameters.get(C_PARA_TITLE);
        // TODO: check, if this is neede: String flags=(String)parameters.get(C_PARA_FLAGS);
        String templatefile=(String)parameters.get(C_PARA_TEMPLATE);
        String navtitle=(String)parameters.get(C_PARA_NAVTITLE);       
        String navpos=(String)parameters.get(C_PARA_NAVPOS);   
        // get the current phase of this wizard
        String step=cms.getRequestContext().getRequest().getParameter("step");
        if (step != null) {
            if (step.equals("1")) {
               //check if the fielname has a file extension
                if (newFile.indexOf(".")==-1) {
                    newFile+=".html";
                }
                try {
                   // create the content for the page file
                   content=createPagefile(C_CLASSNAME,                                          templatefile,
                                          C_CONTENTBODYPATH+currentFilelist.substring(1,currentFilelist.length())+newFile);              
                   // check if the nescessary folders for the content files are existing.
                   // if not, create the missing folders.
                   checkFolders(cms,currentFilelist);
                   
                   // create the page file
                   CmsFile file=cms.createFile(currentFilelist,newFile,content,"page");
                   cms.lockResource(file.getAbsolutePath());
                   cms.writeMetainformation(file.getAbsolutePath(),C_METAINFO_TITLE,title);
                   
                   // now create the page content file
                    try {
                        contentFile=cms.readFile(C_CONTENTBODYPATH+currentFilelist.substring(1,currentFilelist.length()),newFile);
                   } catch (CmsException e) {
                        if (contentFile == null) {
                             contentFile=cms.createFile(C_CONTENTBODYPATH+currentFilelist.substring(1,currentFilelist.length()),newFile,C_DEFAULTBODY.getBytes(),"plain");
                        }
                   }
                        
                   // set the flags for the content file to internal use, the content 
                   // should not be loaded 
                   cms.lockResource(contentFile.getAbsolutePath());
                   
                   cms.chmod(contentFile.getAbsolutePath(), contentFile.getAccessFlags()+C_ACCESS_INTERNAL_READ);
                                   
                   // now check if navigation informations have to be added to the new page.
                   if (navtitle != null) {
                        cms.writeMetainformation(file.getAbsolutePath(),C_METAINFO_NAVTITLE,navtitle);                       
                        
                        // update the navposition.
                        if (navpos != null) {
                            updateNavPos(cms,file,new Integer(navpos).intValue());
                        }
                   }
                   
                   
                  } catch (CmsException ex) {
                    throw new CmsException("Error while creating new Page"+ex.getMessage(),ex.getType(),ex);
                }
            
                // TODO: ErrorHandling
                
                // now return to filelist
                try {
                    cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
                } catch (Exception e) {
                      throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST,CmsException.C_UNKNOWN_EXCEPTION,e);
                }
            }
        } else {
            session.removeValue(C_PARA_FILE);
        }
        // get the document to display
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);          
        // process the selected template 
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
    }
    
    /** 
     * Create the pagefile for this new page.
     * @classname The name of the class used by this page.
     * @template The name of the template (content) used by this page.
     * @return Bytearray containgin the XML code for the pagefile.
     */
    private byte[] createPagefile(String classname, String template, String contenttemplate)
        throws CmsException{
        byte[] xmlContent= null;
        try {
            I_CmsXmlParser parser = A_CmsXmlContent.getXmlParser();
		    Document docXml = parser.createEmptyDocument("page");	
        
            Element firstElement = docXml.getDocumentElement();
        
            // add element CLASS
            Element elClass= docXml.createElement("CLASS");
		    firstElement.appendChild(elClass);
            Node noClass = docXml.createTextNode(classname);
		    elClass.appendChild(noClass);
        
            // add element MASTERTEMPLATE
            Element elTempl= docXml.createElement("MASTERTEMPLATE");
		    firstElement.appendChild(elTempl);
            Node noTempl = docXml.createTextNode(template);
		    elTempl.appendChild(noTempl);     
        
            //add element ELEMENTDEF
            Element elEldef=docXml.createElement("ELEMENTDEF");
            elEldef.setAttribute("name","body");
    	    firstElement.appendChild(elEldef);    
        
            //add element ELEMENTDEF.CLASS
            Element elElClass= docXml.createElement("CLASS");
		    elEldef.appendChild(elElClass);
            Node noElClass = docXml.createTextNode(classname);
		    elElClass.appendChild(noElClass);
        
            //add element ELEMENTDEF.TEMPLATE
            Element elElTempl= docXml.createElement("TEMPLATE");
		    elEldef.appendChild(elElTempl);
            Node noElTempl = docXml.createTextNode(contenttemplate);
		    elElTempl.appendChild(noElTempl);     
        
            // generate the output
            StringWriter writer = new StringWriter();
            parser.getXmlText(docXml,writer);
            xmlContent = writer.toString().getBytes();
        } catch (Exception e) {
            throw new CmsException(e.getMessage(),CmsException.C_UNKNOWN_EXCEPTION,e);
        }
               
        return xmlContent;
    }
    
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
      public Integer getTemplates(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
            throws CmsException {

            Vector files=cms.getFilesInFolder(C_CONTENTTEMPLATEPATH);
             Enumeration enum=files.elements();
            while (enum.hasMoreElements()) {
                CmsFile file =(CmsFile)enum.nextElement();
                String nicename=cms.readMetainformation(file.getAbsolutePath(),C_METAINFO_TITLE);
                if (nicename == null) {
                       nicename=file.getName();
                }
                names.addElement(nicename);
                values.addElement(file.getAbsolutePath());
            }
            return new Integer(0);           
      }
    
      /**
       * This method checks if all nescessary folders are exisitng in the content body
       * folder and creates the missing ones. <br>
       * All page contents files are stored in the content body folder in a mirrored directory
       * structure of the OpenCms filesystem. Therefor it is nescessary to create the 
       * missing folders when a new page document is createg.
       * @param cms The CmsObject
       * @param path The path in the CmsFilesystem where the new page should be created.
       * @exception CmsException if something goes wrong.
       */
      private void checkFolders(A_CmsObject cms, String path) 
          throws CmsException {
          String completePath=C_CONTENTBODYPATH;
          StringTokenizer t=new StringTokenizer(path,"/");
          // check if all folders are there
          while (t.hasMoreTokens()) {
              String foldername=t.nextToken();
               try {
                // try to read the folder. if this fails, an exception is thrown  
                cms.readFolder(completePath+foldername+"/");
              } catch (CmsException e) {
                  // the folder could not be read, so create it.
                  cms.createFolder(completePath,foldername);                              
              }
              completePath+=foldername+"/";        
          }          
     }
    
      /**
      * Gets the files displayed in the navigation select box.
      * @param cms The CmsObject.
      * @param lang The langauge definitions.
      * @param names The names of the new rescources.
      * @param values The links that are connected with each resource.
      * @param parameters Hashtable of parameters (not used yet).
      * @returns The vectors names and values are filled with the information found in the 
      * workplace.ini.
      * @exception Throws CmsException if something goes wrong.
      */
      public Integer getNavPos(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
            throws CmsException {

            HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
            CmsFolder folder=null;
            CmsFile file=null;
            String nicename=null;
            String currentFilelist=null;
            Hashtable storage=new Hashtable();
            int max=0;
                  
            // get the current folder 
            currentFilelist=(String)session.getValue(C_PARA_FILELIST);
            if (currentFilelist==null) {
                currentFilelist=cms.rootFolder().getAbsolutePath();
            }          
          
            // get all files and folders in the current filelist.
            Vector files=cms.getFilesInFolder(currentFilelist);
            Vector folders=cms.getSubFolders(currentFilelist);
                        
            // combine folder and file vector
            Vector filefolders=new Vector();
            Enumeration enum=folders.elements();
            while (enum.hasMoreElements()) {
                folder=(CmsFolder)enum.nextElement();
                filefolders.addElement(folder);
            }
            enum=files.elements();
            while (enum.hasMoreElements()) {
                file=(CmsFile)enum.nextElement();
                filefolders.addElement(file);
            }
                      
            //now check files and folders that are not deleted and include navigation
            // information
            enum=filefolders.elements();
            while (enum.hasMoreElements()) {
                CmsResource res =(CmsResource)enum.nextElement();
                
                // check if the resource is not marked as deleted
                if (res.getState() != C_STATE_DELETED) {
                    String navpos= cms.readMetainformation(res.getAbsolutePath(),C_METAINFO_NAVPOS);                    

                    // check if there is a navpos for this file/folder
                    if (navpos!= null) {
                        nicename=cms.readMetainformation(res.getAbsolutePath(),C_METAINFO_NAVTITLE);
                        if (nicename == null) {
                            nicename=res.getName();
                        }
                     // add this file/folder to the storage. Use the NavPos as its position                              
                     storage.put(navpos,lang.getDataValue("input.next")+" "+nicename);
                     if (new Integer(navpos).intValue() > max) {
                         max=new Integer(navpos).intValue();
                     }
                   }
                }
            }
            
            // first and last element
            storage.put("0",lang.getDataValue("input.firstelement"));
            storage.put(new Integer(max+1).toString(),lang.getDataValue("input.lastelement"));
            
            // finally fill the result vectors
            for (int i=0;i<=max+1;i++) {
                String name=(String)storage.get(new Integer(i).toString());
                if (name!= null) {
               
                    names.addElement(name);
                    values.addElement(new Integer(i).toString());
                }
            }
            return new Integer(values.size()-1);           
      }      
    
      
    /**
     * Updates the navigation position of all resources in the actual folder.
     * @param cms The CmsObject.
     * @param newfile The new file added to the nav.
     * @param The position of the new file.
     */  
    private void updateNavPos(A_CmsObject cms, CmsFile newfile, int newpos)
        throws CmsException {
               
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
        CmsFolder folder=null;
        CmsFile file=null;
        Hashtable storage = new Hashtable();
        int max=0;
        
        // get the current folder 
        String currentFilelist=(String)session.getValue(C_PARA_FILELIST);
        if (currentFilelist==null) {
            currentFilelist=cms.rootFolder().getAbsolutePath();
        }          
          
        // get all files and folders in the current filelist.
        Vector files=cms.getFilesInFolder(currentFilelist);
        Vector folders=cms.getSubFolders(currentFilelist);
                        
        // combine folder and file vector
        Vector filefolders=new Vector();
        Enumeration enum=folders.elements();
        while (enum.hasMoreElements()) {
            folder=(CmsFolder)enum.nextElement();
            filefolders.addElement(folder);
        }
        enum=files.elements();
        while (enum.hasMoreElements()) {
            file=(CmsFile)enum.nextElement();
            filefolders.addElement(file);
        } 
        
        enum=filefolders.elements();
   
        // get all resources and store them in a hashtable storage for sorting
        while (enum.hasMoreElements()) {
            CmsResource res =(CmsResource)enum.nextElement();      
            // check if deleted
            if (res.getState() != C_STATE_DELETED) {
                String navpos= cms.readMetainformation(res.getAbsolutePath(),C_METAINFO_NAVPOS);                    

                // check if there is a navpos for this file/folder
                if (navpos!= null) {
                    int pos=new Integer(navpos).intValue();
                    if (pos > max) {
                        max=pos;
                    }
                     // add this file/folder to the storage. Use the NavPos as its position                              
                    storage.put(new Integer(pos).toString(),res.getAbsolutePath());       
                }
            }
        }
        
        // alter the newpos if nescessary. This has be done when the "last entry" in the
        // selectbox was selected
        if (newpos>max+1) {
            newpos=max+1;
        }
        // now update all metainformations
        
        // WARNING: THIS HAD TO BE DISABLED BECAUSE OF FILE-LOCK PROBLEMS
        // New files are ALWAYS added at the end of the Nav !
        // TODO: find a workaround to add files at any navpos
        /* for (int i=0;i<=max+2;i++) {
                String name=(String)storage.get(new Integer(i).toString());
                int pos=i;
                // add the new file
                if (i==newpos+1) {                   
                    cms.writeMetainformation(newfile.getAbsolutePath(),C_METAINFO_NAVPOS,new Integer(newpos+1).toString());
                 }
                // for all files displayed after the new file -> alter the nav position
                if (i>newpos) {
                    pos=i+1;
                }
                // update the existing nav entrys
                if (name!= null) {
                    cms.writeMetainformation(name,C_METAINFO_NAVPOS,new Integer(pos).toString());
                }
            } */
        if (max<20) {
            max=20;
        }
        cms.writeMetainformation(newfile.getAbsolutePath(),C_METAINFO_NAVPOS,new Integer(max+1).toString());             
      }
}