/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/workplace/Attic/CmsAdminGroups.java,v $
* Date   : $Date: 2005/05/20 14:32:31 $
* Version: $Revision: 1.3 $
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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsRequestContext;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsLegacyException;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace admin group screens.
 * <P>
 *
 * @author Mario Stanke
 * @version $Revision: 1.3 $ $Date: 2005/05/20 14:32:31 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsAdminGroups extends CmsWorkplaceDefault {


    /**
     * String constant which is submitted in the select box 'supergroup'
     */

    // Could cause a problem if a real groupname happens to be "none_selected"
    final static String C_NO_SUPERGROUP_SELECTED = "none_selected";

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
        if(OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Getting content of element " + ((elementName == null) ? "<root>" : elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: " + ((templateSelector == null) ? "<default>" : templateSelector));
        }
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        CmsRequestContext reqCont = cms.getRequestContext();
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        boolean groupYetChanged = true;
        boolean groupYetEstablished = true;

        // find out which template (=perspective) should be used
        String perspective = (String)parameters.get("perspective");
        if(perspective != null && perspective.equals("group")) {
            session.removeValue("ERROR");
            if(CmsXmlTemplateLoader.getRequest(reqCont).getParameter("CHANGE") != null) {

                // change data of selected user
                perspective = "changegroup";
                groupYetChanged = false;
            }
            else {
                if(parameters.get("DELETE") != null) {

                    // delete the selected user
                    perspective = "deletegroup";
                }
                else {
                    if(parameters.get("NEW") != null) {

                        // establish new group
                        perspective = "newgroup";
                        groupYetEstablished = false;
                    }
                }
            }
        }
        if(perspective == null) {

            // display the first template, which lets you chose the action
            perspective = new String("group");
        }
        if(perspective.equals("newgroup") || perspective.equals("changegroup")) {

            // first the common part of the two actions:
            // read the parameters like group name, description, ...
            String groupname, description, supergroup;
            boolean projectManager, projectCoWorker, role;
            if(session.getValue("ERROR") == null) {
                groupname = (String)parameters.get("GROUPNAME");
                description = (String)parameters.get("GROUPDESC");
                supergroup = (String)parameters.get("SUPERGROUP");
                projectManager = (parameters.get("PROJECTMANAGER") != null);
                projectCoWorker = (parameters.get("PROJECTCOWORKER") != null);
                role = (parameters.get("ROLE") != null);
            }
            else {

                // an error has occurred before, retrieve the form data from the session
                groupname = (String)session.getValue("GROUPNAME");
                description = (String)session.getValue("GROUPDESC");
                supergroup = (String)session.getValue("SUPERGROUP");
                projectManager = (session.getValue("PROJECTMANAGER") != null);
                projectCoWorker = (session.getValue("PROJECTCOWORKER") != null);
                role = (session.getValue("ROLE") != null);

                // remove the data from parameters
                parameters.remove("ADD");
                parameters.remove("REMOVE");
                parameters.remove("OK");
                session.removeValue("ERROR");
            }
            if(groupname == null) {
                groupname = "";
            }
            if(description == null) {
                description = "";
            }
            if(supergroup == null) {
                supergroup = "";
            }
            session.putValue("SUPERGROUP", supergroup);

            // vectors of Strings that hold the selected and not selected Users
            Vector selectedUsers = (Vector)session.getValue("selectedUsers");
            Vector notSelectedUsers = (Vector)session.getValue("notSelectedUsers");
            if(perspective.equals("newgroup")) {

                // input is the form for establishing a new group
                templateSelector = "newgroup";
                if(!groupYetEstablished) {

                    // first time the form is visited
                    groupname = "";
                    selectedUsers = new Vector();
                    notSelectedUsers = new Vector();
                    List users = cms.getUsers();
                    for(int z = 0;z < users.size();z++) {
                        notSelectedUsers.addElement(((CmsUser)users.get(z)).getName());
                    }
                    session.putValue("selectedUsers", selectedUsers);
                    session.putValue("notSelectedUsers", notSelectedUsers);
                }
                if(parameters.get("ADD") != null) {

                    // add a new group to selectedGroups
                    String username = (String)parameters.get("NOTSELECTEDUSERS");
                    if(username != null) {
                        selectedUsers.addElement(username);
                        notSelectedUsers.removeElement(username);
                    }
                    session.putValue("selectedUsers", selectedUsers);
                    session.putValue("notSelectedUsers", notSelectedUsers);
                }
                else {
                    if(parameters.get("REMOVE") != null) {

                        // delete a new group from selectedGroups
                        // and move it to notSelectedGroups
                        String username = (String)parameters.get("SELECTEDUSERS");
                        if(username != null) {
                            notSelectedUsers.addElement(username);
                            selectedUsers.removeElement(username);
                        }
                        session.putValue("selectedUsers", selectedUsers);
                        session.putValue("notSelectedUsers", notSelectedUsers);
                    }
                    else {
                        if(parameters.get("OK") != null) {

                            // form submitted, try to establish new group
                            try {
                                if(groupname == null || groupname.equals("")) {
                                    throw new CmsLegacyException("no groupname", CmsLegacyException.C_NO_GROUP);
                                }
                                if(C_NO_SUPERGROUP_SELECTED.equals(supergroup)) {
                                    supergroup = ""; // no supergroup
                                }
                                CmsGroup newGroup = cms.createGroup(groupname, description, 0, supergroup);
                                newGroup.setProjectManager(projectManager);
                                newGroup.setProjectCoWorker(projectCoWorker);
                                newGroup.setRole(role);
                                cms.writeGroup(newGroup);
                                for(int z = 0;z < selectedUsers.size();z++) {
                                    cms.addUserToGroup((String)selectedUsers.elementAt(z), groupname);
                                }
                                session.removeValue("selectedUsers");
                                session.removeValue("notSelectedUsers");
                                session.removeValue("SUPERGROUP");
                                session.removeValue("PROJECTMANAGER");
                                session.removeValue("PROJECTCOWORKER");
                                session.removeValue("ROLE");
                                session.removeValue("ERROR");
                                templateSelector = ""; //successful
                            }
                            catch(CmsException e) {

                                // save the form data in the session, so it can be displayed again later
                                session.putValue("ERROR", new String("yes")); // remeber that an error has occurred
                                session.putValue("GROUPNAME", groupname);
                                session.putValue("GROUPDESC", description);
                                session.putValue("SUPERGROUP", supergroup);
                                if(projectManager) {
                                    session.putValue("PROJECTMANAGER", "yes");
                                }
                                else {
                                    session.removeValue("PROJECTMANAGER");
                                }
                                if(projectCoWorker) {
                                    session.putValue("PROJECTCOWORKER", "yes");
                                }
                                else {
                                    session.removeValue("PROJECTCOWORKER");
                                }
                                if(role) {
                                    session.putValue("ROLE", "yes");
                                }
                                else {
                                    session.removeValue("ROLE");
                                }
                                if ((e instanceof CmsLegacyException) && (((CmsLegacyException)e).getType() == CmsLegacyException.C_NO_GROUP) && e.getMessage().equals("no groupname")) {
                                    templateSelector = "errordatamissing1";
                                }
                                else {
                                    throw e; // hand the exception down
                                }
                            }
                        }
                    }
                }
            }
            else {

                // input is the form for changing the group data
                templateSelector = "changegroup";
                if(!groupYetChanged) {

                    // form visited for the first time, not yet changed

                    // read the data from the group object
                    CmsGroup theGroup = cms.readGroup(groupname);
                    if(theGroup == null) {
                        throw new CmsLegacyException("user does not exist");
                    }
                    projectManager = theGroup.getProjectmanager();
                    projectCoWorker = theGroup.getProjectCoWorker();
                    role = theGroup.getRole();
                    description = theGroup.getDescription();
                    CmsGroup parent = cms.getParent(groupname);
                    if(parent != null) {
                        supergroup = cms.getParent(groupname).getName();
                    }
                    else {
                        supergroup = "";
                    }
                    List users = cms.getUsersOfGroup(groupname);
                    if(users != null) {
                        selectedUsers = new Vector();
                        for(int z = 0;z < users.size();z++) {
                            selectedUsers.addElement(((CmsUser)users.get(z)).getName());
                        }
                    }
                    users = cms.getUsers();
                    if(users != null) {
                        notSelectedUsers = new Vector();
                        for(int z = 0;z < users.size();z++) {
                            String name = ((CmsUser)users.get(z)).getName();
                            if(!selectedUsers.contains(name)) {
                                notSelectedUsers.addElement(name);
                            }
                        }
                    }
                }
                else {
                    if(parameters.get("ADD") != null) {

                        // add a new user to selectedUsers
                        String username = (String)parameters.get("NOTSELECTEDUSERS");
                        if(username != null) {
                            selectedUsers.addElement(username);
                            notSelectedUsers.removeElement(username);
                        }
                    }
                    else {
                        if(parameters.get("REMOVE") != null) {

                            // delete a group from selectedUsers
                            // and move it to notSelectedUsers
                            String username = (String)parameters.get("SELECTEDUSERS");
                            if(username != null) {
                                notSelectedUsers.addElement(username);
                                selectedUsers.removeElement(username);
                            }
                        }
                        else {
                            if(parameters.get("OK") != null) {

                                // form submitted, try to change the group data
                                try {
                                    CmsGroup theGroup = cms.readGroup(groupname);
                                    if("".equals(supergroup) || supergroup.equals(C_NO_SUPERGROUP_SELECTED)) {
                                        cms.setParentGroup(groupname, null);
                                    }
                                    else {
                                        cms.setParentGroup(groupname, supergroup);
                                    }
                                    theGroup = cms.readGroup(groupname);
                                    theGroup.setDescription(description);
                                    theGroup.setProjectManager(projectManager);
                                    theGroup.setProjectCoWorker(projectCoWorker);
                                    theGroup.setRole(role);
                                    cms.writeGroup(theGroup);
                                    theGroup = cms.readGroup(groupname);

                                    // now change the list of users of this group but take into account that
                                    // the default group of a user can't be removed
                                    List allUsers = cms.getUsersOfGroup(groupname);
                                    boolean defaultProblem = false;
                                    Vector falseUsers = new Vector();
                                    for(int z = 0;z < allUsers.size();z++) {
                                        String theUserName = ((CmsUser)allUsers.get(z)).getName();
                                        if(!selectedUsers.contains(theUserName)) {
                                            cms.removeUserFromGroup(theUserName, groupname);
                                        }
                                    }
                                    for(int z = 0;z < selectedUsers.size();z++) {
                                        cms.addUserToGroup((String)selectedUsers.elementAt(z), groupname);
                                    }
                                    cms.writeGroup(theGroup);
                                    session.removeValue("selectedUsers");
                                    session.removeValue("notSelectedUsers");
                                    session.removeValue("PROJECTMANAGER");
                                    session.removeValue("PROJECTCOWORKER");
                                    session.removeValue("ROLE");
                                    session.removeValue("ERROR");
                                    if(defaultProblem) {
                                        xmlTemplateDocument.setData("RMDEFAULTDETAIL", "The following users which were to be removed had " + groupname + " as default group: " + falseUsers);
                                        templateSelector = "errorremovedefault";
                                    }
                                    else {
                                        templateSelector = ""; //successful
                                    }
                                }
                                catch(CmsException e) {

                                    // remeber that an error has occurred
                                    session.putValue("ERROR", new String("yes"));
                                    session.putValue("GROUPDESC", description);
                                    session.putValue("SUPERGROUP", supergroup);
                                    if ((e instanceof CmsLegacyException) && (((CmsLegacyException)e).getType() == CmsLegacyException.C_NO_GROUP)) {
                                        templateSelector = "errornogroup2";
                                    }
                                    else {
                                        if ((e instanceof CmsLegacyException) && (((CmsLegacyException)e).getType() == CmsLegacyException.C_NO_USER  && e.getMessage().equals("user data missing"))) {
                                            templateSelector = "errordatamissing2";
                                        }
                                        else {
                                            throw e; // hand the exception down
                                        }
                                    }
                                } // catch
                            } // else if 'OK'
                        }
                    }
                } // groupYetChanged
                session.putValue("selectedUsers", selectedUsers);
                session.putValue("notSelectedUsers", notSelectedUsers);
                session.putValue("SUPERGROUP", supergroup);
            }

            // again common part for 'newgroup' and 'changegroup':
            // set the variables for display in the document
            if(groupname == null) {
                groupname = "";
            }
            if(description == null) {
                description = "";
            }
            if(supergroup == null) {
                supergroup = "";
            }
            xmlTemplateDocument.setData("GROUPNAME", groupname);
            xmlTemplateDocument.setData("GROUPDESC", description);
            xmlTemplateDocument.setData("SUPERGROUP", supergroup);
            xmlTemplateDocument.setData("PCWCHECKED", projectCoWorker ? "checked" : "");
            xmlTemplateDocument.setData("PMCHECKED", projectManager ? "checked" : "");
            xmlTemplateDocument.setData("PRCHECKED", role ? "checked" : "");
        } // belongs to: 'if perspective is newgroup or changegroup'
        else {
            if(perspective.equals("deletegroup")) {
                String groupname = (String)parameters.get("GROUPNAME");
                xmlTemplateDocument.setData("GROUPNAME", groupname);
                templateSelector = "RUsureDelete";
            }
            else {
                if(perspective.equals("reallydeletegroup")) {

                    // deleting the group
                    try {
                        String groupname = (String)parameters.get("GROUPNAME");
                        cms.deleteGroup(groupname);
                        templateSelector = "";
                    }
                    catch(Exception e) {

                        // groupname == null or delete failed
                        xmlTemplateDocument.setData("DELETEDETAILS", CmsException.getStackTraceAsString(e));
                        templateSelector = "deleteerror";
                    }
                }
            }
        }

        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

    /**
     * Gets all groups for a select box
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
        List groups = cms.getGroups();
        int retValue = 0;

        // fill the names and values
        for(int z = 0;z < groups.size();z++) {
            String name = ((CmsGroup)groups.get(z)).getName();
            names.addElement(name);
            values.addElement(name);
        }
        return new Integer(retValue);
    }

    /**
     * Gets all users, that have not yet been selected for the group
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

    public Integer getNotSelectedUsers(CmsObject cms, CmsXmlLanguageFile lang,
            Vector names, Vector values, Hashtable parameters) throws CmsException {
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        Vector notSelectedUsers = (Vector)session.getValue("notSelectedUsers");
        if(notSelectedUsers != null) {
            for(int z = 0;z < notSelectedUsers.size();z++) {
                String name = (String)notSelectedUsers.elementAt(z);
                names.addElement(name);
                values.addElement(name);
            }
        }
        return new Integer(-1); // nothing preselected
    }

    /**
     * Gets all users that have been selected to be in the group
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters <em>(not used here)</em>.
     * @return Index representing the default Group of the user
     * @throws CmsException
     */

    public Integer getSelectedUsers(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        Vector selectedUsers = (Vector)session.getValue("selectedUsers");
        if(selectedUsers != null) {
            for(int z = 0;z < selectedUsers.size();z++) {
                String name = (String)selectedUsers.elementAt(z);
                names.addElement(name);
                values.addElement(name);
            }
        }
        else {
            selectedUsers = new Vector();
        }
        return new Integer(-1);
    }

    /**
     * Gets all supergroups of the actual group (in session or in form data) for a selectbox
     * <P>
     * The given vectors <code>names</code> and <code>values</code> will
     * be filled with the appropriate information to be used for building
     * a select box.
     *
     * @param cms CmsObject Object for accessing system resources.
     * @param names Vector to be filled with the appropriate values in this method.
     * @param values Vector to be filled with the appropriate values in this method.
     * @param parameters Hashtable containing all user parameters.
     * @return Index representing the current value in the vectors.
     * @throws CmsException
     */

    public Integer getSuperGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        int retValue = -1;
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String actualGroup = (String)session.getValue("GROUPNAME");
        String temp = (String)parameters.get("GROUPNAME");
        if(temp != null) {
            actualGroup = temp;
        }
        if(parameters.get("NEW") != null) {
            // user pressed the NEW-button so we dont need the actual Group
            actualGroup = "";
        }
        String supergroup = (String)session.getValue("SUPERGROUP");
        temp = (String)parameters.get("SUPERGROUP");
        if(temp != null) {
            supergroup = temp;
        }
        if(supergroup == null) {
            supergroup = "";
        }
        names.addElement(lang.getLanguageValue("input.none"));
        values.addElement(C_NO_SUPERGROUP_SELECTED);
        List groups = cms.getGroups();
        int selectedGroup = 0;
        for(int z = 0;z < groups.size();z++) {
            String name = ((CmsGroup)groups.get(z)).getName();
            if(name.equals(supergroup)) {
                retValue = selectedGroup;
            }
            if(!name.equals(actualGroup)) {
                names.addElement(name);
                values.addElement(name);
                selectedGroup++;
            }
        }
        return new Integer(retValue + 1);
    }
    
    /**
     * Determines if the group icon is shown in the administration view depending on the property value of the key "workplace.administration.showusergroupicon".<p>
     * 
     * @param cms the CmsObject
     * @param lang the workplace language file
     * @param parameters the parameters
     * @return Boolean to determine if group icon is shown in the administration view
     */
    public Boolean isVisible(CmsObject cms, CmsXmlLanguageFile lang, Hashtable parameters) {
        return new Boolean(OpenCms.getWorkplaceManager().showUserGroupIcon());
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
