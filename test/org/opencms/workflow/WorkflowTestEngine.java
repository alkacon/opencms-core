/*
 * File   : $Source: /alkacon/cvs/opencms/test/org/opencms/workflow/Attic/WorkflowTestEngine.java,v $
 * Date   : $Date: 2006/08/19 13:40:58 $
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

import org.opencms.file.CmsProject;
import org.opencms.security.CmsPrincipal;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Workflow test engine implementation.<p>
 */
public class WorkflowTestEngine implements I_CmsWorkflowEngine {

    /**
     * Dummy principal implementation.<p>
     */
    class TestPrincipal extends CmsPrincipal {

        /**
         * Constructor used to set the principal name.<p>
         * 
         * @param name the principal name
         */
        public TestPrincipal(String name) {

            this.setName(name);
        }

        /**
         * @see org.opencms.security.I_CmsPrincipal#checkName(java.lang.String)
         */
        public void checkName(String name) {

            // noop   
        }
    }

    /**
     * Dummy implementation of workflow state.
     */
    static class TestWorkflowState implements I_CmsWorkflowState {

        /** The name of the workflow state. */
        private String m_name;

        /**
         * Constructor to initialize the name of the state.<p>
         * 
         * @param name the name of the type
         */
        public TestWorkflowState(String name) {

            m_name = name;
        }

        /**
         * @see org.opencms.workflow.I_CmsWorkflowState#getId()
         */
        public String getId() {

            return m_name;
        }

        /**
         * @see org.opencms.workflow.I_CmsWorkflowState#getName(java.util.Locale)
         */
        public String getName(Locale locale) {

            return m_name;
        }
    }

    /**
     * Dummy implementation of a workflow instance.
     */
    static class TestWorkflowTask {

        private int m_id;

        private long m_startTime;

        private I_CmsWorkflowState m_state;

        private I_CmsWorkflowType m_type;

        /**
         * Creates a new task.<p>
         * 
         * @param id id of the task
         * @param type type of the workflow
         * @param state the initial state
         */
        public TestWorkflowTask(int id, I_CmsWorkflowType type, I_CmsWorkflowState state) {

            m_id = id;
            m_type = type;
            m_state = state;
            m_startTime = 0L;
        }

        /**
         * Returns the id of the task.<p>
         * 
         * @return the id of the task
         */
        public int getId() {

            return m_id;
        }

        /**
         * Returns the start time of the task.<p>
         * 
         * @return the start time of the task
         */
        public long getStartTime() {

            return m_startTime;
        }

        /**
         * Returns the state of the task.<p>
         * 
         * @return the state of the task
         */
        public I_CmsWorkflowState getState() {

            return m_state;
        }

        /**
         * Returns the type of the task.<p>
         * 
         * @return the type of the task
         */
        public I_CmsWorkflowType getType() {

            return m_type;
        }

        /**
         * Sets the start time of the task.<p>
         * 
         * @param time the start time
         */
        public void setStartTime(long time) {

            m_startTime = time;
        }

        /**
         * Sets the state of the task.<p>
         * 
         * @param state the new state of the task
         */
        public void setState(I_CmsWorkflowState state) {

            m_state = state;
        }
    }

    /**
     * Dummy implementation of workflow transition.
     */
    static class TestWorkflowTransition implements I_CmsWorkflowTransition {

        /** The name of the workflow type. */
        private String m_name;

        /**
         * Constructor to initialize the name of the transition.<p>
         * 
         * @param name the name of the type
         */
        public TestWorkflowTransition(String name) {

            m_name = name;
        }

        /**
         * @see org.opencms.workflow.I_CmsWorkflowTransition#getId()
         */
        public String getId() {

            return m_name;
        }

        /**
         * @see org.opencms.workflow.I_CmsWorkflowTransition#getName(java.util.Locale)
         */
        public String getName(Locale locale) {

            return m_name;
        }
    }

    /**
     * Dummy implementation of workflow type.
     */
    static class TestWorkflowType implements I_CmsWorkflowType {

        /** The name of the workflow type. */
        private String m_name;

        /**
         * Constructor to initialize the name of the type.<p>
         * 
         * @param name the name of the type
         */
        public TestWorkflowType(String name) {

            m_name = name;
        }

        /**
         * @see org.opencms.workflow.I_CmsWorkflowType#getId()
         */
        public String getId() {

            return m_name;
        }

        /**
         * @see org.opencms.workflow.I_CmsWorkflowType#getName(java.util.Locale)
         */
        public String getName(Locale locale) {

            return m_name;
        }
    }

    /** Workflow transition. */
    private static final I_CmsWorkflowTransition APPROVE = new TestWorkflowTransition("Approve");

    /** Workflow state. */
    private static final I_CmsWorkflowState APPROVED = new TestWorkflowState("Approved");

    /** Workflow transition. */
    private static final I_CmsWorkflowTransition PUBLISH = new TestWorkflowTransition("Publish");

