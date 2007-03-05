/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workflow/Attic/I_CmsWorkflowManager.java,v $
 * Date   : $Date: 2007/03/05 16:04:43 $
 * Version: $Revision: 1.1.2.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import org.opencms.db.CmsSecurityManager;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.security.I_CmsPrincipal;

import java.util.List;
import java.util.Locale;

/**
 * The workflow manager provides "high level" workflow functionality that connects to the OpenCms user iterface (GUI).<p>
 * 
 * An implementation of the {@link I_CmsWorkflowManager} usually provides "high level" workflow functionality 
 * such as operations required by a GUI. The collected information is then passed to the 
 * {@link I_CmsWorkflowEngine} which handles the "low level" workflow functionality like states and persistence.<p> 
 * 
 * @author Carsten Weinholz
 * 
 * @version $Revision: 1.1.2.4 $ 
 * 
 * @since 6.5.0 
 */
public interface I_CmsWorkflowManager {

    /**
     * Aborts a task, all changes on assigned resources are left.<p>
     * 
     * @param cms the cms object
     * @param wfProject the workflow project assigned to the task
     * @param message the additional message
     * @throws CmsException if aborting the task fails
     */
    void abortTask(CmsObject cms, CmsProject wfProject, String message) throws CmsException;

    /**
     * Adds a resource to a workflow project.<p>
     * 
     * @param cms the cms object
     * @param wfProject the workflow project
     * @param resource the resource to add
     * @throws CmsException if the resource cannot be added 
     * 
     */
    void addResource(CmsObject cms, CmsProject wfProject, CmsResource resource) throws CmsException;

    /**
     * Adds a resource to a workflow project.<p>
     * 
     * @param cms the cms object
     * @param wfProject the workflow project
     * @param resourcePath resource the resource to add
     * @throws CmsException if the resource cannot be added 
     */
    void addResource(CmsObject cms, CmsProject wfProject, String resourcePath) throws CmsException;

    /**
     * Creates a new empty workflow project used to collect resources for a workflow.<p>
     * 
     * @param cms the cms object
     * @param description short task description
     * @return a new empty workflow project
     * @throws CmsException if the project creation fails
     */
    CmsProject createTask(CmsObject cms, String description) throws CmsException;

    /**
     * Returns the group initially resonsible for workflows of the given type.<p>
     *  
     * @param type the workflow type
     * @return the group currently responsible
     * @throws CmsException if the agents cannot be retrieved
     */
    I_CmsPrincipal getAgent(I_CmsWorkflowType type) throws CmsException;

    /**
     * Returns the log for the given workflow project.<p>
     * 
     * The log consists of entries of type <code>I_CmsWorkflowLogEntry</code>.
     * 
     * @param wfProject the workflow project
     * @return the log for the given workflow project
     * @throws CmsException if the log cannot be retrieved
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getLog(CmsProject)
     */
    List getLog(CmsProject wfProject) throws CmsException;

    /**
     * Returns the group responsible for managing workflow projects.<p>
     * 
     * @param type the workflow type
     * @return the group responsible for managing projects of this type
     * @throws CmsException if the manager cannot be retrieved
     */
    I_CmsPrincipal getManager(I_CmsWorkflowType type) throws CmsException;

    /**
     * Returns the task of a resource.<p>
     * 
     * @param cms a cms object
     * @param resourcename name of the resource
     * @return the workflow project of the resource
     * @throws CmsException if the resource is not locked in a workflow project
     */
    CmsProject getTask(CmsObject cms, String resourcename) throws CmsException;

    /**
     * Returns the group responsible for the given workflow project in the current state.<p>
     * 
     * @param wfProject the workflow project
     * @return the group currently responsible
     * @throws CmsException if the agents cannot be retrieved
     */
    I_CmsPrincipal getTaskAgent(CmsProject wfProject) throws CmsException;

    /**
     * Returns the task description for the workflow project.<p>
     * 
     * @param wfProject the workflow project 
     * @return the task description
     * @throws CmsException if the description cannot be retrieved
     */
    String getTaskDescription(CmsProject wfProject) throws CmsException;

