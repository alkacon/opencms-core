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

import org.opencms.json.JSONObject;
import org.opencms.xml.xml2json.CmsJsonHandlerContext;
import org.opencms.xml.xml2json.CmsJsonRequest;

/**
 * Abstract class representing a JSON document.
 */
public abstract class A_CmsJsonDocument {

    /** The handler context. */
    protected CmsJsonHandlerContext m_context;

    /** The JSON document. */
    protected JSONObject m_json = new JSONObject(true);

    /** The JSON request. */
    protected CmsJsonRequest m_jsonRequest;

    /**
     * Creates a new JSON document.<p>
     *
     * @param jsonRequest the JSON request
     */
    protected A_CmsJsonDocument(CmsJsonRequest jsonRequest) {

        m_jsonRequest = jsonRequest;
        m_context = jsonRequest.getContext();
    }
}
