
/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminProjectNew.java,v $
* Date   : $Date: 2001/05/15 19:29:05 $
* Version: $Revision: 1.47 $
*
* Copyright (C) 2000  The OpenCms Group
*
* This File is part of OpenCms -
* the Open Source Content Mananagement System

*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* For further information about OpenCms, please see the
* OpenCms Website: http://www.opencms.com
*
* You should have received a copy of the GNU General Public License
* long with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package com.opencms.workplace;

import com.opencms.file.*;
import com.opencms.core.*;
import com.opencms.util.*;
import com.opencms.template.*;
import java.util.*;
import java.io.*;
import javax.servlet.http.*;

/**
 * Template class for displaying OpenCms workplace admin project screens.
 * <P>
 *
 * @author Andreas Schouten
 * @author Michael Emmerich
 * @author Mario Stanke
 * @version $Revision: 1.47 $ $Date: 2001/05/15 19:29:05 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 */

public class CmsAdminProjectNew extends CmsWorkplaceDefault implements I_CmsConstants {


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
    private static String C_NEWRESOURCES = "ALLRES";

    private static String C_PROJECTNEW_THREAD = "project_new_thread";

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
     * Check if this resource should is writeable.
     * @param cms The CmsObject
     * @param res The resource to be checked.
     * @return True or false.
     * @exception CmsException if something goes wrong.
     */

    private boolean checkWriteable(CmsObject cms, String resPath) {
        boolean access = false;
        int accessflags;
        try {
            CmsResource res = cms.readFolder(resPath);
            accessflags = res.getAccessFlags();
            boolean groupAccess = false;
            Enumeration allGroups = cms.getGroupsOfUser(cms.getRequestContext().currentUser().getName()).elements();
            while((!groupAccess) && allGroups.hasMoreElements()) {
                groupAccess = cms.readGroup(res).equals((CmsGroup)allGroups.nextElement());
            }
            if(((accessflags & C_ACCESS_PUBLIC_WRITE) > 0)
                    || (cms.getRequestContext().isAdmin())
                    || (cms.readOwner(res).equals(cms.getRequestContext().currentUser())
                    && (accessflags & C_ACCESS_OWNER_WRITE) > 0)
                    || (groupAccess && (accessflags & C_ACCESS_GROUP_WRITE) > 0)) {
                access = true;
            }
        }
        catch(CmsException e) {
            access = false;
        }
        return access;
    }

    /**
     * Gets the content of a defined section in a given template file and its subtemplates
     * with the given parameters.
     *
     * @see getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters)
     * @param cms CmsObject Object for accessing system resources.
     * @param templateFile Filename of the template file.
     * @param elementName Element name of this template in our parent template.
     * @param parameters Hashtable with all template class parameters.
     * @param templateSelector template section that should be processed.
     */

