/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsAdminSyncProperties.java,v $
* Date   : $Date: 2003/09/25 14:38:59 $
* Version: $Revision: 1.26 $
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

import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsSession;
import com.opencms.file.CmsObject;
import com.opencms.file.CmsProject;
import com.opencms.file.CmsRegistry;
import com.opencms.file.CmsRequestContext;
import com.opencms.file.CmsResource;
import com.opencms.template.CmsXmlTemplateFile;

import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace administration synchronisation properties.
 *
 * Creation date: ()
 * @author Edna Falkenhan
 */
public class CmsAdminSyncProperties extends CmsWorkplaceDefault {

    private final String C_STEP = "step";
    private final String C_SYNCPROJECT = "syncproject";
    private final String C_SYNCPATH = "syncpath";
    private final String C_SYNCRESOURCES = "syncresources";
    private final String C_ADDFOLDER = "addfolder";
    private final String C_CURPROJECT = "currentProject";

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
    public byte[] getContent(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) throws CmsException {
        if(OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }

        CmsXmlTemplateFile templateDocument = getOwnTemplateFile(cms, templateFile, elementName, parameters, templateSelector);
        CmsRegistry reg = cms.getRegistry();
        CmsRequestContext reqCont = cms.getRequestContext();
        I_CmsSession session = reqCont.getSession(true);
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);

        String projectName = new String();
        String syncPath = new String();
        String allResources = new String();
        Vector folders = new Vector();
        String projectId = new String();
        int numRes = 0;

        // clear session values on first load
        String step = (String)parameters.get(C_STEP);
        projectId = (String)parameters.get(C_SYNCPROJECT);
        syncPath = (String)parameters.get(C_SYNCPATH);
        allResources = (String)parameters.get(C_SYNCRESOURCES);

