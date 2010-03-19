/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsGalleryDialogBean.java,v $
 * Date   : $Date: 2010/03/19 10:11:54 $
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

import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This bean contains the gallery dialog information required for displaying the content of the gallery tabs.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $ 
 * 
 * @since 8.0.0
 */
public class CmsGalleryDialogBean implements IsSerializable {

    /** The categories to display in the list of available categories. */
    private List<CmsListInfoBean> m_categories;

    /** The dialogmode of the current gallery (view, widget, editor, properties, ade).*/
    private String m_dialogMode;

    /** The galleries to display in the list with available galleries. */
    private List<CmsListInfoBean> m_galleries;

    /** The available workplace locales. */
    private TreeMap<String, String> m_locales;

    /** The configured tabs for this gallery. */
    private ArrayList<String> m_tabs;

    /** The types to display in the list of available categories. */
    private List<CmsListInfoBean> m_typeIds;

    /** The types to display in the list of available categories. */
    private List<CmsListInfoBean> m_types;

    //TODO: add sitemap data
    //TODO: add vfs tree data
    //TODO: add container page data
    // TODO: do we need locale for resource here?

    /**
     * Returns the categories.<p>
     *
     * @return the categories
     */
    public List<CmsListInfoBean> getCategories() {

        return m_categories;
    }

    /**
     * Returns the dialogMode.<p>
     *
     * @return the dialogMode
     */
    public String getDialogMode() {

        return m_dialogMode;
    }

    /**
     * Returns the galleries.<p>
     *
     * @return the galleries
     */
    public List<CmsListInfoBean> getGalleries() {

        return m_galleries;
    }

    /**
     * Returns the locales.<p>
     *
     * @return the locales
     */
    public TreeMap<String, String> getLocales() {

        return m_locales;
    }

    /**
     * Returns the tabs.<p>
     *
     * @return the tabs
     */
    public ArrayList<String> getTabs() {

        return m_tabs;
    }

    /**
     * Returns the typeIds.<p>
     *
     * @return the typeIds
     */
    public List<CmsListInfoBean> getTypeIds() {

        return m_typeIds;
    }

    /**
     * Returns the types.<p>
     *
     * @return the types
     */
    public List<CmsListInfoBean> getTypes() {

        return m_types;
    }

    /**
     * Sets the categories.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(List<CmsListInfoBean> categories) {

        m_categories = categories;
    }

    /**
     * Sets the dialogMode.<p>
     *
     * @param dialogMode the dialogMode to set
     */
    public void setDialogMode(String dialogMode) {

        m_dialogMode = dialogMode;
    }

    /**
     * Sets the galleries.<p>
     *
     * @param galleries the galleries to set
     */
    public void setGalleries(List<CmsListInfoBean> galleries) {

        m_galleries = galleries;
    }

    /**
     * Sets the locales.<p>
     *
     * @param locales the locales to set
     */
    public void setLocales(TreeMap<String, String> locales) {

        m_locales = locales;
    }

    /**
     * Sets the tabs.<p>
     *
     * @param tabs the tabs to set
     */
    public void setTabs(ArrayList<String> tabs) {

        m_tabs = tabs;
    }

    /**
     * Sets the typeIds.<p>
     *
     * @param typeIds the typeIds to set
     */
    public void setTypeIds(List<CmsListInfoBean> typeIds) {

        m_typeIds = typeIds;
    }

    /**
     * Sets the types.<p>
     *
     * @param types the types to set
     */
    public void setTypes(List<CmsListInfoBean> types) {

        m_types = types;
    }

}
