/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewResourceUpload.java,v $
 * Date   : $Date: 2000/03/28 09:10:41 $
 * Version: $Revision: 1.1 $
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
 * Template class for displaying the new resource upload screen
 * of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/03/28 09:10:41 $
 */
public class CmsNewResourceUpload extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                                   I_CmsConstants {
    
     /** Vector containing all names of the radiobuttons */
     private Vector m_names = null;
     
     /** Vector containing all links attached to the radiobuttons */
     private Vector m_values = null;
    
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
     * Gets the content of the new resource upload page template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The upload template file
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

        // get the parameters from the request and session
        String step=(String)parameters.get("STEP");                
        String newtype=(String)parameters.get("R_NEU");       
        String currentFolder=(String)session.getValue(C_PARA_FILELIST);
        
        
        // get filename and file content if available
        String filename=null;
        byte[] filecontent=new byte[0];
        
        // get the filename
        Enumeration files=cms.getRequestContext().getRequest().getFileNames();
        while (files.hasMoreElements()) {
           filename=(String)files.nextElement();
        }             
        if (filename != null) {
            session.putValue(C_PARA_FILE,filename);
        }        
        filename=(String)session.getValue(C_PARA_FILE);
        
        // get the filecontent
        if (filename != null) {
            filecontent=cms.getRequestContext().getRequest().getFile(filename);
        }        
        if (filecontent != null) {
            session.putValue(C_PARA_FILECONTENT,filecontent);          
        }
        
        filecontent=(byte[])session.getValue(C_PARA_FILECONTENT);
        
        // there was a file uploaded, so select its type
        if (step != null) {
            if (step.equals("1")) {
                // display the select filetype screen
                if (filename!= null) {
                    template="step1";   
                }
            } else if (step.equals("2")) {
                // create the new file.    
                // todo: error handling if file already exits              
                A_CmsResourceType type=cms.getResourceType(newtype);
                cms.createFile(currentFolder,filename,filecontent,type.getResourceName());
                // remove the values form the session
                session.removeValue(C_PARA_FILE);
                session.removeValue(C_PARA_FILECONTENT);
                // return to the filelist
                try {
                    cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
                } catch (Exception ex) {
                    throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST,CmsException.C_UNKNOWN_EXCEPTION,ex);
                                              
                }
              
            }
            
        }
        
        // get the document to display
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);          
     
       if (filename != null) {
            xmlTemplateDocument.setXmlData("FILENAME",filename);
       }
     
        // process the selected template 
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
    }
    
      /**
      * Gets the resources displayed in the Radiobutton group on the chtype dialog.
      * @param cms The CmsObject.
      * @param lang The langauge definitions.
      * @param names The names of the new rescources.
      * @param values The links that are connected with each resource.
      * @param parameters Hashtable of parameters (not used yet).
      * @returns The vectors names and values are filled with the information found in the 
      * workplace.ini.
      * @exception Throws CmsException if something goes wrong.
      */
      public void getResources(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
            throws CmsException {

           // Check if the list of available resources is not yet loaded from the workplace.ini
            if(m_names == null || m_values == null) {
              m_names = new Vector();
              m_values = new Vector();

            CmsXmlWpConfigFile configFile = new CmsXmlWpConfigFile(cms);            
            configFile.getWorkplaceIniData(m_names, m_values,"RESOURCETYPES","RESOURCE");
            }
            
            // Check if the temportary name and value vectors are not initialized, create 
            // them if nescessary.
            if (names == null) {
                names=new Vector();
            }
            if (values == null) {
                values=new Vector();
            }
            
            // OK. Now m_names and m_values contain all available
            // resource information.
            // Loop through the vectors and fill the result vectors.
            int numViews = m_names.size();        
            for(int i=0; i<numViews; i++) {
                String loopValue = (String)m_values.elementAt(i);
                String loopName = (String)m_names.elementAt(i);
                values.addElement(loopValue);
                names.addElement(loopName);
            }
      }     

      private byte[] getByte(Byte[] array) {
        byte[] output= new byte[array.length];
        for (int i=0; i<array.length;i++) {
            output[i]=array[i].byteValue();
        }
      return output;
  }
   
     
}