        if(step == null) {
            // if the dialog was opened the first time remove the session values
            // and get the id of the current project
            if(session.getValue(C_STEP) == null){
                // remove all session values
                session.removeValue(C_SYNCPROJECT);
                session.removeValue(C_SYNCPATH);
                session.removeValue(C_SYNCRESOURCES);
                session.removeValue(C_ADDFOLDER);
                session.removeValue("lasturl");
                // get the id of the current project when the dialog is opened.
                // when changing the project in the dialog the currentProject is set
                // because of the folder-tree that is used to choose resources.
                // when leaving the dialog the currentProject is reset to the original project.
                session.putValue(C_CURPROJECT, reqCont.currentProject().getId() + "");
                session.putValue(C_STEP, "nextstep");
            }
        } else {
            if("OK".equalsIgnoreCase(step)) {
                projectId = (String)parameters.get(C_SYNCPROJECT);
                syncPath = (String)parameters.get(C_SYNCPATH);
                allResources = (String)parameters.get(C_SYNCRESOURCES);
                // the form has just been submitted, store the data in the session
                if(((projectId == null) || projectId.equals("")) ||
                    ((syncPath == null) || syncPath.equals("")) ||
                    ((allResources == null) || allResources.equals(""))) {
                    templateSelector = "datamissing";
                } else {
                    // all the required data has been entered
                    session.putValue(C_SYNCPROJECT, projectId);
                    session.putValue(C_SYNCPATH, syncPath);
                    session.putValue(C_SYNCRESOURCES, allResources);
                    // 'allResources' has the "form res1;res2;...resk;"
                    // this is because the simpler 'getParameterValues' method doesn't work with Silverstream
                    folders = parseResources(allResources);
                    numRes = folders.size();
                    for(int i = 0;i < numRes;i++) {
                        // modify the foldername if nescessary (the root folder is always given
                        // as a nice name)
                        if(lang.getLanguageValue("title.rootfolder").equals(folders.elementAt(i))) {
                            folders.setElementAt("/", i);
                        }
                    }
                    checkRedundancies(folders);
                    numRes = folders.size(); // could have been changed
                    // check if all the resources are writeable
                    // if not, return a message
                    Vector notWriteable = new Vector();
                    for(int i = numRes - 1;i >= 0;i--) {
                        String theFolder = (String)folders.elementAt(i);
                        if(!checkWriteable(cms, theFolder, Integer.parseInt(projectId))) {
                            notWriteable.addElement(theFolder);
                            templateSelector = "errorsyncproperties";
                        }
                    }
                    if("errorsyncproperties".equals(templateSelector)){
                        // at least one of the choosen folders was not writeable
                        templateDocument.setData("details", "The following folders were not writeable:"
                                + notWriteable.toString());
                    }
                }
                if(templateSelector == null || "".equals(templateSelector)){
                    // create the Hashtable for the resources
                    Hashtable hashResources = new Hashtable();
                    numRes = folders.size();
                    for(int i = 0;i < numRes;i++) {
                        String key = C_SYNCHRONISATION_RESOURCETAG+(i+1);
                        String value = (String)folders.elementAt(i);
                        hashResources.put(key, value);
                    }
                    // get the project name
                    CmsProject theProject = cms.readProject(Integer.parseInt(projectId));
                    projectName = theProject.getName();
                    // now update the registry
                    reg.setSystemValue(C_SYNCHRONISATION_PROJECT, projectName);
                    reg.setSystemValue(C_SYNCHRONISATION_PATH, syncPath);
                    reg.setSystemValues(C_SYNCHRONISATION_RESOURCE, hashResources);
                    templateSelector = "done";
                    // if the currentProject was changed in the dialog reset it
                    // to the original currentProject
                    int curProjectId = Integer.parseInt((String)session.getValue(C_CURPROJECT));
                    if (curProjectId != reqCont.currentProject().getId()){
                        reqCont.setCurrentProject(curProjectId);
                    }
                    // remove the values from the session
                    session.removeValue(C_CURPROJECT);
                    session.removeValue(C_STEP);
                }
            } else if("fromerrorpage".equals(step)) {
                // after an error fill in the data from the session into the template
                templateDocument.setData(C_SYNCPROJECT, (String)session.getValue(C_SYNCPROJECT));
                templateDocument.setData(C_SYNCPATH, (String)session.getValue(C_SYNCPATH));
                templateDocument.setData(C_ADDFOLDER, "");
                templateDocument.setData(C_SYNCRESOURCES, (String)session.getValue(C_SYNCRESOURCES));
                templateSelector = "";
            } else if("cancel".equals(step)){
                // if the currentProject was changed in the dialog
                // reset to the original currentProject
                int curProjectId = Integer.parseInt((String)session.getValue(C_CURPROJECT));
                if (curProjectId != reqCont.currentProject().getId()){
                    reqCont.setCurrentProject(curProjectId);
                }
                // remove the values from the session
                session.removeValue(C_CURPROJECT);
                session.removeValue(C_STEP);
                templateSelector = "done";
            }
        }
        // if there are still values in the session (like after an error), use them
        if((projectId == null) || ("".equals(projectId))) {
            projectId = (String)session.getValue(C_SYNCPROJECT);
        }
        if((syncPath == null) || ("".equals(syncPath))) {
            syncPath = (String)session.getValue(C_SYNCPATH);
        }
        if((allResources == null) || ("".equals(allResources))) {
            allResources = (String)session.getValue(C_SYNCRESOURCES);
        }
        // try to read the values from the registry file and check if they are
        // available in the VFS
        if((projectId == null) || ("".equals(projectId))) {
            projectName = reg.getSystemValue(C_SYNCHRONISATION_PROJECT);
            int countAccessible = 0;
            if((projectName != null) && (!"".equals(projectName))){
                // does this is an accessible project or
                // if there is more than one project with this name
                Vector allProjects = cms.getAllAccessibleProjects();
                for (int i = 0; i < allProjects.size(); i++){
                    CmsProject nextProject = (CmsProject)allProjects.elementAt(i);
                    if(projectName.equals(nextProject.getName())){
                        countAccessible++;
                        projectId = nextProject.getId()+"";
                    }
                }
                if ((countAccessible == 0) || (countAccessible > 1)){
                    projectId = "";
                }
            } else {
                projectId = "";
            }
        }

