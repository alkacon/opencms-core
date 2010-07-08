/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/Attic/CmsResultsTabHandler.java,v $
 * Date   : $Date: 2010/07/08 06:50:25 $
 * Version: $Revision: 1.10 $
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
 * The results tab handler.<p>
 * 
 * This class receives event information from the results tab and 
 * delegates it to the gallery controller.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.10 $ 
 * 
 * @since 8.0.0
 */
public class CmsResultsTabHandler extends A_CmsTabHandler {

    /**
     * Constructor.<p>
     * 
     * @param controller the gallery controller
     */
    public CmsResultsTabHandler(CmsGalleryController controller) {

        super(controller);
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#clearParams()
     */
    @Override
    public void clearParams() {

        // nothing to do here
    }

    /**
     * Returns if resource entries in the search result are selectable.<p>
     * 
     * @return if resource entries in the search result are selectable
     */
    public boolean hasSelectResource() {

        return m_controller.hasSelectResource();
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSelection()
     */
    @Override
    public void onSelection() {

        m_controller.updateResultsTab();
    }

    /**
     * @see org.opencms.ade.galleries.client.A_CmsTabHandler#onSort(java.lang.String)
     */
    @Override
    public void onSort(String sortParams) {

        m_controller.sortResults(sortParams);
    }

    /**
     * Will be triggered when the result item is clicked.<p>
     * 
     * @param resourcePath the resource path of the result
     * @param resourceType 
     */
    public void openPreview(String resourcePath, String resourceType) {

        m_controller.openPreview(resourcePath, resourceType);

    }

    /**
     * Selects the given resource and sets its path into the xml-content field or editor link.<p>
     * 
     * @param resourcePath the item resource path 
     * @param title the resource title
     * @param resourceType the item resource type
     */
    public void selectResource(String resourcePath, String title, String resourceType) {

        m_controller.selectResource(resourcePath, title, resourceType);
    }
}