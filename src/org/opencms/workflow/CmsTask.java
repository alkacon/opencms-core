/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workflow/Attic/CmsTask.java,v $
 * Date   : $Date: 2005/06/21 15:50:00 $
 * Version: $Revision: 1.8 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workflow;

import org.opencms.main.I_CmsConstants;
import org.opencms.util.CmsUUID;

/**
 * Describes an OpenCms task.<p>
 * 
 * @author  Alexander Kandzior (a.kandzior@alkacon.com)
 * @version $Revision: 1.8 $
 */
public class CmsTask {

    /** The id of the user who is the agent of this task. */
    private CmsUUID m_agentUserId;

    /** The auto finish value of this task. */
    private int m_autoFinish;

    /** Timestamp when the task has been ended. */
    private java.sql.Timestamp m_endTime;

    /** Escalation type of this task. */
    private int m_escalationType;

    /** Link to the html page which handles this task. */
    private String m_htmlLink;

    /** The id of this task. */
    private int m_id;

    /** The id of the user who initiated this task. */
    private CmsUUID m_initiatorUserId;

    /** The id of the milestone to which this task belongs. */
    private int m_milestone;

    /** The name of this task. */
    private String m_name;

    /** The id of the user who was the original agent. */
    private CmsUUID m_originalUserId;

    /** The id of the task which is the parent of this task. */
    private int m_parent;

    /** Percentage value of this task. */
    private int m_percentage;

    /** Permission flag of this task. */
    private String m_permission;

    /** Priority of this task. */
    private int m_priority;

    /** The id of the role which is set for this task. */
    private CmsUUID m_roleId;

    /** The id of the task which is the root task of this task. */
    private int m_root;

    /** Timestamp when this task has been started. */
    private java.sql.Timestamp m_startTime;

    /** State of this task. */
    private int m_state;

    /** Type of this task. */
    private int m_taskType;

    /** Timestamp when this task has to be completed. */
    private java.sql.Timestamp m_timeOut;

    /** Timestamp when this task has to be activated. */
    private java.sql.Timestamp m_wakeupTime;

    /**
     * Creates a new CmsTask object with default values for all members.<p>
     */
    public CmsTask() {

        m_id = I_CmsConstants.C_UNKNOWN_ID;
        m_name = null;
        m_state = 0;
        m_taskType = I_CmsConstants.C_UNKNOWN_ID;
        m_root = I_CmsConstants.C_UNKNOWN_ID;
        m_parent = I_CmsConstants.C_UNKNOWN_ID;
        m_initiatorUserId = CmsUUID.getNullUUID();
        m_roleId = CmsUUID.getNullUUID();
        m_agentUserId = CmsUUID.getNullUUID();
        m_originalUserId = CmsUUID.getNullUUID();
        m_startTime = null;
        m_wakeupTime = null;
        m_timeOut = null;
        m_endTime = null;
        m_percentage = 0;
        m_permission = "-rw-rw-rw";
        m_priority = I_CmsConstants.C_UNKNOWN_ID;
        m_escalationType = 0;
        m_htmlLink = null;
        m_milestone = I_CmsConstants.C_UNKNOWN_ID;
        m_autoFinish = 0;
    }

    /**
     * Creates a new CmsTask object.<p>
     *
     * @param id the id of this task
     * @param name the name of this task
     * @param state state of this task
     * @param taskType type of this task
     * @param root the id of the task which is the root task of this task
     * @param parent the id of the task which is the parent of this task
     * @param initiatorUserId the id of the user who initiated this task
     * @param roleId the id of the role which is set for this task
     * @param agentUserId the id of the user who is the agent of this task
     * @param originalUserId the id of the user who was the original agent
     * @param startTime timestamp when this task has been started
     * @param wakeupTime timestamp when this task has to be activated
     * @param timeOut timestamp when this task has to be completed
     * @param endTime timestamp when the task has been ended
     * @param percentage percentage value of this task
     * @param permission permission flag of this task
     * @param priority priority of this task
     * @param escalationType escalation type of this task
     * @param htmlLink link to the html page which handles this task
     * @param milestone the id of the milstone to which this task belongs
     * @param autofinish the auto finish value of this task
     */
    public CmsTask(
        int id,
        String name,
        int state,
        int taskType,
        int root,
        int parent,
        CmsUUID initiatorUserId,
        CmsUUID roleId,
        CmsUUID agentUserId,
        CmsUUID originalUserId,
        java.sql.Timestamp startTime,
        java.sql.Timestamp wakeupTime,
        java.sql.Timestamp timeOut,
        java.sql.Timestamp endTime,
        int percentage,
        String permission,
        int priority,
        int escalationType,
        String htmlLink,
        int milestone,
        int autofinish) {

        m_id = id;
        m_name = name;
        m_state = state;
        m_taskType = taskType;
        m_root = root;
        m_parent = parent;
        m_initiatorUserId = initiatorUserId;
        m_roleId = roleId;
        m_agentUserId = agentUserId;
        m_originalUserId = originalUserId;
        m_startTime = startTime;
        m_wakeupTime = wakeupTime;
        m_timeOut = timeOut;
        m_endTime = endTime;
        m_percentage = percentage;
        m_permission = permission;
        m_priority = priority;
        m_escalationType = escalationType;
        m_htmlLink = htmlLink;
        m_milestone = milestone;
        m_autoFinish = autofinish;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (obj instanceof CmsTask) {
            return ((CmsTask)obj).m_id == m_id;
        }
        return false;
    }

