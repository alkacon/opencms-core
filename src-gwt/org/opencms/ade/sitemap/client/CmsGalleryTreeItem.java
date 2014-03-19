/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.client;

import org.opencms.ade.sitemap.shared.CmsGalleryFolderEntry;
import org.opencms.ade.sitemap.shared.CmsGalleryType;
import org.opencms.gwt.client.ui.CmsListItemWidget;
import org.opencms.gwt.client.ui.tree.CmsTreeItem;
import org.opencms.gwt.shared.CmsIconUtil;
import org.opencms.gwt.shared.CmsListInfoBean;
import org.opencms.gwt.shared.property.CmsClientProperty;

/**
 * Tree item for the gallery view.<p>
 */
public class CmsGalleryTreeItem extends CmsTreeItem {

    /**
     * Constructor.<p>
     * 
     * @param galleryFolder the gallery folder
     */
    public CmsGalleryTreeItem(CmsGalleryFolderEntry galleryFolder) {

        super(true, createListWidget(galleryFolder));
    }

    /**
     * Constructor.<p>
     * 
     * @param galleryType the gallery type
     */
    public CmsGalleryTreeItem(CmsGalleryType galleryType) {

        super(true, createListWidget(galleryType));
    }

    /**
     * Creates the list item widget for the given folder.<p>
     * 
     * @param galleryFolder the gallery folder
     * 
     * @return the list item widget
     */
    private static CmsListItemWidget createListWidget(CmsGalleryFolderEntry galleryFolder) {

        String title = galleryFolder.getOwnProperties().get(CmsClientProperty.PROPERTY_TITLE).getStructureValue();
        CmsListInfoBean infoBean = new CmsListInfoBean(title, galleryFolder.getSitePath(), null);
        CmsListItemWidget result = new CmsListItemWidget(infoBean);
        result.setIcon(CmsIconUtil.getResourceIconClasses(
            galleryFolder.getResourceType(),
            galleryFolder.getSitePath(),
            false));
        return result;
    }

    /**
     * Creates the list item widget for the given type.<p>
     * 
     * @param galleryType the gallery type
     * 
     * @return the list item widget
     */
    private static CmsListItemWidget createListWidget(CmsGalleryType galleryType) {

        CmsListInfoBean infoBean = new CmsListInfoBean(galleryType.getNiceName(), galleryType.getDescription(), null);
        CmsListItemWidget result = new CmsListItemWidget(infoBean);
        result.setIcon(CmsIconUtil.getResourceIconClasses(galleryType.getTypeName(), null, false));
        return result;
    }
}
