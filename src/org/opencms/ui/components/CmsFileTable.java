/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ui.components;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.i18n.CmsMessages;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsCustomComponent;
import org.opencms.ui.I_CmsContextMenuBuilder;
import org.opencms.ui.apps.CmsAppWorkplaceUi;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.vaadin.peter.contextmenu.ContextMenu;

import com.google.common.collect.Lists;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.shared.MouseEventDetails.MouseButton;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.RowHeaderMode;

/**
 * Table for displaying resources.<p>
 */
public class CmsFileTable extends A_CmsCustomComponent {

    /** The serial version id. */
    private static final long serialVersionUID = 5460048685141699277L;

    /** File table property name. */
    public static final String PROPERTY_RESOURCE_NAME = "resourceName";

    /** File table property name. */
    public static final String PROPERTY_TYPE_ICON = "typeIcon";

    /** File table property name. */
    public static final String PROPERTY_TITLE = "title";

    /** File table property name. */
    public static final String PROPERTY_RESOURCE_TYPE = "resourceType";

    /** File table property name. */
    public static final String PROPERTY_NAVIGATION_TEXT = "navigationText";

    /** File table property name. */
    public static final String PROPERTY_SIZE = "size";

    /** File table property name. */
    public static final String PROPERTY_DATE_MODIFIED = "dateModified";

    /** File table property name. */
    public static final String PROPERTY_DATE_CREATED = "dateCreated";

    /** File table property name. */
    public static final String PROPERTY_DATE_RELEASED = "dateReleased";

    /** File table property name. */
    public static final String PROPERTY_DATE_EXPIRED = "dateExpired";

    /** File table property name. */
    public static final String PROPERTY_STATE = "state";

    /** File table property name. */
    public static final String PROPERTY_USER_CREATED = "userCreated";

    /** File table property name. */
    public static final String PROPERTY_USER_MODIFIED = "userModified";

    /** File table property name. */
    public static final String PROPERTY_USER_LOCKED = "userLocked";

    /** File table property name. */
    public static final String PROPERTY_PERMISSIONS = "permissions";

    private I_CmsContextMenuBuilder m_menuBuilder;

    private CmsMessages messages;

    /** The table used to display the resource data. */
    private Table m_fileTable;

    /** The resource data container. */
    private IndexedContainer m_container;
    private ContextMenu m_menu;

    /**
     * Default constructor.<p>
     */
    public CmsFileTable() {

        super();
        m_container = new IndexedContainer();
        m_container.addContainerProperty(PROPERTY_TYPE_ICON, Resource.class, null);
        m_container.addContainerProperty(PROPERTY_RESOURCE_NAME, String.class, null);
        m_container.addContainerProperty(PROPERTY_TITLE, String.class, null);
        m_container.addContainerProperty(PROPERTY_NAVIGATION_TEXT, String.class, null);
        m_container.addContainerProperty(PROPERTY_RESOURCE_TYPE, String.class, null);
        m_container.addContainerProperty(PROPERTY_SIZE, Integer.class, null);
        m_container.addContainerProperty(PROPERTY_PERMISSIONS, String.class, null);
        m_container.addContainerProperty(PROPERTY_DATE_MODIFIED, String.class, null);
        m_container.addContainerProperty(PROPERTY_USER_MODIFIED, String.class, null);
        m_container.addContainerProperty(PROPERTY_DATE_CREATED, String.class, null);
        m_container.addContainerProperty(PROPERTY_USER_CREATED, String.class, null);
        m_container.addContainerProperty(PROPERTY_DATE_RELEASED, String.class, "-");
        m_container.addContainerProperty(PROPERTY_DATE_EXPIRED, String.class, "-");
        m_container.addContainerProperty(PROPERTY_STATE, String.class, null);
        m_container.addContainerProperty(PROPERTY_USER_LOCKED, String.class, null);
        m_fileTable = new Table();
        setCompositionRoot(m_fileTable);
        m_fileTable.addStyleName("borderless");
        m_fileTable.setSizeFull();
        m_fileTable.setColumnCollapsingAllowed(true);
        m_fileTable.setSelectable(true);
        m_fileTable.setMultiSelect(true);

        m_fileTable.setContainerDataSource(m_container);

        // following also sets the column order
        m_fileTable.setVisibleColumns(
            PROPERTY_RESOURCE_NAME,
            PROPERTY_TITLE,
            PROPERTY_NAVIGATION_TEXT,
            PROPERTY_RESOURCE_TYPE,
            PROPERTY_SIZE,
            PROPERTY_PERMISSIONS,
            PROPERTY_DATE_MODIFIED,
            PROPERTY_USER_MODIFIED,
            PROPERTY_DATE_CREATED,
            PROPERTY_USER_CREATED,
            PROPERTY_DATE_RELEASED,
            PROPERTY_DATE_EXPIRED,
            PROPERTY_STATE,
            PROPERTY_USER_LOCKED);

        messages = org.opencms.workplace.explorer.Messages.get().getBundle(CmsAppWorkplaceUi.get().getLocale());

        // using the same order as above
        m_fileTable.setColumnHeaders(
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_NAME_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_TITLE_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_NAVTEXT_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_TYPE_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_SIZE_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_PERMISSIONS_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATELASTMODIFIED_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_USERLASTMODIFIED_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATECREATED_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_USERCREATED_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATERELEASED_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_DATEEXPIRED_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_STATE_0),
            messages.key(org.opencms.workplace.explorer.Messages.GUI_INPUT_LOCKEDBY_0));

