/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsNewExplorerFileList.java,v $
* Date   : $Date: 2002/05/02 07:15:07 $
* Version: $Revision: 1.46 $
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

import java.util.*;
import com.opencms.launcher.*;
import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.template.*;
import com.opencms.template.cache.*;
import com.opencms.util.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * Template class for dumping files to the output without further
 * interpreting or processing.
 * This can be used for plain text files or files containing graphics.
 *
 * @author Alexander Lucas
 * @version $Revision: 1.46 $ $Date: 2002/05/02 07:15:07 $
 */

public class CmsNewExplorerFileList implements I_CmsDumpTemplate,I_CmsLogChannels,I_CmsConstants,I_CmsWpConstants {


    /**
     * Template cache is not used here since we don't include
     * any subtemplates.
     */
    private static I_CmsTemplateCache m_cache = null;


    /**
     * This is the nummber of resources that are shown on one page.
     * If a folder contains more than this we have to split the entrys
     * on more than one page.
     * TODO: this should be saved iin the usersettiings, so each user
     *      can say how much he wants to see at once(and how long he has to wait for it)
     */
    private final static int C_ENTRYS_PER_PAGE = 50;

    // the session key for the current page
    private final static String C_SESSION_CURRENT_PAGE = "explorerFilelistCurrentPage";

    /** Boolean for additional debug output control */
    private static final boolean C_DEBUG = false;

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
     * Insert the method's description here.
     * Creation date: (07.12.00 17:08:30)
     * @return java.lang.String
     * @param value java.lang.String
     */

    private String getChars(String value) {
        String ret = "";
        int num;
        for(int i = 0;i < value.length();i++) {
            num = value.charAt(i);
            if((num > 122) || (num < 48)) {
                ret += "&#" + num + ";";
            }
            else {
                ret += value.charAt(i);
            }
        }
        return ret + "";
    }

