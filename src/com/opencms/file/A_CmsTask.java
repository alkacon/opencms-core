/*
 * File   : $Source: /alkacon/cvs/opencms/src/com/opencms/file/Attic/A_CmsTask.java,v $
 * Date   : $Date: 2000/02/20 16:59:00 $
 * Version: $Revision: 1.10 $
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

package com.opencms.file;

import java.util.*;


/**
 * This abstract class describes a task in the Cms.
 * 
 * @author Ruediger Gutfleisch
 * @version $Revision: 1.10 $ $Date: 2000/02/20 16:59:00 $
 */
public abstract class A_CmsTask
{
	/**
	 * Returns the id of this task.
	 * 
	 * @return the id of this task.
	 */
	abstract public int getId();
	
	/**
	 * Returns the name of this task.
	 * 
	 * @return the name of this task.
	 */
	abstract public String getName();
	

	/**
	 * Returns the state of this task.
	 * 
	 * @return the state of this task.
	 */
	abstract public int getState();
	
	/**
	 * Returns the state of this task as an String.
	 * 
	 * @return the state of this task.
	 */
	abstract public String getStateString();
	
	/**
	 * Returns the type of this task.
	 * 
	 * @return the type of this task.
	 */
	abstract public int getTaskType();
	
	/**
	 * Returns the root id of this task.
	 * 
	 * @return the root id of this task.
	 */
	abstract public int getRoot();
	
	/**
	 * Returns the parent id of this task.
	 * 
	 * @return the parent id of this task.
	 */
	abstract public int getParent();
	
	/**
	 * Returns the initiator user id of this task.
	 * 
	 * @return the initiator user id of this task.
	 */
	abstract public int getInitiatorUser();
	
	/**
	 * Returns the role group id of this task.
	 * 
	 * @return the role group id of this task.
	 */
	abstract public int getRole();
	
	/**
	 * Returns the agent user id of this task.
	 * 
	 * @return the agent user id of this task.
	 */
	abstract public int getAgentUser();
	
	/**
	 * Returns the original agent user id of this task.
	 * 
	 * @return the original agent user id of this task.
	 */
	abstract public int getOriginalUser();
	
	/**
	 * Returns the starttime of this task.
	 * 
	 * @return the starttime of this task.
	 */
	abstract public java.sql.Timestamp getStartTime();
	
	/**
	 * Returns the wakeuptime of this task.
	 * 
	 * @return the wakeuptime of this task.
	 */
	abstract public java.sql.Timestamp getWakeupTime();
	
	/**
	 * Returns the timeout of this task.
	 * 
	 * @return the timeout of this task.
	 */
	abstract public java.sql.Timestamp getTimeOut();
	
	/**
	 * Returns the endtime of this task.
	 * 
	 * @return the endtime of this task.
	 */
	abstract public java.sql.Timestamp getEndTime();
	
	/**
	 * Returns the percentage of this task.
	 * 
	 * @return the percentage of this task.
	 */
	abstract public int getPercentage();
	
	/**
	 * Returns the permission of this task.
	 * 
	 * @return the permission of this task.
	 */
	abstract public String getPermission();
	
	/**
	 * Returns the priority of this task.
	 * 
	 * @return the priority of this task.
	 */
	abstract public int getPriority();
	
	/**
	 * Returns the escalationtype of this task.
	 * 
	 * @return the escalationtype of this task.
	 */	
	abstract public int getEscalationType();
	
	/**
	 * Returns the htmllink of this task.
	 * 
	 * @return the htmllink of this task.
	 */	
	abstract public String getHtmlLink();
	
	/**
	 * Returns the milestone id of this task.
	 * 
	 * @return the milestone id of this task.
	 */
	abstract public int getMilestone();
	
	/**
	 * Returns the autofinish flag id of this task.
	 * 
	 * @return the autofinish flag id of this task.
	 */
	abstract public int getAutoFinish();

	abstract void setName(String taskname);
	abstract void setRoot(int root);
	abstract void setParent(int parent);
	abstract void setTaskType(int tasktype);
	abstract void setRole(int role);
	abstract void setAgentUser(int agentuser);
	abstract void setOriginalUser(int originaluser);
	abstract void setInitiatorUser(int initiatoruser);
	abstract void setWakeupTime(java.sql.Timestamp wakeuptime);
	abstract void setTimeOut(java.sql.Timestamp timeout);
	abstract void setPriority(int priority);
	abstract void setPercentage(int percentage);
	abstract void setState(int state);
	abstract void setStartTime(java.sql.Timestamp starttime);
	abstract void setMilestone(int milestone);

	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	abstract public String toString();
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
	abstract public boolean equals(Object obj);

}
