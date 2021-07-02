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

package org.opencms.xml.xml2json.document;

import org.opencms.file.CmsResource;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.xml.xml2json.CmsJsonHandlerException;
import org.opencms.xml.xml2json.CmsJsonRequest;
import org.opencms.xml.xml2json.CmsResourceDataJsonHelper;
import org.opencms.xml.xml2json.CmsXmlContentJsonHandler.PathNotFoundException;

/**
 * Class representing a JSON document for a CMS resource.<p>
 */
public class CmsJsonDocumentResource extends A_CmsJsonDocument implements I_CmsJsonDocument {

    /** The resource data helper. */
    protected CmsResourceDataJsonHelper m_helper;

    /** The resource. */
    protected CmsResource m_resource;

    /**
     * Creates a new JSON document.<p>
     *
     * @param jsonRequest the JSON request
     * @param resource the resource
     */
    protected CmsJsonDocumentResource(CmsJsonRequest jsonRequest, CmsResource resource) {

        super(jsonRequest);
        m_resource = resource;
        initHelper();
    }

    /**
     * @see org.opencms.xml.xml2json.document.I_CmsJsonDocument#getJson()
     */
    public Object getJson()
    throws JSONException, CmsException, CmsJsonHandlerException, PathNotFoundException, Exception {

        boolean showWrapper = true;
        Boolean paramWrapper = m_jsonRequest.getParamWrapper();
        if ((paramWrapper != null) && (paramWrapper.booleanValue() == false)) {
            showWrapper = false;
        }
        if (showWrapper) {
            insertJsonResource();
        }
        return m_json;
    }

    /**
     * If the request parameter "wrapper" is set, inserts information about this resource into the JSON document.<p>
     * @throws JSONException
     * @throws CmsException
     */
    protected void insertJsonResource() throws JSONException, CmsException {

        insertJsonResourceAttributes();
        insertJsonResourceFile();
        insertJsonResourceParams();
        insertJsonResourcePathAndLink();
        insertJsonResourceProperties();
    }

    /**
     * Inserts the resource attributes into this JSON document.<p>
     *
     * @throws JSONException if JSON rendering fails
     */
    protected void insertJsonResourceAttributes() throws JSONException {

        m_json.put("attributes", m_helper.attributes());
    }

    /**
     * Inserts file type information into this JSON document.<p>
     *
     * @throws JSONException if JSON rendering fails
     */
    protected void insertJsonResourceFile() throws JSONException {

        boolean isContent = !m_resource.isFolder() && CmsResourceTypeXmlContent.isXmlContent(m_resource);
        m_json.put("isFolder", m_resource.isFolder());
        m_json.put("isXmlContent", Boolean.valueOf(isContent));
    }

    /**
     * Inserts information about the effective request parameters into this JSON document.<p>
     *
     * @throws JSONException if JSON rendering fails
     */
    protected void insertJsonResourceParams() throws JSONException {

        JSONObject json = new JSONObject(true);
        for (String key : m_context.getParameters().keySet()) {
            json.put(key, m_context.getParameters().get(key));
        }
        m_json.put("params", json);
    }

    /**
     * Inserts path and link information about this resource into this JSON document.<p>
     *
     * @throws JSONException if JSON rendering fails
     */
    protected void insertJsonResourcePathAndLink() throws JSONException {

        m_helper.addPathAndLink(m_json);
    }

    /**
     * Inserts the properties of this resource into the JSON document.<p>
     *
     * @throws JSONException if JSON rendering fails
     * @throws CmsException if reading the properties fails
     */
    protected void insertJsonResourceProperties() throws JSONException, CmsException {

        m_helper.addProperties(m_json);
    }

    /**
     * Initializes the resource data helper.<p>
     */
    private void initHelper() {

        m_helper = new CmsResourceDataJsonHelper(
            m_context.getCms(),
            m_resource,
            m_context.getAccessPolicy()::checkPropertyAccess);
    }
}