    /** Workflow state. */
    private static final I_CmsWorkflowState PUBLISHED = new TestWorkflowState("Published");

    /** Workflow transition. */
    private static final I_CmsWorkflowTransition REJECT = new TestWorkflowTransition("Reject");

    /** Workflow state. */
    private static final I_CmsWorkflowState REJECTED = new TestWorkflowState("Rejected");

    /** Workflow transition. */
    private static final I_CmsWorkflowTransition RELEASE = new TestWorkflowTransition("Release");

    /** Workflow state. */
    private static final I_CmsWorkflowState RELEASED = new TestWorkflowState("Released");

    /** Workflow type. */
    private static final I_CmsWorkflowType REVIEW_4_EYE = new TestWorkflowType("4 Eye Review");

    /** The list of transitions in 4 eye review workflow. */
    private static List[] REVIEW_4_EYE_TRANSITIONS = new List[] {
        // RELEASED - PUBLISH / REJECT
        Arrays.asList(new I_CmsWorkflowTransition[] {PUBLISH, REJECT}),
        // APPROVED - N/A
        Arrays.asList(new I_CmsWorkflowTransition[] {}),
        // REJECTED - RELEASE
        Arrays.asList(new I_CmsWorkflowTransition[] {RELEASE}),
        // PUBLISHED- N/A
        Arrays.asList(new I_CmsWorkflowTransition[] {})};

    /** Workflow type. */
    private static final I_CmsWorkflowType REVIEW_6_EYE = new TestWorkflowType("6 Eye Review");

    /** The list of transitions in 6 eye review workflow. */
    private static List[] REVIEW_6_EYE_TRANSITIONS = new List[] {
        // RELEASED - APPROVE / REJECT
        Arrays.asList(new I_CmsWorkflowTransition[] {APPROVE, REJECT}),
        // APPROVED - PUBLISH / REJECT
        Arrays.asList(new I_CmsWorkflowTransition[] {PUBLISH, REJECT}),
        // REJECTED - RELEASE
        Arrays.asList(new I_CmsWorkflowTransition[] {RELEASE}),
        // PUBLISHED- N/A
        Arrays.asList(new I_CmsWorkflowTransition[] {})};

    /**  The list of states. */
    private static List WORKFLOW_STATES = Arrays.asList(new I_CmsWorkflowState[] {
        RELEASED,
        APPROVED,
        REJECTED,
        PUBLISHED});

    /** The list of available workflow transitions. */
    private static List WORKFLOW_TRANSITIONS = Arrays.asList(new I_CmsWorkflowTransition[] {
        RELEASE,
        APPROVE,
        REJECT,
        PUBLISH});

    /** The list of available workflow types. */
    private static List WORKFLOW_TYPES = Arrays.asList(new I_CmsWorkflowType[] {REVIEW_4_EYE, REVIEW_6_EYE});

    /** The map of currently initialized tasks. */
    private Map m_tasks;

