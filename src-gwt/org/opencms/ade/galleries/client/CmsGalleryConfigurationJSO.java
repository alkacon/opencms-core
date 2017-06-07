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

package org.opencms.ade.galleries.client;

import org.opencms.ade.galleries.shared.CmsGalleryTabConfiguration;
import org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryTabId;
import org.opencms.gwt.client.util.CmsDomUtil;
import org.opencms.util.CmsStringUtil;

import java.util.Arrays;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Gallery configuration java-script overlay object.<p>
 */
public final class CmsGalleryConfigurationJSO extends JavaScriptObject implements I_CmsGalleryConfiguration {

    /**
     * Hiding constructor.<p>
     */
    protected CmsGalleryConfigurationJSO() {

        // nothing to do
    }

    /**
     * Parses the given JSON configuration string.<p>
     *
     * @param conf the JSON configuration string
     *
     * @return the gallery configuration object
     */
    public static CmsGalleryConfigurationJSO parseConfiguration(String conf) {

        return (CmsGalleryConfigurationJSO)CmsDomUtil.parseJSON(conf);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getCurrentElement()
     */
    public String getCurrentElement() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_CURRENT_ELEMENT);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getGalleryMode()
     */
    public GalleryMode getGalleryMode() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(internalGetGalleryMode())) {
            return Enum.valueOf(GalleryMode.class, internalGetGalleryMode());
        }
        return null;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getGalleryPath()
     */
    public String getGalleryPath() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_GALLERY_PATH);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getGalleryStoragePrefix()
     */
    public String getGalleryStoragePrefix() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_GALLERY_STORAGE_PREFIX);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getGalleryTypeName()
     */
    public String getGalleryTypeName() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_GALLERY_NAME);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getGalleryTypes()
     */
    public String[] getGalleryTypes() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(internalGetGalleryTypes())) {
            return internalGetGalleryTypes().split(",");
        }
        return null;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getImageFormatNames()
     */
    public native String getImageFormatNames()/*-{
                                              if (typeof this[@org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants::CONFIG_IMAGE_FORMAT_NAMES] != 'undefined') {
                                              return this[@org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants::CONFIG_IMAGE_FORMAT_NAMES]
                                              .toString();
                                              }
                                              return null;
                                              }-*/;

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getImageFormats()
     */
    public native String getImageFormats()/*-{
                                          if (typeof this[@org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants::CONFIG_IMAGE_FORMATS] != 'undefined') {
                                          return this[@org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants::CONFIG_IMAGE_FORMATS]
                                          .toString();
                                          }
                                          return null;
                                          }-*/;

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getLocale()
     */
    public String getLocale() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_LOCALE);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getReferencePath()
     */
    public String getReferencePath() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_REFERENCE_PATH);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getResourceTypes()
     */
    public List<String> getResourceTypes() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(internalGetResourceTypes())) {
            return Arrays.asList(internalGetResourceTypes().split(","));
        }
        return null;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getSearchTypes()
     */
    public List<String> getSearchTypes() {

        if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(internalGetSearchTypes())) {
            return Arrays.asList(internalGetSearchTypes().split(","));
        }
        return null;
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getStartFolder()
     */
    public String getStartFolder() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_START_FOLDER);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getStartSite()
     */
    public native String getStartSite()/*-{
                                       if (typeof this[@org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants::CONFIG_START_SITE] != 'undefined') {
                                       return this[@org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants::CONFIG_START_SITE];
                                       }
                                       return null;
                                       }-*/;

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getTabConfiguration()
     */
    public CmsGalleryTabConfiguration getTabConfiguration() {

        return CmsGalleryTabConfiguration.resolve(getTabConfigString());

    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getTreeToken()
     */
    public String getTreeToken() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_TREE_TOKEN);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#getUploadFolder()
     */
    public String getUploadFolder() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_UPLOAD_FOLDER);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#isGalleriesSelectable()
     */
    public boolean isGalleriesSelectable() {

        return Boolean.valueOf(getString(I_CmsGalleryProviderConstants.CONFIG_GALLERIES_SELECTABLE)).booleanValue();
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#isResultsSelectable()
     */
    public boolean isResultsSelectable() {

        String resultsSelectable = getString(I_CmsGalleryProviderConstants.CONFIG_RESULTS_SELECTABLE);
        if (resultsSelectable == null) {
            return true;
        }
        return Boolean.valueOf(resultsSelectable).booleanValue();
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#isIncludeFiles()
     */
    public boolean isIncludeFiles() {

        return getTabConfiguration().getTabs().contains(GalleryTabId.cms_tab_results);
    }

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#isShowSiteSelector()
     */
    public native boolean isShowSiteSelector()/*-{
                                              // defaults to true
                                              return 'false' != ''
                                              + this[@org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants::CONFIG_SHOW_SITE_SELECTOR]
                                              }-*/;

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#isUseFormats()
     */
    public native boolean isUseFormats()/*-{
                                        // defaults to false
                                        return 'true' == ''
                                        + this[@org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants::CONFIG_USE_FORMATS]
                                        }-*/;

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#setCurrentElement(java.lang.String)
     */
    public native void setCurrentElement(String currentElement)/*-{
                                                               this[@org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants::CONFIG_CURRENT_ELEMENT] = currentElement;
                                                               }-*/;

    /**
     * @see org.opencms.ade.galleries.shared.I_CmsGalleryConfiguration#setStartFolder(java.lang.String)
     */
    public native void setStartFolder(String startFolder)/*-{

                                                         this[@org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants::CONFIG_START_FOLDER]=startFolder;
                                                         }-*/;

    /**
     * Gets a string-valued attribute.<p>
     *
     * @param key the name of the attribute
     *
     * @return the value of the attribute
     */
    private native String getString(String key) /*-{
                                                if (typeof this[key] != 'undefined') {
                                                return this[key];
                                                }
                                                return null;
                                                }-*/;

    /**
     * Gets the tab configuration string.<p>
     *
     * @return the tab configuration string
     */
    private String getTabConfigString() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_TAB_CONFIG);
    }

    /**
     * Returns the gallery mode name.<p>
     *
     * @return the gallery mode name
     */
    private String internalGetGalleryMode() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_GALLERY_MODE);
    }

    /**
     * Returns the gallery types in a comma separated list.<p>
     *
     * @return the gallery types in a comma separated list
     */
    private String internalGetGalleryTypes() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_GALLERY_TYPES);
    }

    /**
     * Returns the resource types in a comma separated list.<p>
     *
     * @return the resource types in a comma separated list
     */
    private String internalGetResourceTypes() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_RESOURCE_TYPES);
    }

    /**
     * Returns the search types in a comma separated list.<p>
     *
     * @return the search types in a comma separated list
     */
    private String internalGetSearchTypes() {

        return getString(I_CmsGalleryProviderConstants.CONFIG_SEARCH_TYPES);
    }

}
