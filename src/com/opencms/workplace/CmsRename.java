package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.servlet.http.*;

import java.util.*;

/**
 * Template class for displaying the rename screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/02/11 14:04:30 $
 */
public class CmsRename extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                             I_CmsConstants {
           

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the rename template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The lock template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */
    public byte[] getContent(A_CmsObject cms, String templateFile, String elementName, 
                             Hashtable parameters, String templateSelector)
        throws CmsException {
        String result = null;     
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);   
        
        // the template to be displayed
        String template=null;
        
         String lock=(String)parameters.get(C_PARA_LOCK);
        String filename=(String)parameters.get(C_PARA_FILE);
        if (filename != null) {
            session.putValue(C_PARA_FILE,filename);        
        }
        filename=(String)session.getValue(C_PARA_FILE);
        String newFile=(String)parameters.get("name");
        CmsFile file=(CmsFile)cms.readFileHeader(filename);
        //check if the name parameter was included in the request
        // if not, the lock page is shown for the first time
    
        if (newFile == null) {
            session.putValue("name",file.getName());
        } else {
            System.err.println("##############");
            System.err.println(file.getAbsolutePath());
            System.err.println(file.getPath()+newFile);
            System.err.println("##############");
            cms.renameFile(file.getAbsolutePath(),newFile);
             try {
                cms.getRequestContext().getResponse().sendCmsRedirect( getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST);
            } catch (Exception e) {
                  throw new CmsException("Redirect fails :"+ getConfigFile(cms).getWorkplaceActionPath()+C_WP_EXPLORER_FILELIST,CmsException.C_UNKNOWN_EXCEPTION,e);
            } 
        }
        
  
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);          
        xmlTemplateDocument.setXmlData("OWNER",cms.readOwner(file).getName());
        xmlTemplateDocument.setXmlData("GROUP",cms.readGroup(file).getName());
        
        // process the selected template 
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
    
    }
    
     /**
     * Pre-Sets the value of the new name input field.
     * This method is directly called by the content definiton.
     * @param Cms The CmsObject.
     * @param lang The language file.
     * @return Value that is pre-set into the anew name field.
     * @exception CmsExeption if something goes wrong.
     */
    public String setValue(A_CmsObject cms, CmsXmlLanguageFile lang)
        throws CmsException {
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
        
        String name=(String)session.getValue("name");
      
      return name;
    }

}