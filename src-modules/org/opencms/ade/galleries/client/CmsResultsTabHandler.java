/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/Attic/CmsResultsTabHandler.java,v $
 * Date   : $Date: 2010/05/27 10:28:29 $
 * Version: $Revision: 1.6 $
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
 * @version $Revision: 1.6 $ 
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
     * Will be triggered when the result item is clicked.<p>
     * 
     * @param resourcePath the resource path of the result
     * @param resourceType 
     */
    public void onClick(String resourcePath, String resourceType) {

        m_controller.openPreview(resourcePath, resourceType);

    }

    /**
     * Will be triggered when categories parameter are removed.<p>
     */
    public void onRemoveCategories() {

        m_controller.clearCategories();
    }

    /**
     * Will be triggered when galleries parameter are removed.<p>
     */
    public void onRemoveGalleries() {

        m_controller.clearGalleries();
    }

    /**
     * Will be triggered when types parameter are removed.<p>
     */
    public void onRemoveTypes() {

        m_controller.clearTypes();
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
}