package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.servlet.http.*;

import java.util.*;

/**
 * Template class for displaying the explorer head of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/02/04 08:59:46 $
 */
public class CmsExplorerHead extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                                     I_CmsConstants {
  
     /**
     * Overwrties the getContent method of the CmsWorkplaceDefault.<br>
     *
     * @param cms The CmsObject.
     * @param templateFile The login template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, 
                             Hashtable parameters, String templateSelector)
        throws CmsException {
        String viewfile = null;     
        String currentFilelist;

        // the template to be displayed
        String template="template";
        Hashtable preferences=new Hashtable();
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);

        // if the viewfile value is included in the request, exchange the explorer
        // read with a back button to the file list
        viewfile=(String)parameters.get("VIEWFILE");
        if (viewfile!= null) {
            template="viewfile";
        }
       
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);        
      
        // get the current filelist to calculate its patent
        currentFilelist=(String)session.getValue(C_PARA_FILELIST);
        // if no filelist parameter was given, use the current folder
        if (currentFilelist==null) {
              currentFilelist=cms.getRequestContext().currentFolder().getAbsolutePath();
        } 
        if (!currentFilelist.equals("/")) {
            // cut off last "/"
            currentFilelist=currentFilelist.substring(0,currentFilelist.length()-1);
            // now get the partent folder
            int end=currentFilelist.lastIndexOf("/");
            if (end>0) {
                currentFilelist=currentFilelist.substring(0,end+1);
                session.putValue(C_PARA_FILELIST,currentFilelist);
            }                   
       }
    
        xmlTemplateDocument.setXmlData("FILELIST",currentFilelist);
        if (currentFilelist.equals("/")) {
               xmlTemplateDocument.setXmlData("PARENT",xmlTemplateDocument.getProcessedXmlDataValue("PARENT_DISABLED",this));
        } else {
               xmlTemplateDocument.setXmlData("PARENT",xmlTemplateDocument.getProcessedXmlDataValue("PARENT_ENABLED",this));
        }
        // process the selected template
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
    
    }
}
