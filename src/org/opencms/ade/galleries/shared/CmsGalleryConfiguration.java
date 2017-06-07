/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * The gallery configuration.<p>
 */
public class CmsGalleryConfiguration implements I_CmsGalleryConfiguration, IsSerializable {

    /** The current element. */
    private String m_currentElement;

    /** Flag to enable selection of galleries in the gallery tab. */
    private boolean m_galleriesSelectable;

    /** The gallery mode. */
    private GalleryMode m_galleryMode;

    /** The gallery path. */
    private String m_galleryPath;

    /** The prefix for the key used to store the last selected gallery. */
    private String m_galleryStoragePrefix;

    /** The gallery type name. */
    private String m_galleryTypeName;

    /** The gallery types. */
    private String[] m_galleryTypes;

    /** The image format names. */
    private String m_imageFormatNames;

    /** The image formats. */
    private String m_imageFormats;

    /** The content locale. */
    private String m_locale;

    /** The reference path. */
    private String m_referencePath;

    /** The resource types. */
    private List<String> m_resourceTypes;

    /** Flag which disables selection of results when set to false. */
    private boolean m_resultsSelectable;

    /** The search types. */
    private List<String> m_searchTypes;

    /** The show site selector flag. */
    private boolean m_showSiteSelector;

    /** The start folder. */
    private String m_startFolder;

    /** The start site. */
    private String m_startSite;

    /** The tab configuration. */
    private CmsGalleryTabConfiguration m_tabConfiguration;

    /** The tree token. */
    private String m_treeToken;

    /** The upload folder. */
    private String m_uploadFolder;

    /** The use formats flag. */
    private boolean m_useFormats;

    /**
     * Constructor.<p>
     */
    public CmsGalleryConfiguration() {

        // set defaults
        m_showSiteSelector = true;
    }

