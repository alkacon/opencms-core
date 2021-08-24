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

import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.xml2json.CmsJsonRequest;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerException;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerXmlContent.PathNotFoundException;
import org.opencms.xml.xml2json.renderer.CmsJsonRendererContainerPage;

/**
 * Class representing a JSON document for a container page.
 */
public class CmsJsonDocumentContainerPage extends CmsJsonDocumentXmlContent {

    /** The container page renderer. */
    private CmsJsonRendererContainerPage m_renderer;

    /**
     * Creates a new container page JSON document.<p>
     *
     * @param jsonRequest the JSON request
     * @param xmlContent the XML content
     * @throws Exception if something goes wrong
     */
    public CmsJsonDocumentContainerPage(CmsJsonRequest jsonRequest, CmsXmlContent xmlContent)
    throws Exception {

        this(jsonRequest, xmlContent, true);
    }

    /**
     * Creates a new container page JSON document.<p>
     *
     * @param jsonRequest the JSON request
     * @param xmlContent the XML content
     * @param embedLinkedModelgroups whether to embedLinkedContents
     * @throws Exception if something goes wrong
     */
    public CmsJsonDocumentContainerPage(
        CmsJsonRequest jsonRequest,
        CmsXmlContent xmlContent,
        boolean embedLinkedModelgroups)
    throws Exception {

        super(jsonRequest, xmlContent, embedLinkedModelgroups);
        initRenderer();
    }

    /**
     * @see org.opencms.xml.xml2json.document.CmsJsonDocumentResource#getJson()
     */
    @Override
    public Object getJson()
    throws JSONException, CmsException, CmsJsonHandlerException, PathNotFoundException, Exception {

        insertJsonContainerPage();
        Boolean paramContent = m_jsonRequest.getParamContent();
        if (paramContent.booleanValue()) {
            insertJsonLinkedContents();
        }
        insertJsonWrapper();
        return m_json;
    }

    /**
     * Initializes the container page renderer.<p>
     */
    private void initRenderer() {

        m_renderer = new CmsJsonRendererContainerPage(
            m_context.getCms(),
            m_xmlContent.getFile(),
            m_context.getAccessPolicy()::checkPropertyAccess);
    }

    /**
     * Inserts a JSON representation of this container page into this JSON document.<p>
     *
     * @throws Exception if JSON rendering fails
     */
    private void insertJsonContainerPage() throws Exception {

        m_json = (JSONObject)m_renderer.renderJson();
    }
}
