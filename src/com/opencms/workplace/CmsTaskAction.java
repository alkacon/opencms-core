/*
* File   : $Source: /alkacon/cvs/opencms/src/com/opencms/workplace/Attic/CmsTaskAction.java,v $
* Date   : $Date: 2004/07/18 16:27:12 $
* Version: $Revision: 1.54 $
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
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workflow.CmsTask;
import org.opencms.workflow.CmsTaskLog;
import org.opencms.workplace.I_CmsWpConstants;

import com.opencms.legacy.CmsXmlTemplateLoader;

import java.util.GregorianCalendar;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

/**
 * This helper-class is used to do task-actions like create or forward. It uses the
 * workplace-languagefile to add task-logs in the language of the user.
 * <P>
 *
 * @author Andreas Schouten
 * @version $Revision: 1.54 $ $Date: 2004/07/18 16:27:12 $
 * @see com.opencms.workplace.CmsXmlWpTemplateFile
 * 
 * @deprecated Will not be supported past the OpenCms 6 release.
 */

public class CmsTaskAction implements I_CmsWpConstants {


    /**
     * Constant for generating user javascriptlist
     */
    private final static String C_ALL_ROLES = "___all";

    /**
     * Accepts a task.
     * @param cms The cms-object.
     * @param int taskid The id of the task.
     * @throws CmsException Throws CmsExceptions, that are be
     * thrown in calling methods.
     */

    public static void accept(CmsObject cms, int taskid) throws CmsException {
        cms.acceptTask(taskid);
        String comment = "";
        cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_ACCEPTED);