    /**
     * Constructor.<p>
     *
     * @param configuration the initial configuration
     */
    public CmsGalleryConfiguration(I_CmsGalleryConfiguration configuration) {

        m_currentElement = configuration.getCurrentElement();
        m_galleryMode = configuration.getGalleryMode();
        m_galleryPath = configuration.getGalleryPath();
        m_galleryTypes = configuration.getGalleryTypes();
        m_imageFormatNames = configuration.getImageFormatNames();
        m_imageFormats = configuration.getImageFormats();
        m_locale = configuration.getLocale();
        m_referencePath = configuration.getReferencePath();
        m_resourceTypes = configuration.getResourceTypes();
        m_searchTypes = configuration.getSearchTypes();
        m_showSiteSelector = configuration.isShowSiteSelector();
        m_startFolder = configuration.getStartFolder();
        m_startSite = configuration.getStartSite();
        m_tabConfiguration = configuration.getTabConfiguration();
        m_useFormats = configuration.isUseFormats();
        m_treeToken = configuration.getTreeToken();
        m_galleryStoragePrefix = configuration.getGalleryStoragePrefix();
        m_galleriesSelectable = configuration.isGalleriesSelectable();
        m_resultsSelectable = configuration.isResultsSelectable();
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getCurrentElement()
     */
    public String getCurrentElement() {

        return m_currentElement;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getGalleryMode()
     */
    public GalleryMode getGalleryMode() {

        return m_galleryMode;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getGalleryPath()
     */
    public String getGalleryPath() {

        return m_galleryPath;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getGalleryStoragePrefix()
     */
    public String getGalleryStoragePrefix() {

        return m_galleryStoragePrefix;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getGalleryTypeName()
     */
    public String getGalleryTypeName() {

        return m_galleryTypeName;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getGalleryTypes()
     */
    public String[] getGalleryTypes() {

        return m_galleryTypes;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getImageFormatNames()
     */
    public String getImageFormatNames() {

        return m_imageFormatNames;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getImageFormats()
     */
    public String getImageFormats() {

        return m_imageFormats;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getLocale()
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getReferencePath()
     */
    public String getReferencePath() {

        return m_referencePath;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getResourceTypes()
     */
    public List<String> getResourceTypes() {

        return m_resourceTypes;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getSearchTypes()
     */
    public List<String> getSearchTypes() {

        return m_searchTypes;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getStartFolder()
     */
    public String getStartFolder() {

        return m_startFolder;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getStartSite()
     */
    public String getStartSite() {

        return m_startSite;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getTabConfiguration()
     */
    public CmsGalleryTabConfiguration getTabConfiguration() {

        return m_tabConfiguration;
    }

    /**
     * Gets the array of tab ids.<p>
     *
     * @return the tab ids
     */
    public GalleryTabId[] getTabIds() {

        if (m_tabConfiguration == null) {
            return null;
        }
        return m_tabConfiguration.getTabs().toArray(new GalleryTabId[] {});
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getTreeToken()
     */
    public String getTreeToken() {

        return m_treeToken;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getUploadFolder()
     */
    public String getUploadFolder() {

        return m_uploadFolder;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#isGalleriesSelectable()
     */
    public boolean isGalleriesSelectable() {

        return m_galleriesSelectable;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#isIncludeFiles()
     */
    public boolean isIncludeFiles() {

        return getTabConfiguration().getTabs().contains(GalleryTabId.cms_tab_results);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#isResultsSelectable()
     */
    public boolean isResultsSelectable() {

        return m_resultsSelectable;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#isShowSiteSelector()
     */
    public boolean isShowSiteSelector() {

        return m_showSiteSelector;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#isUseFormats()
     */
    public boolean isUseFormats() {

        return m_useFormats;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#setCurrentElement(java.lang.String)
     */
    public void setCurrentElement(String currentElement) {

        m_currentElement = currentElement;
    }

    /**
     * Enables or disables selection of galleries in the gallery tab.<p>
     *
     * @param galleriesSelectable true if the galleries should be made selectable
     */
    public void setGalleriesSelectable(boolean galleriesSelectable) {

        m_galleriesSelectable = galleriesSelectable;
    }

    /**
     * Sets the galleryMode.<p>
     *
     * @param galleryMode the galleryMode to set
     */
    public void setGalleryMode(GalleryMode galleryMode) {

        m_galleryMode = galleryMode;
    }

    /**
     * Sets the galleryPath.<p>
     *
     * @param galleryPath the galleryPath to set
     */
    public void setGalleryPath(String galleryPath) {

        m_galleryPath = galleryPath;
    }

    /**
     * Sets the prefix for the key used to store the last selected gallery.<p>
     *
     * @param prefix the new prefix
     */
    public void setGalleryStoragePrefix(String prefix) {

        m_galleryStoragePrefix = prefix;
    }

    /**
     * Sets the gallery type name.<p>
     *
     * @param galleryTypeName the gallery type name
     */
    public void setGalleryTypeName(String galleryTypeName) {

        m_galleryTypeName = galleryTypeName;
    }

    /**
     * Sets the galleryTypes.<p>
     *
     * @param galleryTypes the galleryTypes to set
     */
    public void setGalleryTypes(String... galleryTypes) {

        m_galleryTypes = galleryTypes;
    }

    /**
     * Sets the image format names.<p>
     *
     * @param imageFormatNames the imageFormatNames to set
     */
    public void setImageFormatNames(String imageFormatNames) {

        m_imageFormatNames = imageFormatNames;
    }

    /**
     * Sets the image formats.<p>
     *
     * @param imageFormats the image formats to set
     */
    public void setImageFormats(String imageFormats) {

        m_imageFormats = imageFormats;
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
     * Sets the referencePath.<p>
     *
     * @param referencePath the referencePath to set
     */
    public void setReferencePath(String referencePath) {

        m_referencePath = referencePath;
    }

    /**
     * Sets the resourceTypes.<p>
     *
     * @param resourceTypes the resourceTypes to set
     */
    public void setResourceTypes(List<String> resourceTypes) {

        m_resourceTypes = resourceTypes;
    }

    /**
     * Disables selection of results if the given parameter is 'false'.<p>
     *
     * @param resultsSelectable false if result selection should be disabled
     */
    public void setResultsSelectable(boolean resultsSelectable) {

        m_resultsSelectable = resultsSelectable;

    }

    /**
     * Sets the searchTypes.<p>
     *
     * @param searchTypes the searchTypes to set
     */
    public void setSearchTypes(List<String> searchTypes) {

        m_searchTypes = searchTypes;
    }

    /**
     * Sets the showSiteSelector.<p>
     *
     * @param showSiteSelector the showSiteSelector to set
     */
    public void setShowSiteSelector(boolean showSiteSelector) {

        m_showSiteSelector = showSiteSelector;
    }

    /**
     * Sets the start folder.<p>
     *
     * @param startFolder the start folder
     */
    public void setStartFolder(String startFolder) {

        m_startFolder = startFolder;
    }

    /**
     * Sets the startSite.<p>
     *
     * @param startSite the startSite to set
     */
    public void setStartSite(String startSite) {

        m_startSite = startSite;
    }

    /**
     * Sets the tab configuration.<p>
     *
     * @param tabConfig the gallery tab configuration
     */
    public void setTabConfiguration(CmsGalleryTabConfiguration tabConfig) {

        m_tabConfiguration = tabConfig;
    }

    /**
     * Sets the upload folder.<p>
     *
     * @param uploadFolder the upload folder
     */
    public void setUploadFolder(String uploadFolder) {

        m_uploadFolder = uploadFolder;
    }

    /**
     * Sets the use formats flag.<p>
     *
     * @param useFormats the use formats flag to set
     */
    public void setUseFormats(boolean useFormats) {

        m_useFormats = useFormats;
    }
}
