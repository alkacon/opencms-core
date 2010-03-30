/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsGalleryDialogBean.java,v $
 * Date   : $Date: 2010/03/30 14:08:36 $
 * Version: $Revision: 1.2 $
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This bean holding the gallery dialog information required for displaying the content of the gallery tabs.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public class CmsGalleryDialogBean implements IsSerializable {

    /** The categories to display in the list of available categories. */
    private LinkedHashMap<String, CmsCategoriesListInfoBean> m_categories;

    /** The galleries to display in the list with available galleries. */
    private LinkedHashMap<String, CmsGalleriesListInfoBean> m_galleries;

    /** The available workplace locales. */
    private TreeMap<String, String> m_locales;

    /** The configured tabs for this gallery. */
    private ArrayList<String> m_tabs;

    /** The types to display in the list of available categories. */
    private LinkedHashMap<String, CmsTypesListInfoBean> m_types;

    //TODO: add sitemap data, add vfs tree data, add container page data, resource locales if required

    /**
     * Returns the categories map.<p>
     *
     * @return the categories
     */
    public LinkedHashMap<String, CmsCategoriesListInfoBean> getCategories() {

        return m_categories;
    }

    /**
     * Returns the galleries map.<p>
     *
     * @return the galleries
     */
    public LinkedHashMap<String, CmsGalleriesListInfoBean> getGalleries() {

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
    public LinkedHashMap<String, CmsTypesListInfoBean> getTypes() {

        return m_types;
    }

    /**
     * Sets the categories map.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(LinkedHashMap<String, CmsCategoriesListInfoBean> categories) {

        m_categories = categories;
    }

    /**
     * Sets the galleries map.<p>
     *
     * @param galleries the galleries to set
     */
    public void setGalleries(LinkedHashMap<String, CmsGalleriesListInfoBean> galleries) {

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
    public void setTypes(LinkedHashMap<String, CmsTypesListInfoBean> types) {

        m_types = types;
    }
}