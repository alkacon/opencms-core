/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/client/Attic/CmsBrokenLinksPanel.java,v $
 * Date   : $Date: 2010/04/13 09:17:18 $
 * Version: $Revision: 1.4 $
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
import org.opencms.gwt.client.ui.CmsButton;
import org.opencms.gwt.client.ui.CmsList;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.CmsTextButton;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * 
 * The panel for showing links which would be broken by publishing.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 */
public class CmsBrokenLinksPanel extends Composite {

    /** The UiBinder interface. */
    protected interface I_CmsBrokenLinksPanelUiBinder extends UiBinder<Widget, CmsBrokenLinksPanel> {
        // empty
    }

    /** The UiBinder instance for this widget. */
    private static final I_CmsBrokenLinksPanelUiBinder UI_BINDER = GWT.create(I_CmsBrokenLinksPanelUiBinder.class);

    /** The button which makes the publish dialog go back to the "resource selection" state. */
    @UiField
    protected CmsTextButton m_backButton;

    /** The button which cancels the publish dialog. */
    @UiField
    protected CmsTextButton m_cancelButton;

    /** The text shown above the resource panel. */
    @UiField
    protected Label m_label;

    /** The list containing the resource widgets representing broken links. */
    @UiField
    protected CmsList m_list;

    /** The button which causes a "forced publish". */
    @UiField
    protected CmsTextButton m_publishButton;

    /** The publish dialog containing this widget. */
    private CmsPublishDialog m_publishDialog;

    /**
     * Creates a new instance.<p>
     * 
     * @param publishDialog the publish dialog to which this broken links panel belongs.
     */
    public CmsBrokenLinksPanel(CmsPublishDialog publishDialog) {

        initWidget(UI_BINDER.createAndBindUi(this));
        prepareButton(m_publishButton, Messages.get().key(Messages.GUI_PUBLISH_DIALOG_PUBLISH_0));
        prepareButton(m_cancelButton, Messages.get().key(Messages.GUI_PUBLISH_DIALOG_CANCEL_BUTTON_0));
        prepareButton(m_backButton, Messages.get().key(Messages.GUI_PUBLISH_DIALOG_BACK_0));
        m_label.setText(Messages.get().key(Messages.GUI_PUBLISH_DIALOG_BROKEN_LINKS_0));
        m_publishDialog = publishDialog;

    }

    /**
     * Adds a resource bean to be displayed.<p>
     * 
     * @param res a resource bean
     */
    public void addEntry(CmsPublishResource res) {

        CmsListItemWidget itemWidget = CmsPublishGroupPanel.createListItemWidget(res);
        CmsTreeItem item = new CmsTreeItem(false, itemWidget);
        item.setOpen(true);
        item.setContentVisible(true);
        for (CmsPublishResource subRes : res.getRelated()) {
            CmsListItemWidget subWidget = CmsPublishGroupPanel.createListItemWidget(subRes);
            CmsTreeItem subItem = new CmsTreeItem(false, subWidget);
            item.addChild(subItem);
        }
        m_list.addItem(item);

    }

    /**
     * Returns the buttons which should be shown in the publish dialog's button panel.<p>
     * 
     * @return a list of buttons
     */
    public List<CmsButton> getButtons() {

        List<CmsButton> result = new ArrayList<CmsButton>();
        result.add(m_publishButton);
        result.add(m_cancelButton);
        result.add(m_backButton);
        return result;
    }

    /**
     * Sets the resources to be displayed.<p> 
     * 
     * @param resourceBeans the resource beans to be displayed 
     */
    public void setEntries(Collection<CmsPublishResource> resourceBeans) {

        m_list.clear();
        for (CmsPublishResource res : resourceBeans) {
            addEntry(res);
        }
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
     * The event handler for the publish button.<p>
     * 
     * @param e the click event 
     */
    @UiHandler("m_publishButton")
    protected void doClickPublish(ClickEvent e) {

        m_publishDialog.onRequestForcePublish();
    }

    /**
     * Sets the text on a button and formats the button.<p>
     * 
     * @param button the button
     * @param text the text to put on the button
     */
    private void prepareButton(CmsTextButton button, String text) {

        button.setText(text);
        button.useMinWidth(true);
    }
}
