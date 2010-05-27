/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/preview/Attic/I_CmsResourcePreview.java,v $
 * Date   : $Date: 2010/05/27 10:28:29 $
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

package org.opencms.ade.galleries.client.preview;


/**
 * Interface for resource preview within the galleries dialog.<p>
 * 
 * @author Tobias Herrmann
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsResourcePreview {

    /** The open preview function key. */
    String KEY_OPEN_PREVIEW_FUNCTION = "openPreview";

    /** The preview provider list key. */
    String KEY_PREVIEW_PROVIDER_LIST = "__CMS_PREVIEW_PROVIDER_LIST";

    /** The select resource function key. */
    String KEY_SELECT_RESOURCE_FUNCTION = "selectResource";

    /**
     * Returns the preview name, should return the same as in {@link org.opencms.ade.galleries.I_CmsPreviewProvider#getPreviewName()}.<p>
     * 
     * @return the preview name
     */
    String getPreviewName();

    /**
     * Opens the preview for the given resource in the given gallery mode.<p>
     * 
     * @param galleryMode the gallery mode
     * @param resourcePath the resource path
     * @param parentElementId the dom element id to insert the preview into
     */
    void openPreview(String galleryMode, String resourcePath, String parentElementId);

    /**
     * Sets the selected resource in the opening editor for the given gallery mode.<p>
     * 
     * @param galleryMode the gallery mode
     * @param resourcePath the resource path
     */
    void selectResource(String galleryMode, String resourcePath);
}