    /**
     * Returns the agent user id of this task.<p>
     * 
     * @return the agent user id of this task
     */
    public CmsUUID getAgentUser() {

        return m_agentUserId;
    }

    /**
     * Returns the autofinish flag of this task.<p>
     * 
     * @return the autofinish flag of this task
     */
    public int getAutoFinish() {

        return m_autoFinish;
    }

    /**
     * Returns the endtime of this task.<p>
     * 
     * @return the endtime of this task
     */
    public java.sql.Timestamp getEndTime() {

        return m_endTime;
    }

    /**
     * Returns the escalation type of this task.<p>
     * 
     * @return the escalation type of this task
     */
    public int getEscalationType() {

        return m_escalationType;
    }

    /**
     * Returns the html link of this task.<p>
     * 
     * @return the htmllink of this task
     */
    public String getHtmlLink() {

        return m_htmlLink;
    }

    /**
     * Returns the id of this task.<p>
     * 
     * @return the id of this task
     */
    public int getId() {

        return m_id;
    }

    /**
     * Returns the initiator user id of this task.<p>
     * 
     * @return the initiator user id of this task
     */
    public CmsUUID getInitiatorUser() {

        return m_initiatorUserId;
    }

    /**
     * Returns the milestone value of this task.<p>
     * 
     * @return the milestone value of this task
     */
    public int getMilestone() {

        return m_milestone;
    }

    /**
     * Returns the name of this task.<p>
     * 
     * @return the name of this task
     */
    public String getName() {

        return m_name;
    }

    /**
     * Returns the original agent user id of this task.<p>
     * 
     * @return the original agent user id of this task
     */
    public CmsUUID getOriginalUser() {

        return m_originalUserId;
    }

    /**
     * Returns the parent id of this task.<p>
     * 
     * @return the parent id of this task
     */
    public int getParent() {

        return m_parent;
    }

    /**
     * Returns the percentage of this task.<p>
     * 
     * @return the percentage of this task
     */
    public int getPercentage() {

        return m_percentage;
    }

    /**
     * Returns the permission of this task.<p>
     * 
     * @return the permission of this task
     */
    public String getPermission() {

        return m_permission;
    }

    /**
     * Returns the priority of this task.<p>
     * 
     * @return the priority of this task
     */
    public int getPriority() {

        return m_priority;
    }

    /**
     * Returns the role group id of this task.<p>
     * 
     * @return the role group id of this task
     */
    public CmsUUID getRole() {

        return m_roleId;
    }

    /**
     * Returns the root id of this task.<p>
     * 
     * @return the root id of this task
     */
    public int getRoot() {

        return m_root;
    }

    /**
     * Returns the starttime of this task.<p>
     * 
     * @return the starttime of this task
     */
    public java.sql.Timestamp getStartTime() {

        return m_startTime;
    }

    /**
     * Returns the state of this task.<p>
     * 
     * @return the state of this task
     */
    public int getState() {

        return m_state;
    }

    /**
     * Returns the state of this task as String.<p>
     * 
     * @return the state of this task as String
     */
    public String getStateString() {

        return State2String(m_state);
    }

    /**
     * Returns the type of this task.<p>
     * 
     * @return the type of this task
     */
    public int getTaskType() {

        return m_taskType;
    }

    /**
     * 
     * Returns the timeout date of this task.<p>
     * 
     * @return the timeout date of this task
     */
    public java.sql.Timestamp getTimeOut() {

        return m_timeOut;
    }

