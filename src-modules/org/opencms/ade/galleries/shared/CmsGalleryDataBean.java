/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsGalleryDataBean.java,v $
 * Date   : $Date: 2010/05/19 09:02:51 $
 * Version: $Revision: 1.3 $
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
import org.opencms.gwt.shared.CmsCategoryTreeEntry;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This bean holding the gallery dialog information required for displaying the content of the gallery tabs.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 8.0.0
 */
//TODO: rename in CmsGalleryDialogData
public class CmsGalleryDataBean implements IsSerializable {

    /** Name of the used JS variable. */
    public static final String DICT_NAME = "cms_gallery_data_bean";

    //TODO: add sitemap data, add vfs tree data, add container page data, resource locales if required

    /** The category tree entry to display as tree. */
    private CmsCategoryTreeEntry m_categoryTreeEntry;

    /** The galleries to display in the list with available galleries. */
    private List<CmsGalleryFolderBean> m_galleries;

    /** The available workplace locales. */
    private Map<String, String> m_locales;

    /** The gallery mode. */
    private GalleryMode m_mode;

    /** The start up tab id. */
    private GalleryTabId m_startTab;

    /** The types to display in the list of available categories. */
    private List<CmsResourceTypeBean> m_types;

    /**
     * Returns the categories.<p>
     *
     * @return the categories
     */
    public CmsCategoryTreeEntry getCategories() {

        return m_categoryTreeEntry;
    }

    /**
     * Returns the galleries map.<p>
     *
     * @return the galleries
     */
    public List<CmsGalleryFolderBean> getGalleries() {

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
     * Returns the gallery mode.<p>
     *
     * @return the gallery mode
     */
    public GalleryMode getMode() {

        return m_mode;
    }

    /**
     * Returns the start tab.<p>
     *
     * @return the startTab
     */
    public GalleryTabId getStartTab() {

        return m_startTab;
    }

    /**
     * Returns the types map.<p>
     *
     * @return the types
     */
    public List<CmsResourceTypeBean> getTypes() {

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
     * Sets the galleries map.<p>
     *
     * @param galleries the galleries to set
     */
    public void setGalleries(List<CmsGalleryFolderBean> galleries) {

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
     * Sets the gallery mode.<p>
     *
     * @param mode the gallery mode to set
     */
    public void setMode(GalleryMode mode) {

        m_mode = mode;
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
     * Sets the types map.<p>
     *
     * @param types the types to set
     */
    public void setTypes(List<CmsResourceTypeBean> types) {

        m_types = types;
    }
}