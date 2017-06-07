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

package org.opencms.ade.containerpage.shared;

import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Gallery data loaded initially for the 'Add' menu in the page editor.<p>
 */
public class CmsContainerPageGalleryData implements IsSerializable {

    /** The gallery configuration. */
    private CmsGalleryDataBean m_galleryData;

    /** The last gallery search (may be null). */
    private CmsGallerySearchBean m_gallerySearch;

    /**
     * Default constructor.<p>
     */
    public CmsContainerPageGalleryData() {
        // do nothing
    }

    /**
     * Returns the galleryData.<p>
     *
     * @return the galleryData
     */
    public CmsGalleryDataBean getGalleryData() {

        return m_galleryData;
    }

    /**
     * Returns the gallerySearch.<p>
     *
     * @return the gallerySearch
     */
    public CmsGallerySearchBean getGallerySearch() {

        return m_gallerySearch;
    }

    /**
     * Sets the galleryData.<p>
     *
     * @param galleryData the galleryData to set
     */
    public void setGalleryData(CmsGalleryDataBean galleryData) {

        m_galleryData = galleryData;
    }

    /**
     * Sets the gallerySearch.<p>
     *
     * @param gallerySearch the gallerySearch to set
     */
    public void setGallerySearch(CmsGallerySearchBean gallerySearch) {

        m_gallerySearch = gallerySearch;
    }

}
