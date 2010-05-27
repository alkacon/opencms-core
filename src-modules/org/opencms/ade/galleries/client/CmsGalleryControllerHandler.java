/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/Attic/CmsGalleryControllerHandler.java,v $
 * Date   : $Date: 2010/05/27 10:28:29 $
 * Version: $Revision: 1.14 $
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

import org.opencms.ade.galleries.client.preview.ui.A_CmsPreview;
import org.opencms.ade.galleries.client.preview.ui.CmsImagePreview;
import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.shared.CmsCategoryBean;
import org.opencms.ade.galleries.shared.CmsGalleryDataBean;
import org.opencms.ade.galleries.shared.CmsGalleryFolderBean;
import org.opencms.ade.galleries.shared.CmsGallerySearchBean;
import org.opencms.ade.galleries.shared.CmsPreviewInfoBean;
import org.opencms.ade.galleries.shared.CmsResourceTypeBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;

import java.util.List;

/**
 * Gallery dialog controller handler.<p>
 * 
 * Delegates the actions of the gallery controller to the gallery dialog.
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.14 $ 
 * 
 * @since 8.0.0

 */
public class CmsGalleryControllerHandler {

    /** The reference to the gallery dialog. */
    private CmsGalleryDialog m_galleryDialog;

    // TODO: move to the preview controller handler
    /** The reference to the preview dialog. */
    private A_CmsPreview m_previewDialog;

    /**
     * Constructor.<p>
     * 
     * @param galleryDialog the reference to the gallery dialog 
     */
    public CmsGalleryControllerHandler(CmsGalleryDialog galleryDialog) {

        m_galleryDialog = galleryDialog;
    }

    /**
     * Returns the dialog root element id.<p>
     * 
     * @return the dialog root element id
     */
    public String getDialogElementId() {

        return m_galleryDialog.getDialogElementId();
    }

    /**
     * Will be triggered when the categories tab is selected.<p>
     */
    public void onCategoriesTabSelection() {

        if (!m_galleryDialog.getCategoriesTab().isInitOpen()) {
            return;
        }
        m_galleryDialog.getCategoriesTab().openFirstLevel();
        m_galleryDialog.getCategoriesTab().setInitOpen(false);
    }

    /**
     * Deletes the html content of the categories parameter and removes the style.<p>
     * 
     * @param categories the categories to remove from selection 
     */
    public void onClearCategories(List<String> categories) {

        m_galleryDialog.getResultsTab().removeCategories();
        if (categories != null) {
            m_galleryDialog.getCategoriesTab().uncheckCategories(categories);
        }
    }

    /**
     * Deletes the html content of the galleries parameter and removes the style.<p>
     * 
     * @param galleries the galleries to remove from selection
     */
    public void onClearGalleries(List<String> galleries) {

        m_galleryDialog.getResultsTab().removeGalleries();
        if (galleries != null) {
            m_galleryDialog.getGalleriesTab().uncheckGalleries(galleries);
        }
    }

    /**
     * Deletes the html content of the types parameter and removes the style.<p>
     * 
     * @param types the types to be removed from selection
     */
    public void onClearTypes(List<String> types) {

        m_galleryDialog.getResultsTab().removeTypes();
        if (types != null) {
            m_galleryDialog.getTypesTab().uncheckTypes(types);
        }
    }

    /**
     * Will be triggered when the galleries tab is selected.<p>
     */
    public void onGalleriesTabSelection() {

        // do nothing
    }

    /**
     * Will be triggered when the initial search is performed.<p>
     *  
     * @param searchObj the current search object
     * @param dialogBean the current dialog data bean
     * @param controller the dialog controller
     */
    public void onInitialSearch(
        CmsGallerySearchBean searchObj,
        CmsGalleryDataBean dialogBean,
        CmsGalleryController controller) {

        m_galleryDialog.fillTabs(dialogBean.getMode().getTabs(), controller);
        if ((m_galleryDialog.getGalleriesTab() != null) && (dialogBean.getGalleries() != null)) {
            setGalleriesTabContent(dialogBean.getGalleries(), searchObj.getGalleries());
        }
        if ((m_galleryDialog.getTypesTab() != null) && (dialogBean.getTypes() != null)) {
            setTypesTabContent(dialogBean.getTypes(), searchObj.getTypes());
        }
        if ((m_galleryDialog.getCategoriesTab() != null) && (dialogBean.getCategories() != null)) {
            setCategoriesTabContent(dialogBean.getCategories());
        }
        if (dialogBean.getStartTab() == GalleryTabId.cms_tab_results) {
            m_galleryDialog.getResultsTab().fillContent(
                searchObj,
                m_galleryDialog.getTypesTab().getTypesParamsPanel(searchObj.getTypes()),
                m_galleryDialog.getGalleriesTab().getGallerisParamsPanel(searchObj.getGalleries()),
                m_galleryDialog.getCategoriesTab().getCategoriesParamsPanel(searchObj.getCategories()));
        }
        m_galleryDialog.selectTab(dialogBean.getStartTab());
    }

