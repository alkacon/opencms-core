/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/client/Attic/CmsPublishGroupPanel.java,v $
 * Date   : $Date: 2010/05/05 14:33:31 $
 * Version: $Revision: 1.13 $
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

import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItem;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.I_CmsButton;
import org.opencms.gwt.client.ui.css.I_CmsInputLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle.I_CmsListItemWidgetCss;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.client.util.CmsStyleVariable;
import org.opencms.gwt.client.util.I_CmsHasSize;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;

/**
 * A panel representing a single publish group.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.13 $
 * 
 * @since 8.0.0
 */
public class CmsPublishGroupPanel extends Composite implements I_CmsHasSize {

    /** The CSS bundle used for this widget. */
    protected static final I_CmsPublishCss CSS = I_CmsPublishLayoutBundle.INSTANCE.publishCss();

    /** Text metrics key. */
    private static final String TM_PUBLISH_LIST = "PublishList";

    /** The handler which is called when the publish item selection changes. */
    protected I_CmsPublishSelectionChangeHandler m_selectionChangeHandler;

    /** The group header (containing the label and add/remove buttons). */
    private CmsListItem m_header = new CmsListItem();

    /** Resources which don't have any problems. */
    private ArrayList<CmsTreeItem> m_noProblemItems = new ArrayList<CmsTreeItem>();

    /** The number of resources with problems in this group.<p> */
    private int m_numProblems;

    /** The root panel of this widget. */
    private CmsList<CmsTreeItem> m_panel = new CmsList<CmsTreeItem>();

    /** Flag which indicates whether we're in 'only show resources with problems' mode. */
    private boolean m_problemMode;

    /** The button for selecting all resources in the group. */
    private CmsPushButton m_selectAll;

    /** The list of item selection controllers for this group. */
    private List<CmsPublishItemSelectionController> m_selectionControllers = new ArrayList<CmsPublishItemSelectionController>();

    /** The button for deselecting all resources in the group. */
    private CmsPushButton m_selectNone;

    /** The number of resources in this group. */
    private int m_size;

    /**
     * Constructs a new instance.<p>
     * 
     * @param title the title of the group
     * @param group the list of resource beans for the group
     * @param selectionChangeHandler the handler for selection changes for publish resources 
     */
    public CmsPublishGroupPanel(
        String title,
        List<CmsPublishResource> group,
        I_CmsPublishSelectionChangeHandler selectionChangeHandler) {

        initWidget(m_panel);
        m_panel.add(m_header);
        m_panel.truncate(TM_PUBLISH_LIST, CmsPublishDialog.DIALOG_WIDTH);
        for (CmsPublishResource resourceBean : group) {
            addResource(resourceBean, false);
        }
        initSelectButtons();

        if (m_numProblems == m_size) {
            m_selectAll.setEnabled(false);
            m_selectNone.setEnabled(false);
        }
        Label label = new Label(title);
        label.addStyleName(CSS.groupHeader());
        m_header.add(label);

        FlowPanel clear = new FlowPanel();
        clear.setStyleName(CSS.clear());
        m_header.add(clear);
        m_selectionChangeHandler = selectionChangeHandler;
    }

    /**
     * Creates a basic list item widget for a given publish resource bean.<p>
     * 
     * @param resourceBean the publish resource bean
     * 
     * @return the list item widget representing the publish resource bean 
     */
    public static CmsListItemWidget createListItemWidget(CmsPublishResource resourceBean) {

        CmsListInfoBean info = new CmsListInfoBean();
        info.setTitle(getTitle(resourceBean));
        info.setSubTitle(resourceBean.getName());
        String stateLabel = Messages.get().key(Messages.GUI_PUBLISH_RESOURCE_STATE_0);
        String stateName = CmsPublishUtil.getStateName(resourceBean.getState());
        // this can be null for the source resources of broken relations in the 'broken links' 
        // panel since these resources don't have to be new or deleted or changed
        if (stateName != null) {
            info.addAdditionalInfo(stateLabel, stateName);
        }
        info.setValueStyle(stateLabel, CmsPublishUtil.getStateStyle(resourceBean.getState()));

        CmsListItemWidget itemWidget = new CmsListItemWidget(info);
        if (resourceBean.getInfo() != null) {
            Image warningImage = new Image(I_CmsPublishLayoutBundle.INSTANCE.warning());
            warningImage.setTitle(resourceBean.getInfo().getValue());
            String permaVisible = I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible();

            warningImage.addStyleName(permaVisible);
            itemWidget.addButton(warningImage);
        }
        Image icon = new Image(resourceBean.getIcon());
        icon.addStyleName(I_CmsLayoutBundle.INSTANCE.listItemWidgetCss().permaVisible());
        itemWidget.setIcon(icon);
        return itemWidget;
    }