    /**
     * Constructor initializes the internal map of tasks.<p>
     */
    public WorkflowTestEngine() {

        m_tasks = new HashMap();
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#abort(org.opencms.file.CmsProject, java.lang.String)
     */
    public void abort(CmsProject wfProject, String message) {

        // nothing todo
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getAgent(org.opencms.file.CmsProject)
     */
    public Principal getAgent(CmsProject wfProject) {

        // only members of group2 are allowed to work with initialized workflow instances
        if (m_tasks.containsKey(Integer.toString(wfProject.getId()))) {
            return new TestPrincipal("group2");
        } else {
            return new TestPrincipal("Users");
        }
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getAgent(org.opencms.workflow.I_CmsWorkflowType)
     */
    public Principal getAgent(I_CmsWorkflowType type) {

        // initially, all users are allowed to initialize a workflow of this type
        return new TestPrincipal("Users");
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getLog(org.opencms.file.CmsProject)
     */
    public List getLog(CmsProject wfProject) {

        return null;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getManager(org.opencms.file.CmsProject)
     */
    public Principal getManager(CmsProject wfProject) {

        return new TestPrincipal("group3");
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getManager(org.opencms.workflow.I_CmsWorkflowType)
     */
    public Principal getManager(I_CmsWorkflowType type) {

        return new TestPrincipal("group3");
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getOwner(org.opencms.file.CmsProject)
     */
    public Principal getOwner(CmsProject wfProject) {

        return null;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getStartTime(org.opencms.file.CmsProject)
     */
    public long getStartTime(CmsProject wfProject) {

        return ((TestWorkflowTask)m_tasks.get(Integer.toString(wfProject.getId()))).getStartTime();
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getState(org.opencms.file.CmsProject)
     */
    public I_CmsWorkflowState getState(CmsProject wfProject) {

        return ((TestWorkflowTask)m_tasks.get(Integer.toString(wfProject.getId()))).getState();
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getTransitions()
     */
    public List getTransitions() {

        return WORKFLOW_TRANSITIONS;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getTransitions(org.opencms.file.CmsProject)
     */
    public List getTransitions(CmsProject wfProject) {

        TestWorkflowTask task = getTask(wfProject);

        switch (WORKFLOW_TYPES.indexOf(task.getType())) {
            case 0: // 4 eye
                return REVIEW_4_EYE_TRANSITIONS[WORKFLOW_STATES.indexOf(task.getState())];

            case 1: // 6 eye
                return REVIEW_6_EYE_TRANSITIONS[WORKFLOW_STATES.indexOf(task.getState())];

            default:
                return Arrays.asList(new Object[] {});
        }
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getType(org.opencms.file.CmsProject)
     */
    public I_CmsWorkflowType getType(CmsProject wfProject) {

        return ((TestWorkflowTask)m_tasks.get(Integer.toString(wfProject.getId()))).getType();
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getWorkflowTypes()
     */
    public List getWorkflowTypes() {

        return WORKFLOW_TYPES;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#getWorkflowTypes(java.lang.String)
     */
    public List getWorkflowTypes(String resourcePath) {

        return WORKFLOW_TYPES;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#init(org.opencms.file.CmsProject, org.opencms.workflow.I_CmsWorkflowType, java.lang.String)
     */
    public I_CmsWorkflowAction init(CmsProject wfProject, I_CmsWorkflowType type, String message) {

        TestWorkflowTask newTask = new TestWorkflowTask(
            wfProject.getId(),
            type,
            (I_CmsWorkflowState)WORKFLOW_STATES.get(0));
        newTask.setStartTime(System.currentTimeMillis());
        m_tasks.put(Integer.toString(wfProject.getId()), newTask);

        return CmsDefaultWorkflowAction.NOOP;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#signal(org.opencms.file.CmsProject, org.opencms.workflow.I_CmsWorkflowTransition, java.lang.String)
     */
    public I_CmsWorkflowAction signal(CmsProject wfProject, I_CmsWorkflowTransition transition, String message)
    throws CmsWorkflowException {

        TestWorkflowTask task = getTask(wfProject);
        I_CmsWorkflowAction action = CmsDefaultWorkflowAction.NOOP;

        switch (WORKFLOW_STATES.indexOf(task.getState())) {
            case 0: // Released - task can be approved (6 eye only), published (4 eye only) or rejected
                if (transition.equals(APPROVE) && task.getType().equals(REVIEW_6_EYE)) {
                    task.setState(APPROVED);
                } else if (transition.equals(PUBLISH) && task.getType().equals(REVIEW_4_EYE)) {
                    action = CmsDefaultWorkflowAction.PUBLISH;
                    task.setState(PUBLISHED);
                } else if (transition.equals(REJECT)) {
                    task.setState(REJECTED);
                } else {
                    throw new CmsWorkflowException(Messages.get().container(
                        Messages.ERR_WORKFLOW_ENGINE_1,
                        "Invalid transition "
                            + "\""
                            + transition.getId()
                            + "\""
                            + " in state "
                            + "\""
                            + task.getState().getId()
                            + "\""));
                }
                break;
            case 1: // Approved - task can be published or rejected
                if (transition.equals(PUBLISH)) {
                    action = CmsDefaultWorkflowAction.PUBLISH;
                    task.setState(PUBLISHED);
                } else if (transition.equals(REJECT)) {
                    task.setState(REJECTED);
                } else {
                    throw new CmsWorkflowException(Messages.get().container(
                        Messages.ERR_WORKFLOW_ENGINE_1,
                        "Invalid transition "
                            + "\""
                            + transition.getId()
                            + "\""
                            + " in state "
                            + "\""
                            + task.getState().getId()
                            + "\""));
                }
                break;
            case 2: // Rejected - task can be released again
                if (transition.equals(RELEASE)) {
                    task.setState(RELEASED);
                } else {
                    throw new CmsWorkflowException(Messages.get().container(
                        Messages.ERR_WORKFLOW_ENGINE_1,
                        "Invalid transition "
                            + "\""
                            + transition.getId()
                            + "\""
                            + " in state "
                            + "\""
                            + task.getState().getId()
                            + "\""));
                }
                break;
            default:
                throw new CmsWorkflowException(Messages.get().container(
                    Messages.ERR_WORKFLOW_ENGINE_1,
                    "Invalid state " + "\"" + task.getState().getId() + "\""));
        }

        return action;
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowEngine#undo(org.opencms.file.CmsProject, java.lang.String)
     */
    public void undo(CmsProject wfProject, String message) {

        // todo
    }

    /**
     * Returns the task assigend to a workflow project.<p>
     * 
     * @param wfProject the project
     * @return the task assigend to the project
     */
    private TestWorkflowTask getTask(CmsProject wfProject) {

        return (TestWorkflowTask)m_tasks.get(Integer.toString(wfProject.getId()));
    }
}