/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProjectNew.java,v $
* Date   : $Date: 2003/09/17 14:30:14 $
* Version: $Revision: 1.87 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsGroup;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsRegistry;
import com.opencms.file.CmsRequestContext;
import com.opencms.file.CmsResource;
import com.opencms.template.CmsXmlTemplateFile;
import com.opencms.util.Utils;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace admin project screens.
 * <P>
 *
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Mario Stanke
 * @version $Revision: 1.87 $ $Date: 2003/09/17 14:30:14 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminProjectNew extends CmsWorkplaceDefault {


    /** Session key */
    private static String C_NEWNAME = "new_project_name";


    /** Session key */
    private static String C_NEWDESCRIPTION = "new_project_description";


    /** Session key */
    private static String C_NEWGROUP = "new_project_group";


    /** Session key */
    private static String C_NEWMANAGERGROUP = "new_project_managergroup";

    /** Session key */
    private static String C_NEWFOLDER = "new_project_folder";


    /** Session key */
    private static String C_NEWTYPE = "projecttype";

    /** Session key */
    private static String C_NEWRESOURCES = "ALLRES";

    /** Session key */
    private static String C_NEWCHANNELS = "ALLCHAN";

    /** Check whether some of the resources are redundant because a superfolder has also
     *  been selected.
     *
     * @param resources containts the full pathnames of all the resources
     * @return A vector with the same resources, but the paths in the return value are disjoint
     */

    private void checkRedundancies(Vector resources) {
        int i, j;
        if(resources == null) {
            return ;
        }
        Vector redundant = new Vector();
        int n = resources.size();
        if(n < 2) {

            // no check needed, because there is only one resource or
            // no resources selected, return empty Vector
            return ;
        }
        for(i = 0;i < n;i++) {
            redundant.addElement(new Boolean(false));
        }
        for(i = 0;i < n - 1;i++) {
            for(j = i + 1;j < n;j++) {
                if(((String)resources.elementAt(i)).length() <
                        ((String)resources.elementAt(j)).length()) {
                    if(((String)resources.elementAt(j)).startsWith((String)resources.elementAt(i))) {
                        redundant.setElementAt(new Boolean(true), j);
                    }
                }
                else {
                    if(((String)resources.elementAt(i)).startsWith((String)resources.elementAt(j))) {
                        redundant.setElementAt(new Boolean(true), i);
                    }
                }
            }
        }
        for(i = n - 1;i >= 0;i--) {
            if(((Boolean)redundant.elementAt(i)).booleanValue()) {
                resources.removeElementAt(i);
            }
        }
    }

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters.
     *
     * @see #getContent(CmsObject, String, String, Hashtable, String)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */
    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        if(OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).debug("Template file is: " + templateFile);
            OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsRequestContext reqCont = cms.getRequestContext();
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        // flag for extended features in the editor, e.g. list of external links
        CmsRegistry registry = cms.getRegistry();
        boolean extendedNavigation = "on".equals(registry.getSystemValue("extendedNavigation"));
        // clear session values on first load
        String initial = (String)parameters.get(C_PARA_INITIAL);
        if(initial != null) {

            // remove all session values
            session.removeValue(C_NEWNAME);
            session.removeValue(C_NEWGROUP);
            session.removeValue(C_NEWDESCRIPTION);
            session.removeValue(C_NEWMANAGERGROUP);
            session.removeValue(C_NEWFOLDER);
            session.removeValue(C_NEWRESOURCES);
            session.removeValue(C_NEWCHANNELS);
            session.removeValue(C_NEWTYPE);
            session.removeValue("lasturl");
            session.removeValue("newProjectCallingFrom");
            reqCont.setCurrentProject(I_CmsConstants.C_PROJECT_ONLINE_ID);
        }
        String newName, newGroup, newDescription, newManagerGroup;
        int projectType = 0;
        String newType = new String();
        String action = new String();
        action = (String)parameters.get("action");
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile,
                elementName, parameters, templateSelector);

        //look if we come from the explorer view
        String fileToGo = (String)parameters.get(C_PARA_RESOURCE);
        if (fileToGo == null){
            fileToGo = (String)session.getValue("newProjectCallingFrom");
        } else {
            CmsResource resource = cms.readFileHeader(fileToGo);
            if (resource.isFolder() && !resource.getRootPath().endsWith(I_CmsConstants.C_FOLDER_SEPARATOR)) {
                fileToGo += I_CmsConstants.C_FOLDER_SEPARATOR;
            }
        }
        String lasturl = (String)parameters.get("lasturl");
        if (lasturl == null){
            lasturl = (String)session.getValue("lasturl");
        }
        newName = (String)parameters.get(C_PROJECTNEW_NAME);
        if(newName == null) {
            newName = (String)session.getValue(C_NEWNAME);
        }
        String errorTemplateAddOn = "";
        if (fileToGo != null){
            // this is from the explorer view
            if((!cms.getRequestContext().isProjectManager()) && (!cms.isAdmin())){
                // user has no rights to create a project
                return startProcessing(cms, xmlTemplateDocument, elementName,parameters, "norigths");
            }
            errorTemplateAddOn = "explorer";
            session.putValue("newProjectCallingFrom", fileToGo);
            xmlTemplateDocument.setData("pathCorrection","");
            xmlTemplateDocument.setData("backButton",lasturl);
            xmlTemplateDocument.setData("myUrl","resource_to_project.html");
            xmlTemplateDocument.setData("dontDoIt", " //");
            // we have to put the file in the box and set the projectname
            xmlTemplateDocument.setData("doThis","addFolder(document.PROJECTNEW.new_ressources,'"+fileToGo+"');");
            if (newName == null){
                newName = getProjectName(cms,fileToGo);
            }
        }else{
            // this is from the administration view
            xmlTemplateDocument.setData("pathCorrection","../../");
            xmlTemplateDocument.setData("backButton","../../../action/administration_content_top.html?sender=" + C_VFS_PATH_WORKPLACE + "administration/project/");
            xmlTemplateDocument.setData("myUrl","index.html");
            xmlTemplateDocument.setData("dontDoIt", "");
            xmlTemplateDocument.setData("doThis","");
        }

        xmlTemplateDocument.setData("onlineId", "" + I_CmsConstants.C_PROJECT_ONLINE_ID);

        newGroup = (String)parameters.get(C_PROJECTNEW_GROUP);
        newDescription = (String)parameters.get(C_PROJECTNEW_DESCRIPTION);
        newManagerGroup = (String)parameters.get(C_PROJECTNEW_MANAGERGROUP);
        String allResources = (String)parameters.get(C_NEWRESOURCES);
        String allChannels = (String)parameters.get(C_NEWCHANNELS);
        newType = (String)parameters.get(C_NEWTYPE);

        // if there are still values in the session (like after an error), use them
        if(newGroup == null) {
            newGroup = (String)session.getValue(C_NEWGROUP);
        }
        if(newDescription == null) {
            newDescription = (String)session.getValue(C_NEWDESCRIPTION);
        }
        if(newManagerGroup == null) {
            newManagerGroup = (String)session.getValue(C_NEWMANAGERGROUP);
        }
        if(allResources == null) {
            allResources = (String)session.getValue(C_NEWRESOURCES);
        }
        if(allChannels == null) {
            allChannels = (String)session.getValue(C_NEWCHANNELS);
        }
        if(newType == null) {
            newType = (String)session.getValue(C_NEWTYPE);
        }
        if(newName == null) {
            newName = "";
        }
        if(newGroup == null) {
            newGroup = "";
        }
        if(newDescription == null) {
            newDescription = "";
        }
        if(newManagerGroup == null) {
            newManagerGroup = "";
        }
        if(allResources == null) {
            allResources = "";
        }
        if(allChannels == null) {
            allChannels = "";
        }
        if(newType == null || "".equals(newType)) {
            projectType = I_CmsConstants.C_PROJECT_TYPE_NORMAL;
            newType = "";
        } else {
            projectType = I_CmsConstants.C_PROJECT_TYPE_TEMPORARY;
        }

        if(parameters.get("submitform") != null) {
            // the form has just been submitted, store the data in the session
            session.putValue(C_NEWNAME, newName);
            session.putValue(C_NEWGROUP, newGroup);
            session.putValue(C_NEWDESCRIPTION, newDescription);
            session.putValue(C_NEWMANAGERGROUP, newManagerGroup);
            session.putValue(C_NEWTYPE, newType);
            session.putValue(C_NEWCHANNELS, allChannels);
            if(newName.equals("") || newGroup.equals("") || newManagerGroup.equals("")
                    || (allResources.equals("") && allChannels.equals(""))) {
                templateSelector = "datamissing"+errorTemplateAddOn;
            }
            else {
                session.putValue(C_NEWRESOURCES, allResources);
                // all the required data has been entered, display 'Please wait'
                templateSelector = "wait";
                action = "start";
            }
        }

        // is the wait-page showing?
        if("working".equals(action)) {
            // create new Project
            try {
                // append the C_VFS_GALLERY_PICS and C_VFS_GALLERY_DOWNLOAD path to the list of all resources
                String picspath = getConfigFile(cms).getPicGalleryPath();
                String downloadpath = getConfigFile(cms).getDownGalleryPath();
                String linkpath = getConfigFile(cms).getLinkGalleryPath();
                String htmlPath = getConfigFile(cms).getHtmlGalleryPath();
                allResources = allResources + ";" + picspath + ";"
                        + downloadpath + ";" + linkpath + ";" + htmlPath;
                if(extendedNavigation){
                    allResources = allResources + ";" + C_VFS_PATH_DEFAULTMODULE + "elements/";
                }
                // 'allResurces' has the "form res1;res2;...resk;"
                // this is because the simpler 'getParameterValues' method doesn't work with Silverstream
                Vector folders = parseResources(allResources);
                int numRes = folders.size();
                for(int i = 0;i < numRes;i++) {
                    // modify the foldername if nescessary (the root folder is always given
                    // as a nice name)
                    if(lang.getLanguageValue("title.rootfolder").equals(folders.elementAt(i))) {
                        folders.setElementAt("/", i);
                    }
                }
                checkRedundancies(folders);
                numRes = folders.size(); // could have been changed
                // now get the vector for the channels
                Vector channels = parseResources(allChannels);
                int numChan = channels.size();
                for(int j = 0;j < numChan;j++) {
                    // modify the channelname if nescessary (the root folder is always given
                    // as a nice name)
                    if(lang.getLanguageValue("title.rootfolder").equals(channels.elementAt(j))) {
                        channels.setElementAt("/", j);
                    }
                }
                checkRedundancies(channels);
                numChan = channels.size();
                // finally create the project
                CmsProject project = cms.createProject(newName, newDescription, newGroup,
                        newManagerGroup, projectType);
                // change the current project
                reqCont.setCurrentProject(project.getId());
                // copy the resources to the current project
                try {
                    for(int i = 0;i < folders.size();i++) {
                        cms.copyResourceToProject((String)folders.elementAt(i));
                    }
                    //now copy the channels to the project
                    cms.getRequestContext().saveSiteRoot();
                    cms.setContextToCos();
                    for(int j = 0; j < channels.size(); j++){
                        cms.copyResourceToProject((String)channels.elementAt(j));
                    }
                    cms.getRequestContext().restoreSiteRoot();
                } catch(CmsException e) {
                    cms.getRequestContext().restoreSiteRoot();
                    // if there are no projectresources in the project delete the project
                    Vector projectResources = cms.readAllProjectResources(project.getId());
                    if((projectResources == null) || (projectResources.size() == 0)){
                        cms.deleteProject(project.getId());
                        reqCont.setCurrentProject(C_PROJECT_ONLINE_ID);
                    }
                    if(OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).isWarnEnabled() ) {
                        OpenCms.getLog(CmsLog.CHANNEL_WORKPLACE_XML).warn(e.getMessage(), e);
                    }
                    throw e;
                }
                // project ready; clear the session
                session.removeValue(C_NEWNAME);
                session.removeValue(C_NEWGROUP);
                session.removeValue(C_NEWDESCRIPTION);
                session.removeValue(C_NEWMANAGERGROUP);
                session.removeValue(C_NEWFOLDER);
                session.removeValue(C_NEWTYPE);
                session.removeValue(C_NEWRESOURCES);
                session.removeValue(C_NEWCHANNELS);
                session.removeValue("lasturl");
                session.removeValue("newProjectCallingFrom");
                return startProcessing(cms, xmlTemplateDocument, elementName, parameters, "done");
            } catch(CmsException exc) {
                xmlTemplateDocument.setData("details", Utils.getStackTrace(exc));
                templateSelector = "errornewproject"+errorTemplateAddOn;
            }
        }
        // after an error the form data is retrieved and filled into the template
        xmlTemplateDocument.setData(C_NEWNAME, newName);
        xmlTemplateDocument.setData(C_NEWDESCRIPTION, newDescription);
        xmlTemplateDocument.setData(C_NEWTYPE, newType);

        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters,
                templateSelector);
    }

    /**
     * Gets all groups, that may work for a project.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors.
     * @throws CmsException
     */

    public Integer getGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        // get all groups
        Vector groups = cms.getGroups();
        int retValue = -1;
        String defaultGroup = OpenCms.getDefaultUsers().getGroupUsers();
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String enteredGroup = (String)session.getValue(C_NEWGROUP);
        if(enteredGroup != null && !enteredGroup.equals("")) {

            // if an error has occurred before, take the previous entry of the user
            defaultGroup = enteredGroup;
        }

        // fill the names and values
        int n = 0;
        for(int z = 0;z < groups.size();z++) {
            if(((CmsGroup)groups.elementAt(z)).getProjectCoWorker()) {
                String name = ((CmsGroup)groups.elementAt(z)).getName();
                if(defaultGroup.equals(name)) {
                    retValue = n;
                }
                names.addElement(name);
                values.addElement(name);
                n++; // count the number of ProjectCoWorkers
            }
        }
        return new Integer(retValue);
    }

    /**
     * Gets all groups, that may manage a project.
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the current value in the vectors.
     * @throws CmsException
     */

    public Integer getManagerGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        // get all groups
        Vector groups = cms.getGroups();
        int retValue = -1;
        String defaultGroup = OpenCms.getDefaultUsers().getGroupProjectmanagers();
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String enteredGroup = (String)session.getValue(C_NEWMANAGERGROUP);
        if(enteredGroup != null && !enteredGroup.equals("")) {

            // if an error has occurred before, take the previous entry of the user
            defaultGroup = enteredGroup;
        }

        // fill the names and values
        int n = 0;
        for(int z = 0;z < groups.size();z++) {
            if(((CmsGroup)groups.elementAt(z)).getProjectmanager()) {
                String name = ((CmsGroup)groups.elementAt(z)).getName();
                if(defaultGroup.equals(name)) {
                    retValue = n;
                }
                names.addElement(name);
                values.addElement(name);
                n++; // count the number of project managers
            }
        }
        return new Integer(retValue);
    }

    public Integer getSelectedResources(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String[] newProjectResources = (String[])session.getValue(C_NEWRESOURCES);
        if(newProjectResources != null) {
            for(int i = 0;i < newProjectResources.length;i++) {
                names.addElement(newProjectResources[i]);
                values.addElement(newProjectResources[i]);
            }
        }

        // no current folder, set index to -1
        return new Integer(-1);
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

    /** Parse the string which holds all resources
     *
     * @param resources containts the full pathnames of all the resources, separated by semicolons
     * @return A vector with the same resources
     */

    private Vector parseResources(String resources) {
        Vector ret = new Vector();
        if(resources != null) {
            StringTokenizer resTokenizer = new StringTokenizer(resources, ";");
            while(resTokenizer.hasMoreElements()) {
                String path = (String)resTokenizer.nextElement();
                ret.addElement(path);
            }
        }
        return ret;
    }

    /** gets the projectname for a contexgenerated project
     *
     * @param cms the cmsObject
     * @param resource the name of the resource
     * @return A vector with the same resources
     */

    private String getProjectName(CmsObject cms, String resource) {
        String ret = resource;
        if (ret.endsWith("/")){
            ret = ret.substring(0, ret.length()-1);
        }
        ret = ret.substring(ret.lastIndexOf('/')+1);
        if (ret.length() > 14){
            ret = ret.substring(0,13);
        }
        try{
            Vector allProjects = cms.getAllAccessibleProjects();
            Vector theNames = new Vector();
            // count all projects starting with the same name
            int count = 0;
            for (int i = 0; i < allProjects.size(); i++){
                String currProject = ((CmsProject)allProjects.elementAt(i)).getName();
                if (currProject.startsWith(ret)){
                    count++;
                    theNames.addElement(currProject);
                }
            }
            if ((count > 0) && (count < 99)){
                // get the highest version nummber
                int version = 1;
                for (int i = 0; i<theNames.size(); i++){
                    int currVersion = 0;
                    try{
                        currVersion = Integer.parseInt(((String)theNames.elementAt(i)).substring(ret.length()+1));
                    }catch(Exception e){
                    }
                    if ((currVersion > version)&& (currVersion < 100)){
                        version = currVersion;
                    }
                }
                if (version < 99){
                    ret = ret + "_" + (version + 1);
                }
            }
        }catch(CmsException e){
        }
        return ret;
    }
}
