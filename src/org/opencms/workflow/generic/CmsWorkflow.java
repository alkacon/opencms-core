/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workflow/generic/Attic/CmsWorkflow.java,v $
 * Date   : $Date: 2006/08/25 13:16:57 $
 * Version: $Revision: 1.1.2.4 $
 *
 * Copyright (c) 2005 Alkacon Software GmbH
 * All Rights Reserved.
 *
 * THIS SOFTWARE IS NEITHER FREEWARE NOR PUBLIC DOMAIN!
 *
 * To use this software you must purchease a licencse from Alkacon Software GmbH.
 * In order to use this source code, you need written permission from
 * Alkacon Software GmbH. Redistribution of this source code, in modified or
 * unmodified form, is not allowed.
 *
 * ALKACON SOFTWARE GMBH MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THIS SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ALKACON SOFTWARE GMBH SHALL NOT BE LIABLE FOR ANY
 * DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * File created on 06. November 2003, 16:02
 */

package org.opencms.workflow.generic;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.i18n.CmsMessages;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsPermissionSet;
import org.opencms.util.CmsStringUtil;
import org.opencms.workflow.I_CmsWorkflowAction;
import org.opencms.workflow.I_CmsWorkflowManager;
import org.opencms.workflow.I_CmsWorkflowTransition;
import org.opencms.workflow.I_CmsWorkflowType;
import org.opencms.workplace.CmsMultiDialog;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceSettings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * Creates the workflow dialog.<p> 
 * 
 * In order to have the jsp available in the context menu, add these entries
 * to the <code>opencms-workplace.xml</code>:<p>
 * 
 * <code>
 * &lt;entry key="GUI_EXPLORER_CONTEXT_WORKFLOW_INIT_0" uri="commons/workflow_init.jsp" rules="a a aaaa aaaa dddd" order="41"/&gt;<br>
 * &lt;entry key="GUI_EXPLORER_CONTEXT_WORKFLOW_SIGNAL_0" uri="commons/workflow_signal.jsp" rules="a a aaaa aaaa dddd" order="42"/&gt;<br>
 * &lt;entry key="GUI_EXPLORER_CONTEXT_WORKFLOW_ABORT_0" uri="commons/workflow_abort.jsp" rules="a a aaaa aaaa dddd" order="43"/&gt;<br>
 * </code>
 * 
 * @author Carsten Weinholz
 * 
 * @version $Revision: 1.1.2.4 $ 
 * 
 * @since 7.0.0
 */
public class CmsWorkflow extends CmsMultiDialog {

    /** Value for the action: abort a task. */
    public static final int ACTION_ABORT = 400;

    /** Value for the action: initialize a task. */
    public static final int ACTION_INIT = 200;

    /** Value for the action: signal a transition. */
    public static final int ACTION_SIGNAL = 300;

    /** The dialog type: signal a transition. */
    public static final String DIALOG_TYPE_ABORT = "abort";

    /** The dialog type: init a task. */
    public static final String DIALOG_TYPE_INIT = "init";

    /** The dialog type: signal a transition. */
    public static final String DIALOG_TYPE_SIGNAL = "signal";

    /** Request parameter name for the description. */
    public static final String PARAM_DESCRIPTION = "description";

    /** Request parameter name for the task type. */
    public static final String PARAM_TASKTYPE = "tasktype";

    /** Request parameter name for the transition. */
    public static final String PARAM_TRANSITION = "transition";

    /** Request parameter name for the undo flag. */
    public static final String PARAM_UNDO = "undo";

    /** Type of the operation which is performed: signal a state change for a task. */
    public static final int TYPE_ABORT = 3;

    /** Type of the operation which is performed: initialize a task. */
    public static final int TYPE_INIT = 1;

    /** Type of the operation which is performed: signal a state change for a task. */
    public static final int TYPE_SIGNAL = 2;

    /** Unknow operation type. */
    public static final int TYPE_UNKNOWN = 0;

