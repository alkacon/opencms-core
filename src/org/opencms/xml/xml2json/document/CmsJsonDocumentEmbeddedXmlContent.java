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

import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.xml2json.CmsJsonRequest;

/**
 * Class representing a JSON document for an embedded XML content.
 */
public class CmsJsonDocumentEmbeddedXmlContent extends CmsJsonDocumentXmlContent {

    /**
     * Creates a new JSON document.
     *
     * @param jsonRequest the JSON request
     * @param xmlContent the XML content
     * @throws Exception if something goes wrong
     */
    public CmsJsonDocumentEmbeddedXmlContent(CmsJsonRequest jsonRequest, CmsXmlContent xmlContent)
    throws Exception {

        super(jsonRequest, xmlContent);
        m_throwException = false;
    }

    /**
     * @see org.opencms.xml.xml2json.document.CmsJsonDocumentXmlContent#isShowWrapperRequest()
     */
    @Override
    protected boolean isShowWrapperRequest() {

        return m_jsonRequest.getParamWrapper(true).booleanValue();
    }
}
