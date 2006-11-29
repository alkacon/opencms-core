
package org.opencms.workflow;

import org.opencms.workflow.generic.CmsDefaultWorkflowManager;

/**
 * Class to test internal methods of standard workflow manager.<p>
 */
public class WorkflowTestManager extends CmsDefaultWorkflowManager {

    /**
     * Internal constructor.
     */
    public WorkflowTestManager() {

        if (CmsDefaultWorkflowManager.m_workflowEngine == null) {
            CmsDefaultWorkflowManager.m_workflowEngine = new WorkflowTestEngine();
        }
    }
}