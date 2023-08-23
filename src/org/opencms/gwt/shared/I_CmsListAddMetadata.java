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

package org.opencms.gwt.shared;

import java.util.List;

/**
 * AutoBean interface for the data injected into the page by the cms:enable-list-add tag.
 */
public interface I_CmsListAddMetadata {

    /**
     * Gets the post create handler.
     *
     * @return the post create handler
     */
    String getPostCreateHandler();

    /**
     * Gets the types that should be creatable.
     *
     * @return the types
     */
    List<String> getTypes();

    /**
     * Gets the upload folder.
     *
     * @return the upload folder
     */
    String getUploadFolder();

    /**
     * Sets the post create handler.
     *
     * @param postCreateHandler the new post create handler
     */
    void setPostCreateHandler(String postCreateHandler);

    /**
     * Sets the types that should be creatable.
     *
     * @param types the new types
     */
    void setTypes(List<String> types);

    /**
     * Sets the upload folder.
     *
     * @param uploadFolder the upload folder
     */
    void setUploadFolder(String uploadFolder);

}
