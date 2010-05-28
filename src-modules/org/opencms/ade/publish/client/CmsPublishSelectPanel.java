/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/client/Attic/CmsPublishSelectPanel.java,v $
 * Date   : $Date: 2010/05/28 08:46:16 $
 * Version: $Revision: 1.20 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.gwt.client.util.CmsListSplitter;
import org.opencms.gwt.client.util.CmsMessages;
import org.opencms.gwt.client.util.CmsPair;
import org.opencms.gwt.client.util.CmsScrollToBottomHandler;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
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
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.20 $
 * 
 * @since 8.0.0
 */
public class CmsPublishSelectPanel extends Composite implements I_CmsPublishSelectionChangeHandler {

    /** The UiBinder interface for this widget. */
    protected interface I_CmsPublishSelectPanelUiBinder extends UiBinder<Widget, CmsPublishSelectPanel> {
        // empty
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

    /** The publish list resources indexed by UUID. */
    private Map<CmsUUID, CmsPublishResource> m_publishResources;

    /** The publish list resources indexed by path. */
    private Map<String, CmsPublishResource> m_publishResourcesByPath;

    /** The list splitter which gets the next set of groups when the user scrolls down. */
    private CmsListSplitter<CmsPublishGroupPanel> m_splitter;

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

                setAllCheckboxes(true);
                onChangePublishSelection();
            }
        });

        m_selectNone.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent e) {

                setAllCheckboxes(false);
                onChangePublishSelection();
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
     * Check for problems with new/deleted folders in the publish selection.<p>
     * 
     * @param resourceIds the ids of the resources selected for publishing 
     * @return true if there are problems with nested 
     */
    public boolean checkForProblems(Set<CmsUUID> resourceIds) {

        List<CmsPublishResource> pubResources = new ArrayList<CmsPublishResource>();
        Set<CmsUUID> publishIds = getResourcesToPublish();
        for (CmsUUID publishId : publishIds) {
            pubResources.add(m_publishResources.get(publishId));
        }
        for (CmsPublishResource pubResource : pubResources) {
            String parentPath = CmsResource.getParentFolder(pubResource.getName());
            CmsPublishResource parent = m_publishResourcesByPath.get(parentPath);
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

        Set<CmsUUID> result = new HashSet<CmsUUID>();
        for (CmsPublishGroupPanel groupPanel : m_groupPanels) {
            result.addAll(groupPanel.getResourcesToPublish());
        }
        return result;
    }

    /**
     * Returns the set of ids of resources which have been selected for removal.<p>
     * 
     * @return a set of id strings
     */
    public Set<CmsUUID> getResourcesToRemove() {

        Set<CmsUUID> result = new HashSet<CmsUUID>();
        for (CmsPublishGroupPanel groupPanel : m_groupPanels) {
            result.addAll(groupPanel.getResourcesToRemove());
        }
        return result;
    }

    /**
     * @see org.opencms.ade.publish.client.I_CmsPublishSelectionChangeHandler#onChangePublishSelection()
     */
    public void onChangePublishSelection() {

        boolean enablePublishButton = (getResourcesToRemove().size() != 0) || (getResourcesToPublish().size() != 0);
        m_publishButton.setEnabled(enablePublishButton);

    }

    /**
     * Sets the state of all publish checkboxes in this widget to a given value.<p>
     * 
     * @param checked the new value for all the publish checkboxes
     */
    public void setAllCheckboxes(boolean checked) {

        for (CmsPublishGroupPanel groupPanel : m_groupPanels) {
            groupPanel.setAllSelected(checked);
        }
    }

    /**
     * Sets the publish groups used by this widget.<p>
     * 
     * @param groups the new publish groups 
     */
    public void setGroups(List<CmsPublishGroup> groups) {

        m_problemsPanel.clear();
        m_checkboxProblems.setChecked(false);
        m_problemsPanel.setVisible(false);
        m_groupPanels.clear();
        m_groupPanelContainer.clear();
        m_publishButton.setEnabled(false);

        int numGroups = groups.size();
        setResourcesVisible(numGroups > 0);
        if (numGroups == 0) {
            return;
        }
        int numProblems = 0;
        m_publishResources = new HashMap<CmsUUID, CmsPublishResource>();
        m_publishResourcesByPath = new HashMap<String, CmsPublishResource>();
        for (CmsPublishGroup group : groups) {
            String header = group.getName();
            List<CmsPublishResource> resourceBeans = group.getResources();
            CmsPublishGroupPanel groupPanel = new CmsPublishGroupPanel(header, resourceBeans, this);
            m_groupPanels.add(groupPanel);
            numProblems += groupPanel.countProblems();
            for (CmsPublishResource pubResource : group.getResources()) {
                m_publishResources.put(pubResource.getId(), pubResource);
                m_publishResourcesByPath.put(pubResource.getName(), pubResource);
            }
        }
        resetGroups();
        m_publishButton.setEnabled(true);
        showProblemCount(numProblems);
        onChangePublishSelection();
    }

    /**
     * Adds more groups if there are still undisplayed groups left.<p>
     */
    protected void addMoreGroups() {

        if ((m_splitter != null) && m_splitter.hasMore()) {
            for (CmsPublishGroupPanel groupPanel : m_splitter.getMore()) {
                m_groupPanelContainer.add(groupPanel);
            }
        }
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

        for (CmsPublishGroupPanel groupPanel : m_groupPanels) {
            groupPanel.setProblemMode(enabled);
        }
        resetGroups();
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

                addMoreGroups();
            }
        }, SCROLL_THRESHOLD));

    }

    /**
     * Resets the publish group view.<p>
     * 
     * This method is called when the 'only show resources with problems' mode is toggled.
     */
    private void resetGroups() {

        m_groupPanelContainer.clear();
        m_splitter = new CmsListSplitter<CmsPublishGroupPanel>(m_groupPanels, MIN_BATCH_SIZE);
        addMoreGroups();
        m_scrollPanel.setScrollPosition(0);
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
        m_checkboxProblems.setVisible(visible);
    }

    /**
     * Shows the problem count in the panel.<p>
     * 
     * @param numProblems the number of resources with publish problems
     */
    private void showProblemCount(int numProblems) {

        m_problemsPanel.clear();
        if (numProblems > 0) {
            String message = Messages.get().key(Messages.GUI_PUBLISH_DIALOG_PROBLEM_1, "" + numProblems);
            m_problemsPanel.add(new InlineLabel(message));
            m_problemsPanel.setVisible(true);
        }
    }
}
