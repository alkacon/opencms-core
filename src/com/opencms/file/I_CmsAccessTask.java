package com.opencms.file;

import java.util.*;

import com.opencms.core.*;

/**
 * This interface describes the access to projects in the Cms.<BR/>
 * 
 * All methods have package-visibility for security-reasons.
 * 
 * @author Andreas Schouten
 * @version $Revision: 1.1 $ $Date: 2000/01/11 17:41:33 $
 */
interface I_CmsAccessTask {

	/**
	 * Reads a task from the Cms.
	 * 
	 * @param id The id of the task to read.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsTask readTask(int id)
		throws CmsException;
	
	/**
	 * Creates a new task.
	 * 
	 * @param projectid Project id to witch the task belongs.
	 * @param owner User who hast created the task 
	 * @param agent User who will edit the task 
	 * @param role Usergroup for the task
	 * @param taskname Name of the task
	 * @param taskcomment Description of the task
	 * @param timeout Time when the task must finished
	 * @param priority Id for the priority
	 * 
	 * @return A new Task Object
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 
	 public A_CmsTask createTask(int projectid, A_CmsUser owner, A_CmsUser agent, A_CmsGroup role, 
								String taskname, String taskcomment, 
								java.sql.Timestamp timeout, int priority)
		throws CmsException;
	
	/**
	 * Updates a task.
	 * 
	 * @param project The project that will be written.
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public A_CmsTask writeTask(A_CmsTask task)
		throws CmsException;
	 
	 
	 /**
	 * Ends a task from the Cms.
	 * 
	 * @param task The task to end.
	 * @param user The user who ends the task
	 * 
	 * @exception CmsException Throws CmsException if something goes wrong.
	 */
	 public int endTask(A_CmsTask task, A_CmsUser user)
		 throws CmsException;
}
