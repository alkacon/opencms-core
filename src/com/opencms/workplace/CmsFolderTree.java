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
 * @version $Revision: 1.2 $ $Date: 2000/01/28 11:09:43 $
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
    
    /**
     * Creates the folder tree in the workplace explorer.
     * @exception Throws CmsException if something goes wrong.
     */
     public Object getTree(A_CmsObject cms, String tagcontent, A_CmsXmlContent doc, Object userObj) 
            throws CmsException {
            Hashtable parameters = (Hashtable)userObj;
            StringBuffer output=new StringBuffer();  

            // get current and root folder
            CmsFolder rootFolder=cms.rootFolder();
            CmsFolder currentFolder=cms.getRequestContext().currentFolder();
            
            //get the template
            CmsXmlWpTemplateFile template=(CmsXmlWpTemplateFile)doc;
            
            String tab=template.getProcessedXmlDataValue("TREEIMG_EMPTY0");
            showTree(cms,rootFolder,currentFolder,template,output,tab);
            return output.toString();
     }

     
    private void showTree(A_CmsObject cms, CmsFolder curfolder, CmsFolder endfolder,
                          CmsXmlWpTemplateFile template, StringBuffer output,
                          String tab) 
    throws CmsException {
        
        String newtab=new String();
        String folderimg=new String();
        String treeswitch=new String();
        CmsFolder lastFolder=null;
        Vector subfolders;
        
        // if the actual folder is the root folder, no other folder has to be opened
        if (endfolder.getAbsolutePath().equals("/")) {
            Vector list=cms.getSubFolders(endfolder.getAbsolutePath());
            Enumeration enum =list.elements();
            while (enum.hasMoreElements()) {
                CmsFolder folder=(CmsFolder)enum.nextElement();
                template.setXmlData("TREEENTRY",folder.getName());
                template.setXmlData("TREETAB",tab);
                output.append(template.getProcessedXmlDataValue("TREELINE"));
            }
        } else {
            // otherwise find the folder that has to be opened and displayed with its
            // subfolders.
            Vector list=cms.getSubFolders(curfolder.getAbsolutePath());
            Enumeration enum =list.elements();
            if (list.size()>0) {
                lastFolder = (CmsFolder)list.lastElement();     
            } else {
                lastFolder=null;
            }
            
            while (enum.hasMoreElements()) { 
                CmsFolder folder=(CmsFolder)enum.nextElement();
                subfolders=cms.getSubFolders(folder.getAbsolutePath());                         
                
                // check if this folder must diplayes open
                if (folder.getAbsolutePath().equals(endfolder.getAbsolutePath())) {
                    folderimg=template.getProcessedXmlDataValue("TREEIMG_FOLDEROPEN");   
                } else {
                    folderimg=template.getProcessedXmlDataValue("TREEIMG_FOLDERCLOSE");   
                }
                
                // now check if a treeswitch has to displayed
                
                // is this the last element of the current folder, so display the end image
                if (folder.getAbsolutePath().equals(lastFolder.getAbsolutePath())) {
                    // if there are any subfolders extisintg, use the + or - box
                    if (subfolders.size() >0) {
                        // test if the + or minus must be displayed
                        if (endfolder.getAbsolutePath().startsWith(folder.getAbsolutePath())) {
                            treeswitch=template.getProcessedXmlDataValue("TREEIMG_MEND");    
                        } else {
                            treeswitch=template.getProcessedXmlDataValue("TREEIMG_PEND"); 
                        }
                    } else {
                        treeswitch=template.getProcessedXmlDataValue("TREEIMG_END");              
                    }
                } else {
                    // use the cross image
                    
                    // if there are any subfolders extisintg, use the + or - box
                    if (subfolders.size() >0) {
                         // test if the + or minus must be displayed
                        if (endfolder.getAbsolutePath().startsWith(folder.getAbsolutePath())) {
                            treeswitch=template.getProcessedXmlDataValue("TREEIMG_MCROSS");
                        } else {                            
                            treeswitch=template.getProcessedXmlDataValue("TREEIMG_PCROSS");
                        }
                    } else {
                        treeswitch=template.getProcessedXmlDataValue("TREEIMG_CROSS");
                    }
                }
    
                if (folder.getAbsolutePath().equals(lastFolder.getAbsolutePath())) {
                    newtab=tab+template.getProcessedXmlDataValue("TREEIMG_EMPTY");     
                } else {
                    newtab=tab+template.getProcessedXmlDataValue("TREEIMG_VERT");     
                }
                
                // set all data for the treeline tag
                template.setXmlData("TREEENTRY",folder.getName());
                template.setXmlData("TREETAB",tab);
                template.setXmlData("TREEFOLDER",folderimg);
                template.setXmlData("TREESWITCH",treeswitch);
                output.append(template.getProcessedXmlDataValue("TREELINE"));
                
                //finally process all subfolders if nescessary
                if (endfolder.getAbsolutePath().startsWith(folder.getAbsolutePath())) {
                    showTree(cms,folder,endfolder,template,output,newtab);
                }
            }
        }
     }
}