    /** 
     * Utility method for getting the title of a publish resource bean, or a default title 
     * if the bean has no title.<p>
     * 
     * @param resourceBean the resource bean for which the title should be retrieved
     *  
     * @return the bean's title, or a default title
     */
    private static String getTitle(CmsPublishResource resourceBean) {

        String title = resourceBean.getTitle();
        if ((title == null) || title.equals("")) {
            title = Messages.get().key(Messages.GUI_NO_TITLE_0);
        }
        return title;
    }

    /**
     * Returns the number of problematic resources in the current group.<p>
     * 
     * @return the number of resources with problems 
     */
    public int countProblems() {

        return m_numProblems;
    }

    /**
     * Gets the ids of all resources selected for publishing.<p>
     * 
     * @return a list of ids
     */
    public List<CmsUUID> getResourcesToPublish() {

        List<CmsUUID> result = new ArrayList<CmsUUID>();
        for (CmsPublishItemSelectionController itemController : m_selectionControllers) {
            itemController.addIdToPublish(result);
        }
        return result;
    }

    /**
     * Returns the ids of resources which have been selected for removal from the publish list.<p>
     * 
     * @return a list of ids 
     */
    public List<CmsUUID> getResourcesToRemove() {

        List<CmsUUID> result = new ArrayList<CmsUUID>();
        for (CmsPublishItemSelectionController itemController : m_selectionControllers) {
            itemController.addIdToRemove(result);
        }
        return result;
    }

    /**
     * @see org.opencms.gwt.client.util.I_CmsHasSize#getSize()
     */
    public int getSize() {

        return m_problemMode ? m_numProblems : m_size;
    }

    /**
     * Sets the state of all checkboxes of this group to a given value.<p>
     * 
     * @param checked the new value for all checkboxes of this group
     */
    public void setAllSelected(boolean checked) {

        for (CmsPublishItemSelectionController itemController : m_selectionControllers) {
            itemController.selectIfPossible(checked);
        }
    }

    /**
     * Enables or disables the 'only show resources with problems' mode.<p>
     * 
     * @param enabled
     */
    public void setProblemMode(boolean enabled) {

        m_problemMode = enabled;
        setNoProblemItemsVisible(!enabled);
        if (m_numProblems == 0) {
            // if no items in the group are problem items, hide or show the whole group 
            setVisible(!enabled);
        }
    }

    /**
     * Adds a resource bean to this group.<p>
     * 
     * @param resourceBean the resource bean which should be added
     * @param indent if true, indent the widget representing the resource bean (for related resources)
     */
    private void addResource(CmsPublishResource resourceBean, boolean indent) {

        CmsTreeItem row = buildItem(resourceBean);
        m_panel.add(row);
        if (resourceBean.getInfo() != null) {
            m_numProblems += 1;
        } else {
            m_noProblemItems.add(row);
        }
        // we don't count related resources.
        m_size += 1;

        for (CmsPublishResource related : resourceBean.getRelated()) {
            row.addChild(buildItem(related));
        }
    }

