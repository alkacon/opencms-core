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

package org.opencms.ui.apps.messages;

import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.CmsSessionInfo;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.FontOpenCms;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;

/**
 * Class for the table to show all current sessions.<p>
 */
public class CmsBroadcastTable extends Table {

    /**
     * The menu entry to switch to the explorer of concerning site.<p>
     */
    class KillEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            showKillDialog(
                data,
                CmsVaadinUtils.getMessageText(
                    Messages.GUI_MESSAGES_DESTROY_SESSIONS_1,
                    CmsBroadcastApp.getUserNames(data, CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_AND_0))),
                CmsBroadcastTable.this);

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_DESTROY_SESSION_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * The menu entry to switch to the explorer of concerning site.<p>
     */
    class SendBroadcastEntry
    implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            CmsBroadcastApp.showSendBroadcastDialog(
                data,
                CmsVaadinUtils.getMessageText(
                    Messages.GUI_MESSAGES_BROADCAST_SESSIONS_1,
                    CmsBroadcastApp.getUserNames(data, CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_AND_0))),
                CmsBroadcastTable.this);

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry.I_HasCssStyles#getStyles()
         */
        public String getStyles() {

            return ValoTheme.LABEL_BOLD;
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_BROADCAST_SEND_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * All table properties.<p>
     */
    enum TableProperty {

        /**Date of release column.*/
        DateCreated(Messages.GUI_MESSAGES_BROADCAST_COLS_CREATION_0, String.class, "", false),
        /**Icon.*/
        Icon(null, Label.class, null, false),

        /**Icon column.*/
        InactiveTime(Messages.GUI_MESSAGES_BROADCAST_COLS_INACTIVE_0, String.class, "", false),

        /**Is Broadcast send but not displayed.*/
        IS_WAITING(null, Boolean.class, new Boolean(false), false),

        /**Date of expiration column. */
        OrgUnit(Messages.GUI_MESSAGES_BROADCAST_COLS_ORGUNIT_0, String.class, "", false),

        /**Last modified column. */
        Project(Messages.GUI_MESSAGES_BROADCAST_COLS_PROJECT_0, String.class, "", false),

        /**Path column.*/
        Site(Messages.GUI_MESSAGES_BROADCAST_COLS_SITE_0, String.class, "", false),

        /**Broken links column. */
        UserName(Messages.GUI_MESSAGES_BROADCAST_COLS_USER_0, String.class, "", false);

        /**Indicates if column is collapsable.*/
        private boolean m_collapsable;

        /**Default value for column.*/
        private Object m_defaultValue;

        /**Header Message key.*/
        private String m_headerMessage;

        /**Type of column property.*/
        private Class<?> m_type;

        /**
         * constructor.
         *
         * @param headerMessage key
         * @param type to property
         * @param defaultValue of column
         * @param collapsable should this column be collapsable?
         */
        TableProperty(String headerMessage, Class<?> type, Object defaultValue, boolean collapsable) {
            m_headerMessage = headerMessage;
            m_type = type;
            m_defaultValue = defaultValue;
            m_collapsable = collapsable;
        }

        /**
         * Returns list of all properties with non-empty header.<p>
         *
         * @return list of properties
         */
        static List<TableProperty> withHeader() {

            List<TableProperty> props = new ArrayList<TableProperty>();

            for (TableProperty prop : TableProperty.values()) {
                if (prop.m_headerMessage != null) {
                    props.add(prop);
                }
            }
            return props;
        }

        /**
         * Returns the default value of property.<p>
         *
         * @return object
         */
        Object getDefaultValue() {

            return m_defaultValue;
        }

        /**
         * Returns localized header.<p>
         *
         * @return string for header
         */
        String getLocalizedMessage() {

            if (m_headerMessage == null) {
                return "";
            }
            return CmsVaadinUtils.getMessageText(m_headerMessage);
        }

        /**
         * Returns tye of value for given property.<p>
         *
         * @return type
         */
        Class<?> getType() {

            return m_type;
        }

        /**
         * Indicates if column is collapsable.<p>
         *
         * @return boolean, true = is collapsable
         */
        boolean isCollapsable() {

            return m_collapsable;
        }

    }

    /** The logger for this class. */
    static Log LOG = CmsLog.getLog(CmsBroadcastTable.class.getName());

    /**vaadin serial id.*/
    private static final long serialVersionUID = 4136423899776482696L;

    /**Container holding table data.*/
    private IndexedContainer m_container;

    /** The context menu. */
    private CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * public constructor.<p>
     */
    public CmsBroadcastTable() {
        try {
            ini();
            addGeneratedColumn(TableProperty.IS_WAITING, new ColumnGenerator() {

                private static final long serialVersionUID = 7293882771960600520L;

                public Object generateCell(Table source, Object itemId, Object columnId) {

                    if (((Boolean)(source.getItem(itemId).getItemProperty(columnId).getValue())).booleanValue()) {
                        Label textfield = new Label(FontOpenCms.BROADCAST.getHtml());
                        textfield.setContentMode(ContentMode.HTML);
                        return textfield;
                    }
                    return null;
                }
            });
            setColumnWidth(TableProperty.IS_WAITING, 30);
            setCellStyleGenerator(new CellStyleGenerator() {

                private static final long serialVersionUID = 1L;

                public String getStyle(Table source, Object itemId, Object propertyId) {

                    if (TableProperty.UserName.equals(propertyId)) {
                        return " " + OpenCmsTheme.HOVER_COLUMN;
                    }
                    return null;
                }
            });
            addItemClickListener(new ItemClickListener() {

                private static final long serialVersionUID = 7957778390938304845L;

                public void itemClick(ItemClickEvent event) {

                    onItemClick(event, event.getItemId(), event.getPropertyId());
                }

            });
        } catch (CmsException e) {
            LOG.error("Unable to read sessions", e);
        }

    }

    /**
     *Shows the dialog to destroy given sessions.<p>
     *
     * @param ids to kill session
     * @param caption of the window
     * @param table to be updated
     */
    protected static void showKillDialog(Set<String> ids, String caption, final CmsBroadcastTable table) {

        final Window window = CmsBasicDialog.prepareWindow();
        window.setCaption(caption);
        window.setContent(new CmsKillSessionDialog(ids, new Runnable() {

            public void run() {

                window.close();
                try {
                    table.ini();
                } catch (CmsException e) {
                    LOG.error("Error on reading session information", e);
                }

            }

        }));
        A_CmsUI.get().addWindow(window);
    }

    /**
     * Filters the table according to given search string.<p>
     *
     * @param search string to be looked for.
     */
    public void filterTable(String search) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(search)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(TableProperty.UserName, search, true, false),
                    new SimpleStringFilter(TableProperty.Site, search, true, false),
                    new SimpleStringFilter(TableProperty.Project, search, true, false)));
        }
    }

    /**
     * Initializes the table.<p>
     *
     * @throws CmsException when somethink goes wrong
     */
    protected void ini() throws CmsException {

        if (m_container == null) {
            m_container = new IndexedContainer();
            setContainerDataSource(m_container);
        } else {
            m_container.removeAllItems();
        }
        for (TableProperty prop : TableProperty.values()) {
            m_container.addContainerProperty(prop, prop.getType(), prop.getDefaultValue());
            setColumnHeader(prop, prop.getLocalizedMessage());
        }

        setColumnWidth(TableProperty.Icon, 30);
        setSelectable(true);
        setMultiSelect(true);

        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        List<CmsSessionInfo> sessionInfos = OpenCms.getSessionManager().getSessionInfos();
        List<CmsOrganizationalUnit> manageableOus = OpenCms.getRoleManager().getManageableOrgUnits(
            A_CmsUI.getCmsObject(),
            "",
            true,
            false);
        for (CmsSessionInfo session : sessionInfos) {
            CmsUser user = A_CmsUI.getCmsObject().readUser(session.getUserId());
            CmsOrganizationalUnit userOu = OpenCms.getOrgUnitManager().readOrganizationalUnit(
                A_CmsUI.getCmsObject(),
                user.getOuFqn());
            if (!(manageableOus.contains(userOu) && !user.isWebuser())) {
                continue;
            }
            //            CmsListItem item = getList().newItem(sessionInfo.getSessionId().toString());
            Item item = m_container.addItem(session.getSessionId().getStringValue());
            item.getItemProperty(TableProperty.UserName).setValue(user.getName());
            item.getItemProperty(TableProperty.DateCreated).setValue(
                CmsDateUtil.getDateTimeShort(session.getTimeCreated()));
            item.getItemProperty(TableProperty.InactiveTime).setValue(
                String.valueOf(
                    CmsStringUtil.formatRuntime(
                        (new Long(System.currentTimeMillis() - session.getTimeUpdated())).longValue())));
            item.getItemProperty(TableProperty.OrgUnit).setValue(userOu.getName());
            item.getItemProperty(TableProperty.Project).setValue(
                A_CmsUI.getCmsObject().readProject(session.getProject()).getName());
            item.getItemProperty(TableProperty.Site).setValue(session.getSiteRoot());
            Label textfield = new Label(FontOpenCms.CIRCLE.getHtml());
            textfield.setContentMode(ContentMode.HTML);
            item.getItemProperty(TableProperty.Icon).setValue(textfield);
            item.getItemProperty(TableProperty.IS_WAITING).setValue(
                new Boolean(!session.getBroadcastQueue().isEmpty()));

        }

        setVisibleColumns(
            TableProperty.Icon,
            TableProperty.IS_WAITING,
            TableProperty.UserName,
            TableProperty.DateCreated,
            TableProperty.InactiveTime,
            TableProperty.Project,
            TableProperty.Site);

    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        if (m_menuEntries == null) {
            m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
            m_menuEntries.add(new SendBroadcastEntry());
            m_menuEntries.add(new KillEntry());
        }
        return m_menuEntries;
    }

    /**
     * Handles the table item clicks, including clicks on images inside of a table item.<p>
     *
     * @param event the click event
     * @param itemId of the clicked row
     * @param propertyId column id
     */
    void onItemClick(MouseEvents.ClickEvent event, Object itemId, Object propertyId) {

        if (!event.isCtrlKey() && !event.isShiftKey()) {

            changeValueIfNotMultiSelect(itemId);

            // don't interfere with multi-selection using control key
            if (event.getButton().equals(MouseButton.RIGHT) || (propertyId == TableProperty.Icon)) {

                m_menu.setEntries(getMenuEntries(), (Set<String>)getValue());
                m_menu.openForTable(event, itemId, propertyId, this);
            } else if (event.getButton().equals(MouseButton.LEFT) && TableProperty.UserName.equals(propertyId)) {
                CmsBroadcastApp.showSendBroadcastDialog(
                    Collections.singleton(((Set<String>)getValue()).iterator().next()),
                    CmsVaadinUtils.getMessageText(
                        Messages.GUI_MESSAGES_BROADCAST_SESSIONS_1,
                        CmsBroadcastApp.getUserNames(
                            Collections.singleton(((Set<String>)getValue()).iterator().next()),
                            CmsVaadinUtils.getMessageText(Messages.GUI_MESSAGES_AND_0))),
                    this);
            }

        }
    }

    /**
     * Checks value of table and sets it new if needed:<p>
     * if multiselect: new itemId is in current Value? -> no change of value<p>
     * no multiselect and multiselect, but new item not selected before: set value to new item<p>
     *
     * @param itemId if of clicked item
     */
    private void changeValueIfNotMultiSelect(Object itemId) {

        @SuppressWarnings("unchecked")
        Set<String> value = (Set<String>)getValue();
        if (value == null) {
            select(itemId);
        } else if (!value.contains(itemId)) {
            setValue(null);
            select(itemId);
        }
    }

}
