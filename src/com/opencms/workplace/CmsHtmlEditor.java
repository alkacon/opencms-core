package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;

/**
 * Template class for displaying the text HTML editor of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.1 $ $Date: 2000/02/01 08:30:31 $
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
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "****************************************");
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "getting content of element " + ((elementName==null)?"<root>":elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "template file is: " + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, getClassName() + "selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }

        
        String content = (String)parameters.get("CONTENT");
        String file = (String)parameters.get("file");
        if(content!=null && (!"".equals(content))) {
            CmsFile editFile= cms.readFile(file);
            Encoder encoder = new Encoder();
            content = encoder.unescape(content);
            editFile.setContents(content.getBytes());
            cms.writeFile(editFile);
        }
        
        System.err.println("*** WORKPLACE URL: " + workplaceUrl(cms));        
        
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }            

    
    public Object setText(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
        
        System.err.println("*** CmsHtmlEditor setText started");
        
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
