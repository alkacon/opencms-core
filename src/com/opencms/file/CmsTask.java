package com.opencms.file;

/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/CmsTask.java,v $
 * Date   : $Date: 2000/08/08 14:08:23 $
 * Version: $Revision: 1.12 $
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

import com.opencms.core.*;
import java.util.*;


/**
 * This abstract class describes a task in the Cms.
 * 
 * @author Ruediger Gutfleisch
 * @version $Revision: 1.12 $ $Date: 2000/08/08 14:08:23 $
 */
public class CmsTask implements I_CmsConstants {

	/**
	 * The Id of the task.
	 */
	private int			m_Id = C_UNKNOWN_ID;

	/**
	 * The name of the task.
	 */
	private String		m_Name = null;
	
	/**
	 * The id of the user who initiated the task.
	 */
	private int			m_InitiatorUser = C_UNKNOWN_ID;
	
	/**
	 * The id of the user name who was the original agent.
	 */
	private int			m_OriginalUser = C_UNKNOWN_ID;
	
	/**
	 * The id of the user who is the agent of the task.
	 */
	private int			m_AgentUser = C_UNKNOWN_ID;
	
	/**
	 * The id of the role which has to do the task.
	 */
	private int			m_Role = C_UNKNOWN_ID;
	
	private int			m_AutoFinish = 0;
	
	/**
	 * Link to the html page which handle the task.
	 */
	private String		m_HtmlLink = null;

	/**
	 * The id of the Milstone to wich the task belongs.
	 */
	private int			m_Milestone = C_UNKNOWN_ID;
	
	/**
	 * The id of the task which is the root task of this task.
	 */
	private int			m_Root = C_UNKNOWN_ID;
	
	/**
	 * The id of the task which is the parent of this task.
	 */
	private int			m_Parent = C_UNKNOWN_ID;

	/**
	 * percentage value of the task.
	 */
	private int			m_Percentage = 0;
	
	/**
	 * Permissin flag of the task.
	 */
	private String		m_Permission = "-rw-rw-rw";
	
	/**
	 * State of the task.
	 */
	private int			m_State = 0;
	
	/**
	 * Type of the task.
	 */
	private int			m_TaskType = C_UNKNOWN_ID;
	
	/**
	 * Escalationtype of the task.
	 */
	private int			m_EscalationType = 0;
	
	/**
	 * Priority of the task.
	 */
	private int			m_Priority = C_UNKNOWN_ID;
	
	/**
	 * Timestamp when the task has been started.
	 */
	private java.sql.Timestamp m_StartTime = null;
	
	/**
	 * Timestamp when the task has been ended.
	 */
	private java.sql.Timestamp m_EndTime    = null;
	
	/**
	 * Timestamp when the task has to be completed.
	 */
	private java.sql.Timestamp m_TimeOut    = null;
	
	/**
	 * Timestamp when the task has to be activated.
	 */
	private java.sql.Timestamp m_WakeupTime = null;
	