    /** The workflow dialog URI. */
    public static final String URI_WORKFLOW_ABORT_DIALOG = PATH_DIALOGS + "workflow_abort.jsp";

    /** The workflow dialog URI. */
    public static final String URI_WORKFLOW_INIT_DIALOG = PATH_DIALOGS + "workflow_init.jsp";

    /** The workflow dialog URI. */
    public static final String URI_WORKFLOW_SIGNAL_DIALOG = PATH_DIALOGS + "workflow_signal.jsp";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsWorkflow.class);

    /** The task description message. */
    private String m_paramDescription;

    /** The mail text. */
    private String m_paramMailText;

    /** The send mail flag. */
    private String m_paramSendMail;

    /** The task type. */
    private String m_paramTasktype;

    /** The transition. */
    private String m_paramTransition;

    /** The undo flag. */
    private String m_paramUndo;

    /**
     * Default constructor needed for dialog handler implementation.<p>
     */
    public CmsWorkflow() {

        super(null);
    }

    /**
     * Public constructor.<p>
     * 
     * @param jsp an initialized JSP action element
     */
    public CmsWorkflow(CmsJspActionElement jsp) {

        super(jsp);
    }

    /**
     * Public constructor with JSP variables.<p>
     * 
     * @param context the JSP page context
     * @param req the JSP request
     * @param res the JSP response
     */
    public CmsWorkflow(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        this(new CmsJspActionElement(context, req, res));
    }

    /**
     * Builds the HTML for the info line of the current task.<p>
     * 
     * @param cms the CmsObject
     * @param filename the current file
     * @param messages the localized workplace messages
     * @return the HTML for the info line of the current task
     */
    public static String buildTaskInfo(CmsObject cms, String filename, CmsMessages messages) {

        StringBuffer taskInfo = new StringBuffer();

        I_CmsWorkflowManager wfManager = OpenCms.getWorkflowManager();

        try {
            CmsProject wfProject = wfManager.getTask(cms, filename);
            taskInfo.append(messages.key(
                Messages.GUI_TASK_INFO_2,
                wfManager.getTaskDescription(wfProject),
                wfManager.getTaskState(wfProject, messages.getLocale())));
        } catch (CmsException e) {
            // should usually never happen
            LOG.error(e.getLocalizedMessage());
        }

        return taskInfo.toString();
    }

    /**
     * Builds the HTML for the select box of the currently available task types.<p>
     * 
     * @param cms the cms object
     * @param filename the current file
     * @param attributes optional attributes for the &lt;select&gt; tag, do not add the "name" atribute!
     * @param messages the localized workplace messages
     * 
     * @return the HTML for the select box of the currently available task types
     * @throws CmsException if something goes wrong
     */
    public static String buildTasktypeSelector(CmsObject cms, String filename, String attributes, CmsMessages messages)
    throws CmsException {

        I_CmsWorkflowManager wfManager = OpenCms.getWorkflowManager();
        List options = new ArrayList();
        List values = new ArrayList();

        CmsResource res = cms.readResource(filename);
        for (Iterator i = wfManager.getWorkflowTypes(cms, res).iterator(); i.hasNext();) {
            I_CmsWorkflowType t = (I_CmsWorkflowType)i.next();
            options.add(t.getName(messages.getLocale()));
            values.add(t.getId());
        }

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(attributes)) {
            attributes = " " + attributes;
        } else {
            attributes = "";
        }
        return CmsWorkplace.buildSelect(
            "name=\"" + PARAM_TASKTYPE + "\"" + attributes,
            options,
            values,
            values.size() - 1,
            true);
    }

    /**
     * Builds the HTML for the select box of the currently available transitions.<p>
     * 
     * @param cms the CmsObject
     * @param filename the current file
     * @param attributes optional attributes for the &lt;select&gt; tag, do not add the "name" atribute!
     * @param messages the localized workplace messages
     * 
     * @return the HTML for the select box of the currently available task types
     */
    public static String buildTransitionSelector(CmsObject cms, String filename, String attributes, CmsMessages messages) {

        I_CmsWorkflowManager wfManager = OpenCms.getWorkflowManager();
        List options = new ArrayList();
        List values = new ArrayList();
        
        try {
            CmsResource resource = cms.readResource(filename);
            CmsLock lock = cms.getLockForWorkflow(resource);
            if (lock.isWorkflow()) {
                CmsProject wfProject = cms.readProject(lock.getProjectId());
                for (Iterator i = wfManager.getTransitions(wfProject).iterator(); i.hasNext();) {
                    I_CmsWorkflowTransition t = (I_CmsWorkflowTransition)i.next();
                    options.add(t.getName(messages.getLocale()));
                    values.add(t.getId());
                }
            }
        } catch (Exception e) {
            // should usually never happen
            LOG.error(e.getLocalizedMessage());
        }
        
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(attributes)) {
            attributes = " " + attributes;
        } else {
            attributes = "";
        }
        return CmsWorkplace.buildSelect(
            "name=\"" + PARAM_TRANSITION + "\"" + attributes,
            options,
            values,
            values.size() - 1,
            true);
    }

    /**
     * Returns if the resource is currently locked in a workflow.<p>
     * 
     * @param cms the cms object
     * @param filename name of the resource
     * @return if the resource is currently locked in a workflow
     */
    public static boolean isInWorkflow (CmsObject cms, String filename) {
        
        CmsLock lock = null;
        try {
            CmsResource resource = cms.readResource(filename);
            lock = cms.getLockForWorkflow(resource);
        } catch (CmsException exc) {
            // noop
        }
        
        return lock != null && lock.isWorkflow();
    }
    
    /**
     * Determines if the resource should be locked, unlocked or if the lock should be stolen.<p>
     * 
     * @param cms the CmsObject
     * @return the dialog action: lock, change lock (steal) or unlock
     */
    public static int getDialogAction(CmsObject cms) {

        String fileName = CmsResource.getName(cms.getRequestContext().getUri());

        if ("workflow_init.jsp".equals(fileName)) {
            // a "init" action is requested
            return TYPE_INIT;
        } else if ("workflow_signal.jsp".equals(fileName)) {
            // a "signal" action is requested
            return TYPE_SIGNAL;
        } else if ("workflow_abort.jsp".equals(fileName)) {
            // a "abort" action is requested
            return TYPE_ABORT;
        } else {
            // action is unknown
            return TYPE_UNKNOWN;
        }
    }

    /**
     * Builds the HTML for the info line of the current task.<p>
     * 
     * @return the HTML for the info line of the current task
     */
    public String buildTaskInfo() {

        return buildTaskInfo(getCms(), getParamResource(), getMessages());
    }

    /**
     * Builds the HTML for the select box of the currently available task types.<p>
     * 
     * @return the HTML for a navigation position select box
     * @throws CmsException if something goes wrong
     */
    public String buildTasktypeSelector() throws CmsException {

        return buildTasktypeSelector(getCms(), getParamResource(), null, getMessages());
    }

    /**
     * Builds the HTML for the select box of the currently available transitions.<p>
     * 
     * @return the HTML for a navigation position select box
     */
    public String buildTransitionSelector() {

        return buildTransitionSelector(getCms(), getParamResource(), null, getMessages());
    }

    /**
     * Checks if the current file is locked in a workflow.<p>
     * If not, an exception is thrown.
     * 
     * @throws CmsException if the current file is not locked in a workflow
     */
    public void checkInWorkflow() throws CmsException {
        
        if (!isInWorkflow(getCms(), getParamResource())) {
            throw new CmsException (Messages.get().container(Messages.ERR_NOT_IN_WORKFLOW_PROJECT_1, getParamResource()));
        }
    }
    
    /**
     * Checks if the current file is not locked in a workflow.<p>
     * If it is locked, an exception is thrown.
     * 
     * @throws CmsException if the current file is already locked in a workflow
     */
    public void checkNotInWorkflow() throws CmsException {
        
        if (isInWorkflow(getCms(), getParamResource())) {
            throw new CmsException (Messages.get().container(Messages.ERR_ALREADY_IN_WORKFLOW_PROJECT_1, getParamResource()));
        }
    }    
            
    /**
     * Returns the dialog uri depending on the name of .<p>
     * 
     * @param resource the current resource
     * @param jsp the action element
     * @return the appropriate dialog uri
     */
    public String getDialogUri(String resource, CmsJspActionElement jsp) {

        switch (getDialogAction(jsp.getCmsObject())) {
            case TYPE_INIT:
                return URI_WORKFLOW_INIT_DIALOG;
            case TYPE_SIGNAL:
                return URI_WORKFLOW_SIGNAL_DIALOG;
            case TYPE_ABORT:
                return URI_WORKFLOW_ABORT_DIALOG;
            default:
                LOG.error("Unknown action type for resource " + resource);
                return null;
        }
    }

    /**
     * Returns the value of the description parameter.<p>
     * 
     * @return the value of the description parameter
     */
    public String getParamDescription() {

        return m_paramDescription;
    }

    /**
     * Returns the value of the mail text parameter.<p>
     * 
     * @return the value of the mail text parameter
     */
    public String getParamMailText() {

        return m_paramMailText;
    }

    /**
     * Returns the value of the send mail flag parameter.<p>
     * 
     * @return the value of the send mail flag parameter
     */
    public String getParamSendMail() {

        return m_paramSendMail;
    }

    /**
     * Returns the value of the task type parameter.<p>
     * 
     * @return the value of the task type parameter
     */
    public String getParamTasktype() {

        return m_paramTasktype;
    }

    /**
     * Returns the value of the transition parameter.<p>
     * 
     * @return the value of the transition parameter
     */
    public String getParamTransition() {

        return m_paramTransition;
    }

    /**
     * Returns the value of the undo flag parameter.<p>
     * 
     * @return the value of the undo flag parameter
     */
    public String getParamUndo() {

        return m_paramUndo;
    }

    /**
     * Returns the name of the task.<p>
     * 
     * @return the name of the task
     */
    public String getTaskName() {

        I_CmsWorkflowManager wfManager = OpenCms.getWorkflowManager();

        try {
            CmsProject wfProject = wfManager.getTask(getCms(), getParamResource());
            return wfProject.getName();
        } catch (CmsException exc) {
            return "";
        }
    }

    /**
     * Performs the operation, will be called by the JSP page.<p>
     * 
     * @throws JspException if problems including sub-elements occur
     */
    public void performAction() throws JspException {

        // save initialized instance of this class in request attribute for included sub-elements
        getJsp().getRequest().setAttribute(SESSION_WORKPLACE_CLASS, this);

        try {

            if (performDialogOperation()) {
                // if no exception is caused and "true" is returned the lock/unlock operation was successful          
                actionCloseDialog();
            } else {
                // "false" returned, display "please wait" screen
                getJsp().include(FILE_DIALOG_SCREEN_WAIT);
            }
        } catch (Throwable e) {
            // exception occured, show error dialog
            includeErrorpage(this, e);
        }
    }

    /**
     * Sets the value of the description parameter.<p>
     * 
     * @param value the value of the description parameter
     */
    public void setParamDescription(String value) {

        m_paramDescription = value;
    }

    /**
     * Sets the value of the mail text parameter.<p>
     * 
     * @param value the value of the mail text parameter
     */
    public void setParamMailText(String value) {

        m_paramMailText = value;
    }

    /**
     * Sets the value of the send mail flag.<p>
     * 
     * @param value the value of the send mail flag
     */
    public void setParamSendMail(String value) {

        m_paramSendMail = value;
    }

    /**
     * Sets the value of the task type parameter.<p>
     * 
     * @param value the value of the task type parameter
     */
    public void setParamTasktype(String value) {

        m_paramTasktype = value;
    }

    /**
     * Sets the value of the transition parameter.<p>
     * 
     * @param value the value of the transition parameter
     */
    public void setParamTransition(String value) {

        m_paramTransition = value;
    }

    /**
     * Sets the value of the undo flag parameter.<p>
     * 
     * @param value the value of the undo flag parameter
     */
    public void setParamUndo(String value) {

        m_paramUndo = value;
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initMessages()
     */
    protected void initMessages() {

        addMessages(Messages.get().getBundleName());
    }

    /**
     * @see org.opencms.workplace.CmsWorkplace#initWorkplaceRequestValues(org.opencms.workplace.CmsWorkplaceSettings, javax.servlet.http.HttpServletRequest)
     */
    protected void initWorkplaceRequestValues(CmsWorkplaceSettings settings, HttpServletRequest request) {

        // fill the parameter values in the get/set methods
        fillParamValues(request);

        // set the action for the JSP switch 
        if (DIALOG_CONFIRMED.equals(getParamAction())) {
            setAction(ACTION_CONFIRMED);
        } else if (DIALOG_CANCEL.equals(getParamAction())) {
            setAction(ACTION_CANCEL);
        } else if (DIALOG_WAIT.equals(getParamAction())) {
            setAction(ACTION_WAIT);
        } else {
            switch (getDialogAction(getCms())) {
                case TYPE_INIT:
                    setDialogTitle(Messages.GUI_INIT_WORKFLOW_RESOURCE_1, Messages.GUI_INIT_WORKFLOW_MULTI_2);
                    setParamDialogtype(DIALOG_TYPE_INIT);
                    // check the required permissions       
                    if (!checkResourcePermissions(CmsPermissionSet.ACCESS_WRITE, false)) {
                        // no write permissions for the resource, set cancel action to close dialog
                        setAction(ACTION_CANCEL);
                        return;
                    } else {
                        setAction(ACTION_INIT);
                    }
                    break;
                case TYPE_SIGNAL:
                    setDialogTitle(Messages.GUI_INIT_WORKFLOW_RESOURCE_1, Messages.GUI_INIT_WORKFLOW_MULTI_2);
                    setParamDialogtype(DIALOG_TYPE_SIGNAL);
                    setAction(ACTION_SIGNAL);
                    break;
                case TYPE_ABORT:
                    setDialogTitle(Messages.GUI_INIT_WORKFLOW_RESOURCE_1, Messages.GUI_INIT_WORKFLOW_MULTI_2);
                    setParamDialogtype(DIALOG_TYPE_ABORT);
                    setAction(ACTION_ABORT);
                    break;
                default:
                    setAction(ACTION_DEFAULT);
            }
            // set action depending on user settings

        }
    }

    /**
     * Performs the lock/unlock/steal lock operation.<p>
     * 
     * @return true, if the operation was performed, otherwise false
     * @throws CmsException if operation is not successful
     */
    protected boolean performDialogOperation() throws CmsException {

        //on multi resource operation display "please wait" screen
        if (isMultiOperation() && !DIALOG_WAIT.equals(getParamAction())) {
            return false;
        }
        // determine action to perform (lock, unlock, change lock)
        int dialogAction = getDialogAction(getCms());

        // now perform the operation on the resource(s)
        Iterator i = getResourceList().iterator();
        while (i.hasNext()) {
            String resName = (String)i.next();
            try {
                performSingleResourceOperation(resName, dialogAction);
            } catch (CmsException e) {
                // collect exceptions to create a detailed output
                addMultiOperationException(e);
            }
        }
        // send the notification email in case of an unlock operation
        if (dialogAction == TYPE_INIT) {
            //            try {
            // do init stuff
            //            } catch (CmsException e) {
            //                // collect exceptions to create a detailed output
            //                addMultiOperationException(e);
            //            }
        }
        // generate the error message for exception
        String message;
        switch (dialogAction) {
            case TYPE_INIT:
                message = Messages.ERR_WORKFLOW_INIT_0;
                break;
            case TYPE_SIGNAL:
                message = Messages.ERR_WORKFLOW_SIGNAL_0;
                break;
            case TYPE_ABORT:
                message = Messages.ERR_WORKFLOW_ABORT_0;
                break;
            default:
                message = Messages.ERR_WORKFLOW_UNKNOWN_0;
                break;
        }
        checkMultiOperationException(Messages.get(), message);

        return true;
    }

    /**
     * Performs the lock state operation on a single resource.<p>
     * 
     * @param resourceName the resource name to perform the operation on
     * @param dialogAction the lock action: lock, unlock or change lock
     * @throws CmsException if the operation fails
     */
    protected void performSingleResourceOperation(String resourceName, int dialogAction) throws CmsException {

        I_CmsWorkflowManager wfManager = OpenCms.getWorkflowManager();
        CmsProject wfProject;

        // read the resource to make sure it exists
        getCms().readResource(resourceName, CmsResourceFilter.ALL);

        // perform action depending on dialog uri
        switch (dialogAction) {
            case TYPE_INIT:
                wfProject = null;
                try {
                    I_CmsWorkflowType wfType = wfManager.getWorkflowType(getParamTasktype());
                    wfProject = wfManager.createTask(getCms(), getParamDescription());
                    wfManager.addResource(getCms(), wfProject, resourceName);
                    I_CmsWorkflowAction wfAction = wfManager.init(
                        getCms(),
                        wfProject,
                        wfType,
                        (Boolean.valueOf(getParamSendMail()).booleanValue()) ? getParamMailText() : null);

                    if (wfAction.equals(CmsDefaultWorkflowAction.FORWARD)) {
                        try {
                            String forwardUri = (String)wfAction.getParameter(CmsDefaultWorkflowAction.FORWARD_URI);
                            forwardUri = OpenCms.getLinkManager().substituteLink(getCms(), forwardUri);
                            setParamCloseLink(forwardUri);

                        } catch (Exception exc) {
                            LOG.error(this, exc);
                        }
                    }
                } catch (CmsException exc) {
                    // abort task if something failed here
                    if (wfProject != null) {
                        wfManager.abortTask(getCms(), wfProject, null);
                    }
                    throw (exc);
                }
                break;
            case TYPE_SIGNAL:
                wfProject = wfManager.getTask(getCms(), resourceName);
                I_CmsWorkflowAction wfAction = wfManager.signal(
                    getCms(),
                    wfProject,
                    wfManager.getWorkflowTransition(getParamTransition()),
                    (Boolean.valueOf(getParamSendMail()).booleanValue()) ? getParamMailText() : null);

                if (wfAction.equals(CmsDefaultWorkflowAction.FORWARD)) {
                    try {
                        String forwardUri = (String)wfAction.getParameter(CmsDefaultWorkflowAction.FORWARD_URI);
                        forwardUri = OpenCms.getLinkManager().substituteLink(getCms(), forwardUri);
                        setParamCloseLink(forwardUri);

                    } catch (Exception exc) {
                        LOG.error(this, exc);
                    }
                }
                break;
            case TYPE_ABORT:
                wfProject = wfManager.getTask(getCms(), resourceName);
                if (Boolean.valueOf(getParamUndo()).booleanValue()) {
                    wfManager.undoTask(
                        getCms(),
                        wfProject,
                        (Boolean.valueOf(getParamSendMail()).booleanValue()) ? getParamMailText() : null);
                } else {
                    wfManager.abortTask(
                        getCms(),
                        wfProject,
                        (Boolean.valueOf(getParamSendMail()).booleanValue()) ? getParamMailText() : null);
                }
                break;
            default:
        }
    }
}
