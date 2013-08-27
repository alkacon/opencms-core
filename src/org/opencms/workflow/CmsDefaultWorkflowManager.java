/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
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

import org.opencms.ade.publish.CmsPublish;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The default implementation of the workflow manager interface, which offers only publish functionality.<p>
 */
public class CmsDefaultWorkflowManager extends A_CmsWorkflowManager {

    /** The forced publish workflow action. */
    public static final String ACTION_FORCE_PUBLISH = "forcepublish";

    /** The publish workflow action. */
    public static final String ACTION_PUBLISH = "publish";

    /** The name for the publish action. */
    public static final String WORKFLOW_PUBLISH = "WORKFLOW_PUBLISH";

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#executeAction(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsWorkflowAction, org.opencms.ade.publish.shared.CmsPublishOptions, java.util.List)
     */
    public CmsWorkflowResponse executeAction(
        CmsObject userCms,
        CmsWorkflowAction action,
        CmsPublishOptions options,
        List<CmsResource> resources) throws CmsException {

        String actionKey = action.getAction();
        if (CmsWorkflowAction.ACTION_CANCEL.equals(actionKey)) {
            return new CmsWorkflowResponse(true, actionKey, null, null, null);
        } else if (ACTION_PUBLISH.equals(actionKey)) {
            return actionPublish(userCms, options, resources);
        } else if (ACTION_FORCE_PUBLISH.equals(actionKey)) {
            return actionForcePublish(userCms, options, resources);
        }
        throw new CmsInvalidActionException(actionKey);
    }

    /**
     * Gets the localized label for a given CMS context and key.<p>
     *  
     * @param cms the CMS context 
     * @param key the localization key 
     * 
     * @return the localized label 
     */
    public String getLabel(CmsObject cms, String key) {

        CmsMessages messages = Messages.get().getBundle(getLocale(cms));
        return messages.key(key);
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getWorkflowPublishResources(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsWorkflow, org.opencms.ade.publish.shared.CmsPublishOptions)
     */
    public List<CmsPublishResource> getWorkflowPublishResources(
        CmsObject cms,
        CmsWorkflow workflow,
        CmsPublishOptions options) {

        CmsPublish publish = new CmsPublish(cms, options);
        return publish.getPublishResourceBeans();
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getWorkflowResources(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsWorkflow, org.opencms.ade.publish.shared.CmsPublishOptions)
     */
    public List<CmsResource> getWorkflowResources(CmsObject cms, CmsWorkflow workflow, CmsPublishOptions options) {

        CmsPublish publish = new CmsPublish(cms, options);
        return publish.getPublishResources();
    }

    /**
     * @see org.opencms.workflow.I_CmsWorkflowManager#getWorkflows(org.opencms.file.CmsObject)
     */
    public Map<String, CmsWorkflow> getWorkflows(CmsObject cms) {

        Map<String, CmsWorkflow> result = new LinkedHashMap<String, CmsWorkflow>();
        List<CmsWorkflowAction> actions = new ArrayList<CmsWorkflowAction>();
        String publishLabel = getLabel(cms, Messages.GUI_WORKFLOW_ACTION_PUBLISH_0);
        CmsWorkflowAction publishAction = new CmsWorkflowAction(ACTION_PUBLISH, publishLabel, true, true);
        actions.add(publishAction);
        String workflowLabel = getLabel(cms, Messages.GUI_WORKFLOW_PUBLISH_0);
        CmsWorkflow publishWorkflow = new CmsWorkflow(WORKFLOW_PUBLISH, workflowLabel, actions);
        result.put(WORKFLOW_PUBLISH, publishWorkflow);
        return result;
    }

    /**
     * The implementation of the "forcepublish" workflow action.<p>
     * 
     * @param userCms the user CMS context 
     * @param resources the resources which the action should process 
     * @param options the publish options to use 
     * @return the workflow response
     *  
     * @throws CmsException if something goes wrong 
     */
    protected CmsWorkflowResponse actionForcePublish(
        CmsObject userCms,
        CmsPublishOptions options,
        List<CmsResource> resources) throws CmsException {

        CmsPublish publish = new CmsPublish(userCms, options.getParameters());
        publish.publishResources(resources);
        CmsWorkflowResponse response = new CmsWorkflowResponse(
            true,
            "",
            new ArrayList<CmsPublishResource>(),
            new ArrayList<CmsWorkflowAction>(),
            null);
        return response;
    }

    /**
     * The implementation of the "publish" workflow action.<p>
     * 
     * @param userCms the user CMS context 
     * @param options the publish options 
     * @param resources the resources which the action should process 
     * 
     * @return the workflow response 
     * @throws CmsException if something goes wrong 
     */
    protected CmsWorkflowResponse actionPublish(
        CmsObject userCms,
        CmsPublishOptions options,
        List<CmsResource> resources) throws CmsException {

        CmsPublish publish = new CmsPublish(userCms, options);
        List<CmsPublishResource> brokenResources = publish.getBrokenResources(resources);
        if (brokenResources.size() == 0) {
            publish.publishResources(resources);
            CmsWorkflowResponse response = new CmsWorkflowResponse(
                true,
                "",
                new ArrayList<CmsPublishResource>(),
                new ArrayList<CmsWorkflowAction>(),
                null);
            return response;
        } else {
            String brokenResourcesLabel = getLabel(userCms, Messages.GUI_BROKEN_LINKS_0);
            boolean canForcePublish = OpenCms.getWorkplaceManager().getDefaultUserSettings().isAllowBrokenRelations()
                || OpenCms.getRoleManager().hasRole(userCms, CmsRole.VFS_MANAGER);
            List<CmsWorkflowAction> actions = new ArrayList<CmsWorkflowAction>();
            if (canForcePublish) {
                String forceLabel = getLabel(userCms, Messages.GUI_WORKFLOW_ACTION_FORCE_PUBLISH_0);
                actions.add(new CmsWorkflowAction(ACTION_FORCE_PUBLISH, forceLabel, true, true));
            }
            CmsWorkflowResponse response = new CmsWorkflowResponse(
                false,
                brokenResourcesLabel,
                brokenResources,
                actions,
                null);
            return response;
        }
    }
}
