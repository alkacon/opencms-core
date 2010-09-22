/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/I_CmsADEConfiguration.java,v $
 * Date   : $Date: 2010/09/22 14:27:47 $
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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

import java.util.Collection;

import javax.servlet.ServletRequest;

/**
 * Configurable & expandable configuration.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.3 $ 
 * 
 * @since 7.6 
 */
public interface I_CmsADEConfiguration {

    /**
     * Creates a new element of a given type at the configured location.<p>
     * 
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     * @param type the type of the element to be created
     * 
     * @return the CmsResource representing the newly created element
     * 
     * @throws CmsException if something goes wrong
     */
    CmsResource createNewElement(CmsObject cms, String cntPageUri, ServletRequest request, String type)
    throws CmsException;

    /**
     * Returns the list of creatable elements.<p>
     * 
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     * 
     * @return the list of creatable elements
     * 
     * @throws CmsException if something goes wrong 
     */
    Collection<CmsResource> getCreatableElements(CmsObject cms, String cntPageUri, ServletRequest request)
    throws CmsException;

    /**
     * Returns the maximal size of the favorite list.<p>
     * 
     * @param cms the current opencms context
     * 
     * @return the maximal size of the favorite list
     * 
     * @throws CmsException if something goes wrong 
     */
    int getFavoriteListMaxSize(CmsObject cms) throws CmsException;

    /**
     * Returns the formatter for a given content element and a container type and width.<p>
     * 
     * @param cms the CMS context 
     * @param res the content element resource 
     * @param cntType the container type 
     * @param width the container width
     *  
     * @return the element's formatter uri for the given container type and width
     *   
     * @throws CmsException if something goes wrong  
     */
    String getFormatterForContainerTypeAndWidth(CmsObject cms, CmsResource res, String cntType, int width)
    throws CmsException;

    /**
     * Returns the name of the next new file of the given type to be created.<p>
     * 
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     * @param type the resource type name
     * 
     * @return the name of the next new file of the given type to be created
     * 
     * @throws CmsException if something goes wrong
     */
    String getNextNewFileName(CmsObject cms, String cntPageUri, ServletRequest request, String type)
    throws CmsException;

    /**
     * Returns the maximal size of the recent list.<p>
     * 
     * @param cms the current opencms context
     * 
     * @return the maximal size of the recent list
     * 
     * @throws CmsException if something goes wrong 
     */
    int getRecentListMaxSize(CmsObject cms) throws CmsException;

    /**
     * Returns the list of searchable resource types.<p>
     * 
     * @param cms the current opencms context
     * @param cntPageUri the container page uri
     * @param request the current request
     * 
     * @return the list of searchable resource types, identified by a sample resource
     * 
     * @throws CmsException if something goes wrong 
     */
    Collection<CmsResource> getSearchableResourceTypes(CmsObject cms, String cntPageUri, ServletRequest request)
    throws CmsException;

    /**
     * Returns the size of a search page.<p>
     * 
     * @param cms the current opencms context
     * 
     * @return the maximal size of the favorite list
     * 
     * @throws CmsException if something goes wrong 
     */
    int getSearchPageSize(CmsObject cms) throws CmsException;

    /**
     * Initializes the configuration.<p>
     * 
     * @param cms the CMS object  
     * @param moduleParamKey the name of the module parameter which contains the name of the ADE configuration file  
     */
    void init(CmsObject cms, String moduleParamKey);

}
