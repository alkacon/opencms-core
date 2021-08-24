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

package org.opencms.xml.xml2json.handler;

import org.opencms.file.CmsResource;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsStringUtil;
import org.opencms.xml.xml2json.CmsJsonRequest;
import org.opencms.xml.xml2json.CmsJsonResult;
import org.opencms.xml.xml2json.document.CmsJsonDocumentFolder;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Produces directory listings in JSON format.
 */
public class CmsJsonHandlerFolder implements I_CmsJsonHandler {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsJsonHandlerFolder.class);

    /**
     * @see org.opencms.xml.xml2json.handler.I_CmsJsonHandler#getOrder()
     */
    public double getOrder() {

        return 200;
    }

    /**
     * @see org.opencms.xml.xml2json.handler.I_CmsJsonHandler#matches(org.opencms.xml.xml2json.handler.CmsJsonHandlerContext)
     */
    public boolean matches(CmsJsonHandlerContext context) {

        return (context.getResource() != null) && context.getResource().isFolder();
    }

    /**
     * @see org.opencms.xml.xml2json.handler.I_CmsJsonHandler#renderJson(org.opencms.xml.xml2json.handler.CmsJsonHandlerContext)
     */
    public CmsJsonResult renderJson(CmsJsonHandlerContext context) {

        try {
            try {
                CmsResource indexJson = context.getRootCms().readResource(
                    CmsStringUtil.joinPaths(context.getPath(), "index.json"));

                return new CmsJsonResult(indexJson);

            } catch (CmsVfsResourceNotFoundException e) {
                CmsJsonRequest jsonRequest = new CmsJsonRequest(context, this);
                jsonRequest.validate();
                if (jsonRequest.hasErrors()) {
                    return new CmsJsonResult(jsonRequest.getErrorsAsJson(), HttpServletResponse.SC_BAD_REQUEST);
                }
                CmsJsonDocumentFolder jsonDocument = new CmsJsonDocumentFolder(jsonRequest);
                return new CmsJsonResult(jsonDocument.getJson(), HttpServletResponse.SC_OK);
            }
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return new CmsJsonResult(e.getLocalizedMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