        m_fileTable.setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        m_fileTable.setItemIconPropertyId(PROPERTY_TYPE_ICON);

        m_fileTable.setColumnCollapsed(PROPERTY_NAVIGATION_TEXT, true);
        m_fileTable.setColumnCollapsed(PROPERTY_PERMISSIONS, true);
        m_fileTable.setColumnCollapsed(PROPERTY_USER_MODIFIED, true);
        m_fileTable.setColumnCollapsed(PROPERTY_DATE_CREATED, true);
        m_fileTable.setColumnCollapsed(PROPERTY_USER_CREATED, true);
        m_fileTable.setColumnCollapsed(PROPERTY_STATE, true);
        m_fileTable.setColumnCollapsed(PROPERTY_USER_LOCKED, true);

        m_fileTable.addItemClickListener(new ItemClickListener() {

            public void itemClick(ItemClickEvent event) {

                System.out.println("hallo!");

                if (event.getButton().equals(MouseButton.RIGHT)) {
                    Set<CmsUUID> selection = (Set<CmsUUID>)m_fileTable.getValue();
                    if (selection == null) {
                        m_fileTable.select(event.getItemId());
                    } else if (!selection.contains(event.getItemId())) {
                        m_fileTable.setValue(null);
                        m_fileTable.select(event.getItemId());
                    }

                }
            }
        });
        m_menu = new ContextMenu();
        m_fileTable.addValueChangeListener(new ValueChangeListener() {

            public void valueChange(ValueChangeEvent event) {

                Set<CmsUUID> selectedIds = (Set<CmsUUID>)event.getProperty().getValue();
                if ((selectedIds != null) && !selectedIds.isEmpty()) {
                    m_menu.removeAllItems();
                    m_menuBuilder.buildContextMenu(selectedIds, m_menu);
                }
            }
        });

        m_menu.setAsTableContextMenu(m_fileTable);

    }

    /**
     * Fills the resource table.<p>
     *
     * @param cms the current CMS context
     * @param resources the resources which should be displayed in the table
     */
    public void fillTable(CmsObject cms, List<CmsResource> resources) {

        Locale wpLocale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        m_container.removeAllItems();
        CmsResourceUtil resUtil = new CmsResourceUtil(cms);
        for (CmsResource resource : resources) {
            try {
                resUtil.setResource(resource);
                Item resourceItem = m_container.addItem(resource.getStructureId());
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
                CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                    type.getTypeName());
                resourceItem.getItemProperty(PROPERTY_TYPE_ICON).setValue(
                    new ExternalResource(
                        CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + settings.getIcon())));
                resourceItem.getItemProperty(PROPERTY_RESOURCE_NAME).setValue(resource.getName());
                resourceItem.getItemProperty(PROPERTY_TITLE).setValue(resUtil.getTitle());
                resourceItem.getItemProperty(PROPERTY_NAVIGATION_TEXT).setValue(resUtil.getNavText());
                resourceItem.getItemProperty(PROPERTY_RESOURCE_TYPE).setValue(
                    CmsWorkplaceMessages.getResourceTypeName(wpLocale, type.getTypeName()));
                if (resource.isFile()) {
                    resourceItem.getItemProperty(PROPERTY_SIZE).setValue(Integer.valueOf(resource.getLength()));
                }
                resourceItem.getItemProperty(PROPERTY_PERMISSIONS).setValue(resUtil.getPermissionString());
                resourceItem.getItemProperty(PROPERTY_DATE_MODIFIED).setValue(
                    messages.getDateTime(resource.getDateLastModified()));
                resourceItem.getItemProperty(PROPERTY_USER_MODIFIED).setValue(resUtil.getUserLastModified());
                resourceItem.getItemProperty(PROPERTY_DATE_CREATED).setValue(
                    messages.getDateTime(resource.getDateCreated()));
                resourceItem.getItemProperty(PROPERTY_USER_CREATED).setValue(resUtil.getUserCreated());
                resourceItem.getItemProperty(PROPERTY_DATE_RELEASED).setValue(resUtil.getDateReleased());
                resourceItem.getItemProperty(PROPERTY_DATE_EXPIRED).setValue(resUtil.getDateExpired());
                resourceItem.getItemProperty(PROPERTY_STATE).setValue(resUtil.getStateName());
                resourceItem.getItemProperty(PROPERTY_USER_LOCKED).setValue(resUtil.getLockedByName());
            } catch (CmsException e) {
                e.printStackTrace();
                Notification.show(e.getMessage());
            }
        }
    }

    /**
     * Gets the selected structure ids.<p>
     *
     * @return the set of selected structure ids
     */
    @SuppressWarnings("unchecked")
    public Set<CmsUUID> getSelectedIds() {

        return (Set<CmsUUID>)m_fileTable.getValue();
    }

    public Set<CmsUUID> getValue() {

        return (Set<CmsUUID>)m_fileTable.getValue();
    }

    public void setMenuBuilder(I_CmsContextMenuBuilder builder) {

        m_menuBuilder = builder;
    }

    List<CmsResource> getSelectedItems() {

        List<CmsResource> result = Lists.newArrayList();
        return result;

    }
}
