/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourceFolder.java,v $
 * Date   : $Date: 2000/04/03 10:48:32 $
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

import org.w3c.dom.*;
import org.xml.sax.*;

import javax.servlet.http.*;

import java.util.*;
import java.io.*;

/**
 * Template class for displaying the new resource screen for a new folder
 * of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.6 $ $Date: 2000/04/03 10:48:32 $
 */
public class CmsNewResourceFolder extends CmsWorkplaceDefault implements I_CmsWpConstants,
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
     * Gets the content of the new resource page template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The new folder template file
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

        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
        //get the current filelist
        String currentFilelist=(String)session.getValue(C_PARA_FILELIST);
        if (currentFilelist==null) {
                currentFilelist=cms.rootFolder().getAbsolutePath();
        }   
        // get request parameters
        String newFolder=(String)parameters.get(C_PARA_NEWFOLDER);
        String title=(String)parameters.get(C_PARA_TITLE);
        String navtitle=(String)parameters.get(C_PARA_NAVTEXT);       
        String navpos=(String)parameters.get(C_PARA_NAVPOS);   
        // get the current phase of this wizard
        String step=cms.getRequestContext().getRequest().getParameter("step");
        if (step != null) {
            if (step.equals("1")) {
                try {
                            
                   // create the folder
                   CmsFolder folder=cms.createFolder(currentFilelist,newFolder);
                   cms.lockResource(folder.getAbsolutePath());
                   cms.writeProperty(folder.getAbsolutePath(),C_PROPERTY_TITLE,title);
         
                   // now check if navigation informations have to be added to the new page.
                   if (navtitle != null) {
                       cms.writeProperty(folder.getAbsolutePath(),C_PROPERTY_NAVTEXT,navtitle);                       
                        
                        // update the navposition.
                        if (navpos != null) {
                            updateNavPos(cms,folder,navpos);
                        }
                   }
                    cms.unlockResource(folder.getAbsolutePath());
                  } catch (CmsException ex) {
                    throw new CmsException("Error while creating new Folder"+ex.getMessage(),ex.getType(),ex);
                }
            
                // TODO: ErrorHandling
                
                // now return to filelist
                template="update";
               /* try {
                    cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
                } catch (Exception e) {
                      throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST,CmsException.C_UNKNOWN_EXCEPTION,e);
                }*/
            }
        } else {
            //session.removeValue(C_PARA_FOLDER);
        }
        // get the document to display
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);          
        // process the selected template 
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
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
                if (file.getState() != C_STATE_DELETED) {
                    String nicename=cms.readProperty(file.getAbsolutePath(),C_PROPERTY_TITLE);
                    if (nicename == null) {
                           nicename=file.getName();
                    }
                    names.addElement(nicename);
                    values.addElement(file.getAbsolutePath());
                }
            }
            return new Integer(0);           
      }
      
      /**
      * Gets the files displayed in the navigation select box.
      * @param cms The CmsObject.
      * @param lang The langauge definitions.
      * @param names The names of the new rescources.
      * @param values The links that are connected with each resource.
      * @param parameters Hashtable of parameters (not used yet).
      * @returns The vectors names and values are filled with data for building the navigation.
      * @exception Throws CmsException if something goes wrong.
      */
      public Integer getNavPos(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
            throws CmsException {

            // get the nav information
            Hashtable storage = getNavData(cms);
            
            if (storage.size() >0) {
                String[] nicenames=(String[])storage.get("NICENAMES");
                int count=((Integer)storage.get("COUNT")).intValue();      
    
                // finally fill the result vectors
                for (int i=0;i<=count;i++) {
                    names.addElement(nicenames[i]);
                    values.addElement(nicenames[i]);
                }
            } else {
                values=new Vector();
            }
   
            return new Integer(values.size()-1);           
      }      
    
      
    /**
     * Updates the navigation position of all resources in the actual folder.
     * @param cms The CmsObject.
     * @param newfile The new file added to the nav.
     * @param navpos The file after which the new entry is sorted.
     */  
    private void updateNavPos(A_CmsObject cms, CmsFolder newfolder, String newpos)
        throws CmsException {
        
            float newPos=0;
              
            // get the nav information
            Hashtable storage = getNavData(cms);
            
            if (storage.size() >0 ) {
                String[] nicenames=(String[])storage.get("NICENAMES");
                String[] positions=(String[])storage.get("POSITIONS");
                int count=((Integer)storage.get("COUNT")).intValue();                    
            
                // now find the file after which the new file is sorted
                int pos=0;
                for (int i=0;i<nicenames.length;i++) {
                    if (newpos.equals((String)nicenames[i])) {
                        pos=i;
                    }                                                              
                }
             
                 if (pos < count) {
                     float low=new Float(positions[pos]).floatValue();
                     float high=new Float(positions[pos+1]).floatValue();
                    newPos= (high+low)/2;
                } else {
                     newPos= new Float(positions[pos]).floatValue()+1;
                }
            } else {
                newPos=1;
            }
            cms.writeProperty(newfolder.getAbsolutePath(),C_PROPERTY_NAVPOS,new Float(newPos).toString());             
      }
    
      /**
      * Sorts a set of three String arrays containing navigation information depending on 
      * their navigation positions.
      * @param cms Cms Object for accessign files.
      * @param filenames Array of filenames
      * @param nicenames Array of well formed navigation names
      * @param positions Array of navpostions
      */
      private void sort(A_CmsObject cms, String[] filenames, String[] nicenames,
                                 String[] positions,int  max){
        // Sorting algorithm
        // This method uses an bubble sort, so replace this with something more
        // efficient
       
        for (int i=max-1;i>1;i--) {
            for (int j=1;j<i;j++) {
                
                float a=new Float(positions[j]).floatValue();
                float b=new Float(positions[j+1]).floatValue();
                if (a > b) {
                    String tempfilename= filenames[j];
                    String tempnicename = nicenames[j];
                    String tempposition = positions[j];
                    filenames[j]=filenames[j+1];
                    nicenames[j]=nicenames[j+1];
                    positions[j]=positions[j+1];
                    filenames[j+1]=tempfilename;
                    nicenames[j+1]=tempnicename;
                    positions[j+1]=tempposition;                    
                }
            }
        }
      }
      
      /** 
       * Gets all required navigation information from the files and subfolders of a folder.
       * A file list of all files and folder is created, for all those resources, the navigation
       * property is read. The list is sorted by their navigation position.
       * @param cms The CmsObject.
       * @return Hashtable including three arrays of strings containing the filenames, 
       * nicenames and navigation positions.
       * @exception Throws CmsException if something goes wrong.
       */
      private Hashtable getNavData(A_CmsObject cms) 
           throws CmsException {
            HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   

            CmsXmlLanguageFile lang= new CmsXmlLanguageFile(cms);
            
            String[] filenames;
            String[] nicenames;
            String[] positions;
            
            Hashtable storage=new Hashtable();
            
            CmsFolder folder=null;
            CmsFile file=null;
            String nicename=null;
            String currentFilelist=null;
            int count=1;
            float max=0;
            
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
                      
            if (filefolders.size()>0) {
            // Create some arrays to store filename, nicename and position for the
            // nav in there. The dimension of this arrays is set to the number of
            // found files and folders plus two more entrys for the first and last
            // element.
            filenames=new String[filefolders.size()+2];
            nicenames=new String[filefolders.size()+2];
            positions=new String[filefolders.size()+2];
                 
                //now check files and folders that are not deleted and include navigation
                // information
                enum=filefolders.elements();
                while (enum.hasMoreElements()) {
                    CmsResource res =(CmsResource)enum.nextElement();
                
                    // check if the resource is not marked as deleted
                    if (res.getState() != C_STATE_DELETED) {
                        String navpos= cms.readProperty(res.getAbsolutePath(),C_PROPERTY_NAVPOS);                    

                        // check if there is a navpos for this file/folder
                        if (navpos!= null) {
                            nicename=cms.readProperty(res.getAbsolutePath(),C_PROPERTY_NAVTEXT);
                            if (nicename == null) {
                                nicename=res.getName();
                            }
                        // add this file/folder to the storage.                        
                        filenames[count]=res.getAbsolutePath();
                        nicenames[count]=nicename;
                        positions[count]=navpos;     
                        if (new Float(navpos).floatValue() > max) {
                             max = new Float(navpos).floatValue();
                        }
                        count++;
                        
                        }
                    }
                }
            } else {
                filenames=new String[2];
                nicenames=new String[2];
                positions=new String[2];
            }
             
            // now add the first and last value
            filenames[0]="FIRSTENTRY";
            nicenames[0]=lang.getDataValue("input.firstelement");
            positions[0]="0";
            filenames[count]="LASTENTRY";
            nicenames[count]=lang.getDataValue("input.lastelement");
            positions[count]=new Float(max+1).toString();
        
            // finally sort the nav information.
            sort(cms,filenames,nicenames,positions,count);     
     
            // put all arrays into a hashtable to return them to the calling method.
            storage.put("FILENAMES",filenames);
            storage.put("NICENAMES",nicenames);
            storage.put("POSITIONS",positions);
            storage.put("COUNT",new Integer(count));
         
            
            return storage;            
      }
}