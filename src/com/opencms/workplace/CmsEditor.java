/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsEditor.java,v $
 * Date   : $Date: 2000/04/05 08:45:55 $
 * Version: $Revision: 1.11 $
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

import javax.servlet.http.*;

/**
 * Template class for displaying the text editor of the OpenCms workplace.<P>
 * Reads the edirtor layout from a editor template file of the content type 
 * <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.11 $ $Date: 2000/04/05 08:45:55 $
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
     * Displays the editor described by the template file <code>templateFile</code>.
     * This can be either the HTML editor or the text editor.
     * <p>
     * The given template file will be scanned for special section "ie" and "ns"
     * that can be used to generate browser specific versions of the editors
     * (MS IE or Netscape Navigator). If no such section exists, the default
     * section will be displayed.
     * 
     * @see getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms A_CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        
        // Get all editor parameters
        String file = (String)parameters.get(C_PARA_FILE);
        String content = (String)parameters.get(C_PARA_CONTENT);
        String action = (String)parameters.get(C_PARA_ACTION);
        String jsfile = (String)parameters.get(C_ROOT_TEMPLATE_NAME + "." + C_PARA_JSFILE);        
        
        boolean existsFileParam = ((file != null) && (!"".equals(file)));
        boolean saveRequested = ((action != null) && (C_EDIT_ACTION_SAVE.equals(action) || C_EDIT_ACTION_SAVEEXIT.equals(action)));
        boolean exitRequested = ((action != null) && (C_EDIT_ACTION_EXIT.equals(action) || C_EDIT_ACTION_SAVEEXIT.equals(action)));

        // For further processing we possibly need the encoder            
        Encoder enc = new Encoder();

        // CmsFile object of the file to be edited
        CmsFile editFile = null;
        
        // If there is a file parameter and no content, try to read the file. 
        // If the user requested a "save file", also load the file.
        if(existsFileParam && (content == null || saveRequested)) {
            editFile = readFile(cms, file);
            
            // If there is no content set, this is the first request of the editor.
            // So load the file content and set the "content" parameter.        
            if(content == null) {
                content = new String(editFile.getContents());
                content = enc.escape(content);
                parameters.put(C_PARA_CONTENT, content);
            }
            
            // If the user requested a file save, write the file content
            // back to the database.            
            if(saveRequested) {
                String decodedContent = enc.unescape(content);
                editFile.setContents(decodedContent.getBytes());
                cms.writeFile(editFile);
            }                
        }
        
        // Check if we should leave th editor instead of start processing
        if(exitRequested) {
            try {
                cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceMainPath());
            } catch(IOException e) {
                throwException("Could not send redirect to workplace main screen.", e);
            }
            return "".getBytes();
        }
            
        // Load the template file and get the browser specific section name
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        String sectionName = getBrowserSpecificSection(cms, xmlTemplateDocument);

        // Put the "file" datablock for processing in the template file.
        // It will be inserted in a hidden input field and given back when submitting.
        xmlTemplateDocument.setXmlData(C_PARA_FILE, file);
        xmlTemplateDocument.setXmlData(C_PARA_JSFILE, jsfile);                
        xmlTemplateDocument.setXmlData("editorframe", (String)parameters.get("root.editorframe"));                
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, sectionName);
    }                  
    
    /** 
     * User method for setting the editable text in the editor window.
     * <P>
     * This method can be called in the editor's template file using
     * <code>&lt;METHOD name="setText"/&gt></code>. This call will be replaced
     * by the content of the file that should be edited.
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document.  
     * @param userObj Hashtable with parameters.
     * @return String or byte[] with the content of the file that should be edited.
     */
    public Object setText(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) {        
        Hashtable parameters = (Hashtable)userObj;
        String content = (String)parameters.get(C_PARA_CONTENT);        
        if(content==null) {
            content = "";
        }                    
        return content;
    }        

    /**
     * Reads in the requested file to be edited by calling the corresponding
     * method in the cms object.
     * @param cms Cms object for accessing system resources
     * @param filename Name of the file to be loaded
     * @return CmsFile object of the loaded file
     */
    private CmsFile readFile(A_CmsObject cms, String filename) throws CmsException {
        CmsFile result = null;
        try {
            result = cms.readFile(filename);
        } catch(Exception e) {
            // Anything is wrong. Perhaps a wrong file name ???
            String errorMessage = "Error while reading file " + filename + ": " + e;
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
        return result;
    }
    
    /**
     * Get the name of the section that should be loaded from the editor's
     * template file for displaying the editor.
     * MS IE and Netscape Navigator use different ways to display
     * the text editor, so we must distinguish here.
     * @param cms cms object for accessing the original HTTP request
     * @param templateFile the editor's template file containing different sections
     * @return name of the browser specific section in <code>templateFile</code>
     */
    private String getBrowserSpecificSection(A_CmsObject cms, CmsXmlTemplateFile templateFile) {        
        HttpServletRequest orgReq = (HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest();                
        String browser = orgReq.getHeader("user-agent");                
        String result = null;
        if(browser.indexOf("MSIE") >-1) {
            if(templateFile.hasSection("ie")) {          
                result = "ie";
            }
        } else {
            if(templateFile.hasSection("ns")) {          
    	    	result = "ns";
            }
	   	}
        return result;
    }
}
