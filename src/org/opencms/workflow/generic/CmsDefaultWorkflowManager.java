/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workflow/generic/Attic/CmsDefaultWorkflowManager.java,v $
 * Date   : $Date: 2006/08/21 17:04:18 $
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

package org.opencms.workflow.generic;

import org.opencms.db.CmsPublishList;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsUser;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.relations.CmsRelationType;
import org.opencms.report.CmsLogReport;
import org.opencms.security.CmsPrincipal;
import org.opencms.security.CmsRole;
import org.opencms.security.I_CmsPrincipal;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsWorkflowException;
import org.opencms.workflow.I_CmsWorkflowAction;
import org.opencms.workflow.I_CmsWorkflowEngine;
import org.opencms.workflow.I_CmsWorkflowManager;
import org.opencms.workflow.I_CmsWorkflowState;
import org.opencms.workflow.I_CmsWorkflowTransition;
import org.opencms.workflow.I_CmsWorkflowType;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Provides basic methods supporting workflow functionalities.<p> 
 * 
 * @author Carsten Weinholz
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.0.0
 */
public class CmsDefaultWorkflowManager implements I_CmsWorkflowManager {

    /** The admin cms object. */
    protected static CmsObject m_cms;

    /** The class name of the workflow engine connector. */
    protected static String m_engineClazz;

    /** The workflow manager singleton. */
    protected static I_CmsWorkflowManager m_instance;

    /** The workflow engine. */
    protected static I_CmsWorkflowEngine m_workflowEngine;