    /**
     * Returns the wakeup time of this task.<p>
     * 
     * @return the wakeup time of this task
     */
    public java.sql.Timestamp getWakeupTime() {

        return m_wakeupTime;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {

        return (new Integer(m_id)).hashCode();
    }

    /**
     * Sets the agent user id for this task.<p>
     * 
     * @param agentUserId the agent user id for this task
     */
    public void setAgentUser(CmsUUID agentUserId) {

        m_agentUserId = agentUserId;
    }

    /**
     * Sets the initiator user id for this task.<p>
     * 
     * @param initiatorUserId the initiator user id for this task
     */
    public void setInitiatorUser(CmsUUID initiatorUserId) {

        m_initiatorUserId = initiatorUserId;
    }

    /**
     * Sets the milestone value of this task.<p>
     * 
     * @param milestone the milestone value of this task
     */
    public void setMilestone(int milestone) {

        m_milestone = milestone;
    }

    /**
     * Sets the name of this task.<p>
     * 
     * @param taskname the name of this task
     */
    public void setName(String taskname) {

        m_name = taskname;
    }

    /**
     * Sets the original user id of this task.<p>
     * 
     * @param originalUserId the original user id of this task
     */
    public void setOriginalUser(CmsUUID originalUserId) {

        m_originalUserId = originalUserId;
    }

    /**
     * Sets the parent id of this task.<p>
     * 
     * @param parent the parent id of this task
     */
    public void setParent(int parent) {

        m_parent = parent;
    }

    /**
     * Sets the percentage for this task.<p>
     * 
     * @param percentage the percentage for this task
     */
    public void setPercentage(int percentage) {

        m_percentage = percentage;
    }

    /**
     * Sets the priority for this task.<p>
     * 
     * @param priority the priority for this task
     */
    public void setPriority(int priority) {

        m_priority = priority;
    }

    /**
     * Sets the role id for this task.<p>
     * 
     * @param roleId the role id for this task
     */
    public void setRole(CmsUUID roleId) {

        m_roleId = roleId;
    }

    /**
     * Sets the root value for this task.<p>
     * 
     * @param root the root value for this task
     */
    public void setRoot(int root) {

        m_root = root;
    }

    /**
     * Sets the start time of this task.<p>
     * 
     * @param starttime the start time of this task
     */
    public void setStartTime(java.sql.Timestamp starttime) {

        m_startTime = starttime;
    }

    /**
     * Sets the state of this task.<p>
     * 
     * @param state the state of this task
     */
    public void setState(int state) {

        m_state = state;
    }

    /**
     * Sets the type of this task.<p>
     * 
     * @param tasktype the type of this task
     */
    public void setTaskType(int tasktype) {

        m_taskType = tasktype;
    }

    /**
     * Sets the timeout value for this task.<p>
     * 
     * @param timeout the timeout value for this task
     */
    public void setTimeOut(java.sql.Timestamp timeout) {

        m_timeOut = timeout;
    }

    /**
     * Sets the wakeup time for this task.<p>
     * 
     * @param wakeuptime the wakeup time for this task
     */
    public void setWakeupTime(java.sql.Timestamp wakeuptime) {

        m_wakeupTime = wakeuptime;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("[Task]");
        result.append(" id:");
        result.append(this.getId());
        result.append(" name:");
        result.append(this.getName());
        result.append(" root:");
        result.append(this.getRoot());
        result.append(" state:");
        result.append(this.getStateString());
        result.append(" owner:");
        result.append(this.getInitiatorUser());
        result.append(" agent:");
        result.append(this.getAgentUser());
        result.append(" role:");
        result.append(this.getRole());
        return result.toString();
    }

    /**
     * Returns a String representation for the task state.<p>
     * 
     * @param state the state to get a String representtion for 
     * @return a String representation for the task state
     */
    private String State2String(int state) {

        String result = null;

        switch (state) {
            case CmsTaskService.TASK_STATE_PREPARE:
                result = Messages.get().key(Messages.GUI_TASK_STATE_PREPARED_0);
                break;
            case CmsTaskService.TASK_STATE_START:
                result = Messages.get().key(Messages.GUI_TASK_STATE_START_0);
                break;
            case CmsTaskService.TASK_STATE_STARTED:
                result = Messages.get().key(Messages.GUI_TASK_STATE_STARTED_0);
                break;
            case CmsTaskService.TASK_STATE_NOTENDED:
                result = Messages.get().key(Messages.GUI_TASK_STATE_RUNNING_0);
                break;
            case CmsTaskService.TASK_STATE_ENDED:
                result = Messages.get().key(Messages.GUI_TASK_STATE_ENDED_0);
                break;
            case CmsTaskService.TASK_STATE_HALTED:
                result = Messages.get().key(Messages.GUI_TASK_STATE_HALTED_0);
                break;
            default:
                result = Messages.get().key(Messages.GUI_TASK_STATE_UNKNOWN_0);
        }
        return result;
    }
}
