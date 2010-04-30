/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsGalleryDialogBean.java,v $
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

package org.opencms.ade.galleries.shared;

import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.SortParams;
import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This bean holding the gallery dialog information required for displaying the content of the gallery tabs.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 8.0.0
 */
//TODO: rename in CmsGalleryDialogData
public class CmsGalleryDialogBean implements IsSerializable {

    /**
     * Provides ascending sorting according to the object id.<p>
     * 
     * Applicable for all CmsListInfoBeans implementing I_CmsItemId.
     */
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
    //TODO: remove replaced by list   private LinkedHashMap<String, CmsCategoriesListInfoBean> m_categories;
    private ArrayList<CmsCategoriesListInfoBean> m_categories;

    /** The galleries to display in the list with available galleries. */
    //TODO: remove replaced by private LinkedHashMap<String, CmsGalleriesListInfoBean> m_galleries; 
    private ArrayList<CmsGalleriesListInfoBean> m_galleries;

    /** The available workplace locales. */
    private TreeMap<String, String> m_locales;

    /** The configured tabs for this gallery. */
    private ArrayList<String> m_tabs;

    /** The types to display in the list of available categories. */
    //TODO: remove replaced by private private LinkedHashMap<String, CmsTypesListInfoBean> m_types;
    private ArrayList<CmsTypesListInfoBean> m_types;

    /**
     * Returns the categories map.<p>
     *
     * @return the categories
     */
    public ArrayList<CmsCategoriesListInfoBean> getCategories() {

        return m_categories;
    }

    /**
     * Returns the galleries map.<p>
     *
     * @return the galleries
     */
    public ArrayList<CmsGalleriesListInfoBean> getGalleries() {

        return m_galleries;
    }

    /**
     * Returns the locales map.<p>
     *
     * @return the locales
     */
    public TreeMap<String, String> getLocales() {

        return m_locales;
    }

    /**
     * Returns the tabs arrays.<p>
     *
     * @return the tabs
     */
    public ArrayList<String> getTabs() {

        return m_tabs;
    }

    /**
     * Returns the types map.<p>
     *
     * @return the types
     */
    public ArrayList<CmsTypesListInfoBean> getTypes() {

        return m_types;
    }

    /**
     * Sets the categories map.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(ArrayList<CmsCategoriesListInfoBean> categories) {

        m_categories = categories;
    }

    /**
     * Sets the galleries map.<p>
     *
     * @param galleries the galleries to set
     */
    public void setGalleries(ArrayList<CmsGalleriesListInfoBean> galleries) {

        m_galleries = galleries;
    }

    /**
     * Sets the locales map.<p>
     *
     * @param locales the locales to set
     */
    public void setLocales(TreeMap<String, String> locales) {

        m_locales = locales;
    }

    /**
     * Sets the tabs array.<p>
     *
     * @param tabs the tabs to set
     */
    public void setTabs(ArrayList<String> tabs) {

        m_tabs = tabs;
    }

    /**
     * Sets the types map.<p>
     *
     * @param types the types to set
     */
    public void setTypes(ArrayList<CmsTypesListInfoBean> types) {

        m_types = types;
    }

    /**
     * Sorts the gallery list.<p>
     * 
     * @param sortParams the sort parameters
     */
    public void sortGalleries(String sortParams) {

        if (SortParams.title_asc.name().equals(sortParams)) {
            Collections.sort(m_galleries);
        } else if (SortParams.title_desc.name().equals(sortParams)) {
            Collections.sort(m_galleries, new CmsSortTitleDesc());
        } else if (SortParams.type_asc.name().equals(sortParams)) {
            Collections.sort(m_galleries, new CmsSortTypeAsc());
        } else if (SortParams.type_desc.name().equals(sortParams)) {
            Collections.sort(m_galleries, new CmsSortTypeDesc());
        } else if (SortParams.path_asc.name().equals(sortParams)) {
            Collections.sort(m_galleries, new CmsSortIdAsc());
        } else if (SortParams.path_desc.name().equals(sortParams)) {
            Collections.sort(m_galleries, new CmsSortIdDesc());
        }
    }

    /**
     * Sorts the types list.<p>
     * 
     * @param sortParams the sort parameters
     */
    public void sortTypes(String sortParams) {

        if (SortParams.title_asc.name().equals(sortParams)) {
            Collections.sort(m_types);
        } else if (SortParams.title_desc.name().equals(sortParams)) {
            Collections.sort(m_types, new CmsSortTitleDesc());
        }
    }
}