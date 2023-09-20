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

import org.opencms.ui.FontOpenCms;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.Table;

/**
 * Table showing available items from A_CmsEditUserGroupRoleDialog.<p>
 */
public class CmsAvailableRoleOrPrincipalTable extends Table {

    /**
     * Remove function.<p>
     */
    class EntryAdd implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            m_dialog.addItem(context);
            m_dialog.init();

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return m_dialog.getAddActionCaption();
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**vaadin serial id. */
    private static final long serialVersionUID = -2348361817972942946L;

    /**Icon property. */
    private static final String PROP_ICON = "icon";

    /**Name property. */
    private static final String PROP_NAME = "name";

    /**Add column. */
    private static final String PROP_ADD = "add";

    /** The context menu. */
    CmsContextMenu m_menu;

    /**Indexed Container. */
    IndexedContainer m_container;

    /**Dialog which holds the table. */
    A_CmsEditUserGroupRoleDialog m_dialog;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * public constructor.<p>
     *
     * @param dialog which displays the table
     */
    @SuppressWarnings("deprecation")
    public CmsAvailableRoleOrPrincipalTable(A_CmsEditUserGroupRoleDialog dialog) {

        m_dialog = dialog;
        setSizeFull();

        setHeight("100%");
        m_container = dialog.getAvailableItemsIndexedContainer(PROP_NAME, PROP_ICON);
        m_container.addContainerProperty(PROP_ADD, com.vaadin.ui.Button.class, null);
        setContainerDataSource(m_container);
        sort(new Object[] {PROP_NAME}, new boolean[] {true});
        setItemIconPropertyId(PROP_ICON);
        setColumnWidth(null, 40);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setSelectable(true);
        setMultiSelect(true);
        setColumnHeader(PROP_NAME, dialog.getItemName());
        setColumnHeader(PROP_ADD, "");

        setColumnWidth(PROP_ADD, 40);
        if (m_dialog.getFurtherColumnId() != null) {
            setVisibleColumns(PROP_ADD, PROP_NAME, m_dialog.getFurtherColumnId());
        } else {
            setVisibleColumns(PROP_ADD, PROP_NAME);
        }

        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 4807195510202231174L;

            public void itemClick(ItemClickEvent event) {

                if (!event.isCtrlKey() && !event.isShiftKey()) {

                    changeValueIfNotMultiSelect(event.getItemId());

                    if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                        m_menu.setEntries(getMenuEntries(), m_dialog.getStringSetValue((Set<Object>)getValue()));
                        m_menu.openForTable(
                            event,
                            event.getItemId(),
                            event.getPropertyId(),
                            CmsAvailableRoleOrPrincipalTable.this);
                    }

                }
            }

        });
        setItemDescriptionGenerator(new ItemDescriptionGenerator() {

            private static final long serialVersionUID = 7367011213487089661L;

            public String generateDescription(Component source, Object itemId, Object propertyId) {

                return m_dialog.getDescriptionForItemId(itemId);
            }
        });
        addGeneratedColumn(PROP_ADD, new ColumnGenerator() {

            private static final long serialVersionUID = -778841579899729529L;

            public Object generateCell(Table source, final Object itemId, Object columnId) {

                Button button = new Button(FontOpenCms.CIRCLE_PLUS);
                button.addStyleName(ValoTheme.BUTTON_BORDERLESS);
                button.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
                button.addClickListener(new Button.ClickListener() {

                    private static final long serialVersionUID = -44051469061574153L;

                    public void buttonClick(ClickEvent event) {

                        m_dialog.addItem(m_dialog.getStringSetValue(Collections.singleton(itemId)));
                        m_dialog.init();

                    }
                });
                //button.set
                return button;
            }

        });
    }

    /**
     * Filters the table according to given search string.<p>
     *
     * @param search string to be looked for.
     */
    public void filterTable(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            if (CmsStringUtil.isEmptyOrWhitespaceOnly(m_dialog.getFurtherColumnId())) {
                m_container.addContainerFilter(new Or(new SimpleStringFilter(PROP_NAME, search, true, false)));
            } else {
                m_container.addContainerFilter(
                    new Or(
                        new SimpleStringFilter(PROP_NAME, search, true, false),
                        new SimpleStringFilter(m_dialog.getFurtherColumnId(), search, true, false)));
            }

        }
        if ((getValue() != null) & !((Set<String>)getValue()).isEmpty()) {
            setCurrentPageFirstItemId(((Set<String>)getValue()).iterator().next());
        }
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
            m_menuEntries.add(new EntryAdd());
        }
        return m_menuEntries;
    }
}
