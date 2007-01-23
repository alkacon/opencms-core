/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/webdav/Attic/I_CmsWebdavItem.java,v $
 * Date   : $Date: 2007/01/23 16:58:11 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.webdav;

import java.io.InputStream;

/**
 * This class represents items for the WebDAV interface. This can be
 * files or folders (collections). 
 * 
 * @author Peter Bonrad
 */
public interface I_CmsWebdavItem {

    /**
     * Returns the content of this item as a byte array.
     * 
     * @return the content of this item as a byte array
     */
    byte[] getContent();

    /**
     * Returns the length of the content of this item.
     * 
     * @return the content length of this item as long
     */
    long getContentLength();

    /**
     * Returns the date of the creation of this item.
     * 
     * @return the creation date if this item as long.
     */
    long getCreationDate();

    /**
     * Returns the date of the last modification of this item.
     * 
     * @return the last modification date of the item as long
     */
    long getLastModifiedDate();

    /**
     * Returns the mime type of this item.
     * 
     * @return the mime type of this item
     */
    String getMimeType();

    /**
     * Returns the name of this item.
     * 
     * @return the name of this item
     */
    String getName();

    /**
     * Returns the content if this item as a stream.
     * 
     * @return the stream content of this item as a InputStream
     */
    InputStream getStreamContent();

    /**
     * Checks if this item is a collection.
     * 
     * @return true if this item is a collection otherwise false
     */
    boolean isCollection();

}
