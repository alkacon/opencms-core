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
 * @version $Revision: 1.2 $ $Date: 2000/02/07 10:42:48 $
 */
public class CmsExplorerHead extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                                     I_CmsConstants {
       /** Definition of the Datablock PARENT */   
     private final static String C_PARENT="PARENT";
     
     /** Definition of the Datablock PARENT_ENABLED */   
     private final static String C_PARENT_ENABLED="PARENT_ENABLED";    

     /** Definition of the Datablock PARENT_DISABLED */   
     private final static String C_PARENT_DISABLED="PARENT_DISABLED";    
  
     /** Definition of the Datablock PREVIOUS */   
     private final static String C_PREVIOUS="PREVIOUS";
     
     /** Definition of the Datablock PREVIOUS_ENABLED */   
     private final static String C_PREVIOUS_ENABLED="PREVIOUS_ENABLED";    

     /** Definition of the Datablock PREVIOUS_DISABLED */   
     private final static String C_PREVIOUS_DISABLED="PREVIOUS_DISABLED"; 
     
     /** Definition of the Datablock FILELIST */   
     private final static String C_FILELIST="FILELIST";
     
     /** Definition of the Datablock PREVIOUSLIST */   
     private final static String C_PREVIOUSLIST="PREVIOUSLIST";
     
     
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
//String filelist;
        String previous;
        String currentFilelist=null;;
        String previousFilelist=null;
        String newFilelist=null;


        // the template to be displayed
        String template="template";
        Hashtable preferences=new Hashtable();
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);

        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);        
       
        // if the viewfile value is included in the request, exchange the explorer
        // read with a back button to the file list
        viewfile=(String)parameters.get(C_PARA_VIEWFILE);
        if (viewfile!= null) {
            template="viewfile";
        }

        //check if a previous filelist parameter was included in the request.
        // if a previous filelist was included, overwrite the value in the session for later use.
        previous=(String)parameters.get(C_PARA_PREVIOUSLIST);
        if (previous != null) {
                session.putValue(C_PARA_PREVIOUSLIST,previous);
        }

        // get the previous current filelist to calculate the link for the back button.
        previousFilelist=(String)session.getValue(C_PARA_PREVIOUSLIST);
                
        //check if a filelist  parameter was included in the request.
        // if a filelist was included, overwrite the value in the session for later use.
      //  filelist=cms.getRequestContext().getRequest().getParameter(C_PARA_FILELIST);
      //  if (filelist != null) {
      //          session.putValue(C_PARA_FILELIST,filelist);
      //  }
        
        // get the current filelist to calculate its patent
        currentFilelist=(String)session.getValue(C_PARA_FILELIST);
        // if no filelist parameter was given, use the current folder
        if (currentFilelist==null) {
              currentFilelist=cms.getRequestContext().currentFolder().getAbsolutePath();
        } 
        if (!currentFilelist.equals("/")) {
            // cut off last "/"
            newFilelist=currentFilelist.substring(0,currentFilelist.length()-1);
            // now get the partent folder
            int end=newFilelist.lastIndexOf("/");
            if (end>-1) {
                newFilelist=newFilelist.substring(0,end+1);
                session.putValue(C_PARA_FILELIST,newFilelist);
            }                   
        } else {
            newFilelist=currentFilelist;
        }
    
        // put the refereences to the filelist and the previous filelist into the
        // template.
        xmlTemplateDocument.setXmlData(C_FILELIST,newFilelist);
        xmlTemplateDocument.setXmlData(C_PREVIOUSLIST,previousFilelist);
        
        // update the value for the back link.
        // this is required that for the explorer head after the back link is used.
        session.putValue(C_PARA_PREVIOUSLIST,currentFilelist);
               
        // set the parent button to enabled if not on the root folder
        if (currentFilelist.equals("/")) {
               xmlTemplateDocument.setXmlData(C_PARENT,xmlTemplateDocument.getProcessedXmlDataValue(C_PARENT_DISABLED,this));
        } else {
               xmlTemplateDocument.setXmlData(C_PARENT,xmlTemplateDocument.getProcessedXmlDataValue(C_PARENT_ENABLED,this));
        }
        
        // set the parent button to enabled if not on the root folder
        if (previousFilelist == null) {
               xmlTemplateDocument.setXmlData(C_PREVIOUS,xmlTemplateDocument.getProcessedXmlDataValue(C_PREVIOUS_DISABLED,this));
        } else {
               xmlTemplateDocument.setXmlData(C_PREVIOUS,xmlTemplateDocument.getProcessedXmlDataValue(C_PREVIOUS_ENABLED,this));
        }
        // process the selected template
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
    
    }
}
