package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This abstract class describes a resource broker for tasks in the Cms.<BR/>
 * <B>All</B> Methods get a first parameter: A_CmsUser. It is the current user. This 
 * is for security-reasons, to check if this current user has the rights to call the
 * method.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.2 $ $Date: 1999/12/13 16:34:38 $
 */
abstract class A_CmsRbTask { 	
	
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
	abstract Vector readTasks(A_CmsUser callingUSer, A_CmsProject project, int orderBy)
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
	abstract Vector readOpenTasks(A_CmsUser callingUSer, A_CmsProject project, int orderBy)
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
	abstract Vector readDoneTasks(A_CmsUser callingUSer, A_CmsProject project, int orderBy)
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
	abstract Vector readTasks(A_CmsUser callingUSer, A_CmsProject project, 
					 A_CmsUser user, int orderBy) 
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
	abstract Vector readOpenTasks(A_CmsUser callingUSer, A_CmsProject project, 
						 A_CmsUser user, int orderBy)
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
	abstract Vector readPosedTasks(A_CmsUser callingUSer, A_CmsProject project, 
						  A_CmsUser user, int orderBy) 
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
	abstract Vector readDoneTasks(A_CmsUser callingUSer, A_CmsProject project, 
						 A_CmsUser user, int orderBy) 
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
	abstract Vector readTasks(A_CmsUser callingUSer, A_CmsProject project, 
					 A_CmsGroup group, int orderBy) 
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
	abstract Vector readOpenTasks(A_CmsUser callingUSer, A_CmsProject project, 
						 A_CmsGroup group, int orderBy) 
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
	abstract Vector readDoneTasks(A_CmsUser callingUSer, A_CmsProject project, 
						 A_CmsGroup group, int orderBy) 
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
	abstract void writeTask(A_CmsUser callingUSer, A_CmsTask task) 
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
	abstract A_CmsTask createTask(A_CmsUser callingUSer, A_CmsProject project, 
						 A_CmsUser user /* add parameters here */ ) 
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
	abstract void forwardTask(A_CmsUser callingUSer, A_CmsTask task, 
					 A_CmsUser newUser) 
		throws CmsException;	
}
