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

package org.opencms.ui.apps.user;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsPrincipal;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.event.FieldEvents.TextChangeEvent;
import com.vaadin.v7.event.FieldEvents.TextChangeListener;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.v7.ui.TextField;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Abstract class for dialogs to change role or groups of a given user.<p>
 */
public abstract class A_CmsEditUserGroupRoleDialog extends CmsBasicDialog {

    /**Height of table. */
    private static final String ITEM_HEIGHT = "550px";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(A_CmsEditUserGroupRoleDialog.class);

    /**Vaadin serial id. */
    private static final long serialVersionUID = -5088800626506962263L;

    /** The app instance. */
    protected CmsAccountsApp m_app;

    /**CmsObject. */
    protected CmsObject m_cms;

    /**User object to be edited.*/
    protected CmsPrincipal m_principal;

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param userId id of user
     * @param window window
     * @param app the app instance
     */
    public A_CmsEditUserGroupRoleDialog(CmsObject cms, CmsUUID userId, final Window window, final CmsAccountsApp app) {

        CmsVaadinUtils.readAndLocalizeDesign(this, CmsVaadinUtils.getWpMessagesForCurrentLocale(), null);
        m_cms = cms;
        m_app = app;

        try {
            m_principal = m_cms.readUser(userId);
            displayResourceInfoDirectly(Collections.singletonList(CmsAccountsApp.getPrincipalInfo(m_principal)));
            window.setCaption(CmsVaadinUtils.getMessageText(getWindowCaptionMessageKey(), m_principal.getSimpleName()));
        } catch (CmsException e) {
            LOG.error("Can't read user", e);
        }

        getHLayout().setExpandRatio(getHLayout().getComponent(0), 1);
        getHLayout().setExpandRatio(getHLayout().getComponent(1), 1);

        getCloseButton().addClickListener(new ClickListener() {

            private static final long serialVersionUID = -6272630588945629762L;

            public void buttonClick(ClickEvent event) {

                window.close();
                app.reload();
            }
        });
        init();
    }

    /**
     * Adds given items.<p>
     *
     * @param data data containing information about item to add (see getStringSetValue())
     */
    public abstract void addItem(Set<String> data);

    /**
     * Caption for the add action.<p>
     *
     * @return String
     */
    public abstract String getAddActionCaption();

    /**
     * Caption for the list with items to add.<p>
     *
     * @return String
     */
    public abstract String getAddCaptionText();

    /**
     * Gets container with items which are available but not set.<p>
     *
     * @param caption caption property
     * @param propIcon icon property
     * @return IndexedContainer
     */
    public abstract IndexedContainer getAvailableItemsIndexedContainer(String caption, String propIcon);

    /**
     * Gets the close button to close the window.<p>
     *
     * @return the vaadin button
     */
    public abstract Button getCloseButton();

    /**
     * Gets the caption for the table with the currently set elements for the user.<p>
     *
     * @return String
     */
    public abstract String getCurrentTableCaption();

    /**
     * Gets the description for the item.<p>
     *
     * @param itemId to get description for
     * @return String
     */
    public abstract String getDescriptionForItemId(Object itemId);

    /**
     * Gets the empty message.<p>
     *
     * @return String
     */
    public abstract String getEmptyMessage();

    /**
     * Further ID for a column.
     *
     * @return id
     */
    public abstract String getFurtherColumnId();

    /**
     * Gets the horizontal layout holding the tables.<p>
     *
     * @return horizontal layout
     */
    public abstract HorizontalLayout getHLayout();

    /**
     * Get name of the items.<p>
     *
     * @return String
     */
    public abstract String getItemName();

    /**
     * Gets container for items which are set for the user.<p>
     *
     * @param propName caption property
     * @param propIcon icon property
     * @param propStatus status property
     * @return IndexedContainer
     */
    public abstract IndexedContainer getItemsOfUserIndexedContainer(
        String propName,
        String propIcon,
        String propStatus);

    /**
     *Gets layout for the table with items which are set to the user.<p>
     *
     * @return layout
     */
    public abstract VerticalLayout getLeftTableLayout();

