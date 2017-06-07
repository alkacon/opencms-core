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

import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsWorkflowAction;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsPushButton;
import org.opencms.gwt.client.ui.CmsScrollPanel;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.client.util.CmsDomUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * The panel for showing links which would be broken by publishing.<p>
 *
 * @since 8.0.0
 */
public class CmsBrokenLinksPanel extends Composite {

    /** The UiBinder interface. */
    protected interface I_CmsBrokenLinksPanelUiBinder extends UiBinder<Widget, CmsBrokenLinksPanel> {
        // empty
    }

    /** Text metrics key. */
    private static final String TM_PUBLISH_BROKEN = "PublishBroken";

    /** The UiBinder instance for this widget. */
    private static final I_CmsBrokenLinksPanelUiBinder UI_BINDER = GWT.create(I_CmsBrokenLinksPanelUiBinder.class);

    /** The button which makes the publish dialog go back to the "resource selection" state. */
    @UiField
    protected CmsPushButton m_backButton;

    /** The button which cancels the publish dialog. */
    @UiField
    protected CmsPushButton m_cancelButton;

    /** The text shown above the resource panel. */
    @UiField
    protected Label m_label;

    /** The list containing the resource widgets representing broken links. */
    @UiField
    protected CmsList<CmsTreeItem> m_list;

    /** The scroll panel containing the group panel. */
    @UiField
    protected CmsScrollPanel m_scrollPanel;

    /** The action buttons. */
    private List<CmsPushButton> m_actionButtons;

    /** The available work flow actions. */
    private List<CmsWorkflowAction> m_actions;

    /** The publish dialog containing this widget. */
    private CmsPublishDialog m_publishDialog;

    /** Button slot mapping for showing broken links. */
    public static int[] SLOT_MAPPING;

    /**
     * Creates a new instance.<p>
     *
     * @param publishDialog the publish dialog to which this broken links panel belongs.
     * @param scrollPanelHeight the available scroll panel height
     */
    public CmsBrokenLinksPanel(CmsPublishDialog publishDialog, int scrollPanelHeight) {

        initWidget(UI_BINDER.createAndBindUi(this));
        m_scrollPanel.getElement().getStyle().setPropertyPx(CmsDomUtil.Style.maxHeight.toString(), scrollPanelHeight);
        prepareButton(m_cancelButton, Messages.get().key(Messages.GUI_PUBLISH_DIALOG_CANCEL_BUTTON_0));
        prepareButton(m_backButton, Messages.get().key(Messages.GUI_PUBLISH_DIALOG_BACK_0));
        m_label.setText(Messages.get().key(Messages.GUI_PUBLISH_DIALOG_BROKEN_LINKS_0));
        m_publishDialog = publishDialog;
        m_list.truncate(TM_PUBLISH_BROKEN, CmsPublishDialog.DIALOG_WIDTH);
        m_actionButtons = new ArrayList<CmsPushButton>();
    }

    static {
        SLOT_MAPPING = new int[4];
        SLOT_MAPPING[CmsPublishGroupPanel.SLOT_WARNING] = 0;
        SLOT_MAPPING[CmsPublishGroupPanel.SLOT_EDIT] = -1;
        SLOT_MAPPING[CmsPublishGroupPanel.SLOT_REMOVE] = -1;
        SLOT_MAPPING[CmsPublishGroupPanel.SLOT_MENU] = -1;
    }

    /**
      * Adds a resource bean to be displayed.<p>
      *
      * @param res a resource bean
      * @return the list item widget of the created entry
      */
    public CmsListItemWidget addEntry(CmsPublishResource res) {

        final CmsListItemWidget itemWidget = CmsPublishGroupPanel.createListItemWidget(res, SLOT_MAPPING);
        CmsTreeItem item = new CmsTreeItem(false, itemWidget);

        item.setOpen(true);
        for (CmsPublishResource subRes : res.getRelated()) {
            final CmsListItemWidget subWidget = CmsPublishGroupPanel.createListItemWidget(subRes, SLOT_MAPPING);
            CmsTreeItem subItem = new CmsTreeItem(false, subWidget);
            item.addChild(subItem);
        }
        m_list.addItem(item);
        m_scrollPanel.onResizeDescendant();
        return itemWidget;
    }

    /**
     * Returns the buttons which should be shown in the publish dialog's button panel.<p>
     *
     * @return a list of buttons
     */
    public List<CmsPushButton> getButtons() {

        List<CmsPushButton> result = new ArrayList<CmsPushButton>();
        result.add(m_backButton);
        result.add(m_cancelButton);
        m_actionButtons.clear();
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
                m_actionButtons.add(actionButton);
            }
        }
        result.addAll(m_actionButtons);
        return result;
    }

    /**
     * Sets the resources to be displayed.<p>
     *
     * @param resourceBeans the resource beans to be displayed
     * @param actions the available actions
     */
    public void setEntries(Collection<CmsPublishResource> resourceBeans, List<CmsWorkflowAction> actions) {

        m_list.clear();
        CmsListItemWidget listItemWidget = null;
        for (CmsPublishResource res : resourceBeans) {
            listItemWidget = addEntry(res);
        }
        if (listItemWidget != null) {
            final CmsListItemWidget lastListItemWidget = listItemWidget;
            Timer timer = new Timer() {

                @Override
                public void run() {

                    CmsDomUtil.resizeAncestor(lastListItemWidget);
                }
            };
            timer.schedule(10);
        }
        m_actions = actions;
    }

    /**
     * Updates the dialog title.<p>
     **/
    public void updateTitle() {

        m_publishDialog.setCaption(
            Messages.get().key(
                Messages.GUI_PUBLISH_DIALOG_PROBLEMS_2,
                m_publishDialog.getSelectedWorkflow().getNiceName(),
                String.valueOf(m_list.getWidgetCount())));
    }

    /**
     * The event handler for the back button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_backButton")
    protected void doClickBack(ClickEvent e) {

        m_publishDialog.onGoBack();
    }

    /**
     * The event handler for the cancel button.<p>
     *
     * @param e the click event
     */
    @UiHandler("m_cancelButton")
    protected void doClickCancel(ClickEvent e) {

        m_publishDialog.onCancel();
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
     * Sets the text on a button and formats the button.<p>
     *
     * @param button the button
     * @param text the text to put on the button
     */
    private void prepareButton(CmsPushButton button, String text) {

        button.setText(text);
        button.setUseMinWidth(true);
    }
}