    /**
     * Returns the group responsible for managing the workflow project.<p>
     * 
     * @param wfProject the workflow project
     * @return the group currently responsible
     * @throws CmsException if the manager cannot be retrieved
     */
    I_CmsPrincipal getTaskManager(CmsProject wfProject) throws CmsException;

    /**
     * Returns the group or user who initiated the task of the given workflow project.
     * 
     * @param wfProject the workflow project
     * @return the group or user currently responsible
     * @throws CmsException if the owner cannot be retrieved
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getOwner(CmsProject)
     */
    I_CmsPrincipal getTaskOwner(CmsProject wfProject) throws CmsException;

    /**
     * Returns a List of all available tasks.<p>
     * 
     * The list contains instance of class <code>CmsProject</code> 
     * identifiying a workflow project.
     *  
     * @return all available tasks
     * @throws CmsException if the list of tasks cannot be retrieved
     */
    List getTasks() throws CmsException;

    /**
     * Returns a List of all available tasks for a distinct user.<p>
     * All task are returned, where the given <code>CmsUser</code>
     * is currently the agent or where he is a member of the current agent group.<p>
     *  
     * The list contains instance of class <code>CmsProject</code> 
     * identifiying a workflow project.
     * 
     * @param user the user
     * @return all available tasks with a distinct user
     * @throws CmsException if the list of tasks cannot be retrieved
     */
    List getTasksForAgent(CmsUser user) throws CmsException;

    /**
     * Returns a List of all available workflow projects with a distinct owner.<p>
     * All workflow projects are returned, where the given <code>CmsUser</code>
     * is directly the owner or where he is a member of the owners group.<p>
     *  
     * The list contains instance of class <code>CmsProject</code> 
     * identifiying a workflow project.
     * 
     * @param user the user
     * @return all available workflow projects with a distinct owner
     * @throws CmsException if the list of workflow projects cannot be retrieved
     */
    List getTasksForOwner(CmsUser user) throws CmsException;

    /**
     * Returns the start time of a workflow project.<p>
     * 
     * @param wfProject the workflow project
     * @return the start time of a workflow project
     * @throws CmsException if the start time cannot be retrieved
     */
    long getTaskStartTime(CmsProject wfProject) throws CmsException;

    /**
     * Returns the name of the current state.
     * 
     * @param wfProject the workflow project
     * @param locale the locale to use for the name of the state
     * @return the name of the current state
     * @throws CmsException if the state cannot be retrieved
     */
    String getTaskState(CmsProject wfProject, Locale locale) throws CmsException;

    /**
     * Returns the task type for the workflow project.<p>
     * 
     * @param wfProject the workflow project
     * @param locale the locale to use
     * @return the task type for the workflow project
     * @throws CmsException if the task type cannot be retrieved
     */
    String getTaskType(CmsProject wfProject, Locale locale) throws CmsException;

    /**
     * Gets a list of all transitions used by the workflow engine.<p>
     * 
     * @return all transitions used by the workflow engine
     * @throws CmsException if the list cannot be retrieved 
     */
    List getTransitions() throws CmsException;

    /**
     * Gets a list of possible transitions in the workflow.<p>
     * The list contains instances of I_CmsWorkflowTransition used to identify a transition.
     *  
     * @param wfProject the workflow project
     * @return a list of possible transitions
     * @throws CmsException if the list cannot be retrieved
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getTransitions(CmsProject)
     */
    List getTransitions(CmsProject wfProject) throws CmsException;

    /**
     * Returns the transition identified by an id.<p>
     * 
     * @param id the id
     * @return the transition identified by the id
     * @throws CmsException if something goes wrong
     */
    I_CmsWorkflowTransition getWorkflowTransition(String id) throws CmsException;

    /**
     * Returns the workflow type identified by an id.<p>
     * 
     * @param id the id
     * @return the workflow type identified by the id
     * @throws CmsException if something goes wrong
     */
    I_CmsWorkflowType getWorkflowType(String id) throws CmsException;

