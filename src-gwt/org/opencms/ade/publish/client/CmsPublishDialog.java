/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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
import org.opencms.ade.publish.shared.CmsPublishGroupList;
import org.opencms.ade.publish.shared.CmsPublishListToken;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.ade.publish.shared.CmsWorkflowActionParams;
import org.opencms.ade.publish.shared.CmsWorkflowResponse;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishServiceAsync;
import org.opencms.gwt.client.CmsCoreProvider;
import org.opencms.gwt.client.CmsEditableDataJSO;
import org.opencms.gwt.client.rpc.CmsRpcAction;
import org.opencms.gwt.client.ui.CmsNotification;
import org.opencms.gwt.client.ui.CmsPopup;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.contenteditor.I_CmsContentEditorHandler;
import org.opencms.gwt.client.ui.contextmenu.CmsContextMenuHandler;
import org.opencms.gwt.client.ui.resourceinfo.CmsResourceInfoView;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
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
     * A type which represents the state of a publish action.<p>
     */
    public enum State {
        /** The publish dialog was cancelled. */
        cancel,

        /** The publish dialog has failed. */
        failure,

        /** The publish dialog has succeeded. */
        success;
    }

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

            start(0, true);
            setLastAction(m_action);
            CmsWorkflowActionParams actionParams = getWorkflowActionParams();
            getService().executeAction(m_action, actionParams, this);

        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(CmsWorkflowResponse result) {

            stop(false);
            onReceiveStatus(result);

        }
    }

    /**
     * The action for loading the publish list.<p>
     */
    private class CmsPublishListAction extends CmsRpcAction<CmsPublishGroupList> {

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
            CmsPublishOptions options = getPublishOptions();
            boolean projectChanged = m_projectChanged;
            m_projectChanged = false;

            getService().getResourceGroups(getSelectedWorkflow(), options, projectChanged, this);
        }

        /**
         * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
         */
        @Override
        protected void onResponse(CmsPublishGroupList result) {

            stop(false);
            onReceivePublishList(result);

        }
    }

    /** The dialog width in pixels. */
    public static final int DIALOG_WIDTH = 766;

    /** The project map used by showPublishDialog. */
    public static Map<String, String> m_staticProjects;

    /** The index of the "broken links" panel. */
    public static final int PANEL_BROKEN_LINKS = 1;

    /** The index of the publish selection panel. */
    public static final int PANEL_SELECT = 0;

    /** The CSS bundle used for this widget. */
    private static final I_CmsPublishCss CSS = I_CmsPublishLayoutBundle.INSTANCE.publishCss();
    /** Flag indicating if the CSS has been initialized. */
    private static boolean CSS_INITIALIZED;

    /** The publish service instance. */
    private static I_CmsPublishServiceAsync SERVICE;

    /** The panel for selecting the resources to publish or remove from the publish list. */
    protected CmsPublishSelectPanel m_publishSelectPanel;

    /** Flag to keep track of whether the user just changed the project. */
    boolean m_projectChanged;

    /** The panel for showing the links that would be broken by publishing. */
    private CmsBrokenLinksPanel m_brokenLinksPanel;

    /** The content editor handler. */
    private I_CmsContentEditorHandler m_editorHandler;

    /** Stores a failure message. */
    private String m_failureMessage;

    /** Stores the last workflow action. */
    private CmsWorkflowAction m_lastAction;

    /** The context menu handler. */
    private CmsContextMenuHandler m_menuHandler;

    /** The root panel of this dialog which contains both the selection panel and the panel for displaying broken links. */
    private DeckPanel m_panel = new DeckPanel();

    /** The current publish list options. */
    private CmsPublishOptions m_publishOptions;

    /** Stores the state. */
    private State m_state = State.cancel;

    /** The id of the current workflow. */
    private String m_workflowId;

    /** The available workflows. */
    private Map<String, CmsWorkflow> m_workflows;

    /**
     * Constructs a new publish dialog.<p>
     *
     * @param initData the initial data
     * @param refreshAction the action to perform on a context menu triggered refresh
     * @param editorHandler the content editor handler
     */
    public CmsPublishDialog(
        CmsPublishData initData,
        final Runnable refreshAction,
        I_CmsContentEditorHandler editorHandler) {

        super(800);
        initCss();
        setGlassEnabled(true);
        setPositionFixed();
        setAutoHideEnabled(false);
        setModal(true);
        addStyleName(CSS.publishDialog());
        m_menuHandler = new CmsResourceInfoView.ContextMenuHandler() {

            @Override
            public void refreshResource(CmsUUID structureId) {

                CmsPublishDialog.this.hide();
                refreshAction.run();
            }
        };
        m_editorHandler = editorHandler;
        m_menuHandler.setEditorHandler(editorHandler);
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
     * Shows the publish dialog.<p>
     *
     * @param result the publish data
     * @param handler the dialog close handler (may be null)
     * @param refreshAction the action to execute on a context menu triggered refresh
     * @param editorHandler the content editor handler (may be null)
     */
    public static void showPublishDialog(
        CmsPublishData result,
        CloseHandler<PopupPanel> handler,
        Runnable refreshAction,
        I_CmsContentEditorHandler editorHandler) {

        CmsPublishDialog publishDialog = new CmsPublishDialog(result, refreshAction, editorHandler);
        if (handler != null) {
            publishDialog.addCloseHandler(handler);
        }
        publishDialog.centerHorizontally(50);
        // replace current notification widget by overlay
        publishDialog.catchNotifications();
    }

    /**
     * Convenience method which opens a publish dialog.<p>
     *
     * @param handler the close handler
     * @param params the additional publish dialog parameters
     * @param refreshAction the action to execute after a context menu triggered refresh
     * @param editorHandler the content editor handler
     */
    public static void showPublishDialog(
        final HashMap<String, String> params,
        final CloseHandler<PopupPanel> handler,
        final Runnable refreshAction,
        final I_CmsContentEditorHandler editorHandler) {

        List<String> structureIds = Lists.newArrayList();
        for (CmsEditableDataJSO editableData : CmsDomUtil.getAllEditableDataForPage()) {
            structureIds.add("" + editableData.getStructureId());
        }

        List<Element> collectorInfos = CmsDomUtil.getElementsByClass(CmsGwtConstants.CLASS_COLLECTOR_INFO);
        int j = 1;
        for (Element elem : collectorInfos) {
            String infoJson = elem.getAttribute("rel");
            params.put(CmsPublishOptions.PARAM_COLLECTOR_INFO + "." + j, infoJson);
            j += 1;
        }
        String editableIdList = CmsStringUtil.listAsString(structureIds, ",");
        params.put(CmsPublishOptions.PARAM_COLLECTOR_ITEMS, editableIdList);

        (new CmsRpcAction<CmsPublishData>() {

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#execute()
             */
            @Override
            public void execute() {

                start(0, true);
                getService().getInitData(params, this);
            }

            /**
             * @see org.opencms.gwt.client.rpc.CmsRpcAction#onResponse(java.lang.Object)
             */
            @Override
            protected void onResponse(CmsPublishData result) {

                stop(false);
                showPublishDialog(result, handler, refreshAction, editorHandler);
            }
        }).execute();
    }

    /**
     * Convenience method which opens a publish dialog.<p>
     *
     * @param refreshAction the action to execute after a context menu triggered refresh
     */
    public static void showPublishDialog(Runnable refreshAction) {

        showPublishDialog(new HashMap<String, String>(), null, refreshAction, null);
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
     * Gets the context menu handler.<p>
     *
     * @return the context menu handler
     */
    public CmsContextMenuHandler getContextMenuHandler() {

        return m_menuHandler;
    }

    /**
     * Returns the content editor handler.<p>
     *
     * @return the content editor handler
     */
    public I_CmsContentEditorHandler getEditorHandler() {

        return m_editorHandler;
    }

    /**
     * Gets the failure message.<p>
     *
     * @return the failure message
     */
    public String getFailureMessage() {

        return m_failureMessage;
    }

    /**
     * Gets the last workflow action.<p>
     *
     * @return the last workflow action
     */
    public CmsWorkflowAction getLastAction() {

        return m_lastAction;
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
     * Gets the publish dialog state.<p>
     *
     * @return the publish dialog state
     */
    public State getState() {

        return m_state;
    }

    /**
     * Checks whether the publish dialog has failed.<p>
     *
     * @return checks whether the publish dialog has succeeded
     */
    public boolean hasFailed() {

        return m_state == State.failure;
    }

    /**
     * Checks whether the publish dialog has succeeded.<p>
     *
     * @return true if the publish dialog has succeeded
     */
    public boolean hasSucceeded() {

        return m_state == State.success;
    }

    /**
     * Method which is called when the cancel button is pressed.<p>
     */
    public void onCancel() {

        if (m_publishSelectPanel.m_model != null) {
            final List<CmsUUID> toRemove = m_publishSelectPanel.m_model.getIdsOfAlreadyPublishedResources();
            if (toRemove.isEmpty()) {
                hide();
            } else {
                CmsRpcAction<CmsWorkflowResponse> action = new CmsRpcAction<CmsWorkflowResponse>() {

                    @Override
                    public void execute() {

                        start(0, true);
                        CmsWorkflowActionParams params = getWorkflowActionParams();
                        getService().executeAction(
                            new CmsWorkflowAction(CmsWorkflowAction.ACTION_CANCEL, "", true),
                            params,
                            this);
                    }

                    @Override
                    protected void onResponse(CmsWorkflowResponse result) {

                        stop(false);
                        hide();

                    }
                };
                action.execute();
            }
        } else {
            hide();
        }
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
    public void onReceivePublishList(CmsPublishGroupList groups) {

        m_publishSelectPanel.setGroupList(groups, true, groups.getOverrideWorkflowId());
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
            succeed();
            hide();
            CmsNotification.get().send(
                CmsNotification.Type.NORMAL,
                org.opencms.gwt.client.Messages.get().key(org.opencms.gwt.client.Messages.GUI_DONE_0));

        } else {
            m_failureMessage = brokenResources.getMessage();
            m_state = State.failure;
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
            m_publishSelectPanel.updateDialogTitle();
        } else if (panelId == PANEL_BROKEN_LINKS) {
            for (CmsPushButton button : m_brokenLinksPanel.getButtons()) {
                addButton(button);
            }
            m_brokenLinksPanel.updateTitle();
        }
    }

    /**
     * This is called when the user just changed the project.<p>
     */
    public void setProjectChanged() {

        m_projectChanged = true;
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
        if (!m_workflows.containsKey(workflowId)) {
            m_workflowId = "WORKFLOW_PUBLISH";
        }
    }

    /**
     * Sets the publish dialog state to 'success'.<p>
     */
    public void succeed() {

        m_state = State.success;
        CmsCoreProvider.get().fireEvent(new CmsPublishEvent());
    }

    /**
     * Method which is called when the publish options are changed.<p>
     */
    public void updateResourceList() {

        (new CmsPublishListAction()).execute();
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
     * Gets the workflow action parameters to which the workflow action should be applied.<p>
     *
     * @return the workflow action parameters
     */
    protected CmsWorkflowActionParams getWorkflowActionParams() {

        if (m_publishSelectPanel.isShowResources()) {
            List<CmsUUID> resourcesToPublish = new ArrayList<CmsUUID>(m_publishSelectPanel.getResourcesToPublish());
            List<CmsUUID> resourcesToRemove = new ArrayList<CmsUUID>(m_publishSelectPanel.getResourcesToRemove());
            CmsWorkflowActionParams actionParams = new CmsWorkflowActionParams(resourcesToPublish, resourcesToRemove);
            return actionParams;
        } else {
            CmsPublishOptions options = getPublishOptions();
            CmsWorkflow workflow = getSelectedWorkflow();
            return new CmsWorkflowActionParams(new CmsPublishListToken(workflow, options));

        }
    }

    /**
     * Sets the last workflow action.<p>
     *
     * @param action a workflow action
     */
    protected void setLastAction(CmsWorkflowAction action) {

        m_lastAction = action;
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
