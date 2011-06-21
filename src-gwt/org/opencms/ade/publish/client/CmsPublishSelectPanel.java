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
import org.opencms.file.CmsResource;
import org.opencms.gwt.client.ui.CmsAlertDialog;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;
import org.opencms.gwt.client.util.CmsMessages;
import org.opencms.gwt.client.util.CmsScrollToBottomHandler;
import org.opencms.util.CmsPair;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
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
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
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
    private static final int SCROLL_THRESHOLD = 100;

    /** Text metrics key. */
    private static final String TM_PUBLISH = "Publish";

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

    /** The label which is displayed when there are no resources to publish. */
    @UiField
    protected Label m_noResources;

    /** The panel which shows a message telling the user the number of problems. */
    @UiField
    protected Panel m_problemsPanel;

    /** The project select box. */
    @UiField
    protected CmsSelectBox m_projectSelector;

    /** The button for publishing. */
    @UiField
    protected CmsPushButton m_publishButton;

    /** The publish dialog which contains this panel. */
    protected CmsPublishDialog m_publishDialog;

    /** The data model for the publish dialog. */
    protected CmsPublishDataModel m_model;

    /** The global map of selection controllers for all groups. */
    protected Map<CmsUUID, CmsPublishItemSelectionController> m_selectionControllers = Maps.newHashMap();

    /** The current publish list options. */
    protected CmsPublishOptions m_publishOptions;

    /** The scroll panel containing the group panel. */
    @UiField
    protected ScrollPanel m_scrollPanel;

    /** The button for selecting all resources for publishing. */
    @UiField
    protected CmsPushButton m_selectAll;

    /** The label in front of the "select all/none" buttons. */
    @UiField
    protected InlineLabel m_selectLabel;

    /** The button for de-selecting all resources for publishing. */
    @UiField
    protected CmsPushButton m_selectNone;

    /** The label shown in front of the project selector. */
    @UiField
    protected InlineLabel m_selectorLabel;

    /** The panel containing the project selector. */
    @UiField
    protected FlowPanel m_selectorPanel;

    /** The top button bar. */
    @UiField
    protected Panel m_topBar;

    /** The list of group panels for each publish list group. */
    private List<CmsPublishGroupPanel> m_groupPanels = new ArrayList<CmsPublishGroupPanel>();

    /** The current group panel. */
    private CmsPublishGroupPanel m_currentGroupPanel;

    /** The current group index used for scrolling. */
    private int m_currentGroupIndex;

    /** The label displaying the resource count. */
    @UiField
    protected InlineHTML m_resourceCountLabel;

    /** Flag which indicates whether only resources with problems should be shown. */
    private boolean m_showProblemsOnly;

    /** Flag to indicate whether new items are currently being added to the list. */
    protected boolean m_loading;

    /**
     * Creates a new instance.<p>
     * 
     * @param publishDialog the publish dialog to which this panel should belong
     * @param projects a map of projects, where the keys are the project ids and the values are the names of the projects 
     * @param publishOptions the initial publish options
     */
    public CmsPublishSelectPanel(
        CmsPublishDialog publishDialog,
        List<CmsProjectBean> projects,
        CmsPublishOptions publishOptions) {

        m_publishOptions = publishOptions;

        initWidget(UI_BINDER.createAndBindUi(this));
        m_checkboxProblems.setVisible(false);

        List<CmsPair<String, String>> items = new ArrayList<CmsPair<String, String>>();
        CmsMessages messages = Messages.get();
        items.add(new CmsPair<String, String>(
            CmsUUID.getNullUUID().toString(),
            messages.key(Messages.GUI_PUBLISH_DIALOG_MY_CHANGES_0)));
        boolean foundOldProject = false;
        for (CmsProjectBean project : projects) {
            items.add(new CmsPair<String, String>(project.getId().toString(), project.getName()));

            // look if the project id from the last publish list is among the available projects.
            // (this might not be the case if the project has been deleted in the meantime.)
            if (project.getId().equals(publishOptions.getProjectId())) {
                foundOldProject = true;
            }

        }
        m_projectSelector.setItems(items);
        m_projectSelector.addStyleName(CSS.selector());
        m_publishDialog = publishDialog;
        m_checkboxRelated.setChecked(publishOptions.isIncludeRelated());
        m_checkboxSiblings.setChecked(publishOptions.isIncludeSiblings());
        if (foundOldProject) {
            m_projectSelector.selectValue(publishOptions.getProjectId().toString());
        }

        m_projectSelector.addValueChangeHandler(new ValueChangeHandler<String>() {

            /**
             * @see ValueChangeHandler#onValueChange(ValueChangeEvent)
             */
            public void onValueChange(ValueChangeEvent<String> event) {

                m_publishOptions.setProjectId(new CmsUUID(event.getValue()));
                m_publishDialog.onChangeOptions();
            }
        });
        m_projectSelector.truncate(TM_PUBLISH, 200);

        m_checkboxRelated.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent e) {

                m_publishOptions.setIncludeRelated(m_checkboxRelated.isChecked());
                m_publishDialog.onChangeOptions();
            }
        });
        m_checkboxSiblings.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent e) {

                m_publishOptions.setIncludeSiblings(m_checkboxSiblings.isChecked());
                m_publishDialog.onChangeOptions();
            }
        });

        m_checkboxProblems.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent e) {

                setProblemMode(m_checkboxProblems.isChecked());
            }
        });

        m_publishButton.setText(messages.key(Messages.GUI_PUBLISH_DIALOG_PUBLISH_0));
        m_publishButton.setUseMinWidth(true);
        m_cancelButton.setText(messages.key(Messages.GUI_PUBLISH_DIALOG_CANCEL_BUTTON_0));
        m_cancelButton.setUseMinWidth(true);

        m_selectAll.setText(messages.key(Messages.GUI_PUBLISH_TOP_PANEL_ALL_BUTTON_0));
        m_selectAll.setImageClass(I_CmsInputLayoutBundle.INSTANCE.inputCss().checkBoxImageChecked());
        m_selectAll.setUseMinWidth(true);

        m_selectNone.setText(messages.key(Messages.GUI_PUBLISH_TOP_PANEL_NONE_BUTTON_0));
        m_selectNone.setImageClass(I_CmsInputLayoutBundle.INSTANCE.inputCss().checkBoxImageUnchecked());
        m_selectNone.setUseMinWidth(true);

        m_noResources.setText(messages.key(Messages.GUI_PUBLISH_DIALOG_NO_RES_0));
        m_selectAll.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent e) {

                m_model.signalAll(Signal.publish);
                CmsPublishSelectPanel.this.onChangePublishSelection();
            }
        });

        m_selectNone.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent e) {

                m_model.signalAll(Signal.unpublish);
                CmsPublishSelectPanel.this.onChangePublishSelection();
            }
        });

        m_checkboxSiblings.setText(messages.key(Messages.GUI_PUBLISH_CHECKBOXES_SIBLINGS_0));
        m_checkboxRelated.setText(messages.key(Messages.GUI_PUBLISH_CHECKBOXES_REL_RES_0));
        m_checkboxProblems.setText(messages.key(Messages.GUI_PUBLISH_CHECKBOXES_PROBLEMS_0));

        m_selectLabel.setText(messages.key(Messages.GUI_PUBLISH_TOP_PANEL_LEFT_LABEL_0));
        m_selectorLabel.setText(messages.key(Messages.GUI_PUBLISH_TOP_PANEL_RIGHT_LABEL_0));
        addScrollHandler();

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
        result.add(m_publishButton);
        return result;
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

        return new HashSet<CmsUUID>(m_model.getRemoveIds());
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

        m_publishButton.setEnabled(shouldEnablePublishButton());

    }

    /**
     * Sets the publish groups used by this widget.<p>
     * 
     * @param groups the new publish groups
     * @param newData true if the groups are new data which has been loaded  
     */
    public void setGroups(List<CmsPublishGroup> groups, boolean newData) {

        m_model = new CmsPublishDataModel(groups, this);
        m_resourceCountLabel.setHTML(formatResourceCount(m_model.getPublishResources().size()));
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
        m_publishButton.setEnabled(false);

        int numGroups = groups.size();
        setResourcesVisible(numGroups > 0);

        if (numGroups == 0) {
            return;
        }

        m_publishButton.setEnabled(true);
        addMoreListItems();
        showProblemCount(m_model.countProblems());
        onChangePublishSelection();
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
     * Adds more groups if there are still undisplayed groups left.<p>
     */
    protected void addMoreGroups() {

        //TODO: adding more groups  
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
                    return true;
                }
            } else if (m_currentGroupIndex < m_model.getGroups().size() - 1) {
                // didn't find item in the current group, so skip to next group if available  
                // and create the group widget 
                m_currentGroupIndex += 1;
                m_currentGroupPanel = addGroupPanel(m_model.getGroups().get(m_currentGroupIndex), m_currentGroupIndex);
            } else {
                // all groups exhausted 
                return false;
            }
        }
    }

    /**
     * The method to call when the items have finished being loaded into the list.<p>
     */
    protected void finishLoading() {

        m_loading = false;
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
     * The event handler for the publish button.<p>
     * 
     * @param e the event 
     */
    @UiHandler("m_publishButton")
    protected void onClickPublish(ClickEvent e) {

        if (!checkForProblems(getResourcesToPublish())) {
            m_publishDialog.onRequestPublish();
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
}
