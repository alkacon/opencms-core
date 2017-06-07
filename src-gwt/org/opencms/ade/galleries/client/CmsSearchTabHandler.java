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

import org.opencms.ade.galleries.shared.CmsGallerySearchScope;

/**
 * The full text search tab handler.<p>
 *
 * This class receives events information from the full text search tab and
 * delegates them to the gallery search controller.
 *
 * @since 8.0.0
 */
public class CmsSearchTabHandler extends A_CmsTabHandler {

    /**
     * Constructor.<p>
     *
     * @param controller the gallery controller
     */
    public CmsSearchTabHandler(CmsGalleryController controller) {

        super(controller);
    }

    /**
     * Delegates the clear input action (click on the clear button) to the controller.<p>
     */
    public void clearInput() {

        m_controller.clearTextSearch();
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#clearParams()
     */
    @Override
    public void clearParams() {

        m_controller.clearTextSearch();
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSelection()
     */
    @Override
    public void onSelection() {

        // no list present
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSort(java.lang.String, java.lang.String)
     */
    @Override
    public void onSort(String sortParams, String filter) {

        // not available for this tab
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#removeParam(java.lang.String)
     */
    @Override
    public void removeParam(String paramKey) {

        m_controller.removeTextSearchParameter(paramKey);
    }

    /**
     * Delegates the value for the created until date to the controller.<p>
     *
     * @param createdEnd the created until date as long
     */
    public void setDateCreatedEnd(long createdEnd) {

        m_controller.addDateCreatedEnd(createdEnd);
    }

    /**
     * Delegates the value for the created since date to the controller.<p>
     *
     * @param createdStart the created since date as long
     */
    public void setDateCreatedStart(long createdStart) {

        m_controller.addDateCreatedStart(createdStart);
    }

    /**
     * Delegates the value for the modified until date to the controller.<p>
     *
     * @param modifiedEnd the modified until date as long
     */
    public void setDateModifiedEnd(long modifiedEnd) {

        m_controller.addDateModifiedEnd(modifiedEnd);
    }

    /**
     * Delegates the value for the modified since date to the controller.<p>
     *
     * @param modifiedStart the modified since date as long
     */
    public void setDateModifiedStart(long modifiedStart) {

        m_controller.addDateModifiedStart(modifiedStart);
    }

    /**
     * Sets if the search should include expired or unreleased resources.<p>
     *
     * @param includeExpired if the search should include expired or unreleased resources
     * @param fireEvent true if a change event should be fired
     */
    public void setIncludeExpired(boolean includeExpired, boolean fireEvent) {

        m_controller.setIncludeExpired(includeExpired, fireEvent);
    }

    /**
     * Delegates the locale value to the controller.<p>
     *
     * @param locale the locale
     */
    public void setLocale(String locale) {

        m_controller.addLocale(locale);
    }

    /**
     * Sets the search scope.<p>
     *
     * @param scope the search scope
     */
    public void setScope(CmsGallerySearchScope scope) {

        m_controller.addScope(scope);
    }
}