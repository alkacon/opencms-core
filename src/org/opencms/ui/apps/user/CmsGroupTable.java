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
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.user.CmsOuTree.CmsOuTreeType;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.menu.CmsMenuItemVisibilityMode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Window;
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
 * Class for the table containing groups of a ou.<p>
 */
public class CmsGroupTable extends Table implements I_CmsFilterableTable, I_CmsToggleTable {

    /**
     * Delete context menu entry.<p>
     */
    class EntryDelete implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow();
            CmsDeleteMultiplePrincipalDialog dialog = new CmsDeleteMultiplePrincipalDialog(
                m_cms,
                context,
                window,
                m_app);
            window.setContent(dialog);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUP_DELETE_0));
            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUP_DELETE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Edit context menu entry.<p>
     */
    class EntryEdit implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow();
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_GROUP_0));
            window.setContent(new CmsGroupEditDialog(m_cms, new CmsUUID(context.iterator().next()), window, m_app));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_EDIT_GROUP_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Open entry for context menu.<p>
     */
    class EntryOpen implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            updateApp(context.iterator().next());
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

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            if (m_app != null) {
                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            } else {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
        }

    }

    /**
     * Show resources context menu entry.<p>
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

            if (context.size() > 1) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
        }

    }

    /**
     * Show resources context menu entry.<p>
     */
    class ImExport implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_IMEXPORT_DIALOGNAME_0));
            window.setContent(
                CmsImportExportUserDialog.getExportUserDialogForGroup(
                    new CmsUUID(context.iterator().next()),
                    m_ou,
                    window));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_IMEXPORT_CONTEXTMENUNAME_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;

        }

    }

    /**Table properties.<p>*/
    enum TableProperty {
        /**Icon column.*/
        Icon(null, Resource.class, new CmsCssIcon(OpenCmsTheme.ICON_GROUP)),
        /**Name column. */
        Name(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUP_NAME_0), String.class, ""),
        /**Desription column. */
        Description(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUP_DESCRIPTION_0), String.class, ""),
        /**OU column. */
        OU(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUP_OU_0), String.class, "");

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

    /**vaadin serial id. */
    private static final long serialVersionUID = -6511159488669996003L;

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsGroupTable.class);

    /**Indexed container. */
    private IndexedContainer m_container;

    /**CmsObject. */
    CmsObject m_cms;

    /** The context menu. */
    CmsContextMenu m_menu;

    /**List of groups. */
    List<CmsGroup> m_groups;

    /**List of indirect groups. */
    List<CmsGroup> m_indirects;

    /**flag indicates if all indirect items (for sub OUs) are loaded. */
    private boolean m_fullLoaded;

    /**Calling app. */
    protected CmsAccountsApp m_app;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**Vaadin component. */
    private VerticalLayout m_emptyLayout;

    /**The ou. */
    protected String m_ou;

    /**
     * public constructor.<p>
     *
     * @param ou ou name
     * @param app calling app.
     * @param showAll
     */
    public CmsGroupTable(String ou, CmsAccountsApp app, boolean showAll) {

        m_app = app;
        m_ou = ou;
        m_indirects = new ArrayList<CmsGroup>();
        List<CmsGroup> directs;
        try {
            m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            m_cms.getRequestContext().setSiteRoot("");
        } catch (CmsException e) {
            m_cms = A_CmsUI.getCmsObject();
        }
        try {
            directs = OpenCms.getOrgUnitManager().getGroups(m_cms, ou, false);
            m_fullLoaded = false;
            if (showAll) {
                setAllGroups(directs);
            } else {
                m_groups = directs;
            }
        } catch (CmsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        init(ou);
        setVisibleColumns(TableProperty.Name, TableProperty.Description, TableProperty.OU);
    }

    /**
     * Filters the table.<p>
     *
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

    public int getCurrentSize() {

        return size();
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsFilterableTable#getEmptyLayout()
     */
    public VerticalLayout getEmptyLayout() {

        m_emptyLayout = CmsVaadinUtils.getInfoLayout(CmsOuTreeType.GROUP.getEmptyMessageKey());
        setVisible(size() > 0);
        m_emptyLayout.setVisible(size() == 0);
        return m_emptyLayout;
    }

    /**
     * @see org.opencms.ui.apps.user.I_CmsToggleTable#toggle(boolean)
     */
    public void toggle(boolean pressed) {

        try {
            if (pressed && !m_fullLoaded) {
                setAllGroups(m_groups);
            }
        } catch (CmsException e) {
            LOG.error("Error loading groups", e);
        }
        fillContainer(pressed);
    }

    /**
     * Fills the container.<p>
     *
     * @param showIndirect true-> show all user, false -> only direct user
     */
    protected void fillContainer(boolean showIndirect) {

        m_container.removeAllContainerFilters();
        m_container.removeAllItems();
        for (CmsGroup group : m_groups) {
            if (!m_indirects.contains(group)) {
                addGroupToContainer(m_container, group);
            }
        }
        if (showIndirect) {
            for (CmsGroup group : m_indirects) {
                addGroupToContainer(m_container, group);
            }
        }
    }

    /**
     * Updates the app.<p>
     *
     * @param uuid of current group
     */
    protected void updateApp(String uuid) {

        try {
            CmsGroup group = m_cms.readGroup(new CmsUUID(uuid));
            m_app.update(group.getOuFqn(), CmsOuTreeType.GROUP, group.getId(), "");
        } catch (CmsException e) {
            LOG.error("unable to read group.", e);
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
            m_menuEntries.add(new EntryOpen());
            m_menuEntries.add(new EntryEdit());
            m_menuEntries.add(new EntryShowResources());
            m_menuEntries.add(new ImExport());
            m_menuEntries.add(new EntryDelete());

        }
        return m_menuEntries;
    }

    private void addGroupToContainer(IndexedContainer container, CmsGroup group) {

        Item item = container.addItem(group);
        item.getItemProperty(TableProperty.Name).setValue(group.getName());
        item.getItemProperty(TableProperty.Description).setValue(group.getDescription(A_CmsUI.get().getLocale()));
        item.getItemProperty(TableProperty.OU).setValue(group.getOuFqn());
    }

    /**
     * Initializes the table.<p>
     *
     * @param ou name
     */
    private void init(String ou) {

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
        setMultiSelect(true);
        setVisibleColumns(TableProperty.Name, TableProperty.OU);

        for (CmsGroup group : m_groups) {
            addGroupToContainer(m_container, group);
        }

        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 4807195510202231174L;

            @SuppressWarnings("unchecked")
            public void itemClick(ItemClickEvent event) {

                changeValueIfNotMultiSelect(event.getItemId());

                if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                    Set<String> groupIds = new HashSet<String>();
                    for (CmsGroup group : (Set<CmsGroup>)getValue()) {
                        groupIds.add(group.getId().getStringValue());
                    }
                    m_menu.setEntries(getMenuEntries(), groupIds);
                    m_menu.openForTable(event, event.getItemId(), event.getPropertyId(), CmsGroupTable.this);
                    return;
                }
                if (event.getButton().equals(MouseButton.LEFT) && event.getPropertyId().equals(TableProperty.Name)) {
                    updateApp((((Set<CmsGroup>)getValue()).iterator().next()).getId().getStringValue());
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
    }

    private void setAllGroups(List<CmsGroup> directs) throws CmsException {

        m_fullLoaded = true;
        m_groups = OpenCms.getOrgUnitManager().getGroups(m_cms, m_ou, true);
        m_indirects.clear();
        for (CmsGroup group : m_groups) {
            if (!directs.contains(group)) {
                m_indirects.add(group);
            }
        }

    }

}
