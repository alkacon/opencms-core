/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/CmsCntPageManager.java,v $
 * Date   : $Date: 2009/10/14 14:38:02 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
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

import java.util.List;

import javax.servlet.ServletRequest;

/**
 * Container page manager.<p>
 * 
 * Provides all relevant functions for container pages.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 7.9.2
 */
public class CmsCntPageManager {

    /** The current cms context. */
    protected CmsObject m_cms;

    /** The container page uri. */
    protected String m_cntPageUri;

    /** The request itself. */
    protected ServletRequest m_request;

    /** The configuration instance. */
    protected I_CmsADEConfiguration m_configuration;

    /**
     * Creates a new ADE manager.<p>
     * 
     * @param cms the cms context 
     * @param cntPageUri the container page uri
     * @param request the request itself
     * @param configuration the ADE configuration
     */
    public CmsCntPageManager(
        CmsObject cms,
        String cntPageUri,
        ServletRequest request,
        I_CmsADEConfiguration configuration) {

        m_cms = cms;
        m_cntPageUri = cntPageUri;
        m_request = request;
        m_configuration = configuration;
    }

    /**
     * Creates a new element of a given type at the configured location.<p>
     * 
     * @param type the type of the element to be created
     * 
     * @return the CmsResource representing the newly created element
     * 
     * @throws CmsException if something goes wrong
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#createNewElement(CmsObject, String, ServletRequest, String)
     */
    public CmsResource createNewElement(String type) throws CmsException {

        return m_configuration.createNewElement(m_cms, m_cntPageUri, m_request, type);
    }

    /**
     * Returns the list of creatable elements.<p>
     * 
     * @return the list of creatable elements
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getCreatableElements(CmsObject, String, ServletRequest)
     */
    public List<CmsResource> getCreatableElements() throws CmsException {

        return m_configuration.getCreatableElements(m_cms, m_cntPageUri, m_request);
    }

    /**
     * Returns the name of the next new file of the given type to be created.<p>
     * 
     * @param type the resource type name
     * 
     * @return the name of the next new file of the given type to be created
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getNextNewFileName(CmsObject, String, ServletRequest, String)
     */
    public String getNextNewFileName(String type) throws CmsException {

        return m_configuration.getNextNewFileName(m_cms, m_cntPageUri, m_request, type);
    }

    /**
     * Returns the list of searchable resource types.<p>
     * 
     * @return the list of searchable resource types
     * 
     * @throws CmsException if something goes wrong 
     *
     * @see org.opencms.xml.containerpage.I_CmsADEConfiguration#getSearchableResourceTypes(CmsObject, String, ServletRequest)
     */
    public List<String> getSearchableResourceTypes() throws CmsException {

        return m_configuration.getSearchableResourceTypes(m_cms, m_cntPageUri, m_request);
    }
}
