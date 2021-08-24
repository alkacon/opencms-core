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

import org.opencms.file.CmsFile;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.xml2json.CmsJsonRequest;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerException;
import org.opencms.xml.xml2json.handler.CmsJsonHandlerXmlContent.PathNotFoundException;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * Class representing a JSON document for a folder.
 */
public class CmsJsonDocumentFolder extends A_CmsJsonDocument implements I_CmsJsonDocument {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJsonDocumentFolder.class);

    /**
     * Creates a new JSON document.
     *
     * @param jsonRequest the JSON request
     */
    public CmsJsonDocumentFolder(CmsJsonRequest jsonRequest) {

        super(jsonRequest);
    }

    /**
     * @see org.opencms.xml.xml2json.document.I_CmsJsonDocument#getJson()
     */
    public Object getJson()
    throws JSONException, CmsException, CmsJsonHandlerException, PathNotFoundException, Exception {

        CmsResource target = m_context.getResource();
        int levels = m_jsonRequest.getParamLevels(1).intValue();
        m_json = folderListingJson(target, levels);
        return m_json;
    }

    /**
     * Formats folder listing as a JSON object, with the individual file names in the folder as keys.
     *
     * @param target the folder
     * @param levelsLeft the number of levels to format (if 1, only the direct children are listed)
     *
     * @return the JSON representation of the folder listing
     * @throws Exception if something goes wrong
     */
    protected JSONObject folderListingJson(CmsResource target, int levelsLeft) throws Exception {

        List<CmsResource> children = m_context.getCms().readResources(target, CmsResourceFilter.DEFAULT, false);
        JSONObject result = new JSONObject(true);
        for (CmsResource resource : children) {
            JSONObject childEntry = formatResource(resource);
            if (resource.isFolder() && (levelsLeft > 1)) {
                JSONObject childrenJson = folderListingJson(resource, levelsLeft - 1);
                childEntry.put("children", childrenJson);
            }
            result.put(resource.getName(), childEntry);
        }
        return result;
    }

    /**
     * Formats a resource object as JSON.<p>
     *
     * @param resource the resource
     * @return the JSON
     * @throws Exception if something goes wrong
     */
    private JSONObject formatResource(CmsResource resource) throws Exception {

        boolean isContent = false;
        if (!resource.isFolder()) {
            isContent = CmsResourceTypeXmlContent.isXmlContent(resource);
        }
        Boolean paramContent = m_jsonRequest.getParamContent();
        Boolean paramWrapper = m_jsonRequest.getParamWrapper(true);
        if (isContent && paramContent.booleanValue()) {
            CmsFile file = m_context.getCms().readFile(resource);
            CmsXmlContent xmlContent = CmsXmlContentFactory.unmarshal(m_context.getCms(), file);
            CmsJsonDocumentEmbeddedXmlContent jsonDocumentXmlContent = new CmsJsonDocumentEmbeddedXmlContent(
                m_jsonRequest,
                xmlContent);
            return (JSONObject)jsonDocumentXmlContent.getJson();
        } else if (paramWrapper.booleanValue()) {
            CmsJsonDocumentResource jsonDocumentResource = new CmsJsonDocumentResource(m_jsonRequest, resource);
            return (JSONObject)jsonDocumentResource.getJson();
        } else {
            return new JSONObject();
        }
    }

}
