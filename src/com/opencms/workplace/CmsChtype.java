/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsChtype.java,v $
 * Date   : $Date: 2000/08/02 13:34:56 $
 * Version: $Revision: 1.12 $
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
 * Template class for displaying the type screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.12 $ $Date: 2000/08/02 13:34:56 $
 */
public class CmsChtype extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                             I_CmsConstants {
           
     /** Definition of the Datablock RADIOSIZE */ 
     private final static String C_RADIOSIZE="RADIOSIZE";

    /** Vector containing all names of the radiobuttons */
     private Vector m_names = null;
     
     /** Vector containing all links attached to the radiobuttons */
     private Vector m_values = null;
    
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
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the chtype template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The chtype template file
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
            session.removeValue("lasturl");
        }        
        
        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);
        
        String newtype=(String)parameters.get(C_PARA_NEWTYPE);
 
        // get the filename
        String filename=(String)parameters.get(C_PARA_FILE);
        if (filename != null) {
            session.putValue(C_PARA_FILE,filename);        
        }        
        filename=(String)session.getValue(C_PARA_FILE);
		CmsFile file=(CmsFile)cms.readFileHeader(filename);
	
      
        // check if the newtype parameter is available. This parameter is set when
        // the new file type is selected
        if (newtype != null) {
            // get the new resource type
            CmsResourceType type=cms.getResourceType(newtype);
            cms.chtype(file.getAbsolutePath(),type. getResourceName());
                
            session.removeValue(C_PARA_FILE);
            // return to filelist 
            try {
                if(lasturl == null || "".equals(lasturl)) {
                    cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
                } else {
                    cms.getRequestContext().getResponse().sendRedirect(lasturl);                       
                }                            
			} catch (Exception e) {
			    throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST,CmsException.C_UNKNOWN_EXCEPTION,e);
			}     
            return null;
        }  
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);
	    // set all required datablocks
        xmlTemplateDocument.setData("OWNER",Utils.getFullName(cms.readOwner(file)));
        xmlTemplateDocument.setData("GROUP",cms.readGroup(file).getName());
        xmlTemplateDocument.setData("FILENAME",file.getName()); 
        
        getResources(cms,null,null,null,null,null);
        if (m_names != null) {
            xmlTemplateDocument.setData(C_RADIOSIZE,new Integer(m_names.size()).toString());
        }
        
        // process the selected template 
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
    }
    	  
      /**
      * Gets the resources displayed in the Radiobutton group on the chtype dialog.
      * @param cms The CmsObject.
      * @param lang The langauge definitions.
      * @param names The names of the new rescources (used for optional images).
      * @param values The links that are connected with each resource.
      * @param descriptions Description that will be displayed for the new resource.
      * @param parameters Hashtable of parameters (not used yet).
      * @returns The vectors names and values are filled with the information found in the 
      * workplace.ini.
      * @exception Throws CmsException if something goes wrong.
      */
      public void getResources(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Vector descriptions, Hashtable parameters) 
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
            if (descriptions == null) {
                descriptions=new Vector();
            }
            
            // OK. Now m_names and m_values contain all available
            // resource information.
            // Loop through the vectors and fill the result vectors.
            int numViews = m_names.size();        
            for(int i=0; i<numViews; i++) {
                String loopValue = (String)m_values.elementAt(i);
                String loopName = (String)m_names.elementAt(i);
                values.addElement(loopValue);
                names.addElement("file_" + loopName);
                String descr;
                if(lang != null) {
                    descr = lang.getLanguageValue("fileicon." + loopName);
                } else {
                    descr = loopName;
                }
                descriptions.addElement(descr);
            }
      }
}
	    