    /** The list of actions. */
    private static final List ACTIONS = Arrays.asList(new Object[] {
        CmsDefaultWorkflowAction.PUBLISH,
        CmsDefaultWorkflowAction.FINISH,
        CmsDefaultWorkflowAction.FORWARD});

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDefaultWorkflowManager.class);

    /** Map of all available transitions. */
    protected HashMap m_workflowTransitions;

    /** Map of all available workflow types. */
    protected HashMap m_workflowTypes;

    /** The relation filter to use. */
    private CmsRelationFilter m_relationFilter;

    /**
     * Constructor to create an uninitialized workflow manager.<p>
     */
    public CmsDefaultWorkflowManager() {

        // noop;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#abortTask(org.opencms.file.CmsObject, org.opencms.file.CmsProject, java.lang.String)
     */
    public void abortTask(CmsObject cms, CmsProject wfProject, String message) throws CmsException {

        // ensure that only administrators and workflow project managers are allowed to abort a task
        boolean accept = cms.hasRole(CmsRole.ADMINISTRATOR);
        I_CmsPrincipal manager = getTaskManager(wfProject);
        accept |= cms.userInGroup(cms.getRequestContext().currentUser().getName(), manager.getName());
        accept |= wfProject.getOwnerId().equals(cms.getRequestContext().currentUser().getId());
        if (accept) {
            m_workflowEngine.abort(wfProject, message);
            abortWorkflowProject(wfProject);
        } else {
            throw new CmsWorkflowException(Messages.get().container(
                Messages.ERR_ABORT_NOT_ALLOWED_2,
                cms.getRequestContext().currentUser().getName(),
                wfProject.getName()));
        }
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#addResource(org.opencms.file.CmsObject, org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public void addResource(CmsObject cms, CmsProject wfProject, CmsResource resource) throws CmsException {

        CmsLock lock = cms.getLock(resource, false);

        if (lock.isNullLock() || (lock.isExclusiveOwnedBy(cms.getRequestContext().currentUser()))) {

            // ensure that only administrators, workflow project managers and agents are allowed to add a resource
            boolean accept = cms.hasRole(CmsRole.ADMINISTRATOR);
            I_CmsPrincipal agent = getTaskAgent(wfProject);
            accept |= cms.userInGroup(cms.getRequestContext().currentUser().getName(), agent.getName());
            I_CmsPrincipal manager = getTaskManager(wfProject);
            accept |= cms.userInGroup(cms.getRequestContext().currentUser().getName(), manager.getName());

            if (accept) {
                Map relations = new HashMap();
                relations.put(resource.getRootPath(), resource);
                Iterator itRelations = cms.getRelationsForResource(
                    cms.getRequestContext().removeSiteRoot(resource.getRootPath()),
                    getRelationFilter()).iterator();
                while (itRelations.hasNext()) {
                    CmsRelation relation = (CmsRelation)itRelations.next();
                    try {
                        CmsResource target = relation.getTarget(cms, CmsResourceFilter.DEFAULT);
                        if (!relations.containsKey(target.getRootPath())
                            && (target.getState() == CmsResource.STATE_NEW || target.getState() == CmsResource.STATE_CHANGED)) {
                            relations.put(target.getRootPath(), target);
                        }
                    } catch (CmsVfsResourceNotFoundException e) {
                        // ignore broken links
                    }
                }
                try {
                    // change cms to root site
                    cms.getRequestContext().saveSiteRoot();
                    cms.getRequestContext().setSiteRoot("/");
                    for (Iterator i = relations.values().iterator(); i.hasNext();) {
                        CmsResource r = (CmsResource)i.next();
                        CmsLock rLock = cms.getLock(r.getRootPath());
                        if (rLock.isNullLock() || (rLock.isExclusiveOwnedBy(cms.getRequestContext().currentUser()))) {
                            // if the resource was locked before, unlock it
                            if (!rLock.isNullLock()) {
                                cms.unlockResource(cms.getSitePath(r));
                            }
                            addResourceToWorkflowProject(wfProject, r);
                            // lock the resource and set its flags and project reference appropriately
                            cms.lockResourceInWorkflow(cms.getSitePath(r), wfProject);
                        } else {
                            // the resource is locked and cannot be added to the workflow project
                            throw new CmsWorkflowException(Messages.get().container(
                                Messages.ERR_ADDING_LOCKED_RESOURCE_2,
                                r.getRootPath(),
                                wfProject.getName()));
                        }
                    }
                } finally {
                    cms.getRequestContext().restoreSiteRoot();
                }
            } else {
                throw new CmsWorkflowException(Messages.get().container(
                    Messages.ERR_ADD_NOT_ALLOWED_2,
                    cms.getRequestContext().currentUser().getName(),
                    wfProject.getName()));
            }
        } else {
            throw new CmsWorkflowException(Messages.get().container(
                Messages.ERR_ADDING_LOCKED_RESOURCE_2,
                resource.getName(),
                wfProject.getName()));
        }
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#addResource(org.opencms.file.CmsObject, org.opencms.file.CmsProject, java.lang.String)
     */
    public void addResource(CmsObject cms, CmsProject wfProject, String resourcePath) throws CmsException {

        CmsResource resource = cms.readResource(resourcePath);
        addResource(cms, wfProject, resource);
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#createTask(org.opencms.file.CmsObject, java.lang.String)
     */
    public CmsProject createTask(CmsObject cms, String description) throws CmsException {

        StringBuffer buf = new StringBuffer();
        buf.append(Messages.get().container(Messages.NEW_TASK_TITLE_0).key());

        return createWorkflowProject(cms.getRequestContext().currentUser(), buf.toString(), "Description:"
            + description);
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getAgent(org.opencms.workflow.I_CmsWorkflowType)
     */
    public I_CmsPrincipal getAgent(I_CmsWorkflowType type) throws CmsException {

        Principal agent = m_workflowEngine.getAgent(type);
        return CmsPrincipal.readPrefixedPrincipal(m_cms, I_CmsPrincipal.PRINCIPAL_GROUP + " " + agent.getName());
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getEngine()
     */
    public String getEngine() {

        return m_engineClazz;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getLog(org.opencms.file.CmsProject)
     */
    public List getLog(CmsProject wfProject) throws CmsException {

        throw new CmsException(Messages.get().container(Messages.ERR_NOT_IMPLEMENTED_1, "getLog"));
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getManager(org.opencms.workflow.I_CmsWorkflowType)
     */
    public I_CmsPrincipal getManager(I_CmsWorkflowType type) throws CmsException {

        Principal agent = m_workflowEngine.getManager(type);
        return CmsPrincipal.readPrefixedPrincipal(m_cms, I_CmsPrincipal.PRINCIPAL_GROUP + " " + agent.getName());
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTask(org.opencms.file.CmsObject, java.lang.String)
     */
    public CmsProject getTask(CmsObject cms, String resourcename) throws CmsException {

        CmsResource resource = cms.readResource(resourcename, CmsResourceFilter.ALL);
        CmsLock lock = cms.getLock(resource, false);
        if (lock.isWorkflow()) {
            return cms.readProject(lock.getProjectId());
        } else {
            throw new CmsWorkflowException(Messages.get().container(
                Messages.ERR_NOT_IN_WORKFLOW_PROJECT_1,
                resource.getName()));
        }
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTaskAgent(org.opencms.file.CmsProject)
     */
    public I_CmsPrincipal getTaskAgent(CmsProject wfProject) throws CmsException {

        Principal agent = m_workflowEngine.getAgent(wfProject);
        return CmsPrincipal.readPrefixedPrincipal(m_cms, I_CmsPrincipal.PRINCIPAL_GROUP + " " + agent.getName());
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTaskDescription(org.opencms.file.CmsProject)
     */
    public String getTaskDescription(CmsProject wfProject) {

        return wfProject.getDescription();
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTaskManager(org.opencms.file.CmsProject)
     */
    public I_CmsPrincipal getTaskManager(CmsProject wfProject) throws CmsException {

        Principal agent = m_workflowEngine.getManager(wfProject);
        return CmsPrincipal.readPrefixedPrincipal(m_cms, I_CmsPrincipal.PRINCIPAL_GROUP + " " + agent.getName());
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTaskOwner(org.opencms.file.CmsProject)
     */
    public I_CmsPrincipal getTaskOwner(CmsProject wfProject) throws CmsException {

        CmsUUID ownerId = wfProject.getOwnerId();
        return CmsPrincipal.readPrincipal(m_cms, ownerId);
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTasks()
     */
    public List getTasks() throws CmsException {

        throw new CmsException(Messages.get().container(Messages.ERR_NOT_IMPLEMENTED_1, "getTasks"));
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTasksForAgent(org.opencms.file.CmsUser)
     */
    public List getTasksForAgent(CmsUser user) throws CmsException {

        throw new CmsException(Messages.get().container(Messages.ERR_NOT_IMPLEMENTED_1, "getTasksForAgent"));
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTasksForOwner(org.opencms.file.CmsUser)
     */
    public List getTasksForOwner(CmsUser user) throws CmsException {

        throw new CmsException(Messages.get().container(Messages.ERR_NOT_IMPLEMENTED_1, "getTasksForOwner"));
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTaskStartTime(org.opencms.file.CmsProject)
     */
    public long getTaskStartTime(CmsProject wfProject) throws CmsException {

        return m_workflowEngine.getStartTime(wfProject);
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTaskState(org.opencms.file.CmsProject, java.util.Locale)
     */
    public String getTaskState(CmsProject wfProject, Locale locale) throws CmsException {

        I_CmsWorkflowState state = m_workflowEngine.getState(wfProject);
        if (state != null) {
            return state.getName(locale);
        } else {
            throw new CmsException(Messages.get().container(
                Messages.ERR_WORKFLOW_NOT_INITIALIZED_0));
        }        
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTaskType(org.opencms.file.CmsProject, java.util.Locale)
     */
    public String getTaskType(CmsProject wfProject, Locale locale) throws CmsException {

        I_CmsWorkflowType type = m_workflowEngine.getType(wfProject);
        if (type != null) {
            return type.getName(locale);
        } else {
            throw new CmsException(Messages.get().container(
                Messages.ERR_WORKFLOW_NOT_INITIALIZED_0));
        }
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTransitions()
     */
    public List getTransitions() throws CmsException {

        return m_workflowEngine.getTransitions();
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getTransitions(org.opencms.file.CmsProject)
     */
    public List getTransitions(CmsProject wfProject) throws CmsException {

        return m_workflowEngine.getTransitions(wfProject);
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getWorkflowTransition(java.lang.String)
     */
    public I_CmsWorkflowTransition getWorkflowTransition(String id) throws CmsException {

        if (m_workflowTransitions == null) {
            m_workflowTransitions = new HashMap();
            for (Iterator i = getTransitions().iterator(); i.hasNext();) {
                I_CmsWorkflowTransition t = (I_CmsWorkflowTransition)i.next();
                m_workflowTransitions.put(t.getId(), t);
            }
        }

        return (I_CmsWorkflowTransition)m_workflowTransitions.get(id);
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getWorkflowType(java.lang.String)
     */
    public I_CmsWorkflowType getWorkflowType(String id) throws CmsException {

        if (m_workflowTypes == null) {
            m_workflowTypes = new HashMap();
            for (Iterator i = getWorkflowTypes().iterator(); i.hasNext();) {
                I_CmsWorkflowType t = (I_CmsWorkflowType)i.next();
                m_workflowTypes.put(t.getId(), t);
            }
        }

        return (I_CmsWorkflowType)m_workflowTypes.get(id);
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getWorkflowTypes()
     */
    public List getWorkflowTypes() throws CmsException {

        return m_workflowEngine.getWorkflowTypes();
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getWorkflowTypes(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public List getWorkflowTypes(CmsObject cms, CmsResource resource) throws CmsException {

        return m_workflowEngine.getWorkflowTypes(resource.getRootPath());
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#init(org.opencms.file.CmsObject, org.opencms.file.CmsProject, org.opencms.workflow.I_CmsWorkflowType, java.lang.String)
     */
    public I_CmsWorkflowAction init(CmsObject cms, CmsProject wfProject, I_CmsWorkflowType type, String message)
    throws CmsException {

        I_CmsWorkflowAction action = null;

        // ensure that only administrators, workflow project managers and agents are allowed to init a workflow
        boolean accept = cms.hasRole(CmsRole.ADMINISTRATOR);
        I_CmsPrincipal agent = getAgent(type);
        accept |= cms.userInGroup(cms.getRequestContext().currentUser().getName(), agent.getName());
        I_CmsPrincipal manager = getManager(type);
        accept |= cms.userInGroup(cms.getRequestContext().currentUser().getName(), manager.getName());

        if (accept) {
            action = m_workflowEngine.init(wfProject, type, message);

            // change the task title appropriately
            List resources = getAssignedResources(wfProject);
            if (resources.size() > 1) {
                wfProject.setName(Messages.get().container(
                    Messages.TASK_TITLE_MULTI_3,
                    type.getName(CmsLocaleManager.getDefaultLocale()),
                    "",
                    Integer.toString(resources.size())).key());
            } else {
                wfProject.setName(Messages.get().container(
                    Messages.TASK_TITLE_SINGLE_3,
                    type.getName(CmsLocaleManager.getDefaultLocale()),
                    "",
                    resources.get(0)).key());
            }
        } else {
            throw new CmsWorkflowException(Messages.get().container(
                Messages.ERR_INIT_NOT_ALLOWED_2,
                cms.getRequestContext().currentUser().getName(),
                type.getName(CmsLocaleManager.getDefaultLocale())));
        }

        if (action.equals(CmsDefaultWorkflowAction.SIGNAL)) {
            action = signal(
                cms,
                wfProject,
                (I_CmsWorkflowTransition)action.getParameter(CmsDefaultWorkflowAction.SIGNAL_TRANSITION),
                message);
        }

        return action;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#initialize(org.opencms.file.CmsObject)
     */
    public void initialize(CmsObject cms) {

        m_cms = cms;

        Object initClass;
        try {
            initClass = Class.forName(m_engineClazz).newInstance();
        } catch (Throwable t) {
            LOG.error(Messages.get().getBundle().key(Messages.LOG_INIT_WORKFLOW_ENGINE_FAILURE_1, m_engineClazz), t);
            return;
        }
        if (initClass instanceof I_CmsWorkflowEngine) {
            m_workflowEngine = (I_CmsWorkflowEngine)initClass;
            if (CmsLog.INIT.isInfoEnabled()) {
                CmsLog.INIT.info(Messages.get().getBundle().key(Messages.INIT_WORKFLOW_ENGINE_SUCCESS_1, m_engineClazz));
            }
        } else {
            if (CmsLog.INIT.isErrorEnabled()) {
                CmsLog.INIT.error(Messages.get().getBundle().key(Messages.INIT_WORKFLOW_ENGINE_INVALID_1, m_engineClazz));
            }
        }
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#isEditableInWorkflow(org.opencms.file.CmsObject, org.opencms.file.CmsResource)
     */
    public boolean isEditableInWorkflow(CmsObject cms, CmsResource resource) {

        return false;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#isLockableInWorkflow(org.opencms.file.CmsObject, org.opencms.file.CmsResource, boolean)
     */
    public boolean isLockableInWorkflow(CmsObject cms, CmsResource resource, boolean managersOnly) {

        boolean isLockable = false;

        try {
            CmsLock lock = cms.getLock(resource, false);
            if (!lock.isNullLock()) {
                CmsProject project = cms.readProject(lock.getProjectId());

                if (project.getType() == CmsProject.PROJECT_TYPE_WORKFLOW) {
                    isLockable = cms.hasRole(CmsRole.ADMINISTRATOR);
                    if (!isLockable && !managersOnly) {
                        I_CmsPrincipal agents = getTaskAgent(project);
                        isLockable = cms.userInGroup(cms.getRequestContext().currentUser().getName(), agents.getName());
                    }
                    if (!isLockable) {
                        I_CmsPrincipal managers = getTaskManager(project);
                        isLockable = cms.userInGroup(
                            cms.getRequestContext().currentUser().getName(),
                            managers.getName());
                    }
                }
            }
        } catch (CmsException exc) {
            LOG.error(exc);
        }

        return isLockable;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#publishTask(org.opencms.file.CmsObject, org.opencms.file.CmsProject, java.lang.String)
     */
    public void publishTask(CmsObject cms, CmsProject wfProject, String message) throws CmsException {

        // ensure that only administrators and workflow project managers are allowed to publish a task
        boolean accept = cms.hasRole(CmsRole.ADMINISTRATOR);
        I_CmsPrincipal manager = getTaskManager(wfProject);
        accept |= cms.userInGroup(cms.getRequestContext().currentUser().getName(), manager.getName());

        if (accept) {
            publishWorkflowProject(wfProject);
        } else {
            throw new CmsWorkflowException(Messages.get().container(
                Messages.ERR_PUBLISH_NOT_ALLOWED_2,
                cms.getRequestContext().currentUser().getName(),
                wfProject.getName()));
        }
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#setEngine(java.lang.String)
     */
    public void setEngine(String engineClazz) {

        m_engineClazz = engineClazz;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#signal(org.opencms.file.CmsObject, org.opencms.file.CmsProject, org.opencms.workflow.I_CmsWorkflowTransition, java.lang.String)
     */
    public I_CmsWorkflowAction signal(
        CmsObject cms,
        CmsProject wfProject,
        I_CmsWorkflowTransition transition,
        String message) throws CmsException {

        // ensure that only administrators, workflow project managers and agents are allowed to send a signal
        boolean accept = cms.hasRole(CmsRole.ADMINISTRATOR);
        I_CmsPrincipal agent = getTaskAgent(wfProject);
        accept |= cms.userInGroup(cms.getRequestContext().currentUser().getName(), agent.getName());
        I_CmsPrincipal manager = getTaskManager(wfProject);
        accept |= cms.userInGroup(cms.getRequestContext().currentUser().getName(), manager.getName());

        I_CmsWorkflowAction action = null;
        if (accept) {
            // ensure that all workflow resources are unlocked
            
            // TODO: Supply functionality by other means
            // Why is this done here anyway?
            int todo_v7 = 0;
            OpenCms.getLockManager().removeResourcesInProject(wfProject.getId(), true, true);
            
            action = m_workflowEngine.signal(wfProject, transition, message);
        } else {
            throw new CmsWorkflowException(Messages.get().container(
                Messages.ERR_SIGNAL_NOT_ALLOWED_2,
                cms.getRequestContext().currentUser().getName(),
                wfProject.getName()));
        }

        switch (ACTIONS.indexOf(action)) {
            case 0: // CmsDefaultWorkflowAction.PUBLISH
                publishWorkflowProject(wfProject);
                break;
            case 1: // CmsDefaultWorkflowAction.FINISH
                abortWorkflowProject(wfProject);
                break;
            case 2: // CmsDefaultWorkflo
                break;
            default:
        // no action required
        }

        return action;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#undoTask(org.opencms.file.CmsObject, org.opencms.file.CmsProject, java.lang.String)
     */
    public void undoTask(CmsObject cms, CmsProject wfProject, String message) throws CmsException {

        // ensure that only administrators and workflow project managers are allowed to undo a task
        boolean accept = cms.hasRole(CmsRole.ADMINISTRATOR);
        I_CmsPrincipal manager = getTaskManager(wfProject);
        accept |= cms.userInGroup(cms.getRequestContext().currentUser().getName(), manager.getName());

        if (accept) {
            // send an undo signal to the workflow engine
            m_workflowEngine.undo(wfProject, message);
            undoWorkflowProject(wfProject);
        } else {
            throw new CmsWorkflowException(Messages.get().container(
                Messages.ERR_UNDO_NOT_ALLOWED_2,
                cms.getRequestContext().currentUser().getName(),
                wfProject.getName()));
        }
    }

    /**
     * Aborts a workflow project.
     * 
     * All resources in the project are unlocked, but left with their current state and content.
     * Afterwards, the workflow project is deleted.
     * 
     * @param wfProject the workflow project
     * @throws CmsException if aborting the project fails
     */
    public void abortWorkflowProject(CmsProject wfProject) throws CmsException {

        // TODO: Supply functionality by other means
        // In this case, extend deleteProject by special logic
        int todo_v7 = 0;

        // remove all locks
        OpenCms.getLockManager().removeResourcesInProject(wfProject.getId(), false, false);

        // delete the project
        m_cms.deleteProject(wfProject.getId());
    }

    /**
     * Adds a resource to a workflow project.<p>
     * 
     * @param wfProject the workflow project
     * @param resource the resource to add
     * @throws CmsException if the resource cannot be added 
     * 
     */
    public void addResourceToWorkflowProject(CmsProject wfProject, CmsResource resource) throws CmsException {

        CmsProject offlineProject = m_cms.getRequestContext().currentProject();
        try {
            // put the resource into the workflow project
            m_cms.getRequestContext().setCurrentProject(wfProject);
            m_cms.getRequestContext().setSiteRoot("/");

            m_cms.copyResourceToProject(m_cms.getSitePath(resource), wfProject);
        } finally {
            m_cms.getRequestContext().setCurrentProject(offlineProject);
        }
    }

    /**
     * Creates a new empty workflow project used to collect resources for a workflow.<p>
     * 
     * @param user the current user
     * @param name the project name
     * @param description the project description
     * @return a new empty workflow project
     * @throws CmsException if the project creation fails
     */
    public CmsProject createWorkflowProject(CmsUser user, String name, String description) throws CmsException {

        CmsProject wfProject = m_cms.createProject(
            name,
            description,
            OpenCms.getDefaultUsers().getGroupUsers(),
            OpenCms.getDefaultUsers().getGroupProjectmanagers(),
            CmsProject.PROJECT_TYPE_WORKFLOW);

        wfProject.setOwnerId(user.getId());
        m_cms.writeProject(wfProject);

        return wfProject;
    }

    /**
     * Returns all resources assigned to a workflow project.<p>
     * 
     * @param wfProject the workflow project
     * @return all resources assigned to the workflow project as <code>String</code>
     * @throws CmsException if something goes wrong
     */
    protected List getAssignedResources(CmsProject wfProject) throws CmsException {

        return m_cms.readProjectResources(wfProject);
    }

    /**
     * Returns a list of resources to publish for a workflow project.<p>
     * 
     * @param wfProject the workflow project
     * @return a list of resources to publish for the workflow project
     * @throws CmsException if something goes wrong
     */
    protected CmsPublishList getPublishList(CmsProject wfProject) throws CmsException {

        List resourcesToPublish = new ArrayList();
        List assignedResources = getAssignedResources(wfProject);
        
        for (Iterator i = assignedResources.iterator(); i.hasNext();) {
            String resourceName = (String)i.next();
            CmsResource resource = m_cms.readResource(resourceName);
            if (resource.getState() != CmsResource.STATE_UNCHANGED) {
                resourcesToPublish.add(resource);
            }
        }

        return m_cms.getPublishList(resourcesToPublish, false, true);
    }

    /**
     * Publishes a workflow project.
     * 
     * All resources in the project are unlocked and then published.
     * Afterwards, the workflow project is deleted.
     * 
     * @param wfProject the workflow project
     * @throws CmsException if publishing the resources in the project fails
     */
    public void publishWorkflowProject(CmsProject wfProject) throws CmsException {

        CmsProject offlineProject = m_cms.getRequestContext().currentProject();
        try {
            // TODO: Supply functionality by other means
            // In this case, check in publishProject for wfProject
            int todo_v7 = 0;
            OpenCms.getLockManager().removeResourcesInProject(wfProject.getId(), true, false);
            
            m_cms.getRequestContext().setCurrentProject(wfProject);
            CmsPublishList pL = getPublishList(wfProject);
            
            m_cms.publishProject(
                new CmsLogReport(m_cms.getRequestContext().getLocale(), this.getClass()),
                pL);

            m_cms.deleteProject(wfProject.getId());
        } finally {
            m_cms.getRequestContext().setCurrentProject(offlineProject);
        }
    }

    /**
     * Undo all changes made on resources in a workflow project.
     * 
     * All changed resources in the project are reverted to the last published version.
     * Afterwards, the workflow project is deleted.
     * 
     * @param wfProject the workflow project
     * @throws CmsException if undoing the changes of resources in the project fails
     */
    public void undoWorkflowProject(CmsProject wfProject) throws CmsException {

        CmsProject offlineProject = m_cms.getRequestContext().currentProject();
        try {
            m_cms.getRequestContext().setCurrentProject(wfProject);

            for (Iterator i = getAssignedResources(wfProject).iterator(); i.hasNext();) {
                String resourceName = (String)i.next();
                CmsResource resource = m_cms.readResource(resourceName);
                if (resource.getState() != CmsResource.STATE_UNCHANGED) {
                    m_cms.undoChanges(resourceName, CmsResource.UNDO_CONTENT);
                }
            }

            m_cms.unlockProject(wfProject.getId());
            m_cms.deleteProject(wfProject.getId());
        } finally {
            m_cms.getRequestContext().setCurrentProject(offlineProject);
        }
    }

    /**
     * Returns the relation filter to use.<p>
     * 
     * @return the relation filter to use
     */
    private CmsRelationFilter getRelationFilter() {

        if (m_relationFilter == null) {
            m_relationFilter = CmsRelationFilter.TARGETS;
            m_relationFilter = m_relationFilter.filterType(CmsRelationType.EMBEDDED_IMAGE);
            m_relationFilter = m_relationFilter.filterType(CmsRelationType.ATTACHMENT);
        }
        return m_relationFilter;
    }
}
