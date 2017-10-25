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
import org.opencms.file.CmsUser;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsDateUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Table;
import com.vaadin.ui.Window;

/**
 * Table for user.<p>
 *
 */
public class CmsUserTable extends Table implements I_CmsFilterableTable {

    /**
     *Entry to addition info dialog.<p>
     */
    class EntryAddInfos implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);
            CmsAdditionalInfosDialog dialog = new CmsAdditionalInfosDialog(
                m_cms,
                new CmsUUID(context.iterator().next()),
                window);
            window.setContent(dialog);
            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return "Additionalinfos";
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Menu entry to delete user.<p>
     */
    class EntryDelete implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow();
            CmsDeletePrincipalDialog dialog = new CmsDeletePrincipalDialog(
                m_cms,
                new CmsUUID(context.iterator().next()),
                window);
            window.setContent(dialog);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_DELETE_0));
            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_DELETE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Menu entry to edit user.<p>
     */
    class EntryEdit implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow();
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_USER_0));
            window.setContent(new CmsUserEditDialog(m_cms, new CmsUUID(context.iterator().next()), window));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_USER_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Menu entry to edit groups.<p>
     */
    class EntryEditGroup implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);

            window.setContent(new CmsUserEditGroupDialog(m_cms, new CmsUUID(context.iterator().next()), window));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_USERGROUP_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Menu entry for editing roles of user.<p>
     */
    class EntryEditRole implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.max);

            window.setContent(new CmsUserEditRoleDialog(m_cms, new CmsUUID(context.iterator().next()), window));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_USERROLES_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Menu entry for show resources of user.<p>
     */
    class EntryShowResources implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_SHOW_RESOURCES_0));
            window.setContent(new CmsShowResourcesDialog(context.iterator().next(), window));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_SHOW_RESOURCES_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**Table properties. */
    enum TableProperty {
        /**Icon. */
        Icon(null, Resource.class, new CmsCssIcon("oc-icon-24-user")),
        /**Name. */
        Name(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_NAME_0), String.class, ""),
        /**Description. */
        Description(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_DESCRIPTION_0), String.class, ""),
        /**OU. */
        OU(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_OU_0), String.class, ""),
        /**Last login. */
        LastLogin(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_LAST_LOGIN_0), String.class, "");

        /**Default value for column.*/
        private Object m_defaultValue;

        /**Header Message key.*/
        private String m_headerMessage;

        /**Type of column property.*/
        private Class<?> m_type;

        /**
         * constructor.<p>
         *
         * @param name Name
         * @param type type
         * @param defaultValue value
         */
        TableProperty(String name, Class<?> type, Object defaultValue) {
            m_headerMessage = name;
            m_type = type;
            m_defaultValue = defaultValue;
        }

        /**
         * The default value.<p>
         *
         * @return the default value object
         */
        Object getDefault() {

            return m_defaultValue;
        }

        /**
         * Gets the name of the property.<p>
         *
         * @return a name
         */
        String getName() {

            return m_headerMessage;

        }

        /**
         * Gets the type of property.<p>
         *
         * @return the type
         */
        Class<?> getType() {

            return m_type;
        }

    }

    /**vaadin serial id.*/
    private static final long serialVersionUID = 7863356514060544048L;

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsUserTable.class);

    /**Indexed container. */
    private IndexedContainer m_container;

    /**CmsObject.*/
    CmsObject m_cms;

    /** The context menu. */
    CmsContextMenu m_menu;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * public constructor.<p>
     *
     * @param ou name
     */
    public CmsUserTable(String ou) {
        try {
            m_cms = getCmsObject();
            List<CmsUser> user = OpenCms.getOrgUnitManager().getUsers(m_cms, ou, false);
            init(user);
            setVisibleColumns(TableProperty.Name, TableProperty.Description, TableProperty.LastLogin);
        } catch (CmsException e) {
            LOG.error("Unable to read user.", e);
        }
    }

    /**
     * puiblic constructor.<p>
     *
     * @param ou ou name
     * @param groupID id of group
     */
    public CmsUserTable(String ou, CmsUUID groupID) {
        try {
            m_cms = getCmsObject();
            List<CmsUser> user = m_cms.getUsersOfGroup(m_cms.readGroup(groupID).getName());
            init(user);
            setVisibleColumns(TableProperty.Name, TableProperty.Description, TableProperty.LastLogin);
        } catch (CmsException e) {
            LOG.error("Unable to read group", e);
        }
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsFilterableTable#filter(java.lang.String)
     */
    public void filter(String data) {

        m_container.removeAllContainerFilters();
        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(data)) {
            m_container.addContainerFilter(
                new Or(
                    new SimpleStringFilter(TableProperty.Name, data, true, false),
                    new SimpleStringFilter(TableProperty.Description, data, true, false)));
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
            m_menuEntries.add(new EntryEdit());
            m_menuEntries.add(new EntryEditRole());
            m_menuEntries.add(new EntryEditGroup());
            m_menuEntries.add(new EntryShowResources());
            m_menuEntries.add(new EntryAddInfos());
            m_menuEntries.add(new EntryDelete());
        }
        return m_menuEntries;
    }

    /**
     * Gets CmsObject.<p>
     *
     * @return cmsobject
     */
    private CmsObject getCmsObject() {

        CmsObject cms;
        try {
            cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            //m_cms.getRequestContext().setSiteRoot("");
        } catch (CmsException e) {
            cms = A_CmsUI.getCmsObject();
        }
        return cms;
    }

    /**
     * initializes table
     *
     * @param userList list of user
     */
    private void init(List<CmsUser> userList) {

        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        m_container = new IndexedContainer();

        for (TableProperty prop : TableProperty.values()) {
            m_container.addContainerProperty(prop, prop.getType(), prop.getDefault());
            setColumnHeader(prop, prop.getName());
        }
        setContainerDataSource(m_container);
        setItemIconPropertyId(TableProperty.Icon);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);

        setColumnWidth(null, 40);
        setSelectable(true);

        setVisibleColumns(TableProperty.Name, TableProperty.OU);

        for (CmsUser group : userList) {

            Item item = m_container.addItem(group);
            item.getItemProperty(TableProperty.Name).setValue(group.getName());
            item.getItemProperty(TableProperty.Description).setValue(group.getDescription(A_CmsUI.get().getLocale()));
            item.getItemProperty(TableProperty.OU).setValue(group.getOuFqn());
            item.getItemProperty(TableProperty.LastLogin).setValue(
                CmsDateUtil.getDateTime(new Date(group.getLastlogin()), DateFormat.SHORT, A_CmsUI.get().getLocale()));
        }

        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 4807195510202231174L;

            public void itemClick(ItemClickEvent event) {

                setValue(null);
                select(event.getItemId());
                if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                    m_menu.setEntries(
                        getMenuEntries(),
                        Collections.singleton(((CmsUser)getValue()).getId().getStringValue()));
                    m_menu.openForTable(event, event.getItemId(), event.getPropertyId(), CmsUserTable.this);
                }

            }

        });
    }
}
