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

package org.opencms.ade.galleries.client;

/**
 * The galleries(folders) tab handler.<p>
 *
 * This class receives event information from the galleries tab and
 * delegates it to the gallery controller.
 *
 * @since 8.0.0
 */
public class CmsGalleriesTabHandler extends A_CmsTabHandler {

    /**
     * Constructor.<p>
     *
     * @param controller the gallery controller
     */
    public CmsGalleriesTabHandler(CmsGalleryController controller) {

        super(controller);
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#clearParams()
     */
    @Override
    public void clearParams() {

        m_controller.clearGalleries();
    }

    /**
     * Returns true if the galleries should be selectable.<p>
     *
     * @return true if the galleries should be selectable
     */
    public boolean hasGalleriesSelectable() {

        return m_controller.hasGalleriesSelectable();
    }

    /**
     * Will be triggered when the user unchecks the checkbox to deselect a gallery.<p>
     *
     * @param galleryPath the category path as id
     */
    public void onDeselectGallery(String galleryPath) {

        m_controller.removeGallery(galleryPath);
    }

    /**
     * Will be triggered when the user checks the checkbox to select a gallery.<p>
     *
     * @param galleryPath the gallery path as id
     */
    public void onSelectGallery(String galleryPath) {

        m_controller.addGallery(galleryPath);
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSelection()
     */
    @Override
    public void onSelection() {

        m_controller.updateGalleriesTab();
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSort(java.lang.String,java.lang.String)
     */
    @Override
    public void onSort(String sortParams, String filter) {

        m_controller.sortGalleries(sortParams, filter);
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#removeParam(java.lang.String)
     */
    @Override
    public void removeParam(String paramKey) {

        m_controller.removeGalleryParam(paramKey);
    }
}