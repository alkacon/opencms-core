package org.opencms.workflow;

import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;

/**
 * Class to test internal methods of standard workflow manager.<p>
 */
public class WorkflowTestManager extends CmsDefaultWorkflowManager {
    
    /** The wrapped workflow manager. */ 
    private CmsDefaultWorkflowManager m_manager;
    
    
    /**
     * Internal constructor.
     */
    public WorkflowTestManager() {
        m_manager = new CmsDefaultWorkflowManager();
        if (CmsDefaultWorkflowManager.m_workflowEngine == null) {
            CmsDefaultWorkflowManager.m_workflowEngine = new WorkflowTestEngine();
        }
    }
    
    /**
     * @see org.opencms.workflow.CmsDefaultWorkflowManager#abortWorkflowProject(org.opencms.file.CmsProject)
     */
    public void abortWorkflowProject(CmsProject wfProject) throws CmsException {

        m_manager.abortWorkflowProject(wfProject);
    }
    
    /**
     * @see org.opencms.workflow.CmsDefaultWorkflowManager#addResourceToWorkflowProject(org.opencms.file.CmsProject, org.opencms.file.CmsResource)
     */
    public void addResourceToWorkflowProject (CmsProject wfProject, CmsResource resource) throws CmsException {

        m_manager.addResourceToWorkflowProject(wfProject, resource);
    }
    
    /**
     * @see org.opencms.workflow.CmsDefaultWorkflowManager#createWorkflowProject(org.opencms.file.CmsUser, java.lang.String, java.lang.String)
     */
    public CmsProject createWorkflowProject(CmsUser user, String name, String description) throws CmsException {

        return m_manager.createWorkflowProject(user, name, description);
    }
    
    /**
     * @see org.opencms.workflow.CmsDefaultWorkflowManager#publishWorkflowProject(org.opencms.file.CmsProject)
     */
    public void publishWorkflowProject(CmsProject wfProject) throws CmsException {

        m_manager.publishWorkflowProject(wfProject);
    }  
    
    /**
     * @see org.opencms.workflow.CmsDefaultWorkflowManager#undoWorkflowProject(org.opencms.file.CmsProject)
     */
    public void undoWorkflowProject(CmsProject wfProject) throws CmsException {

        m_manager.undoWorkflowProject(wfProject);
    }            
}