        // send an email if "Benachrichtigung bei Annahme" was selected.
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        CmsTask task = cms.readTask(taskid);
        if(cms.getTaskPar(task.getId(), C_TASKPARA_ACCEPTATION) != null) {
            StringBuffer contentBuf = new StringBuffer(lang.getLanguageValue("task.email.accept.content"));
            contentBuf.append("\n");
            contentBuf.append(lang.getLanguageValue("task.label.project"));
            contentBuf.append(": ");
            String projectname = "?";
            try {
                projectname = cms.readTask(task.getRoot()).getName();
            }
            catch(Exception exc) {


            // no root?!
            }
            contentBuf.append(projectname);
            contentBuf.append("\n");
            contentBuf.append(lang.getLanguageValue("task.label.task"));
            contentBuf.append(": ");
            contentBuf.append(task.getName());
            int projectid = cms.readProject(task).getId();
            contentBuf.append("\n\n\n" + getTaskUrl(cms, taskid, projectid));
            String subject = lang.getLanguageValue("task.email.accept.subject") + " " + CmsUser.getFullName(cms.readAgent(task));
            CmsUser[] users =  {
                cms.readOwner(task)
            };
            try {
                org.opencms.util.CmsMail mail = new org.opencms.util.CmsMail(cms, cms.readAgent(task),
                        users, subject, contentBuf.toString(), "text/plain");
                mail.start();
            }
            catch(Exception exc) {
                if(OpenCms.getLog(CmsTaskAction.class).isWarnEnabled()) {
                    OpenCms.getLog(CmsTaskAction.class).warn("Error while sending task mail", exc);
                }
            }
        }
    }

    /**
     * Creates a new task.
     * @param cms The cms-object.
     * @param agentName The name of the new editor for this task.
     * @param roleName The name of the new role for the user.
     * @param taskName The new name of the task.
     * @param taskcomment The new comment for this task.
     * @param timeoutString The new timeout-date as a string in the following format:
     * "dd.mm.yyyy"
     * @param priorityString the new priority as String ("1" = high,
     * "2" = normal or "3" = low)
     * @param paraAcceptation controls if a message should be send by acceptation. ("checked" | "")
     * @param paraAll controls if a message should be send to all users in a role. ("checked" | "")
     * @param paraCompletion controls if a message should be send by completing this task. ("checked" | "")
     * @param paraDelivery controls if a message should be send by delivering a task. ("checked" | "")
     * @throws CmsException Throws CmsExceptions, that are be
     * thrown in calling methods.
     */

    public static void create(CmsObject cms, String agentName, String roleName, String taskName,
            String taskcomment, String timeoutString, String priorityString, String paraAcceptation,
            String paraAll, String paraCompletion, String paraDelivery) throws CmsException {
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        if(roleName.equals(C_ALL_ROLES)) {
            roleName = cms.readUser(agentName).getDefaultGroup().getName();
        }

        // try to create the task
        int priority = Integer.parseInt(priorityString);

        // create a long from the overgiven date.
        String splittetDate[] = CmsStringUtil.split(timeoutString, ".");
        GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(splittetDate[2]),
                Integer.parseInt(splittetDate[1]) - 1, Integer.parseInt(splittetDate[0]), 0, 0, 0);
        long timeout = cal.getTime().getTime();
        CmsTask task = cms.createTask(agentName, roleName, taskName, timeout, priority);
        cms.setTaskPar(task.getId(), C_TASKPARA_ACCEPTATION, paraAcceptation);
        cms.setTaskPar(task.getId(), C_TASKPARA_ALL, paraAll);
        cms.setTaskPar(task.getId(), C_TASKPARA_COMPLETION, paraCompletion);
        cms.setTaskPar(task.getId(), C_TASKPARA_DELIVERY, paraDelivery);
        String comment = lang.getLanguageValue("task.label.forrole") + ": " + roleName + "\n";
        comment += lang.getLanguageValue("task.label.editor") + ": " + CmsUser.getFullName(cms.readUser(task.getAgentUser())) + "\n";
        comment += taskcomment;
        cms.writeTaskLog(task.getId(), comment, C_TASKLOGTYPE_CREATED);

        // send an email
        // per default send a mail from task's organizer to task's recipient.
        StringBuffer contentBuf = new StringBuffer(lang.getLanguageValue("task.email.create.content"));
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.project"));
        contentBuf.append(": ");
        String projectname = "?";
        try {
            projectname = cms.readTask(task.getRoot()).getName();
        }
        catch(Exception exc) {


        // no root?!
        }
        contentBuf.append(projectname);
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.task"));
        contentBuf.append(": ");
        contentBuf.append(task.getName());
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.actuator"));
        contentBuf.append(": ");
        contentBuf.append(CmsUser.getFullName(cms.readOwner(task)));
        int projectid = cms.readProject(task).getId();
        contentBuf.append("\n\n\n" + getTaskUrl(cms, task.getId(), projectid));
        String subject = lang.getLanguageValue("task.email.create.subject") + " " + CmsUser.getFullName(cms.readUser(task.getAgentUser())) + " / " + roleName;
        CmsUser[] users =  {
            cms.readAgent(task)
        };
        org.opencms.util.CmsMail mail = null;
        try {
            mail = new org.opencms.util.CmsMail(cms, cms.readOwner(task), users, subject, contentBuf.toString(), "text/plain");
        }
        catch(CmsException e) {
            if(OpenCms.getLog(CmsTaskAction.class).isWarnEnabled()) {
                OpenCms.getLog(CmsTaskAction.class).warn("Could not generate mail while creating task for "
                        + cms.readOwner(task).getName(), e);
            }
        }

        // if "Alle Rollenmitglieder von Aufgabe Benachrichtigen" checkbox is selected.
        if(cms.getTaskPar(task.getId(), C_TASKPARA_ALL) != null) {

            // the news deliver always "checked" or ""
            if(cms.getTaskPar(task.getId(), C_TASKPARA_ALL).equals("checked")) {
                try {
                    mail = new org.opencms.util.CmsMail(cms, cms.readOwner(task), cms.readGroup(task),
                            subject, contentBuf.toString(), "text/plain");
                }
                catch(CmsException e) {
                    if(OpenCms.getLog(CmsTaskAction.class).isWarnEnabled()) {
                        OpenCms.getLog(CmsTaskAction.class).warn("Could not generate mail while creating task for "
                                + cms.readOwner(task).getName(), e);
                    }
                }
            }
        }
        if(mail != null) {
            mail.start();
        }
    }

    /**
     * Changes the timeou-date of the task.
     * @param cms The cms-object.
     * @param int taskid The id of the task.
     * @param timeoutString The new timeout-date as a string in the following format:
     * "dd.mm.yyyy"
     * @throws CmsException Throws CmsExceptions, that are be
     * thrown in calling methods.
     */

    public static void due(CmsObject cms, int taskid, String timeoutString) throws CmsException {
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        CmsTask task = cms.readTask(taskid);
        String splittetDate[] = CmsStringUtil.split(timeoutString, ".");
        GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(splittetDate[2]),
                Integer.parseInt(splittetDate[1]) - 1, Integer.parseInt(splittetDate[0]), 0, 0, 0);
        long timeout = cal.getTime().getTime();
        cms.setTimeout(taskid, timeout);

        // add comment
        String comment = "";
        comment += lang.getLanguageValue("task.dialog.due.logmessage1") + " ";
        comment += CmsDateUtil.getDateShort(task.getTimeOut().getTime()) + " ";
        comment += lang.getLanguageValue("task.dialog.due.logmessage2") + " ";
        comment += CmsDateUtil.getDateShort(timeout);
        cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_DUECHANGED);
    }

    /**
     * Ends a task.
     * @param cms The cms-object.
     * @param int taskid The id of the task.
     * @throws CmsException Throws CmsExceptions, that are be
     * thrown in calling methods.
     */

    public static void end(CmsObject cms, int taskid) throws CmsException {
        cms.endTask(taskid);
        String comment = "";
        cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_OK);

        // send an email if "Benachrichtigung bei Abhacken" was selected.
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        CmsTask task = cms.readTask(taskid);
        if(cms.getTaskPar(task.getId(), C_TASKPARA_COMPLETION) != null) {
            StringBuffer contentBuf = new StringBuffer(lang.getLanguageValue("task.email.end.content"));
            contentBuf.append("\n");
            contentBuf.append(lang.getLanguageValue("task.label.project"));
            contentBuf.append(": ");
            String projectname = "?";
            try {
                projectname = cms.readTask(task.getRoot()).getName();
            }
            catch(Exception exc) {


            // no root?!
            }
            contentBuf.append(projectname);
            contentBuf.append("\n");
            contentBuf.append(lang.getLanguageValue("task.label.task"));
            contentBuf.append(": ");
            contentBuf.append(task.getName());
            contentBuf.append("\n");
            contentBuf.append(lang.getLanguageValue("task.label.taskfor"));
            contentBuf.append(": ");
            contentBuf.append(CmsUser.getFullName(cms.readOriginalAgent(task)));
            contentBuf.append("\n");
            contentBuf.append(lang.getLanguageValue("task.label.editor"));
            contentBuf.append(": ");
            contentBuf.append(CmsUser.getFullName(cms.readAgent(task)));
            int projectid = cms.readProject(task).getId();
            contentBuf.append("\n\n\n" + getTaskUrl(cms, task.getId(), projectid));
            String subject = lang.getLanguageValue("task.email.end.subject") + " " + CmsUser.getFullName(cms.readAgent(task));
            CmsUser[] users =  {
                cms.readOwner(task)
            };
            try {
                org.opencms.util.CmsMail mail = new org.opencms.util.CmsMail(cms, cms.readAgent(task),
                        users, subject, contentBuf.toString(), "text/plain");
                mail.start();
            }
            catch(Exception exc) {
                if(OpenCms.getLog(CmsTaskAction.class).isWarnEnabled()) {
                    OpenCms.getLog(CmsTaskAction.class).warn("Error while sending task mail", exc);
                }
            }
        }
    }

    /**
     * Forwards a task. The task is forwarded to a new editor in a new role.
     * @param cms The cms-object.
     * @param int taskid The id of the task.
     * @param newEditorName The name of the new editor for this task.
     * @param newRoleName The name of the new role for the user.
     * @throws CmsException Throws CmsExceptions, that are be
     * thrown in calling methods.
     */

    public static void forward(CmsObject cms, int taskid, String newEditorName, String newRoleName) throws CmsException {
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        CmsUser newEditor = cms.readUser(newEditorName);
        if(newRoleName.equals(C_ALL_ROLES)) {
            newRoleName = cms.readUser(newEditorName).getDefaultGroup().getName();
        }
        CmsGroup oldRole = cms.readGroup(newRoleName);
        cms.forwardTask(taskid, oldRole.getName(), newEditor.getName());
        String comment = lang.getLanguageValue("task.dialog.forward.logmessage");
        comment += " " + CmsUser.getFullName(newEditor);
        cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_FORWARDED);

        // send an email if "Benachrichtigung bei Weiterleitung" was selected.
        CmsTask task = cms.readTask(taskid);
        if(cms.getTaskPar(task.getId(), C_TASKPARA_DELIVERY) != null) {
            StringBuffer contentBuf = new StringBuffer(lang.getLanguageValue("task.email.forward.content"));
            contentBuf.append("\n");
            contentBuf.append(lang.getLanguageValue("task.label.project"));
            contentBuf.append(": ");
            String projectname = "?";
            try {
                projectname = cms.readTask(task.getRoot()).getName();
            }
            catch(Exception exc) {


            // no root?!
            }
            contentBuf.append(projectname);
            contentBuf.append("\n");
            contentBuf.append(lang.getLanguageValue("task.label.task"));
            contentBuf.append(": ");
            contentBuf.append(task.getName());
            contentBuf.append("\n");
            contentBuf.append(lang.getLanguageValue("task.label.actuator"));
            contentBuf.append(": ");
            contentBuf.append(CmsUser.getFullName(cms.readOwner(task)));
            contentBuf.append("\n");
            contentBuf.append(lang.getLanguageValue("task.label.taskfor"));
            contentBuf.append(": ");
            contentBuf.append(CmsUser.getFullName(cms.readOriginalAgent(task)));
            contentBuf.append("\n");
            contentBuf.append(lang.getLanguageValue("task.label.editor"));
            contentBuf.append(": ");
            contentBuf.append(CmsUser.getFullName(cms.readAgent(task)));
            int projectid = cms.readProject(task).getId();
            contentBuf.append("\n\n\n" + getTaskUrl(cms, task.getId(), projectid));
            String subject = lang.getLanguageValue("task.email.forward.subject") + " " + CmsUser.getFullName(cms.readUser(task.getAgentUser())) + " / " + newRoleName;

            // if "Alle Rollenmitglieder von Aufgabe Benachrichtigen" checkbox is selected.
            if(cms.getTaskPar(task.getId(), C_TASKPARA_ALL) != null) {
                try {
                    org.opencms.util.CmsMail mail = new org.opencms.util.CmsMail(cms, cms.getRequestContext().currentUser(),
                            cms.readGroup(task), subject, contentBuf.toString(), "text/plain");
                    mail.start();
                }
                catch(Exception exc) {
                    if(OpenCms.getLog(CmsTaskAction.class).isWarnEnabled()) {
                        OpenCms.getLog(CmsTaskAction.class).warn("Error while sending task mail", exc);
                    }
                }
            }
            else {

                // send a mail to user
                CmsUser[] user =  {
                    cms.readAgent(task)
                };
                try {
                    org.opencms.util.CmsMail mail1 = new org.opencms.util.CmsMail(cms,
                            cms.getRequestContext().currentUser(), user, subject, contentBuf.toString(), "text/plain");
                    mail1.start();
                }
                catch(Exception exc) {
                    if(OpenCms.getLog(CmsTaskAction.class).isWarnEnabled()) {
                        OpenCms.getLog(CmsTaskAction.class).warn("Error while sending task mail", exc);
                    }
                }

                // send a mail to owner
                CmsUser[] owner =  {
                    cms.readOwner(task)
                };
                try {
                    org.opencms.util.CmsMail mail2 = new org.opencms.util.CmsMail(cms, cms.getRequestContext().currentUser(),
                            owner, subject, contentBuf.toString(), "text/plain");
                    mail2.start();
                }
                catch(Exception exc) {
                    if(OpenCms.getLog(CmsTaskAction.class).isWarnEnabled()) {
                        OpenCms.getLog(CmsTaskAction.class).warn("Error while sending task mail", exc);
                    }
                }
            }
        }
    }

    /**
     * Gets the description of a task.
     * The description is stored in the task-log.
     * @param cms The cms-object.
     * @param int taskid The id of the task.
     * @return String the comment-string.
     * @throws CmsException Throws CmsExceptions, that are be
     * thrown in calling methods.
     */

    public static String getDescription(CmsObject cms, int taskid) throws CmsException {
        StringBuffer retValue = new StringBuffer("");
        CmsTaskLog tasklog;
        Vector taskdocs = cms.readTaskLogs(taskid);

        // go through all tasklogs to find the comment
        for(int i = 1;i <= taskdocs.size();i++) {
            tasklog = (CmsTaskLog)taskdocs.elementAt(taskdocs.size() - i);
            int type = tasklog.getType();

            // check if this is a type "created" or "new"
            if((type == C_TASKLOGTYPE_CREATED) || (type == C_TASKLOGTYPE_REACTIVATED)) {
                String comment[] = CmsStringUtil.split(tasklog.getComment(), "\n");
                for(int j = 2;j < comment.length;j++) {
                    retValue.append(comment[j] + "\n");
                }
                break;
            }
        }
        return retValue.toString();
    }

    /**
     * Sends a message to the editor of the task.
     * @param cms The cms-object.
     * @param int taskid The id of the task.
     * @param message The text of the message.
     * @throws CmsException Throws CmsExceptions, that are be
     * thrown in calling methods.
     */

    public static void message(CmsObject cms, int taskid, String message) throws CmsException {
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        CmsTask task = cms.readTask(taskid);
        String comment = lang.getLanguageValue("task.dialog.message.head") + " ";
        if((message != null) && (message.length() != 0)) {
            comment += CmsUser.getFullName(cms.readAgent(task)) + "\n";
            comment += message;
            cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_CALL);
        }

        // send an email
        StringBuffer contentBuf = new StringBuffer(lang.getLanguageValue("task.email.message.content"));
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.project"));
        contentBuf.append(": ");
        String projectname = "?";
        try {
            projectname = cms.readTask(task.getRoot()).getName();
        }
        catch(Exception exc) {


        // no root?!
        }
        contentBuf.append(projectname);
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.task"));
        contentBuf.append(": ");
        contentBuf.append(task.getName());
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.actuator"));
        contentBuf.append(": ");
        contentBuf.append(CmsUser.getFullName(cms.readOwner(task)));
        int projectid = cms.readProject(task).getId();
        contentBuf.append("\n\n\n" + getTaskUrl(cms, task.getId(), projectid));
        String subject = lang.getLanguageValue("task.email.message.subject") + " " + CmsUser.getFullName(cms.readOwner(task));
        CmsUser[] users =  {
            cms.readAgent(task)
        };
        try {
            org.opencms.util.CmsMail mail = new org.opencms.util.CmsMail(cms,
                    cms.readOwner(task), users, subject, contentBuf.toString(), "text/plain");
            mail.start();
        }
        catch(Exception exc) {
            if(OpenCms.getLog(CmsTaskAction.class).isWarnEnabled()) {
                OpenCms.getLog(CmsTaskAction.class).warn("Error while sending task mail", exc);
            }
        }
    }

    /**
     * Changes the priority of a task.
     * @param cms The cms-object.
     * @param int taskid The id of the task.
     * @param priorityString the new priority as String ("1" = high,
     * "2" = normal or "3" = low)
     * @throws CmsException Throws CmsExceptions, that are be
     * thrown in calling methods.
     */

    public static void priority(CmsObject cms, int taskid, String priorityString) throws CmsException {
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        CmsTask task = cms.readTask(taskid);
        int priority = Integer.parseInt(priorityString);
        cms.setPriority(taskid, priority);

        // add comment
        String comment = "";
        comment += lang.getLanguageValue("task.dialog.priority.logmessage1") + " ";
        comment += lang.getLanguageValue("task.dialog.priority.logmessageprio" + task.getPriority()) + " ";
        comment += lang.getLanguageValue("task.dialog.priority.logmessage2") + " ";
        comment += lang.getLanguageValue("task.dialog.priority.logmessageprio" + priority) + " ";
        cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_PRIORITYCHANGED);
    }

    /**
     * Sends a message to the initiator (owner) of the task.
     * @param cms The cms-object.
     * @param int taskid The id of the task.
     * @param message The text of the message.
     * @throws CmsException Throws CmsExceptions, that are be
     * thrown in calling methods.
     */

    public static void query(CmsObject cms, int taskid, String message) throws CmsException {
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        CmsTask task = cms.readTask(taskid);
        String comment = lang.getLanguageValue("task.dialog.query.head") + " ";
        if((message != null) && (message.length() != 0)) {
            comment += CmsUser.getFullName(cms.readOwner(task)) + "\n";
            comment += message;
            cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_CALL);
        }

        // send an email.
        StringBuffer contentBuf = new StringBuffer(lang.getLanguageValue("task.email.query.content"));
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.project"));
        contentBuf.append(": ");
        String projectname = "?";
        try {
            projectname = cms.readTask(task.getRoot()).getName();
        }
        catch(Exception exc) {


        // no root?!
        }
        contentBuf.append(projectname);
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.task"));
        contentBuf.append(": ");
        contentBuf.append(task.getName());
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.editor"));
        contentBuf.append(": ");
        contentBuf.append(CmsUser.getFullName(cms.readAgent(task)));
        int projectid = cms.readProject(task).getId();
        contentBuf.append("\n\n\n" + getTaskUrl(cms, task.getId(), projectid));
        String subject = lang.getLanguageValue("task.email.query.subject") + " " + CmsUser.getFullName(cms.readAgent(task));
        CmsUser[] users =  {
            cms.readOwner(task)
        };
        try {
            org.opencms.util.CmsMail mail = new org.opencms.util.CmsMail(cms, cms.readAgent(task),
                    users, subject, contentBuf.toString(), "text/plain");
            mail.start();
        }
        catch(Exception exc) {
            if(OpenCms.getLog(CmsTaskAction.class).isWarnEnabled()) {
                OpenCms.getLog(CmsTaskAction.class).warn("Error while sending task mail", exc);
            }
        }
    }

    /**
     * Reaktivates a task.
     * @param cms The cms-object.
     * @param int taskid The id of the task.
     * @param agentName The name of the new editor for this task.
     * @param roleName The name of the new role for the user.
     * @param taskName The new name of the task.
     * @param taskcomment The new comment for this task.
     * @param timeoutString The new timeout-date as a string in the following format:
     * "dd.mm.yyyy"
     * @param priorityString the new priority as String ("1" = high,
     * "2" = normal or "3" = low)
     * @param paraAcceptation controls if a message should be send by acceptation. ("checked" | "")
     * @param paraAll controls if a message should be send to all users in a role. ("checked" | "")
     * @param paraCompletion controls if a message should be send by completing this task. ("checked" | "")
     * @param paraDelivery controls if a message should be send by delivering a task. ("checked" | "")
     * @throws CmsException Throws CmsExceptions, that are be
     * thrown in calling methods.
     */

    public static void reakt(CmsObject cms, int taskid, String agentName, String roleName, String taskName,
            String taskcomment, String timeoutString, String priorityString,
            String paraAcceptation, String paraAll, String paraCompletion, String paraDelivery) throws CmsException {
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        CmsTask task = cms.readTask(taskid);
        if(roleName.equals(C_ALL_ROLES)) {
            roleName = cms.readUser(agentName).getDefaultGroup().getName();
        }
        cms.setName(taskid, taskName);

        // try to reaktivate the task
        cms.reaktivateTask(taskid);
        int priority = Integer.parseInt(priorityString);
        cms.setPriority(taskid, priority);

        // create a long from the overgiven date.
        String splittetDate[] = CmsStringUtil.split(timeoutString, ".");
        GregorianCalendar cal = new GregorianCalendar(Integer.parseInt(splittetDate[2]), Integer.parseInt(splittetDate[1]) - 1,
                Integer.parseInt(splittetDate[0]), 0, 0, 0);
        long timeout = cal.getTime().getTime();
        cms.setTimeout(taskid, timeout);
        cms.setTaskPar(taskid, C_TASKPARA_ACCEPTATION, paraAcceptation);
        cms.setTaskPar(taskid, C_TASKPARA_ALL, paraAll);
        cms.setTaskPar(taskid, C_TASKPARA_COMPLETION, paraCompletion);
        cms.setTaskPar(taskid, C_TASKPARA_DELIVERY, paraDelivery);
        cms.forwardTask(taskid, roleName, agentName);
        String comment = lang.getLanguageValue("task.label.forrole") + ": " + roleName + "\n";
        comment += lang.getLanguageValue("task.label.editor") + ": " + CmsUser.getFullName(cms.readUser(agentName)) + "\n";
        comment += taskcomment;
        cms.writeTaskLog(task.getId(), comment, C_TASKLOGTYPE_REACTIVATED);

        // send an email
        StringBuffer contentBuf = new StringBuffer(lang.getLanguageValue("task.email.reakt.content"));
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.project"));
        contentBuf.append(": ");
        String projectname = "?";
        try {
            projectname = cms.readTask(task.getRoot()).getName();
        }
        catch(Exception exc) {


        // no root?!
        }
        contentBuf.append(projectname);
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.task"));
        contentBuf.append(": ");
        contentBuf.append(task.getName());
        int projectid = cms.readProject(task).getId();
        contentBuf.append("\n\n\n" + getTaskUrl(cms, task.getId(), projectid));
        String subject = lang.getLanguageValue("task.email.reakt.subject") + " " + CmsUser.getFullName(cms.readUser(task.getAgentUser())) + " / " + roleName;
        CmsUser[] users =  {
            cms.readAgent(task)
        };
        org.opencms.util.CmsMail mail;
        mail = new org.opencms.util.CmsMail(cms, cms.readOwner(task), users, subject, contentBuf.toString(), "text/plain");

        // if "Alle Rollenmitglieder von Aufgabe Benachrichtigen" checkbox is selected.
        if(cms.getTaskPar(task.getId(), C_TASKPARA_ALL) != null) {
            mail = new org.opencms.util.CmsMail(cms, cms.readOwner(task), cms.readGroup(task),
                    subject, contentBuf.toString(), "text/plain");
        }
        try {
            mail.start();
        }
        catch(Exception exc) {
            if(OpenCms.getLog(CmsTaskAction.class).isWarnEnabled()) {
                OpenCms.getLog(CmsTaskAction.class).warn("Error while sending task mail", exc);
            }
        }
    }

    /**
     * Takes a task. The calling user is now the agent for this task.
     * @param cms The cms-object.
     * @param int taskid The id of the task.
     * @throws CmsException Throws CmsExceptions, that are be
     * thrown in calling methods.
     */

    public static void take(CmsObject cms, int taskid) throws CmsException {
        CmsXmlLanguageFile lang = new CmsXmlLanguageFile(cms);
        CmsRequestContext context = cms.getRequestContext();
        CmsTask task = cms.readTask(taskid);
        CmsUser newEditor = context.currentUser();
        CmsGroup oldRole = cms.readGroup(task);

        // has the user the correct role?
        if(cms.userInGroup(newEditor.getName(), oldRole.getName())) {
            cms.forwardTask(taskid, oldRole.getName(), newEditor.getName());
            cms.acceptTask(taskid);
            String comment = lang.getLanguageValue("task.dialog.take.logmessage");
            comment += " " + CmsUser.getFullName(newEditor);
            cms.writeTaskLog(taskid, comment, C_TASKLOGTYPE_TAKE);
        }

        // send an email
        StringBuffer contentBuf = new StringBuffer(lang.getLanguageValue("task.email.take.content"));
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.project"));
        contentBuf.append(": ");
        String projectname = "?";
        try {
            projectname = cms.readTask(task.getRoot()).getName();
        }
        catch(Exception exc) {


        // no root?!
        }
        contentBuf.append(projectname);
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.task"));
        contentBuf.append(": ");
        contentBuf.append(task.getName());
        contentBuf.append("\n");
        contentBuf.append(lang.getLanguageValue("task.label.taskfor"));
        contentBuf.append(": ");
        contentBuf.append(CmsUser.getFullName(cms.readAgent(task)));
        int projectid = cms.readProject(task).getId();
        contentBuf.append("\n\n\n" + getTaskUrl(cms, task.getId(), projectid));
        String subject = lang.getLanguageValue("task.email.take.subject") + " " + CmsUser.getFullName(newEditor);
        CmsUser[] users =  {
            cms.readAgent(task)
        };
        try {
            org.opencms.util.CmsMail mail = new org.opencms.util.CmsMail(cms, cms.readOwner(task),
                    users, subject, contentBuf.toString(), "text/plain");
            mail.start();
        }
        catch(Exception exc) {
            if(OpenCms.getLog(CmsTaskAction.class).isWarnEnabled()) {
                OpenCms.getLog(CmsTaskAction.class).warn("Error while sending task mail", exc);
            }
        }
    }

    public static String getTaskUrl(CmsObject cms, int taskid, int projectid) throws CmsException {
        String servletPath = CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getServletUrl();
        String serverName = ((HttpServletRequest)CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest()).getServerName();
        String scheme =  ((HttpServletRequest)CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest()).getScheme();
        int serverPort = ((HttpServletRequest)CmsXmlTemplateLoader.getRequest(cms.getRequestContext()).getOriginalRequest()).getServerPort();
        if(serverPort != 80) {
            serverName += ":" + serverPort;
        }
        return scheme + "://" + serverName + servletPath + C_VFS_PATH_SYSTEM + "login/index.html?startTaskId="
                    + taskid + "&startProjectId=" + projectid;
    }
}
