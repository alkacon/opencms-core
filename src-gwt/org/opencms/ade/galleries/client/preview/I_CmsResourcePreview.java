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

package org.opencms.ade.galleries.client.preview;

import org.opencms.ade.galleries.client.preview.ui.A_CmsPreviewDialog;
import org.opencms.ade.galleries.client.ui.CmsGalleryDialog;
import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;
import org.opencms.util.CmsUUID;

import java.util.Map;

import com.google.gwt.user.client.Command;

/**
 * Interface for resource preview within the galleries dialog.<p>
 *
 * @param <T> the resource info bean type
 *
 * @since 8.0.0
 */
public interface I_CmsResourcePreview<T extends CmsResourceInfoBean> {

    /**
     * Gets the gallery dialog in which this preview is displayed.<p>
     *
     * @return the gallery dialog
     */
    CmsGalleryDialog getGalleryDialog();

    /**
     * Returns the gallery mode.<p>
     *
     * @return the gallery mode
     */
    GalleryMode getGalleryMode();

    /**
     * Returns the preview handler.<p>
     *
     * @return the preview handler
     */
    I_CmsPreviewHandler<T> getHandler();

    /**
     * Returns the current locale.<p>
     *
     * @return the current locale
     */
    String getLocale();

    /**
     * Returns the preview dialog widget.<p>
     *
     * @return the preview dialog widget
     */
    A_CmsPreviewDialog<T> getPreviewDialog();

    /**
     * Returns the preview name, should return the same as in {@link org.opencms.ade.galleries.preview.I_CmsPreviewProvider#getPreviewName()}.<p>
     *
     * @return the preview name
     */
    String getPreviewName();

    /**
     * Returns the resource path of the current resource.<p>
     *
     * @return the resource path
     */
    String getResourcePath();

    /**
     * Returns the site independent perma link.<p>
     *
     * @return the site independent link
     */
    String getViewLink();

    /**
     * Loads the resource info and displays the retrieved data.<p>
     *
     * @param resourcePath the resource path
     */
    void loadResourceInfo(String resourcePath);

    /**
     * Opens the preview for the given resource in the given gallery mode.<p>
     *
     * @param resourcePath the resource path
     * @param disableSelection if the selection from the preview should be disabled
     */
    void openPreview(String resourcePath, boolean disableSelection);

    /**
     * Removes the preview widget.<p>
     */
    void removePreview();

    /**
     * Saves the changed properties.<p>
     *
     * @param properties the changed properties
     * @param afterSaveCommand the command to execute after saving the properties
     */
    void saveProperties(Map<String, String> properties, Command afterSaveCommand);

    /**
     * Sets the selected resource in the opening editor for the given gallery mode.<p>
     *
     * @param resourcePath the resource path
     * @param structureId the structure id
     * @param title the resource title
     */
    void selectResource(String resourcePath, CmsUUID structureId, String title);

    /**
     * Checks if further user input is required and other wise sets the selected resource
     * via the provided integrator functions <code>setLink</code> and <code>setImage</code>.<p>
     * Returning <code>true</code> when all data has been.
     * If there are any changes, the user will be requested to save those and the editor OK function will be called again.<p>
     *
     * @return <code>true</code> when all data has been set, <code>false</code> if there were any changes that need saving
     */
    boolean setDataInEditor();

    /**
     * Sets the current resource within the editor or xml-content.<p>
     */
    void setResource();

    /**
     * Calls the preview handler to display the given data.<p>
     *
     * @param resourceInfo the resource info data
     */
    void showData(T resourceInfo);
}
