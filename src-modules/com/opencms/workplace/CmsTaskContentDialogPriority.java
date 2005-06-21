/*
* File   : $Source: /alkacon/cvs/opencms/src-modules/com/opencms/workplace/Attic/CmsTaskContentDialogPriority.java,v $
* Date   : $Date: 2005/06/21 15:49:59 $
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
import org.opencms.file.CmsUser;
import org.opencms.i18n.CmsEncoder;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsDateUtil;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskService;

import com.opencms.core.I_CmsSession;
import com.opencms.legacy.CmsXmlTemplateLoader;
import com.opencms.template.A_CmsXmlContent;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

/**
 * Template class for displaying OpenCms workplace task content screens.
 * <P>
 *
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 2005/06/21 15:49:59 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsTaskContentDialogPriority extends CmsWorkplaceDefault {


    /**
     * Constant for generating user javascriptlist
     */
    private final static String C_ALL_ROLES = "___all";


    /**
     * Constant for generating user javascriptlist
     */
    private static String C_ROLE = null;


    /**
     * Constant for generating user javascriptlist
     */
    private static String C_ROLE_1 = null;


    /**
     * Constant for generating user javascriptlist
     */
    private static String C_ROLE_2 = null;


    /**
     * Constant for generating user javascriptlist
     */
    private static String C_USER_1 = null;


    /**
     * Constant for generating user javascriptlist
     */
    private static String C_USER_2 = null;


    /**
     * Constant for generating user javascriptlist
     */
    private static String C_USER_3 = null;


    /**
     * Constant for generating user javascriptlist
     */
    private static String C_USER_4 = null;


    /**
     * Constant for generating user javascriptlist
     */
    private static String C_USER_5 = null;

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
        if(CmsLog.getLog(this).isDebugEnabled() && C_DEBUG) {
            CmsLog.getLog(this).debug("Getting content of element " + ((elementName==null)?"<root>":elementName));
            CmsLog.getLog(this).debug("Template file is: " + templateFile);
            CmsLog.getLog(this).debug("Selected template section is: " + ((templateSelector==null)?"<default>":templateSelector));
        }
        String taskName = "";
        String taskDescription = "";
        String due = "";
        String paraAcceptation = "";
        String paraAll = "";
        String paraCompletion = "";
        String paraDelivery = "";
        String userIdxString = "";
        String groupIdxString = "";
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);
        CmsXmlWpTemplateFile xmlTemplateDocument = (CmsXmlWpTemplateFile)getOwnTemplateFile(cms,
                templateFile, elementName, parameters, templateSelector);

        // are the constants read from the cms already?
        if(C_ROLE == null) {

            // declare the constants
            initConstants(xmlTemplateDocument);
        }
        try {
            String taskidstr = (String)parameters.get("taskid");
            int taskid;
            if(taskidstr == null || taskidstr == "") {
                Integer sessionTaskid = (Integer)session.getValue("taskid");
                taskid = sessionTaskid.intValue();
            }
            else {
                Integer taskidInt = new Integer(taskidstr);
                taskid = taskidInt.intValue();
                session.putValue("taskid", taskidInt);
            }
            CmsTaskService taskService = cms.getTaskService();
            CmsTask task = taskService.readTask(taskid);
            taskName = task.getName();
            taskDescription = CmsTaskAction.getDescription(cms, task.getId());
            paraAcceptation = taskService.getTaskPar(task.getId(), C_TASKPARA_ACCEPTATION);
            paraAll = taskService.getTaskPar(task.getId(), C_TASKPARA_ALL);
            paraCompletion = taskService.getTaskPar(task.getId(), C_TASKPARA_COMPLETION);
            paraDelivery = taskService.getTaskPar(task.getId(), C_TASKPARA_DELIVERY);
            due = CmsDateUtil.getDateShort(task.getTimeOut().getTime());

            // preselect the old user and role in the dialog for forwarding and resurrection
            // compute the indices of the user and role
            String username = taskService.readAgent(task).getName();
            String groupname = taskService.readGroup(task).getName();
            int userindex = 0;
            int groupindex = 0;
            List groups = cms.getGroups();
            List users = cms.getUsersOfGroup(groupname);
            int n = 0;
            for(int z = 0;z < groups.size();z++) {
                CmsGroup group = (CmsGroup)groups.get(z);
                if(group.getRole()) {
                    n++;
                    if(group.getName().equals(groupname)) {
                        groupindex = n;
                    }
                }
            }
            for(int z = 0;z < users.size();z++) {
                if(((CmsUser)users.get(z)).getName().equals(username)) {
                    userindex = z + 1;
                }
            }
            userIdxString = (new Integer(userindex)).toString();
            groupIdxString = (new Integer(groupindex)).toString();
        }
        catch(Exception exc) {


        // unexpected exception - ignoring
        }
        xmlTemplateDocument.setData("task", CmsEncoder.escape(taskName,
            cms.getRequestContext().getEncoding()));
        xmlTemplateDocument.setData("description", CmsEncoder.escape(taskDescription,
            cms.getRequestContext().getEncoding()));
        xmlTemplateDocument.setData("due", due);
        xmlTemplateDocument.setData(C_TASKPARA_ACCEPTATION, paraAcceptation);
        xmlTemplateDocument.setData(C_TASKPARA_ALL, paraAll);
        xmlTemplateDocument.setData(C_TASKPARA_COMPLETION, paraCompletion);
        xmlTemplateDocument.setData(C_TASKPARA_DELIVERY, paraDelivery);
        xmlTemplateDocument.setData("groupindex", groupIdxString);
        xmlTemplateDocument.setData("userindex", userIdxString);

        // Now load the template file and start the processing
        return startProcessing(cms, xmlTemplateDocument, elementName, parameters, templateSelector);
    }

    /**
     * Gets all priorities, that are defined for a project.
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

    public Integer getPriority(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values,
            Hashtable parameters) throws CmsException {

        // get session for current taskid
        I_CmsSession session = CmsXmlTemplateLoader.getSession(cms.getRequestContext(), true);

        // read current task for priority-level
        CmsTask task = cms.getTaskService().readTask(((Integer)session.getValue("taskid")).intValue());

        // add names for priority
        names.addElement(lang.getLanguageValue("task.label.prioritylevel.high"));
        names.addElement(lang.getLanguageValue("task.label.prioritylevel.middle"));
        names.addElement(lang.getLanguageValue("task.label.prioritylevel.low"));

        // add values for priority
        values.addElement(org.opencms.workflow.CmsTaskService.TASK_PRIORITY_HIGH + "");
        values.addElement(CmsTaskService.TASK_PRIORITY_NORMAL + "");
        values.addElement(org.opencms.workflow.CmsTaskService.TASK_PRIORITY_LOW + "");

        // return the current priority
        return new Integer(task.getPriority() - 1);
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

    public Integer getTeams(CmsObject cms, CmsXmlLanguageFile lang, Vector names, Vector values,
            Hashtable parameters) throws CmsException {

        // get all groups
        List groups = cms.getGroups();
        CmsGroup group;
        names.addElement(lang.getLanguageValue("task.label.emptyrole"));
        values.addElement(lang.getLanguageValue("task.label.emptyrole"));

        // fill the names and values
        for(int z = 0;z < groups.size();z++) {
            group = (CmsGroup)groups.get(z);

            // is the group a role?
            if(group.getRole()) {
                String name = group.getName();
                names.addElement(name);
                values.addElement(name);
            }
        }
        names.addElement(lang.getLanguageValue("task.label.allroles"));
        values.addElement(C_ALL_ROLES);

        // no current group, set index to -1
        return new Integer(-1);
    }

    /**
     * @param cms CmsObject Object for accessing system resources.
     * @param tagcontent Unused in this special case of a user method. Can be ignored.
     * @param doc Reference to the A_CmsXmlContent object of the initiating XLM document <em>(not used here)</em>.
     * @param userObj Hashtable with parameters <em>(not used here)</em>.
     * @return String with the pics URL.
     * @throws CmsException
     */

    public Object getUsers(CmsObject cms, String tagcontent, A_CmsXmlContent doc,
            Object userObj) throws CmsException {
        StringBuffer retValue = new StringBuffer();
        retValue.append(C_ROLE);

        // get the language for choose-user
        String chooseUser = (new CmsXmlLanguageFile(cms)).getLanguageValue("task.label.emptyuser");

        // get all groups
        List groups = cms.getGroups();
        for(int n = 0;n < groups.size();n++) {
            if(((CmsGroup)groups.get(n)).getRole()) {
                String groupname = ((CmsGroup)groups.get(n)).getName();

                // get users of this group
                List users = cms.getUsersOfGroup(groupname);

                // create entry for role
                retValue.append(C_ROLE_1 + groupname + C_ROLE_2);
                retValue.append(C_USER_1 + groupname + C_USER_2 + 0 + C_USER_3 + chooseUser + C_USER_4 + C_USER_5);
                for(int m = 0;m < users.size();m++) {
                    CmsUser user = (CmsUser)users.get(m);

                    // create entry for user
                    retValue.append(C_USER_1 + groupname + C_USER_2 + (m + 1) + C_USER_3
                            + user.getName() + C_USER_4 + user.getName() + C_USER_5);
                }
            }
        }

        // generate output for all users
        retValue.append(C_ROLE_1 + C_ALL_ROLES + C_ROLE_2);
        retValue.append(C_USER_1 + C_ALL_ROLES + C_USER_2 + 0 + C_USER_3 + chooseUser + C_USER_4 + C_USER_5);
        List users = cms.getUsers();
        for(int m = 0;m < users.size();m++) {
            CmsUser user = (CmsUser)users.get(m);

            // create entry for user
            retValue.append(C_USER_1 + C_ALL_ROLES + C_USER_2 + (m + 1) + C_USER_3
                    + user.getName() + C_USER_4 + user.getName() + C_USER_5);
        }
        return retValue.toString();
    }

    /**
     * This method initializes all constants, that are needed for genrating this pages.
     *
     * @param document The xml-document to get the constant content from.
     */

    private void initConstants(CmsXmlWpTemplateFile document) {
        try {

            // exists the needed datablocks?
            if(document.hasData("role")) {

                // YES: initialize the constants
                C_ROLE = document.getDataValue("role");
                C_ROLE_1 = document.getDataValue("role_1");
                C_ROLE_2 = document.getDataValue("role_2");
                C_USER_1 = document.getDataValue("user_1");
                C_USER_2 = document.getDataValue("user_2");
                C_USER_3 = document.getDataValue("user_3");
                C_USER_4 = document.getDataValue("user_4");
                C_USER_5 = document.getDataValue("user_5");
            }
        }
        catch(CmsException exc) {
            if(CmsLog.getLog(this).isWarnEnabled() ) {
                CmsLog.getLog(this).warn("Couldn't get xml datablocks for CmsTaskNew", exc);
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
