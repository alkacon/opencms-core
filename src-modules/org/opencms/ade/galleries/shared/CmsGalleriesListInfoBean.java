/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsGalleriesListInfoBean.java,v $
 * Date   : $Date: 2010/04/30 10:17:38 $
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

package org.opencms.ade.galleries.shared;

import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.Map;

//TODO: do we need here I_CmsItemId?? is the id in the LinkedHashMap enough?
/**
 * A specific bean holding all info to be displayed in {@link org.opencms.ade.galleries.client.ui.CmsGalleryListItem}s.<p>
 * 
 * @see org.opencms.ade.galleries.client.ui.CmsGalleryListItem
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public class CmsGalleriesListInfoBean extends CmsListInfoBean
implements I_CmsItemId, Comparable<CmsGalleriesListInfoBean> {

    /** A list with content types corresponding to this gallery type. */
    private ArrayList<String> m_contentTypes;

    /** The gallery type name. */
    private String m_galleryTypeName;

    /** The path to the gallery icon. */
    private String m_iconResource;

    /**
     * The empty default constructor.<p>
     */
    public CmsGalleriesListInfoBean() {

        // an empty constructor
    }

    /**
     * The constructor.<p>
     * 
     * @param title the title of the gallery
     * @param subtitle the path of the gallery
     * @param additionalInfo the additional info if given
     */
    public CmsGalleriesListInfoBean(String title, String subtitle, Map<String, String> additionalInfo) {

        super(title, subtitle, additionalInfo);
    }

    /**
     * Default compareTo method uses title to compare the galleries.<p>
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(CmsGalleriesListInfoBean o) {

        return getTitle().compareTo(o.getTitle());
    }

    /**
     * Returns the content types which can be used within this gallery type.<p>
     *
     * @return the contentTypes
     */
    public ArrayList<String> getContentTypes() {

        return m_contentTypes;
    }

    /**
     * Returns the gallery type name.<p>
     *
     * @return the gallery type name
     */
    public String getGalleryTypeName() {

        return m_galleryTypeName;
    }

    /**
     * Returns the gallery icon.<p>
     *
     * @return the icon
     */
    public String getIconResource() {

        return m_iconResource;
    }

    /**
     * Returns the gallery path as a unique id.<p>
     * 
     * @see org.opencms.ade.galleries.shared.I_CmsItemId#getId()
     */
    public String getId() {

        return getSubTitle();
    }

    /**
     * Sets the content types which can be used within this gallery type.<p>
     *
     * @param contentTypes the contentTypes to set
     */
    public void setContentTypes(ArrayList<String> contentTypes) {

        m_contentTypes = contentTypes;
    }

    /**
     * Sets the gallery type name.<p>
     *
     * @param galleryTypeName the type name of this gallery
     */
    public void setGalleryTypeName(String galleryTypeName) {

        m_galleryTypeName = galleryTypeName;
    }

    /**
     * Sets the gallery icon.<p>
     *
     * @param icon the icon to set
     */
    public void setIconResource(String icon) {

        m_iconResource = icon;
    }

    /**
     * Sets the gallery path as a unique id.<p>
     *         
     * @see org.opencms.ade.galleries.shared.I_CmsItemId#setId(java.lang.String)
     */
    public void setId(String id) {

        setSubTitle(id);
    }
}
