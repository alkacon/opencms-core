/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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
 * @since 8.0.0
 */
public class CmsGalleryDataBean implements IsSerializable {

    /** Name of the used JS variable. */
    public static final String DICT_NAME = "cms_gallery_data_bean";

    /** The category tree entry to display as tree. */
    private List<CmsCategoryTreeEntry> m_categoryTreeEntry;

    /** The current element. */
    private String m_currentElement;

    /** The galleries to display in the list with available galleries. */
    private List<CmsGalleryFolderBean> m_galleries;

    /** The content locale. */
    private String m_locale;

    /** The available workplace locales. */
    private Map<String, String> m_locales;

    /** The gallery mode. */
    private GalleryMode m_mode;

    /** 
     * The gallery reference site-path. 
     * In widget, editor and containerpage mode this will be the edited resource, otherwise the opened gallery folder.
     */
    private String m_referenceSitePath;

    /** The start gallery folder. */
    private String m_startGallery;

    /** The start up tab id. */
    private GalleryTabId m_startTab;

    /** The types to display in the list of available categories. */
    private List<CmsResourceTypeBean> m_types;

    /** A list of beans representing the root folders to display in the VFS tab. */
    private List<CmsVfsEntryBean> m_vfsRootFolders;

    /** The search scope. */
    private CmsGallerySearchScope m_scope;

    /**
     * Returns the categories.<p>
     *
     * @return the categories
     */
    public List<CmsCategoryTreeEntry> getCategories() {

        return m_categoryTreeEntry;
    }

    /**
     * Returns the current element.<p>
     *
     * @return the current element
     */
    public String getCurrentElement() {

        return m_currentElement;
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
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return m_locale;
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
     * Returns the gallery reference site-path.<p>
     * In widget, editor and containerpage mode this will be the edited resource, otherwise the opened gallery folder.<p>
     *
     * @return the gallery reference site-path
     */
    public String getReferenceSitePath() {

        return m_referenceSitePath;
    }

    /**
     * Returns the search scope.<p>
     * 
     * @return the search scope
     */
    public CmsGallerySearchScope getScope() {

        return m_scope;
    }

    /**
     * Returns the start gallery folder.<p>
     *
     * @return the start gallery folder
     */
    public String getStartGallery() {

        return m_startGallery;
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
     * Returns the list of root folders to display in the VFS tab.<p>
     *
     * @return the list root folders to display in the VFS tab
     */
    public List<CmsVfsEntryBean> getVfsRootFolders() {

        return m_vfsRootFolders;
    }

    /**
     * Sets the categories.<p>
     *
     * @param categories the categories to set
     */
    public void setCategories(List<CmsCategoryTreeEntry> categories) {

        m_categoryTreeEntry = categories;
    }

    /**
     * Sets the current element.<p>
     *
     * @param currentElement the current element to set
     */
    public void setCurrentElement(String currentElement) {

        m_currentElement = currentElement;
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
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(String locale) {

        m_locale = locale;
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
     * Sets the gallery reference site-path.<p>
     * In widget, editor and containerpage mode this will be the edited resource, otherwise the opened gallery folder.<p>
     *
     * @param referenceSitePath the gallery reference site-path to set
     */
    public void setReferenceSitePath(String referenceSitePath) {

        this.m_referenceSitePath = referenceSitePath;
    }

    /**
     * Sets the search scope.<p>
     * 
     * @param scope the search scope
     */
    public void setScope(CmsGallerySearchScope scope) {

        m_scope = scope;
    }

    /**
     * Sets the start gallery folder.<p>
     *
     * @param startGallery the start gallery folder to set
     */
    public void setStartGallery(String startGallery) {

        m_startGallery = startGallery;
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

    /**
     * Sets the root folders to be displayed in the VFS folder tab.<p>
     * 
     * @param rootFolders beans representing the root folders 
     */
    public void setVfsRootFolders(List<CmsVfsEntryBean> rootFolders) {

        m_vfsRootFolders = rootFolders;
    }
}