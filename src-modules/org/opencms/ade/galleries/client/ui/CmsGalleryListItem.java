/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/client/ui/Attic/CmsGalleryListItem.java,v $
 * Date   : $Date: 2010/04/28 10:25:47 $
 * Version: $Revision: 1.2 $
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

package org.opencms.ade.galleries.client.ui;

import org.opencms.ade.galleries.shared.I_CmsItemId;
import org.opencms.gwt.client.ui.CmsSimpleListItem;

import com.google.gwt.user.client.ui.Widget;

/**
 * Provides the specific list item for the galleries list.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.
 */
public class CmsGalleryListItem extends CmsSimpleListItem implements I_CmsItemId {

    /** The gallery path to identify the specific gallery. */
    private String m_galleryPath;

    /**
     * The list item constructor.
     *  
     * @param content the content of the category list item
     */
    public CmsGalleryListItem(Widget... content) {

        super(content);
    }

    /**
     * Returns the gallery path as the unique id for the gallery.<p>
     * 
     * @see org.opencms.ade.galleries.shared.I_CmsItemId#getId()
     */
    public String getId() {

        return m_galleryPath;
    }

    /**
     * Sets the gallery path as a unique id for this gallery.<p>
     * 
     * @see org.opencms.ade.galleries.shared.I_CmsItemId#setId(java.lang.String)
     */
    public void setId(String id) {

        m_galleryPath = id;
    }
}