    /**
     * Creates a widget from resource bean data.<p>
     * 
     * @param resourceBean the resource bean for which a widget should be constructed
     * 
     * @return a widget representing the resource bean
     */
    private CmsTreeItem buildItem(CmsPublishResource resourceBean) {

        ClickHandler checkboxHandler = new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                m_selectionChangeHandler.onChangePublishSelection();
            }
        };

        CmsListItemWidget itemWidget = createListItemWidget(resourceBean);
        final CmsStyleVariable styleVar = new CmsStyleVariable(itemWidget);
        styleVar.setValue(CSS.itemToKeep());

        final CmsCheckBox checkbox = new CmsCheckBox();
        checkbox.addClickHandler(checkboxHandler);
        final boolean hasProblem = (resourceBean.getInfo() != null);
        if (hasProblem) {
            // can't select resource with problems
            checkbox.setChecked(false);
            checkbox.setEnabled(false);
        }
        CmsTreeItem row = new CmsTreeItem(false, checkbox, itemWidget);
        final CmsCheckBox remover = new CmsCheckBox();
        final CmsPublishItemSelectionController controller = new CmsPublishItemSelectionController(
            resourceBean.getId(),
            checkbox,
            remover,
            hasProblem);
        m_selectionControllers.add(controller);

        remover.setTitle(Messages.get().key(Messages.GUI_PUBLISH_REMOVE_BUTTON_0));
        remover.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent e) {

                boolean remove = remover.isChecked();
                controller.onClickRemove(remove);
                I_CmsListItemWidgetCss itemWidgetCss = I_CmsLayoutBundle.INSTANCE.listItemWidgetCss();
                styleVar.setValue(remove ? itemWidgetCss.disabledItem() : CSS.itemToKeep());
                remover.setTitle(remove
                ? Messages.get().key(Messages.GUI_PUBLISH_UNREMOVE_BUTTON_0)
                : Messages.get().key(Messages.GUI_PUBLISH_REMOVE_BUTTON_0));

                m_selectionChangeHandler.onChangePublishSelection();
            }
        });
        itemWidget.addButtonToFront(remover);
        row.addStyleName(CSS.publishRow());
        return row;
    }

    /**
     * Initializes the "select all/none" buttons, adds them to the group header and 
     * attaches event handlers to them.<p>
     */
    private void initSelectButtons() {

        m_selectAll = new CmsPushButton();
        m_selectAll.setText(Messages.get().key(Messages.GUI_PUBLISH_TOP_PANEL_ALL_BUTTON_0));
        m_selectAll.setImageClass(I_CmsInputLayoutBundle.INSTANCE.inputCss().checkBoxImageChecked());
        m_selectAll.setSize(I_CmsButton.Size.small);
        m_selectAll.setUseMinWidth(true);
        m_selectAll.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                setAllSelected(true);
                m_selectionChangeHandler.onChangePublishSelection();
            }
        });

        m_selectNone = new CmsPushButton();
        m_selectNone.setText(Messages.get().key(Messages.GUI_PUBLISH_TOP_PANEL_NONE_BUTTON_0));
        m_selectNone.setImageClass(I_CmsInputLayoutBundle.INSTANCE.inputCss().checkBoxImageUnchecked());
        m_selectNone.setSize(I_CmsButton.Size.small);
        m_selectNone.setUseMinWidth(true);
        m_selectNone.addClickHandler(new ClickHandler() {

            /**
             * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
             */
            public void onClick(ClickEvent event) {

                setAllSelected(false);
                m_selectionChangeHandler.onChangePublishSelection();
            }
        });

        FlowPanel selectButtons = new FlowPanel();
        selectButtons.add(m_selectAll);
        selectButtons.add(m_selectNone);
        selectButtons.setStyleName(CSS.selectButtons());
        m_header.add(selectButtons);
    }

    /**
     * Shows or hides the resources which have no problems.<p>
     * 
     * @param visible if true, thre resources are shown, else hidden
     */
    private void setNoProblemItemsVisible(boolean visible) {

        for (CmsTreeItem item : m_noProblemItems) {
            item.setVisible(visible);
        }
    }
}
