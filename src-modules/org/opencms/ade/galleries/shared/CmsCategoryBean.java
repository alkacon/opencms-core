/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsCategoryBean.java,v $
 * Date   : $Date: 2011/05/03 10:48:50 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A specific bean holding all info to be displayed in the categories tab.<p>
 * 
 * @author Polina Smagina
 * 
 * @version $Revision: 1.3 $
 * 
 * @since 8.0.0
 */
public class CmsCategoryBean implements I_CmsHasTitle, I_CmsHasPath, IsSerializable {

    /** The category path as id. */
    private String m_path;

    private String m_description;

    private String m_title;

    /**
     * The constructor.<p>
     * 
     * @param title the title to set 
     * @param description the subtitle to set
     * @param path the category path
     */
    public CmsCategoryBean(String title, String description, String path) {

        m_title = title;
        m_description = description;
        m_path = path;
    }

    /**
     * Returns the category path.<p>
     *
     * @return the category path
     */
    public String getPath() {

        return m_path;
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
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Sets the category path.<p>
     *
     * @param path the category path to set
     */
    public void setPath(String path) {

        m_path = path;
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
     * Sets the title.<p>
     *
     * @param title the title to set
     */
    public void setTitle(String title) {

        m_title = title;
    }
}