	/**
	 * Constructor, creates a new CmsTask object.
	 */
	public CmsTask(){
	}
	/**
	 * Constructor, creates a new CmsTask object.
	 * 
	 * @param resourceName The name (including complete path) of the resouce.
	 * 
	 */
	public CmsTask(int id, String name, int state, int tasktype,
			int root, int parent, int initiatoruser,
			int role, int agentuser, int originaluser,
			java.sql.Timestamp starttime, java.sql.Timestamp wakeuptime,
			java.sql.Timestamp timeout, java.sql.Timestamp endtime,
			int percentage, String permission, int priority,
			int escalationtype, String htmllink, int milestone,int autofinish){

		m_Id = id;
		m_Name = name;
		m_State = state;
		m_TaskType = tasktype;
		m_Root = root;
		m_Parent = parent;
		m_InitiatorUser = initiatoruser;
		m_Role = role;
		m_AgentUser = agentuser;
		m_OriginalUser = originaluser;
		m_StartTime = starttime;
		m_WakeupTime = wakeuptime;
		m_TimeOut = timeout;
		m_EndTime = endtime;
		m_Percentage = percentage;
		m_Permission = permission;
		m_Priority = priority;
		m_EscalationType = escalationtype;
		m_HtmlLink = htmllink;
		m_Milestone = milestone;
		m_AutoFinish = autofinish;
	}
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
	public boolean equals(Object obj) {
		boolean equal=false;
		// check if the object is a CmsUser object
		if (obj instanceof CmsTask) {
			// same ID than the current Task Object?
			if (((CmsTask)obj).getId() == this.getId()){
				equal = true;
			}
		}
		return equal;
	}
	/**
	 * Returns the agent user id of this task.
	 * 
	 * @return the agent user id of this task.
	 */
	public int getAgentUser(){
		return m_AgentUser;
	}
	/**
	 * Returns the autofinish flag id of this task.
	 * 
	 * @return the autofinish flag id of this task.
	 */
	public int getAutoFinish(){
		return m_AutoFinish;
	}
	/**
	 * Returns the endtime of this task.
	 * 
	 * @return the endtime of this task.
	 */
	public java.sql.Timestamp getEndTime(){
		return m_EndTime;
	}
	/**
	 * Returns the escalationtype of this task.
	 * 
	 * @return the escalationtype of this task.
	 */	
	public int getEscalationType(){
		return m_EscalationType;
	}
	/**
	 * Returns the htmllink of this task.
	 * 
	 * @return the htmllink of this task.
	 */
	public String getHtmlLink(){
		return m_HtmlLink;
	}
	/**
	 * Returns the id of this task.
	 * 
	 * @return the id of this task.
	 */
	public int getId() {
		return m_Id; 
	}
	/**
	 * Returns the initiator user id of this task.
	 * 
	 * @return the initiator user id of this task.
	 */	
	public int getInitiatorUser(){
		return m_InitiatorUser;
	}
	/**
	 * Returns the milestone id of this task.
	 * 
	 * @return the milestone id of this task.
	 */
	public int getMilestone(){
		return m_Milestone;
	}
	/**
	 * Returns the name of this task.
	 * 
	 * @return the name of this task.
	 */
	public String getName() {
		return m_Name; 
	}
	/**
	 * Returns the original agent user id of this task.
	 * 
	 * @return the original agent user id of this task.
	 */	
	public int getOriginalUser(){
		return m_OriginalUser;
	}
	/**
	 * Returns the parent id of this task.
	 * 
	 * @return the parent id of this task.
	 */
	public int getParent(){
		return m_Parent;
	}
	/**
	 * Returns the percentage of this task.
	 * 
	 * @return the percentage of this task.
	 */
	public int getPercentage(){
		return m_Percentage;
	}
	/**
	 * Returns the permission of this task.
	 * 
	 * @return the permission of this task.
	 */	
	public String getPermission(){
		return m_Permission;
	}
	/**
	 * Returns the priority of this task.
	 * 
	 * @return the priority of this task.
	 */	
	public int getPriority(){
		return m_Priority;
	}
	/**
	 * Returns the role group id of this task.
	 * 
	 * @return the role group id of this task.
	 */
	public int getRole(){
		return m_Role;
	}
	/**
	 * Returns the root id of this task.
	 * 
	 * @return the root id of this task.
	 */
	public int getRoot(){
		return m_Root;
	}
	/**
	 * Returns the starttime of this task.
	 * 
	 * @return the starttime of this task.
	 */
	public java.sql.Timestamp getStartTime(){
		return m_StartTime;
	}
	/**
	 * Returns the state of this task.
	 * 
	 * @return the state of this task.
	 */
	public int getState() {
		return m_State;	
	}
	/**
	 * Returns the state of this task as an String.
	 * 
	 * @return the state of this task.
	 */
	public String getStateString() {
		return State2String(m_State);	
	}
	/**
	 * Returns the type of this task.
	 * 
	 * @return the type of this task.
	 */
	public int getTaskType(){
		return m_TaskType;
	}
	/**
	 * Returns the timeout of this task.
	 * 
	 * @return the timeout of this task.
	 */
	public java.sql.Timestamp getTimeOut(){
		return m_TimeOut;
	}
	/**
	 * Returns the wakeuptime of this task.
	 * 
	 * @return the wakeuptime of this task.
	 */
	public java.sql.Timestamp getWakeupTime(){
		return m_WakeupTime;
	}
	public void setAgentUser(int agentuser){
		m_AgentUser = agentuser;
	}
	public void setInitiatorUser(int initiatoruser){
		m_InitiatorUser = initiatoruser;
	}
	public void setMilestone(int milestone){
		m_Milestone = milestone;
	}
	public void setName(String taskname){
		m_Name = taskname;
	}
	public void setOriginalUser(int originaluser){
		m_OriginalUser = originaluser;
	}
	public void setParent(int parent){
		m_Parent = parent;
	}
	public void setPercentage(int percentage){
		m_Percentage = percentage;
	}
	public void setPriority(int priority){
		m_Priority = priority;
	}
	public void setRole(int role){
		m_Role = role;
	}
	public void setRoot(int root){
		m_Root = root;
	}
	public void setStartTime(java.sql.Timestamp starttime){
		m_StartTime = starttime;
	}
	public void setState(int state){
		m_State = state;
	}
	public void setTaskType(int tasktype){
		m_TaskType = tasktype;
	}
	public void setTimeOut(java.sql.Timestamp timeout){
		m_TimeOut = timeout;
	}
	public void setWakeupTime(java.sql.Timestamp wakeuptime){
		m_WakeupTime = wakeuptime;
	}
	private String State2String(int state)
	{
		String result = null;
		
		switch(state)
		{
		case C_TASK_STATE_PREPARE:
			{
				result = "Prepared";
				break;
			}
		case C_TASK_STATE_START:
			{
				result = "Start";
				break;
			}
		case C_TASK_STATE_STARTED:
			{
				result = "Started";
				break;				
			}
		case C_TASK_STATE_NOTENDED:
			{
				result = "Not Ended";
				break;				
			}
		case C_TASK_STATE_ENDED:
			{
				result = "Ended";
				break;				
			}
		case C_TASK_STATE_HALTED:
			{
				result = "Halted";
				break;				
			}

		default:
			result = "Unkown";
		}
		return result;
	}
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	public String toString() {
		StringBuffer output=new StringBuffer();
		output.append("[Task]:");
		output.append(" Id=");
		output.append(this.getId());
		output.append(" Name=");
		output.append(this.getName());
		output.append(" Root=");
		output.append(this.getRoot());
		output.append(" State=");
		output.append(this.getStateString());
		output.append(" Owner=");
		output.append(this.getInitiatorUser());
		output.append(" Agent=");
		output.append(this.getAgentUser());
		output.append(" Role=");
		output.append(this.getRole());
		
		return output.toString();
	}
}
