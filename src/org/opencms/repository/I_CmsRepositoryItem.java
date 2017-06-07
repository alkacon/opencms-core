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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.repository;

/**
 * This class represents items in the repository interface. That can be
 * files or folders (collections). <p>
 *
 * @since 6.2.4
 */
public interface I_CmsRepositoryItem {

    /**
     * Returns the content of this item as a byte array.<p>
     *
     * @return the content of this item as a byte array
     */
    byte[] getContent();

    /**
     * Returns the length of the content of this item.<p>
     *
     * @return the content length of this item as long
     */
    long getContentLength();

    /**
     * Returns the date of the creation of this item.<p>
     *
     * @return the creation date if this item as long.
     */
    long getCreationDate();

    /**
     * Returns the date of the last modification of this item.<p>
     *
     * @return the last modification date of the item as long
     */
    long getLastModifiedDate();

    /**
     * Returns the mime type of this item.<p>
     *
     * @return the mime type of this item
     */
    String getMimeType();

    /**
     * Returns the name of this item.<p>
     *
     * @return the name of this item
     */
    String getName();

    /**
     * Checks if this item is a collection.<p>
     *
     * @return true if this item is a collection otherwise false
     */
    boolean isCollection();

}
