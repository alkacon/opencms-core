/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsPublishResource.java,v $
* Date   : $Date: 2004/01/07 10:57:09 $
* Version: $Revision: 1.15.2.1 $
*
* This library is part of OpenCms -
* the Open Source Content Mananagement System
*
* Copyright (C) 2001  The OpenCms Group
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.org
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


package com.opencms.workplace;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsResource;
import com.opencms.report.A_CmsReportThread;
import com.opencms.util.Utils;

import java.util.Hashtable;
import java.util.Vector;

/**
 * Template class for displaying the publish screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.15.2.1 $ $Date: 2004/01/07 10:57:09 $
 */

public class CmsPublishResource extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {

    // session keys
    private static final String C_PUBLISH_THREAD = "publishprojectresthread";
    private static final String C_PUBLISH_LINKCHECK_THREAD = "publishreslinkcheckthread";
    private static final String C_PUBLISH_LASTURL = "lasturlForPublishResource";

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the undelete template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The undelete template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @throws Throws CmsException if something goes wrong.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) 
    throws CmsException {

        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();

        // the template to be displayed
        String template = null;

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {          
            // remove all session values
            session.removeValue(C_PARA_FILE);
            session.removeValue(C_PUBLISH_THREAD);
            session.removeValue(C_PUBLISH_LINKCHECK_THREAD);   
            session.removeValue(C_PUBLISH_LASTURL);                     
            session.removeValue("lasturl");
        }       

        // get the filename parameter
        String filename = (String)parameters.get(C_PARA_FILE);
        if(filename != null) {
            session.putValue(C_PARA_FILE, filename.trim());
        }
        filename = (String)session.getValue(C_PARA_FILE);

        // now get the action paramter and perform the requested action
        String action = (String)parameters.get("action");
        
        // set the required datablocks
        if(action == null) {
            // initial request, display confirm dialog box            
            CmsResource file = readResource(cms, filename);

            xmlTemplateDocument.setData("CHANGEDATE", Utils.getNiceDate(file.getDateLastModified()));
            xmlTemplateDocument.setData("USER", cms.readUser(file.getResourceLastModifiedBy()).getName());
            xmlTemplateDocument.setData("FILENAME", file.getName());
            
        } else if("showResult".equals(action)){
            A_CmsReportThread doTheWork = (A_CmsReportThread)session.getValue(C_PUBLISH_LINKCHECK_THREAD);
            //still working?
            if(doTheWork.isAlive()){
                xmlTemplateDocument.setData("endMethod", "");
                xmlTemplateDocument.setData("text", lang.getLanguageValue("project.publish.message_linkcheck"));
            }else{
                if(doTheWork.brokenLinksFound()){
                    xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod2"));
                    xmlTemplateDocument.setData("autoUpdate","");
                    xmlTemplateDocument.setData("text", lang.getLanguageValue("project.publish.message_brokenlinks")
                                                +"<br>"+lang.getLanguageValue("project.publish.message_brokenlinks2"));
                }else{
                    xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod3"));
                    xmlTemplateDocument.setData("autoUpdate","");
                    xmlTemplateDocument.setData("text", "");
                }
                session.removeValue(C_PUBLISH_LINKCHECK_THREAD);
            }
            xmlTemplateDocument.setData("data", doTheWork.getReportUpdate());
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "updateReport");
            
        } else if("doThePublish".equals(action)){
            
            // linkcheck is ready. Now we can start the publishing
            CmsResource file = readResource(cms, filename);                     
            A_CmsReportThread doPublish = new CmsPublishResourceThread(cms, file.getAbsolutePath());
            doPublish.start();
            session.putValue(C_PUBLISH_THREAD, doPublish);
            // indicate that changes in the user project etc. must be ignored here
            xmlTemplateDocument.setData("actionParameter", "showPublishResult");
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "showresult");
            
        } else if("showPublishResult".equals(action)){
            
            // thread is started and we shoud show the report information.
            A_CmsReportThread doTheWork = (A_CmsReportThread)session.getValue(C_PUBLISH_THREAD);
            if(doTheWork.isAlive()){
                xmlTemplateDocument.setData("endMethod", "");
                xmlTemplateDocument.setData("text", lang.getLanguageValue("project.publish.message_publish"));
            }else{
                xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod"));
                xmlTemplateDocument.setData("autoUpdate","");
                xmlTemplateDocument.setData("text", lang.getLanguageValue("project.publish.message_publish2"));
                session.removeValue(C_PUBLISH_THREAD);
            }
            xmlTemplateDocument.setData("data", doTheWork.getReportUpdate());
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "updateReport");
            
        } else if("done".equals(action)){
            // cleanup the session
            session.removeValue(C_PUBLISH_LASTURL);
            session.removeValue(C_PARA_FILE);
            // return to filelist
            String lasturl = getLastUrl(cms, parameters);            
            try {
                if(lasturl == null || "".equals(lasturl)) {
                    cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                                + C_WP_EXPLORER_FILELIST);
                }else {
                    cms.getRequestContext().getResponse().sendRedirect(lasturl);
                }
            } catch (Exception e) {
                    throw new CmsException("Redirect fails :"
                            + getConfigFile(cms).getWorkplaceActionPath()
                            + C_WP_EXPLORER_FILELIST, CmsException.C_UNKNOWN_EXCEPTION, e);
            }
            return null;
        } else {            
            CmsResource file = readResource(cms, filename);
            String lasturl = getLastUrl(cms, parameters);
                        
            if ("check".equals(action)){
                if(file.getState() != C_STATE_DELETED){
                    if(checkLocked(cms, file)){
                        action = "ok";
                    } else {
                        // ask user if the locks should be removed
                        return startProcessing(cms, xmlTemplateDocument, elementName, parameters,"asklock");
                    }
                } else {
                    action = "ok";
                }
            } else if("rmlocks".equals(action)){
                // remove the locks and publish
                try{
                    unlockResource(cms, file);
                    action = "ok";
                } catch (CmsException exc){
                    xmlTemplateDocument.setData("details", Utils.getStackTrace(exc));
                    xmlTemplateDocument.setData("lasturl", lasturl);
                    return startProcessing(cms, xmlTemplateDocument, elementName, parameters,"errorlock");
                }
            }
            if("ok".equals(action)) {
                // publish is confirmed, let's go
                try{
                    // here is the plan: 
                    // first create a direct publish temp project (A)
                    // then start the link checker with this project (A)
                    // the link checker will delete project (A) after having checked the links
                    // if broken links where found, display a confirmation dialog
                    // if the user continues the publish (or no broken links where found)
                    // auto-create a direct publish temp project (B) and publish this directly
                    
                    int tempProjectId = cms.publishResource(file.getAbsolutePath(), true);
                    if(lasturl == null){
                        lasturl = "";
                    }
                    session.putValue(C_PUBLISH_LASTURL, lasturl);
                    // first part of the publish: check for broken links
                    A_CmsReportThread doCheck = new CmsAdminLinkmanagementThread(cms, tempProjectId, file.getAbsolutePath());
                    doCheck.start();
                    session.putValue(C_PUBLISH_LINKCHECK_THREAD, doCheck);
                    template = "showresult";
                
                } catch(CmsException e){
                    session.removeValue(C_PARA_FILE);
                    xmlTemplateDocument.setData("details", Utils.getStackTrace(e));
                    xmlTemplateDocument.setData("lasturl", lasturl);
                    return startProcessing(cms, xmlTemplateDocument, "", parameters, "error");
                }
            }
        }        
        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
    }
    
    /**
     * Reads a named resource form the VFS.
     * 
     * @param cms the active cms context
     * @param resourceName the name of the resource to read
     * @return CmsResource the read resource, or <code>null</code> if nothing was read
     * @throws CmsException if something goes wrong reading the resource
     */
    private CmsResource readResource(CmsObject cms, String resourceName) throws CmsException {
        CmsResource resource = null;
        if(resourceName.endsWith("/")){
            resource = (CmsResource)cms.readFolder(resourceName, true);
        } else {
            resource = (CmsResource)cms.readFileHeader(resourceName, true);
        }        
        return resource;
    }

    /**
     * check if there are any locked resources in the folder
     *
     * @param cms The CmsObject for accessing system resources
     * @param resource The resource to check
     */
    private boolean checkLocked(CmsObject cms, CmsResource resource) throws CmsException{
        // do not need to check a file
        if(resource.isFile()){
            return true;
        }
        // check if the folder itself is locked
        if(resource.isLocked()){
            return false;
        }
        Vector allFiles = cms.getFilesInFolder(resource.getAbsolutePath());
        Vector allFolders = cms.getSubFolders(resource.getAbsolutePath());
        // first check if any file in the folder is locked
        for(int i=0; i<allFiles.size(); i++){
            CmsResource curFile = (CmsResource)allFiles.elementAt(i);
            if(curFile.isLocked()){
                return false;
            }
        }
        // now check all subfolders
        for(int j=0; j<allFolders.size(); j++){
            CmsResource curFolder = (CmsResource)allFolders.elementAt(j);
            if(!checkLocked(cms, curFolder)){
                return false;
            }
        }
        return true;
    }

    /**
     * Unlocks all resources in the folder
     *
     * @param cms The CmsObject for accessing system resources
     * @param resource The resource to unlock
     */
    private void unlockResource(CmsObject cms, CmsResource resource) throws CmsException{
        // if the folder itself is locked, all subresources are unlocked by unlocking the folder
        if(resource.isLocked()){
            // first lock resource to set locked by to the current user
            if(resource.isLockedBy() != cms.getRequestContext().currentUser().getId()){
                cms.lockResource(resource.getAbsolutePath(),true);
            }
            cms.unlockResource(resource.getAbsolutePath());
        } else {
            // need to unlock each resource
            Vector allFiles = cms.getFilesInFolder(resource.getAbsolutePath());
            Vector allFolders = cms.getSubFolders(resource.getAbsolutePath());
            // unlock the files
            for(int i=0; i<allFiles.size(); i++){
                CmsResource curFile = (CmsResource)allFiles.elementAt(i);
                if(curFile.isLocked()){
                    if(resource.isLockedBy() != cms.getRequestContext().currentUser().getId()){
                        cms.lockResource(curFile.getAbsolutePath(),true);
                    }
                    cms.unlockResource(curFile.getAbsolutePath());
                }
            }
            // unlock the folders
            for(int j=0; j<allFolders.size(); j++){
                CmsResource curFolder = (CmsResource)allFolders.elementAt(j);
                unlockResource(cms, curFolder);
            }
        }
    }

    /**
     * Indicates if the results of this class are cacheable.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if cacheable, <EM>false</EM> otherwise.
     */

    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }
}
