/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/CmsSitemapTemplate.java,v $
 * Date   : $Date: 2010/04/22 14:32:08 $
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

package org.opencms.ade.sitemap.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Sitemap initialization data.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0
 */
public class CmsSitemapTemplate implements IsSerializable {

    /** The description. */
    private String m_description;

    /** The image path. */
    private String m_imgPath;

    /** The site path. */
    private String m_sitePath;

    /** The title. */
    private String m_title;

    /**
     * Constructor.<p>
     */
    public CmsSitemapTemplate() {

        // empty
    }

    /**
     * Constructor.<p>
     * 
     * @param title the title
     * @param description the description
     * @param sitePath the site path
     * @param imgPath the image path
     */
    public CmsSitemapTemplate(String title, String description, String sitePath, String imgPath) {

        m_title = title;
        m_description = description;
        m_sitePath = sitePath;
        m_imgPath = imgPath;
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
     * Returns the image path.<p>
     *
     * @return the image path
     */
    public String getImgPath() {

        return m_imgPath;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }
}
