/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewExplorerFileList.java,v $
* Date   : $Date: 2003/07/11 13:29:30 $
* Version: $Revision: 1.75 $
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

import org.opencms.workplace.CmsWorkplaceAction;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsFolder;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsRequestContext;
import com.opencms.file.CmsResource;
import com.opencms.file.I_CmsRegistry;
import com.opencms.flex.util.CmsMessages;
import com.opencms.flex.util.CmsUUID;
import com.opencms.launcher.I_CmsTemplateCache;
import com.opencms.template.CmsCacheDirectives;
import com.opencms.template.I_CmsDumpTemplate;
import com.opencms.template.cache.A_CmsElement;
import com.opencms.template.cache.CmsElementDump;
import com.opencms.util.Encoder;
import com.opencms.util.Utils;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

/**
 * Template class for dumping files to the output without further
 * interpreting or processing.
 * This can be used for plain text files or files containing graphics.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.75 $ $Date: 2003/07/11 13:29:30 $
 */
public class CmsNewExplorerFileList implements I_CmsDumpTemplate,I_CmsLogChannels,I_CmsConstants,I_CmsWpConstants {

    /**
     * This is the number of resources that are shown on one page.
     * If a folder contains more than this we have to split the entrys
     * to more than one page.
     * TODO: set this in the user preferences, so that each user can select how many resources he wants to see at once
     */
    private final static int C_ENTRYS_PER_PAGE = 50;

    // the session key for the current page
    private final static String C_SESSION_CURRENT_PAGE = "explorerFilelistCurrentPage";

    /** Internal debugging flag */
    public static final int DEBUG = 0;

    public CmsNewExplorerFileList() {
    }

