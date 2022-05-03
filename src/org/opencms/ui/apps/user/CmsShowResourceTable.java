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
import org.opencms.security.CmsAccessControlEntry;
import org.opencms.security.CmsPrincipal;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.A_CmsWorkplaceApp;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.ui.apps.CmsFileExplorerConfiguration;
import org.opencms.ui.apps.Messages;
import org.opencms.ui.apps.user.CmsShowResourcesDialog.DialogType;
import org.opencms.ui.contextmenu.CmsContextMenu;
import org.opencms.ui.contextmenu.CmsMenuItemVisibilityMode;
import org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.event.ItemClickEvent;
import com.vaadin.v7.event.ItemClickEvent.ItemClickListener;
import com.vaadin.v7.ui.Table;

/**
 * Table with resources for which a given principal has permissions.<p>
 */
public class CmsShowResourceTable extends Table {

    /**
     * The menu entry to switch to the explorer of concerning site.<p>
     */
    class ExplorerEntry implements I_CmsSimpleContextMenuEntry<Set<String>> {

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#executeAction(java.lang.Object)
         */
        public void executeAction(Set<String> data) {

            CmsResource res;
            try {
                res = getCms().readResource(data.iterator().next());
                openExplorerForParent(res.getRootPath(), res.getStructureId().getStringValue());
            } catch (CmsException e) {
                e.printStackTrace();
            }

        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getTitle(java.util.Locale)
         */
        public String getTitle(Locale locale) {

            return Messages.get().getBundle(locale).key(Messages.GUI_EXPLORER_TITLE_0);
        }

        /**
         * @see org.opencms.ui.contextmenu.I_CmsSimpleContextMenuEntry#getVisibility(java.lang.Object)
         */
        public CmsMenuItemVisibilityMode getVisibility(Set<String> data) {

            if (data == null) {
                return CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
            }

            return data.size() == 1
            ? CmsMenuItemVisibilityMode.VISIBILITY_ACTIVE
            : CmsMenuItemVisibilityMode.VISIBILITY_INVISIBLE;
        }

    }

    /**vaadin serial id. */
    private static final long serialVersionUID = -7843045876535036146L;

    /** Log instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsShowResourceTable.class);

    /**Icon column. */
    private static final String PROP_ICON = "icon";

    /**Name column. */
    private static final String PROP_NAME = "name";

    /**Permission column. */
    private static final String PROP_PERMISSION = "permission";

    /** The available menu entries. */
    private List<I_CmsSimpleContextMenuEntry<Set<String>>> m_menuEntries;

    /** The context menu. */
    CmsContextMenu m_menu;

    /**Indexed Container. */
    private IndexedContainer m_container;

    /**CmsPrincipal. */
    private CmsPrincipal m_principal;

    /**CmsObject. */
    private CmsObject m_cms;

    /**
     * public constructor.<p>
     *
     * @param cms CmsObject
     * @param principalID id of principal
     * @param type of dialog
     */
    public CmsShowResourceTable(CmsObject cms, CmsUUID principalID, DialogType type) {

        //Set menu
        m_menu = new CmsContextMenu();
        m_menu.setAsTableContextMenu(this);

        setSelectable(true);

        try {
            m_cms = cms;
            m_principal = getPrincipal(cms, type, principalID);

            setSizeFull();
            m_container = new IndexedContainer();

            m_container.addContainerProperty(PROP_ICON, com.vaadin.server.Resource.class, null);
            m_container.addContainerProperty(PROP_NAME, String.class, "");
            m_container.addContainerProperty(PROP_PERMISSION, String.class, "");

            Iterator<CmsResource> iterator = getResourcesFromPrincipal(cms, principalID).iterator();
            while (iterator.hasNext()) {
                CmsResource res = iterator.next();
                CmsResourceUtil resUtil = new CmsResourceUtil(cms, res);

                Item item = m_container.addItem(res);
                item.getItemProperty(PROP_ICON).setValue(resUtil.getSmallIconResource());
                item.getItemProperty(PROP_NAME).setValue(res.getRootPath());
                item.getItemProperty(PROP_PERMISSION).setValue(getPermissionString(cms, res, type));
            }
            setContainerDataSource(m_container);
            setItemIconPropertyId(PROP_ICON);
            setRowHeaderMode(RowHeaderMode.ICON_ONLY);

            setColumnWidth(null, 40);
            setVisibleColumns(PROP_NAME, PROP_PERMISSION);
            setColumnHeader(PROP_NAME, CmsVaadinUtils.getMessageText(Messages.GUI_USERMANAGEMENT_GROUP_NAME_0));
            setColumnHeader(
                PROP_PERMISSION,
                CmsVaadinUtils.getMessageText(org.opencms.workplace.commons.Messages.GUI_PERMISSION_0));
        } catch (CmsException e) {
            LOG.error("Can not read user information.", e);
        }
        addItemClickListener(new ItemClickListener() {

            private static final long serialVersionUID = -4738296706762013443L;

            public void itemClick(ItemClickEvent event) {

                setValue(null);
                select(event.getItemId());

                //Right click or click on icon column (=null) -> show menu
                if (event.getButton().equals(MouseButton.RIGHT) || (event.getPropertyId() == null)) {
                    m_menu.setEntries(getMenuEntries(), Collections.singleton(((CmsResource)getValue()).getRootPath()));
                    m_menu.openForTable(event, event.getItemId(), event.getPropertyId(), CmsShowResourceTable.this);
                }

            }
        });
    }

