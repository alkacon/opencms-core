/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsPublishResource.java,v $
* Date   : $Date: 2004/02/13 13:41:44 $
* Version: $Revision: 1.36 $
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

import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.I_CmsConstants;
import org.opencms.report.A_CmsReportThread;
import org.opencms.threads.CmsPublishThread;
import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.core.I_CmsSession;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

import java.util.Hashtable;

/**
 * Template class for displaying the publish screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.36 $ $Date: 2004/02/13 13:41:44 $
 */

public class CmsPublishResource extends CmsWorkplaceDefault {

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
            session.removeValue(C_PARA_RESOURCE);
            session.removeValue(C_PUBLISH_THREAD);
            session.removeValue(C_PUBLISH_LINKCHECK_THREAD);   
            session.removeValue(C_PUBLISH_LASTURL);                     
            session.removeValue("lasturl");
        }       

        // get the filename parameter
        String filename = (String)parameters.get(C_PARA_RESOURCE);
        if(filename != null) {
            session.putValue(C_PARA_RESOURCE, filename);
        }
        filename = (String)session.getValue(C_PARA_RESOURCE);

        // now get the action paramter and perform the requested action
        String action = (String)parameters.get("action");
        
        // set the required datablocks
        if(action == null) {
            // initial request, display confirm dialog box            
            CmsResource file = readResource(cms, filename);

            xmlTemplateDocument.setData("CHANGEDATE", CmsMessages.getDateTimeShort(file.getDateLastModified()));
            xmlTemplateDocument.setData("USER", cms.readUser(file.getUserLastModified()).getName());
            xmlTemplateDocument.setData("FILENAME", file.getName());
            
        } 
        
        /*
        else if("showResult".equals(action)){
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
            
        } 
                        
        else if("doThePublish".equals(action)){
            
            // linkcheck is ready. Now we can start the publishing
            CmsResource file = readResource(cms, filename);                     
            A_CmsReportThread doPublish = new CmsPublishResourceThread(cms, cms.readAbsolutePath(file));
            doPublish.start();
            session.putValue(C_PUBLISH_THREAD, doPublish);
            // indicate that changes in the user project etc. must be ignored here
            xmlTemplateDocument.setData("actionParameter", "showPublishResult");
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "showresult");
            
        }
        */
         
        else if("showPublishResult".equals(action)){
            
            // thread is started and we should show the report information.
            A_CmsReportThread doTheWork = (A_CmsReportThread)session.getValue(C_PUBLISH_THREAD);
            if (doTheWork != null && doTheWork.isAlive()) {
                xmlTemplateDocument.setData("endMethod", "");
                xmlTemplateDocument.setData("text", lang.getLanguageValue("project.publish.message_publish"));
            } else {
                xmlTemplateDocument.setData("endMethod", xmlTemplateDocument.getDataValue("endMethod"));
                xmlTemplateDocument.setData("autoUpdate", "");
                xmlTemplateDocument.setData("text", lang.getLanguageValue("project.publish.message_publish2"));
                session.removeValue(C_PUBLISH_THREAD);
            }
            xmlTemplateDocument.setData("data", doTheWork.getReportUpdate());
            return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "updateReport");
            
        } else if("done".equals(action)){
            // cleanup the session
            session.removeValue(C_PUBLISH_LASTURL);
            session.removeValue(C_PARA_RESOURCE);
            // return to filelist
            String lasturl = getLastUrl(cms, parameters);            
            try {
                if(lasturl == null || "".equals(lasturl)) {
                    cms.getRequestContext().getResponse().sendCmsRedirect(getConfigFile(cms).getWorkplaceActionPath()
                                + CmsWorkplaceAction.getExplorerFileUri(cms));
                }else {
                    cms.getRequestContext().getResponse().sendRedirect(lasturl);
                }
            } catch (Exception e) {
                    throw new CmsException("Redirect fails :"
                            + getConfigFile(cms).getWorkplaceActionPath()
                            + CmsWorkplaceAction.getExplorerFileUri(cms), CmsException.C_UNKNOWN_EXCEPTION, e);
            }
            return null;
        } else {            
            CmsResource file = readResource(cms, filename);
            String lasturl = getLastUrl(cms, parameters);
            
            if (file.isFolder() && !filename.endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                filename += I_CmsConstants.C_FOLDER_SEPARATOR;
            }
                        
            if ("check".equals(action)){
                if(file.getState() != C_STATE_DELETED){
                    //if(checkLocked(cms, file)){
                    if (cms.countLockedResources(filename)==0) {                   
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
                    cms.lockResource(filename);
                    cms.unlockResource(filename, false);
                    action = "ok";
                } catch (CmsException exc){
                    xmlTemplateDocument.setData("details", CmsException.getStackTraceAsString(exc));
                    xmlTemplateDocument.setData("lasturl", lasturl);
                    return startProcessing(cms, xmlTemplateDocument, elementName, parameters,"errorlock");
                }
            }
            
            if("ok".equals(action)) {
                file = readResource(cms, filename);                                     
                A_CmsReportThread doPublish = new CmsPublishThread(cms, cms.readAbsolutePath(file), false, null);
                doPublish.start();
                session.putValue(C_PUBLISH_THREAD, doPublish);
                // indicate that changes in the user project etc. must be ignored here
                xmlTemplateDocument.setData("actionParameter", "showPublishResult");
                return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "showresult");
                                
                /*
                // publish is confirmed, let's go
                try{
                    // here is the plan: 
                    // first create a direct publish temp project (A)
                    // then start the link checker with this project (A)
                    // the link checker will delete project (A) after having checked the links
                    // if broken links where found, display a confirmation dialog
                    // if the user continues the publish (or no broken links where found)
                    // auto-create a direct publish temp project (B) and publish this directly
                    
                    int tempProjectId = cms.publishResource(cms.readAbsolutePath(file), true);
                    if(lasturl == null){
                        lasturl = "";
                    }
                    session.putValue(C_PUBLISH_LASTURL, lasturl);
                    // first part of the publish: check for broken links
                    A_CmsReportThread doCheck = new CmsAdminLinkmanagementThread(cms, tempProjectId, cms.readAbsolutePath(file));
                    doCheck.start();
                    session.putValue(C_PUBLISH_LINKCHECK_THREAD, doCheck);
                    template = "showresult";
                
                } catch(CmsException e){
                    session.removeValue(C_PARA_RESOURCE);
                    xmlTemplateDocument.setData("details", Utils.getStackTrace(e));
                    xmlTemplateDocument.setData("lasturl", lasturl);
                    return startProcessing(cms, xmlTemplateDocument, "", parameters, "error");
                }
                */
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
            resource = cms.readFolder(resourceName, true);
        } else {
            resource = cms.readFileHeader(resourceName, true);
        }        
        return resource;
    }

    /**
     * check if there are any locked resources in the folder
     *
     * @param cms The CmsObject for accessing system resources
     * @param resource The resource to check
     */
    /*
    private boolean checkLocked(CmsObject cms, CmsResource resource) throws CmsException {
        CmsLock lock = cms.getLock(resource);
        
        // do not need to check a file
        if(resource.isFile()){
            return true;
        }
        // check if the folder itself is locked
        if(!lock.isNullLock()){
            return false;
        }
        List allFiles = cms.getFilesInFolder(cms.readAbsolutePath(resource));
        List allFolders = cms.getSubFolders(cms.readAbsolutePath(resource));
        // first check if any file in the folder is locked
        for(int i=0; i<allFiles.size(); i++){
            CmsResource curFile = (CmsResource)allFiles.get(i);
            lock = cms.getLock(curFile);
            
            if(!lock.isNullLock()){
                return false;
            }
        }
        // now check all subfolders
        for(int j=0; j<allFolders.size(); j++){
            CmsResource curFolder = (CmsResource)allFolders.get(j);
            if(!checkLocked(cms, curFolder)){
                return false;
            }
        }
        return true;
    }
    */

    /**
     * Unlocks all resources in the folder
     *
     * @param cms The CmsObject for accessing system resources
     * @param resource The resource to unlock
     */
    /*
    private void unlockResource(CmsObject cms, CmsResource resource) throws CmsException{
        // if the folder itself is locked, all subresources are unlocked by unlocking the folder
        if(resource.isLocked()){
            // first lock resource to set locked by to the current user
            if(!resource.isLockedBy().equals(cms.getRequestContext().currentUser().getId())){
                cms.lockResource(cms.readAbsolutePath(resource),true);
            }
            cms.unlockResource(cms.readAbsolutePath(resource));
        } else {
            // need to unlock each resource
            List allFiles = cms.getFilesInFolder(cms.readAbsolutePath(resource));
            List allFolders = cms.getSubFolders(cms.readAbsolutePath(resource));
            // unlock the files
            for(int i=0; i<allFiles.size(); i++){
                CmsResource curFile = (CmsResource)allFiles.get(i);
                if(curFile.isLocked()){
                    if(!resource.isLockedBy().equals(cms.getRequestContext().currentUser().getId())){
                        cms.lockResource(cms.readAbsolutePath(curFile),true);
                    }
                    cms.unlockResource(cms.readAbsolutePath(curFile));
                }
            }
            // unlock the folders
            for(int j=0; j<allFolders.size(); j++){
                CmsResource curFolder = (CmsResource)allFolders.get(j);
                unlockResource(cms, curFolder);
            }
        }
    }
    */

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