    /**
     * Gets the parent layout.<p>
     *
     * @return layout
     */
    public abstract VerticalLayout getParentLayout();

    /**
     * Gets layout for the table with available item.<p>
     *
     * @return layout
     */
    public abstract VerticalLayout getRightTableLayout();

    /**
     * Get string values from given set of item-object.<p>
     *
     * @param value set of items to create string set from
     * @return set of strings
     */
    public abstract Set<String> getStringSetValue(Set<Object> value);

    /**
     * Gets the window caption message key.<p>
     *
     * @return message key
     */
    public abstract String getWindowCaptionMessageKey();

    /**
     * Remove items represented as strings (see getStringSetValue()).<p>
     *
     * @param items to be removed
     */
    public abstract void removeItem(Set<String> items);

    /**
     * Init method.<p>
     */
    protected void init() {

        setHeightUndefined();
        removeExistingTable(getLeftTableLayout());
        removeExistingTable(getRightTableLayout());

        final CmsAvailableRoleOrPrincipalTable table = new CmsAvailableRoleOrPrincipalTable(this);
        if (getAvailableItemsIndexedContainer("caption", "icon").size() > 0) {
            getRightTableLayout().addComponent(new FixedHeightPanel(table, ITEM_HEIGHT), 0);
        } else {
            getRightTableLayout().addComponent(
                new FixedHeightPanel(CmsVaadinUtils.getInfoLayout(getEmptyMessage()), ITEM_HEIGHT));
        }
        if (getItemsOfUserIndexedContainer("prop1", "prop2", "prop3").size() > 0) {
            getLeftTableLayout().addComponent(
                new FixedHeightPanel(new CmsCurrentRoleOrPrincipalTable(this, m_cms, m_principal), ITEM_HEIGHT),
                0);
        } else {
            getLeftTableLayout().addComponent(
                new FixedHeightPanel(CmsVaadinUtils.getInfoLayout(getEmptyMessage()), ITEM_HEIGHT));
        }

        TextField siteTableFilter = new TextField();
        siteTableFilter.setIcon(FontOpenCms.FILTER);
        siteTableFilter.setInputPrompt(
            Messages.get().getBundle(UI.getCurrent().getLocale()).key(Messages.GUI_EXPLORER_FILTER_0));
        siteTableFilter.addStyleName(ValoTheme.TEXTFIELD_INLINE_ICON);
        siteTableFilter.setWidth("200px");
        siteTableFilter.addTextChangeListener(new TextChangeListener() {

            private static final long serialVersionUID = 1L;

            public void textChange(TextChangeEvent event) {

                table.filterTable(event.getText());
            }
        });
        if (getParentLayout().getComponent(0) instanceof TextField) {
            getParentLayout().removeComponent(getParentLayout().getComponent(1));
            getParentLayout().removeComponent(getParentLayout().getComponent(0));
        }
        HorizontalLayout caps = new HorizontalLayout();
        caps.setSpacing(true);
        caps.setWidth("100%");
        caps.setHeight("30px");
        caps.addComponent(new Label(getCurrentTableCaption()));
        caps.addComponent(new Label(getAddCaptionText()));
        getParentLayout().addComponent(caps, 0);
        getParentLayout().addComponent(siteTableFilter, 0);
        getParentLayout().setComponentAlignment(siteTableFilter, com.vaadin.ui.Alignment.TOP_RIGHT);
        getParentLayout().setExpandRatio(getParentLayout().getComponent(2), 1);
    }

    /**
     * Check if table exists in given layout. Removes all tables.<p>
     *
     * @param layout to be cleaned from tables
     */
    private void removeExistingTable(VerticalLayout layout) {

        List<Component> tobeRemoved = new ArrayList<Component>();
        Iterator<Component> it = layout.iterator();
        while (it.hasNext()) {
            Component comp = it.next();
            if ((comp instanceof FixedHeightPanel) | (comp instanceof TextField) | (comp instanceof VerticalLayout)) {
                tobeRemoved.add(comp);
            }
        }
        for (Component c : tobeRemoved) {
            layout.removeComponent(c);
        }
    }
}
