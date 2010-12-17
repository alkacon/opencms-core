/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/I_CmsDetailPageFinder.java,v $
 * Date   : $Date: 2010/12/17 08:45:29 $
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

package org.opencms.xml.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.Collection;

/**
 * Interface for classes which can find the detail page for a given resource.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public interface I_CmsDetailPageFinder {

    /**
     * Finds all detail pages for a given resource.<p>
     * 
     * @param cms the current CMS context
     * @param res the resource for which the detail pages should be found
     *  
     * @return the list of detail page 
     * @throws CmsException
     */
    Collection<String> getAllDetailPages(CmsObject cms, CmsResource res) throws CmsException;

    /**
     * Returns the detail page link for the given resource, or null if there is no detail page for the resource.<p>
     * 
     * @param cms the CMS context 
     * @param res the resource  
     * @param linkSource the uri in the context of which the detail page link is being generated
     * @return the detail page link, or null
     *   
     * @throws CmsException if something goes wrong 
     */
    String getDetailPage(CmsObject cms, CmsResource res, String linkSource) throws CmsException;

}
