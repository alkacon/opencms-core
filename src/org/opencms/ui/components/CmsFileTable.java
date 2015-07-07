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
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.ui.A_CmsCustomComponent;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplace;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Resource;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.RowHeaderMode;

/**
 * Table for displaying resources.<p>
 */
public class CmsFileTable extends A_CmsCustomComponent {

    /** The serial version id. */
    private static final long serialVersionUID = 5460048685141699277L;

    /** The table used to display the resource data. */
    private Table m_fileTable;

    /** The resource data container. */
    private IndexedContainer m_container;

    /**
     * Default constructor.<p>
     */
    public CmsFileTable() {

        super();
        m_container = new IndexedContainer();
        m_container.addContainerProperty("typeIcon", Resource.class, null);
        m_container.addContainerProperty("resourceName", String.class, null);
        m_container.addContainerProperty("title", String.class, null);
        m_container.addContainerProperty("resourceType", String.class, null);
        m_fileTable = new Table();
        m_fileTable.addStyleName("borderless");
        m_fileTable.setSizeFull();
        //     show row header w/ icon
        m_fileTable.setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setCompositionRoot(m_fileTable);
        m_fileTable.setContainerDataSource(m_container);
        m_fileTable.setItemIconPropertyId("typeIcon");
        m_fileTable.setColumnCollapsingAllowed(true);
        m_fileTable.setColumnCollapsed("typeIcon", true);
        m_fileTable.setSelectable(true);
        m_fileTable.setMultiSelect(true);

        m_fileTable.setColumnHeader("typeIcon", "");
        m_fileTable.setColumnHeader("resourceName", "Name");
        m_fileTable.setColumnHeader("title", "Title");
        m_fileTable.setColumnHeader("resourceType", "Resource type");
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
        for (CmsResource resource : resources) {
            try {
                Item resourceItem = m_container.addItem(resource.getStructureId());
                resourceItem.getItemProperty("resourceName").setValue(resource.getName());
                resourceItem.getItemProperty("title").setValue(
                    cms.readPropertyObject(resource, CmsPropertyDefinition.PROPERTY_TITLE, false).getValue());
                I_CmsResourceType type = OpenCms.getResourceManager().getResourceType(resource.getTypeId());
                resourceItem.getItemProperty("resourceType").setValue(
                    CmsWorkplaceMessages.getResourceTypeName(wpLocale, type.getTypeName()));
                CmsExplorerTypeSettings settings = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                    type.getTypeName());
                resourceItem.getItemProperty("typeIcon").setValue(
                    new ExternalResource(
                        CmsWorkplace.getResourceUri(CmsWorkplace.RES_PATH_FILETYPES + settings.getIcon())));
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
}
