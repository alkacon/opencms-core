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

package org.opencms.ui.apps.cacheadmin;

import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.main.OpenCms;
import org.opencms.ui.CmsVaadinUtils;
import org.opencms.ui.apps.Messages;
import org.opencms.workplace.explorer.CmsResourceUtil;

import java.util.List;

import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.util.IndexedContainer;
import com.vaadin.server.Resource;
import com.vaadin.v7.ui.Table;

/**
 * Table showing information about variations of images.<p>
 */
public class CmsImageVariationsTable extends Table {

    /**vaadin serial id.*/
    private static final long serialVersionUID = 4050556105747017491L;

    /**The icon property.*/
    protected static final String PROP_ICON = "icon";

    /**The name property. */
    protected static final String PROP_NAME = "name";

    /**The dimension property. */
    protected static final String PROP_DIMENSION = "dimension";

    /**The length property. */
    protected static final String PROP_LENGTH = "length";

    /**Indexed container.*/
    private IndexedContainer m_container;

    /**
     * public constructor.<p>
     * @param resource to show variations from
     */
    public CmsImageVariationsTable(String resource) {

        m_container = new IndexedContainer();

        m_container.addContainerProperty(
            PROP_ICON,
            Resource.class,
            CmsResourceUtil.getBigIconResource(
                OpenCms.getWorkplaceManager().getExplorerTypeSetting(CmsResourceTypeImage.getStaticTypeName()),
                null));
        m_container.addContainerProperty(PROP_NAME, String.class, "");
        m_container.addContainerProperty(PROP_DIMENSION, String.class, "");
        m_container.addContainerProperty(PROP_LENGTH, String.class, "");

        List<CmsVariationBean> variations = CmsImageCacheTable.HELPER.getVariations(resource);
        for (CmsVariationBean var : variations) {
            Item item = m_container.addItem(var);
            item.getItemProperty(PROP_NAME).setValue(var.getName());
            item.getItemProperty(PROP_DIMENSION).setValue(var.getDimensions());
            item.getItemProperty(PROP_LENGTH).setValue(var.getLength());
        }

        setContainerDataSource(m_container);

        setColumnHeader(null, "");
        setColumnHeader(PROP_DIMENSION, CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGECACHE_LIST_COLS_SIZE_0));
        setColumnHeader(PROP_NAME, CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGECACHE_LIST_COLS_RESOURCE_0));
        setColumnHeader(PROP_LENGTH, CmsVaadinUtils.getMessageText(Messages.GUI_CACHE_IMAGECACHE_LIST_COLS_LENGTH_0));

        setItemIconPropertyId(PROP_ICON);
        setRowHeaderMode(RowHeaderMode.ICON_ONLY);
        setWidth("100%");
        setHeight("500px");
        setVisibleColumns(PROP_NAME, PROP_DIMENSION, PROP_LENGTH);

    }
}
