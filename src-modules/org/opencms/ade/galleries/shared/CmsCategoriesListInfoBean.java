/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsCategoriesListInfoBean.java,v $
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

import java.util.Map;

//TODO: do we need here I_CmsItemId?? is the id in the LinkedHashMap enough?
/**
 * A specific bean holding all info to be displayed in {@link org.opencms.ade.galleries.client.ui.CmsCategoryListItem}s.<p>
 * 
 * @see org.opencms.ade.galleries.client.ui.CmsCategoryListItem
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsCategoriesListInfoBean extends CmsListInfoBean implements I_CmsItemId {

    /** The path to the category icon. */
    private String m_iconResource;

    /** THe path to the root category. */
    private String m_rootPath;

    /** The category level. */
    private int m_level;

    /**
     * The empty default constructor.<p>
     */
    public CmsCategoriesListInfoBean() {

        // an empty constructor
    }

    /**
     * The constructor.<p>
     * 
     * @param title the title of the category
     * @param subtitle the category path
     * @param additionalInfo additional info if given
     */
    public CmsCategoriesListInfoBean(String title, String subtitle, Map<String, String> additionalInfo) {

        super(title, subtitle, additionalInfo);
    }

    /**
     * Sets the icon.<p>
     *
     * @param icon the icon to set
     */
    public void setIconResource(String icon) {

        m_iconResource = icon;
    }

    /**
     * Returns the icon.<p>
     *
     * @return the icon
     */
    public String getIconResource() {

        return m_iconResource;
    }

    /**
     * Returns the category path as a unique id for this category.<p>
     * 
     * @see org.opencms.ade.galleries.shared.I_CmsItemId#getId()
     */
    public String getId() {

        return getSubTitle();
    }

    /**
     * Sets the category path as a unique id for this category.<p>
     * 
     * @see org.opencms.ade.galleries.shared.I_CmsItemId#setId(java.lang.String)
     */
    public void setId(String id) {

        setSubTitle(id);
    }

    /**
     * Sets the root path of the category.<p>
     *
     * @param rootPath the root path to set
     */
    public void setRootPath(String rootPath) {

        m_rootPath = rootPath;
    }

    /**
     * Returns the root path of the category.<p>
     *
     * @return the root path
     */
    public String getRootPath() {

        return m_rootPath;
    }

    /**
     * Sets the level of the category.<p>
     *
     * @param level the level of the category to set
     */
    public void setLevel(int level) {

        m_level = level;
    }

    /**
     * Returns the level of the category.<p>
     *
     * @return the level
     */
    public int getLevel() {

        return m_level;
    }

}
