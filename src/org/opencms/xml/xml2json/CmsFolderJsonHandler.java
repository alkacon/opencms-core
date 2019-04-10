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

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;

import java.util.List;

/**
 * Produces directory listings in JSON format.
 */
public class CmsFolderJsonHandler implements I_CmsJsonHandler {

    /**
     * @see org.opencms.xml.xml2json.I_CmsJsonHandler#getOrder()
     */
    public double getOrder() {

        return 200;
    }

    /**
     * @see org.opencms.xml.xml2json.I_CmsJsonHandler#matches(org.opencms.xml.xml2json.CmsJsonHandlerContext)
     */
    public boolean matches(CmsJsonHandlerContext context) {

        return context.getResource().isFolder();
    }

    /**
     * @see org.opencms.xml.xml2json.I_CmsJsonHandler#renderJson(org.opencms.xml.xml2json.CmsJsonHandlerContext)
     */
    public CmsJsonResult renderJson(CmsJsonHandlerContext context) throws CmsException, JSONException {

        try {
            CmsResource indexJson = context.getRootCms().readResource(
                CmsStringUtil.joinPaths(context.getPath(), "index.json"));

            return new CmsJsonResult(indexJson);

        } catch (CmsVfsResourceNotFoundException e) {
            List<CmsResource> children = context.getCms().readResources(
                context.getResource(),
                CmsResourceFilter.DEFAULT,
                false);
            JSONObject result = new JSONObject(true);
            for (CmsResource resource : children) {
                JSONObject childEntry = formatResource(context, resource);
                result.put(resource.getName(), childEntry);
            }
            return new CmsJsonResult(result, 200);
        }

    }

    /**
     * Formats a resource object as JSON.
     * @param context the context
     * @param resource the resource
     *
     * @return the JSON
     *
     * @throws JSONException if something goes wrong with JSON formatting
     * @throws CmsException if something else goes wrong
     */
    private JSONObject formatResource(CmsJsonHandlerContext context, CmsResource resource)
    throws JSONException, CmsException {

        JSONObject result = new JSONObject(true);
        result.put("path", resource.getRootPath());
        result.put("type", OpenCms.getResourceManager().getResourceType(resource).getTypeName());
        result.put("isFolder", resource.isFolder());
        boolean isContent = false;
        if (!resource.isFolder()) {
            isContent = CmsResourceTypeXmlContent.isXmlContent(resource);
        }
        result.put("isXmlContent", Boolean.valueOf(isContent));
        result.put("lastModified", new Double(resource.getDateLastModified()));
        List<CmsProperty> props = context.getCms().readPropertyObjects(resource, false);
        JSONObject propJson = new JSONObject(true);
        for (CmsProperty prop : props) {
            propJson.put(prop.getName(), prop.getValue());
        }
        result.put("properties", propJson);
        return result;
    }
}
