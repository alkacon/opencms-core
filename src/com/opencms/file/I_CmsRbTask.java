package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This interface describes a resourcebroker for tasks in the Cms. All 
 * resourcebroking-methods have the package visibility for security reasons.
 * The MhtObject uses this resourcebrokers to get acess to all tasks.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 1999/12/06 13:57:08 $
 */
public interface I_CmsRbTask { 	
	
	/**
	 * Reads all tasks for a project.
	 * 
	 * @param project The Project in which the tasks are defined.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readTasks(I_CmsProject project, int orderBy) throws CmsException;
	
	/**
	 * Reads all open tasks for a project.
	 * 
	 * @param project The Project in which the tasks are defined.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readOpenTasks(I_CmsProject project, int orderBy) throws CmsException;
	
	/**
	 * Reads all done tasks for a project.
	 * 
	 * @param project The Project in which the tasks are defined.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readDoneTasks(I_CmsProject project, int orderBy) throws CmsException;
	
	/**
	 * Reads all tasks for a user in a project.
	 * 
	 * @param project The Project in which the tasks are defined.
	 * @param user The user who has to process the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readTasks(I_CmsProject project, I_CmsUser user, int orderBy) throws CmsException;
	
	/**
	 * Reads all open tasks for a user in a project.
	 * 
	 * @param project The Project in which the tasks are defined.
	 * @param user The user who has to process the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readOpenTasks(I_CmsProject project, I_CmsUser user, int orderBy) throws CmsException;
	
	/**
	 * Reads all posed tasks of a user in a project.
	 * 
	 * @param project The Project in which the tasks are defined.
	 * @param user The user who has posed the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readPosedTasks(I_CmsProject project, I_CmsUser user, int orderBy) throws CmsException;

	/**
	 * Reads all done tasks of a group in a project.
	 * 
	 * @param project The Project in which the tasks are defined.
	 * @param group The group who has processed the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readDoneTasks(I_CmsProject project, I_CmsUser user, int orderBy) throws CmsException;

	/**
	 * Reads all tasks of a group in a project.
	 * 
	 * @param project The Project in which the tasks are defined.
	 * @param group The group who has to process the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readTasks(I_CmsProject project, I_CmsGroup group, int orderBy) throws CmsException;
	
	/**
	 * Reads all open tasks of a group in a project.
	 * 
	 * @param project The Project in which the tasks are defined.
	 * @param group The group who has to process the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readOpenTasks(I_CmsProject project, I_CmsGroup group, int orderBy) throws CmsException;
	
	/**
	 * Reads all done tasks of a group in a project.
	 * 
	 * @param project The Project in which the tasks are defined.
	 * @param group The group who has to process the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readDoneTasks(I_CmsProject project, I_CmsGroup group, int orderBy) throws CmsException;
	
	/**
	 * Writes the task.
	 * 
	 * @param task The task to write.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	void writeTask(I_CmsTask task) throws CmsException;
	
	/**
	 * Creates a new task.
	 * 
	 * @param project The Project in which the tasks will be defined.
	 * @param user The user who creates the task.
	 * // TODO: add additional parameters here
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	I_CmsTask createTask(I_CmsProject project, I_CmsUser user /* add parameters here */ ) throws CmsException;
		
	/**
	 * Forwrds a task to a new user.
	 * 
	 * @param task The task to forward.
	 * @param newUser The user to forward to.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	void forwardTask(I_CmsTask task, I_CmsUser newUser) throws CmsException;	
	
	/**
	 * Returns a string-representation for this object.
	 * This can be used for debugging.
	 * 
	 * @return string-representation for this object.
	 */
	public String toString();
	
	/**
	 * Compares the overgiven object with this object.
	 * 
	 * @return true, if the object is identically else it returns false.
	 */
    public boolean equals(Object obj);

	/**
	 * Returns the hashcode for this object.
	 * 
	 * @return the hashcode for this object.
	 */
    public int hashCode();    
}
