/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/client/Attic/CmsPublishSelectPanel.java,v $
 * Date   : $Date: 2010/04/08 07:30:07 $
 * Version: $Revision: 1.2 $
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

import org.opencms.ade.publish.shared.CmsClientPublishOptions;
import org.opencms.ade.publish.shared.CmsClientPublishResourceBean;
import org.opencms.ade.publish.shared.CmsPublishGroups;
import org.opencms.gwt.client.i18n.CmsMessages;
import org.opencms.gwt.client.ui.CmsButton;
import org.opencms.gwt.client.ui.CmsTextButton;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsSelectBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
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
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsPublishSelectPanel extends Composite {

    /** The UiBinder interface for this widget. */
    protected interface I_CmsPublishSelectPanelUiBinder extends UiBinder<Widget, CmsPublishSelectPanel> {
        // empty
    }

    /** The CSS bundle used for this widget. */
    private static final I_CmsPublishCss CSS = I_CmsPublishLayoutBundle.INSTANCE.publishCss();

    /** The message bundle used for this widget. */
    private static final CmsMessages MESSAGES = Messages.get();

    /** The UiBinder instance used for this widget. */
    private static I_CmsPublishSelectPanelUiBinder uiBinder = GWT.create(I_CmsPublishSelectPanelUiBinder.class);

    /** The button for escaping from the publish dialog. */
    @UiField
    protected CmsTextButton m_cancelButton;

    /** The checkbox for including related resources. */
    @UiField
    protected CmsCheckBox m_checkboxRelated;

    /** The checkbox for including sibling resources. */
    @UiField
    protected CmsCheckBox m_checkboxSiblings;

    /** The panel containing the publish groups. */
    @UiField
    protected Panel m_groupPanel;

    /** The label which is displayed when there are no resources to publish. */
    @UiField
    protected Label m_noResources;

    /** The label which shows a message telling the user the number of problems. */
    @UiField
    protected FlowPanel m_problemsPanel;

    /** The project select box. */
    protected CmsSelectBox m_projectSelector;

    /** The button for publishing. */
    @UiField
    protected CmsTextButton m_publishButton;

    /** The publish dialog which contains this panel. */
    protected CmsPublishDialog m_publishDialog;

    /** The current publish list options. */
    protected CmsClientPublishOptions m_publishOptions;

    /** The label for the "include related" checkbox. */
    @UiField
    protected InlineLabel m_relatedLabel;

    /** The scroll panel containing the group panel. */
    @UiField
    protected ScrollPanel m_scrollPanel;

    /** The button for selecting all resources for publishing. */
    @UiField
    protected CmsTextButton m_selectAll;

    /** The label in front of the "select all/none" buttons. */
    @UiField
    protected InlineLabel m_selectLabel;

    /** The button for de-selecting all resources for publishing. */
    @UiField
    protected CmsTextButton m_selectNone;

    /** The label shown in front of the project selector. */
    @UiField
    protected InlineLabel m_selectorLabel;

    /** The panel containing the project selector. */
    @UiField
    protected FlowPanel m_selectorPanel;

    /** The label for the "include siblings" checkbox. */
    @UiField
    protected InlineLabel m_siblingsLabel;

    /** The top button bar. */
    @UiField
    protected Panel m_topBar;

    /** The list of group panels for each publish list group. */
    private List<CmsPublishGroupPanel> m_groups = new ArrayList<CmsPublishGroupPanel>();

    /** The number of resources with publish problems. */
    private int m_numProblems;

    /**
     * Creates a new instance.<p>
     * 
     * @param publishDialog the publish dialog to which this panel should belong
     * @param projects a map of projects, where the keys are the project ids and the values are the names of the projects 
     * @param publishOptions the initial publish options
     */
    public CmsPublishSelectPanel(
        CmsPublishDialog publishDialog,
        Map<String, String> projects,
        CmsClientPublishOptions publishOptions) {

        m_publishOptions = publishOptions;

        initWidget(uiBinder.createAndBindUi(this));
        setStyleName(CSS.selectPanel());
        m_groupPanel.setStyleName(CSS.groupPanel());
        m_topBar.setStyleName(CSS.topBar());

        projects.put("", MESSAGES.key(Messages.GUI_PUBLISH_DIALOG_MY_CHANGES_0));
        m_projectSelector = new CmsSelectBox(CmsSelectBox.Mode.TEXT, projects);
        m_projectSelector.addStyleName(CSS.selector());
        m_selectorPanel.add(m_projectSelector);
        m_scrollPanel.setStyleName(CSS.scrollPanel());

        m_publishDialog = publishDialog;
        m_checkboxRelated.setChecked(publishOptions.isIncludeRelated());
        m_checkboxSiblings.setChecked(publishOptions.isIncludeSiblings());
        String project = publishOptions.getProject();
        project = (project == null) ? "" : project;
        m_projectSelector.selectValue(project);

        m_projectSelector.addValueChangeHandler(new ValueChangeHandler<String>() {

            /**
             * @see ValueChangeHandler#onValueChange(ValueChangeEvent)
             */
            public void onValueChange(ValueChangeEvent<String> event) {

                m_publishOptions.setProject(event.getValue());
                m_publishDialog.onChangeOptions();
            }
        });

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

        m_publishButton.setUpFace(MESSAGES.key(Messages.GUI_PUBLISH_DIALOG_PUBLISH_0), null);
        m_publishButton.useMinWidth(true);
        m_cancelButton.setUpFace(MESSAGES.key(Messages.GUI_PUBLISH_DIALOG_CANCEL_BUTTON_0), null);
        m_cancelButton.useMinWidth(true);

        m_selectAll.setUpFace(MESSAGES.key(Messages.GUI_PUBLISH_TOP_PANEL_ALL_BUTTON_0), null);
        m_selectAll.useMinWidth(true);
        m_selectNone.setUpFace(MESSAGES.key(Messages.GUI_PUBLISH_TOP_PANEL_NONE_BUTTON_0), null);
        m_selectNone.useMinWidth(true);

        m_noResources.setText(MESSAGES.key(Messages.GUI_PUBLISH_DIALOG_NO_RES_0));
        m_selectAll.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent e) {

                setAllCheckboxes(true);
            }
        });

        m_selectNone.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent e) {

                setAllCheckboxes(false);
            }
        });

        m_siblingsLabel.setText(MESSAGES.key(Messages.GUI_PUBLISH_CHECKBOXES_SIBLINGS_0));
        m_relatedLabel.setText(MESSAGES.key(Messages.GUI_PUBLISH_CHECKBOXES_REL_RES_0));
        m_relatedLabel.addStyleName(CSS.clear());
        m_siblingsLabel.addStyleName(CSS.clear());
        m_selectLabel.setText(MESSAGES.key(Messages.GUI_PUBLISH_TOP_PANEL_LEFT_LABEL_0));
        m_selectorLabel.setText(MESSAGES.key(Messages.GUI_PUBLISH_TOP_PANEL_RIGHT_LABEL_0));
        m_numProblems = 0;
    }

    static {
        CSS.ensureInjected();
    }

    /**
     * Returns the buttons of this panel which should be shown as the buttons of the publish dialog.<p>
     * @return a list of buttons
     */
    public List<CmsButton> getButtons() {

        List<CmsButton> result = new ArrayList<CmsButton>();
        result.add(m_publishButton);
        result.add(m_cancelButton);
        return result;
    }

    /**
     * Returns the current publish options.<p>
     * 
     * @return a publish options bean
     */
    public CmsClientPublishOptions getPublishOptions() {

        return m_publishOptions;
    }

    /**
     * Returns the ids of the resources which should be published.<p>
     * 
     * @return a set of id strings 
     */
    public Set<String> getResourcesToPublish() {

        Set<String> result = new HashSet<String>();
        for (CmsPublishGroupPanel groupPanel : m_groups) {
            result.addAll(groupPanel.getResourcesToPublish());
        }
        return result;
    }

    /**
     * Returns the set of ids of resources which have been selected for removal.<p>
     * 
     * @return a set of id strings
     */
    public Set<String> getResourcesToRemove() {

        Set<String> result = new HashSet<String>();
        for (CmsPublishGroupPanel groupPanel : m_groups) {
            result.addAll(groupPanel.getResourcesToRemove());
        }
        return result;
    }

    /**
     * @see com.google.gwt.user.client.ui.Widget#onLoad()
     */
    @Override
    public void onLoad() {

        addStyleName(CSS.invisible());
        m_publishDialog.onChangeOptions();
    }

    /**
     * Sets the state of all publish checkboxes in this widget to a given value.<p>
     * 
     * @param checked the new value for all the publish checkboxes
     */
    public void setAllCheckboxes(boolean checked) {

        for (CmsPublishGroupPanel groupPanel : m_groups) {
            groupPanel.setAllSelected(checked);
        }
    }

    /**
     * Sets the publish groups used by this widget.<p>
     * 
     * @param groups the new publish groups 
     */
    public void setGroups(CmsPublishGroups groups) {

        m_scrollPanel.setVisible(true);
        int numGroups = groups.getGroups().size();
        setResourcesVisible(numGroups > 0);
        if (numGroups == 0) {
            return;
        }

        m_numProblems = 0;

        m_groups.clear();
        m_groupPanel.clear();

        for (Map.Entry<String, List<CmsClientPublishResourceBean>> entry : groups.getGroups().entrySet()) {
            String header = entry.getKey();
            List<CmsClientPublishResourceBean> resourceBeans = entry.getValue();
            CmsPublishGroupPanel groupPanel = new CmsPublishGroupPanel(header, resourceBeans);
            m_numProblems += groupPanel.countProblems();
            m_groups.add(groupPanel);
            m_groupPanel.add(groupPanel);
        }
        removeStyleName(CSS.invisible());
        showProblemCount();
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

        m_publishDialog.onRequestPublish();
    }

    /**
     * Shows either the scroll panel or the "no resources" label and hides the other one.<p> 
     * 
     * @param visible if true, set the scroll panel to visible, otherwise the "no resources" label
     */
    private void setResourcesVisible(boolean visible) {

        Widget first = m_scrollPanel;
        Widget second = m_noResources;
        if (!visible) {
            Widget temp = first;
            first = second;
            second = temp;
        }
        first.setVisible(true);
        second.setVisible(false);
    }

    /**
     * Shows the problem count in the panel.<p>
     */
    private void showProblemCount() {

        m_problemsPanel.clear();
        if (m_numProblems > 0) {
            String message = MESSAGES.key(Messages.GUI_PUBLISH_DIALOG_PROBLEM_1, "" + m_numProblems);
            m_problemsPanel.add(new InlineLabel(message));
        }
    }

}
