package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This interface describes a resource broker for tasks in the Cms.<BR/>
 * <B>All</B> Methods get a first parameter: I_CmsUser. It is the current user. This 
 * is for security-reasons, to check if this current user has the rights to call the
 * method.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.3 $ $Date: 1999/12/10 11:10:23 $
 */
public interface I_CmsRbTask { 	
	
	/**
	 * Reads all tasks for a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readTasks(I_CmsUser callingUSer, I_CmsProject project, int orderBy)
		throws CmsException;
	
	/**
	 * Reads all open tasks for a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readOpenTasks(I_CmsUser callingUSer, I_CmsProject project, int orderBy)
		throws CmsException;
	
	/**
	 * Reads all done tasks for a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readDoneTasks(I_CmsUser callingUSer, I_CmsProject project, int orderBy)
		throws CmsException;
	
	/**
	 * Reads all tasks for a user in a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined.
	 * @param user The user who has to process the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readTasks(I_CmsUser callingUSer, I_CmsProject project, 
					 I_CmsUser user, int orderBy) 
		throws CmsException;
	
	/**
	 * Reads all open tasks for a user in a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined.
	 * @param user The user who has to process the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readOpenTasks(I_CmsUser callingUSer, I_CmsProject project, 
						 I_CmsUser user, int orderBy)
		throws CmsException;
	
	/**
	 * Reads all posed tasks of a user in a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined.
	 * @param user The user who has posed the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readPosedTasks(I_CmsUser callingUSer, I_CmsProject project, 
						  I_CmsUser user, int orderBy) 
		throws CmsException;

	/**
	 * Reads all done tasks of a group in a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined.
	 * @param group The group who has processed the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readDoneTasks(I_CmsUser callingUSer, I_CmsProject project, 
						 I_CmsUser user, int orderBy) 
		throws CmsException;

	/**
	 * Reads all tasks of a group in a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined.
	 * @param group The group who has to process the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readTasks(I_CmsUser callingUSer, I_CmsProject project, 
					 I_CmsGroup group, int orderBy) 
		throws CmsException;
	
	/**
	 * Reads all open tasks of a group in a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined.
	 * @param group The group who has to process the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readOpenTasks(I_CmsUser callingUSer, I_CmsProject project, 
						 I_CmsGroup group, int orderBy) 
		throws CmsException;
	
	/**
	 * Reads all done tasks of a group in a project.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks are defined.
	 * @param group The group who has to process the task.
	 * @param orderBy Chooses, how to order the tasks.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	Vector readDoneTasks(I_CmsUser callingUSer, I_CmsProject project, 
						 I_CmsGroup group, int orderBy) 
		throws CmsException;
	
	/**
	 * Writes the task.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param task The task to write.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	void writeTask(I_CmsUser callingUSer, I_CmsTask task) 
		throws CmsException;
	
	/**
	 * Creates a new task.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param project The Project in which the tasks will be defined.
	 * @param user The user who creates the task.
	 * // TODO: add additional parameters here
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	I_CmsTask createTask(I_CmsUser callingUSer, I_CmsProject project, 
						 I_CmsUser user /* add parameters here */ ) 
		throws CmsException;
		
	/**
	 * Forwrds a task to a new user.
	 * 
	 * <B>Security:</B>
	 * // TODO: Add security-police here
	 * 
	 * @param callingUser The user who wants to use this method.
	 * @param task The task to forward.
	 * @param newUser The user to forward to.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	void forwardTask(I_CmsUser callingUSer, I_CmsTask task, 
					 I_CmsUser newUser) 
		throws CmsException;	
}
