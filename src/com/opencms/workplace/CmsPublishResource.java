/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsPublishResource.java,v $
* Date   : $Date: 2001/12/06 10:02:00 $
* Version: $Revision: 1.7 $
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

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import javax.servlet.http.*;
import java.util.*;

/**
 * Template class for displaying the publish screen of the OpenCms workplace.<P>
 * Reads template files of the content type <code>CmsXmlWpTemplateFile</code>.
 *
 * @author Edna Falkenhan
 * @version $Revision: 1.7 $ $Date: 2001/12/06 10:02:00 $
 */

public class CmsPublishResource extends CmsWorkplaceDefault implements I_CmsWpConstants,I_CmsConstants {

    /**
     * Overwrites the getContent method of the CmsWorkplaceDefault.<br>
     * Gets the content of the undelete template and processed the data input.
     * @param cms The CmsObject.
     * @param templateFile The undelete template file
     * @param elementName not used
     * @param parameters Parameters of the request and the template.
     * @param templateSelector Selector of the template tag to be displayed.
     * @return Bytearre containgine the processed data of the template.
     * @exception Throws CmsException if something goes wrong.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);

        // the template to be displayed
        String template = null;

        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {
            // remove all session values
            session.removeValue(C_PARA_FILE);
            session.removeValue("lasturl");
        }
        // get the lasturl parameter
        String lasturl = getLastUrl(cms, parameters);

        String filename = (String)parameters.get(C_PARA_FILE);
        if(filename != null) {
            session.putValue(C_PARA_FILE, filename);
        }
        filename = (String)session.getValue(C_PARA_FILE);
        String action = (String)parameters.get("action");
        CmsResource file = null;
        if(filename.endsWith("/")){
            file = (CmsResource)cms.readFolder(filename, true);
        } else {
            file = (CmsResource)cms.readFileHeader(filename);
        }

        if(action!= null){
            if ("check".equals(action)){
                if(file.getState() != C_STATE_DELETED){
                    if(checkLocked(cms, file)){
                        action = "wait";
                    } else {
                        // ask user if the locks should be removed
                        return startProcessing(cms, xmlTemplateDocument, elementName, parameters,"asklock");
                    }
                } else {
                    action = "wait";
                }
            } else if("rmlocks".equals(action)){
                // remove the locks and publish
                try{
                    unlockResource(cms, file);
                    action = "wait";
                } catch (CmsException exc){
                    xmlTemplateDocument.setData("details", Utils.getStackTrace(exc));
                    xmlTemplateDocument.setData("lasturl", lasturl);
                    return startProcessing(cms, xmlTemplateDocument, elementName, parameters,"errorlock");
                }

            //check if the name parameter was included in the request
            // if not, the publishresource page is shown for the first time
            }
            if((action != null) && "wait".equals(action)) {
                return startProcessing(cms, xmlTemplateDocument, "", parameters, "wait");
            }
            if((action != null) && "ok".equals(action)) {
                // publish the resource
                try{
                    cms.publishResource(file.getAbsolutePath());
                    session.removeValue(C_PARA_FILE);
                    template = "done";
                } catch(CmsException e){
                    session.removeValue(C_PARA_FILE);
                    xmlTemplateDocument.setData("details", Utils.getStackTrace(e));
                    xmlTemplateDocument.setData("lasturl", lasturl);
                    return startProcessing(cms, xmlTemplateDocument, "", parameters, "error");
                }
            }
        }

        // set the required datablocks
        if(action == null) {
            CmsXmlLanguageFile lang = xmlTemplateDocument.getLanguageFile();
            xmlTemplateDocument.setData("CHANGEDATE", Utils.getNiceDate(file.getDateLastModified()));
            xmlTemplateDocument.setData("USER", cms.readUser(file.getResourceLastModifiedBy()).getName());
            xmlTemplateDocument.setData("FILENAME", file.getName());
        }
        // process the selected template
        return startProcessing(cms, xmlTemplateDocument, "", parameters, template);
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
