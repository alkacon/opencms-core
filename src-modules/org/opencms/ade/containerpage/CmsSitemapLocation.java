/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/containerpage/Attic/CmsSitemapLocation.java,v $
 * Date   : $Date: 2010/06/18 07:29:54 $
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

package org.opencms.ade.containerpage;

/**
 * A bean which represents the location of a container page in a sitemap.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsSitemapLocation {

    /** The VFS URI of the sitemap referencing the container page.<p> */
    private String m_sitemapUri;

    /** The site path of the container page. */
    private String m_sitePath;

    /**
     * Constructor.<p>
     * 
     * @param sitemapUri the URI of the sitemap referencing the container page 
     * @param sitePath the site path of the container page 
     */
    public CmsSitemapLocation(String sitemapUri, String sitePath) {

        m_sitemapUri = sitemapUri;
        m_sitePath = sitePath;
    }

    /**
     * Returns the URI of the sitemap referencing the container page.<p> 
     * 
     * @return the URI of the sitemap referencing the container page 
     */
    public String getSitemapUri() {

        return m_sitemapUri;
    }

    /**
     * Returns the site path of the container page.<p>
     * 
     * @return the site path of the container page 
     */
    public String getSitePath() {

        return m_sitePath;
    }

}
