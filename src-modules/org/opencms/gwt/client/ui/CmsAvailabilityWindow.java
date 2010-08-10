/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/Attic/CmsAvailabilityWindow.java,v $
 * Date   : $Date: 2010/08/10 13:01:47 $
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

package org.opencms.gwt.client.ui;

import org.opencms.gwt.client.Messages;
import org.opencms.gwt.client.ui.css.I_CmsLayoutBundle;
import org.opencms.gwt.client.ui.input.CmsCheckBox;
import org.opencms.gwt.client.ui.input.CmsLabel;
import org.opencms.gwt.client.ui.input.CmsTextBox;
import org.opencms.gwt.client.ui.input.datebox.CmsDateBox;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.FlowData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;

/**
 * The availability dialog.<p>
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.2 $
 * 
 * @since version 8.0.0
 */
public class CmsAvailabilityWindow extends Window {

    /** The panel for the window content. */
    private ContentPanel m_panel = new ContentPanel();

    /** The shadow listener for this window. */
    private CmsWindowShadowListener m_shadowListener = new CmsWindowShadowListener(this);

    /** A handler which closes the window on click. */
    private ClickHandler m_closeHandler = new ClickHandler() {

        /**
         * @see com.google.gwt.event.dom.client.ClickHandler#onClick(com.google.gwt.event.dom.client.ClickEvent)
         */
        public void onClick(ClickEvent event) {

            CmsAvailabilityWindow.this.hide();
        }
    };

    /**
     * @see com.extjs.gxt.ui.client.widget.Window#onRender(com.google.gwt.user.client.Element, int)
     */
    @Override
    protected void onRender(Element parent, int pos) {

        super.onRender(parent, pos);

        setHeading("Availability / Publish Scheduled");
        setWidth(450);
        setModal(true);
        setAutoHeight(true);
        setResizable(false);
        setBorders(false);

        m_panel.setHeaderVisible(false);
        m_panel.setWidth("100%");
        m_panel.setBorders(false);

        //////////////////////////
        // create the file info //
        //////////////////////////
        CmsListItemWidget listItem = createListItem();
        listItem.addClickHandler(m_shadowListener);
        m_panel.add(listItem, new FlowData(10));

        /////////////////////////
        // create the publish  //
        /////////////////////////
        CmsFieldSetFactory fieldSetFactory = new CmsFieldSetFactory("Publish scheduled");
        fieldSetFactory.setLabelWidth("200px");

        CmsLabel publishLabel = new CmsLabel("Publish scheduled date:");
        final CmsDateBox publishDate = new CmsDateBox();

        fieldSetFactory.addFieldSetRow(publishLabel, publishDate);
        FieldSet publishSet = fieldSetFactory.createFieldSet();
        publishSet.addListener(Events.Collapse, m_shadowListener);
        publishSet.addListener(Events.Expand, m_shadowListener);

        m_panel.add(publishSet, new FlowData(10));

        //////////////////////////////
        // create the availability //
        //////////////////////////////
        fieldSetFactory = new CmsFieldSetFactory("Availability");
        fieldSetFactory.setLabelWidth("200px");

        CmsLabel dateReleasedLabel = new CmsLabel("Date Released:");
        final CmsDateBox dateReleased = new CmsDateBox();
        fieldSetFactory.addFieldSetRow(dateReleasedLabel, dateReleased);

        CmsLabel dateExpiredLabel = new CmsLabel("Date Expired:");
        final CmsDateBox dateExpired = new CmsDateBox();
        fieldSetFactory.addFieldSetRow(dateExpiredLabel, dateExpired);

        FieldSet availabilitySet = fieldSetFactory.createFieldSet();

        availabilitySet.addListener(Events.Collapse, m_shadowListener);
        availabilitySet.addListener(Events.Expand, m_shadowListener);
        m_panel.add(availabilitySet, new FlowData(10));

        /////////////////////////////
        // create the notification //
        /////////////////////////////
        fieldSetFactory = new CmsFieldSetFactory("Notification settings");
        fieldSetFactory.setLabelWidth("200px");

        CmsLabel intervalLabel = new CmsLabel("Notification Interval (days):");
        final CmsTextBox interval = new CmsTextBox();
        fieldSetFactory.addFieldSetRow(intervalLabel, interval);

        CmsLabel enableLabel = new CmsLabel("Enable Notification");
        final CmsCheckBox enable = new CmsCheckBox();
        fieldSetFactory.addFieldSetRow(enableLabel, enable);

        fieldSetFactory.addTextRow("There are no responsibles for this resource.");

        FieldSet notificationSet = fieldSetFactory.createFieldSet();

        notificationSet.addListener(Events.Collapse, m_shadowListener);
        notificationSet.addListener(Events.Expand, m_shadowListener);

        m_panel.add(notificationSet, new FlowData(10));

        // add the buttons to the panel
        m_panel.add(createButtons(), new FlowData(10));

        add(m_panel);
    }

    /**
     * Creates a list item widget.<p>
     * 
     * @return the list item widget
     */
    private CmsListItemWidget createListItem() {

        Map<String, String> additionalInfo = new HashMap<String, String>();
        additionalInfo.put(
            "Permalink",
            "http://localhost:8080/opencms/opencms/permalink/ae375399-9e05-11de-87d2-dd9f629b113b.config");
        additionalInfo.put("State", "Changed");
        additionalInfo.put("File path", "/demo_t3/_config/restype.config");
        CmsListItemWidget listItem = new CmsListItemWidget(new CmsListInfoBean(
            "Resource Type Configuration",
            "General file infos",
            additionalInfo));
        listItem.setIcon(org.opencms.gwt.shared.CmsIconUtil.getResourceIconClasses("containerpage_config", false));
        return listItem;
    }

    /**
     * Creates the buttons.<p>
     * 
     * @return the panel with the buttons in it
     */
    private FlowPanel createButtons() {

        FlowPanel buttonPanel = new FlowPanel();
        buttonPanel.setStyleName(I_CmsLayoutBundle.INSTANCE.dialogCss().popupButtonPanel());

        CmsPushButton saveButton = new CmsPushButton();
        saveButton.setTitle(Messages.get().key(Messages.GUI_OK_0));
        saveButton.setText(Messages.get().key(Messages.GUI_OK_0));
        saveButton.setSize(I_CmsButton.Size.medium);
        saveButton.setUseMinWidth(true);
        saveButton.addClickHandler(m_closeHandler);

        CmsPushButton cancelButton = new CmsPushButton();
        cancelButton.setTitle(Messages.get().key(Messages.GUI_CANCEL_0));
        cancelButton.setText(Messages.get().key(Messages.GUI_CANCEL_0));
        cancelButton.setSize(I_CmsButton.Size.medium);
        cancelButton.setUseMinWidth(true);
        cancelButton.addClickHandler(m_closeHandler);

        CmsPushButton resetButton = new CmsPushButton();
        resetButton.setTitle(Messages.get().key(Messages.GUI_RESET_0));
        resetButton.setText(Messages.get().key(Messages.GUI_RESET_0));
        resetButton.setSize(I_CmsButton.Size.medium);
        resetButton.setUseMinWidth(true);
        resetButton.addClickHandler(m_closeHandler);

        buttonPanel.add(saveButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }
}
