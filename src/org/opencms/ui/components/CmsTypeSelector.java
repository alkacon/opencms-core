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

package org.opencms.ui.components;

import org.opencms.file.types.A_CmsResourceTypeFolderBase;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.CmsVaadinUtils.PropertyId;
import org.opencms.ui.apps.Messages;
import org.opencms.workplace.explorer.CmsExplorerTypeSettings;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.List;

import com.vaadin.server.Resource;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.v7.shared.ui.combobox.FilteringMode;
import com.vaadin.v7.ui.ComboBox;

/**
 * Type selector component.
 */
@SuppressWarnings("deprecation")
public class CmsTypeSelector extends ComboBox {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new type selector component for given resource types.
     */
    public CmsTypeSelector() {

        setCaption(CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_RESOURCE_TYPE_0));
        setDescription(CmsVaadinUtils.getMessageText(Messages.GUI_SOURCESEARCH_RESOURCE_TYPE_HELP_0));
        setWidthFull();
        addStyleName(OpenCmsTheme.TYPE_SELECT);
    }

    /**
     * Updates the resource types.
     * @param resourceTypes the resource types
     */
    public void updateTypes(List<I_CmsResourceType> resourceTypes) {

        IndexedContainer types = generateResourceTypesContainer(resourceTypes);
        types.addContainerFilter(CmsVaadinUtils.FILTER_NO_FOLDERS);
        setContainerDataSource(types);
        setItemCaptionPropertyId(PropertyId.caption);
        setItemIconPropertyId(PropertyId.icon);
        setFilteringMode(FilteringMode.CONTAINS);
    }

    /**
     * Generates the resource types container.
     * @param resourceTypes the resource types
     * @return the resource types container
     */
    private IndexedContainer generateResourceTypesContainer(List<I_CmsResourceType> resourceTypes) {

        CmsVaadinUtils.sortResourceTypes(resourceTypes);
        IndexedContainer types = new IndexedContainer();
        types.addContainerProperty(PropertyId.caption, String.class, null);
        types.addContainerProperty(PropertyId.icon, Resource.class, null);
        types.addContainerProperty(PropertyId.isFolder, Boolean.class, null);
        types.addContainerProperty(PropertyId.isXmlContent, Boolean.class, null);
        for (I_CmsResourceType type : resourceTypes) {
            CmsExplorerTypeSettings typeSetting = OpenCms.getWorkplaceManager().getExplorerTypeSetting(
                type.getTypeName());
            if (typeSetting != null) {
                Item typeItem = types.addItem(type);
                String caption = CmsVaadinUtils.getMessageText(typeSetting.getKey()) + " (" + type.getTypeName() + ")";
                typeItem.getItemProperty(PropertyId.caption).setValue(caption);
                typeItem.getItemProperty(PropertyId.icon).setValue(
                    CmsResourceUtil.getSmallIconResource(typeSetting, null));
                typeItem.getItemProperty(PropertyId.isXmlContent).setValue(
                    Boolean.valueOf(type instanceof CmsResourceTypeXmlContent));
                typeItem.getItemProperty(PropertyId.isFolder).setValue(
                    Boolean.valueOf(type instanceof A_CmsResourceTypeFolderBase));
            }
        }
        return types;
    }
}
