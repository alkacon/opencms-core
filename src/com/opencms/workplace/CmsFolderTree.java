package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.servlet.http.*;

import java.util.*;

/**
 * Template class for displaying the folder tree of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/01/27 16:54:31 $
 */
public class CmsFolderTree extends CmsWorkplaceDefault {
           
     
    /**
     * Overwries the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the longin templated and processed the data input.
     * If the user has authentificated to the system, the login window is closed and
     * the workplace is opened. <br>
     * If the login was incorrect, an error message is displayed and the login
     * dialog is displayed again.
     * @param cms The CmsObject.
     * @param templateFile The login template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
         
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);        
      
        // process the selected template
        return startProcessing(cms,xmlTemplateDocument,"",parameters,"template");
    
    }
    
     public Object getTree(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
            Hashtable parameters = (Hashtable)userObj;
            StringBuffer output=new StringBuffer();            

                     
            A_CmsUser u =cms.getRequestContext().currentUser();
            
            output.append(u+"<br>");
            
            A_CmsProject p =cms.getRequestContext().currentProject();    
            
            output.append(p+"<br>");
            
            CmsFolder f= cms.rootFolder();
            
            output.append(f+"<br>");
            
            if (f!=null) {
            output.append(f.toString());
            } else {
              output.append("ERROR");
            }
            Vector folders = cms.getSubFolders(f.getAbsolutePath());
            
            output.append(folders.toString()+"<br><br>");
            
            Enumeration enu = folders.elements();
            while (enu.hasMoreElements()) {
                output.append((CmsFolder)enu.nextElement()+"<br>");
            }
            return output.toString();
     }
        
}
