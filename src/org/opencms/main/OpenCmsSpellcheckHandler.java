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

package org.opencms.main;

import org.opencms.file.CmsObject;
import org.opencms.search.solr.spellchecking.CmsSolrSpellchecker;
import org.opencms.site.CmsSite;
import org.opencms.util.CmsStringUtil;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;

/**
 * Handles spell check requests.<p>
 */
public class OpenCmsSpellcheckHandler extends HttpServlet implements I_CmsRequestHandler {

    /** A constant for the optional 'baseUri' parameter. */
    public static final String PARAM_BASE_URI = "baseUri";

    /** A constant for the HTTP 'referer'. */
    protected static final String HEADER_REFERER_KEY = "referer";

    /** The spell check handler name. */
    private static final String HANDLER_NAME = "SpellcheckDictionary";

    /** The handler names used by this request handler. */
    private static final String[] HANDLER_NAMES = new String[] {HANDLER_NAME};

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(OpenCmsSpellcheckHandler.class);

    /** The serial version id. */
    private static final long serialVersionUID = -6028091947126209813L;

    /**
     * Returns the path to the spell check handler.<p>
     *
     * @return the path to the spell check handler
     */
    public static String getSpellcheckHandlerPath() {

        return OpenCmsServlet.HANDLE_PATH + HANDLER_NAME;
    }

    /**
     * Checks if the spell check request handler is configured.<p>
     *
     * @return <code>true</code> if the spell check request handler is configured
     */
    public static boolean isSpellcheckingEnabled() {

        return OpenCmsCore.getInstance().getRequestHandler(HANDLER_NAME) != null;
    }

    /**
     * @see org.opencms.main.I_CmsRequestHandler#getHandlerNames()
     */
    public String[] getHandlerNames() {

        return HANDLER_NAMES;
    }

    /**
     * @see org.opencms.main.I_CmsRequestHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
     */
    public void handle(HttpServletRequest req, HttpServletResponse res, String name) throws IOException {

        CmsObject cms;
        try {
            cms = getCmsObject(req);

            CmsSolrSpellchecker dict = OpenCms.getSearchManager().getSolrDictionary(cms);
            if (dict != null) {
                dict.getSpellcheckingResult(res, req, cms);
            }
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Returns the CMS object.<p>
     *
     * @param req the request
     *
     * @return the CMS object
     *
     * @throws CmsException if something goes wrong
     */
    protected CmsObject getCmsObject(HttpServletRequest req) throws CmsException {

        CmsObject cms = OpenCmsCore.getInstance().initCmsObjectFromSession(req);
        // use the guest user as fall back
        if (cms == null) {
            cms = OpenCmsCore.getInstance().initCmsObject(OpenCms.getDefaultUsers().getUserGuest());
            String siteRoot = OpenCmsCore.getInstance().getSiteManager().matchRequest(req).getSiteRoot();
            cms.getRequestContext().setSiteRoot(siteRoot);
        }
        String baseUri = getBaseUri(req, cms);
        if (baseUri != null) {
            cms.getRequestContext().setUri(baseUri);
        }
        return cms;
    }

    /**
     * Returns the base URI.<p>
     *
     * @param req the servlet request
     * @param cms the CmsObject
     *
     * @return the base URI
     */
    private String getBaseUri(HttpServletRequest req, CmsObject cms) {

        String baseUri = req.getParameter(PARAM_BASE_URI);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(baseUri)) {
            String referer = req.getHeader(HEADER_REFERER_KEY);
            CmsSite site = OpenCms.getSiteManager().getSiteForSiteRoot(cms.getRequestContext().getSiteRoot());
            if (site != null) {
                String prefix = site.getServerPrefix(cms, "/") + OpenCms.getStaticExportManager().getVfsPrefix();
                if ((referer != null) && referer.startsWith(prefix)) {
                    baseUri = referer.substring(prefix.length());
                }
            }
        }
        return baseUri;
    }
}
