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

import java.util.List;

/**
 * The gallery configuration interface.<p>
 */
public interface I_CmsGalleryConfiguration {

    /**
     * Returns the currently selected element.<p>
     *
     * @return the currently selected element
     */
    String getCurrentElement();

    /**
     * Returns the gallery mode.<p>
     *
     * @return the gallery mode
     */
    GalleryMode getGalleryMode();

    /**
     * Returns the start gallery path.<p>
     *
     * @return the start gallery path
     */
    String getGalleryPath();

    /**
     * Gets the prefix for the key used to store the last selected gallery.<p>
     *
     * @return the prefix for the key used to store the last selected gallery
     */
    String getGalleryStoragePrefix();

    /**
     * Returns the gallery type name.<p>
     *
     * @return the gallery type name
     */
    String getGalleryTypeName();

    /**
     * Returns the available gallery types.<p>
     *
     * @return the available gallery types
     */
    String[] getGalleryTypes();

    /**
     * Returns the image format names.<p>
     *
     * @return the image format names
     */
    String getImageFormatNames();

    /**
     * Returns the image formats.<p>
     *
     * @return the image formats
     */
    String getImageFormats();

    /**
     * Returns the content locale.<p>
     *
     * @return the content locale
     */
    String getLocale();

    /**
     * Returns the path of the edited resource.<p>
     *
     * @return the path of the edited resource
     */
    String getReferencePath();

    /**
     * Returns the available resource types.<p>
     *
     * @return the available resource types
     */
    List<String> getResourceTypes();

    /**
     * Returns the searchable types.<p>
     *
     * @return the searchable types
     */
    List<String> getSearchTypes();

    /**
     * Returns the start folder.<p>
     *
     * @return the start folder
     */
    String getStartFolder();

    /**
     * Returns the start site.<p>
     *
     * @return the start site
     */
    String getStartSite();

    /**
     * Gets the tab configuration.<p>
     *
     * @return the gallery tab configuration
     */
    CmsGalleryTabConfiguration getTabConfiguration();

    /**
     * Gets the tree token.<p>
     *
     * The tree token is used to save/load tree opening states for tree tabs in the gallery
     * dialog. If two widget instances use different tree tokens, opening or closing tree entries
     * in one will not effect the tree opening state of the other.
     *
     * @return the tree token
     */
    String getTreeToken();

    /**
     * Returns the upload folder.<p>
     *
     * @return the upload folder
     */
    String getUploadFolder();

    /**
     * Returns true if the galleries should be selectable.<p>
     *
     * @return true if the galleries should be selectable
     */
    boolean isGalleriesSelectable();

    /**
     * Returns false if the results should not be selectable.<p>
     *
     * @return false if the results should not be selectable
     */
    boolean isResultsSelectable();

    /**
     * Returns if files are selectable.<p>
     *
     * @return <code>true</code> if files are selectable
     */
    boolean isIncludeFiles();

    /**
     * Returns if the site selector should be shown.<p>
     *
     * @return <code>true</code> if the site selector should be shown
     */
    boolean isShowSiteSelector();

    /**
     * Returns if image formats should be used in preview.<p>
     *
     * @return <code>true</code> if image format should be used in preview
     */
    boolean isUseFormats();

    /**
     * Sets the currentElement.<p>
     *
     * @param currentElement the currentElement to set
     */
    void setCurrentElement(String currentElement);

    /**
     * Sets the start folder.<p>
     *
     * @param startFolder the start folder
     */
    void setStartFolder(String startFolder);
}
