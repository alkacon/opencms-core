/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsTypesListInfoBean.java,v $
 * Date   : $Date: 2010/03/30 14:08:36 $
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

package org.opencms.ade.galleries.shared;

import org.opencms.gwt.shared.CmsListInfoBean;

import java.util.ArrayList;
import java.util.Map;

//TODO: do we need here I_CmsItemId?? is the id in the LinkedHashMap enough?
/**
 * A specific bean holding all info to be displayed in {@link org.opencms.ade.galleries.client.ui.CmsTypeListItem}s.<p>
 * 
 * @see org.opencms.ade.galleries.client.ui.CmsTypeListItem
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsTypesListInfoBean extends CmsListInfoBean implements I_CmsItemId {

    /** The path to the resource type icon. */
    private String m_iconResource;

    /** The resource type name as a unique id. */
    private String m_resourceTypeName;

    /** The nice name of the resource type name. */
    private String m_niceTypeName;

    /** An array of gallery type names associated with this content type. */
    private ArrayList<String> m_galleryTypeNames;

    /**
     * Returns the resource type icon.<p>
     *
     * @return the iconResource the icon for the resource type
     */
    public String getIconResource() {

        return m_iconResource;
    }

    /**
     * Sets the resource type icon.<p>
     *
     * @param iconResource the resource type icon to set
     */
    public void setIconResource(String iconResource) {

        m_iconResource = iconResource;
    }

    /**
     * The empty default constructor.<p>
     */
    public CmsTypesListInfoBean() {

        // an empty constructor
    }

    /**
     * The constructor.<p>
     * 
     * @param title the nice resource type name
     * @param subtitle the description 
     * @param additionalInfo the sdditional info if given
     */
    public CmsTypesListInfoBean(String title, String subtitle, Map<String, String> additionalInfo) {

        super(title, subtitle, additionalInfo);
    }

    /**
     * Return the resource type name as a unique id.<p>
     * 
     * @see org.opencms.ade.galleries.shared.I_CmsItemId#getId()
     */
    public String getId() {

        return m_resourceTypeName;
    }

    /**
     * Sets the resource type as a unique id.<p>
     * 
     * @see org.opencms.ade.galleries.shared.I_CmsItemId#setId(java.lang.String)
     */
    public void setId(String id) {

        m_resourceTypeName = id;
    }

    /**
     * Sets the nice type name of the resource.<p>
     *
     * @param typeName the nice type name to set
     */
    public void setTypeNiceName(String typeName) {

        m_niceTypeName = typeName;
    }

    /**
     * Returns the nice type name of the resource.<p>
     *
     * @return the nice type name
     */
    public String getTypeNiceName() {

        return m_niceTypeName;
    }

    /**
     * Sets the list with the gallery types names associated with this resource type.<p>
     *
     * @param galleryNames the list with gallery type names to set
     */
    public void setGalleryTypeNames(ArrayList<String> galleryNames) {

        m_galleryTypeNames = galleryNames;
    }

    /**
     * Returns the list with the gallery types names associated with this resource type.<p>
     *
     * @return the gallery type names
     */
    public ArrayList<String> getGalleryTypeNames() {

        return m_galleryTypeNames;
    }
}