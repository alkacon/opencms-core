/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsGalleryDataBean.java,v $
 * Date   : $Date: 2010/05/14 13:34:52 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.galleries.shared;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.shared.CmsCategoryTreeEntry;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This bean holding the gallery dialog information required for displaying the content of the gallery tabs.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
//TODO: rename in CmsGalleryDialogData
public class CmsGalleryDataBean implements IsSerializable {

    /** Name of the used JS variable. */
    public static final String DICT_NAME = "cms_gallery_data_bean";

    /**
     * Provides ascending sorting according to the object id.<p>
     * 
     * Applicable for all CmsListInfoBeans implementing I_CmsItemId.
     */
    @Deprecated
    protected class CmsSortIdAsc implements Comparator<I_CmsItemId> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(I_CmsItemId o1, I_CmsItemId o2) {

            return o1.getId().compareTo(o2.getId());
        }
    }

    /**
     * Provides descending sorting according to the object id.<p>
     * 
     * Applicable for all CmsListInfoBeans implementing I_CmsItemId.
     */
    @Deprecated
    protected class CmsSortIdDesc implements Comparator<I_CmsItemId> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(I_CmsItemId o1, I_CmsItemId o2) {

            return o2.getId().compareTo(o1.getId());
        }
    }

    /**
     * Provides descending sorting according to the object title.<p>
     * 
     * Applicable for all CmsListInfoBeans.
     */
    @Deprecated
    protected class CmsSortTitleDesc implements Comparator<CmsListInfoBean> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CmsListInfoBean o1, CmsListInfoBean o2) {

            return o2.getTitle().compareTo(o1.getTitle());
        }
    }

    /**
     * Provides ascending sorting according to the galleries resource type.<p>
     * 
     * Galleries specific comparator.<p>
     */
    @Deprecated
    protected class CmsSortTypeAsc implements Comparator<CmsGalleriesListInfoBean> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CmsGalleriesListInfoBean o1, CmsGalleriesListInfoBean o2) {

            return o1.getGalleryTypeName().compareTo(o2.getGalleryTypeName());
        }
    }

    /**
     * Provides descending sorting according to the galleries resource type.<p>
     * 
     * Galleries specific comparator.<p>
     */
    @Deprecated
    protected class CmsSortTypeDesc implements Comparator<CmsGalleriesListInfoBean> {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(CmsGalleriesListInfoBean o1, CmsGalleriesListInfoBean o2) {

            return o2.getGalleryTypeName().compareTo(o1.getGalleryTypeName());
        }
    }

    //TODO: add sitemap data, add vfs tree data, add container page data, resource locales if required

    /** The categories to display in the list of available categories. */
    private List<CmsCategoryInfoBean> m_categoriesList;

    /** The category tree entry to display as tree. */
    private CmsCategoryTreeEntry m_categoryTreeEntry;

    /** The galleries to display in the list with available galleries. */
    private List<CmsGalleriesListInfoBean> m_galleries;

    /** The available workplace locales. */
    private Map<String, String> m_locales;

    /** The types to display in the list of available categories. */
    private List<CmsTypesListInfoBean> m_types;

    private GalleryMode m_mode;

    private GalleryTabId m_startTab;

    /**
     * Returns the categories.<p>
     *
     * @return the categories
     */
    public CmsCategoryTreeEntry getCategories() {

        return m_categoryTreeEntry;
    }

    /**
     * Returns the categoriesList.<p>
     *
     * @return the categoriesList
     */
    public List<CmsCategoryInfoBean> getCategoriesList() {

        return m_categoriesList;
    }

    /**
     * Returns the galleries map.<p>
     *
     * @return the galleries
     */
    public List<CmsGalleriesListInfoBean> getGalleries() {

        return m_galleries;
    }

    /**
     * Returns the locales map.<p>
     *
     * @return the locales
     */
    public Map<String, String> getLocales() {

        return m_locales;
    }

    /**
     * Returns the types map.<p>
     *
     * @return the types
     */
    public List<CmsTypesListInfoBean> getTypes() {

        return m_types;
    }

    /**
     * Sets the categories.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(CmsCategoryTreeEntry categories) {

        m_categoryTreeEntry = categories;
    }

    /**
     * Sets the categoriesList.<p>
     *
     * @param categoriesList the categoriesList to set
     */
    public void setCategoriesList(List<CmsCategoryInfoBean> categoriesList) {

        m_categoriesList = categoriesList;
    }

    /**
     * Sets the galleries map.<p>
     *
     * @param galleries the galleries to set
     */
    public void setGalleries(List<CmsGalleriesListInfoBean> galleries) {

        m_galleries = galleries;
    }

    /**
     * Sets the locales map.<p>
     *
     * @param locales the locales to set
     */
    public void setLocales(Map<String, String> locales) {

        m_locales = locales;
    }

    /**
     * Sets the types map.<p>
     *
     * @param types the types to set
     */
    public void setTypes(List<CmsTypesListInfoBean> types) {

        m_types = types;
    }

    /**
     * Sorts the categories according to provided sort parameters.<p>
     * 
     * @param sortParams the sort parameters
     */
    @Deprecated
    public void sortCategories(String sortParams) {

        if (SortParams.title_asc == SortParams.valueOf(sortParams)) {
            if (m_categoriesList == null) {
                m_categoriesList = new ArrayList<CmsCategoryInfoBean>();
                treeToList(m_categoryTreeEntry.getChildren());
            }
            Collections.sort(m_categoriesList);

        } else if (SortParams.title_desc == SortParams.valueOf(sortParams)) {
            if (m_categoriesList == null) {
                m_categoriesList = new ArrayList<CmsCategoryInfoBean>();
                treeToList(m_categoryTreeEntry.getChildren());
            }
            Collections.sort(m_categoriesList, new CmsSortTitleDesc());
        }
    }

    /**
     * Sorts the gallery list.<p>
     * 
     * @param sortParams the sort parameters
     */
    @Deprecated
    public void sortGalleries(String sortParams) {

        if (SortParams.title_asc == SortParams.valueOf(sortParams)) {
            Collections.sort(m_galleries);
        } else if (SortParams.title_desc == SortParams.valueOf(sortParams)) {
            Collections.sort(m_galleries, new CmsSortTitleDesc());
        } else if (SortParams.type_asc == SortParams.valueOf(sortParams)) {
            Collections.sort(m_galleries, new CmsSortTypeAsc());
        } else if (SortParams.type_desc == SortParams.valueOf(sortParams)) {
            Collections.sort(m_galleries, new CmsSortTypeDesc());
        } else if (SortParams.path_asc == SortParams.valueOf(sortParams)) {
            Collections.sort(m_galleries, new CmsSortIdAsc());
        } else if (SortParams.path_desc == SortParams.valueOf(sortParams)) {
            Collections.sort(m_galleries, new CmsSortIdDesc());
        }
    }

    /**
     * Sorts the types list.<p>
     * 
     * @param sortParams the sort parameters
     */
    @Deprecated
    public void sortTypes(String sortParams) {

        if (SortParams.title_asc.name().equals(sortParams)) {
            Collections.sort(m_types);
        } else if (SortParams.title_desc.name().equals(sortParams)) {
            Collections.sort(m_types, new CmsSortTitleDesc());
        }
    }

    /**
     * Converts categories tree to a list of tree info beans.<p>
     * 
     * @param entries the tree entries
     */
    @Deprecated
    private void treeToList(List<CmsCategoryTreeEntry> entries) {

        if (entries != null) {
            for (CmsCategoryTreeEntry entry : entries) {
                CmsCategoryInfoBean bean = new CmsCategoryInfoBean(
                    entry.getTitle(),
                    entry.getPath(),
                    null,
                    entry.getPath(),
                    entry.getIconResource());
                m_categoriesList.add(bean);
                treeToList(entry.getChildren());
            }
        }
    }

    /**
     * Sets the gallery mode.<p>
     *
     * @param mode the gallery mode to set
     */
    public void setMode(GalleryMode mode) {

        m_mode = mode;
    }

    /**
     * Returns the gallery mode.<p>
     *
     * @return the gallery mode
     */
    public GalleryMode getMode() {

        return m_mode;
    }

    /**
     * Sets the start tab.<p>
     *
     * @param startTab the start tab to set
     */
    public void setStartTab(GalleryTabId startTab) {

        m_startTab = startTab;
    }

    /**
     * Returns the start tab.<p>
     *
     * @return the startTab
     */
    public GalleryTabId getStartTab() {

        return m_startTab;
    }
}