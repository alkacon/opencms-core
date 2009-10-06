/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/I_CmsADEConfiguration.java,v $
 * Date   : $Date: 2009/10/06 08:19:06 $
 * Version: $Revision: 1.1.2.1 $
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

package org.opencms.workplace.editors.ade;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Configurable & expandable configuration.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.6 
 */
public interface I_CmsADEConfiguration {

    /**
     * Creates a new element of a given type at the configured location.<p>
     * 
     * @param type the type of the element to be created
     * 
     * @return the CmsResource representing the newly created element
     * 
     * @throws CmsException if something goes wrong
     */
    CmsResource createNewElement(String type) throws CmsException;

    /**
     * Returns the list of creatable elements.<p>
     * 
     * @return the list of creatable elements
     * 
     * @throws CmsException if something goes wrong 
     */
    List<CmsResource> getCreatableElements() throws CmsException;

    /**
     * Returns the maximal size of the favorite list.<p>
     * 
     * @return the maximal size of the favorite list
     * 
     * @throws CmsException if something goes wrong 
     */
    int getFavoriteListMaxSize() throws CmsException;

    /**
     * Returns the name of the next new file of the given type to be created.<p>
     * 
     * @param type the resource type name
     * 
     * @return the name of the next new file of the given type to be created
     * 
     * @throws CmsException if something goes wrong
     */
    String getNextNewFileName(String type) throws CmsException;

    /**
     * Returns the maximal size of the recent list.<p>
     * 
     * @return the maximal size of the recent list
     * 
     * @throws CmsException if something goes wrong 
     */
    int getRecentListMaxSize() throws CmsException;

    /**
     * Returns the list of searchable resource types.<p>
     * 
     * @return the list of searchable resource types
     * 
     * @throws CmsException if something goes wrong 
     */
    List<String> getSearchableResourceTypes() throws CmsException;

    /**
     * Returns the size of a search page.<p>
     * 
     * @return the maximal size of the favorite list
     * 
     * @throws CmsException if something goes wrong 
     */
    int getSearchPageSize() throws CmsException;

    /**
     * Initializes the current configuration.<p>
     * 
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     */
    void init(CmsObject cms, String cntPageUri, HttpServletRequest request);
}
