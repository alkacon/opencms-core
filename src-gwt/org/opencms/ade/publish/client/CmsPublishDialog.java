/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.publish.client;

import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * 
 * Main class for the publish dialog.<p>
 * 
 * This class is mostly responsible for the control flow and RPC calls of the publish dialog.
 * It delegates most of the actual GUI work to the {@link CmsPublishSelectPanel} and {@link CmsBrokenLinksPanel} classes.
 * 
 * @since 8.0.0
 * 
 */
public class CmsPublishDialog extends CmsPopup {

    /**
     * The action for publishing and/or removing resources from the publish list.<p>
     */
    private class CmsPublishAction extends CmsRpcAction<CmsWorkflowResponse> {

        /** If true, try to ignore broken links when publishing. */
        private CmsWorkflowAction m_action;

        /** Creates a new instance of this action. 
         * 
         * @param action the workflow action to execute
         */
        public CmsPublishAction(CmsWorkflowAction action) {

            m_action = action;
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
         */
        @Override
        public void execute() {

            setLoadingMessage(Messages.get().key(Messages.GUI_PUBLISHING_0));
            start(0, true);
            List<CmsUUID> resourcesToPublish = new ArrayList<CmsUUID>(m_publishSelectPanel.getResourcesToPublish());
            List<CmsUUID> resourcesToRemove = new ArrayList<CmsUUID>(m_publishSelectPanel.getResourcesToRemove());
            getService().executeAction(resourcesToPublish, resourcesToRemove, m_action, this);
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(CmsWorkflowResponse result) {

            onReceiveStatus(result);
            stop(true);
        }
    }

    /**
     * The action for loading the publish list.<p>
     */
    private class CmsPublishListAction extends CmsRpcAction<List<CmsPublishGroup>> {

        /**
         * Constructor.<p>
         */
        protected CmsPublishListAction() {

            // nothing to do
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
         */
        @Override
        public void execute() {

            start(0, true);
            getService().getResourceGroups(getSelectedWorkflow(), getPublishOptions(), this);
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(List<CmsPublishGroup> result) {

            onReceivePublishList(result);
            stop(false);
        }
    }

    /** The dialog width in pixels. */
    public static final int DIALOG_WIDTH = 766;

    /** The project map used by showPublishDialog. */
    public static Map<String, String> m_staticProjects;

    /** The CSS bundle used for this widget. */
    private static final I_CmsPublishCss CSS = I_CmsPublishLayoutBundle.INSTANCE.publishCss();

    /** Flag indicating if the CSS has been initialized. */
    private static boolean CSS_INITIALIZED;

    /** The index of the "broken links" panel. */
    private static final int PANEL_BROKEN_LINKS = 1;

    /** The index of the publish selection panel. */
    private static final int PANEL_SELECT = 0;

    /** The publish service instance. */
    private static I_CmsPublishServiceAsync SERVICE;

    /** The panel for selecting the resources to publish or remove from the publish list. */
    protected CmsPublishSelectPanel m_publishSelectPanel;

    /** The panel for showing the links that would be broken by publishing. */
    private CmsBrokenLinksPanel m_brokenLinksPanel;

    /** The root panel of this dialog which contains both the selection panel and the panel for displaying broken links. */
    private DeckPanel m_panel = new DeckPanel();

    /** The current publish list options. */
    private CmsPublishOptions m_publishOptions;

    /** The id of the current workflow. */
    private String m_workflowId;

    /** The available workflows. */
    private Map<String, CmsWorkflow> m_workflows;

    /**
     * Constructs a new publish dialog.<p>
     * 
     * @param initData the initial data 
     */
    public CmsPublishDialog(CmsPublishData initData) {

        super(Messages.get().key(Messages.GUI_PUBLISH_DIALOG_TITLE_0), 800);
        initCss();
        setGlassEnabled(true);
        setPositionFixed();
        setAutoHideEnabled(false);
        setModal(true);
        addStyleName(CSS.publishDialog());
        m_workflows = initData.getWorkflows();
        m_workflowId = initData.getSelectedWorkflowId();
        m_publishOptions = initData.getOptions();
        int availableHeight = Window.getClientHeight() - 290;
        m_publishSelectPanel = new CmsPublishSelectPanel(
            this,
            initData.getProjects(),
            initData.getOptions(),
            initData.getWorkflows(),
            initData.getSelectedWorkflowId(),
            availableHeight);
        m_brokenLinksPanel = new CmsBrokenLinksPanel(this, availableHeight);

        addDialogClose(null);

        m_panel = new DeckPanel();
        m_panel.add(m_publishSelectPanel);
        m_panel.add(m_brokenLinksPanel);
        setMainContent(m_panel);
        onReceivePublishList(initData.getGroups());
    }

    /**
     * Convenience method which opens a publish dialog.<p>
     */
    public static void showPublishDialog() {

        showPublishDialog(null);
    }

    /**
     * Convenience method which opens a publish dialog.<p>
     * 
     * @param handler the close handler
     */
    public static void showPublishDialog(final CloseHandler<PopupPanel> handler) {

        (new CmsRpcAction<CmsPublishData>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                getService().getInitData(this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsPublishData result) {

                stop(false);
                showPublishDialog(result, handler);
            }
        }).execute();
    }

    /**
     * 
     * @param result
     * @param handler
     */
    public static void showPublishDialog(CmsPublishData result, CloseHandler<PopupPanel> handler) {

        CmsPublishDialog publishDialog = new CmsPublishDialog(result);
        if (handler != null) {
            publishDialog.addCloseHandler(handler);
        }
        publishDialog.centerHorizontally(50);
        // replace current notification widget by overlay
        publishDialog.catchNotifications();
    }

    /**
     * Returns the publish service instance.<p>
     * 
     * @return the publish service instance
     */
    protected static I_CmsPublishServiceAsync getService() {

        if (SERVICE == null) {
            SERVICE = GWT.create(I_CmsPublishService.class);
            String serviceUrl = CmsCoreProvider.get().link("org.opencms.ade.publish.CmsPublishService.gwt");
            ((ServiceDefTarget)SERVICE).setServiceEntryPoint(serviceUrl);
        }
        return SERVICE;
    }

    /**
     * Executes the specified action for the selected resources.<p>
     * 
     * @param actionKey the workflow action
     */
    public void executeAction(CmsWorkflowAction actionKey) {

        (new CmsPublishAction(actionKey)).execute();
    }

    /**
     * Returns the current publish options.<p>
     * 
     * @return a publish options bean
     */
    public CmsPublishOptions getPublishOptions() {

        return m_publishOptions;
    }

    /**
     * Method which is called when the cancel button is pressed.<p>
     */
    public void onCancel() {

        hide();
    }

    /**
     * Method which is called when the publish options are changed.<p>
     */
    public void onChangeOptions() {

        (new CmsPublishListAction()).execute();
    }

    /**
     * Method which is  called when the back button is pressed.<p>
     */
    public void onGoBack() {

        setPanel(PANEL_SELECT);
    }

    /**
     * Method which is called after the publish list has been received from the server.<p>
     * 
     * @param groups the groups of the publish list
     */
    public void onReceivePublishList(List<CmsPublishGroup> groups) {

        m_publishSelectPanel.setGroups(groups, true);
        setPanel(PANEL_SELECT);
        if (!isVisible()) {
            center();
        }
    }

    /**
     * Method which is called after the status from a publish action has arrived.<p>
     * 
     * @param brokenResources the list of broken resources
     */
    public void onReceiveStatus(CmsWorkflowResponse brokenResources) {

        if (brokenResources.isSuccess()) {
            hide();
        } else {
            m_brokenLinksPanel.setEntries(brokenResources.getResources(), brokenResources.getAvailableActions());
            setPanel(PANEL_BROKEN_LINKS);
        }
    }

    /**
     * Sets the include related resources option.<p>
     * 
     * @param includeRelated the include related option
     */
    public void setIncludeRelated(boolean includeRelated) {

        m_publishOptions.setIncludeRelated(includeRelated);
    }

    /**
     * Sets the include sibling resources option.<p>
     * 
     * @param includeSiblings the include siblings option
     */
    public void setIncludeSiblings(boolean includeSiblings) {

        m_publishOptions.setIncludeSiblings(includeSiblings);
    }

    /**
     * Changes the currently active panel.<p>
     * 
     * @param panelId the number of the panel to show
     */
    public void setPanel(int panelId) {

        m_panel.showWidget(panelId);
        removeAllButtons();
        if (panelId == PANEL_SELECT) {
            for (CmsPushButton button : m_publishSelectPanel.getButtons()) {
                addButton(button);
            }
        } else if (panelId == PANEL_BROKEN_LINKS) {
            for (CmsPushButton button : m_brokenLinksPanel.getButtons()) {
                addButton(button);
            }
        }
    }

    /**
     * Sets the selected project id.<p>
     * 
     * @param projectId the project id
     */
    public void setProjectId(CmsUUID projectId) {

        m_publishOptions.setProjectId(projectId);
    }

    /**
     * Sets the selected workflow id.<p>
     * 
     * @param workflowId the workflow id
     */
    public void setWorkflowId(String workflowId) {

        m_workflowId = workflowId;
    }

    /**
     * Returns the selected workflow.<p>
     * 
     * @return the selected workflow
     */
    protected CmsWorkflow getSelectedWorkflow() {

        return m_workflows.get(m_workflowId);
    }

    /**
     * Ensures all style sheets are loaded.<p>
     */
    private void initCss() {

        if (!CSS_INITIALIZED) {
            I_CmsPublishLayoutBundle.INSTANCE.publishCss().ensureInjected();
            CSS_INITIALIZED = true;
        }
    }

}
