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
 * @version $Revision: 1.6 $ $Date: 2000/02/10 14:34:27 $
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

     /** Definition of the Datablock STARTUP */   
     private final static String C_STARTUP="STARTUP";
    
     /** Definition of the Datablock STARTUP_FILE */   
     private final static String C_STARTUP_FILE="STARTUP_FILE";
     
     /** Definition of the Datablock STARTUP_FOLDER */   
     private final static String C_STARTUP_FOLDER="STARTUP_FOLDER";
     
     /** Definition of the Datablock  LINK_VALUE */
     private final static String C_LINK_VALUE="LINK_VALUE";   
     
     /**
     * Indicates if the results of this class are cacheable.
     * <P>
     * Complex classes that are able top include other subtemplates
     * have to check the cacheability of their subclasses here!
     * 
     * @param cms A_CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file 
     * @param parameters Hashtable with all template class parameters.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */
    public boolean isCacheable(A_CmsObject cms, String templateFile, Hashtable parameters) {
        return false;
    }
     
     /**
     * Overwrties the getContent method of the CmsWorkplaceDefault.<br>
     * The explorer head works in three different modes that are selected by 
     * different parameters in the request:
     * <ul>
     * <li>Normal mode: No parameter is given, in this mode address input field and the
     * navigation buttons are displayed. </li>
     * <li>View mode: This mode is used wehn the "viewfile" parameter is set. It is used
     * when a file is displayed in the main explorer window and only includes a back button 
     * in the explorer head. </li>
     * <li>URL mode: This mode is activated when a file or folder is given in the address input
     * field. It is used to determine the file or fodler to be displayed. </li>
     * </ul>
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
        String filelist=null;
        String previous=null;
        String url=null;
        String currentFilelist=null;;
        String previousFilelist=null;
        String newFilelist=null;

        // the template to be displayed
        String template=null;
        Hashtable preferences=new Hashtable();
       
        // get session and servlet root
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
        String servlets=((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getServletPath();
           
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms,templateFile);        
       
        // if the viewfile value is included in the request, exchange the explorer
        // read with a back button to the file list
        viewfile=(String)parameters.get(C_PARA_VIEWFILE);
        if (viewfile!= null) {          
            template="viewfile";
        } else {
            
            // Check if the URL parameter was included in the request. It is set when
            // a folder or file is entered in the address input field.
            url=(String)parameters.get(C_PARA_URL);
            if (url ==null) {
                xmlTemplateDocument.clearStartup();
                
            } else {
                
                // check if the requested url is a file or a folder.
                if (url.endsWith("/")) {
                    // the url is a folder, so prepare to update the file list and tree
                    xmlTemplateDocument.setXmlData(C_FILELIST,url);          
                    xmlTemplateDocument.setXmlData(C_STARTUP,xmlTemplateDocument.getProcessedXmlDataValue(C_STARTUP_FOLDER,this));
                    currentFilelist=(String)session.getValue(C_PARA_FILELIST);
                    if (currentFilelist == null) {
                         currentFilelist=cms.getRequestContext().currentFolder().getAbsolutePath();
                    }
                    session.putValue(C_PARA_PREVIOUSLIST,currentFilelist);   
                    session.putValue(C_PARA_FILELIST,url);
                    session.putValue(C_PARA_FOLDER,url);
                } else {
                    // the url is a file, so show the requested document
                    xmlTemplateDocument.setXmlData(C_LINK_VALUE,servlets+url); 
                    xmlTemplateDocument.setXmlData(C_STARTUP,xmlTemplateDocument.getProcessedXmlDataValue(C_STARTUP_FILE,this));
                    // the url is a file
                } 
            }
            
            // check if a previous filelist parameter was included in the request.
            // if a previous filelist was included, overwrite the value in the session for later use.
            previous=(String)parameters.get(C_PARA_PREVIOUSLIST);
            if (previous != null) {
                session.putValue(C_PARA_PREVIOUSLIST,previous);
            }

            // get the previous current filelist to calculate the link for the back button.
            previousFilelist=(String)session.getValue(C_PARA_PREVIOUSLIST);
                
            //check if a filelist  parameter was included in the request.
            //if a filelist was included, overwrite the value in the session for later use.
            filelist=cms.getRequestContext().getRequest().getParameter(C_PARA_FILELIST);
            if (filelist != null) {
               session.putValue(C_PARA_FILELIST,filelist);
            }
        
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
            // session.putValue(C_PARA_PREVIOUSLIST,currentFilelist);
               
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
            
        }
        // process the selected template
        return startProcessing(cms,xmlTemplateDocument,"",parameters,template);
    }

    
    /**
     * Sets the value of the address input filed of the file header.
     * This method is directly called by the content definiton.
     * @param Cms The CmsObject.
     * @param lang The language file.
     * @return Value that is set into the adress field.
     * @exception CmsExeption if something goes wrong.
     */
    public String setValue(A_CmsObject cms, CmsXmlLanguageFile lang)
        throws CmsException {
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);

        // get the current filelist to display it in the address input field.
        String currentFilelist=(String)session.getValue(C_PARA_FILELIST);
        // if no filelist parameter was given, use the current folder
        if (currentFilelist==null) {
             currentFilelist=cms.getRequestContext().currentFolder().getAbsolutePath();
        } 
      return currentFilelist;
    }
}
