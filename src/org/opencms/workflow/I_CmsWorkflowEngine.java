/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workflow/Attic/I_CmsWorkflowEngine.java,v $
 * Date   : $Date: 2006/08/19 13:40:50 $
 * Version: $Revision: 1.1.2.1 $
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

import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsProject;

import java.security.Principal;
import java.util.List;

/**
 * Workflow engines connected to OpenCms must implement this interface to provide "low level" workflow functionality.<p>
 * 
 * The OpenCms core allows to connect an external workflow engine to OpenCms.
 * This interface must be implemented "on top" of this external workflow engine in order to 
 * allow OpenCms resources to be handled in the workflow.<p>
 * 
 * The basic concept can be described as follows: 
 * OpenCms will bundle a set of resources and then hand this set over to the {@link I_CmsWorkflowEngine}.
 * These bundle of resources can then be shifted around in the workflow according to the possible transitions.
 * When the end of the workflow task is reached, the resources will usually be published.<p>
 *
 * An implementation of the {@link I_CmsWorkflowManager} usually provides "high level" workflow functionality 
 * such as operations required by a GUI. The collected information is then passed to the 
 * {@link I_CmsWorkflowEngine} which handles the "low level" workflow functionality like states and persistence.<p> 
 *
 * Please note: The default installation of OpenCms may ship with a simple implementation of this interface.
 * For more complex workflow scenarios, provide your own implementation.<p> 
 * 
 * @author Carsten Weinholz
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.0.0
 */
public interface I_CmsWorkflowEngine {

    /** The finish action code. */
    int FINISH = 2;

    /** The no action code. */
    int NOOP = 0;

    /** The publish action code. */
    int PUBLISH = 1;

    /**
     * TODO: This implementation is incomplete because it is not possible to signal from the engine to OpenCms
     * if OpenCms does not call a method in the engine first - this must be changed.
     */
    int todo1 = 0;

    /** TODO: Genereate new type "CmsWorkflowPackage" (or similar name) instead of using {@link CmsProject}. */
    int todo2 = 0;

    /**
     * Signalizes that the workflow should be aborted.<p>
     * 
     * @param wfProject the workflow project
     * @param message the additional message
     * 
     * @throws CmsWorkflowException if the workflow engine cannot process the required transition
     */
    void abort(CmsProject wfProject, String message) throws CmsWorkflowException;

    /**
     * Returns the agent group responsible for the given workflow project in the current state.<p>
     * 
     * @param wfProject the workflow project
     * 
     * @return the agent group currently responsible
     * 
     * @throws CmsDataAccessException if the agent group cannot be retrieved
     */
    Principal getAgent(CmsProject wfProject) throws CmsDataAccessException;

    /**
     * Returns the agent group initially responsible for workflows of a given type.<p>
     * 
     * @param type the workflow type
     * 
     * @return the agent group initially responsible
     * 
     * @throws CmsDataAccessException if the agent group cannot be retrieved
     */
    Principal getAgent(I_CmsWorkflowType type) throws CmsDataAccessException;

    /**
     * Returns the log for the given workflow project.<p>
     * 
     * The log consists of entries of type {@link I_CmsWorkflowLogEntry}.<p>
     * 
     * @param wfProject the workflow project
     * 
     * @return the log for the given workflow project (instances of {@link I_CmsWorkflowLogEntry})
     * 
     * @throws CmsDataAccessException if the log cannot be retrieved
     */
    List getLog(CmsProject wfProject) throws CmsDataAccessException;

    /**
     * Returns the group responsible for managing a workflow.<p>
     * 
     * @param wfProject the workflow project
     * 
     * @return the group responsible for managing such workflows
     * 
     * @throws CmsDataAccessException if the managing group cannot be retrieved
     */
    Principal getManager(CmsProject wfProject) throws CmsDataAccessException;

    /**
     * Returns the group responsible for managing workflows of a given type.<p>
     * 
     * @param type the workflow type
     * 
     * @return the group responsible for managing such workflows
     * 
     * @throws CmsDataAccessException if the managing group cannot be retrieved
     */
    Principal getManager(I_CmsWorkflowType type) throws CmsDataAccessException;