    /**
     * Gets the content of a given template file.
     *
     * @param cms CmsObject Object for accessing system resources
     * @param templateFile Filename of the template file
     * @param elementName <em>not used here</em>.
     * @param parameters <em>not used here</em>.
     * @return Unprocessed content of the given template file.
     * @exception CmsException
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters) throws CmsException {
        if(A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING && C_DEBUG) {
            A_OpenCms.log(C_OPENCMS_DEBUG, "[CmsDumpTemplate] Now dumping contents of file "
                    + templateFile);
        }
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsXmlWpTemplateFile templateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        CmsXmlLanguageFile lang = templateDocument.getLanguageFile();

        // get the right folder
        String currentFolder = (String)parameters.get("folder");
        if((currentFolder != null) && (!"".equals(currentFolder)) && folderExists(cms,
                currentFolder)) {
            session.putValue(C_PARA_FILELIST, currentFolder);
        }else {
            currentFolder = (String)session.getValue(C_PARA_FILELIST);
            if((currentFolder == null) || (!folderExists(cms, currentFolder))) {
                currentFolder = cms.rootFolder().getAbsolutePath();
                session.putValue(C_PARA_FILELIST, currentFolder);
            }
        }

        String mode = (String)parameters.get("mode");
        // if the parameter mode=listonly is set, only the list will be shown
        boolean listonly = "listonly".equals(mode);
        // if the parameter mode=projectview is set, all changed files in that project will be shown
        boolean projectView = "projectview".equals(mode);
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
        int currentFolderId = (cms.readFolder(currentFolder)).getResourceId();
        // start creating content
        StringBuffer content = new StringBuffer();
        content.append("<html> \n<head> \n<script language=JavaScript>\n");
        content.append("function initialize() {\n");

        if(listonly) {
            content.append("top.openfolderMethod='openthisfolderflat';\n");
        } else {
            content.append("top.openfolderMethod='openthisfolder';\n");
        }
        if(projectView) {
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
            content.append("top.flaturl='" + flaturl + "';\n");
        } else if (!listonly){
            content.append("top.flaturl='';\n");
        }

        // the help_url
        content.append("top.help_url='ExplorerAnsicht/index.html';\n");
        // the project
        content.append("top.setProject(" + cms.getRequestContext().currentProject().getId() + ");\n");
        // the onlineProject
        content.append("top.setOnlineProject(" + cms.onlineProject().getId() + ");\n");
        // set the checksum for the tree
        content.append("top.setChecksum(" + check + ");\n");
        // set the writeAccess for the current Folder
        CmsFolder test = cms.readFolder(currentFolder);
        boolean writeAccess = test.getProjectId() == cms.getRequestContext().currentProject().getId();
        content.append("top.enableNewButton(" + writeAccess + ");\n");
        // the folder
        content.append("top.setDirectory(" + currentFolderId + ",\"" + currentFolder + "\");\n");
        content.append("top.rD();\n\n");

        // now look which filelist colums we want to show
        int filelist = getDefaultPreferences(cms);
        boolean showTitle = (filelist & C_FILELIST_TITLE) > 0;
        boolean showDateChanged = (filelist & C_FILELIST_CHANGED) > 0;
        boolean showOwner = (filelist & C_FILELIST_OWNER) > 0;
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
        if(!(listonly || projectView)){
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
            content.append("\"" + res.getName() + "\",");
            // the path
            if(projectView){
                content.append("\"" + res.getPath() + "\",");
            }else{
                //is taken from top.setDirectory
                content.append("\"\",");
            }
            // the title
            if(showTitle){
                String title = "";
                try {
                    title = cms.readProperty(res.getAbsolutePath(), C_PROPERTY_TITLE);
                }catch(CmsException e) {
                }
                if(title == null) {
                    title = "";
                }
                content.append("\"" + getChars(title) + "\",");
            }else{
                content.append("\"\",");
            }
            // the type
            content.append(res.getType() + ",");
            // date of last change
            if(showDateChanged){
                content.append("\"" + Utils.getNiceDate(res.getDateLastModified()) + "\",");
            }else{
                content.append("\"\",");
            }
            // TODO:user who changed it: content.append("\"" + "TODO" + "\",");
            content.append("\"\",");
            // date
            // not yet used: content.append("\"" + Utils.getNiceDate(res.getDateCreated()) + "\",");
            content.append("\"\",");
            // size
            if(res.isFolder() || (!showSize)) {
                content.append("\"\",");
            }else {
                content.append( res.getLength() + ",");
            }
            // state
            content.append(res.getState() + ",");
            // project
            content.append(res.getProjectId() + ",");
            // owner
            if(showOwner){
                content.append("\"" + cms.readUser(res.getOwnerId()).getName() + "\",");
            }else{
                content.append("\"\",");
            }
            // group
            if(showGroup){
                content.append("\"" + cms.readGroup(res).getName() + "\",");
            }else{
                content.append("\"\",");
            }
            // accessFlags
            content.append(res.getAccessFlags() + ",");
            // locked by
            if(res.isLockedBy() == C_UNKNOWN_ID) {
                content.append("\"\",");
            }else {
                content.append("\"" + cms.lockedBy(res).getName() + "\",");
            }
            // locked in project
            int lockedInProject = res.getLockedInProject();
            String lockedInProjectName = "";
            try {
                lockedInProjectName = cms.readProject(lockedInProject).getName();
            } catch(CmsException exc) {
                // ignore the exception - this is an old project so ignore it
            }
            content.append("\"" + lockedInProjectName + "\"," + lockedInProject + ");\n");
        }

        //  now the tree, only if changed
        if(newTreePlease && (!listonly)) {
            content.append("\n top.rT();\n");
            Vector tree = cms.getFolderTree();
            int startAt = 1;
            int parentId;
            boolean grey = false;
            int onlineProjectId = cms.onlineProject().getId();
            if(onlineProjectId == cms.getRequestContext().currentProject().getId()) {

                // all easy: we are in the onlineProject
                CmsFolder rootFolder = (CmsFolder)tree.elementAt(0);
                content.append("top.aC(");
                content.append(rootFolder.getResourceId() + ", ");
                content.append("\"" + lang.getDataValue("title.rootfolder") + "\", ");
                content.append(rootFolder.getParentId() + ", false);\n");
                for(int i = startAt;i < tree.size();i++) {
                    CmsFolder folder = (CmsFolder)tree.elementAt(i);
                    content.append("top.aC(");
                    // id
                    content.append(folder.getResourceId() + ", ");
                    // name
                    content.append("\"" + folder.getName() + "\", ");
                    // parentId
                    content.append(folder.getParentId() + ", false);\n");
                }
            }else {
                // offline Project
                Hashtable idMixer = new Hashtable();
                CmsFolder rootFolder = (CmsFolder)tree.elementAt(0);
                String folderToIgnore = null;
                if(rootFolder.getProjectId() != onlineProjectId) {
                    startAt = 2;
                    grey = false;
                    idMixer.put(new Integer(((CmsFolder)tree.elementAt(1)).getResourceId()),
                            new Integer(rootFolder.getResourceId()));
                }else {
                    grey = true;
                }
                content.append("top.aC(");
                content.append(rootFolder.getResourceId() + ", ");
                content.append("\"" + lang.getDataValue("title.rootfolder") + "\", ");
                content.append(rootFolder.getParentId() + ", " + grey + ");\n");
                for(int i = startAt;i < tree.size();i++) {
                    CmsFolder folder = (CmsFolder)tree.elementAt(i);
                    if((folder.getState() == C_STATE_DELETED) || (folder.getAbsolutePath().equals(folderToIgnore))) {

                        // if the folder is deleted - ignore it and the following online res
                        folderToIgnore = folder.getAbsolutePath();
                    }else {
                        if(folder.getProjectId() != onlineProjectId) {
                            grey = false;
                            parentId = folder.getParentId();
                            try {
                                // the next res is the same res in the online-project: ignore it!
                                if(folder.getAbsolutePath().equals(((CmsFolder)tree.elementAt(i + 1)).getAbsolutePath())) {
                                    i++;
                                    idMixer.put(new Integer(((CmsFolder)tree.elementAt(i)).getResourceId()),
                                            new Integer(folder.getResourceId()));
                                }
                            }catch(IndexOutOfBoundsException exc) {
                            // ignore the exception, this was the last resource
                            }
                        }else {
                            grey = true;
                            parentId = folder.getParentId();
                            if(idMixer.containsKey(new Integer(parentId))) {
                                parentId = ((Integer)idMixer.get(new Integer(parentId))).intValue();
                            }
                        }
                        content.append("top.aC(");
                        // id
                        content.append(folder.getResourceId() + ", ");
                        // name
                        content.append("\"" + folder.getName() + "\", ");
                        // parentId
                        content.append(parentId + ", " + grey + ");\n");
                    }
                }
            }
        }
        if(listonly || projectView) {
            // only show the filelist
            content.append(" top.dUL(document); \n");
        } else {
            // update all frames
            content.append(" top.dU(document,"+numberOfPages+","+selectedPage+"); \n");
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
     * @exception CmsException
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
            filelist = C_FILELIST_NAME + C_FILELIST_TITLE + C_FILELIST_TYPE + C_FILELIST_CHANGED;
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

        //return templateFile.getAbsolutePath();
        //Vector v = new Vector();
        CmsRequestContext reqContext = cms.getRequestContext();

        //v.addElement(reqContext.currentProject().getName());
        //v.addElement(templateFile);
        //return v;
        return "" + reqContext.currentProject().getId() + ":" + templateFile;
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
                            resources = cms.getResourcesWithProperty(definition, value, type);
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
                            String content = searchForm.getValue01();
                        }
                    }
                }
                // remove the channel resources
                for(int i=0; i<resources.size(); i++){
                    CmsResource curRes = (CmsResource)resources.elementAt(i);
                    if(curRes.getResourceName().startsWith(cms.getRequestContext().getSiteName()+cms.C_ROOTNAME_COS)){
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
        } else {
            return cms.getResourcesInFolder(param);
        }
    }

}
