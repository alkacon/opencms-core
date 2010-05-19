/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsResultItemBean.java,v $
 * Date   : $Date: 2010/05/19 09:02:51 $
 * Version: $Revision: 1.3 $
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

import org.opencms.gwt.shared.sort.I_CmsHasPath;
import org.opencms.gwt.shared.sort.I_CmsHasTitle;
import org.opencms.gwt.shared.sort.I_CmsHasType;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A specific bean holding all info to be displayed in {@link org.opencms.ade.galleries.client.ui.CmsResultListItem}s.<p>
 * 
 * @see org.opencms.ade.galleries.client.ui.CmsResultListItem
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsResultItemBean implements I_CmsHasTitle, I_CmsHasPath, I_CmsHasType, IsSerializable {

    /** The structured id of the resource. */
    private String m_clientId;

    private String m_description;

    private String m_excerpt;

    /** The resource path as a unique resource id. */
    private String m_path;

    /** The resource type name. */
    private String m_type;

    private String m_title;

    /**
     * Returns the structured id.<p>
     *
     * @return the structured id
     */
    public String getClientId() {

        return m_clientId;
    }

    /**
     * Returns the description.<p>
     *
     * @return the description
     */
    public String getDescription() {

        return m_description;
    }

    /**
     * Returns the excerpt.<p>
     *
     * @return the excerpt
     */
    public String getExcerpt() {

        return m_excerpt;
    }

    /**
     * Returns the resourcePath.<p>
     *
     * @return the resourcePath
     */
    public String getPath() {

        return m_path;
    }

    /**
     * Returns the resource type name.<p>
     *
     * @return the resource type name
     */
    public String getType() {

        return m_type;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Sets the structure id.<p>
     *
     * @param clientId the structure id to set
     */
    public void setClientId(String clientId) {

        m_clientId = clientId;
    }

    /**
     * Sets the description.<p>
     *
     * @param description the description to set
     */
    public void setDescription(String description) {

        m_description = description;
    }

    /**
     * Sets the excerpt.<p>
     *
     * @param excerpt the excerpt to set
     */
    public void setExcerpt(String excerpt) {

        m_excerpt = excerpt;
    }

    /**
     * Sets the resource path.<p>
     *
     * @param path the resource path to set
     */
    public void setPath(String path) {

        m_path = path;
    }

    /**
     * Sets the resource type name.<p>
     *
     * @param type the resource type name to set
     */
    public void setType(String type) {

        m_type = type;
    }

    /**
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }
}