    /**
     * Returns the group or user who initiated the task of the given workflow project.
     * 
     * @param wfProject the workflow project
     * 
     * @return the group or user currently responsible
     * 
     * @throws CmsDataAccessException if the owner cannot be retrieved
     */
    Principal getOwner(CmsProject wfProject) throws CmsDataAccessException;

    /**
     * Returns the start time of the task of a workflow project.<p>
     * 
     * @param wfProject the workflow project
     * 
     * @return the start time
     * 
     * @throws CmsDataAccessException if the start time cannot be retrieved
     */
    long getStartTime(CmsProject wfProject) throws CmsDataAccessException;

    /**
     * Returns the name of the current state.
     * 
     * @param wfProject the workflow project
     * 
     * @return the current state
     * 
     * @throws CmsDataAccessException if the state cannot be retrieved
     */
    I_CmsWorkflowState getState(CmsProject wfProject) throws CmsDataAccessException;

    /**
     * Gets a list of all transitions used by the workflow engine.<p>
     * 
     * The list contains instances of {@link I_CmsWorkflowTransition} used to identify a transition.<p>
     * 
     * @return a list of all transitions ({@link I_CmsWorkflowTransition}) used by the workflow engine
     * 
     * @throws CmsDataAccessException if the list cannot be retrieved
     */
    List getTransitions() throws CmsDataAccessException;

    /**
     * Returns a list of possible transitions in the workflow.<p>
     *  
     * The list contains instances of {@link I_CmsWorkflowTransition} used to identify a transition.<p>
     *  
     * @param wfProject the workflow project
     * 
     * @return a list of possible transitions ({@link I_CmsWorkflowTransition})
     * 
     * @throws CmsDataAccessException if the list cannot be retrieved
     */
    List getTransitions(CmsProject wfProject) throws CmsDataAccessException;

    /**
     * Returns the workflow type of the given workflow project.<p>
     * 
     * @param wfProject the workflow project
     * 
     * @return the workflow type of the given project
     * 
     * @throws CmsDataAccessException if the type cannot be retrieved
     */
    I_CmsWorkflowType getType(CmsProject wfProject) throws CmsDataAccessException;

    /**
     * Returns a list of all available workflow types.<p>
     * 
     * The list contains instances of {@link I_CmsWorkflowType} used to identify the available workflow types.<p>
     * 
     * @return a list containing all available workflow types (instances of {@link I_CmsWorkflowType})
     * 
     * @throws CmsDataAccessException if the list cannot be retrieved
     */
    List getWorkflowTypes() throws CmsDataAccessException;

    /**
     * Returns a list of all available workflow types that can be utilized for the given resource.<p>
     * 
     * The list contains instances of {@link I_CmsWorkflowType} used to identify the available workflow types
     * 
     * @param resourcePath the resource
     * 
     * @return a list containing all available workflow types (instances of {@link I_CmsWorkflowType})
     * 
     * @throws CmsDataAccessException if the list cannot be retrieved
     */
    List getWorkflowTypes(String resourcePath) throws CmsDataAccessException;

    /**
     * Initializes a workflow for the given workflow project in the workflow engine.<p>
     * 
     * @param wfProject the workflow project
     * @param type the workflow type
     * @param message the additional message
     * @return the action to perform
     * @throws CmsWorkflowException if the workflow engine cannot initialize the workflow
     * 
     * TODO: remove the return value
     */
    I_CmsWorkflowAction init(CmsProject wfProject, I_CmsWorkflowType type, String message) throws CmsWorkflowException;

    /**
     * Indicates to the workflow engine that the given workflow project should be published now.<p>
     * 
     * @param wfProject the workflow project
     * @param transition the workflow transition 
     * @param message the additional message
     * @return the action to perform
     * @throws CmsWorkflowException if the workflow engine cannot process the required transition
     * 
     * TODO: remove the return value
     */
    I_CmsWorkflowAction signal(CmsProject wfProject, I_CmsWorkflowTransition transition, String message)
    throws CmsWorkflowException;

    /**
     * Indicates to the workflow engine that the workflow should be aborted 
     * and that the changes on resources in the workflow should be undone.<p>
     * 
     * @param wfProject the workflow project
     * @param message the additional message
     * 
     * @throws CmsWorkflowException if the workflow engine cannot process the required transition
     */
    void undo(CmsProject wfProject, String message) throws CmsWorkflowException;
}