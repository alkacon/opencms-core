package com.opencms.file;

import java.util.*;


/**
 * This abstract class describes a task in the Cms.
 * 
 * @author Ruediger Gutfleisch
 * @version $Revision: 1.5 $ $Date: 2000/01/25 19:11:55 $
 */
public abstract class A_CmsTask
{
	
	/**
	 * state values of a task
	 */
    static final int C_TASK_STATE_PREPARE  = 0;
    static final int C_TASK_STATE_START	   = 1;
    static final int C_TASK_STATE_STARTED  = 2;
    static final int C_TASK_STATE_NOTENDED = 3;
    static final int C_TASK_STATE_ENDED	   = 4;
    static final int C_TASK_STATE_HALTED   = 5;
    static final int C_TASK_STATE_MAIL	   = 7;
	static final int C_TASK_STATE_ONHOLD   = 8; 

	
	/**
	 * Returns the id of this task.
	 * 
	 * @return the id of this task.
	 */
	abstract int getId();
	
	/**
	 * Returns the name of this task.
	 * 
	 * @return the name of this task.
	 */
	abstract String getName();
	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	
	abstract int getState();
	abstract int getTaskType();
	abstract int getRoot();
	abstract int getParent();
	abstract int getInitiatorUser();
	abstract int getRole();
	abstract int getAgentUser();
	abstract int getOriginalUser();
	abstract java.sql.Timestamp getStartTime();
	abstract java.sql.Timestamp getWakeupTime();
	abstract java.sql.Timestamp getTimeOut();
	abstract java.sql.Timestamp getEndTime();
	abstract int getPercentage();
	abstract String getPermission();
	abstract int getPriority();
	abstract int getEscalationType();
	abstract String getHtmlLink();
	abstract int getMilestone();
	abstract int getAutoFinish();

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
