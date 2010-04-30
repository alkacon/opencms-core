/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/Attic/CmsGalleryControllerHandler.java,v $
 * Date   : $Date: 2010/04/30 10:17:38 $
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

import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.shared.CmsGalleriesListInfoBean;
import org.opencms.ade.galleries.shared.CmsGalleryDialogBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchObject;
import org.opencms.ade.galleries.shared.CmsTypesListInfoBean;

import java.util.ArrayList;

/**
 * Gallery dialog controller handler.<p>
 * 
 * Delegates the actions of the gallery controller to the gallery dialog.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0

 */
public class CmsGalleryControllerHandler {

    /** The reference to the gallery dialog. */
    private CmsGalleryDialog m_galleryDialog;

    // TODO: wie kann man am Besten die Referenzen auf die tabs uebergeben?
    /**
     * Constructor.<p>
     * 
     * @param galleryDialog the reference to the gallery dialog 
     */
    public CmsGalleryControllerHandler(CmsGalleryDialog galleryDialog) {

        m_galleryDialog = galleryDialog;
    }

    /**
     * Will be triggered when the categories tab is selected.<p>
     */
    public void onCategoriesTabSelection() {

        m_galleryDialog.getCategoriesTab().updateListLayout();
    }

    /**
     * Deletes the html content of the categories parameter and removes the style.<p>
     * 
     * @param categories the categories to remove from selection 
     */
    public void onClearCategories(ArrayList<String> categories) {

        m_galleryDialog.getResultsTab().removeCategories();
        m_galleryDialog.getCategoriesTab().uncheckCategories(categories);
    }

    /**
     * Deletes the html content of the galleries parameter and removes the style.<p>
     * 
     * @param galleries the galleries to remove from selection
     */
    public void onClearGalleries(ArrayList<String> galleries) {

        m_galleryDialog.getResultsTab().removeGalleries();
        m_galleryDialog.getGalleriesTab().uncheckGalleries(galleries);
    }

    /**
     * Deletes the html content of the types parameter and removes the style.<p>
     * 
     * @param types the types to be removed from selection
     */
    public void onClearTypes(ArrayList<String> types) {

        m_galleryDialog.getResultsTab().removeTypes();
        m_galleryDialog.getTypesTab().uncheckTypes(types);
    }

    /**
     * Will be triggered when the galleries tab is selected.<p>
     */
    public void onGalleriesTabSelection() {

        m_galleryDialog.getGalleriesTab().updateListLayout();
    }

    /**
     * Will be triggered when the initial search is performed.<p>
     *  
     * @param searchObj the current search object
     * @param dialogBean the current dialog data bean
     * @param tabIdIndex the tab id to be selected on opening
     * @param isOpenInResults flag to indicate if gallery is opened in results tab
     */
    public void onInitialSearch(
        CmsGallerySearchObject searchObj,
        CmsGalleryDialogBean dialogBean,
        int tabIdIndex,
        boolean isOpenInResults) {

        m_galleryDialog.getGalleriesTab().fillContent(dialogBean);
        m_galleryDialog.getTypesTab().fillContent(dialogBean);
        m_galleryDialog.getCategoriesTab().fillContent(dialogBean);
        m_galleryDialog.getResultsTab().fillContent(
            searchObj,
            m_galleryDialog.getTypesTab().getTypesParamsPanel(searchObj.getTypes()),
            m_galleryDialog.getGalleriesTab().getGallerisParamsPanel(searchObj.getGalleries()),
            m_galleryDialog.getCategoriesTab().getCategoriesParamsPanel(searchObj.getCategories()));

        m_galleryDialog.selectTab(tabIdIndex, isOpenInResults);
    }

    /**
     * Will be triggered when the results tab is selected.<p>
     *
     * @param searchObj the current search object 
     */
    public void onResultTabSelection(CmsGallerySearchObject searchObj) {

        m_galleryDialog.getResultsTab().updateContent(
            searchObj,
            m_galleryDialog.getTypesTab().getTypesParamsPanel(searchObj.getTypes()),
            m_galleryDialog.getGalleriesTab().getGallerisParamsPanel(searchObj.getGalleries()),
            m_galleryDialog.getCategoriesTab().getCategoriesParamsPanel(searchObj.getCategories()));
    }

    /**
     * Will be triggered when the types tab is selected.<p>
     */
    public void onTypesTabSelection() {

        m_galleryDialog.getTypesTab().updateListLayout();
    }

    /**
     * Will be triggered when the sort parameters of the galleries list are changed.<p>
     *  
     * @param galleries the updated galleries list
     */
    public void onUpdateGalleries(ArrayList<CmsGalleriesListInfoBean> galleries) {

        m_galleryDialog.getGalleriesTab().updateGalleries(galleries);
    }

    /**
     * Will be triggered when the sort parameters of the types list are changed.<p>
     *  
     * @param types the updated types list
     */
    public void onUpdateTypes(ArrayList<CmsTypesListInfoBean> types) {

        m_galleryDialog.getTypesTab().updateTypes(types);
    }
}