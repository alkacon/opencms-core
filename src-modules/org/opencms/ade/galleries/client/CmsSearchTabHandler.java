/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/Attic/CmsSearchTabHandler.java,v $
 * Date   : $Date: 2010/07/07 12:42:29 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
 * The full text search tab handler.<p>
 * 
 * This class receives events information from the full text search tab and 
 * delegates them to the gallery search controller.
 * 
 * @author Ruediger Kurz
 * 
 * @version $Revision: 1.4 $ 
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
     * Delegates the clear input action (click on the clear button) to the controller.<p>
     * 
     * @param searchQuery the search query
     */
    public void setSearchQuery(String searchQuery) {

        m_controller.addSearchQuery(searchQuery);
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSelection()
     */
    @Override
    public void onSelection() {

        // no list present
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSort(java.lang.String)
     */
    @Override
    public void onSort(String sortParams) {

        // no list present
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
}