        if((syncPath == null) || ("".equals(syncPath))) {
            syncPath = reg.getSystemValue(C_SYNCHRONISATION_PATH);
            if(syncPath == null){
                syncPath = "";
            }
        }
        if((allResources == null) || ("".equals(allResources))) {
            allResources = "";
            if (!((projectId == null) || ("".equals(projectId)))){
                Hashtable resources = reg.getSystemValues(C_SYNCHRONISATION_RESOURCE);
                numRes = resources.size();
                if (numRes > 0){
                    allResources = new String();
                    for (int i=1; i < numRes+1; i++) {
                        String path = (String)resources.get(C_SYNCHRONISATION_RESOURCETAG+i);
                        // try to read this resource from the project
                        try{
                            cms.readFileHeader(path, Integer.parseInt(projectId), false);
                            allResources = allResources + path + ";";
                        } catch (CmsException exc){
                        }
                    }
                    // remove the last semikolon
                    if (allResources.endsWith(";")){
                        allResources = allResources.substring(0,allResources.lastIndexOf(";"));
                    }
                }
            }
        }

        if(!"done".equals(templateSelector)){
            // Check if the user requested a project change in the dialog
            // and set the currentProject
            if(projectId != null && !("".equals(projectId))) {
                if(!(Integer.parseInt(projectId) == reqCont.currentProject().getId())) {
                    reqCont.setCurrentProject(Integer.parseInt(projectId));
                }
            }
        }

        templateDocument.setData(C_SYNCPROJECT, projectId);
        templateDocument.setData(C_SYNCRESOURCES, allResources);
        templateDocument.setData(C_SYNCPATH, syncPath);
        session.putValue(C_SYNCPROJECT, projectId);
        session.putValue(C_SYNCPATH, syncPath);
        session.putValue(C_SYNCRESOURCES, allResources);

        // Now load the template file and start the processing
        return startProcessing(cms, templateDocument, elementName, parameters, templateSelector);
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
    public boolean isCacheable(CmsObject cms, String templateFile, String elementName, Hashtable parameters, String templateSelector) {
        return false;
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

    public Integer getProjects(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values, Hashtable parameters) throws CmsException {
        // get all projects
        Vector projects = cms.getAllAccessibleProjects();
        int retValue = -1;
        CmsProject curProject = cms.getRequestContext().currentProject();
        String defaultProject = curProject.getId()+"";
        
        I_CmsSession session = cms.getRequestContext().getSession(true);
        String enteredProject = (String)session.getValue(C_SYNCPROJECT);        
        if(enteredProject != null && !"".equals(enteredProject)) {
            // if an error has occurred before, take the previous entry of the user
            defaultProject = enteredProject;
        }

        // fill the names and values
        int n = 0;
        for(int z = 0;z < projects.size();z++) {
            CmsProject loopProject = (CmsProject)projects.elementAt(z);
            if(!loopProject.isOnlineProject()) {
                String loopProjectName = loopProject.getName();
                String loopProjectId = loopProject.getId() + "";
                
                if(defaultProject.equals(loopProjectId)) {
                    retValue = n;
                    cms.getRequestContext().setCurrentProject(Integer.parseInt(loopProjectId));
                }
                
                names.addElement(loopProjectName);
                values.addElement(loopProjectId);
                n++;
            }
        }
        
       return new Integer(retValue);
    }

    /**
     * Gets the resources.
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

    public Integer getResources(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        I_CmsSession session = cms.getRequestContext().getSession(true);
        String enteredResources = (String)session.getValue(C_SYNCRESOURCES);
        Vector resources = parseResources(enteredResources);
        // fill the names and values
        for(int z = 0;z < resources.size();z++) {
            String resourceName = (String)resources.elementAt(z);
            names.addElement(resourceName);
            values.addElement(resourceName);
        }
        return new Integer(-1);
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
     * @throws CmsException if something goes wrong.
     */
    private boolean checkWriteable(CmsObject cms, String resPath, int projectId)  throws CmsException {
		CmsProject theProject = cms.readProject(projectId);
		CmsResource res = cms.readFileHeader(resPath, projectId, false);
        return cms.hasPermissions(theProject, res, C_WRITE_ACCESS);
    }
}