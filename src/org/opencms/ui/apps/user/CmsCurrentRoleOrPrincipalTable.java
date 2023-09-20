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

import org.opencms.file.CmsGroup;
import org.opencms.file.CmsObject;
import org.opencms.main.CmsLog;
import org.opencms.security.CmsPrincipal;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.Table;

/**
 * Class for the table to view and edit groups of a given user.<p>
 */
public class CmsCurrentRoleOrPrincipalTable extends Table {

    /**
     * Remove function.<p>
     */
    class EntryRemove implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            m_dialog.removeItem(context);
            m_dialog.init();
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_REMOVE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        @SuppressWarnings("synthetic-access")
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            List<Item> itemsToCheck = new ArrayList<>();
            for (Object groupObj : m_container.getItemIds()) {
                if (groupObj instanceof CmsGroup) {
                    CmsGroup group = (CmsGroup)groupObj;
                    if (context.contains(group.getName())) {
                        itemsToCheck.add(m_container.getItem(group));
                    }
                }
            }
            for (Item item : itemsToCheck) {
                if (!item.getItemProperty(PROP_STATUS).getValue().equals(Boolean.TRUE)) {
                    return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
                }
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**vaadin serial id. */
    private static final long serialVersionUID = 2443401032626693747L;

    /**Name table column. */
    private static final String PROP_NAME = "name";

    /**Icon table column.*/
    private static final String PROP_ICON = "icon";

    /**Status column. */
    private static final String PROP_STATUS = "status";

    /**Add column. */
    private static final String PROP_REMOVE = "remove";

    /** The log object for this class. */
    protected static final Log LOG = CmsLog.getLog(CmsCurrentRoleOrPrincipalTable.class);

    /**CmsObject.*/
    CmsObject m_cms;

    /**Dialog. */
    A_CmsEditUserGroupRoleDialog m_dialog;

    /**CmsUser to be edited.*/
    CmsPrincipal m_principal;

    /** The context menu. */
    CmsContextMenu m_menu;

    /**Container. */
    private IndexedContainer m_container;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * public constructor.<p>
     *
     * @param dialog dialog
     * @param cms CmsObject
     * @param principal CmsPrincipal
     */
    public CmsCurrentRoleOrPrincipalTable(A_CmsEditUserGroupRoleDialog dialog, CmsObject cms, CmsPrincipal principal) {

        m_cms = cms;
        m_dialog = dialog;
        m_principal = principal;
        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        setSizeFull();
        setHeight("100%");
        m_container = m_dialog.getItemsOfUserIndexedContainer(PROP_NAME, PROP_ICON, PROP_STATUS);

        m_container.addContainerProperty(PROP_REMOVE, com.vaadin.ui.Button.class, null);
        setContainerDataSource(m_container);
        sort(new Object[] {PROP_NAME}, new boolean[] {true});
        setItemIconPropertyId(PROP_ICON);
        setColumnWidth(null, 40);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);

        setSelectable(true);
        setMultiSelect(true);

        setColumnWidth(PROP_REMOVE, 40);
        setColumnHeader(PROP_REMOVE, "");

        if (m_dialog.getFurtherColumnId() != null) {
            setVisibleColumns(PROP_REMOVE, PROP_NAME, m_dialog.getFurtherColumnId());
        } else {
            setVisibleColumns(PROP_REMOVE, PROP_NAME);
        }
        setColumnHeader(PROP_NAME, m_dialog.getItemName());

        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 4807195510202231174L;

            @SuppressWarnings("unchecked")
            public void itemClick(ItemClickEvent event) {

                if (!event.isCtrlKey()
                    && !event.isShiftKey()
                    && ((Boolean)getItem(event.getItemId()).getItemProperty(PROP_STATUS).getValue()).booleanValue()) {

                    changeValueIfNotMultiSelect(event.getItemId());

                    if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                        m_menu.setEntries(getMenuEntries(), m_dialog.getStringSetValue((Set<Object>)getValue()));
                        m_menu.openForTable(
                            event,
                            event.getItemId(),
                            event.getPropertyId(),
                            CmsCurrentRoleOrPrincipalTable.this);
                    }
                }
            }
        });

        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 4685652851810828147L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                if (!((Boolean)source.getItem(itemId).getItemProperty(PROP_STATUS).getValue()).booleanValue()) {
                    return " " + OpenCmsTheme.TABLE_CELL_DISABLED;
                }
                return null;
            }

        });

        setItemDescriptionGenerator(new ItemDescriptionGenerator() {

            private static final long serialVersionUID = 7367011213487089661L;

            public String generateDescription(Component source, Object itemId, Object propertyId) {

                return m_dialog.getDescriptionForItemId(itemId);
            }
        });

        addGeneratedColumn(PROP_REMOVE, new ColumnGenerator() {

            private static final long serialVersionUID = -7212693904376423407L;

            public Object generateCell(Table source, final Object itemId, Object columnId) {

                if (((Boolean)source.getItem(itemId).getItemProperty(PROP_STATUS).getValue()).booleanValue()) {
                    Button button = new Button(FontOpenCms.CIRCLE_MINUS);
                    button.addStyleName(ValoTheme.BUTTON_BORDERLESS);
                    button.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
                    button.addClickListener(new Button.ClickListener() {

                        private static final long serialVersionUID = 3789328000442885119L;

                        public void buttonClick(ClickEvent event) {

                            m_dialog.removeItem(m_dialog.getStringSetValue(Collections.singleton(itemId)));
                            m_dialog.init();

                        }
                    });
                    //button.set
                    return button;
                }
                return null;
            }
        });
    }

    /**
     * Checks value of table and sets it new if needed:<p>
     * if multiselect: new itemId is in current Value? -> no change of value<p>
     * no multiselect and multiselect, but new item not selected before: set value to new item<p>
     *
     * @param itemId if of clicked item
     */
    void changeValueIfNotMultiSelect(Object itemId) {

        @SuppressWarnings("unchecked")
        Set<String> value = (Set<String>)getValue();
        if (value == null) {
            select(itemId);
        } else if (!value.contains(itemId)) {
            setValue(null);
            select(itemId);
        }
    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        if (m_menuEntries == null) {
            m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
            m_menuEntries.add(new EntryRemove());
        }
        return m_menuEntries;
    }
}
