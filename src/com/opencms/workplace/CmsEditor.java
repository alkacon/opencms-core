package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import java.util.*;

/**
 * Template class for displaying the text editor of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Alexander Lucas
 * @version $Revision: 1.3 $ $Date: 2000/01/26 17:06:21 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */
public class CmsEditor extends CmsWorkplaceDefault {
                
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
            throw new CmsException(errorMessage, CmsException.C_BAD_NAME);
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