    /**
     * gets the caching information from the current template class.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return <EM>true</EM> if this class may stream it's results, <EM>false</EM> otherwise.
     */
    public CmsCacheDirectives getCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        // First build our own cache directives.
        return new CmsCacheDirectives(false);
    }

    /**
     * Insert the method's description here.
     * Creation date: (29.11.00 14:05:21)
     * @return boolean
     * @param cms com.opencms.file.CmsObject
     * @param path java.lang.String
     */

    private boolean folderExists(CmsObject cms, String path) {
        try {
            CmsFolder test = cms.readFolder(path);
            if (test.isFile()){
                return false;
            }
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Gets the content of a given template file.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @return Unprocessed content of the given template file.
     * @throws CmsException
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters) throws CmsException {
        if(A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_DEBUG) && I_CmsLogChannels.C_LOGGING && (DEBUG > 0)) {
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsDumpTemplate] Now dumping contents of file "
                    + templateFile);
        }
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String lang = CmsXmlLanguageFile.getCurrentUserLanguage(cms);
        CmsMessages messages = new CmsMessages("com.opencms.workplace.workplace", new Locale(lang, "", ""));        

        // get the right folder
        String currentFolder = (String)parameters.get("folder");
        if ((currentFolder != null) && (currentFolder.startsWith("vfslink:"))) {
            // this is a link chck, remove the prefix
            parameters.put("mode", "vfslink");
            parameters.put("file", currentFolder.substring(8));  
        } else {
            if((currentFolder != null) && (!"".equals(currentFolder)) && 
                    folderExists(cms, currentFolder)) {
            // session.putValue(C_PARA_FILELIST, currentFolder);
            CmsWorkplaceAction.setCurrentFolder(cms, currentFolder);
        }else {
            // currentFolder = (String)session.getValue(C_PARA_FILELIST);
            currentFolder = CmsWorkplaceAction.getCurrentFolder(cms);
            if((currentFolder == null) || (!folderExists(cms, currentFolder))) {
                currentFolder = cms.readAbsolutePath(cms.rootFolder());
                // session.putValue(C_PARA_FILELIST, currentFolder);
                CmsWorkplaceAction.setCurrentFolder(cms, currentFolder);
            }
        }
        }

        String mode = (String)parameters.get("mode");
        // if the parameter mode=listonly is set, only the list will be shown
        boolean listonly = "listonly".equals(mode); 
        // if the parameter mode=projectview is set, all changed files in that project will be shown
        boolean projectView = "projectview".equals(mode);
        // if the parameter mode=vfslinks is set, the links to a target file will be shown
        boolean vfslinkView = "vfslink".equals(mode);
        if (vfslinkView) {
            String file = (String)parameters.get("file");
            boolean found = true;
            try {
                cms.readFileHeader(file);
            } catch (CmsException e) {
                // file was not readable
                found = false;
            }
            if (found) {
                // file / folder exists and is readable
                currentFolder = "vfslink:" + file;
            } else {
                // show the root folder in case of an error and reset the state
                currentFolder = cms.readAbsolutePath(cms.rootFolder());
                vfslinkView = false;
                parameters.remove("mode");
            }
            // session.putValue(C_PARA_FILELIST, currentFolder);          
            CmsWorkplaceAction.setCurrentFolder(cms, currentFolder);  
        }
        
        if (DEBUG > 2) {
            // output parameters
            System.err.println("[" + System.currentTimeMillis() + "] CmsNewExplorerFileList.getContent() called");
            System.err.println("templateFile=" + templateFile);
            System.err.println("elementName=" + elementName);
            Iterator i = parameters.keySet().iterator();
            while (i.hasNext()){
                String key = (String)i.next();
                System.err.println("parameters:  key=" + key + " value=" + parameters.get(key));
            }
            System.err.println();
        }

        boolean noKontext = "false".equals(parameters.get("kontext"));

        // the flaturl to use for changing folders
        String flaturl = (String) parameters.get("flaturl");

        // get the checksum
        String checksum = (String)parameters.get("check");
        boolean newTreePlease = true;
        long check = -1;
        try {
            check = Long.parseLong(checksum);
            if(check == cms.getFileSystemFolderChanges()) {
                newTreePlease = false;
            }
        }catch(Exception e) {
        }
        check = cms.getFileSystemFolderChanges();

        // get the currentFolder Id
        CmsUUID currentFolderId;
        if (! vfslinkView) {
            currentFolderId = (cms.readFolder(currentFolder)).getId();
        } else {
            currentFolderId = CmsUUID.getNullUUID();            
        }
        
        // start creating content
        StringBuffer content = new StringBuffer(2048);
        content.append("<html> \n<head> \n");
        content.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=");
        content.append(cms.getRequestContext().getEncoding());
        content.append("\">\n");
        content.append("<script language=JavaScript>\n");
        content.append("function initialize() {\n");

        if(listonly) {
            content.append("top.openfolderMethod='openthisfolderflat';\n");
        } else {
            content.append("top.openfolderMethod='openthisfolder';\n");
        }
        if(projectView || vfslinkView) {
            content.append("top.projectView=true;\n");
        } else {
            content.append("top.projectView=false;\n");
        }

        // show kontext
        if(noKontext) {
            content.append("top.showKon=false;\n");
        } else {
            content.append("top.showKon=true;\n");
        }

        // the flaturl
        if(flaturl != null) {
            content.append("top.flaturl='");
            content.append(flaturl);
            content.append("';\n");
        } else if (!listonly){
            content.append("top.flaturl='';\n");
        }

        // the help_url
        content.append("top.head.helpUrl='explorer/index.html';\n");
        // the project
        content.append("top.setProject(");
        content.append(cms.getRequestContext().currentProject().getId());
        content.append(");\n");
        // the onlineProject
        content.append("top.setOnlineProject(");
        content.append(I_CmsConstants.C_PROJECT_ONLINE_ID);
        content.append(");\n");
        // set the checksum for the tree
        content.append("top.setChecksum(");
        content.append(check);
        content.append(");\n");
        // set the writeAccess for the current Folder
        boolean writeAccess = true;
        if (! vfslinkView) {        
        	writeAccess = cms.hasPermissions(currentFolder, I_CmsConstants.C_WRITE_ACCESS);
        }
        content.append("top.enableNewButton(");
        content.append(writeAccess);
        content.append(");\n");
        // the folder
        content.append("top.setDirectory(\"");
        content.append(currentFolderId);
        content.append("\",\"");
        content.append(currentFolder);
        content.append("\");\n");
        content.append("top.rD();\n\n");

        // now look which filelist colums we want to show
        int filelist = getDefaultPreferences(cms);
        boolean showTitle = (filelist & C_FILELIST_TITLE) > 0;
        boolean showDateChanged = (filelist & C_FILELIST_DATE_LASTMODIFIED) > 0;
        boolean showOwner = (filelist & C_FILELIST_USER_CREATED) > 0;
        boolean showGroup = (filelist & C_FILELIST_GROUP) > 0;
        boolean showSize = (filelist & C_FILELIST_SIZE) > 0;

        // now get the entries for the filelist
        Vector resources = getRessources(cms, currentFolder, parameters);

        // if a folder contains to much entrys we split them to pages of C_ENTRYS_PER_PAGE
        // but only in the explorer view
        int startat = 0;
        int stopat = resources.size();
        int selectedPage = 1;
        int numberOfPages = 0;
        int maxEntrys = C_ENTRYS_PER_PAGE; // later this comes from the usersettings
        if(!(listonly || projectView || vfslinkView)){
            String selPage = (String)parameters.get("selPage");
            if(selPage == null || "".equals(selPage)){
                selPage = (String)session.getValue(C_SESSION_CURRENT_PAGE);
            }
            if(selPage != null && !"".equals(selPage)){
                try{
                    selectedPage = Integer.parseInt(selPage);
                    session.putValue(C_SESSION_CURRENT_PAGE, selPage);
                }catch(Exception e){
                }
            }
            if(stopat > maxEntrys){
                // we have to splitt
                numberOfPages = (stopat / maxEntrys) +1;
                if(selectedPage > numberOfPages){
                    // the user has changed the folder and then selected a page for the old folder
                    selectedPage =1;
                }
                startat = (selectedPage -1) * maxEntrys;
                if((startat + maxEntrys) < stopat){
                    stopat = startat + maxEntrys;
                }
            }
        }

        for(int i = startat;i < stopat;i++) {
            CmsResource res = (CmsResource)resources.elementAt(i);
            content.append("top.aF(");
            // the name
            content.append("\"");
            content.append(res.getName());
            content.append("\",");
            // the path
            if(projectView || vfslinkView){
                content.append("\"");
                // TODO: check this
                // content.append(res.getPath());
                content.append(cms.readAbsolutePath(res));
                content.append("\",");
            }else{
                //is taken from top.setDirectory
                content.append("\"\",");
            }
            // the title
            if(showTitle){
                String title = "";
                try {
                    title = cms.readProperty(cms.readAbsolutePath(res), C_PROPERTY_TITLE);
                }catch(CmsException e) {
                }
                if(title == null) {
                    title = "";
                }
                content.append("\"");
                if (title != null) content.append(Encoder.escapeHtml(title));
                content.append("\",");
                
            }else{
                content.append("\"\",");
            }
            // the type
            content.append(res.getType());
            content.append(",");
            // date of last change
            if(showDateChanged){
                content.append("\"");
                content.append(Utils.getNiceDate(res.getDateLastModified()));
                content.append("\",");
                
            }else{
                content.append("\"\",");
            }
            // unused field, was intended for the user who changed the resource last
            content.append("\"\",");
            // date
            // unused field, was intended for: content.append("\"" + Utils.getNiceDate(res.getDateCreated()) + "\",");
            content.append("\"\",");
            // size
            if(res.isFolder() || (!showSize)) {
                content.append("\"\",");
            }else {
                content.append( res.getLength());
                content.append(",");                
            }
            // state
            content.append(res.getState());
            content.append(",");            
            // project
            content.append(res.getProjectId());
            content.append(",");            
            // owner
            if(showOwner){
            	// TODO: remove this later
                // content.append("\"");                
                // content.append(cms.readUser(res.getOwnerId()).getName());
                // content.append("\",");
				
				// currently, the current permissions are displayed here
				content.append("\"");
				content.append(cms.getPermissions(cms.readAbsolutePath(res)).getPermissionString());
				content.append("\",");
				                
            }else{
                content.append("\"\",");
            }
            // group
            if(showGroup){
				// TODO: remove this later
                // content.append("\"");                
                // content.append(cms.readGroup(res).getName());
                // content.append("\","); 
				content.append("\"\",");               
            }else{
                content.append("\"\",");
            }
			// TODO: remove this later
            // accessFlags
            //content.append(res.getAccessFlags());
            content.append(0);
            content.append(",");            
            // locked by
            if(res.isLockedBy().isNullUUID()) {
                content.append("\"\",");
            }else {
                content.append("\"");                
                content.append(cms.lockedBy(res).getName());
                content.append("\",");                
            }
            // locked in project
            int lockedInProject = res.getLockedInProject();
            String lockedInProjectName = "";
            try {
                lockedInProjectName = cms.readProject(lockedInProject).getName();
            } catch(CmsException exc) {
                // ignore the exception - this is an old project so ignore it
            }
            content.append("\"");            
            content.append(lockedInProjectName);
            content.append("\",");
            content.append(lockedInProject);
            content.append(");\n");
        }

        //  now the tree, only if changed
        if(newTreePlease && (!(listonly || vfslinkView))) {
            content.append("\n top.rT();\n");
            List tree = cms.getFolderTree();
            int startAt = 1;
            CmsUUID parentId = CmsUUID.getNullUUID();
            boolean grey = false;
            int onlineProjectId = I_CmsConstants.C_PROJECT_ONLINE_ID;
            
            if(cms.getRequestContext().currentProject().isOnlineProject()) {
                Iterator i = tree.iterator();
                
                // all easy: we are in the onlineProject
                CmsFolder rootFolder = (CmsFolder) i.next();
                content.append("top.aC(\"");
                content.append(rootFolder.getId());
                content.append("\", ");
                content.append("\"");
                content.append(messages.key("title.rootfolder"));
                content.append("\", \"");
                content.append(rootFolder.getParentId());
                content.append("\", false);\n");
                
                while(i.hasNext()) {
                    CmsFolder folder = (CmsFolder) i.next();
                    content.append("top.aC(\"");
                    // id
                    content.append(folder.getId());
                    content.append("\", ");
                    // name
                    content.append("\"");
                    content.append(folder.getName());
                    content.append("\", \"");
                    // parentId
                    content.append(folder.getParentId());
                    content.append("\", false);\n");                    
                }
            }else {
                // offline Project
                Hashtable idMixer = new Hashtable();
                CmsFolder rootFolder = (CmsFolder) tree.get(0);
                String folderToIgnore = null;
                if(rootFolder.getProjectId() != onlineProjectId) {
                    //startAt = 2;
                    grey = false;
                    idMixer.put((CmsFolder)tree.get(1), rootFolder.getId());
                }else {
                    grey = true;
                }
                content.append("top.aC(\"");
                content.append(rootFolder.getId());
                content.append("\", ");
                content.append("\"");
                content.append(messages.key("title.rootfolder"));
                content.append("\", \"");
                content.append(rootFolder.getParentId());
                content.append("\", ");
                content.append(grey);
                content.append(");\n");
                for(int i = startAt;i < tree.size();i++) {
                    CmsFolder folder = (CmsFolder)tree.get(i);
                    if((folder.getState() == C_STATE_DELETED) || (cms.readAbsolutePath(folder).equals(folderToIgnore))) {

                        // if the folder is deleted - ignore it and the following online res
                        folderToIgnore = cms.readAbsolutePath(folder);
                    }else {
                        if(folder.getProjectId() != onlineProjectId) {
                            grey = false;
                            parentId = folder.getParentId();
                            try {
                                // the next res is the same res in the online-project: ignore it!
                                if(cms.readAbsolutePath(folder).equals(cms.readAbsolutePath((CmsFolder)tree.get(i + 1)))) {
                                    i++;
                                    idMixer.put((CmsFolder)tree.get(i), folder.getId());
                                }
                            }catch(IndexOutOfBoundsException exc) {
                            // ignore the exception, this was the last resource
                            }
                        }else {
                            grey = true;
                            parentId = folder.getParentId();
                            if(idMixer.containsKey(parentId)) {
                                parentId = (CmsUUID) idMixer.get(parentId);
                            }
                        }
                        content.append("top.aC(\"");
                        // id
                        content.append(folder.getId());
                        content.append("\", ");
                        // name
                        content.append("\"");
                        content.append(folder.getName());
                        content.append("\", \"");
                        // parentId
                        content.append(parentId);
                        content.append("\", ");
                        content.append(grey);
                        content.append(");\n");
                    }
                }
            }
        }
        
        if(listonly || projectView) {
            // only show the filelist
            content.append(" top.dUL(document); \n");
        } else {
            // update all frames
            content.append(" top.dU(document,");
            content.append(numberOfPages);
            content.append(",");
            content.append(selectedPage);
            content.append("); \n");
        }
        
        content.append("}\n");
        content.append("</script>\n</head> \n<BODY onLoad=\"initialize()\"></BODY> \n</html>\n");
        
        return (content.toString()).getBytes();
    }

    /**
     * Gets the content of a given template file.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @param templateSelector <em>not used here</em>.
     * @return Unprocessed content of the given template file.
     * @throws CmsException
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {

        // ignore the templateSelector since we only dump the template
        return getContent(cms, templateFile, elementName, parameters);
    }

    /**
     * Sets the default preferences for the current user if those values are not available.
     * @return Hashtable with default preferences.
     */

    private int getDefaultPreferences(CmsObject cms) {
        int filelist;
        String explorerSettings = (String)cms.getRequestContext().currentUser().getAdditionalInfo(C_ADDITIONAL_INFO_EXPLORERSETTINGS);
        if(explorerSettings != null) {
            filelist = new Integer(explorerSettings).intValue();
        }else {
            filelist = C_FILELIST_NAME + C_FILELIST_TITLE + C_FILELIST_TYPE + C_FILELIST_DATE_LASTMODIFIED;
        }
        return filelist;
    }

    /**
     * Gets the key that should be used to cache the results of
     * this template class.
     * <P>
     * Since this class is quite simple it's okay to return
     * just the name of the template file here.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     * @return key that can be used for caching
     */

    public Object getKey(CmsObject cms, String templateFile, Hashtable parameter,
            String templateSelector) {
        CmsRequestContext reqContext = cms.getRequestContext();
        StringBuffer buf = new StringBuffer();
        buf.append(reqContext.currentProject().getId());
        buf.append(":");
        buf.append(templateFile);
        return new String(buf);
    }

    /**
     * Template cache is not used here since we don't include
     * any subtemplates. So we can always return <code>true</code> here.
     * @return <code>true</code>
     */
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }
    public boolean isProxyPrivateCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
    public boolean isProxyPublicCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
    public boolean isExportable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
    public boolean isStreamable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
    }
    public CmsCacheDirectives collectCacheDirectives(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        CmsCacheDirectives myCd = new CmsCacheDirectives(false);
        return myCd;
    }

    /**
     * Any results of this class are cacheable since we don't include
     * any subtemplates. So we can always return <code>true</code> here.
     * @return <code>true</code>
     */
    public boolean isTemplateCacheSet() {
        return true;
    }

    /**
     * Template cache is not used here since we don't include
     * any subtemplates <em>(not implemented)</em>.
     */

    public void setTemplateCache(I_CmsTemplateCache c) {
        // do nothing.
    }

    /**
     * Template cache is not used here since we don't include
     * any subtemplates. So we can always return <code>false</code> here.
     * @return <code>false</code>
     */

    public boolean shouldReload(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) {
        return false;
    }

    public A_CmsElement createElement(CmsObject cms, String templateFile, Hashtable parameters) {
        return new CmsElementDump(getClass().getName(), templateFile, null, getCacheDirectives(cms, templateFile, null, parameters, null),
                                    cms.getRequestContext().getElementCache().getVariantCachesize());
    }

    /**
     * Get the resources in the folder stored in parameter param
     * or in the project shown in the projectview
     *
     * @param cms The CmsObject
     * @param param The name of the folder
     * @param parameters The parameters of the request
     * @return The vector with all ressources
     */
    private Vector getRessources(CmsObject cms, String param, Hashtable parameters) throws CmsException {
        String mode = (String)parameters.get("mode")!=null?(String)parameters.get("mode"):"";
        String submode = (String)parameters.get("submode")!=null?(String)parameters.get("submode"):"";
        if("projectview".equals(mode)) {
            I_CmsSession session = cms.getRequestContext().getSession(true);
            if("search".equals(submode)){
                Vector resources = new Vector();
                String currentFilter = (String)session.getValue("ocms_search.currentfilter");
                CmsSearchFormObject searchForm = null;
                if(currentFilter != null){
                    searchForm = (CmsSearchFormObject)((Hashtable)session.getValue("ocms_search.allfilter")).get(currentFilter);
                    if((currentFilter != null) && (searchForm != null)){
                        // flag for using lucene for search
                        I_CmsRegistry registry = cms.getRegistry();
                        boolean luceneEnabled = "on".equals(registry.getSystemValue("searchbylucene"));
                        if("property".equals(currentFilter)){
                            String definition = searchForm.getValue02();
                            String value = searchForm.getValue03();
                            int type = Integer.parseInt(searchForm.getValue01());
                            resources = cms.getVisibleResourcesWithProperty(definition, value, type);
                        } else if ("filename".equals(currentFilter)){
                            String filename = searchForm.getValue01();
                            // if lucene is enabled the use lucene for searching by filename
                            // else use the method that reads from the database
                            if(luceneEnabled){
                                // put here the lucene search for filenames
                            } else {
                                resources = cms.readResourcesLikeName(filename);
                            }
                        } else if ("content".equals(currentFilter)){
                            // this search is only available if lucene is enabled
                            searchForm.getValue01();
                        }
                    }
                }
                // remove the channel resources
                for(int i=0; i<resources.size(); i++){
                    CmsResource curRes = (CmsResource)resources.elementAt(i);
                    if(curRes.getResourceName().startsWith(C_COS_DEFAULT)){
                        resources.remove(i);
                    }
                }
                return resources;
            } else {
                String filter = new String();
                filter = (String) session.getValue("filter");
                String projectId = (String) session.getValue("projectid");
                int currentProjectId;
                if(projectId == null || "".equals(projectId)){
                    currentProjectId = cms.getRequestContext().currentProject().getId();
                } else {
                    currentProjectId = Integer.parseInt(projectId);
                }
                session.removeValue("filter");
                return cms.readProjectView(currentProjectId, filter);
            }
        } else if ("vfslink".equals(mode)) {
            String file = (String)parameters.get("file");
            List list = cms.fetchVfsLinksForResource(file);          
            return new Vector(list);
        } else {
            return cms.getResourcesInFolder(param);
        }
    }

}
