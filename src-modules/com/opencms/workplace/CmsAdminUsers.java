/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/workplace/Attic/CmsAdminUsers.java,v $
* Date   : $Date: 2005/05/19 08:57:22 $
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
import org.opencms.security.CmsSecurityException;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsLegacyException;
import com.opencms.legacy.CmsLegacySecurityException;
import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace admin users screens.
 * <P>
 *
 * @author Mario Stanke
 * @version $Revision: 1.3 $ $Date: 2005/05/19 08:57:22 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsAdminUsers extends CmsWorkplaceDefault {

    /**
     * change the groups of the user
     * <P>
     * the Vector newGroups holds all groups, which theUser will be in afterwards
     * the amount of database access is kept small with this funcion
     * @param cms CmsObject Object for accessing system resources.
     * @param theUser the user whose data will be changed
     * @param newGroups Vector of Strings with the names of the new groups of theUser
     * @throws CmsException
     */

    private void changeGroups(CmsObject cms, CmsUser theUser, Vector newGroups) throws CmsException {
        String username = theUser.getName();
        List oldGroups = cms.getGroupsOfUser(username);
        List oldGroupnames = new Vector();

        cms.writeUser(theUser); // update in the database
        theUser = cms.readUser(username);
        if(oldGroups != null) {
            for(int z = 0;z < oldGroups.size();z++) {
                oldGroupnames.add(((CmsGroup)oldGroups.get(z)).getName());
            }

            // delete the user from the groups which are not in newGroups
            for(int z = 0;z < oldGroupnames.size();z++) {
                String groupname = (String)oldGroupnames.get(z);
                if(!newGroups.contains(groupname)) {
                    try {
                        cms.removeUserFromGroup(username, groupname);
                    }catch(CmsException e) {
                    // can happen when this group has been deleted _indirectly_ before
                    }
                }
            }
        }
        if(newGroups != null) {

            // now add the user to the new groups, which he not yet belongs to
            for(int z = 0;z < newGroups.size();z++) {
                String groupname = (String)newGroups.elementAt(z);
                if(!cms.userInGroup(username, groupname)) {
                    cms.addUserToGroup(username, groupname);
                }
            }
        }
        cms.writeUser(theUser); // update in the database
        theUser = cms.readUser(username);
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

        if(OpenCms.getLog(this).isDebugEnabled() && C_DEBUG) {
            OpenCms.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            OpenCms.getLog(this).debug("Template file is: " + templateFile);
            OpenCms.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        CmsRequestContext reqCont = cms.getRequestContext();
        CmsXmlWpTemplateFile xmlTemplateDocument = new CmsXmlWpTemplateFile(cms, templateFile);
        boolean userYetChanged = true;
        boolean userYetEstablished = true;

        // find out which template (=perspective) should be used
        String perspective = (String)parameters.get("perspective");
        if(perspective != null && perspective.equals("user")) {
            session.removeValue("ERROR");
            if(CmsXmlTemplateLoader.getRequest(reqCont).getParameter("CHANGE") != null) {

                // change data of selected user
                perspective = "changeuser";
                userYetChanged = false;
            }else {
                if(CmsXmlTemplateLoader.getRequest(reqCont).getParameter("DELETE") != null) {

                    // delete the selected user
                    perspective = "deleteuser";
                }else {
                    if(CmsXmlTemplateLoader.getRequest(reqCont).getParameter("NEW") != null) {

                        // establish a new user
                        perspective = "newuser";
                        userYetEstablished = false;
                    }
                }
            }
        }
        if(perspective == null) {

            // display the first template, which lets you chose the action
            perspective = new String("user");
        }
        if(perspective.equals("newuser") || perspective.equals("changeuser")) {

            // first the common part of the two actions:
            // read the common parameters like first name, description, ...
            String firstname, desc, street, pwd, pwd2, user, userLastname, town, zipcode,
                    email, defaultGroup;
            if(session.getValue("ERROR") == null) {
                firstname = (String)parameters.get("USERFIRSTNAME");
                desc = (String)parameters.get("USERDESC");
                street = (String)parameters.get("USERSTREET");
                pwd = (String)parameters.get("PWD");
                pwd2 = (String)parameters.get("PWD2");
                user = (String)parameters.get("USER");
                userLastname = (String)parameters.get("USERNAME");
                town = (String)parameters.get("TOWN");
                zipcode = (String)parameters.get("ZIP");
                email = (String)parameters.get("USEREMAIL");
                defaultGroup = (String)parameters.get("DEFAULTGROUP");
            }else {

                // an error has occurred before, retrieve the form data from the session
                firstname = (String)session.getValue("USERFIRSTNAME");
                desc = (String)session.getValue("USERDESC");
                street = (String)session.getValue("USERSTREET");
                pwd = (String)session.getValue("PWD");
                pwd2 = (String)session.getValue("PWD2");
                user = (String)session.getValue("USER");
                userLastname = (String)session.getValue("USERNAME");
                town = (String)session.getValue("TOWN");
                zipcode = (String)session.getValue("ZIP");
                email = (String)session.getValue("USEREMAIL");
                defaultGroup = (String)session.getValue("DEFAULTGROUP");
                session.removeValue("ERROR");
            }
            if(firstname == null) {
                firstname = "";
            }
            if(desc == null) {
                desc = "";
            }
            if(street == null) {
                street = "";
            }
            if(pwd == null) {
                pwd = "";
            }
            if(pwd2 == null) {
                pwd2 = "";
            }
            if(user == null) {
                user = "";
            }
            if(userLastname == null) {
                userLastname = "";
            }
            if(town == null) {
                town = "";
            }
            if(zipcode == null) {
                zipcode = "";
            }
            if(email == null) {
                email = "";
            }
            if(defaultGroup == null) {
                defaultGroup = "";
            }

            // vectors of Strings that hold the selected and not selected Groups
            Vector selectedGroups = (Vector)session.getValue("selectedGroups");
            Vector notSelectedGroups = (Vector)session.getValue("notSelectedGroups");
            if(perspective.equals("newuser")) {

                // input is the form for establishing new users
                templateSelector = "newuser";
                if(!userYetEstablished) {

                    // first time the form is visited
                    user = "";
                    selectedGroups = new Vector();
                    notSelectedGroups = new Vector();
                    selectedGroups.addElement(OpenCms.getDefaultUsers().getGroupUsers()); // preselect Users
                    List groups = cms.getGroups();
                    for(int z = 0;z < groups.size();z++) {
                        String aName = ((CmsGroup)groups.get(z)).getName();
                        if(!OpenCms.getDefaultUsers().getGroupUsers().equals(aName)) {
                            notSelectedGroups.addElement(aName);
                        }
                    }
                    session.putValue("selectedGroups", selectedGroups);
                    session.putValue("notSelectedGroups", notSelectedGroups);
                }
                if(CmsXmlTemplateLoader.getRequest(reqCont).getParameter("ADD") != null) {

                    // add a new group to selectedGroups
                    String groupname = (String)parameters.get("notselectgroup");
                    if(groupname != null) {
                        selectedGroups.addElement(groupname);
                        notSelectedGroups.removeElement(groupname);
                    }
                    session.putValue("selectedGroups", selectedGroups);
                    session.putValue("notSelectedGroups", notSelectedGroups);
                }else {
                    if(CmsXmlTemplateLoader.getRequest(reqCont).getParameter("REMOVE") != null) {

                        // delete a new group from selectedGroups
                        // and move it to notSelectedGroups
                        String groupname = (String)parameters.get("selectgroup");
                        if(groupname != null) {
                            notSelectedGroups.addElement(groupname);
                            selectedGroups.removeElement(groupname);
                            if(groupname.equals(defaultGroup)) {
                                defaultGroup = "";
                            }
                        }
                        session.putValue("selectedGroups", selectedGroups);
                        session.putValue("notSelectedGroups", notSelectedGroups);
                    }else {
                        if(CmsXmlTemplateLoader.getRequest(reqCont).getParameter("OK") != null) {

                            // form submitted, try to establish new user
                            try {
                                if(email.equals("") || userLastname.equals("")
                                        || user.equals("")) {
                                    throw new CmsLegacyException("user data missing",
                                        CmsLegacyException.C_NO_USER);
                                }
                                if(!pwd.equals(pwd2)) {
                                    throw new CmsLegacyException("unequal passwords",
                                        CmsLegacyException.C_SECURITY_INVALID_PASSWORD);
                                }
                                // check the password
                                cms.validatePassword(pwd);

                                Hashtable additionalInfo = new Hashtable();

                                // additionalInfo.put(C_ADDITIONAL_INFO_ZIPCODE, zipcode);
                                // additionalInfo.put(C_ADDITIONAL_INFO_TOWN, town);
                                CmsUser newUser = cms.createUser(user, pwd, desc, additionalInfo);
                                newUser.setEmail(email);
                                newUser.setFirstname(firstname);
                                newUser.setLastname(userLastname);
                                newUser.setAddress(street);
                                newUser.setAdditionalInfo(C_ADDITIONAL_INFO_ZIPCODE, zipcode);
                                newUser.setAdditionalInfo(C_ADDITIONAL_INFO_TOWN, town);
                                newUser.setAdditionalInfo(C_ADDITIONAL_INFO_DEFAULTGROUP, defaultGroup);
                                for(int z = 0;z < selectedGroups.size();z++) {
                                    String groupname = (String)selectedGroups.elementAt(z);
                                    cms.addUserToGroup(user, groupname);
                                }
                                cms.writeUser(newUser); // update in the database
                                session.removeValue("selectedGroups");
                                session.removeValue("notSelectedGroups");
                                session.removeValue("ERROR");
                                templateSelector = ""; //successful
                            }catch(CmsException e) {

                                // save the form data in the session, so it can be displayed again later
                                session.putValue("ERROR", new String("yes")); // remeber that an error has occurred
                                session.putValue("USERFIRSTNAME", firstname);
                                session.putValue("USERDESC", desc);
                                session.putValue("USERSTREET", street);
                                session.putValue("PWD", pwd);
                                session.putValue("PWD2", pwd2);
                                session.putValue("USER", user);
                                session.putValue("USERNAME", userLastname);
                                session.putValue("ZIP", zipcode);
                                session.putValue("TOWN", town);
                                session.putValue("USEREMAIL", email);
                                session.putValue("DEFAULTGROUP", defaultGroup);
                                if(e.getType() == CmsLegacySecurityException.C_SECURITY_INVALID_PASSWORD) {
                                    if(e.getMessage().equals("unequal passwords")) {
                                        templateSelector = "passworderror1";
                                    }else {
                                        if(e.getMessage().equals("password too short")) {
                                            templateSelector = "passworderror2";
                                        }else {
                                            xmlTemplateDocument.setData("reasonOfError", e.getMessage());
                                            xmlTemplateDocument.setData("perspective", "newuser");
                                            templateSelector = "passworderror5";
                                        }
                                    }
                                }else {
                                    if(e.getType() == CmsLegacyException.C_NO_GROUP) {
                                        templateSelector = "errornogroup1";
                                    }else {
                                        if(e.getType() == CmsLegacyException.C_NO_USER
                                                && e.getMessage().equals("user data missing")) {
                                            templateSelector = "errordatamissing1";
                                        }else {
                                            // unknown error
                                            xmlTemplateDocument.setData("details", CmsException.getStackTraceAsString(e));
                                            templateSelector = "error";
                                        }
                                    }
                                }
                            } // catch block
                        } // OK
                    }
                }
            }else {

                // input is the form for changing the user data
                templateSelector = "changeuser";
                boolean disabled = false;
                if(!userYetChanged) {

                    // form visited for the first time, not yet changed
                    // read the data from the user object
                    CmsUser theUser = cms.readUser(user);
                    if(theUser == null) {
                        throw new CmsLegacyException("user does not exist");
                    }
                    firstname = theUser.getFirstname();
                    desc = theUser.getDescription();
                    street = theUser.getAddress();
                    userLastname = theUser.getLastname();
                    email = theUser.getEmail();
                    disabled = theUser.getDisabled();
                    zipcode = (String)theUser.getAdditionalInfo(C_ADDITIONAL_INFO_ZIPCODE);
                    town = (String)theUser.getAdditionalInfo(C_ADDITIONAL_INFO_TOWN);
                    defaultGroup = (String)theUser.getAdditionalInfo(C_ADDITIONAL_INFO_DEFAULTGROUP);
                    List groups = cms.getDirectGroupsOfUser(user);
                    if(groups != null) {
                        selectedGroups = new Vector();
                        for(int z = 0;z < groups.size();z++) {
                            selectedGroups.addElement(((CmsGroup)groups.get(z)).getName());
                        }
                    }else {
                        throw new CmsLegacyException(CmsLegacyException.C_NO_GROUP);
                    }
                    groups = cms.getGroups();
                    if(groups != null) {
                        notSelectedGroups = new Vector();
                        for(int z = 0;z < groups.size();z++) {
                            String name = ((CmsGroup)groups.get(z)).getName();
                            if(!selectedGroups.contains(name)) {
                                notSelectedGroups.addElement(name);
                            }
                        }
                    }
                }else {

                    // fetch data from the form
                    if((String)parameters.get("LOCK") != null) {
                        disabled = true;
                    }
                    if(CmsXmlTemplateLoader.getRequest(reqCont).getParameter("ADD") != null) {

                        // add a new group to selectedGroups
                        String groupname = (String)parameters.get("notselectgroup");
                        if(groupname != null) {
                            if(!selectedGroups.contains(groupname)){
                                selectedGroups.addElement(groupname);
                            }
                            notSelectedGroups.removeElement(groupname);
                        }
                    }else {
                        if(CmsXmlTemplateLoader.getRequest(reqCont).getParameter("REMOVE") != null) {

                            // delete a group from selectedGroups
                            // and move it to notSelectedGroups
                            String groupname = (String)parameters.get("selectgroup");
                            if(groupname != null) {
                                if(!notSelectedGroups.contains(groupname)){
                                    notSelectedGroups.addElement(groupname);
                                    if(groupname.equals(defaultGroup)) {
                                        defaultGroup = "";
                                    }
                                }
                                selectedGroups.removeElement(groupname);
                            }
                        }else {
                            if(CmsXmlTemplateLoader.getRequest(reqCont).getParameter("OK") != null) {

                                // form submitted, try to change the user data
                                try {
                                    if(email.equals("") || userLastname.equals("")
                                            || user.equals("")) {
                                        throw new CmsLegacyException("user data missing",
                                            CmsLegacyException.C_NO_USER);
                                    }
                                    if(!pwd.equals(pwd2)) {
                                        throw new CmsLegacySecurityException("unequal passwords",
                                            CmsLegacySecurityException.C_SECURITY_INVALID_PASSWORD);
                                    }
                                    if(!pwd.equals("")) {
                                        cms.setPassword(user, pwd);
                                    } // if nothing is entered don't change the password
                                    CmsUser theUser = cms.readUser(user);
                                    theUser.setEmail(email);
                                    theUser.setDescription(desc);
                                    theUser.setFirstname(firstname);
                                    theUser.setLastname(userLastname);
                                    theUser.setAddress(street);
                                    theUser.setAdditionalInfo(C_ADDITIONAL_INFO_ZIPCODE, zipcode);
                                    theUser.setAdditionalInfo(C_ADDITIONAL_INFO_TOWN, town);
                                    theUser.setAdditionalInfo(C_ADDITIONAL_INFO_DEFAULTGROUP, defaultGroup);
                                    if((OpenCms.getDefaultUsers().getUserAdmin().equals(theUser.getName()))
                                            && (!selectedGroups.contains(OpenCms.getDefaultUsers().getGroupAdministrators()))) {
                                        throw new CmsLegacyException("cant remove Admin from "
                                                + OpenCms.getDefaultUsers().getGroupAdministrators(), CmsLegacyException.C_NOT_ADMIN);
                                    }
                                    if(disabled && selectedGroups.contains(OpenCms.getDefaultUsers().getGroupAdministrators())) {
                                        throw new CmsLegacyException("disabled admin",
                                            CmsLegacyException.C_NOT_ADMIN);
                                    }
                                    if(disabled == true) {
                                        theUser.setDisabled();
                                    }else {
                                        theUser.setEnabled();
                                    }
                                    changeGroups(cms, theUser, selectedGroups);
                                    session.removeValue("selectedGroups");
                                    session.removeValue("notSelectedGroups");
                                    session.removeValue("DEFAULTGROUP");
                                    session.removeValue("ERROR");
                                    templateSelector = ""; //successful
                                }catch(CmsException e) {
                                    session.putValue("ERROR", new String("yes")); // remeber that an error has occurred
                                    session.putValue("USERFIRSTNAME", firstname);
                                    session.putValue("USERDESC", desc);
                                    session.putValue("USERSTREET", street);
                                    session.putValue("PWD", pwd);
                                    session.putValue("PWD2", pwd2);
                                    session.putValue("USER", user);
                                    session.putValue("USERNAME", userLastname);
                                    session.putValue("ZIP", zipcode);
                                    session.putValue("TOWN", town);
                                    session.putValue("USEREMAIL", email);
                                    session.putValue("DEFAULTGROUP", defaultGroup);
                                    if(e.getType() == CmsLegacySecurityException.C_SECURITY_INVALID_PASSWORD) {
                                        if(e.getMessage().equals("unequal passwords")) {
                                            templateSelector = "passworderror3";
                                        }else {
                                            if(e.getMessage().equals("password too short")) {
                                                templateSelector = "passworderror4";
                                            }else {
                                                xmlTemplateDocument.setData("reasonOfError", e.getMessage());
                                                xmlTemplateDocument.setData("perspective", "changeuser");
                                                templateSelector = "passworderror5";
                                            }
                                        }
                                    }else {
                                        if(e.getType() == CmsLegacyException.C_NO_GROUP) {
                                            templateSelector = "errornogroup2";
                                        }else {
                                            if(e.getType() == CmsLegacyException.C_NO_USER
                                                    && e.getMessage().equals("user data missing")) {
                                                templateSelector = "errordatamissing2";
                                            }else {
                                                if(e.getType() == CmsLegacyException.C_NOT_ADMIN
                                                        && e.getMessage().equals("disabled admin")) {
                                                    templateSelector = "errordisabledadmin";
                                                }else {
                                                    session.putValue("ERROR", new String("yes"));
                                                    throw e; // hand the exception down
                                                }
                                            }
                                        }
                                    }
                                } // catch block
                            } // OK
                        }
                    }
                } // userYetEstablished
                session.putValue("selectedGroups", selectedGroups);
                session.putValue("notSelectedGroups", notSelectedGroups);
                session.putValue("DEFAULTGROUP", defaultGroup);
                xmlTemplateDocument.setData("DISABLED", disabled ? "checked" : "");
            }

            // again common part for 'newuser' and 'changeuser':
            // set the variables for display in the document
            if(firstname == null) {
                firstname = "";
            }
            if(desc == null) {
                desc = "";
            }
            if(street == null) {
                street = "";
            }
            if(pwd == null) {
                pwd = "";
            }
            if(pwd2 == null) {
                pwd2 = "";
            }
            if(user == null) {
                user = "";
            }
            if(userLastname == null) {
                userLastname = "";
            }
            if(town == null) {
                town = "";
            }
            if(zipcode == null) {
                zipcode = "";
            }
            if(email == null) {
                email = "";
            }
            xmlTemplateDocument.setData("USERFIRSTNAME", firstname);
            xmlTemplateDocument.setData("USERDESC", desc);
            xmlTemplateDocument.setData("USERSTREET", street);
            xmlTemplateDocument.setData("PWD", pwd);
            xmlTemplateDocument.setData("PWD2", pwd2);
            xmlTemplateDocument.setData("USER", user);
            xmlTemplateDocument.setData("USERNAME", userLastname);
            xmlTemplateDocument.setData("TOWN", town);
            xmlTemplateDocument.setData("ZIP", zipcode);
            xmlTemplateDocument.setData("EMAIL", email);
        } // belongs to: 'if perspective is newuser or changeuser'
        else {
            if(perspective.equals("deleteuser")) {
                String user = (String)parameters.get("USER");
                xmlTemplateDocument.setData("USER", user);
                templateSelector = "RUsureDelete";
            }else {
                if(perspective.equals("reallydeleteuser")) {

                    // deleting a user
                    String user = (String)parameters.get("USER");
                    try {
                        cms.deleteUser(user);
                        templateSelector = "";
                    }catch(Exception e) {

                        // user == null or delete failed
                        xmlTemplateDocument.setData("DELETEDETAILS", CmsException.getStackTraceAsString(e));
                        templateSelector = "deleteerror";
                    }
                } // delete user
            }
        }

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
        List groups = cms.getGroups();
        int retValue = -1;

        // fill the names and values
        for(int z = 0;z < groups.size();z++) {
            String name = ((CmsGroup)groups.get(z)).getName();
            if(OpenCms.getDefaultUsers().getGroupUsers().equals(name)) {
                retValue = z;
            }
            names.addElement(name);
            values.addElement(name);
        }
        return new Integer(retValue);
    }

    /**
     * Gets all groups in which the user is, i.e. the selected ones and the indirect ones
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

    public Integer getGroupsOfUser(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String defaultGroup = (String)session.getValue("DEFAULTGROUP");
        if(defaultGroup == null) {
            defaultGroup = "";
        }
        getSelectedGroups(cms, lang, names, values, parameters);
        getIndirectGroups(cms, lang, names, values, parameters);
        return new Integer(names.indexOf(defaultGroup));
    }

    /**
     * Gets all groups in which the user is but not the direct ones
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

    public Integer getIndirectGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        Vector selectedGroups = (Vector)session.getValue("selectedGroups");
        Vector indirectGroups = new Vector();
        String groupname, superGroupName;
        CmsGroup superGroup;
        if(selectedGroups != null) {

            // get all parents of the groups
            Enumeration enu = selectedGroups.elements();
            while(enu.hasMoreElements()) {
                groupname = (String)enu.nextElement();
                superGroup = cms.getParent(groupname);
                while((superGroup != null) && (!indirectGroups.contains(superGroup.getName()))) {
                    superGroupName = superGroup.getName();
                    indirectGroups.addElement(superGroupName);

                    // read next super group
                    superGroup = cms.getParent(superGroupName);
                }
            }

            // now remove the direct groups off the list
            for(int z = 0;z < selectedGroups.size();z++) {
                String name = (String)selectedGroups.elementAt(z);
                indirectGroups.removeElement(name);
            }
        }
        for(int z = 0;z < indirectGroups.size();z++) {
            String name = (String)indirectGroups.elementAt(z);
            names.addElement(name);
            values.addElement(name);
        }
        return new Integer(-1); // none preselected
    }

    /**
     * Gets all groups, that have not yet been selected for the user
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

    public Integer getNotSelectedGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {
        int retValue = -1;
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        Vector notSelectedGroups = (Vector)session.getValue("notSelectedGroups");
        if(notSelectedGroups != null) {

            // fill the names and values
            for(int z = 0;z < notSelectedGroups.size();z++) {
                String name = (String)notSelectedGroups.elementAt(z);
                if(OpenCms.getDefaultUsers().getGroupUsers().equals(name)) {
                    retValue = z;
                }
                names.addElement(name);
                values.addElement(name);
            }
        }

        // no current group, set index to -1
        return new Integer(retValue);
    }

    /**
     * Gets all groups that have been selected for the user to be in
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

    public Integer getSelectedGroups(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {
        int retValue = -1;
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        String defaultGroup = (String)session.getValue("DEFAULTGROUP");
        if(defaultGroup == null) {
            defaultGroup = "";
        }
        Vector selectedGroups = (Vector)session.getValue("selectedGroups");
        if(selectedGroups != null) {
            for(int z = 0;z < selectedGroups.size();z++) {
                String name = (String)selectedGroups.elementAt(z);
                if(name.equals(defaultGroup)) {
                    retValue = z;
                }
                names.addElement(name);
                values.addElement(name);
            }
        }else {
            selectedGroups = new Vector();
        }
        return new Integer(retValue);
    }

    /**
     * Gets all users
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

    public Integer getUsers(CmsObject cms, CmsXmlLanguageFile lang, Vector names,
            Vector values, Hashtable parameters) throws CmsException {

        // get all users
        List users = cms.getUsers();
        int retValue = -1;
		String firsname;
		String lastname;
		String username;
		String nameToShow;

        // fill the names and values
        for(int z = 0;z < users.size();z++) {
			firsname = ((CmsUser)users.get(z)).getFirstname();
			//Changed by Le (comundus GmbH)
			lastname = ((CmsUser)users.get(z)).getLastname();
            username = ((CmsUser)users.get(z)).getName();
			nameToShow = username + "   -   " + firsname + " " + lastname;
			//***
            names.addElement(nameToShow);
            values.addElement(username);
        }
        if(users.size() > 0) {
            retValue = 0; // preselect first user
        }
        return new Integer(retValue);


    }
    
    /**
     * Determines if the user icon is shown in the administration view depending on the property value of the key "workplace.administration.showusergroupicon".<p>
     * 
     * @param cms the CmsObject
     * @param lang the workplace language file
     * @param parameters the parameters
     * @return Boolean to determine if user icon is shown in the administration view
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
