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

import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Produces directory listings in JSON format.
 */
public class CmsFolderJsonHandler implements I_CmsJsonHandler {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsFolderJsonHandler.class);

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

        return (context.getResource() != null) && context.getResource().isFolder();
    }

    /**
     * @see org.opencms.xml.xml2json.I_CmsJsonHandler#renderJson(org.opencms.xml.xml2json.CmsJsonHandlerContext)
     */
    public CmsJsonResult renderJson(CmsJsonHandlerContext context) {

        try {
            try {
                CmsResource indexJson = context.getRootCms().readResource(
                    CmsStringUtil.joinPaths(context.getPath(), "index.json"));

                return new CmsJsonResult(indexJson);

            } catch (CmsVfsResourceNotFoundException e) {
                String levelsStr = context.getParameters().get("levels");
                int levels = 1;
                if (levelsStr != null) {
                    boolean invalidLevels = false;
                    try {
                        levels = Integer.parseInt(levelsStr);
                        if (levels < 1) {
                            invalidLevels = true;
                        }
                    } catch (NumberFormatException nfe) {
                        invalidLevels = true;
                    }
                    if (invalidLevels) {
                        return new CmsJsonResult(
                            "Invalid parameter value for 'levels'",
                            HttpServletResponse.SC_BAD_REQUEST);
                    }
                }
                CmsResource target = context.getResource();
                JSONObject result = folderListingJson(context, target, levels);
                return new CmsJsonResult(result, 200);
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return new CmsJsonResult(e.getLocalizedMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Formats folder listing as a JSON object, with the individual file names in the folder as keys.
     *
     * @param context the context
     * @param target the folder
     * @param levelsLeft the number of levels to format (if 1, only the direct children are listed)
     *
     * @return the JSON representation of the folder listing
     *
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong with the JSON processing
     */
    protected JSONObject folderListingJson(CmsJsonHandlerContext context, CmsResource target, int levelsLeft)
    throws CmsException, JSONException {

        List<CmsResource> children = context.getCms().readResources(target, CmsResourceFilter.DEFAULT, false);
        JSONObject result = new JSONObject(true);
        for (CmsResource resource : children) {
            JSONObject childEntry = formatResource(context, resource);
            if (resource.isFolder() && (levelsLeft > 1)) {
                JSONObject childrenJson = folderListingJson(context, resource, levelsLeft - 1);
                childEntry.put("children", childrenJson);
            }
            result.put(resource.getName(), childEntry);
        }
        return result;
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
        CmsResourceDataJsonHelper helper = new CmsResourceDataJsonHelper(
            context.getCms(),
            resource,
            context.getAccessPolicy()::checkPropertyAccess);
        helper.addPathAndLink(result);
        result.put("isFolder", resource.isFolder());
        boolean isContent = false;
        if (!resource.isFolder()) {
            isContent = CmsResourceTypeXmlContent.isXmlContent(resource);
        }
        result.put("attributes", helper.attributes());
        result.put("isXmlContent", Boolean.valueOf(isContent));
        helper.addProperties(result);
        return result;
    }
}