    /**
     * Returns a list of all available workflow types.<p>
     * 
     * The list contains instances of I_CmsWorkflowType used to identify the available workflow types. 
     * 
     * @return the list of all workflow types.
     * @throws CmsException if the list cannot be retrieved
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getWorkflowTypes()
     */
    List getWorkflowTypes() throws CmsException;

    /**
     * Returns a list of all available workflow types that can be utilized for the given resource.<p>
     * 
     * The list contains instances of I_CmsWorkflowType used to identify the available workflow types. 
     * 
     * @param cms the cms object
     * @param resource the resource
     * @return a list containing all available workflow types
     * @throws CmsException if the list cannot be retrieved
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getWorkflowTypes(String)
     */
    List getWorkflowTypes(CmsObject cms, CmsResource resource) throws CmsException;

    /**
     * Initializes a task for the workflow project.<p>
     * 
     * @param cms the cms opbject
     * @param wfProject the workflow project
     * @param type the workflow type
     * @param message the additional message
     * @throws CmsException if the workflow engine cannot initialize the workflow
     * @return the last action requested from the workflow engine
     * @see org.opencms.workflow.I_CmsWorkflowEngine#init(CmsProject, I_CmsWorkflowType, String)
     * 
     * TODO: remove return value
     */
    I_CmsWorkflowAction init(CmsObject cms, CmsProject wfProject, I_CmsWorkflowType type, String message)
    throws CmsException;

    /**
     * Initializes the workflow manager with the OpenCms system configuration.<p>
     * 
     * @param cms an OpenCms context object that must have been initialized with "Admin" permissions
     * @param securityManager the security manager instance
     */
    void initialize(CmsObject cms, CmsSecurityManager securityManager);

    /**
     * Checks if a resource assigned to a task is editable.<p>
     * 
     * First, it is checked, if the resource is already assigned to a workflow and
     * if it is locked in the workflow, but not for a distinct user, then it is checked,
     * if the user is allowed to edit the resource by asking the workflow engine. 
     * 
     * @param cms the current cms context
     * @param resource the resource to check
     * 
     * @return if a resource assigned to a task is editable
     */
    boolean isEditableInWorkflow(CmsObject cms, CmsResource resource);

    /**
     * Checks if a resource assigned to a task is lockable for the given user.<p>
     * 
     * Returns <code>true</code> if the resource is assigned to a workflow task,
     * and if the user is a manager for this task or optionally an agent.
     * 
     * @param user the user to check lockability for
     * @param lock the current lock on the resource to check
     * @param managersOnly <code>true</code> if agents are not allowed
     * 
     * @return if a resource assigned to a task is lockable for the current user
     */
    boolean isLockableInWorkflow(CmsUser user, CmsLock lock, boolean managersOnly);

    /**
     * Publishes the resources in the workflow project assigned to a task.<p>
     * 
     * @param cms the cms object
     * @param wfProject the workflow project assigned to the task
     * @param message the additional message
     * 
     * @throws CmsException if publishing fails
     */
    void publishTask(CmsObject cms, CmsProject wfProject, String message) throws CmsException;

    /**
     * Stes the workflow engine connector to use.<p>
     * 
     * @param engine the workflow engine connector
     */
    void setEngine(I_CmsWorkflowEngine engine);

    /**
     * Signalizes a transition in the workflow.<p>
     * 
     * @param cms the cms object
     * @param wfProject the workflow project
     * @param transition id of the transition 
     * @param message the additional message
     * @throws CmsException if the workflow engine cannot process the required transition
     * @return the last action requested from the workflow engine
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getTransitions(CmsProject)
     * 
     * TODO: remove return value
     */
    I_CmsWorkflowAction signal(CmsObject cms, CmsProject wfProject, I_CmsWorkflowTransition transition, String message)
    throws CmsException;

    /**
     * Aborts a task, all changes are reverted.<p>
     *  
     * @param cms the cms object
     * @param wfProject the workflow project assigned to the task
     * @param message the additional message
     * @throws CmsException if aborting/undoing the task fails
     */
    void undoTask(CmsObject cms, CmsProject wfProject, String message) throws CmsException;
}