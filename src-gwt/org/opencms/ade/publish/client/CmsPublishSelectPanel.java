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

import org.opencms.ade.publish.client.CmsPublishItemStatus.Signal;
import org.opencms.ade.publish.shared.CmsProjectBean;
import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsWorkflow;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.ui.input.CmsTriStateCheckBox;
import org.opencms.gwt.client.ui.input.CmsTriStateCheckBox.State;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.gwt.client.util.CmsMessages;
import org.opencms.gwt.client.util.CmsScrollToBottomHandler;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This is the main widget of the publish dialog.<p>
 * 
 * It allows the user to choose which resources from the publish list should be published
 * and/or removed from the publish list.
 * 
 * @since 8.0.0
 */
public class CmsPublishSelectPanel extends Composite
implements I_CmsPublishSelectionChangeHandler, I_CmsPublishItemStatusUpdateHandler {

    /**
     * Data with which to update a check box.
     */
    public static class CheckBoxUpdate {

        /** The new state. */
        private CmsTriStateCheckBox.State m_state;

        /** The new text. */
        private String m_text;

        /**
         * Gets the new state.<p>
         * 
         * @return the new state 
         */
        public CmsTriStateCheckBox.State getState() {

            return m_state;
        }

        /**
         * Gets the new text.<p>
         * 
         * @return the new text 
         */
        public String getText() {

            return m_text;
        }

        /**
         * Sets the new state.<p>
         * 
         * @param state the new state 
         */
        public void setState(CmsTriStateCheckBox.State state) {

            m_state = state;
        }

        /**
         * Sets the new text.<p>
         * 
         * @param text the new text 
         */
        public void setText(String text) {

            m_text = text;
        }

    }

    /** The UiBinder interface for this widget. */
    protected interface I_CmsPublishSelectPanelUiBinder extends UiBinder<Widget, CmsPublishSelectPanel> {
        // empty
    }

    /**
     * Command for adding more list items to the list of publish items.<p>
     */
    protected class MoreItemsCommand implements RepeatingCommand {

        /** The number of items left to add. */
        private int m_numItems;

        /**
         * Creates a new instance.<p>
         * 
         * @param numItems the maximal number of items to add  
         */
        public MoreItemsCommand(int numItems) {

            m_numItems = numItems;
        }

        /**
         * @see com.google.gwt.core.client.Scheduler.RepeatingCommand#execute()
         */
        public boolean execute() {

            if (m_numItems == 0) {
                finishLoading();
                return false;
            }
            boolean hasMore = CmsPublishSelectPanel.this.addNextItem();
            if (!hasMore) {
                finishLoading();
                return false;
            }
            m_numItems -= 1;
            return true;
        }

    }

    /** The CSS bundle used for this widget. */
    private static final I_CmsPublishCss CSS = I_CmsPublishLayoutBundle.INSTANCE.publishCss();

    /**
     * When dynamically adding groups on scrolling, the number of groups should be calculated such that the total sum of resources
     * in the groups is the smallest number greater or equal to this constant.<p> 
     */
    private static final int MIN_BATCH_SIZE = 20;

    /** The scroll threshold for the list of problem resources. */
    private static final int SCROLL_THRESHOLD = 200;

    /** The UiBinder instance used for this widget. */
    private static final I_CmsPublishSelectPanelUiBinder UI_BINDER = GWT.create(I_CmsPublishSelectPanelUiBinder.class);

    /** The button for escaping from the publish dialog. */
    @UiField
    protected CmsPushButton m_cancelButton;

    /** The checkbox for the "show problems only" mode. */
    @UiField
    protected CmsCheckBox m_checkboxProblems;

    /** The checkbox for including related resources. */
    @UiField
    protected CmsCheckBox m_checkboxRelated;

    /** The checkbox for including sibling resources. */
    @UiField
    protected CmsCheckBox m_checkboxSiblings;

    /** The panel containing the publish groups. */
    @UiField
    protected Panel m_groupPanelContainer;

    /** Flag to indicate whether new items are currently being added to the list. */
    protected boolean m_loading;

    /** The data model for the publish dialog. */
    protected CmsPublishDataModel m_model;

    /** The label which is displayed when there are no resources to publish. */
    @UiField
    protected Label m_noResources;

    /** The panel which shows a message telling the user the number of problems. */
    @UiField
    protected Panel m_problemsPanel;

    /** The project select box. */
    @UiField
    protected CmsSelectBox m_projectSelector;

    /** The publish dialog which contains this panel. */
    protected CmsPublishDialog m_publishDialog;

    /** The scroll panel containing the group panel. */
    @UiField
    protected CmsScrollPanel m_scrollPanel;

    /** The global map of selection controllers for all groups. */
    protected Map<CmsUUID, CmsPublishItemSelectionController> m_selectionControllers = Maps.newHashMap();

    /** The label shown in front of the project selector. */
    @UiField
    protected InlineLabel m_selectorLabel;

    /** The panel containing the project selector. */
    @UiField
    protected FlowPanel m_selectorPanel;

    /** The top button bar. */
    @UiField
    protected Panel m_topBar;

    /** The workflow selector. */
    @UiField
    protected CmsSelectBox m_workflowSelector;

    /** The workflow selector label. */
    @UiField
    protected InlineLabel m_workflowsLabel;

    /** The action buttons. */
    private List<CmsPushButton> m_actionButtons;

    /** The available actions. */
    private List<CmsWorkflowAction> m_actions;

    /** The current group index used for scrolling. */
    private int m_currentGroupIndex;

    /** The current group panel. */
    private CmsPublishGroupPanel m_currentGroupPanel;

    /** The list of group panels for each publish list group. */
    private List<CmsPublishGroupPanel> m_groupPanels = new ArrayList<CmsPublishGroupPanel>();

    /** Flag indicating that the panel has been initialized. */
    private boolean m_initialized;
    /** Checkbox for selecting/deselecting all items. */
    private CmsTriStateCheckBox m_selectAll;

    /** Flag which indicates whether only resources with problems should be shown. */
    private boolean m_showProblemsOnly;

    /**
     * Creates a new instance.<p>
     * 
     * @param publishDialog the publish dialog to which this panel should belong
     * @param projects a map of projects, where the keys are the project ids and the values are the names of the projects 
     * @param publishOptions the initial publish options
     * @param workflows the available workflows
     * @param selectedWorkflowId the selected workflow id
     * @param scrollPanelHeight the available scroll panel height
     */
    public CmsPublishSelectPanel(
        CmsPublishDialog publishDialog,
        List<CmsProjectBean> projects,
        CmsPublishOptions publishOptions,
        Map<String, CmsWorkflow> workflows,
        String selectedWorkflowId,
        int scrollPanelHeight) {

        m_publishDialog = publishDialog;
        m_actions = workflows.get(selectedWorkflowId).getActions();
        m_actionButtons = new ArrayList<CmsPushButton>();
        initWidget(UI_BINDER.createAndBindUi(this));
        m_selectAll = new CmsTriStateCheckBox("");
        m_selectAll.setNextStateAfterIntermediateState(State.on);
        m_selectAll.addValueChangeHandler(new ValueChangeHandler<CmsTriStateCheckBox.State>() {

            public void onValueChange(ValueChangeEvent<State> event) {

                State state = event.getValue();
                if (state == State.on) {
                    m_model.signalAll(Signal.publish);
                } else if (state == State.off) {
                    m_model.signalAll(Signal.unpublish);
                }
            }
        });
        m_topBar.add(m_selectAll);

        m_scrollPanel.getElement().getStyle().setPropertyPx(CmsDomUtil.Style.maxHeight.toString(), scrollPanelHeight);
        m_checkboxProblems.setVisible(false);
        CmsMessages messages = Messages.get();
        LinkedHashMap<String, String> workflowSelectorItems = new LinkedHashMap<String, String>();
        for (CmsWorkflow workflow : workflows.values()) {
            workflowSelectorItems.put(workflow.getId(), workflow.getNiceName());
        }
        LinkedHashMap<String, String> projectSelectItems = new LinkedHashMap<String, String>();
        projectSelectItems.put(CmsUUID.getNullUUID().toString(), messages.key(Messages.GUI_PUBLISH_DIALOG_MY_CHANGES_0));
        boolean foundOldProject = false;
        boolean selectedWorkflowProject = false;
        for (CmsProjectBean project : projects) {
            if (project.isWorkflowProject()) {
                workflowSelectorItems.put(project.getId().toString(), project.getName());
                if (project.getId().equals(publishOptions.getProjectId())) {
                    selectedWorkflowProject = true;
                }
            } else {
                projectSelectItems.put(project.getId().toString(), project.getName());
                // look if the project id from the last publish list is among the available projects.
                // (this might not be the case if the project has been deleted in the meantime.)
                if (project.getId().equals(publishOptions.getProjectId())) {
                    foundOldProject = true;
                }
            }
        }

        m_workflowSelector.setItems(workflowSelectorItems);

        m_workflowSelector.addStyleName(CSS.selector());
        if (!(workflows.size() > 1)) {
            m_workflowSelector.setEnabled(false);
        }

        m_workflowsLabel.setText(messages.key(Messages.GUI_PUBLISH_WORKFLOW_SELECT_0));

        m_projectSelector.setItems(projectSelectItems);
        m_projectSelector.addStyleName(CSS.selector());
        if (!publishOptions.getProjectId().isNullUUID() && foundOldProject) {
            m_projectSelector.setFormValueAsString(publishOptions.getProjectId().toString());
        }

        m_checkboxRelated.setChecked(publishOptions.isIncludeRelated());
        m_checkboxSiblings.setChecked(publishOptions.isIncludeSiblings());
        if (selectedWorkflowProject) {
            m_projectSelector.setEnabled(false);
            m_workflowSelector.setFormValueAsString(publishOptions.getProjectId().toString());
        } else {
            m_workflowSelector.setFormValueAsString(selectedWorkflowId);
        }
        m_cancelButton.setText(messages.key(Messages.GUI_PUBLISH_DIALOG_CANCEL_BUTTON_0));
        m_cancelButton.setUseMinWidth(true);
        //        m_selectAll.setText(messages.key(Messages.GUI_PUBLISH_TOP_PANEL_ALL_BUTTON_0));
        //        m_selectAll.setImageClass(I_CmsInputLayoutBundle.INSTANCE.inputCss().checkBoxImageChecked());
        //        m_selectAll.setUseMinWidth(true);
        //        m_selectNone.setText(messages.key(Messages.GUI_PUBLISH_TOP_PANEL_NONE_BUTTON_0));
        //        m_selectNone.setImageClass(I_CmsInputLayoutBundle.INSTANCE.inputCss().checkBoxImageUnchecked());
        //        m_selectNone.setUseMinWidth(true);
        m_noResources.setText(messages.key(Messages.GUI_PUBLISH_DIALOG_NO_RES_0));
        m_checkboxSiblings.setText(messages.key(Messages.GUI_PUBLISH_CHECKBOXES_SIBLINGS_0));
        m_checkboxRelated.setText(messages.key(Messages.GUI_PUBLISH_CHECKBOXES_REL_RES_0));
        m_checkboxProblems.setText(messages.key(Messages.GUI_PUBLISH_CHECKBOXES_PROBLEMS_0));
        m_selectorLabel.setText(messages.key(Messages.GUI_PUBLISH_TOP_PANEL_RIGHT_LABEL_0));
        addScrollHandler();
        m_initialized = true;
    }

    /** 
     * Formats a number of publish resources in a more user-friendly form.<p>
     * 
     * @param resourceCount a number of resources
     *  
     * @return the formatted number of resources 
     */
    public static String formatResourceCount(int resourceCount) {

        return Messages.get().key(Messages.GUI_RESOURCE_COUNT_1, "" + resourceCount);
    }

    /**
     * Updates the state of a check box used for selecting/deselecting items.<p>
     * 
     * @param states the selection states of the items for the check box 
     * 
     * @return the data needed to update the check box 
     */
    public static CheckBoxUpdate updateCheckbox(Set<CmsPublishItemStatus.State> states) {

        CheckBoxUpdate result = new CheckBoxUpdate();
        boolean hasPublish = states.contains(CmsPublishItemStatus.State.publish);
        boolean hasNormal = states.contains(CmsPublishItemStatus.State.normal);
        String textSelectAll = Messages.get().key(Messages.GUI_CHECKBOX_SELECT_ALL_0);
        String textDeselectAll = Messages.get().key(Messages.GUI_CHECKBOX_DESELECT_ALL_0);
        if (hasNormal && hasPublish) {
            result.setText(textSelectAll);
            result.setState(CmsTriStateCheckBox.State.middle);
        } else if (hasNormal) {
            result.setText(textSelectAll);
            result.setState(CmsTriStateCheckBox.State.off);
        } else if (hasPublish) {
            result.setText(textDeselectAll);
            result.setState(CmsTriStateCheckBox.State.on);
        } else {
            result.setText(textSelectAll);
            result.setState(CmsTriStateCheckBox.State.off);
        }
        return result;
    }

    /** 
     * Check for problems with new/deleted folders in the publish selection.<p>
     * 
     * @param resourceIds the ids of the resources selected for publishing 
     * @return true if there are problems with nested 
     */
    public boolean checkForProblems(Set<CmsUUID> resourceIds) {

        List<CmsPublishResource> pubResources = new ArrayList<CmsPublishResource>();
        Set<CmsUUID> publishIds = getResourcesToPublish();
        for (CmsUUID publishId : publishIds) {
            pubResources.add(m_model.getPublishResources().get(publishId));
        }
        for (CmsPublishResource pubResource : pubResources) {
            String parentPath = CmsResource.getParentFolder(pubResource.getName());
            CmsPublishResource parent = m_model.getPublishResourcesByPath().get(parentPath);
            if (parent != null) {
                boolean parentIsNew = parent.getState().isNew();
                boolean parentIsDeleted = parent.getState().isDeleted();
                if (parentIsNew || parentIsDeleted) {
                    if (!resourceIds.contains(parent.getId())) {
                        String title = Messages.get().key(Messages.ERR_CANT_PUBLISH_RESOURCE_TITLE_0);
                        String message = null;
                        if (parentIsNew) {
                            message = Messages.get().key(
                                Messages.ERR_PUBLISH_CANT_PUBLISH_NEW_RESOURCE_2,
                                pubResource.getName(),
                                parent.getName());
                        }
                        if (parentIsDeleted) {
                            message = Messages.get().key(
                                Messages.ERR_PUBLISH_CANT_PUBLISH_DELETED_RESOURCE_2,
                                pubResource.getName(),
                                parent.getName());
                        }
                        CmsAlertDialog alert = new CmsAlertDialog(title, message);
                        alert.center();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns the buttons of this panel which should be shown as the buttons of the publish dialog.<p>
     * 
     * @return a list of buttons
     */
    public List<CmsPushButton> getButtons() {

        List<CmsPushButton> result = new ArrayList<CmsPushButton>();
        result.add(m_cancelButton);
        m_actionButtons.clear();
        boolean enable = shouldEnablePublishButton();
        if (m_actions != null) {
            for (final CmsWorkflowAction action : m_actions) {
                CmsPushButton actionButton = new CmsPushButton();
                actionButton.setText(action.getLabel());
                actionButton.setUseMinWidth(true);
                actionButton.addClickHandler(new ClickHandler() {

                    public void onClick(ClickEvent event) {

                        executeAction(action);
                    }
                });
                actionButton.setEnabled(enable);
                m_actionButtons.add(actionButton);
            }
        }
        result.addAll(m_actionButtons);
        return result;
    }

    /**
     * Returns the ids of the resources which should be published.<p>
     * 
     * @return a set of id strings 
     */
    public Set<CmsUUID> getResourcesToPublish() {

        return new HashSet<CmsUUID>(m_model.getPublishIds());
    }

    /**
     * Returns the set of ids of resources which have been selected for removal.<p>
     * 
     * @return a set of id strings
     */
    public Set<CmsUUID> getResourcesToRemove() {

        Set<CmsUUID> result = new HashSet<CmsUUID>(m_model.getRemoveIds());
        result.addAll(m_model.getIdsOfAlreadyPublishedResources());
        return result;
    }

    /**
     * Gets the global map of selection controllers.<p>
     * 
     * @return the map of selection controller 
     */
    public Map<CmsUUID, CmsPublishItemSelectionController> getSelectionControllers() {

        return m_selectionControllers;
    }

    /**
     * @see org.opencms.ade.publish.client.I_CmsPublishSelectionChangeHandler#onChangePublishSelection()
     */
    public void onChangePublishSelection() {

        enableActions(shouldEnablePublishButton());
        Map<Integer, Set<CmsPublishItemStatus.State>> states = m_model.computeGroupSelectionStates();
        for (Map.Entry<Integer, Set<CmsPublishItemStatus.State>> entry : states.entrySet()) {
            int key = entry.getKey().intValue();
            if (key == -1) {
                updateCheckboxState(entry.getValue());
            } else {
                if (key < m_groupPanels.size()) {
                    m_groupPanels.get(key).updateCheckboxState(entry.getValue());
                }
            }

        }
    }

    /**
     * Sets the publish groups used by this widget.<p>
     * 
     * @param groups the new publish groups
     * @param newData true if the groups are new data which has been loaded  
     */
    public void setGroups(List<CmsPublishGroup> groups, boolean newData) {

        m_model = new CmsPublishDataModel(groups, this);
        m_model.setSelectionChangeAction(new Runnable() {

            public void run() {

                onChangePublishSelection();
            }
        });
        m_currentGroupIndex = 0;
        m_currentGroupPanel = null;
        m_problemsPanel.clear();
        if (newData) {
            m_showProblemsOnly = false;
            m_checkboxProblems.setChecked(false);
            m_checkboxProblems.setVisible(false);
            m_problemsPanel.setVisible(false);
        }
        m_groupPanels.clear();
        m_groupPanelContainer.clear();
        m_scrollPanel.onResizeDescendant();
        enableActions(false);

        int numGroups = groups.size();
        setResourcesVisible(numGroups > 0);

        if (numGroups == 0) {
            return;
        }

        enableActions(true);
        addMoreListItems();
        showProblemCount(m_model.countProblems());
    }

    /**
     * Returns true if the publish button should be enabled.<p>
     * 
     * @return true if the publish button should be enabled 
     */
    public boolean shouldEnablePublishButton() {

        boolean enablePublishButton = (getResourcesToRemove().size() != 0) || (getResourcesToPublish().size() != 0);
        return enablePublishButton;

    }

    /**
     * @see org.opencms.ade.publish.client.I_CmsPublishItemStatusUpdateHandler#update(org.opencms.util.CmsUUID, org.opencms.ade.publish.client.CmsPublishItemStatus)
     */
    public void update(CmsUUID id, CmsPublishItemStatus status) {

        CmsPublishItemSelectionController selectionController = m_selectionControllers.get(id);
        if (selectionController != null) {
            selectionController.update(status);
        }
    }

    /**
     * Updates the dialog title.<p>
     **/
    public void updateDialogTitle() {

        String title;
        if (m_model.getGroups().size() > 1) {
            title = Messages.get().key(
                Messages.GUI_PUBLISH_DIALOG_TITLE_3,
                m_publishDialog.getSelectedWorkflow().getNiceName(),
                String.valueOf(m_model.getGroups().size()),
                String.valueOf(m_model.getPublishResources().size()));
        } else {
            title = m_publishDialog.getSelectedWorkflow().getNiceName();
        }
        m_publishDialog.setCaption(title);
    }

    /**
     * Adds more publish list items to the panel.<p>
     */
    protected void addMoreListItems() {

        MoreItemsCommand cmd = new MoreItemsCommand(MIN_BATCH_SIZE);
        // we use a repeating command instead of a loop because a loop locks up the browser for too long in IE7.
        Scheduler.get().scheduleFixedDelay(cmd, 0);
    }

    /**
     * Tries to add a new publish list item to the panel, and returns false if there aren't any items left.<p>
     * 
     * @return true if an item could be added, false if no items are left 
     */
    protected boolean addNextItem() {

        // this method is so complicated because to add the next item, 
        // you may need to skip to another group and create the corresponding widget

        if (m_model.isEmpty()) {
            return false;
        }
        // now we know there is at least one group
        if (m_currentGroupPanel == null) {
            // this case happens if the method is called for the first time  
            m_currentGroupPanel = addGroupPanel(m_model.getGroups().get(0), 0);
        }
        while (true) {
            if (m_currentGroupPanel.hasMoreItems()) {
                // found next item in the current group 
                boolean found = m_currentGroupPanel.addNextItem();
                if (found) {
                    m_scrollPanel.onResizeDescendant();
                    return true;
                }
            } else if (m_currentGroupIndex < (m_model.getGroups().size() - 1)) {
                // didn't find item in the current group, so skip to next group if available  
                // and create the group widget 
                m_currentGroupIndex += 1;
                m_currentGroupPanel = addGroupPanel(m_model.getGroups().get(m_currentGroupIndex), m_currentGroupIndex);
            } else {
                // all groups exhausted 
                m_scrollPanel.onResizeDescendant();
                return false;
            }
        }
    }

    /**
     * Executes the given action.<p>
     * 
     * @param action the action to execute on the selected resources
     */
    protected void executeAction(CmsWorkflowAction action) {

        m_publishDialog.executeAction(action);
    }

    /**
     * The method to call when the items have finished being loaded into the list.<p>
     */
    protected void finishLoading() {

        m_loading = false;
        onChangePublishSelection();
    }

    /**
     * The event handler for the Cancel button.<p>
     * 
     * @param e the event
     */
    @UiHandler("m_cancelButton")
    protected void onClickCancel(ClickEvent e) {

        m_publishDialog.onCancel();
    }

    /**
     * Handles the click event for problem resources check box.<p>
     * 
     * @param event the click event
     * 
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    @UiHandler("m_checkboxProblems")
    protected void onProblemClick(ClickEvent event) {

        setProblemMode(m_checkboxProblems.isChecked());
    }

    /**
     * Handling the value change event for the project selector.<p>
     * 
     * @param event the change event
     *  
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(ValueChangeEvent)
     */
    @UiHandler("m_projectSelector")
    protected void onProjectChange(ValueChangeEvent<String> event) {

        if (m_initialized) {
            m_publishDialog.setProjectId(new CmsUUID(event.getValue()));
            m_publishDialog.updateResourceList();
        }
    }

    /**
     * Handles the click event for related resources check box.<p>
     * 
     * @param event the click event
     * 
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    @UiHandler("m_checkboxRelated")
    protected void onRelatedClick(ClickEvent event) {

        m_publishDialog.setIncludeRelated(m_checkboxRelated.isChecked());
        m_publishDialog.updateResourceList();
    }

    /**
     * Handles the click event for sibling resources check box.<p>
     * 
     * @param event the click event
     * 
     * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
     */
    @UiHandler("m_checkboxSiblings")
    protected void onSiblingClick(ClickEvent event) {

        m_publishDialog.setIncludeSiblings(m_checkboxSiblings.isChecked());
        m_publishDialog.updateResourceList();
    }

    /**
     * Handling the value change event for the project selector.<p>
     * 
     * @param event the change event
     *  
     * @see com.google.gwt.event.logical.shared.ValueChangeHandler#onValueChange(ValueChangeEvent)
     */
    @UiHandler("m_workflowSelector")
    protected void onWorkflowChange(ValueChangeEvent<String> event) {

        if (m_initialized) {
            m_publishDialog.setWorkflowId(event.getValue());
            // check for workflow project
            if (!m_publishDialog.getSelectedWorkflow().getId().equals(event.getValue())) {
                m_publishDialog.setProjectId(new CmsUUID(event.getValue()));
                m_projectSelector.setEnabled(false);
            } else {
                m_projectSelector.setEnabled(true);
                m_publishDialog.setProjectId(new CmsUUID(m_projectSelector.getFormValueAsString()));
            }
            m_actions = m_publishDialog.getSelectedWorkflow().getActions();
            m_publishDialog.updateResourceList();
        }
    }

    /**
     * Enables or disables the "only show resources with problems" mode.<p>
     * 
     * @param enabled if true, enable the mode, else disable it
     */
    protected void setProblemMode(boolean enabled) {

        m_showProblemsOnly = enabled;
        setGroups(m_model.getGroups(), false);
    }

    /**
     * The method which should be called when new items start being inserted into the list.<p>
     */
    protected void startLoading() {

        m_loading = true;
    }

    /**
     * Adds a new group panel.<p>
     * 
     * @param group the publish group for which a panel should be added   
     * @param currentIndex the index of the publish group 
     * 
     * @return the publish group panel which has been added    
     */
    private CmsPublishGroupPanel addGroupPanel(CmsPublishGroup group, int currentIndex) {

        String header = group.getName();
        CmsPublishGroupPanel groupPanel = new CmsPublishGroupPanel(
            header,
            currentIndex,
            this,
            m_model,
            m_selectionControllers,
            m_showProblemsOnly);
        m_groupPanels.add(groupPanel);
        m_groupPanelContainer.add(groupPanel);
        return groupPanel;
    }

    /**
     * Adds the scroll handler to the scroll panel which makes more groups visible when the user 
     * scrolls to the bottom.<p>
     */
    private void addScrollHandler() {

        m_scrollPanel.addScrollHandler(new CmsScrollToBottomHandler(new Runnable() {

            /**
             * @see java.lang.Runnable#run()
             */
            public void run() {

                if (!m_loading) {
                    startLoading();
                    addMoreListItems();
                }
            }
        }, SCROLL_THRESHOLD));

    }

    /**
     * Enables action buttons.<p>
     * 
     * @param enable <code>true</code> to enable the action buttons
     */
    private void enableActions(boolean enable) {

        for (CmsPushButton button : m_actionButtons) {
            button.setEnabled(enable);
        }
    }

    /**
     * Shows either the scroll panel or the "no resources" label and hides the other one.<p> 
     * 
     * @param visible if true, set the scroll panel to visible, otherwise the "no resources" label
     */
    private void setResourcesVisible(boolean visible) {

        m_noResources.setVisible(!visible);
        m_scrollPanel.setVisible(visible);
        m_topBar.getElement().getStyle().setVisibility(visible ? Visibility.VISIBLE : Visibility.HIDDEN);
        m_checkboxSiblings.setVisible(visible);
        m_checkboxRelated.setVisible(visible);
    }

    /**
     * Shows the problem count in the panel.<p>
     * 
     * @param numProblems the number of resources with publish problems
     */
    private void showProblemCount(int numProblems) {

        m_problemsPanel.clear();
        if (numProblems > 0) {
            HorizontalPanel errorBox = new HorizontalPanel();
            Label warnIcon = new Label();
            warnIcon.addStyleName(org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.INSTANCE.gwtImages().style().warningIcon());
            String message = Messages.get().key(Messages.GUI_PUBLISH_DIALOG_PROBLEM_1, "" + numProblems);
            errorBox.add(warnIcon);
            errorBox.add(new Label(message));
            m_problemsPanel.add(errorBox);
            m_problemsPanel.setVisible(true);
        }
        m_checkboxProblems.setVisible(numProblems > 0);
    }

    /**
     * Updates the state of the check box for all items.<p>
     * 
     * @param value the state to use to update the check box 
     */
    private void updateCheckboxState(Set<org.opencms.ade.publish.client.CmsPublishItemStatus.State> value) {

        CheckBoxUpdate update = updateCheckbox(value);
        m_selectAll.setText(update.getText());
        m_selectAll.setState(update.getState(), false);

    }
}
