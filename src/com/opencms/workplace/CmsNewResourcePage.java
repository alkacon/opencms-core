/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourcePage.java,v $
 * Date   : $Date: 2000/02/17 11:03:20 $
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
 * @version $Revision: 1.3 $ $Date: 2000/02/17 11:03:20 $
 */
public class CmsNewResourcePage extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                                   I_CmsConstants {
    
     /** Definition of the Datablock RADIOSIZE */ 
     private final static String C_RADIOSIZE="RADIOSIZE";
     
     /** Vector containing all names of the radiobuttons */
     private Vector m_names = null;
     
     /** Vector containing all links attached to the radiobuttons */
     private Vector m_values = null;
    
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
        String result = null;     
        // the template to be displayed
        String template=null;
        String type=null;
        byte[] content=new byte[0];
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
 
        //get the current filelist
        String currentFilelist=(String)session.getValue(C_PARA_FILELIST);
        
        // get request parameters
        String newFile=(String)parameters.get(C_PARA_NEWFILE);
        String title=(String)parameters.get(C_PARA_TITLE);
        String flags=(String)parameters.get(C_PARA_FLAGS);
        String templatefile=(String)parameters.get(C_PARA_TEMPLATE);
               
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
                   System.err.println("create content");
                   content=createPagefile("com.opencms.workplace.CmsXmlTemplate",
                                          templatefile,
                                          C_CONTENTTEMPLATEPATH+currentFilelist.substring(1,currentFilelist.length())+newFile);              
                   // check if the nescessary folders for the content files are existing.
                   // if not, create the missing folders.
                   System.err.println("check folders");
                   checkFolders(cms,currentFilelist);
                   
                   // create the page file
                   System.err.println("create file "+currentFilelist+newFile);
                   CmsFile file=cms.createFile(currentFilelist,newFile,content,"page");
                   System.err.println("lock file "+file);
                   cms.lockResource(file.getAbsolutePath());
                   System.err.println("write metainfo file "+file);
                   cms.writeMetainformation(file.getAbsolutePath(),C_METAINFO_TITLE,title);
                   
                   // now create the page content file
                   System.err.println("create content "+C_CONTENTBODYPATH+currentFilelist+newFile);
                   CmsFile contentfile=cms.createFile(C_CONTENTBODYPATH+currentFilelist.substring(1,currentFilelist.length()),newFile,new byte[0],"plain");
                   
                   // set the flags for the content file to internal use, the content 
                   // should not be loaded 
           /*        System.err.println("set flags");
                   cms.lockResource(contentfile.getAbsolutePath());
                   contentfile.setAccessFlags(contentfile.getAccessFlags()+C_ACCESS_INTERNAL_READ);
                   System.err.println("update file "+contentfile.getAbsolutePath());
                   cms.writeFile(contentfile);
                   cms.unlockResource(contentfile.getAbsolutePath());
                   System.err.println("done");*/
                } catch (CmsException ex) {
                    throw new CmsException("###"+ex.getMessage(),ex.getType(),ex);
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
                CmsFolder folder= cms.readFolder(completePath+foldername+"/");
              } catch (CmsException e) {
                  // the folder could not be read, so create it.
                  CmsFolder folder=cms.createFolder(completePath,foldername);                              
              }
              completePath+=foldername+"/";        
          }          
     }
    
    
}