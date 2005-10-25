/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/workplace/tools/content/check/CmsContentCheckResource.java,v $
 * Date   : $Date: 2005/10/25 15:14:32 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.workplace.tools.content.check;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This object encapuslates a CmsResource, its content and unmarshalled xml content
 * for processing in the content check plugins.<p>
 * 
 *
 * @author  Michael Emmerich
 * 
 * @version $Revision: 1.1.2.2 $ 
 * 
 * @since 6.1.2 
 */
public class CmsContentCheckResource {

    /** Encapsulated content array. */
    private byte[] m_content;

    /** List of errors found during content check. */
    private List m_errors;

    /** Encapsulated CmsResource. */
    private CmsResource m_resource;

    /** List of warnings found during content check. */
    private List m_warnings;

    /** Encapsulated unmashalled xml content. */
    private CmsXmlContent m_xmlcontent;

    /**
     * Constructor, creates an CmsContentCheckResource object.<p>
     * 
     * @param res the CmsResource to encapsulate in the CmsContentCheckResource.
     */
    public CmsContentCheckResource(CmsResource res) {

        m_resource = res;
        m_content = null;
        m_xmlcontent = null;
        m_errors = new ArrayList();
        m_warnings = new ArrayList();
    }

    /** Adds a new error to the list of errors for this resource.<p>
     * 
     * @param error the error message to be added
     */
    public void addError(String error) {

        m_errors.add(error);
    }

    /** Adds a list of errors to the list of errors for this resource.<p>
     *  
     * @param errors the error messages to be added
     */
    public void addErrors(List errors) {

        m_errors.addAll(errors);
    }

    /** Adds a new warning to the list of warnings for this resource.<p>
     * 
     * @param warning the warning message to be added
     */
    public void addWarning(String warning) {

        m_warnings.add(warning);
    }

    /** Adds a list of warnings to the list of warnings for this resource.<p>
     *  
     * @param warnings the error messages to be added
     */
    public void addWarnings(List warnings) {

        m_warnings.addAll(warnings);
    }

    /**
     * Gets the encapuslated file content.<p>
     * 
     * @return the byte array holding the file content
     */
    public byte[] getContent() {

        return m_content;
    }

    /**
     * Gets the list of all errors found during the content checks for this resource.<p>
     * @return List of erros, delivered as strings
     */
    public List getErrors() {

        return m_errors;
    }

    /**
     * Gets the encapsulated CmsResource.<p>
     * 
     * @return the CmsResource
     */
    public CmsResource getResource() {

        return m_resource;
    }

    /**
     * Gets the root path of the encapsulated CmsResource.<p>
     * 
     * @return root path of the encapsulated CmsResource
     */
    public String getResourceName() {

        return m_resource.getRootPath();
    }

    /**
     * Gets the list of all warnings found during the content checks for this resource.<p>
     * @return List of warnings, delivered as strings
     */
    public List getWarnings() {

        return m_warnings;
    }

    /**
     * Gets the encapuslated and unmarshalled xml content.<p>
     * 
     * @return the unmarshalled xml content
     */
    public CmsXmlContent getXmlContent() {

        return m_xmlcontent;
    }

    /**
     * Loads the content of the encapsulated CmsResource and stores it within the 
     * CmsContentCheckResource. If the content is already existing, it is not loaded
     * again.<p>
     * 
     * @param cms the CmsObject
     * @throws CmsException if loading of the content fails
     */
    public void upgradeContent(CmsObject cms) throws CmsException {

        if (m_content == null) {
            m_content = CmsFile.upgrade(m_resource, cms).getContents();
        }
    }

    /**
     * Unmarshalls the content of the encapsulated CmsResource and stores it within the 
     * CmsContentCheckResource. If the xmlcontent is already existing, it is not unmarshalled
     * again.<p>
     * 
     * @param cms the CmsObject
     * @throws CmsException if loading of the content fails
     */
    public void upgradeXmlContent(CmsObject cms) throws CmsException {

        if (m_xmlcontent == null) {
            CmsFile file = CmsFile.upgrade(m_resource, cms);
            m_xmlcontent = CmsXmlContentFactory.unmarshal(cms, file);
        }
    }
}
