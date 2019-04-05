/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.xml.xml2json;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Provides context information to JSON handlers.<p>
 *
 * Also lazily loads the resource or content to be rendered as JSON.
 */
public class CmsJsonHandlerContext {

    /** The CMS context with the original site root. */
    private CmsObject m_cms;

    /** The CMS context initialized with the root site. */
    private CmsObject m_rootCms;

    /** The path below the JSON handler prefix. */
    private String m_path;

    /** The resource for the path (initially null). */
    private CmsResource m_resource;

    /** The file for the path (initially null). */
    private CmsFile m_file;

    /** The request parameters from the resource init handler call. */
    private Map<String, String> m_parameters;

    /** Temporary data storage to be used by individual handlers. */
    private Map<String, Object> m_tempData = new LinkedHashMap<>();

    /** The XML content (initially null). */
    private CmsXmlContent m_content;

    /**
     * Creates a new instance.
     *
     * @param cms the CMS context
     * @param path the path below the JSON handler
     * @param params the request parameters
     * @throws CmsException if something goes wrong
     */
    public CmsJsonHandlerContext(CmsObject cms, String path, Map<String, String> params)
    throws CmsException {

        m_cms = cms;
        m_rootCms = OpenCms.initCmsObject(m_cms);
        m_rootCms.getRequestContext().setSiteRoot("");
        m_path = path;
        m_parameters = params;
    }

    /**
     * Gets the CMS context.
     *
     * @return the CMS context
     */
    public CmsObject getCms() {

        return m_cms;
    }

    /**
     * Loads the XML content.<p>
     *
     * This only works if the resource for the given path is actually an  XML content.
     *
     * @return the XML content for the path
     *
     * @throws CmsException if something goes wrong
     */
    public CmsXmlContent getContent() throws CmsException {

        if (m_content == null) {
            m_content = CmsXmlContentFactory.unmarshal(m_cms, getFile());
        }
        return m_content;
    }

    /**
     * Gets the file for the path.
     *
     * This will only work if the resource for the path is actually a file.
     *
     * @return the file for the path
     *
     * @throws CmsException if something goes wrong
     */
    public CmsFile getFile() throws CmsException {

        if (m_file == null) {
            m_file = m_cms.readFile(getResource());
        }
        return m_file;
    }

    /**
     * Gets the request parameters.
     *
     * @return the request parameters
     */
    public Map<String, String> getParameters() {

        return Collections.unmodifiableMap(m_parameters);
    }

    /**
     * Gets the resource.
     *
     * @return the resource
     * @throws CmsException if something goes wrong
     */
    public CmsResource getResource() throws CmsException {

        if (m_resource == null) {
            m_resource = m_rootCms.readResource(m_path, CmsResourceFilter.IGNORE_EXPIRATION);
        }
        return m_resource;
    }

    /**
     * Gets the CMS context initialized to the root site.
     *
     * @return the CMS context initialized to the root site
     */
    public CmsObject getRootCms() {

        return m_rootCms;
    }

    /**
     * Gets the temporary storage to be used by individual handlers.
     *
     * @return the temporary storage
     */
    public Map<String, Object> getTempStorage() {

        return m_tempData;
    }

}
