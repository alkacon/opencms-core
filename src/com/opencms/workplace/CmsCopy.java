package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;

import javax.servlet.http.*;

import java.util.*;

/**
 * Template class for displaying the copy file screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 * 
 * @author Michael Emmerich
 * @version $Revision: 1.1 $ $Date: 2000/02/15 14:42:32 $
 */
public class CmsCopy extends CmsWorkplaceDefault implements I_CmsWpConstants,
                                                             I_CmsConstants {
           
    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the copy template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The copy template file
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
        
        // String lock=(String)parameters.get(C_PARA_LOCK);
        String filename=(String)parameters.get(C_PARA_FILE);
        if (filename != null) {
            session.putValue(C_PARA_FILE,filename);        
        }
        filename=(String)session.getValue(C_PARA_FILE);
        String newFile=(String)parameters.get("newfile");
        String newFolder=(String)parameters.get("newfolder");
        CmsFile file=(CmsFile)cms.readFileHeader(filename);
        //check if the folder parameter was included in the request
        // if not, the copy page is shown for the first time
    
        System.err.println("#####newFile "+newFile);
        System.err.println("#####newFolder "+newFolder);
        
        if (newFile == null) {
            session.putValue("name",file.getName());
        } else {
             cms.copyFile(file.getAbsolutePath(),newFolder+newFile);
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
     * Pre-Sets the value of the copy field.
     * This method is directly called by the content definiton.
     * @param Cms The CmsObject.
     * @param lang The language file.
     * @return Value that is pre-set into the copy field.
     * @exception CmsExeption if something goes wrong.
     */
    public String setValue(A_CmsObject cms, CmsXmlLanguageFile lang)
        throws CmsException {
        HttpSession session= ((HttpServletRequest)cms.getRequestContext().getRequest().getOriginalRequest()).getSession(true);
        
        String name=(String)session.getValue("name");
      
      return name;
    }
  
     /**
     * Gets all views available in the workplace screen.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will 
     * be filled with the appropriate information to be used for building
     * a select box.
     * <P>
     * <code>names</code> will contain language specific view descriptions
     * and <code>values</code> will contain the correspondig URL for each
     * of these views after returning from this method.
     * <P>
     * 
     * @param cms A_CmsObject Object for accessing system resources.
     * @param lang reference to the currently valid language file
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the user's current workplace view in the vectors.
     * @exception CmsException
     */
    public Integer getFolder(A_CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) 
            throws CmsException {
        
        // Let's see if we have a session
        A_CmsRequestContext reqCont = cms.getRequestContext();
        HttpSession session = ((HttpServletRequest)reqCont.getRequest().getOriginalRequest()).getSession(false);
       
        // get current and root folder
        CmsFolder rootFolder=cms.rootFolder();
        
        //add the root folder
        names.addElement(lang.getLanguageValue("title.rootfolder"));
        values.addElement("/");
        getTree(cms,rootFolder,names,values);
   
        return new Integer(0);
    }

    private void getTree(A_CmsObject cms,CmsFolder root,Vector names,Vector values)
        throws CmsException{
        Vector folders=cms.getSubFolders(root.getAbsolutePath());
        A_CmsProject currentProject = cms.getRequestContext().currentProject();
        Enumeration enu=folders.elements();
        while (enu.hasMoreElements()) {
            CmsFolder folder=(CmsFolder)enu.nextElement();
            // check if the current folder is part of the current project
            if (folder.inProject(currentProject)) {
                String name=folder.getAbsolutePath();
                name=name.substring(1,name.length()-1);
                names.addElement(name);
                values.addElement(folder.getAbsolutePath());
            }
            getTree(cms,folder,names,values);
        }
    }
}