    public byte[] getContent(CmsObject cms, String templateFile, String elementName,
            Hashtable parameters, String templateSelector) throws CmsException {
        if(C_DEBUG && (A_OpenCms.isLogging() && I_CmsLogChannels.C_PREPROCESSOR_IS_LOGGING)) {
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName()
                    + "getting content of element " + ((elementName == null) ? "<root>" : elementName));
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "template file is: "
                    + templateFile);
            A_OpenCms.log(C_OPENCMS_DEBUG, this.getClassName() + "selected template section is: "
                    + ((templateSelector == null) ? "<default>" : templateSelector));
        }
        I_CmsSession session = cms.getRequestContext().getSession(true);
        CmsRequestContext reqCont = cms.getRequestContext();
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);

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
            session.removeValue("lasturl");
            session.removeValue("newProjectCallingFrom");
            reqCont.setCurrentProject(cms.onlineProject().getId());
        }
        String newName, newGroup, newDescription, newManagerGroup, newFolder;
        String action = new String();
        action = (String)parameters.get("action");
        CmsXmlTemplateFile xmlTemplateDocument = getOwnTemplateFile(cms, templateFile,
                elementName, parameters, templateSelector);

        //look if we come from the explorer view
        String fileToGo = (String)parameters.get("file");
        if (fileToGo == null){
            fileToGo = (String)session.getValue("newProjectCallingFrom");
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
            xmlTemplateDocument.setData("backButton","../../../action/administration_content_top.html?sender=/system/workplace/administration/project/");
            xmlTemplateDocument.setData("myUrl","index.html");
            xmlTemplateDocument.setData("dontDoIt", "");
            xmlTemplateDocument.setData("doThis","");
        }

        xmlTemplateDocument.setData("onlineId", "" + cms.onlineProject().getId());
        newGroup = (String)parameters.get(C_PROJECTNEW_GROUP);
        newDescription = (String)parameters.get(C_PROJECTNEW_DESCRIPTION);
        newManagerGroup = (String)parameters.get(C_PROJECTNEW_MANAGERGROUP);
        String allResources = (String)parameters.get(C_NEWRESOURCES);

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

        // first we look if the thread is allready running
        if((action != null) && ("working".equals(action))) {

            // still working?
            Thread doProjectNew = (Thread)session.getValue(C_PROJECTNEW_THREAD);
            if(doProjectNew.isAlive()) {
                String time = (String)parameters.get("time");
                int wert = Integer.parseInt(time);
                wert += 20;
                xmlTemplateDocument.setData("time", "" + wert);
                return startProcessing(cms, xmlTemplateDocument, elementName, parameters,
                        "wait");
            }
            else {

                // thread has come to an end, was there an error?
                String errordetails = (String)session.getValue(C_SESSION_THREAD_ERROR);
                if(errordetails == null) {

                    // project ready; clear the session
                    session.removeValue(C_NEWNAME);
                    session.removeValue(C_NEWGROUP);
                    session.removeValue(C_NEWDESCRIPTION);
                    session.removeValue(C_NEWMANAGERGROUP);
                    session.removeValue(C_NEWFOLDER);
                    session.removeValue("lasturl");
                    session.removeValue("newProjectCallingFrom");
                    return startProcessing(cms, xmlTemplateDocument, elementName,
                            parameters, "done");
                }
                else {
                    // get errorpage:
                    xmlTemplateDocument.setData(C_NEWNAME, newName);
                    xmlTemplateDocument.setData(C_NEWDESCRIPTION, newDescription);
                    xmlTemplateDocument.setData("details", errordetails);
                    session.removeValue(C_SESSION_THREAD_ERROR);
                    return startProcessing(cms, xmlTemplateDocument, elementName,
                            parameters, "errornewproject"+errorTemplateAddOn);
                }
            }
        }
        if(parameters.get("submitform") != null) {

            // the form has just been submitted, store the data in the session
            session.putValue(C_NEWNAME, newName);
            session.putValue(C_NEWGROUP, newGroup);
            session.putValue(C_NEWDESCRIPTION, newDescription);
            session.putValue(C_NEWMANAGERGROUP, newManagerGroup);
            if(newName.equals("") || newGroup.equals("") || newManagerGroup.equals("")
                    || allResources.equals("")) {
                templateSelector = "datamissing"+errorTemplateAddOn;
            }
            else {
                session.putValue(C_NEWRESOURCES, allResources);

                // all the required data has been entered, display 'Please wait'
                //templateSelector = "wait";
                action = "start";
            }
        }

        // is the wait-page showing?
        if("start".equals(action)) {

            // YES: get the stored data
            newName = (String)session.getValue(C_NEWNAME);
            newGroup = (String)session.getValue(C_NEWGROUP);
            newDescription = (String)session.getValue(C_NEWDESCRIPTION);
            newManagerGroup = (String)session.getValue(C_NEWMANAGERGROUP);
            allResources = (String)session.getValue(C_NEWRESOURCES);

            // create new Project
            try {
                // append the /content/bodys/, /pics/ and /download/ path to the list of all resources
                String picspath = getConfigFile(cms).getPicGalleryPath();
                String downloadpath = getConfigFile(cms).getDownGalleryPath();
                allResources = allResources + C_CONTENTPATH + ";" + picspath + ";"
                        + downloadpath;
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

                // finally create the project
                CmsProject project = cms.createProject(newName, newDescription, newGroup,
                        newManagerGroup);
                // change the current project
                reqCont.setCurrentProject(project.getId());

                // start the thread for: copy the resources to the project
                // first clear the session entry if necessary
                if(session.getValue(C_SESSION_THREAD_ERROR) != null) {
                    session.removeValue(C_SESSION_THREAD_ERROR);
                }
                Thread doProjectNew = new CmsAdminNewProjectThread(cms, folders, session);
                doProjectNew.start();
                session.putValue(C_PROJECTNEW_THREAD, doProjectNew);
                xmlTemplateDocument.setData("time", "10");
                templateSelector = "wait";
            }catch(CmsException exc) {
                xmlTemplateDocument.setData("details", Utils.getStackTrace(exc));
                templateSelector = "errornewproject"+errorTemplateAddOn;
            }
        }

        // after an error the form data is retrieved and filled into the template
        xmlTemplateDocument.setData(C_NEWNAME, newName);
        xmlTemplateDocument.setData(C_NEWDESCRIPTION, newDescription);

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
     * @exception CmsException
     */

    public Integer getGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        // get all groups
        Vector groups = cms.getGroups();
        int retValue = -1;
        String defaultGroup = C_GROUP_USERS;
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
     * @exception CmsException
     */

    public Integer getManagerGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        // get all groups
        Vector groups = cms.getGroups();
        int retValue = -1;
        String defaultGroup = C_GROUP_PROJECTLEADER;
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