    /**
     * Will be triggered when the preview is opened.<p>
     * 
     * @param previewBean the preview bean
     */
    // TODO: open the right pewview depending on the resource type
    // TODO: move to the preview handler
    public void onOpenPreview(String dialogMode, CmsPreviewInfoBean previewBean) {

        // 
        int dialogHeight = m_galleryDialog.getParentPanel().getOffsetHeight();
        int dialogWidth = m_galleryDialog.getParentPanel().getOffsetWidth();
        // TODO: the preview dialog should exists already, only the new size should be updated and the new content filled
        // TODO: do not create new dialog
        /*A_CmsPreview preview = new CmsPreviewDefault(
            dialogMode,
            dialogHeight,
            dialogWidth);
        preview.fillPreviewPanel(dialogHeight, dialogWidth, previewBean.getPreviewHtml());
        preview.fillTabs(dialogHeight, dialogWidth, previewBean);*/

        A_CmsPreview preview = new CmsImagePreview(dialogMode, dialogHeight, dialogWidth);
        preview.fillPreviewPanel(dialogHeight, dialogWidth, previewBean.getPreviewHtml());
        preview.fillTabs(dialogHeight, dialogWidth, previewBean);
        m_galleryDialog.getParentPanel().add(preview);
    }

    /**
     * Will be triggered when the results tab is selected.<p>
     *
     * @param searchObj the current search object 
     */
    public void onResultTabSelection(CmsGallerySearchBean searchObj) {

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

        // do nothing
    }

    /**
     * Will be triggered when the tree is selected.<p>
     * 
     * @param categoryTreeEntry the category root entry
     * @param selectedCategories the selected categories
     */
    public void onUpdateCategories(CmsCategoryTreeEntry categoryTreeEntry, List<String> selectedCategories) {

        m_galleryDialog.getCategoriesTab().updateContent(categoryTreeEntry, selectedCategories);
    }

    /**
     * Will be triggered when categories list is sorted.<p>
     *  
     * @param categoriesList the updated categories list
     * @param selectedCategories the selected categories
     */
    public void onUpdateCategories(List<CmsCategoryBean> categoriesList, List<String> selectedCategories) {

        m_galleryDialog.getCategoriesTab().updateContent(categoriesList, selectedCategories);
    }

    /**
     * Will be triggered when the sort parameters of the galleries list are changed.<p>
     *  
     * @param galleries the updated galleries list
     * @param selectedGalleries the list of galleries to select
     */
    public void onUpdateGalleries(List<CmsGalleryFolderBean> galleries, List<String> selectedGalleries) {

        m_galleryDialog.getGalleriesTab().updateContent(galleries, selectedGalleries);
    }

    /**
     * Will be triggered when the sort parameters of the types list are changed.<p>
     *  
     * @param types the updated types list
     * @param selectedTypes the list of types to select
     */
    public void onUpdateTypes(List<CmsResourceTypeBean> types, List<String> selectedTypes) {

        m_galleryDialog.getTypesTab().updateContent(types, selectedTypes);
    }

    /**
     * Sets the list content of the category tab.<p>
     * 
     * @param categoryRoot the root category tree entry
     */
    public void setCategoriesTabContent(CmsCategoryTreeEntry categoryRoot) {

        m_galleryDialog.getCategoriesTab().fillContent(categoryRoot);
    }

    /**
     * Sets the list content of the galleries tab.<p>
     * 
     * @param galleryInfos the gallery info beans
     * @param selectedGalleries the selected galleries
     */
    public void setGalleriesTabContent(List<CmsGalleryFolderBean> galleryInfos, List<String> selectedGalleries) {

        m_galleryDialog.getGalleriesTab().fillContent(galleryInfos, selectedGalleries);
    }

    /**
     * Sets the list content of the types tab.<p>
     * 
     * @param typeInfos the type info beans
     * @param selectedTypes the selected types
     */
    public void setTypesTabContent(List<CmsResourceTypeBean> typeInfos, List<String> selectedTypes) {

        m_galleryDialog.getTypesTab().fillContent(typeInfos, selectedTypes);
    }

}