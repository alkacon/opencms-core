package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;
import java.io.*;

/**
 * Template class for displaying the text HTML editor of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.2 $ $Date: 2000/02/02 10:07:16 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsHtmlEditor extends CmsWorkplaceDefault {
                
    

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

        
        String content = (String)parameters.get("CONTENT");
        String file = (String)parameters.get("file");
        String exit = (String)parameters.get("EXIT");
        
        System.err.println("*** Parameter exit: " + exit);
        
        boolean existsContentParam = (content!=null && (!"".equals(content)));
        boolean existsFileParam = (file!=null && (!"".equals(file)));
        
        // Check the existance of the "file" parameter
        if(! existsFileParam) {
            throwException("No file requested", CmsException.C_BAD_NAME);
        }
        
        // Try to read the file. If it doesn't exist, an exception
        // will be thrown.
        CmsFile editFile = cms.readFile(file);
        CmsFile tempFile = null;
        String temporaryFileName = editFile.getParent() + C_WP_TEMP_PREFIX + editFile.getName();
                                
/*        if(!existsContentParam) {
            // This should be the first call of this class
            // since there is no content given back to be saved.
            // Create temporary file here
            try {
                cms.copyFile(file, temporaryFileName);               
            } catch(CmsException e) {
                if(e.getType() == CmsException.C_FILE_EXISTS) {
                    // The temporary file already exists.
                    // Delete it and try copying again.
                    try {
                        cms.deleteFile(temporaryFileName);
                        cms.copyFile(file, temporaryFileName);
                    } catch(CmsException e2) {
                        throwException("Could not create temporary file " + temporaryFileName + ". File already exist.", e2);
                    }
                } else {
                    // This was no FILE EXISTS exception. Cancel editing
                    throwException("Could not create temporary file " + temporaryFileName + ". ", e);                                    
                }
            }
        } else {            
            // We got a possibly changed content as parameter.
            // There must be a temp file. read it and set it's content.
            // But only, if no cancel was requested!
            if(exit == null || "save".equals(exit)) {
                tempFile = cms.readFile(temporaryFileName);
                Encoder encoder = new Encoder();
                content = encoder.unescape(content);
                tempFile.setContents(content.getBytes());
                cms.writeFile(tempFile);
            }
        }*/
        
        if(exit != null && ("save".equals(exit) || "cancel".equals(exit))) {
            // The user requested EXIT
            // Delete the temporary file and 
            // get the path of the workplace main screen and send a redirect.
/*            try {                
                cms.deleteFile(temporaryFileName);
            } catch(CmsException e) {
                // Could not delete temporary file :-(
                if(A_OpenCms.isLogging()) {
                    A_OpenCms.log(C_OPENCMS_INFO, getClassName() + "Could not remove temporary file " + temporaryFileName + ". ");
                }
            }*/
            try {
                System.err.println("**** trying to send redirect");
                cms.getRequestContext().getResponse().sendCmsRedirect(workplaceUrl(cms));
            } catch(IOException e) {
                throwException("Could not send redirect to workplace main screen.", e);
            }
            return "".getBytes();
        }
        
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }            

    
    public Object setText(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        
        Hashtable parameters = (Hashtable)userObj;
        String filename = (String)parameters.get("file");
        
        // Check the existance of the "file" parameter
        if(filename==null || "".equals(filename)) {
            String errorMessage = "[CmsButton] No file requested.";
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, errorMessage);
            }
            return("EMPTY");
            // throw new CmsException(errorMessage, CmsException.C_BAD_NAME);
        }
        
        String content = null;
        
        // try to load the contents of the file
        try {
            content = new String(cms.readFile(filename).getContents());                
        } catch(Exception e) {
            // Anything is wrong. Perhaps a wrong file name ???
            String errorMessage = "Error while reading file " + filename + ": " + e;
            if(A_OpenCms.isLogging()) {
                A_OpenCms.log(C_OPENCMS_CRITICAL, "[CmsDumpTemplate] " + errorMessage);
                if(!(e instanceof CmsException)) {
                    // Should not happen. Print out detailled error information                    
                    e.printStackTrace();
                }
            }
            // throw this exception again, so it can be displayed in 
            // the servlet.
            if(e instanceof CmsException) {
                throw (CmsException)e;
            } else {
                throw new CmsException(errorMessage, CmsException.C_UNKNOWN_EXCEPTION);
            }
        }
            
        // Escape the text for including it in HTML text
        Encoder enc = new Encoder();
        return enc.escape(content);
    }
}
