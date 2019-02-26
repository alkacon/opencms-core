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
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.themes.ValoTheme;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.data.util.filter.Or;
import com.vaadin.v7.data.util.filter.SimpleStringFilter;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.Table;
import com.vaadin.v7.ui.VerticalLayout;

/**
 * Table for the roles.<p>
 */
public class CmsRoleTable extends Table implements I_CmsFilterableTable {

    /**
     *Entry to addition info dialog.<p>
     */
    class EntryOpen implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            updateApp(CmsRole.valueOfId(new CmsUUID(context.iterator().next())));
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

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_OPEN_0);
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
        Icon(null, Resource.class, new CmsCssIcon(OpenCmsTheme.ICON_ROLE)),
        /**Name. */
        Name(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_NAME_0), String.class, ""),
        /**Description. */
        Description(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_DESCRIPTION_0), String.class, ""),
        /**OU. */
        OU(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_OU_0), String.class, "");

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

    /**Name of group to show user for, or null. */
    protected String m_group;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**Parent ou. */
    private String m_parentOU;

    /**AccountsApp instance. */
    private CmsAccountsApp m_app;

    /**
     * public constructor.<p>
     * @param app calling app
     *
     * @param ou name
     */
    public CmsRoleTable(CmsAccountsApp app, String ou) {

        try {
            m_app = app;
            m_parentOU = ou;
            m_cms = getCmsObject();
            List<CmsRole> roles = OpenCms.getRoleManager().getRoles(m_cms, ou, false);
            init(roles);
        } catch (CmsException e) {
            LOG.error("Unable to read user.", e);
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
     * @see org.opencms.ui.apps.user.I_CmsFilterableTable#getEmptyLayout()
     */
    public VerticalLayout getEmptyLayout() {

        VerticalLayout layout = new VerticalLayout();
        layout.setVisible(false);
        return layout;
    }

    /**
     * Updates app.<p>
     * @param role to be set
     */
    protected void updateApp(CmsRole role) {

        m_app.update(m_parentOU, CmsOuTreeType.ROLE, role.getId(), "");
    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        if (m_menuEntries == null) {
            m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
            m_menuEntries.add(new EntryOpen());
            //            m_menuEntries.add(new EntryEditRole());
            //            m_menuEntries.add(new EntryEditGroup());
            //            m_menuEntries.add(new EntryShowResources());
            //            m_menuEntries.add(new EntryAddInfos());
            //            m_menuEntries.add(new EntrySwitchUser());
            //            m_menuEntries.add(new EntryRemoveFromGroup());
            //            m_menuEntries.add(new EntryDelete());
            //            m_menuEntries.add(new EntryKillSession());
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
     * initializes table.
     *
     * @param roles list of user
     */
    private void init(List<CmsRole> roles) {

        CmsRole.applySystemRoleOrder(roles);
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

        for (CmsRole role : roles) {

            Item item = m_container.addItem(role);
            item.getItemProperty(TableProperty.Name).setValue(role.getName(A_CmsUI.get().getLocale()));
            item.getItemProperty(TableProperty.Description).setValue(role.getDescription(A_CmsUI.get().getLocale()));
            item.getItemProperty(TableProperty.OU).setValue(role.getOuFqn());
        }

        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 4807195510202231174L;

            public void itemClick(ItemClickEvent event) {

                setValue(null);
                select(event.getItemId());
                if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                    m_menu.setEntries(
                        getMenuEntries(),
                        Collections.singleton(((CmsRole)getValue()).getId().getStringValue()));
                    m_menu.openForTable(event, event.getItemId(), event.getPropertyId(), CmsRoleTable.this);
                } else if (event.getButton().equals(MouseButton.LEFT)
                    && event.getPropertyId().equals(TableProperty.Name)) {
                    updateApp((CmsRole)getValue());
                }

            }

        });
        setCellStyleGenerator(new CellStyleGenerator() {

            private static final long serialVersionUID = 1L;

            public String getStyle(Table source, Object itemId, Object propertyId) {

                if (TableProperty.Name.equals(propertyId)) {
                    return " " + OpenCmsTheme.HOVER_COLUMN;
                }

                return "";
            }
        });
        setVisibleColumns(TableProperty.Name, TableProperty.Description, TableProperty.OU);
    }
}