    /**
     * Checks if table is empty.<p>
     *
     * @return true if table is empty
     */
    public boolean hasNoEntries() {

        return m_container.size() == 0;
    }

    /**
     * Gets CmsObject.<p>
     *
     * @return CmsObject
     */
    protected CmsObject getCms() {

        return m_cms;
    }

    /**
     * Returns the available menu entries.<p>
     *
     * @return the menu entries
     */
    List<I_CmsSimpleContextMenuEntry<Set<String>>> getMenuEntries() {

        if (m_menuEntries == null) {
            m_menuEntries = new ArrayList<I_CmsSimpleContextMenuEntry<Set<String>>>();
            m_menuEntries.add(new ExplorerEntry());
        }
        return m_menuEntries;
    }

    /**
     * Opens the explorer for given path and selected resource.<p>
     *
     * @param rootPath to be opened
     * @param uuid to be selected
     */
    void openExplorerForParent(String rootPath, String uuid) {

        String parentPath = CmsResource.getParentFolder(rootPath);

        if (!rootPath.startsWith(m_cms.getRequestContext().getSiteRoot())) {
            m_cms.getRequestContext().setSiteRoot("");
        }
        CmsAppWorkplaceUi.get().showApp(
            CmsFileExplorerConfiguration.APP_ID,
            m_cms.getRequestContext().getCurrentProject().getUuid()
                + A_CmsWorkplaceApp.PARAM_SEPARATOR
                + m_cms.getRequestContext().getSiteRoot()
                + A_CmsWorkplaceApp.PARAM_SEPARATOR
                + parentPath.substring(m_cms.getRequestContext().getSiteRoot().length())
                + A_CmsWorkplaceApp.PARAM_SEPARATOR
                + uuid
                + A_CmsWorkplaceApp.PARAM_SEPARATOR);
    }

    /**
     * Gets the permission string.<p>
     *
     * @param cms CmsObject
     * @param res Resource to get permission for
     * @param type dialog type
     * @return permission string for given resource
     * @throws CmsException thrown if ACE can not be read
     */
    private String getPermissionString(CmsObject cms, CmsResource res, DialogType type) throws CmsException {

        if (type.equals(DialogType.User)) {
            return cms.getPermissions(res.getRootPath(), m_principal.getName()).getPermissionString();
        } else if (type.equals(DialogType.Group)) {
            Iterator<CmsAccessControlEntry> itAces = cms.getAccessControlEntries(res.getRootPath(), false).iterator();
            while (itAces.hasNext()) {
                CmsAccessControlEntry ace = itAces.next();
                if (ace.getPrincipal().equals(m_principal.getId())) {
                    return ace.getPermissions().getPermissionString();
                }
            }
        }
        return "";
    }

    /**
     * Get principal from id.<p>
     *
     * @param cms CmsObject
     * @param type user or group
     * @param id id of principal
     * @return Principal
     * @throws CmsException exception
     */
    private CmsPrincipal getPrincipal(CmsObject cms, DialogType type, CmsUUID id) throws CmsException {

        if (type.equals(DialogType.Group)) {
            return cms.readGroup(id);
        }
        if (type.equals(DialogType.User)) {
            return cms.readUser(id);
        }
        return null;
    }

    /**
     * Get resources set for the given principal.<p>
     *
     * @param cms CmsObject
     * @param id id of principal
     * @return Set of CmsResource
     * @throws CmsException if resources can not be read
     */
    private Set<CmsResource> getResourcesFromPrincipal(CmsObject cms, CmsUUID id) throws CmsException {

        return cms.getResourcesForPrincipal(id, null, false);
    }

}
