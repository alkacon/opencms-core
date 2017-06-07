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

import org.opencms.ade.galleries.shared.CmsResourceInfoBean;
import org.opencms.ade.galleries.shared.I_CmsGalleryProviderConstants.GalleryMode;

import java.util.Map;

/**
 * Interface for the preview controller.<p>
 *
 * @param <T> the resource info bean type
 *
 * @since 8.0.0
 */
public interface I_CmsPreviewController<T extends CmsResourceInfoBean> {

    /**
     * Checks if further user input is required and other wise sets the selected resource via the provided integrator functions <code>setLink</code> and <code>setImage</code>.
     * Returning <code>true</code> when all data has been set and the dialog should be closed.
     *
     * @return <code>true</code> when all data has been set and the dialog should be closed
     */
    boolean closeGalleryDialog();

    /**
     * Loads the resource info and displays the retrieved data.<p>
     *
     * @param resourcePath the resource path
     */
    void loadResourceInfo(String resourcePath);

    /**
     * Removes the preview.<p>
     */
    void removePreview();

    /**
     * Saves the changed properties.<p>
     *
     * @param properties the changed properties
     */
    void saveProperties(Map<String, String> properties);

    /**
     * Sets the current resource within the editor or xml-content.<p>
     *
     * @param galleryMode the gallery mode
     */
    void setResource(GalleryMode galleryMode);

    /**
     * Calls the preview handler to display the given data.<p>
     *
     * @param resourceInfo the resource info data
     */
    void showData(T resourceInfo);
}