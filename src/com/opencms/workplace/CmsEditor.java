/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsEditor.java,v $
 * Date   : $Date: 2000/02/20 16:10:24 $
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

import java.util.*;
import java.io.*;

/**
 * Template class for displaying the text editor of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.7 $ $Date: 2000/02/20 16:10:24 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsEditor extends CmsWorkplaceDefault {

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
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters. 
     * 
     * @see getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(C_DEBUG && A_OpenCms.isLogging()) {
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "getting content of element " + ((elementName==null)?"<root>":elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }

        CmsFile editFile = null;
        
        String content = (String)parameters.get("CONTENT");
        String file = (String)parameters.get("file");
        //String exit = (String)parameters.get("EXIT");
        //String save = (String)parameters.get("save");
        String action = (String)parameters.get("action");
        String jsfile = (String)parameters.get("editor.jsfile");
        
        boolean existsContentParam = (content!=null && (!"".equals(content)));
        boolean existsFileParam = ((file != null) && (!"".equals(file)));
        //boolean saveRequested = ((save != null) && "1".equals(save));
        //boolean exitRequested = ((exit != null) && "1".equals(exit));
        boolean saveRequested = ((action != null) && ("save".equals(action) || "saveexit".equals(action)));
        boolean exitRequested = ((action != null) && ("exit".equals(action) || "saveexit".equals(action)));
        
        
        // If there is a file parameter and no content, try to read the file. 
        // If the user requested a "save file", also load the file.
        if(existsFileParam && (content == null || saveRequested)) {
            try {
                editFile = cms.readFile(file);
            } catch(Exception e) {
                // Anything is wrong. Perhaps a wrong file name ???
                String errorMessage = "Error while reading file " + file + ": " + e;
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_CRITICAL, getClassName() + errorMessage);
                    if(!(e instanceof CmsException)) {
                        // Should not happen. Print out detailled error information                    
                        e.printStackTrace();
                    }
                }
                // throw this exception again, so it can be displayed in the servlet.
                if(e instanceof CmsException) {
                    throw (CmsException)e;
                } else {
                    throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
                }
            }

            // OK. The file was loaded successfully            
            // For further processing we possibly need the encoder            
            Encoder enc = new Encoder();
            
            // If there is no content set, this is the first request of the editor.
            // So load the file content and set the "content" parameter.        
            if(content == null) {
                content = new String(editFile.getContents());
                content = enc.escape(content);
                parameters.put("CONTENT", content);
            }
            
            // If the user requested a file save, write the file content
            // back to the database.            
            if(saveRequested) {
                String decodedContent = enc.unescape(content);
                editFile.setContents(decodedContent.getBytes());
                cms.writeFile(editFile);
            }                
        } // end if(existsFileParam && (content == null))
        
        // Check if we should leave th editor instead of start processing
        if(exitRequested) {
            try {
                cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceMainPath());
            } catch(IOException e) {
                throwException("Could not send redirect to workplace main screen.", e);
            }
            return "".getBytes();
        }
        
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);

        // Put the "file" datablock for processing in the template file.
        // It will be inserted in a hidden input field and given back when submitting.
        xmlTemplateDocument.setXmlData("file", file);
        xmlTemplateDocument.setXmlData("jsfile", jsfile);                
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }                  
    
    public Object setText(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        
        Hashtable parameters = (Hashtable)userObj;
        String content = (String)parameters.get("CONTENT");        
        boolean existsContentParam = (content!=null && (!"".equals(content)));
                
        // Check the existance of the "file" parameter
        if(content==null || "".equals(content)) {
            String errorMessage = getClassName() + "No content found.";
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, errorMessage);
            }
            return("");
            // throw new CmsException(errorMessage, CmsException.C_BAD_NAME);
        }
                    
        // Escape the text for including it in HTML text
        return content;
    }        
}
