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
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsOrganizationalUnit;
import org.opencms.security.CmsRole;
import org.opencms.security.CmsRoleViolationException;
import org.opencms.ui.A_CmsUI;
import org.opencms.ui.CmsCssIcon;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.components.CmsBasicDialog;
import org.opencms.ui.components.CmsBasicDialog.DialogWidth;
import org.opencms.ui.components.OpenCmsTheme;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsStringUtil;

import java.util.ArrayList;
import java.util.Collections;
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
 * Class to show ous in table for account management.<p>
 */
public class CmsOUTable extends Table implements I_CmsFilterableTable {

    /**
     * Menu Entry for Delete option.<p>
     */
    class EntryDelete implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(final Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow();
            CmsDeleteOUDialog dialog = new CmsDeleteOUDialog(m_cms, context.iterator().next(), window, m_app);
            window.setContent(dialog);
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_OU_DELETE_0));
            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_OU_DELETE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (getItem(context.iterator().next()).getItemProperty(TableProperty.Type).getValue().equals(
                CmsOuTreeType.OU)) {
                if (isAdmin()) {
                    return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
                }
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }

            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;

        }
    }

    /**
     * Edit menu entry.<p>
     */
    class EntryEdit implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow();
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_OU_EDIT_WINDOW_CAPTION_0));
            window.setContent(new CmsOUEditDialog(m_cms, context.iterator().next(), window, m_app));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_OU_EDIT_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            if (getItem(context.iterator().next()).getItemProperty(TableProperty.Type).getValue().equals(
                CmsOuTreeType.OU)) {
                if (isAdmin()) {
                    return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
                }
                return CmsMenuItemVisibilityMode.VISIBILITY_INACTIVE;
            }

            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;

        }

    }

    /**
     * Entry for new user in the context menu.<p>
     */
    class EntryImportExportUser implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            openImportExportDialog(m_cms, context.iterator().next());
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

            if (getItem(context.iterator().next()).getItemProperty(TableProperty.Type).getValue().equals(
                CmsOuTreeType.OU)) {
                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }

    }

    /**
     * New ou menu entry.<p>
     */
    class EntryNewGroup implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow();
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_GROUP_0));
            window.setContent(new CmsGroupEditDialog(m_cms, window, m_parentOu, m_app));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_GROUP_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            Object typeObj = getItem(context.iterator().next()).getItemProperty(TableProperty.Type).getValue();
            if (((I_CmsOuTreeType)typeObj).isGroup()) {
                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;

            }
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }

    }

    /**
     * Entry for new user in the context menu.<p>
     */
    class EntryNewUser implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            Window window = CmsBasicDialog.prepareWindow();
            window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_USER_0));
            window.setContent(new CmsUserEditDialog(m_cms, window, m_parentOu, m_app));

            A_CmsUI.get().addWindow(window);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_ADD_USER_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> context) {

            Object typeObj = getItem(context.iterator().next()).getItemProperty(TableProperty.Type).getValue();
            if (((I_CmsOuTreeType)typeObj).isUser()) {
                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            }
            return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }

    }

    /**
     * Entry to open item in context menu.<p>
     */
    class EntryOpen implements I_CmsSimpleContextMenuEntry<Set<String>>, I_CmsSimpleContextMenuEntry.I_HasCssStyles {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> context) {

            String itemId = context.iterator().next();
            updateApp(itemId);

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

            if (m_app != null) {
                return CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE;
            } else {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }
        }

    }

    /**
     * Table properties.<p>
     */
    enum TableProperty {
        /** Description property.*/
        Description(Messages.GUI_USERMANAGEMENT_OU_DESCRIPTION_0, String.class, ""),
        /**Icon property. */
        Icon(null, Resource.class, new CmsCssIcon(OpenCmsTheme.ICON_OU)),
        /** Name property. */
        Name(Messages.GUI_USERMANAGEMENT_OU_NAME_0, String.class, ""),
        /** Resources property.*/
        Ressources(Messages.GUI_USERMANAGEMENT_OU_EDIT_PANEL2_0, String.class, ""),
        /** Type property. */
        Type(null, I_CmsOuTreeType.class, CmsOuTreeType.OU);

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

            return m_headerMessage == null ? "" : CmsVaadinUtils.getMessageText(m_headerMessage);

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

    /** Log instance for this class. */
    static final Log LOG = CmsLog.getLog(CmsOUTable.class);

    /**vaadin serial id. */
    private static final long serialVersionUID = -1080519790145391678L;

    /**Calling app. */
    protected CmsAccountsApp m_app;

    /**Parent ou. */
    protected String m_parentOu;

    /**CmsObject. */
    CmsObject m_cms;

    /** The context menu. */
    CmsContextMenu m_menu;

    /**Indexed container. */
    private IndexedContainer m_container;

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /**
     * public constructor.<p>
     *
     * @param ou ou
     * @param app calling app
     */
    public CmsOUTable(String ou, CmsAccountsApp app) {

        m_app = app;
        init(ou);
    }

    /**
     * Opens the import/export dialog for a specific OU.
     *
     * @param cms the CMS context
     * @param ou the OU
     */
    public static void openImportExportDialog(CmsObject cms, String ou) {

        boolean includeTechnicalFields = false;
        try {
            OpenCms.getRoleManager().checkRole(cms, CmsRole.ADMINISTRATOR);
            includeTechnicalFields = true;
        } catch (CmsRoleViolationException e) {
            // ok
        }
        Window window = CmsBasicDialog.prepareWindow(DialogWidth.wide);
        window.setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_USER_IMEXPORT_DIALOGNAME_0));
        window.setContent(
            CmsImportExportUserDialog.getExportUserDialogForOU(
                ou,
                window,
                includeTechnicalFields));

        A_CmsUI.get().addWindow(window);
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
     * Checks if user has role to edit ou.<p>
     *
     * @return true if admin
     */
    protected boolean isAdmin() {

        return OpenCms.getRoleManager().hasRole(m_cms, CmsRole.ADMINISTRATOR.forOrgUnit(m_parentOu));
    }

    /**
     * Updates app.<p>
     *
     * @param itemId of current item
     */
    protected void updateApp(String itemId) {

        I_CmsOuTreeType foundType = null;
        for (I_CmsOuTreeType type : m_app.getTreeTypeProvider().getTreeTypes()) {
            if (itemId.equals(type.getId())) {
                foundType = type;
                break;
            }
        }
        if (foundType != null) {
            m_app.update(m_parentOu, foundType, null);
            return;
        }
        m_app.update(itemId, CmsOuTreeType.OU, null, "");
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
            m_menuEntries.add(new EntryNewGroup());
            m_menuEntries.add(new EntryNewUser());
            m_menuEntries.add(new EntryImportExportUser());
            m_menuEntries.add(new EntryDelete());
        }
        return m_menuEntries;
    }

    /**
     * Adds ou to table.<p>
     *
     * @param ou to be added
     */
    private void addOuToTable(CmsOrganizationalUnit ou) {

        if (m_app.isParentOfManagableOU(ou.getName())) {
            Item item = m_container.addItem(ou.getName());
            if (item != null) {
                item.getItemProperty(TableProperty.Name).setValue(ou.getName());
                item.getItemProperty(TableProperty.Description).setValue(ou.getDisplayName(A_CmsUI.get().getLocale()));
                if (ou.hasFlagWebuser()) {
                    item.getItemProperty(TableProperty.Icon).setValue(new CmsCssIcon(OpenCmsTheme.ICON_OU_WEB));
                }
            }
        }
    }

    /**
     * initializes table.<p>
     *
     * @param parentOu ou name
     */
    private void init(String parentOu) {

        m_parentOu = parentOu;
        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        m_container = new IndexedContainer();

        setContainerDataSource(m_container);

        for (TableProperty prop : TableProperty.values()) {
            m_container.addContainerProperty(prop, prop.getType(), prop.getDefault());
            setColumnHeader(prop, prop.getName());
        }
        setContainerDataSource(m_container);
        setItemIconPropertyId(TableProperty.Icon);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);

        setColumnWidth(null, 40);
        setSelectable(true);

        addGeneratedColumn(TableProperty.Ressources, new ColumnGenerator() {

            private static final long serialVersionUID = 4624734503799549261L;

            public Object generateCell(Table source, Object itemId, Object columnId) {

                String out = "";
                try {
                    boolean isOu = true;
                    for (I_CmsOuTreeType type : m_app.getTreeTypeProvider().getTreeTypes()) {
                        if (type.getId().equals(itemId)) {
                            isOu = false;
                        }
                    }
                    if (isOu) {
                        List<CmsResource> resources = OpenCms.getOrgUnitManager().getResourcesForOrganizationalUnit(
                            m_cms,
                            (String)itemId);
                        if (!resources.isEmpty()) {
                            out = resources.get(0).getRootPath();
                            int i = 1;
                            while ((resources.size() > i) & (out.length() < 50)) {
                                out += ", " + resources.get(i).getRootPath();
                            }
                            if (resources.size() > i) {
                                out += " ...";
                            }
                        }
                    }
                } catch (CmsException e) {
                    LOG.error("unable to read resources.", e);
                }
                return out;
            }
        });

        setVisibleColumns(TableProperty.Name, TableProperty.Description, TableProperty.Ressources);

        try {
            m_cms = OpenCms.initCmsObject(A_CmsUI.getCmsObject());
            m_cms.getRequestContext().setSiteRoot("");
        } catch (CmsException e) {
            m_cms = A_CmsUI.getCmsObject();
        }
        try {
            if (m_app.isOUManagable(m_parentOu)) {

                for (I_CmsOuTreeType treeType : m_app.getTreeTypeProvider().getTreeTypes()) {
                    if (treeType.showInOuTable() && treeType.isValidForOu(m_cms, m_parentOu)) {
                        Item item = m_container.addItem(treeType.getId());
                        item.getItemProperty(TableProperty.Name).setValue(treeType.getName());
                        item.getItemProperty(TableProperty.Icon).setValue(treeType.getIcon());
                        item.getItemProperty(TableProperty.Type).setValue(treeType);
                    }
                }
            }
            List<CmsOrganizationalUnit> webOus = new ArrayList<CmsOrganizationalUnit>();
            for (CmsOrganizationalUnit ou : OpenCms.getOrgUnitManager().getOrganizationalUnits(
                m_cms,
                parentOu,
                false)) {

                if (ou.hasFlagWebuser()) {
                    webOus.add(ou);
                } else {
                    addOuToTable(ou);
                }
            }
            for (CmsOrganizationalUnit ou : webOus) {
                addOuToTable(ou);
            }
        } catch (CmsException e) {
            LOG.error("Unable to read ous", e);
        }

        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = 4807195510202231174L;

            public void itemClick(ItemClickEvent event) {

                setValue(null);
                select(event.getItemId());
                if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                    m_menu.setEntries(getMenuEntries(), Collections.singleton((String)getValue()));
                    m_menu.openForTable(event, event.getItemId(), event.getPropertyId(), CmsOUTable.this);
                    return;
                }
                if (event.getButton().equals(MouseButton.LEFT) && event.getPropertyId().equals(TableProperty.Name)) {
                    updateApp((String)getValue());
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
}
